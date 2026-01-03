// =====================================================
// course-detail-review.js (FULL - FIXED)
// ✅ 폼 기본 submit 방지 (페이지 이동 막기)
// ✅ courseId: hidden input 우선, 없으면 URL에서 파싱
// ✅ 등록/수정/삭제 + 목록/요약 로드
// ✅ 409(이미 등록)도 화면 메시지로 처리
// ✅ startEditReview / deleteReview 전역(window) 구현 (onclick 동작 보장)
// =====================================================

// ================================
// courseId
// ================================
function getCourseId() {
  // 1) DOM hidden input 우선
  const el = document.getElementById("courseId");
  if (el && el.value) return String(el.value).trim();

  // 2) URL 쿼리스트링에서 가져오기
  const params = new URLSearchParams(window.location.search);
  const q = params.get("courseId");
  return q && q.trim() ? q.trim() : null;
}

// ================================
// CSRF (meta)
// ================================
function getCsrf() {
  const token = document.querySelector('meta[name="_csrf"]')?.getAttribute("content") || "";
  const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content") || "";
  return { token, header };
}

// ================================
// date parse
// ================================
function toDateSafe(v) {
  if (!v) return null;

  if (typeof v === "string") {
    const d = new Date(v);
    return isNaN(d.getTime()) ? null : d;
  }

  if (Array.isArray(v)) {
    const [y, m, d, hh = 0, mm = 0, ss = 0] = v;
    const dt = new Date(y, m - 1, d, hh, mm, ss);
    return isNaN(dt.getTime()) ? null : dt;
  }

  return null;
}

// ================================
// fetch wrapper
// ================================
async function apiFetch(url, options = {}) {
  const baseHeaders = {
    Accept: "application/json",
    ...(options.headers || {}),
  };

  const res = await fetch(url, {
    credentials: "same-origin",
    ...options,
    headers: baseHeaders,
  });

  const text = await res.text();

  let data = null;
  try {
    data = JSON.parse(text);
  } catch {
    // 서버가 HTML(/error)로 보내면 여기로 옴
  }

  if (!res.ok) {
    console.error("[apiFetch]", res.status, url, text);
    return { res, data, text, ok: false };
  }

  return { res, data, text, ok: true };
}

// ================================
// UI message
// ================================
function setMessage(msg, type = "info") {
  const el = document.getElementById("reviewMessage");
  if (!el) return;

  el.textContent = msg || "";
  el.className = "";
  el.classList.add("review-message", `is-${type}`);
}

// XSS 방어(최소)
function escapeHtml(str) {
  return String(str)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

// ================================
// helpers: edit mode
// ================================
function ensureEditingHiddenInput() {
  const form = document.getElementById("reviewForm");
  if (!form) return null;

  let hid = document.getElementById("editingReviewId");
  if (!hid) {
    hid = document.createElement("input");
    hid.type = "hidden";
    hid.id = "editingReviewId";
    form.appendChild(hid);
  }
  return hid;
}

function clearEditMode() {
  document.getElementById("editingReviewId")?.remove();

  const btn = document.querySelector('#reviewForm button[type="submit"]');
  if (btn) btn.textContent = "수강평 등록";
}

function setSubmitButton(text) {
  const btn = document.querySelector('#reviewForm button[type="submit"]');
  if (btn) btn.textContent = text;
}

// ================================
// render reviews
// ================================
function renderReviews(reviews) {
  const list = document.getElementById("reviewList");
  if (!list) return;

  list.innerHTML = "";

  if (!reviews || reviews.length === 0) {
    list.innerHTML = "<p>아직 등록된 수강평이 없습니다.</p>";
    return;
  }

  reviews.forEach((r) => {
    const isMine = r.mine === true || r.isMine === true;

    const card = document.createElement("div");
    card.className = "review-card";
    card.setAttribute("data-review-id", r.reviewId ?? "");

    // ✅ 수정 버튼에서 쓰도록 값 저장
    card.setAttribute("data-rating", r.rating ?? "");
    card.setAttribute("data-content", r.content ?? "");

    const rating = Number(r.rating ?? 0);
    const safeRating = Math.min(5, Math.max(0, isNaN(rating) ? 0 : rating));

    const nickname = (r.nickname ?? "익명").toString();
    const content = (r.content ?? "").toString();

    const d = toDateSafe(r.updatedAt ?? r.createdAt);
    const dateText = d ? d.toLocaleDateString() : "";

    card.innerHTML = `
      <div class="review-top">
        <b>${escapeHtml(nickname)}</b>
        <span>${"★".repeat(safeRating)}${"☆".repeat(5 - safeRating)}</span>
      </div>
      <div class="review-date">${escapeHtml(dateText)}</div>
      <p>${escapeHtml(content)}</p>
      ${isMine ? `
        <div class="review-actions">
          <button type="button" onclick="startEditReview(${Number(r.reviewId)})">수정</button>
          <button type="button" onclick="deleteReview(${Number(r.reviewId)})">삭제</button>
        </div>` : ""}
    `;

    list.appendChild(card);
  });
}

// ================================
// load reviews + summary
// ================================
async function loadReviews() {
  const courseId = getCourseId();
  if (!courseId) return;

  const listRes = await apiFetch(`/api/reviews?courseId=${encodeURIComponent(courseId)}`);
  const reviews = Array.isArray(listRes.data)
    ? listRes.data
    : (listRes.data?.items ?? listRes.data?.data ?? []);

  renderReviews(reviews);

  const summary = await apiFetch(`/api/reviews/summary?courseId=${encodeURIComponent(courseId)}`);
  if (summary.ok && summary.data) {
    const avg = Number(summary.data.average ?? 0);
    const count = Number(summary.data.count ?? 0);

    const avgEl = document.getElementById("avgRating");
    const cntEl = document.getElementById("reviewCount");
    const starEl = document.getElementById("avgStars");

    if (avgEl) avgEl.innerText = (isNaN(avg) ? 0 : avg).toFixed(1);
    if (cntEl) cntEl.innerText = `${isNaN(count) ? 0 : count}개의 수강평`;
    if (starEl) {
      const rounded = Math.round(isNaN(avg) ? 0 : avg);
      starEl.innerText = "★".repeat(rounded) + "☆".repeat(5 - rounded);
    }
  }
}

// =====================================================
// ✅ 전역(window) 수정/삭제 (onclick에서 호출됨)
// =====================================================

// 수정 시작
window.startEditReview = function (reviewId) {
  const card = document.querySelector(`.review-card[data-review-id="${reviewId}"]`);
  if (!card) {
    setMessage("수정할 리뷰를 찾을 수 없습니다.", "error");
    return;
  }

  const wrapper = document.getElementById("reviewFormWrapper");
  if (wrapper) wrapper.style.display = "";

  const rating = String(card.getAttribute("data-rating") || "5");
  const content = String(card.getAttribute("data-content") || "");

  const ratingEl = document.getElementById("rating");
  const commentEl = document.getElementById("comment");

  if (ratingEl) ratingEl.value = rating;
  if (commentEl) commentEl.value = content;

  const hid = ensureEditingHiddenInput();
  if (hid) hid.value = String(reviewId);

  setSubmitButton("수강평 수정");
  setMessage("수정할 내용을 변경 후 '수강평 수정'을 눌러주세요.", "info");
};

// 삭제
window.deleteReview = async function (reviewId) {
  if (!confirm("정말 삭제하시겠습니까?")) return;

  setMessage("");

  const { token, header } = getCsrf();

  const res = await apiFetch(`/api/reviews/${reviewId}`, {
    method: "DELETE",
    headers: {
      ...(token && header ? { [header]: token } : {}),
    },
  });

  if (!res.ok) {
    if (res.res.status === 401) {
      setMessage("로그인 후 이용 가능합니다.", "warn");
      return;
    }
    setMessage(`삭제 실패 (HTTP ${res.res.status})`, "error");
    return;
  }

  setMessage("삭제되었습니다.", "ok");
  clearEditMode();
  await loadReviews();
};

// ================================
// submit create/update review (IMPORTANT)
// ================================
async function bindReviewForm() {
  const form = document.getElementById("reviewForm");
  if (!form) return;

  // ✅ 중복 바인딩 방지
  if (form.dataset.bound === "true") return;
  form.dataset.bound = "true";

  form.addEventListener("submit", async (e) => {
    // ✅ 페이지 이동(폼 기본 제출) 막기
    e.preventDefault();
    e.stopPropagation();

    setMessage("");

    const courseId = getCourseId();
    if (!courseId) {
      setMessage("courseId를 찾을 수 없습니다.", "error");
      return;
    }

    const rating = Number(document.getElementById("rating")?.value ?? 0);
    const content = (document.getElementById("comment")?.value ?? "").trim();

    if (!content) {
      setMessage("후기를 입력해 주세요.", "error");
      return;
    }

    const { token, header } = getCsrf();
    const editingId = document.getElementById("editingReviewId")?.value?.trim();

    // ✅ 수정 모드면 PUT
    if (editingId) {
      const up = await apiFetch(`/api/reviews/${encodeURIComponent(editingId)}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          ...(token && header ? { [header]: token } : {}),
        },
        body: JSON.stringify({
          rating: isNaN(rating) ? 0 : rating,
          content,
        }),
      });

      if (!up.ok) {
        if (up.res.status === 401) {
          setMessage("로그인 후 이용 가능합니다.", "warn");
        } else {
          const msg =
            up.data?.message ||
            up.data?.error ||
            `수정 실패 (HTTP ${up.res.status})`;
          setMessage(msg, "error");
        }
        return;
      }

      setMessage("수강평이 수정되었습니다.", "ok");
      const commentEl = document.getElementById("comment");
      if (commentEl) commentEl.value = "";

      clearEditMode();
      await loadReviews();
      return;
    }

    // ✅ 등록 모드면 POST
    const res = await apiFetch(`/api/reviews?courseId=${encodeURIComponent(courseId)}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        ...(token && header ? { [header]: token } : {}),
      },
      body: JSON.stringify({
        rating: isNaN(rating) ? 0 : rating,
        content,
      }),
    });

    if (!res.ok) {
      const msg =
        res.data?.message ||
        res.data?.error ||
        (typeof res.text === "string" && res.text.includes("이미")
          ? "이미 이 강의에 수강평이 등록되었습니다."
          : "") ||
        `등록 실패 (HTTP ${res.res.status})`;

      if (res.res.status === 409) {
        setMessage("이미 이 강의에 수강평이 등록되었습니다. (내 리뷰에서 '수정'을 눌러 수정하세요)", "warn");
      } else if (res.res.status === 401) {
        setMessage("로그인 후 이용 가능합니다.", "warn");
      } else {
        setMessage(msg, "error");
      }
      return;
    }

    // 성공
    setMessage("수강평이 등록되었습니다.", "ok");
    const commentEl = document.getElementById("comment");
    if (commentEl) commentEl.value = "";

    await loadReviews();
  });
}

// ================================
// init
// ================================
document.addEventListener("DOMContentLoaded", async () => {
  // 로그인 여부로 폼 노출
  const wrapper = document.getElementById("reviewFormWrapper");
  if (wrapper) {
    wrapper.style.display = window.IS_LOGGED_IN === true ? "" : "none";
  }

  await bindReviewForm();
  await loadReviews();
});
