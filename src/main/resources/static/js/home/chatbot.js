// =====================================================
// ✅ LearnIT Chatbot (FULL FILE) - FIXED
// 변경사항 요약
// 1) removeExistingMenu(): quick-menu "하나"만 삭제 → "전부" 삭제로 수정
// 2) NOTICE: 메뉴를 2개 쌓는 구조를 안정화(기존 메뉴 정리 후 keep로만 추가)
// 3) showMainMenu(): 시작 시 메뉴 정리 추가(잔여 메뉴 방지)
// =====================================================

// ===============================
// ✅ DOM
// ===============================
const chatbotToggle = document.getElementById("chatbot-toggle");
const chatbotWindow = document.getElementById("chatbot-window");
const chatbotClose = document.getElementById("chatbot-close");
const chatbotBody = document.getElementById("chatbot-body");
const chatbotInput = document.getElementById("chatbot-input");
const chatbotSend = document.getElementById("chatbot-send");

// ===============================
// ✅ 전역값(서버 주입) 방어
// ===============================
window.IS_LOGGED_IN = typeof window.IS_LOGGED_IN === "boolean" ? window.IS_LOGGED_IN : false;
window.CSRF_HEADER = window.CSRF_HEADER || "X-CSRF-TOKEN";
window.CSRF_TOKEN = window.CSRF_TOKEN || "";
window.DEFAULT_QNA_COURSE_ID = Number(window.DEFAULT_QNA_COURSE_ID || 1);

// ===============================
// ✅ 상태
// ===============================
const state = {
    mode: "IDLE",
    chatSessionId: null,
};

// ===============================
// ✅ 메뉴
// ===============================
const MAIN_MENU = [
    { key: "COURSE_FIND", label: "📚 강의 찾기" },
    { key: "MY_LEARNING", label: "▶️ 내 학습 현황" },
    { key: "PAYMENT", label: "💳 결제내역확인" },
    { key: "NOTICE", label: "📢 공지 & 이벤트" },
    { key: "HUMAN", label: "🧑‍💼 상담원 연결" },
];

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

// ✅ quick-menu(버튼 영역) "전부" 제거 (중요!)
function removeExistingMenu() {
    chatbotBody.querySelectorAll(".quick-menu").forEach((m) => m.remove());
}

// 메뉴 버튼을 아래에 붙임 (dataset.action 사용) - 기존 메뉴 제거 후 1개만 유지
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

// ✅ 메뉴를 "추가로" 붙이는 버전 (기존 메뉴를 삭제하지 않음)
function appendMenuButtonsKeep(buttons) {
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

// 채팅 입력 잠금/해제
function setChatInputEnabled(enabled) {
    chatbotInput.disabled = !enabled;
    chatbotSend.disabled = !enabled;

    if (enabled) {
        chatbotInput.placeholder = "메시지를 입력하세요...";
        chatbotInput.classList.remove("disabled");
    } else {
        chatbotInput.placeholder = "메뉴에서 선택해 주세요.";
        chatbotInput.classList.add("disabled");
    }
}

// "다른 문의?" + (추가 버튼) + MAIN_MENU 를 한 번에 표시
function showFollowUpMenu(extraButtons = []) {
    addBotMessage("다음엔 다른 문의가 필요하신가요? 😊");
    appendMenuButtons([...extraButtons, ...MAIN_MENU]);
}

// ===============================
// ✅ 로그인 필요 UI (메뉴별 문구 지원)
// ===============================
function showLoginRequired(message, returnUrl) {
    state.mode = "IDLE";
    setChatInputEnabled(false);

    addBotMessage(message || "이 기능은 로그인이 필요해요. 로그인 해주세요.");

    removeExistingMenu();
    const quickMenu = document.createElement("div");
    quickMenu.className = "quick-menu";

    const loginBtn = document.createElement("button");
    loginBtn.type = "button";
    loginBtn.className = "quick-btn";
    loginBtn.textContent = "🔐 로그인";
    loginBtn.onclick = () => {
        const url = returnUrl
            ? `/login?returnUrl=${encodeURIComponent(returnUrl)}`
            : "/login";
        window.location.href = url;
    };

    const homeBtn = document.createElement("button");
    homeBtn.type = "button";
    homeBtn.className = "quick-btn";
    homeBtn.dataset.action = "HOME";
    homeBtn.textContent = "🏠 처음으로";

    quickMenu.appendChild(loginBtn);
    quickMenu.appendChild(homeBtn);

    chatbotBody.appendChild(quickMenu);
    chatbotBody.scrollTop = chatbotBody.scrollHeight;
}

// ===============================
// ✅ 로그인 필요한 페이지 이동
// ===============================
function goWithLogin(targetUrl, loginMessage) {
    if (window.IS_LOGGED_IN) {
        window.location.href = targetUrl;
        return;
    }
    showLoginRequired(
        loginMessage || "이 기능은 로그인이 필요해요. 로그인 해주세요.",
        targetUrl
    );
}

// ===============================
// ✅ 메인 메뉴 표시(처음으로)
// ===============================
function showMainMenu() {
    state.mode = "IDLE";
    setChatInputEnabled(false);

    // ✅ 잔여 메뉴 방지
    removeExistingMenu();

    addBotMessage("원하시는 항목을 선택해 주세요 😊");
    appendMenuButtons(MAIN_MENU);
}

// ===============================
// ✅ 메뉴 처리
// ===============================
function handleAction(actionKey) {
    if (actionKey === "HOME") {
        showMainMenu();
        return;
    }

    switch (actionKey) {
        // ---------------------------------
        // 📚 강의 찾기: 채팅 가능 + 챗봇 추천 대화
        // ---------------------------------
        case "COURSE_FIND":
            state.mode = "COURSE_RECOMMEND";
            state.chatSessionId = null;
            setChatInputEnabled(true);
            addBotMessage(
                "원하시는 강의/목표를 채팅으로 적어주세요 😊 예) 자바 입문, 스프링 프로젝트, SQL 기초"
            );
            appendMenuButtons([{ key: "HOME", label: "🏠 처음으로" }]);
            break;

        // ---------------------------------
        // ▶️ 내 학습 현황: 로그인 필수
        // ---------------------------------
        case "MY_LEARNING": {
            setChatInputEnabled(false);

            if (!window.IS_LOGGED_IN) {
                showLoginRequired(
                    "내학습 현황은 로그인이 필요해요. 로그인 해주세요.",
                    "http://localhost:8081/mypage/courses"
                );
                return;
            }

            addBotMessage("학습 현황은 아래에서 확인 할수 있습니다.");
            showFollowUpMenu([{ key: "NAV_MY_COURSES", label: "📈 학습현황 확인" }]);
            break;
        }

        case "NAV_MY_COURSES":
            goWithLogin(
                "http://localhost:8081/mypage/courses",
                "내학습 현황은 로그인이 필요해요. 로그인 해주세요."
            );
            break;

        // ---------------------------------
        // 💳 결제내역확인: 로그인 필수
        // ---------------------------------
        case "PAYMENT": {
            setChatInputEnabled(false);

            if (!window.IS_LOGGED_IN) {
                showLoginRequired(
                    "결제내역 확인은 로그인이 필요해요. 로그인 해주세요.",
                    "http://localhost:8081/mypage/purchase"
                );
                return;
            }

            addBotMessage("결제내역확인은 아래에서 확인 할수 있습니다.");
            showFollowUpMenu([{ key: "NAV_PURCHASE", label: "💳 결제내역확인" }]);
            break;
        }

        case "NAV_PURCHASE":
            goWithLogin(
                "http://localhost:8081/mypage/purchase",
                "결제내역 확인은 로그인이 필요해요. 로그인 해주세요."
            );
            break;

        // ---------------------------------
        // 📢 공지 & 이벤트: 채팅 불가 + 버튼 이동 + followup
        // ---------------------------------
        case "NOTICE":
            state.mode = "IDLE";
            setChatInputEnabled(false);

            // ✅ 기존 메뉴 전부 제거 (중요)
            removeExistingMenu();

            // ✅ 1) 안내
            addBotMessage("공지 & 이벤트는 아래에서 확인 할수 있습니다.");

            // ✅ 2) 공지 이동 버튼
            appendMenuButtonsKeep([{ key: "NAV_NOTICE", label: "📢 공지 & 이벤트" }]);

            // ✅ 3) 다음 안내
            addBotMessage("다음엔 다른 문의가 필요하신가요? 😊");

            // ✅ 4) 메인 메뉴
            appendMenuButtonsKeep(MAIN_MENU);
            break;

        case "NAV_NOTICE":
            window.location.href = "http://localhost:8081/notice";
            break;

        // ---------------------------------
        // 🧑‍💼 상담원 연결: 로그인 필수
        // ---------------------------------
        case "HUMAN":
            if (!window.IS_LOGGED_IN) {
                showLoginRequired("상담원 문의는 로그인이 필요해요. 로그인 해주세요.");
                return;
            }

            state.mode = "HUMAN_WAIT_CONTENT";
            setChatInputEnabled(true);
            addBotMessage("문의 내용을 챗봇에 작성해 주세요. 확인 후 연락 드리겠습니다 😊");
            appendMenuButtons([{ key: "HOME", label: "🏠 처음으로" }]);
            break;

        default:
            addBotMessage("원하시는 항목을 선택해 주세요 😊");
            appendMenuButtons(MAIN_MENU);
    }
}

function getConversationId() {
    let id = sessionStorage.getItem("chat_conversation_id");
    if (!id) {
        id = "c_" + Date.now() + "_" + Math.random().toString(16).slice(2);
        sessionStorage.setItem("chat_conversation_id", id);
    }
    return id;
}

// ===============================
// ✅ 입력 전송
// ===============================
async function sendMessage() {
    const msg = chatbotInput.value.trim();
    if (!msg) return;

    addUserMessage(msg);
    chatbotInput.value = "";
    chatbotInput.focus();

    // ---------------------------------
    // 📚 강의 찾기: 챗봇(chat-agent) 추천 대화
    // ---------------------------------
    if (state.mode === "COURSE_RECOMMEND") {
        try {
            const headers = { "Content-Type": "application/json" };
            if (window.CSRF_TOKEN) headers[window.CSRF_HEADER] = window.CSRF_TOKEN;

            const res = await fetch("/api/chatbot/chat", {
                method: "POST",
                headers,
                body: JSON.stringify({
                    message: msg,
                    sessionId: state.chatSessionId, // ✅ 유지 전송
                }),
            });

            if (!res.ok) throw new Error("chat failed");
            const data = await res.json();

            // ✅ 응답에 sessionId가 오면 저장 (처음 응답에서만 생성되는 경우 대응)
            if (data.sessionId) {
                state.chatSessionId = data.sessionId;
            }

            addBotMessage(data.reply || "추천 결과를 가져왔어요!");
            return;
        } catch (e) {
            addBotMessage("추천을 불러오는 중 오류가 발생했어요. 잠시 후 다시 시도해 주세요.");
            return;
        }
    }

    // ---------------------------------
    // 🧑‍💼 상담원 문의: DB 저장
    // ---------------------------------
    if (state.mode === "HUMAN_WAIT_CONTENT") {
        try {
            const headers = { "Content-Type": "application/json" };
            if (window.CSRF_TOKEN) headers[window.CSRF_HEADER] = window.CSRF_TOKEN;

            const payload = {
                courseId: Number(window.DEFAULT_QNA_COURSE_ID || 1),
                title: "상담원 문의",
                content: msg,
            };

            const res = await fetch("/api/chatbot/qna", {
                method: "POST",
                headers,
                body: JSON.stringify(payload),
            });

            if (!res.ok) throw new Error("qna failed");

            addBotMessage("접수되었습니다. 확인 후 연락 드리겠습니다 😊");

            state.mode = "IDLE";
            setChatInputEnabled(false);
            showFollowUpMenu();
            return;
        } catch (e) {
            addBotMessage("죄송해요. 접수 중 오류가 발생했어요. 잠시 후 다시 시도해 주세요.");
            return;
        }
    }

    // ---------------------------------
    // 기본 모드(원칙상 채팅 비활성)
    // ---------------------------------
    addBotMessage("메뉴에서 항목을 선택해 주세요 😊");
}

// ===============================
// ✅ 이벤트 리스너
// ===============================

// 열기
chatbotToggle.addEventListener("click", () => {
    chatbotWindow.style.display = "flex";
    chatbotWindow.style.flexDirection = "column";
    chatbotToggle.style.opacity = "0";
    chatbotToggle.style.pointerEvents = "none";

    setChatInputEnabled(false);
    showMainMenu();
});

// 닫기
chatbotClose.addEventListener("click", () => {
    chatbotWindow.style.display = "none";
    chatbotToggle.style.opacity = "1";
    chatbotToggle.style.pointerEvents = "auto";
});

// 전송
chatbotSend.addEventListener("click", () => sendMessage());
chatbotInput.addEventListener("keydown", (e) => {
    if (e.key === "Enter") sendMessage();
});

// quick 버튼 클릭
document.addEventListener("click", (e) => {
    const btn = e.target.closest(".quick-btn");
    if (!btn) return;

    const actionKey = btn.dataset.action;

    // ✅ 클릭 즉시 기존 메뉴 전부 제거
    removeExistingMenu();

    // 사용자 메시지 표시
    addUserMessage(btn.textContent);

    handleAction(actionKey);
});
