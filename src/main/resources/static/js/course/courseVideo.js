/* =========================================
   [1] 전역 변수 및 초기화
   ========================================= */
const videoInput = document.getElementById('video-url');
const courseInput = document.getElementById('course-id');
const chapterInput = document.getElementById('chapter-id');

const dbVideoUrl = videoInput ? videoInput.value : null;
const currentCourseId = courseInput ? courseInput.value : null;
let currentChapterId = chapterInput ? chapterInput.value : null;

// 상태 관리 변수
let monacoEditor = null;
let player = null;
let quizData = null;      // 퀴즈 데이터
let userAnswers = [];     // 사용자 답안
let currentQIndex = 0;    // 현재 문제 번호

// 초기 로드 시 퀴즈인지 체크
document.addEventListener('DOMContentLoaded', () => {
    if (dbVideoUrl === 'QUIZ') {
        playContent(currentChapterId, 'QUIZ');
    }
});


/* =========================================
   [2] 유튜브 플레이어 (IFrame API)
   ========================================= */
function getVideoId(url) {
    if (!url || url === 'QUIZ') return null;
    try {
        const regExp = /^.*(youtu.be\/|v\/|u\/\w\/|embed\/|watch\?v=|&v=)([^#&?]*).*/;
        const match = url.match(regExp);
        return (match && match[2].length === 11) ? match[2] : new URL(url).searchParams.get("v");
    } catch (e) { return null; }
}

const currentVideoId = getVideoId(dbVideoUrl);
const storageKey = "yt-time-" + (currentVideoId || "default");
let savedTime = Number(localStorage.getItem(storageKey)) || 0;

// API 로드 시 자동 실행
window.onYouTubeIframeAPIReady = function() {
    if (!currentVideoId) return;
    player = new YT.Player('player', {
        height: '100%', width: '100%', videoId: currentVideoId,
        playerVars: { 'start': savedTime, 'rel': 0, 'autoplay': 0 },
        events: { 'onReady': onPlayerReady, 'onStateChange': onPlayerStateChange }
    });
};

function onPlayerReady(event) {
    if (player && player.getDuration) {
        const duration = Math.floor(player.getDuration());
        if (duration > 0) saveDurationToServer(duration);
    }
    if (savedTime > 0) player.seekTo(savedTime);
}

function onPlayerStateChange(event) {
    if (event.data === YT.PlayerState.PAUSED) {
        saveProgress();
    }
    if (event.data === YT.PlayerState.ENDED) {
        localStorage.removeItem(storageKey);
    }
}

function saveProgress() {
    if (!player || typeof player.getCurrentTime !== 'function') return;
    const time = Math.floor(player.getCurrentTime());
    localStorage.setItem(storageKey, time);

    // 퀴즈일 때는 진도율 저장 스킵
    if (dbVideoUrl !== 'QUIZ' && currentCourseId && currentChapterId) {
        fetch(`/course/log?courseId=${currentCourseId}&chapterId=${currentChapterId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ playTime: time }),
            keepalive: true
        }).catch(console.error);
    }
}

// 10초마다 자동 저장 & 페이지 이탈 시 저장
setInterval(() => {
    if (player && player.getPlayerState && player.getPlayerState() === YT.PlayerState.PLAYING) saveProgress();
}, 10000);
window.addEventListener('beforeunload', saveProgress);


function saveDurationToServer(duration) {
    if (!currentChapterId || dbVideoUrl === 'QUIZ') return;
    fetch(`/course/log/duration?chapterId=${currentChapterId}&duration=${duration}`, { method: 'POST' })
        .catch(console.error);
}


/* =========================================
   [3] UI 제어 및 화면 전환
   ========================================= */
let currentActiveTab = null;

// 사이드 패널 열기
function openPanel(tabName) {
    const wrapper = document.getElementById('side-panel-wrapper');
    const targetContent = document.getElementById('content-' + tabName);

    if (wrapper.classList.contains('open') && currentActiveTab === tabName) {
        closePanel(); return;
    }

    document.querySelectorAll('.panel-content-box').forEach(el => el.style.display = 'none');
    if (targetContent) targetContent.style.display = 'flex';

    if (!wrapper.classList.contains('open')) wrapper.classList.add('open');

    // 에디터 탭 열릴 때 레이아웃 갱신 (입력창 사라짐 방지)
    if (tabName === 'interpreter' && monacoEditor) {
        setTimeout(() => monacoEditor.layout(), 100);
    }
    currentActiveTab = tabName;
}

function closePanel() {
    document.getElementById('side-panel-wrapper').classList.remove('open');
    currentActiveTab = null;
}

function toggleSection(headerElement) {
    headerElement.classList.toggle('collapsed');
}

// [핵심] 콘텐츠 재생/전환 로직 (영상 <-> 퀴즈)
function playContent(chapterId, videoUrl) {
    // 1. 퀴즈 챕터인 경우
    if (videoUrl === 'QUIZ') {
        // 영상 숨기기 & 정지
        const playerDiv = document.getElementById('player');
        if (playerDiv) playerDiv.style.display = 'none';
        if (player && typeof player.pauseVideo === 'function') player.pauseVideo();

        // 퀴즈 화면 보이기
        const quizWrapper = document.getElementById('quiz-wrapper');
        if (quizWrapper) quizWrapper.style.display = 'block';

        currentChapterId = chapterId;
        loadQuiz(chapterId); // 퀴즈 데이터 로드
        return;
    }

    // 2. 일반 영상인 경우
    const quizWrapper = document.getElementById('quiz-wrapper');
    if (quizWrapper) quizWrapper.style.display = 'none'; // 퀴즈 숨김

    const playerDiv = document.getElementById('player');
    if (playerDiv) playerDiv.style.display = 'block'; // 영상 보임

    // 페이지 이동 (SSR)
    const courseId = document.getElementById('course-id').value;
    window.location.href = `/course/play?courseId=${courseId}&chapterId=${chapterId}`;
}


/* =========================================
   [4] Monaco Editor (인터프리터)
   ========================================= */
require.config({ paths: { 'vs': 'https://cdnjs.cloudflare.com/ajax/libs/monaco-editor/0.34.1/min/vs' }});
require(['vs/editor/editor.main'], function () {
    const container = document.getElementById('monaco-editor-container');
    if(!container) return;

    monacoEditor = monaco.editor.create(container, {
        value: "print('Hello, LearnIT!')",
        language: 'python',
        theme: 'vs-light',
        lineNumbersMinChars: 3,
        automaticLayout: true // 창 크기 변경 시 자동 조절
    });
});

const langSelector = document.getElementById('language-selector');
if(langSelector) {
    langSelector.addEventListener('change', function() {
        const langId = this.value;
        let langMode = 'python';
        let sampleCode = "print('Hello, Python!')";

        if(langId === '62') { langMode = 'java'; sampleCode = 'public class Main {\n    public static void main(String[] args) {\n        System.out.println("Hello, Java!");\n    }\n}'; }
        else if(langId === '63') { langMode = 'javascript'; sampleCode = "console.log('Hello, JS!');"; }
        else if(langId === '54') { langMode = 'cpp'; sampleCode = '#include <iostream>\nint main() {\n    std::cout << "Hello, C++!";\n    return 0;\n}'; }

        monaco.editor.setModelLanguage(monacoEditor.getModel(), langMode);
        monacoEditor.setValue(sampleCode);
    });
}

function getCsrfHeader() {
    const header = document.querySelector('meta[name="_csrf_header"]');
    const token = document.querySelector('meta[name="_csrf"]');
    return (header && token) ? { [header.content]: token.content } : {};
}

function runCode() {
    const code = monacoEditor.getValue();
    const languageId = document.getElementById('language-selector').value;
    const consoleDiv = document.getElementById('output-console');

    consoleDiv.innerText = "실행 중입니다...";

    fetch('/api/interpreter/run', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...getCsrfHeader() },
        body: JSON.stringify({ code, languageId })
    })
        .then(res => res.json())
        .then(data => { consoleDiv.innerText = data.output; })
        .catch(err => { consoleDiv.innerText = "에러 발생: " + err; });
}


/* =========================================
   [5] 퀴즈 시스템 로직
   ========================================= */
function loadQuiz(chapterId) {
    console.log("퀴즈 로드: " + chapterId);
    fetch(`/api/quiz?chapterId=${chapterId}`)
        .then(response => {
            if (response.status === 403) return response.text().then(msg => { throw new Error(msg); });
            if (response.status === 204) { alert("등록된 퀴즈가 없습니다."); closePanel(); return null; }
            return response.json();
        })
        .then(data => {
            if (!data) return;
            quizData = data;

            // 제목 업데이트
            const titleEl = document.getElementById('display-quiz-title');
            if(titleEl) titleEl.innerText = data.title;

            currentQIndex = 0;
            userAnswers = [];
            showStep('start');
        })
        .catch(err => {
            alert("⚠️ " + err.message);
            // 403 에러 시 이전 페이지나 메인으로 돌려보낼 수도 있음
        });
}

function startQuizLogic() {
    if (!quizData?.questions?.length) return alert("퀴즈 데이터 오류");
    showStep('question');
    renderQuestion();
}

function renderQuestion() {
    const question = quizData.questions[currentQIndex];
    const total = quizData.questions.length;

    document.getElementById('curr-q-idx').innerText = currentQIndex + 1;
    document.getElementById('question-content').innerText = question.content;

    const container = document.getElementById('options-container');
    container.innerHTML = '';

    question.options.forEach(opt => {
        const btn = document.createElement('div');
        btn.className = 'option-item';
        btn.innerText = opt.content;
        btn.onclick = () => selectOption(btn, question.questionId, opt.optionId);
        container.appendChild(btn);
    });

    const nextBtn = document.getElementById('btn-next-question');
    nextBtn.disabled = true;
    nextBtn.style.backgroundColor = "#ccc";
    nextBtn.innerText = (currentQIndex === total - 1) ? '제출 하기' : '다음 문제';
}

function selectOption(btn, qId, oId) {
    document.querySelectorAll('.option-item').forEach(el => el.classList.remove('selected'));
    btn.classList.add('selected');

    const existing = userAnswers.find(a => a.questionId === qId);
    if (existing) existing.optionId = oId;
    else userAnswers.push({ questionId: qId, optionId: oId });

    const nextBtn = document.getElementById('btn-next-question');
    nextBtn.disabled = false;
    nextBtn.style.backgroundColor = (currentQIndex === quizData.questions.length - 1) ? "#00c471" : "#333";
}

function nextQuestion() {
    if (currentQIndex === quizData.questions.length - 1) submitQuiz();
    else {
        currentQIndex++;
        renderQuestion();
    }
}

function submitQuiz() {
    if (userAnswers.length < quizData.questions.length) return alert("모든 문제를 풀어주세요.");

    fetch('/api/quiz/submit', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...getCsrfHeader() },
        body: JSON.stringify({ quizId: quizData.quizId, answers: userAnswers })
    })
        .then(res => res.json())
        .then(result => {
            renderResult(result);
            showStep('result');
        })
        .catch(() => alert("채점 중 오류가 발생했습니다."));
}

function renderResult(result) {
    document.getElementById('result-score').innerText = result.score;
    const msgEl = document.getElementById('result-msg');
    if(msgEl) {
        msgEl.innerText = result.isPassed ? "축하합니다! 합격입니다 🎉" : "아쉽네요. 다시 도전해보세요 💪";
        msgEl.style.color = result.isPassed ? "#00c471" : "#ff4d4f";
    }
}

function showStep(stepName) {
    document.querySelectorAll('.quiz-step').forEach(el => el.style.display = 'none');
    const target = document.getElementById(`quiz-step-${stepName}`);
    if(target) target.style.display = (stepName === 'question') ? 'block' : 'flex';
}