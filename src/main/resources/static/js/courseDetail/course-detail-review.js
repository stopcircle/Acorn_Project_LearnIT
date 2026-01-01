// ================================
// courseId
// ================================
function getCourseId() {
  return document.getElementById("courseId")?.value ?? null;
}

// ================================
// date parse
// ================================
function toDateSafe(v) {
  if (!v) return null;
  if (typeof v === "string") return new Date(v);
  if (Array.isArray(v)) {
    const [y,m,d,hh=0,mm=0,ss=0] = v;
    return new Date(y, m-1, d, hh, mm, ss);
  }
  return null;
}

// ================================
// fetch wrapper
// ================================
async function apiFetch(url, options={}) {
  const res = await fetch(url, { credentials:"same-origin", ...options });
  const text = await res.text();
  let data = null;
  try { data = JSON.parse(text); } catch {}
  return { res, data, text };
}

// ================================
// render reviews
// ================================
function renderReviews(reviews){
  const list = document.getElementById("reviewList");
  if (!list) return;
  list.innerHTML = "";

  if (!reviews || reviews.length === 0) {
    list.innerHTML = "<p>아직 등록된 수강평이 없습니다.</p>";
    return;
  }

  reviews.forEach(r => {
    const card = document.createElement("div");
    card.className = "review-card";
    card.setAttribute("data-review-id", r.reviewId);
    card.setAttribute("data-rating", r.rating);
    card.setAttribute("data-content", r.content);

    card.innerHTML = `
      <div class="review-top">
        <b>${r.nickname}</b>
        <span>${"★".repeat(r.rating)}${"☆".repeat(5-r.rating)}</span>
      </div>
      <div class="review-date"></div>
      <p>${r.content}</p>
      ${r.mine ? `
      <div class="review-actions">
        <button onclick="startEditReview(${r.reviewId})">수정</button>
        <button onclick="deleteReview(${r.reviewId})">삭제</button>
      </div>` : ""}
    `;

    const d = toDateSafe(r.updatedAt ?? r.createdAt);
    card.querySelector(".review-date").innerText =
      d ? d.toLocaleDateString() : "";

    list.appendChild(card);
  });
}

// ================================
// load
// ================================
async function loadReviews(){
  const courseId = getCourseId();
  if (!courseId) return;

  const { data } = await apiFetch(`/api/reviews?courseId=${courseId}`);
  renderReviews(data);

  const summary = await apiFetch(`/api/reviews/summary?courseId=${courseId}`);
  if (summary.data) {
    const avg = summary.data.average ?? 0;
    const count = summary.data.count ?? 0;

    document.getElementById("avgRating").innerText = avg.toFixed(1);
    document.getElementById("reviewCount").innerText = `${count}개의 수강평`;
    document.getElementById("avgStars").innerText =
      "★".repeat(Math.round(avg)) + "☆".repeat(5-Math.round(avg));
  }
}

// ================================
// init
// ================================
document.addEventListener("DOMContentLoaded", async () => {
  console.log("test");
  await loadReviews();
});
