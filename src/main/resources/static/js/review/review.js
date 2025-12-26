// ================================
// 로그인 정보 (/api/me 로 확인)
// ================================
window.LOGIN_USER_ID = null;
window.LOGIN_USER_NICKNAME = null;

async function fetchMe() {
    try {
        const res = await fetch("/api/me");
        if (!res.ok) {
            console.error("me API 실패", res.status);
            return;
        }

        const data = await res.json();

        if (data.loggedIn) {
            window.LOGIN_USER_ID = data.userId;
            window.LOGIN_USER_NICKNAME = data.nickname;
        }
    } catch (e) {
        console.error("me API 에러", e);
    }
}

// ================================
// courseId 가져오기 (hidden input)
// ================================
function getCourseId() {
    const el = document.getElementById("courseId");
    if (!el) {
        console.error("hidden input #courseId를 찾을 수 없습니다.");
        return null;
    }
    return el.value;
}

// ================================
// 리뷰 목록 렌더링
// ================================
function renderReviews(reviews) {
    const container = document.getElementById("reviewList");
    container.innerHTML = "";

    if (!reviews || reviews.length === 0) {
        container.innerHTML = "<p>아직 등록된 수강평이 없습니다.</p>";
        return;
    }

    const loginUserId = window.LOGIN_USER_ID
        ? Number(window.LOGIN_USER_ID)
        : null;

    reviews.forEach(r => {
        const card = document.createElement("div");
        card.className = "review-card";
        card.dataset.reviewId = r.reviewId;
        card.dataset.rating = r.rating;
        card.dataset.content = r.content;
        card.dataset.userId = r.userId;   // 내 리뷰 구분용

        const isMine = loginUserId !== null && loginUserId === Number(r.userId);

        card.innerHTML = `
            <div class="review-view">
                <div class="review-top">
                    <b>${r.nickname ?? "익명"}</b>
                    <span>★ ${r.rating}</span>
                </div>
                <p>${r.content}</p>

                ${isMine ? `
                <div class="review-actions">
                    <button type="button" onclick="startEditReview(${r.reviewId})">수정</button>
                    <button type="button" onclick="deleteReview(${r.reviewId})">삭제</button>
                </div>` : ""}
            </div>

            <div class="review-edit" style="display:none;">
                <div>
                    <label>별점</label>
                    <select class="edit-rating">
                        <option value="5">5</option>
                        <option value="4">4</option>
                        <option value="3">3</option>
                        <option value="2">2</option>
                        <option value="1">1</option>
                    </select>
                </div>

                <textarea class="edit-content" rows="4"></textarea>

                <button type="button" onclick="saveEditReview(${r.reviewId})">저장</button>
                <button type="button" onclick="cancelEditReview(${r.reviewId})">취소</button>
            </div>
        `;

        container.appendChild(card);
    });
}

// ================================
// 리뷰 목록 불러오기 (GET /api/reviews)
// ================================
async function loadReviews() {
    const courseId = getCourseId();
    if (!courseId) return;

    try {
        const res = await fetch(`/api/reviews?courseId=${courseId}`);
        if (!res.ok) {
            throw new Error("리뷰 조회 실패: " + res.status);
        }

        const reviews = await res.json();
        renderReviews(reviews);
    } catch (e) {
        console.error(e);
        const container = document.getElementById("reviewList");
        container.innerHTML = "<p>리뷰를 불러오는 중 오류가 발생했습니다.</p>";
    }
}

// ================================
// 내 리뷰 위치로 스크롤 (중복 등록 시)
// ================================
function scrollToMyReview() {
    const loginUserId = window.LOGIN_USER_ID
        ? Number(window.LOGIN_USER_ID)
        : null;

    if (!loginUserId) return;

    const myCard = document.querySelector(
        `.review-card[data-user-id="${loginUserId}"]`
    );

    if (myCard) {
        myCard.scrollIntoView({ behavior: "smooth", block: "center" });
        myCard.classList.add("highlight-review");
        setTimeout(() => myCard.classList.remove("highlight-review"), 2000);
    }
}

// ================================
// 리뷰 등록 (POST /api/reviews?courseId=...)
// ================================
async function submitReview(event) {
    event.preventDefault(); // 폼 기본 submit 막기

    const courseId = getCourseId();
    const rating = document.getElementById("rating").value;
    const comment = document.getElementById("comment").value;
    const msgEl = document.getElementById("reviewMessage");

    if (!courseId) {
        msgEl.innerText = "courseId가 없습니다.";
        return;
    }

    if (!comment.trim()) {
        msgEl.innerText = "후기를 입력해주세요.";
        return;
    }

    try {
        const res = await fetch(`/api/reviews?courseId=${courseId}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                rating: Number(rating),
                content: comment
            })
        });

        if (res.status === 401) {
            msgEl.innerText = "로그인 후 이용 가능합니다.";
            return;
        }

        if (!res.ok) {
            const text = await res.text();

            if (text.includes("이미 이 강의에 수강평이 등록되었습니다.")) {
                alert("이미 이 강의에 수강평이 등록되어 있습니다.\n작성한 수강평 위치로 이동합니다.");
                scrollToMyReview();
                return;
            }

            msgEl.innerText = "리뷰 등록 실패: " + text;
            return;
        }

        msgEl.innerText = "리뷰가 등록되었습니다.";

        // 새 목록 로드
        await loadReviews();

        // 폼 초기화
        document.getElementById("rating").value = "5";
        document.getElementById("comment").value = "";

    } catch (e) {
        console.error(e);
        msgEl.innerText = "리뷰 등록 중 오류가 발생했습니다.";
    }
}

// ================================
// 수정 모드 on/off
// ================================
function startEditReview(reviewId) {
    const card = document.querySelector(`.review-card[data-review-id="${reviewId}"]`);
    if (!card) return;

    const viewEl = card.querySelector(".review-view");
    const editEl = card.querySelector(".review-edit");

    const ratingSelect = editEl.querySelector(".edit-rating");
    const contentTextarea = editEl.querySelector(".edit-content");

    ratingSelect.value = card.dataset.rating;
    contentTextarea.value = card.dataset.content;

    viewEl.style.display = "none";
    editEl.style.display = "block";
}

function cancelEditReview(reviewId) {
    const card = document.querySelector(`.review-card[data-review-id="${reviewId}"]`);
    if (!card) return;

    const viewEl = card.querySelector(".review-view");
    const editEl = card.querySelector(".review-edit");

    editEl.style.display = "none";
    viewEl.style.display = "block";
}

// ================================
// 저장(수정) - PUT /api/reviews/{reviewId}
// ================================
async function saveEditReview(reviewId) {
    const card = document.querySelector(`.review-card[data-review-id="${reviewId}"]`);
    if (!card) return;

    const rating = Number(card.querySelector(".edit-rating").value);
    const content = card.querySelector(".edit-content").value.trim();
    const msgEl = document.getElementById("reviewMessage");

    if (!content) {
        msgEl.innerText = "내용을 입력해주세요.";
        return;
    }

    try {
        const res = await fetch(`/api/reviews/${reviewId}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ rating, content })
        });

        if (!res.ok) {
            const text = await res.text();
            msgEl.innerText = "리뷰 수정 실패: " + text;
            return;
        }

        msgEl.innerText = "리뷰가 수정되었습니다.";
        await loadReviews();
    } catch (e) {
        console.error(e);
        msgEl.innerText = "리뷰 수정 중 오류가 발생했습니다.";
    }
}

// ================================
// 삭제 - DELETE /api/reviews/{reviewId}
// ================================
async function deleteReview(reviewId) {
    const msgEl = document.getElementById("reviewMessage");

    if (!confirm("정말 이 수강평을 삭제하시겠습니까?")) return;

    try {
        const res = await fetch(`/api/reviews/${reviewId}`, {
            method: "DELETE"
        });

        if (!res.ok) {
            const text = await res.text();
            msgEl.innerText = "리뷰 삭제 실패: " + text;
            return;
        }

        msgEl.innerText = "리뷰가 삭제되었습니다.";
        await loadReviews();
    } catch (e) {
        console.error(e);
        msgEl.innerText = "리뷰 삭제 중 오류가 발생했습니다.";
    }
}

// ================================
// 확인
// ================================
async function checkReviewFormVisible() {
    const courseId = getCourseId();
    if (!courseId) return;

    try {
        const res = await fetch(`/api/reviews/check-enrollment?courseId=${courseId}`);
        if (!res.ok) {
            console.error("check-enrollment 실패", res.status);
            return;
        }

        const data = await res.json(); // { loggedIn, enrolled }

        const formWrapper = document.getElementById("reviewFormWrapper");
        if (!formWrapper) return;

        if (data.loggedIn && data.enrolled) {
            formWrapper.style.display = "block";  // 로그인 + 수강자 → 폼 보이기
        } else {
            formWrapper.style.display = "none";   // 나머지 → 폼 숨기기
        }
    } catch (e) {
        console.error("checkReviewFormVisible 에러", e);
    }
}
// ================================
// 페이지 로딩 시
// ================================
document.addEventListener("DOMContentLoaded", async () => {
    await fetchMe();           // 로그인 정보
    await loadReviews();       // 리뷰 목록 (누구나 보기)
    await checkReviewFormVisible(); // 폼 노출 여부
});
