document.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll("[data-action]").forEach(btn => {
    btn.addEventListener("click", () => {
      const action = btn.dataset.action;

      if (action === "login") {
        alert("로그인이 필요합니다. (로그인 페이지 구현 후 이동)");
        return;
      }

      if (action === "enrollGo") {
        const ok = confirm("수강신청 페이지로 이동하시겠습니까?");
        if (ok) location.href = "/Enroll"; // 너희 수강신청 페이지 URL로 바꿔
        return;
      }

if (action === "cartGo") {
  const ok = confirm("장바구니에 담고 이동하시겠습니까?");
  if (!ok) return;

  const params = new URLSearchParams(location.search);
  const courseId = params.get("courseId");

  fetch("/cart/add?courseId=" + courseId, { method: "POST" })
    .then(res => res.text())
    .then(text => {
      if (text === "DUP") {
        alert("이미 장바구니에 담긴 강의입니다.");
        location.href = "/cart"; // 원하면 여기서 이동 안 시켜도 됨
        return;
      }
      location.href = "/cart";
    })
    .catch(() => alert("장바구니 담기 실패"));
  return;
}



      if (action === "payGo") {
        const ok = confirm("결제하기 화면으로 이동하시겠습니까?");
        if (ok) {
          location.href = "/Payment"; // 임시 페이지로 바꿔도 됨
        }
        return;
      }
    });
  });
});
