// 마이페이지 레이아웃 공통 JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // 네비게이션 active 클래스 처리
    const currentPath = window.location.pathname;
    const navLinks = document.querySelectorAll('.mypage-mobile-nav .nav-link');
    
    navLinks.forEach(link => {
        const linkPath = link.getAttribute('data-path');
        // /mypage/dashboard 또는 /mypage로 접근 시 대시보드 활성화
        if (linkPath === '/mypage' && (currentPath === '/mypage' || currentPath === '/mypage/')) {
            link.classList.add('active');
        } else if (linkPath !== '/mypage' && currentPath.startsWith(linkPath)) {
            link.classList.add('active');
        }
    });

    // 이미지 에러 처리 (fallback 이미지)
    const imagesWithFallback = document.querySelectorAll('img[data-fallback]');
    imagesWithFallback.forEach(img => {
        img.addEventListener('error', function() {
            const fallback = this.getAttribute('data-fallback');
            if (fallback && this.src !== fallback) {
                this.src = fallback;
            }
        });
    });
});

