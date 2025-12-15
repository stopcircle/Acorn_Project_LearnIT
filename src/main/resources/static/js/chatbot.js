const chatbotToggle = document.getElementById('chatbot-toggle');
const chatbotWindow = document.getElementById('chatbot-window');
const chatbotClose = document.getElementById('chatbot-close');
const chatbotBody = document.getElementById('chatbot-body');
const chatbotInput = document.getElementById('chatbot-input');
const chatbotSend = document.getElementById('chatbot-send');

//열기
chatbotToggle.addEventListener('click', () => {
    chatbotWindow.style.display = 'flex';
    chatbotWindow.style.flexDirection = 'column';
    chatbotToggle.style.opacity = '0';
    chatbotToggle.style.pointerEvents = 'none';
});

//닫기
chatbotClose.addEventListener('click', () => {
    chatbotWindow.style.display = 'none';
    chatbotToggle.style.opacity = '1';
    chatbotToggle.style.pointerEvents = 'auto';
});

//메시지 전송
chatbotSend.addEventListener('click', sendMessage);
chatbotInput.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') sendMessage();
});

function sendMessage() {
    const msg = chatbotInput.value.trim();
    if (!msg) return;

    // 사용자 메시지
    const userDiv = document.createElement('div');
    userDiv.className = 'user-message';
    userDiv.textContent = msg;
    chatbotBody.appendChild(userDiv);

    chatbotInput.value = '';
    chatbotBody.scrollTop = chatbotBody.scrollHeight;

    // 봇 응답 (데모)
    setTimeout(() => {
        const botDiv = document.createElement('div');
        botDiv.className = 'bot-message';
        botDiv.textContent = "죄송합니다, 현재는 데모 버전이에요. 자세한 상담은 직접 문의해주세요 😊";
        chatbotBody.appendChild(botDiv);
        chatbotBody.scrollTop = chatbotBody.scrollHeight;
    }, 600);
}