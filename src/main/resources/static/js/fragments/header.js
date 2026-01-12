document.addEventListener("DOMContentLoaded", function () {
  const btnMenu = document.getElementById("btn-menu");
  const mobileNav = document.getElementById("mobile-nav");
  const overlay = document.getElementById("mobile-nav-overlay");
  const btnClose = document.getElementById("mobile-nav-close");

  // -----------------------------
  // Cart badge: 실시간 갱신
  // - 서버에서 cartCount를 th:if로 렌더링하면(0일 때 DOM이 없어짐) JS로 갱신 불가
  // - header.html에서 항상 span을 렌더링하도록 수정했고,
  //   여기서 /cart/count를 호출해 숫자 + 노출/숨김을 동기화한다.
  // -----------------------------
  const badgeEls = () => Array.from(document.querySelectorAll(".cart-count-badge"));

  function setBadgeCount(count) {
    const n = Number.isFinite(count) ? count : 0;
    badgeEls().forEach((el) => {
      el.textContent = String(n);
      el.classList.toggle("is-hidden", n <= 0);
    });
  }

  async function refreshCartBadge() {
    try {
      const res = await fetch("/cart/count", {
        method: "GET",
        headers: { Accept: "text/plain, application/json" },
        cache: "no-store",
      });

      if (!res.ok) return;
      const text = (await res.text()).trim();
      const count = parseInt(text, 10);
      if (!Number.isNaN(count)) setBadgeCount(count);
    } catch (e) {
      // 네트워크/세션 등으로 실패해도 UI는 기존 값을 유지
    }
  }

  function openNav() {
    if (!mobileNav) return;
    mobileNav.classList.add("is-open");
    mobileNav.setAttribute("aria-hidden", "false");
  }

  function closeNav() {
    if (!mobileNav) return;
    mobileNav.classList.remove("is-open");
    mobileNav.setAttribute("aria-hidden", "true");
  }

  if (btnMenu) btnMenu.addEventListener("click", openNav);
  if (overlay) overlay.addEventListener("click", closeNav);
  if (btnClose) btnClose.addEventListener("click", closeNav);

  // 최초 1회 갱신
  refreshCartBadge();

  // 페이지 내에서 장바구니가 변경되면(무한스크롤 리스트/상세페이지 등) 이벤트로 갱신
  document.addEventListener("cart:updated", refreshCartBadge);

  // 탭 복귀 시(포커스) 한 번 더 동기화
  window.addEventListener("focus", refreshCartBadge);

  // 예외 케이스(폼 submit 등으로 변경)도 커버하기 위한 가벼운 폴링
  setInterval(refreshCartBadge, 5000);
});
