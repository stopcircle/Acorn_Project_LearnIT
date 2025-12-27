document.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll("[data-action]").forEach(btn => {
    btn.addEventListener("click", async () => {
      const action = btn.dataset.action;

      if (action === "login") {
        alert("로그인이 필요합니다. (로그인 페이지 구현 후 이동)");
        return;
      }

      // ✅ 수강신청 가기
      if (action === "enroll") {
        const courseId = btn.dataset.courseId;
        alert("수강신청 페이지로 이동(추후 연결) courseId=" + courseId);
        // location.href = `/enroll?courseId=${courseId}`;
        return;
      }

      // ✅ 장바구니 담기
      if (action === "cartAdd") {
        const courseId = btn.dataset.courseId;

        try {
          const res = await fetch("/cart/add", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
            body: new URLSearchParams({ courseId })
          });

          const text = (await res.text()).trim(); // "OK" or "DUP"

          // ✅ 중복이면 여기서 팝업 띄우고 끝
          if (text === "DUP") {
            alert("이미 장바구니에 담긴 강의입니다.");
            return;
          }

          // ✅ 신규로 담겼으면 이동 여부 confirm
          if (text === "OK") {
            const ok = confirm("장바구니에 담았습니다.\n장바구니 페이지로 이동하시겠습니까?");
            if (ok) location.href = "/cart";
            return;
          }

          // ✅ 예상 못한 응답
          alert("장바구니 처리 중 알 수 없는 응답입니다: " + text);

        } catch (e) {
          alert("장바구니 담기에 실패했습니다. 서버 상태를 확인하세요.");
        }
        return;
      }
    });
  });
});
