document.addEventListener("DOMContentLoaded", () => {
  // ✅ 모달 DOM
  const modalDup = document.getElementById("modal-cart-dup");
  const btnDupYes = document.getElementById("btn-cart-dup-yes");
  const btnDupNo  = document.getElementById("btn-cart-dup-no");
// ✅ 신규 담김 모달
const modalAdded = document.getElementById("modal-cart-added");
const btnAddedYes = document.getElementById("btn-cart-added-yes");
const btnAddedNo  = document.getElementById("btn-cart-added-no");

  const modalIncomplete = document.getElementById("modal-incomplete");
  const btnIncompleteOk = document.getElementById("btn-incomplete-ok");

  // ✅ 중복 모달: 예 -> 장바구니 이동 / 아니오 -> 닫기
  if (btnDupYes) btnDupYes.addEventListener("click", () => {
    location.href = "/cart";
  });
  if (btnDupNo) btnDupNo.addEventListener("click", () => {
    if (modalDup) modalDup.style.display = "none";
  });
  // ✅ 신규 담김 모달 버튼
  if (btnAddedYes) btnAddedYes.addEventListener("click", () => {
    location.href = "/cart";
  });
  if (btnAddedNo) btnAddedNo.addEventListener("click", () => {
    modalAdded.style.display = "none";
  });


  // ✅ 이어보기(미완성) 모달 닫기
  if (btnIncompleteOk) btnIncompleteOk.addEventListener("click", () => {
    if (modalIncomplete) modalIncomplete.style.display = "none";
  });

  document.querySelectorAll("[data-action]").forEach(btn => {
    btn.addEventListener("click", async () => {
      const action = btn.dataset.action;

      // 1) 비로그인: 로그인 필요
      if (action === "login") {
        alert("로그인이 필요합니다. (로그인 페이지 구현 후 이동)");
        return;
      }

      // 2) 수강신청
      if (action === "enroll") {
        const courseId = btn.dataset.courseId;
        alert("수강신청 페이지로 이동(추후 연결) courseId=" + courseId);
        return;
      }

      // 2) 장바구니 담기
     // ✅ 장바구니 담기
     if (action === "cartAdd") {
       const courseId = btn.dataset.courseId;

       try {
         const res = await fetch("/cart/add", {
           method: "POST",
           headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
           body: new URLSearchParams({ courseId })
         });

         const text = await res.text(); // OK or DUP

         if (text === "DUP") {
           // ✅ 중복이면: 기존 모달 그대로
           modalDup.style.display = "flex";
           return;
         }

         if (text === "OK") {
           // ✅ 신규 담김이면: "이동할까요?" 모달 띄우기
           modalAdded.style.display = "flex";
           return;
         }

         alert("알 수 없는 응답: " + text);

       } catch (e) {
         alert("장바구니 담기에 실패했습니다. 서버 상태를 확인하세요.");
       }
       return;
     }


      // 3) 수강중: 이어보기
      if (action === "continue") {
        if (modalIncomplete) modalIncomplete.style.display = "flex";
        return;
      }
    });
  });
});
