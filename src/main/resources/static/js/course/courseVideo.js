// HTML hidden input에서 정보 가져오기 (URL, CourseID, ChapterID)
const videoInput = document.getElementById('video-url');
const courseInput = document.getElementById('course-id');
const chapterInput = document.getElementById('chapter-id');

const dbVideoUrl = videoInput ? videoInput.value : null;
const currentCourseId = courseInput ? courseInput.value : null;
const currentChapterId = chapterInput ? chapterInput.value : null;

// ID 추출 함수 (모든 유튜브 주소 형식 대응)
function getVideoId(url) {
    if (!url) return null;
    try {
        const regExp = /^.*(youtu.be\/|v\/|u\/\w\/|embed\/|watch\?v=|&v=)([^#&?]*).*/;
        const match = url.match(regExp);
        if (match && match[2].length === 11) return match[2];
        const urlObj = new URL(url);
        return urlObj.searchParams.get("v");
    } catch (e) {
        console.error("URL 파싱 실패:", e);
        return null;
    }
}

const currentVideoId = getVideoId(dbVideoUrl);
const storageKey = "yt-time-" + currentVideoId;
let player;

// 저장된 시간 불러오기
let savedTime = 0;
try {
    const time = localStorage.getItem(storageKey);
    savedTime = (time && !isNaN(time)) ? Number(time) : 0;
} catch (e) {
    savedTime = 0;
}

// 유튜브 API 준비되면 실행 (window 전역 객체에 등록)
window.onYouTubeIframeAPIReady = function() {
    if (!currentVideoId) return;

    player = new YT.Player('player', {
        height: '100%',
        width: '100%',
        videoId: currentVideoId,
        playerVars: {
            'start': savedTime,
            'rel': 0,
            'autoplay': 0
        },
        events: {
            'onStateChange': onPlayerStateChange
        }
    });
};

// 상태 변화 감지
function onPlayerStateChange(event) {
    if (event.data === YT.PlayerState.PAUSED) {
        const currentTime = Math.floor(player.getCurrentTime());
        saveCurrentTime(currentTime);
        saveProgressToServer(currentTime);
    }
    if (event.data === YT.PlayerState.ENDED) {
        localStorage.removeItem(storageKey);
    }
}

// 로컬 스토리지에 시간 저장 (이어보기용)
function saveCurrentTime(time) {    //추가
    if(time === undefined && player) time = Math.floor(player.getCurrentTime());
    localStorage.setItem(storageKey, time);
}

// 서버로 진도율 전송 (DB 저장용)
function saveProgressToServer(time) {
    if (!currentCourseId || !currentChapterId) return;

    const payload = { playTime: time };
    const url = `/course/log?courseId=${currentCourseId}&chapterId=${currentChapterId}`;

    fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
        keepalive: true
    })
        .then(response => {
            if (!response.ok) console.error("서버 저장 실패");
        })
        .catch(error => console.error("통신 에러:", error));
}

// 자동 저장 (10초마다) - 로컬스토리지 & 서버 둘 다 저장
setInterval(() => {
    if (player && player.getPlayerState && player.getPlayerState() === YT.PlayerState.PLAYING) {
        const currentTime = Math.floor(player.getCurrentTime());

        // 1. 이어보기 저장
        saveCurrentTime();

        // 2. 서버 DB로 진도율 전송
        saveProgressToServer(currentTime);
    }
}, 10000);

function handlePageExit() {
    if (player && typeof player.getCurrentTime === 'function') {
        const currentTime = Math.floor(player.getCurrentTime());

        // 0초 이상일 때만 저장
        if (currentTime > 0) {
            saveCurrentTime(currentTime);      // 로컬 스토리지 저장
            saveProgressToServer(currentTime); // 서버 DB 저장
            console.log("페이지 이탈 감지 저장:", currentTime);
        }
    }
}

// 브라우저 닫기, 새로고침, 탭 닫기 감지
window.addEventListener('beforeunload', handlePageExit);

// 모바일: 탭 전환, 최소화, 홈 화면 이동
document.addEventListener('visibilitychange', () => {
    if (document.visibilityState === 'hidden') {
        handlePageExit();
    }
});

// API가 먼저 로드되었을 경우를 대비해 수동 실행
if (window.YT && window.YT.Player && typeof window.YT.Player === 'function') {
    window.onYouTubeIframeAPIReady();
}

// 현재 열린 탭 ID 기억
let currentActiveTab = null;

// 패널 열기 & 탭 전환 함수
function openPanel(tabName) {
    const wrapper = document.getElementById('side-panel-wrapper');
    const contentId = 'content-' + tabName;
    const targetContent = document.getElementById(contentId);

    // 1) 이미 열려있는 탭을 또 누르면 -> 닫기
    if (wrapper.classList.contains('open') && currentActiveTab === tabName) {
        closePanel();
        return;
    }

    // 2) 모든 콘텐츠 숨기기 (초기화)
    const allContents = document.querySelectorAll('.panel-content-box');
    allContents.forEach(el => el.style.display = 'none');

    // 3) 내가 누른 콘텐츠만 보여주기
    if (targetContent) {
        targetContent.style.display = 'flex'; // flex로 보여야 내부 레이아웃 유지됨
    }

    // 4) 패널이 닫혀있다면 열기 (너비 확장)
    if (!wrapper.classList.contains('open')) {
        wrapper.classList.add('open');
    }

    // 5) 현재 탭 갱신
    currentActiveTab = tabName;
}

// 패널 닫기 함수 (X 버튼용)
function closePanel() {
    const wrapper = document.getElementById('side-panel-wrapper');
    wrapper.classList.remove('open');
    currentActiveTab = null; // 상태 초기화
}