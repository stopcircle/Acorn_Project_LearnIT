// ================================================
// Q&A Panel JS (FINAL - 상태 유지 버전)
// - 수정/삭제/등록 후 qnaLoad()로 전체 리렌더해도
//   "열려있는 입력창 + 작성중 텍스트 + 스크롤" 유지
// - 너가 올린 현재 코드 기반으로 정리 (복붙 즉시 사용)
// ================================================

let __qnaInitialized = false;

// ✅ 리로드 전/후 UI 상태 저장
let __qnaUiState = null;

function initQnaPanel() {
    if (__qnaInitialized) {
        qnaLoad();
        return;
    }
    __qnaInitialized = true;

    document.getElementById("qna-btn-create")?.addEventListener("click", qnaCreate);
    qnaLoad();
}

// ================================================
// ✅ UI 상태 저장/복원
// ================================================
function captureQnaUiState() {
    const state = {
        scrollTop: 0,
        questions: {} // qnaId -> 상태
    };

    const listEl = document.getElementById("qna-list");
    if (listEl) state.scrollTop = listEl.scrollTop || 0;

    document.querySelectorAll(".qna-item[data-qna-id]").forEach(item => {
        const qnaId = item.dataset.qnaId;

        const q = {
            qEditOpen: false,
            qEditValue: "",
            aComposeOpen: false,
            aComposeValue: "",
            aResolvedValue: null,
            answers: {} // answerId -> 상태
        };

        // 질문 수정창
        const qEditBox = item.querySelector('[data-role="q-edit"]');
        const qContentBox = item.querySelector('[data-role="q-content"]');
        if (qEditBox && qContentBox) {
            q.qEditOpen = (qEditBox.style.display !== "none");
            const ta = qEditBox.querySelector("textarea");
            if (ta) q.qEditValue = ta.value ?? "";
        }

        // 답변 작성창
        const aCompose = item.querySelector('[data-role="a-compose"]');
        if (aCompose) {
            q.aComposeOpen = (aCompose.style.display !== "none");
            const ta = aCompose.querySelector("textarea");
            if (ta) q.aComposeValue = ta.value ?? "";
            const sel = aCompose.querySelector('[data-role="resolved"]');
            if (sel) q.aResolvedValue = sel.value ?? null;
        }

        // 답변/대댓글/답변수정 상태
        item.querySelectorAll(".qna-answer[data-answer-id]").forEach(ansEl => {
            const answerId = ansEl.dataset.answerId;

            const a = {
                aEditOpen: false,
                aEditValue: "",
                aResolvedValue: null,
                replyOpen: false,
                replyValue: ""
            };

            const aEdit = ansEl.querySelector('[data-role="a-edit"]');
            const aContent = ansEl.querySelector('[data-role="a-content"]');
            if (aEdit && aContent) {
                a.aEditOpen = (aEdit.style.display !== "none");
                const ta = aEdit.querySelector("textarea");
                if (ta) a.aEditValue = ta.value ?? "";
                const sel = aEdit.querySelector('[data-role="resolved"]');
                if (sel) a.aResolvedValue = sel.value ?? null;
            }

            const reply = ansEl.querySelector('[data-role="reply"]');
            if (reply) {
                a.replyOpen = (reply.style.display !== "none");
                const ta = reply.querySelector("textarea");
                if (ta) a.replyValue = ta.value ?? "";
            }

            q.answers[answerId] = a;
        });

        state.questions[qnaId] = q;
    });

    return state;
}

function restoreQnaUiState(state) {
    if (!state) return;

    const listEl = document.getElementById("qna-list");
    if (listEl) listEl.scrollTop = state.scrollTop || 0;

    Object.entries(state.questions || {}).forEach(([qnaId, q]) => {
        const item = document.querySelector(`.qna-item[data-qna-id="${qnaId}"]`);
        if (!item) return; // 삭제되어 사라진 경우

        // 질문 수정 복원
        const qEditBox = item.querySelector('[data-role="q-edit"]');
        const qContentBox = item.querySelector('[data-role="q-content"]');
        if (qEditBox && qContentBox) {
            if (q.qEditOpen) {
                qContentBox.style.display = "none";
                qEditBox.style.display = "block";
            }
            const ta = qEditBox.querySelector("textarea");
            if (ta && typeof q.qEditValue === "string") ta.value = q.qEditValue;
        }

        // 답변 작성창 복원
        const aCompose = item.querySelector('[data-role="a-compose"]');
        if (aCompose) {
            aCompose.style.display = q.aComposeOpen ? "block" : "none";
            const ta = aCompose.querySelector("textarea");
            if (ta && typeof q.aComposeValue === "string") ta.value = q.aComposeValue;
            const sel = aCompose.querySelector('[data-role="resolved"]');
            if (sel && q.aResolvedValue != null) sel.value = q.aResolvedValue;
        }

        // 답변/대댓글 복원
        Object.entries(q.answers || {}).forEach(([answerId, a]) => {
            const ansEl = item.querySelector(`.qna-answer[data-answer-id="${answerId}"]`);
            if (!ansEl) return;

            const aEdit = ansEl.querySelector('[data-role="a-edit"]');
            const aContent = ansEl.querySelector('[data-role="a-content"]');
            if (aEdit && aContent) {
                if (a.aEditOpen) {
                    aContent.style.display = "none";
                    aEdit.style.display = "block";
                }
                const ta = aEdit.querySelector("textarea");
                if (ta && typeof a.aEditValue === "string") ta.value = a.aEditValue;
                const sel = aEdit.querySelector('[data-role="resolved"]');
                if (sel && a.aResolvedValue != null) sel.value = a.aResolvedValue;
            }

            const reply = ansEl.querySelector('[data-role="reply"]');
            if (reply) {
                reply.style.display = a.replyOpen ? "block" : "none";
                const ta = reply.querySelector("textarea");
                if (ta && typeof a.replyValue === "string") ta.value = a.replyValue;
            }
        });
    });
}

// ================================================
// 데이터 로드
// ================================================
async function qnaLoad() {
    const courseId = document.getElementById("course-id")?.value;
    if (!courseId) return;

    // ✅ 리로드 전 상태 저장
    __qnaUiState = captureQnaUiState();

    const listEl = document.getElementById("qna-list");
    const emptyEl = document.getElementById("qna-empty");
    if (!listEl) return;

    listEl.innerHTML = "";
    if (emptyEl) emptyEl.style.display = "none";

    const res = await fetchWithCsrf(`/api/qna/questions?courseId=${courseId}`, { method: "GET" });
    if (!res.ok) {
        listEl.innerHTML = `<div class="qna-error">Q&A를 불러오지 못했어요.</div>`;
        return;
    }

    const data = await res.json();

    if (!data || data.length === 0) {
        if (emptyEl) emptyEl.style.display = "block";
        return;
    }

    data.forEach(q => listEl.appendChild(renderQuestion(q)));

    // ✅ 리로드 후 상태 복원
    restoreQnaUiState(__qnaUiState);
}

// ================================================
// 질문 등록
// ================================================
async function qnaCreate() {
    const courseId = document.getElementById("course-id")?.value;
    const ta = document.getElementById("qna-new-content");
    const hint = document.getElementById("qna-compose-hint");
    if (!courseId || !ta) return;

    const content = (ta.value || "").trim();
    if (!content) {
        if (hint) hint.textContent = "내용을 입력해 주세요.";
        return;
    }
    if (hint) hint.textContent = "";

    const res = await fetchWithCsrf(`/api/qna/questions`, {
        method: "POST",
        body: JSON.stringify({ courseId: Number(courseId), content })
    });

    if (res.status === 403) {
        if (hint) hint.textContent = "수강중인 회원만 질문을 작성할 수 있어요.";
        return;
    }
    if (!res.ok) {
        if (hint) hint.textContent = "질문 등록에 실패했어요.";
        return;
    }

    ta.value = "";
    await qnaLoad();
}

// ================================================
// 질문 렌더링
// ================================================
function renderQuestion(q) {
    const wrap = document.createElement("div");
    wrap.className = "qna-item";
    wrap.dataset.qnaId = q.qnaId;

    const resolvedBadge = (q.isResolved === "Y")
        ? `<span class="qna-badge resolved">해결</span>`
        : `<span class="qna-badge pending">미해결</span>`;

    const ownerActions = `
      ${q.canEditQuestion ? `<button class="qna-link" data-act="q-edit">수정</button>` : ``}
      ${q.canDeleteQuestion ? `<button class="qna-link danger" data-act="q-del">삭제</button>` : ``}
    `;

    const adminAnswerActions = q.canAnswer
        ? `<button class="qna-btn" data-act="a-open">답변</button>`
        : ``;

    wrap.innerHTML = `
      <div class="qna-head">
        <div class="qna-meta">
          <span class="qna-nick">${escapeHtml(q.writerNickname || "사용자")}</span>
          <span class="qna-dot">·</span>
          <span class="qna-time">${escapeHtml(q.createdAt || "")}</span>
          ${resolvedBadge}
        </div>
        <div class="qna-actions">
          ${ownerActions}
        </div>
      </div>

      <div class="qna-content" data-role="q-content">${escapeHtml(q.content || "")}</div>

      <div class="qna-edit" data-role="q-edit" style="display:none;">
        <textarea class="qna-ta">${escapeHtml(q.content || "")}</textarea>
        <div class="qna-row">
          <button class="qna-btn primary" data-act="q-save">저장</button>
          <button class="qna-btn" data-act="q-cancel">취소</button>
        </div>
      </div>

      <div class="qna-answer-area">
        <div class="qna-answer-actions">
          ${adminAnswerActions}
        </div>

        <div class="qna-answer-compose" data-role="a-compose" style="display:none;">
          <textarea class="qna-ta" placeholder="답변을 입력하세요."></textarea>

          <div class="qna-row">
            ${q.canSetResolved ? `
              <label class="qna-select-wrap">
                <span>해결여부</span>
                <select class="qna-select" data-role="resolved">
                  <option value="N" ${q.isResolved === "N" ? "selected" : ""}>미해결</option>
                  <option value="Y" ${q.isResolved === "Y" ? "selected" : ""}>해결</option>
                </select>
              </label>
            ` : ``}

            <button class="qna-btn primary" data-act="a-create">등록</button>
            <button class="qna-btn" data-act="a-cancel">닫기</button>
          </div>
          <div class="qna-hint" data-role="a-hint"></div>
        </div>

        <div class="qna-answers" data-role="answers"></div>
      </div>
    `;

    // 답변 렌더
    const answersBox = wrap.querySelector('[data-role="answers"]');
    (q.answers || []).forEach(a => answersBox.appendChild(renderAnswer(q, a)));

    // 이벤트 위임
    wrap.addEventListener("click", async (e) => {
        const act = e.target?.dataset?.act;
        if (!act) return;

        const qnaId = q.qnaId;
        const courseId = document.getElementById("course-id")?.value;

        if (act === "q-edit") {
            const contentBox = wrap.querySelector('[data-role="q-content"]');
                const editBox = wrap.querySelector('[data-role="q-edit"]');
                const ta = editBox.querySelector('textarea');

                // ✅ 최신 표시 내용을 textarea에 넣고 편집 모드 오픈
                ta.value = contentBox.textContent || "";

                contentBox.style.display = "none";
                editBox.style.display = "block";
                return;
        }

        if (act === "q-cancel") {
                const contentBox = wrap.querySelector('[data-role="q-content"]');
                const editBox = wrap.querySelector('[data-role="q-edit"]');
                const ta = editBox.querySelector('textarea');

                // ✅ 취소 시 textarea를 표시 내용으로 되돌림
                ta.value = contentBox.textContent || "";

                editBox.style.display = "none";
                contentBox.style.display = "block";
                return;
        }

        if (act === "q-save") {
             const contentBox = wrap.querySelector('[data-role="q-content"]');
                const editBox = wrap.querySelector('[data-role="q-edit"]');
                const ta = editBox.querySelector('textarea');

                const content = (ta.value || "").trim();
                if (!content) return alert("내용을 입력해 주세요.");

                const res = await fetchWithCsrf(`/api/qna/questions/${qnaId}`, {
                    method: "PUT",
                    body: JSON.stringify({ content })
                });

                if (res.status === 403) return alert("본인 질문만 수정할 수 있어요.");
                if (!res.ok) return alert("수정 실패");

                // ✅ 화면에 즉시 반영
                contentBox.textContent = content;

                // ✅ 편집 모드 닫기 (이게 안 되어서 저장버튼이 계속 보였던 거)
                editBox.style.display = "none";
                contentBox.style.display = "block";

                // ✅ 수정하면 is_resolved는 N이니까 배지도 즉시 갱신
                const badge = wrap.querySelector(".qna-badge");
                if (badge) {
                    badge.className = "qna-badge pending";
                    badge.textContent = "미해결";
                }

                return;
        }

        if (act === "q-del") {
            if (!confirm("정말 삭제할까요? (삭제 시 답변도 함께 숨김 처리됩니다)")) return;

            // ✅ 백엔드가 courseId 파라미터 요구하면 붙여도 됨 (안 받으면 무시됨)
            const url = courseId
                ? `/api/qna/questions/${qnaId}?courseId=${encodeURIComponent(courseId)}`
                : `/api/qna/questions/${qnaId}`;

            const res = await fetchWithCsrf(url, { method: "DELETE" });
            if (res.status === 403) return alert("삭제 권한이 없어요.");
            if (!res.ok) return alert("삭제 실패");

            await qnaLoad(); // ✅ 상태 복원 로직
        }

        if (act === "a-open") {
            wrap.querySelector('[data-role="a-compose"]').style.display = "block";
        }

        if (act === "a-cancel") {
            wrap.querySelector('[data-role="a-compose"]').style.display = "none";
        }

        if (act === "a-create") {
            const ta = wrap.querySelector('[data-role="a-compose"] textarea');
            const hint = wrap.querySelector('[data-role="a-hint"]');
            const resolvedSel = wrap.querySelector('[data-role="resolved"]');

            const content = (ta.value || "").trim();
            if (!content) {
                if (hint) hint.textContent = "답변 내용을 입력해 주세요.";
                return;
            }
            if (hint) hint.textContent = "";

            const payload = {
                qnaId,
                content,
                parentAnswerId: null,
                isResolved: resolvedSel ? resolvedSel.value : null
            };

            // ✅ 백엔드가 courseId 쿼리 요구하면 자동으로 붙임 (요구 안 해도 무시됨)
            const url = courseId
                ? `/api/qna/answers?courseId=${encodeURIComponent(courseId)}`
                : `/api/qna/answers`;

            const res = await fetchWithCsrf(url, {
                method: "POST",
                body: JSON.stringify(payload)
            });

            if (res.status === 403) {
                if (hint) hint.textContent = "관리자(전체/강의)만 답변할 수 있어요.";
                return;
            }
            if (!res.ok) {
                if (hint) hint.textContent = "답변 등록 실패";
                return;
            }

            ta.value = "";
            wrap.querySelector('[data-role="a-compose"]').style.display = "none";
            await qnaLoad(); // ✅ 상태 복원 로직
        }
    });

    return wrap;
}

// ================================================
// 답변 렌더링
// ================================================
function renderAnswer(question, a) {
    const el = document.createElement("div");
    el.className = "qna-answer";
    el.dataset.answerId = a.answerId;

    const adminActions = a.canEdit ? `<button class="qna-link" data-act="a-edit">수정</button>` : ``;
    const adminDelete = a.canDelete ? `<button class="qna-link danger" data-act="a-del">삭제</button>` : ``;
    const replyBtn = a.canReply ? `<button class="qna-link" data-act="a-reply">대댓글</button>` : ``;

    el.innerHTML = `
      <div class="qna-answer-head">
        <div class="qna-meta">
          <span class="qna-nick">${escapeHtml(a.writerNickname || "사용자")}</span>
          <span class="qna-dot">·</span>
          <span class="qna-time">${escapeHtml(a.createdAt || "")}</span>
        </div>
        <div class="qna-actions">
          ${replyBtn}
          ${adminActions}
          ${adminDelete}
        </div>
      </div>

      <div class="qna-content" data-role="a-content">${escapeHtml(a.content || "")}</div>

      <div class="qna-edit" data-role="a-edit" style="display:none;">
        <textarea class="qna-ta">${escapeHtml(a.content || "")}</textarea>

        <div class="qna-row">
          ${question.canSetResolved ? `
            <label class="qna-select-wrap">
              <span>해결여부</span>
              <select class="qna-select" data-role="resolved">
                <option value="N" ${question.isResolved === "N" ? "selected" : ""}>미해결</option>
                <option value="Y" ${question.isResolved === "Y" ? "selected" : ""}>해결</option>
              </select>
            </label>
          ` : ``}

          <button class="qna-btn primary" data-act="a-save">저장</button>
          <button class="qna-btn" data-act="a-cancel">취소</button>
        </div>
      </div>

      <div class="qna-reply" data-role="reply" style="display:none;">
        <textarea class="qna-ta" placeholder="답변에 대한 댓글(대댓글)을 입력하세요."></textarea>
        <div class="qna-row">
          <button class="qna-btn primary" data-act="a-reply-save">등록</button>
          <button class="qna-btn" data-act="a-reply-cancel">취소</button>
        </div>
        <div class="qna-hint" data-role="r-hint"></div>
      </div>
    `;

    // 대댓글(자식) 렌더 + 삭제 버튼
    if (a.children && a.children.length > 0) {
        const childrenBox = document.createElement("div");
        childrenBox.className = "qna-children";

        a.children.forEach(ch => {
            const chEl = document.createElement("div");
            chEl.className = "qna-answer child";
            chEl.dataset.answerId = ch.answerId;

            const delBtn = ch.canDelete
                ? `<button class="qna-link danger" data-act="c-del">삭제</button>`
                : ``;

            chEl.innerHTML = `
              <div class="qna-answer-head">
                <div class="qna-meta">
                  <span class="qna-nick">${escapeHtml(ch.writerNickname || "사용자")}</span>
                  <span class="qna-dot">·</span>
                  <span class="qna-time">${escapeHtml(ch.createdAt || "")}</span>
                </div>
                <div class="qna-actions">
                  ${delBtn}
                </div>
              </div>
              <div class="qna-content">${escapeHtml(ch.content || "")}</div>
            `;

            // ✅ 대댓글 삭제 이벤트
            chEl.addEventListener("click", async (e) => {
                const act = e.target?.dataset?.act;
                if (act !== "c-del") return;

                if (!confirm("대댓글을 삭제할까요?")) return;

                const res = await fetchWithCsrf(`/api/qna/answers/${ch.answerId}`, { method: "DELETE" });
                if (res.status === 403) return alert("삭제 권한이 없어요.");
                if (!res.ok) return alert("삭제 실패");

                chEl.remove();
            });

            childrenBox.appendChild(chEl);
        });

        el.appendChild(childrenBox);
    }


    el.addEventListener("click", async (e) => {
        const act = e.target?.dataset?.act;
        if (!act) return;

        const answerId = a.answerId;
        const courseId = document.getElementById("course-id")?.value;

        if (act === "a-edit") {
            el.querySelector('[data-role="a-content"]').style.display = "none";
            el.querySelector('[data-role="a-edit"]').style.display = "block";
        }

        if (act === "a-cancel") {
            el.querySelector('[data-role="a-edit"]').style.display = "none";
            el.querySelector('[data-role="a-content"]').style.display = "block";
        }

        if (act === "a-save") {
            const ta = el.querySelector('[data-role="a-edit"] textarea');
            const resolvedSel = el.querySelector('[data-role="resolved"]');
            const content = (ta.value || "").trim();
            if (!content) return alert("내용을 입력해 주세요.");

            const payload = {
                content,
                isResolved: resolvedSel ? resolvedSel.value : null
            };

            const url = courseId
                ? `/api/qna/answers/${answerId}?courseId=${encodeURIComponent(courseId)}`
                : `/api/qna/answers/${answerId}`;

            const res = await fetchWithCsrf(url, {
                method: "PUT",
                body: JSON.stringify(payload)
            });

            if (res.status === 403) return alert("수정 권한이 없어요.");
            if (!res.ok) return alert("수정 실패");

            await qnaLoad(); // ✅ 상태 복원
        }

        if (act === "a-del") {
            if (!confirm("이 답변을 삭제할까요?")) return;

            const url = courseId
                ? `/api/qna/answers/${answerId}?courseId=${encodeURIComponent(courseId)}`
                : `/api/qna/answers/${answerId}`;

            const res = await fetchWithCsrf(url, { method: "DELETE" });
            if (res.status === 403) return alert("삭제 권한이 없어요.");
            if (!res.ok) return alert("삭제 실패");

            await qnaLoad(); // ✅ 상태 복원
        }

        if (act === "a-reply") {
            el.querySelector('[data-role="reply"]').style.display = "block";
        }

        if (act === "a-reply-cancel") {
            el.querySelector('[data-role="reply"]').style.display = "none";
        }

        if (act === "a-reply-save") {
            const ta = el.querySelector('[data-role="reply"] textarea');
            const hint = el.querySelector('[data-role="r-hint"]');
            const content = (ta.value || "").trim();

            if (!content) {
                if (hint) hint.textContent = "내용을 입력해 주세요.";
                return;
            }
            if (hint) hint.textContent = "";

            const url = courseId
                ? `/api/qna/answers?courseId=${encodeURIComponent(courseId)}`
                : `/api/qna/answers`;

            const res = await fetchWithCsrf(url, {
                method: "POST",
                body: JSON.stringify({
                    qnaId: question.qnaId,
                    parentAnswerId: answerId,
                    content,
                    isResolved: null
                })
            });

            if (res.status === 403) {
                if (hint) hint.textContent = "대댓글 권한이 없어요.";
                return;
            }
            if (!res.ok) {
                if (hint) hint.textContent = "등록 실패";
                return;
            }

            ta.value = "";
            el.querySelector('[data-role="reply"]').style.display = "none";
            await qnaLoad(); // ✅ 상태 복원
        }
    });

    return el;
}

// ================================================
// util
// ================================================
function escapeHtml(str) {
    return String(str ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#039;");
}
