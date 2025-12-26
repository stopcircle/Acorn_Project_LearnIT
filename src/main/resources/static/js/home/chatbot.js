const chatbotToggle = document.getElementById('chatbot-toggle');
const chatbotWindow = document.getElementById('chatbot-window');
const chatbotClose = document.getElementById('chatbot-close');
const chatbotBody = document.getElementById('chatbot-body');
const chatbotInput = document.getElementById('chatbot-input');
const chatbotSend = document.getElementById('chatbot-send');

// 로그인 여부(없으면 false로)
window.IS_LOGGED_IN = typeof window.IS_LOGGED_IN === 'boolean' ? window.IS_LOGGED_IN : false;
// 내 수강 목록(없으면 빈 배열)
window.MY_COURSES = Array.isArray(window.MY_COURSES) ? window.MY_COURSES : [];
// CSRF(없으면 빈값)
window.CSRF_HEADER = window.CSRF_HEADER || "X-CSRF-TOKEN";
window.CSRF_TOKEN = window.CSRF_TOKEN || "";

// ===============================
// ✅ 상태 (강의 문의 흐름용)
// ===============================
const state = {
    mode: "IDLE", // IDLE | QNA_COURSE_WAIT_CONTENT
    selectedCourseId: null,
    selectedCourseTitle: null,
};

// ===============================
// ✅ 메뉴 데이터
// ===============================
const MAIN_MENU = [
    { key: "COURSE_FIND", label: "📚 강의 찾기" },
    { key: "MY_LEARNING", label: "▶️ 내 학습 현황" },
    { key: "QNA", label: "❓ 강의/학습 문의" },
    { key: "PAYMENT", label: "💳 결제관리" },
    { key: "NOTICE", label: "📢 공지 & 이벤트" },
    { key: "HUMAN", label: "🧑‍💼 상담원 연결" },
];

const SUB_MENU = {
    COURSE_FIND: [
        { key: "COURSE_POP", label: "🔥 인기 강의" },
        { key: "COURSE_NEW", label: "🆕 신규 강의" },
        { key: "COURSE_CAT", label: "🗂️ 카테고리" },
        { key: "HOME", label: "🏠 메인 메뉴" },
    ],
    MY_LEARNING: [
        { key: "MY_PROGRESS", label: "📈 진도율 확인" },
        { key: "MY_CERT", label: "🏅 수료증/완료 강의" },
        { key: "HOME", label: "🏠 메인 메뉴" },
    ],
    QNA: [
        { key: "QNA_COURSE", label: "📘 강의 내용 질문" },
        { key: "HOME", label: "🏠 메인 메뉴" },
    ],
    PAYMENT: [
        { key: "PAY_ERR", label: "💳 결제 오류" },
        { key: "REFUND", label: "↩️ 환불 안내" },
        { key: "COUPON", label: "🏷️ 쿠폰/할인" },
        { key: "RECEIPT", label: "🧾 영수증" },
        { key: "HOME", label: "🏠 메인 메뉴" },
    ],
    NOTICE: [
        { key: "NOTICE_LATEST", label: "📢 최신 공지" },
        { key: "HOME", label: "🏠 메인 메뉴" },
    ],
    HUMAN: [
        { key: "HUMAN_CONNECT", label: "🧑‍💼 상담원 연결" },
        { key: "HUMAN_HOURS", label: "⏰ 운영시간" },
        { key: "HOME", label: "🏠 메인 메뉴" },
    ],
};

// ===============================
// ✅ UI 유틸
// ===============================
function addBotMessage(text) {
    const botDiv = document.createElement("div");
    botDiv.className = "bot-message";
    botDiv.textContent = text;
    chatbotBody.appendChild(botDiv);
    chatbotBody.scrollTop = chatbotBody.scrollHeight;
}

function addUserMessage(text) {
    const userDiv = document.createElement("div");
    userDiv.className = "user-message";
    userDiv.textContent = text;
    chatbotBody.appendChild(userDiv);
    chatbotBody.scrollTop = chatbotBody.scrollHeight;
}

// ✅ 기존 메뉴 제거(한 번에 하나만 유지)
function removeExistingMenu() {
    const existing = chatbotBody.querySelector(".quick-menu");
    if (existing) existing.remove();
}

// ✅ 메뉴 버튼을 "채팅 흐름처럼" 아래에 새로 붙임
function appendMenuButtons(buttons) {
    removeExistingMenu();

    const quickMenu = document.createElement("div");
    quickMenu.className = "quick-menu";

    buttons.forEach((b) => {
        const el = document.createElement("button");
        el.type = "button";
        el.className = "quick-btn";
        el.dataset.action = b.key;
        el.textContent = b.label;
        quickMenu.appendChild(el);
    });

    chatbotBody.appendChild(quickMenu);
    chatbotBody.scrollTop = chatbotBody.scrollHeight;
}

function showMainMenu() {
    // 흐름 리셋
    state.mode = "IDLE";
    state.selectedCourseId = null;
    state.selectedCourseTitle = null;

    addBotMessage("원하시는 항목을 선택해 주세요 😊");
    appendMenuButtons(MAIN_MENU);
}

function showSubMenu(mainKey) {
    const list = SUB_MENU[mainKey] || [{ key: "HOME", label: "🏠 메인 메뉴" }];
    addBotMessage("원하시는 항목을 선택해 주세요 😊");
    appendMenuButtons(list);
}

// ===============================
// ✅ 로그인 버튼
// ===============================
function showLoginButtons() {
    removeExistingMenu();

    const quickMenu = document.createElement("div");
    quickMenu.className = "quick-menu";

    const loginBtn = document.createElement("button");
    loginBtn.className = "quick-btn";
    loginBtn.textContent = "🔐 로그인하기";
    loginBtn.onclick = () => {
        window.location.href = "/login"; // 로그인 경로 맞춰서 수정
    };

    const homeBtn = document.createElement("button");
    homeBtn.className = "quick-btn";
    homeBtn.dataset.action = "HOME";
    homeBtn.textContent = "🏠 메인 메뉴";

    quickMenu.appendChild(loginBtn);
    quickMenu.appendChild(homeBtn);

    chatbotBody.appendChild(quickMenu);
    chatbotBody.scrollTop = chatbotBody.scrollHeight;
}

// ===============================
// ✅ QNA: 수강 목록 보여주기
// ===============================
function showMyCourseListForQna() {
    // 비로그인 → 로그인 유도
    if (!window.IS_LOGGED_IN) {
        addBotMessage("강의 내용 문의는 로그인 후 이용 가능해요. 로그인 하시겠어요?");
        showLoginButtons();
        return;
    }

    const courses = window.MY_COURSES;

    if (!courses || courses.length === 0) {
        addBotMessage("현재 수강 중인 강의가 없어요. 강의를 수강 신청한 뒤 이용해 주세요.");
        appendMenuButtons([{ key: "HOME", label: "🏠 메인 메뉴" }]);
        return;
    }

    addBotMessage("어떤 강의에 대해 문의하시나요? 아래에서 선택해 주세요.");

    removeExistingMenu();
    const quickMenu = document.createElement("div");
    quickMenu.className = "quick-menu";

    courses.forEach((c) => {
        const b = document.createElement("button");
        b.type = "button";
        b.className = "quick-btn";
        b.dataset.action = "SELECT_COURSE_FOR_QNA";
        b.dataset.courseId = String(c.id);
        b.dataset.courseTitle = c.title;
        b.textContent = `🎓 ${c.title}`;
        quickMenu.appendChild(b);
    });

    const home = document.createElement("button");
    home.type = "button";
    home.className = "quick-btn";
    home.dataset.action = "HOME";
    home.textContent = "🏠 메인 메뉴";
    quickMenu.appendChild(home);

    chatbotBody.appendChild(quickMenu);
    chatbotBody.scrollTop = chatbotBody.scrollHeight;
}

// ===============================
// ✅ 메뉴 동작
// ===============================
function handleAction(actionKey) {
    if (actionKey === "HOME") {
        showMainMenu();
        return;
    }

    // 메인 메뉴 선택 → 하위 메뉴
    if (SUB_MENU[actionKey]) {
        showSubMenu(actionKey);
        return;
    }

    // ✅ 강의 내용 질문 진입
    if (actionKey === "QNA_COURSE") {
        showMyCourseListForQna();
        return;
    }

    // ✅ 내 학습 현황 하위(로그인 필요)
    if ((actionKey === "MY_PROGRESS" || actionKey === "MY_CERT") && !window.IS_LOGGED_IN) {
        addBotMessage("해당 기능은 로그인 후 이용 가능해요. 로그인 하시겠어요?");
        showLoginButtons();
        return;
    }

    // 기타 데모 응답
    switch (actionKey) {
        case "COURSE_POP":
            addBotMessage("인기 강의를 보여드릴게요! 관심 분야가 있나요? (예: 개발/디자인/마케팅)");
            break;
        case "COURSE_NEW":
            addBotMessage("신규 강의를 안내할게요! 관심 분야가 있나요?");
            break;
        case "PAY_ERR":
            addBotMessage("결제 오류 유형을 알려주세요. (카드/간편결제/결제완료 후 미반영 등)");
            break;
        case "HUMAN_CONNECT":
            addBotMessage("상담원 연결을 진행할게요. 운영시간은 평일 09:00~18:00 입니다.");
            break;
        case "HUMAN_HOURS":
            addBotMessage("운영시간은 평일 09:00~18:00 입니다.");
            break;
        default:
            addBotMessage("선택하신 항목을 처리할게요. 조금만 더 자세히 알려주세요 😊");
    }
}

// ===============================
// ✅ 입력 전송
// - 평소: 안내 메시지
// - 강의 문의 작성 모드: Spring API로 저장 후 접수 메시지
// ===============================
async function sendMessage() {
    const msg = chatbotInput.value.trim();
    if (!msg) return;

    addUserMessage(msg);
    chatbotInput.value = "";

    // ✅ 강의 문의 내용 작성 중이면 DB 저장
    if (state.mode === "QNA_COURSE_WAIT_CONTENT") {
        try {
            const payload = {
                courseId: Number(state.selectedCourseId),
                content: msg,
            };

            const headers = { "Content-Type": "application/json" };
            if (window.CSRF_TOKEN) headers[window.CSRF_HEADER] = window.CSRF_TOKEN;

            const res = await fetch("/api/chatbot/inquiries", {
                method: "POST",
                headers,
                body: JSON.stringify(payload),
            });

            if (!res.ok) throw new Error("failed");

            addBotMessage("접수했습니다. 마이페이지의 답변을 기다려주세요.");

            // 상태 초기화
            state.mode = "IDLE";
            state.selectedCourseId = null;
            state.selectedCourseTitle = null;

            // 원하면 메인 메뉴 재노출
            // showMainMenu();
            return;

        } catch (e) {
            addBotMessage("죄송해요. 접수 중 오류가 발생했어요. 잠시 후 다시 시도해 주세요.");
            return;
        }
    }

    // ✅ 일반 자유 입력 응답
    addBotMessage("문의 내용을 확인했어요. 조금만 더 자세히 알려주세요 😊");
}

// ===============================
// ✅ 이벤트 리스너
// ===============================

// 열기
chatbotToggle.addEventListener('click', () => {
    chatbotWindow.style.display = 'flex';
    chatbotWindow.style.flexDirection = 'column';
    chatbotToggle.style.opacity = '0';
    chatbotToggle.style.pointerEvents = 'none';

    showMainMenu();
});

// 닫기
chatbotClose.addEventListener('click', () => {
    chatbotWindow.style.display = 'none';
    chatbotToggle.style.opacity = '1';
    chatbotToggle.style.pointerEvents = 'auto';
});

// 전송
chatbotSend.addEventListener('click', () => sendMessage());
chatbotInput.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') sendMessage();
});

// 버튼 클릭(메인/하위/수강강의 공용)
document.addEventListener("click", (e) => {
    const btn = e.target.closest(".quick-btn");
    if (!btn) return;

    const actionKey = btn.dataset.action;

    // ✅ 클릭 즉시 메뉴 제거(선택한 순간 기존 버튼 사라짐)
    removeExistingMenu();

    // 사용자 메시지 표시
    addUserMessage(btn.textContent);

    // ✅ 수강 강의 선택 → 문의 내용 입력 모드 전환
    if (actionKey === "SELECT_COURSE_FOR_QNA") {
        state.mode = "QNA_COURSE_WAIT_CONTENT";
        state.selectedCourseId = btn.dataset.courseId;
        state.selectedCourseTitle = btn.dataset.courseTitle;

        addBotMessage(`"${state.selectedCourseTitle}" 문의 내용을 아래 입력창에 작성 후 전송해 주세요 😊`);
        return;
    }

    handleAction(actionKey);
});
