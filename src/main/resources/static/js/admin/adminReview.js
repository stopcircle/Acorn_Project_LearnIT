/**
 * 관리자 리뷰 페이지 - 아코디언 토글 기능
 */

/**
 * 아코디언 토글 함수 (버튼 클릭용)
 * @param {HTMLElement} element - 클릭한 버튼 요소
 */
function toggleAccordion(element) {
    const reviewId = element.getAttribute('data-review-id');
    if (!reviewId) {
        return;
    }
    toggleAccordionByReviewId(reviewId);
}

/**
 * 아코디언 토글 함수 (행 클릭용)
 * @param {HTMLElement} row - 클릭한 행 요소
 */
function toggleAccordionByRow(row) {
    const reviewId = row.getAttribute('data-review-id');
    if (!reviewId) {
        return;
    }
    toggleAccordionByReviewId(reviewId);
}

/**
 * 리뷰 ID로 아코디언 토글
 * @param {string} reviewId - 리뷰 ID
 */
function toggleAccordionByReviewId(reviewId) {
    // 해당 리뷰의 아코디언 행 찾기
    const accordionRow = document.querySelector(`.accordion-row[data-review-id="${reviewId}"]`);
    if (!accordionRow) {
        return;
    }

    // 현재 활성 상태 확인
    const isActive = accordionRow.classList.contains('active');

    // 해당 아코디언만 토글 (다른 아코디언은 유지)
    if (isActive) {
        accordionRow.classList.remove('active');
    } else {
        accordionRow.classList.add('active');
    }
}

// DOM 로드 완료 후 이벤트 리스너 등록
document.addEventListener('DOMContentLoaded', function() {
    // 아코디언 토글 버튼에 이벤트 리스너 추가 (인라인 onclick 대신)
    document.querySelectorAll('.btn-toggle-accordion').forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            toggleAccordion(this);
        });
    });
});

/**
 * 댓글 상태 필터 드롭다운 토글
 * @param {Event} event - 클릭 이벤트
 */
function toggleStatusFilter(event) {
    event.stopPropagation();
    const dropdown = document.getElementById('statusFilterDropdown');
    const labelRect = event.target.closest('.status-filter-header').getBoundingClientRect();

    dropdown.style.display = dropdown.style.display === 'block' ? 'none' : 'block';

    if (dropdown.style.display === 'block') {
        dropdown.style.left = `${labelRect.left}px`;
        dropdown.style.top = `${labelRect.bottom + 8}px`;
    }
}

/**
 * 댓글 상태로 필터링
 * @param {string} status - 필터링할 상태 (전체: '', Active, Approved, Rejected)
 */
function filterByStatus(status) {
    const form = document.getElementById('filterForm');
    form.elements['commentStatus'].value = status;
    form.submit();
}

// 드롭다운 외부 클릭 시 닫기
document.addEventListener('click', function(event) {
    const dropdown = document.getElementById('statusFilterDropdown');
    const label = document.querySelector('.status-filter-label');
    if (dropdown && label && !dropdown.contains(event.target) && !label.contains(event.target)) {
        dropdown.style.display = 'none';
    }
});
