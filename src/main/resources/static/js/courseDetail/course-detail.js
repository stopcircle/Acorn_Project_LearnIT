document.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll("[data-action]").forEach(btn => {
    btn.addEventListener("click", () => {
      const action = btn.dataset.action;

      // ✅ 로그인 구현 전: login action은 로그인 페이지로 보내는 로직만 넣어둠
      if (action === "login") {
        alert("로그인이 필요합니다. (로그인 페이지 구현 후 이동)");
        // location.href = "/login";  // 로그인 페이지 생기면 이거 활성화
        return;
      }

      // ✅ 장바구니 / 구매하기는 confirm yes/no
      if (action === "cart") {
        const ok = confirm("장바구니에 담을까요?");
        if (ok) {
          alert("장바구니 담기 처리(추후 API 연결)");
          // TODO: fetch("/api/cart", {method:"POST"...})
        }
        return;
      }

      if (action === "buy") {
        const ok = confirm("구매하시겠습니까?");
        if (ok) {
          alert("구매 처리(결제 구현 후 이동)");
          // location.href = "/payment?courseId=...";
        }
      }
    });
  });
});
