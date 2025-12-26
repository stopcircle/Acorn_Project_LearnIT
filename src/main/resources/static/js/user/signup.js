document.addEventListener("DOMContentLoaded", () => {
    // 중복확인 버튼 이벤트 리스너
    const checkDuplicateBtn = document.querySelector(".btn-check-duplicate");
    if (checkDuplicateBtn) {
        checkDuplicateBtn.addEventListener("click", checkUsername);
    }

    // 취소 버튼 이벤트 리스너
    const cancelBtn = document.querySelector(".btn-cancel");
    if (cancelBtn) {
        cancelBtn.addEventListener("click", () => {
            location.href = '/home';
        });
    }
});

function checkUsername() {
    const username = document.getElementById('name').value;
    if (!username) {
        alert('아이디를 입력해주세요.');
        return;
    }
    
    fetch('/api/check-username?username=' + username)
        .then(response => response.json())
        .then(data => {
            if (data.exists) {
                alert('이미 사용 중인 아이디입니다.');
            } else {
                alert('사용 가능한 아이디입니다.');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('중복 확인 중 오류가 발생했습니다.');
        });
}

