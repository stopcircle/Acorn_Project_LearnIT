let currentPage = 1;

// ✅ notice와 동일한 페이지 블록(5개 단위)
const PAGE_BLOCK_SIZE = 5;

// ✅ 현재 적용중인 필터(서버 페이징 기준으로 동작)
let statusFilters = []; // ex: ["ACTIVE","BANNED"]
let roleFilters = [];   // ex: ["ADMIN","SUB_ADMIN"]

document.addEventListener("DOMContentLoaded", () => {
  document.getElementById("btnSearch")?.addEventListener("click", () => {
    currentPage = 1;
    loadUsers();
  });

  // ✅ 엔터로 검색
  document.getElementById("searchKeyword")?.addEventListener("keydown", (e) => {
    if (e.key === "Enter") {
      e.preventDefault();
      currentPage = 1;
      loadUsers();
    }
  });

  // ✅ 검색/필터 전체 초기화
  document.getElementById("btnResetAll")?.addEventListener("click", () => {
    resetAllConditions();
  });

  // ✅ 필터 팝업 열기
  document.querySelectorAll(".filter-btn").forEach(btn => {
    btn.addEventListener("click", (e) => {
      e.stopPropagation();
      const which = btn.dataset.filter; // status | role
      toggleFilterPopup(which);
    });
  });

  // 팝업 내부 클릭 시 닫히지 않게
  document.querySelectorAll(".filter-popup").forEach(p => {
    p.addEventListener("click", (e) => e.stopPropagation());
  });

  // 바깥 클릭 시 모두 닫기
  document.addEventListener("click", () => {
    closeAllFilterPopups();
  });

  loadUsers();
});

function resetAllConditions() {
  const typeEl = document.getElementById("searchType");
  const kwEl = document.getElementById("searchKeyword");

  if (typeEl) typeEl.value = "email";
  if (kwEl) kwEl.value = "";

  statusFilters = [];
  roleFilters = [];

  currentPage = 1;
  closeAllFilterPopups();
  loadUsers(1);
}

function csrfHeaders() {
  const token = document.querySelector('meta[name="_csrf"]')?.getAttribute("content");
  const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content");
  if (token && header) return { [header]: token };
  return {};
}

async function apiFetch(url, options = {}) {
  const headers = { ...(options.headers || {}), ...csrfHeaders() };
  const res = await fetch(url, { credentials: "same-origin", ...options, headers });

  if (!res.ok) {
    const text = await res.text().catch(() => "");
    throw new Error(text || `HTTP ${res.status}`);
  }

  const ct = res.headers.get("content-type") || "";
  if (ct.includes("application/json")) return await res.json();
  return null;
}

function escapeHtml(str) {
  return String(str)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

// ✅ 서버 전이 규칙과 동일하게(현재 상태 유지 포함)
function allowedNextStatuses(current) {
  if (current === "SIGNUP_PENDING") return ["SIGNUP_PENDING", "ACTIVE"];
  if (current === "ACTIVE") return ["ACTIVE", "BANNED", "DELETE"]; // SIGNUP_PENDING 불가
  if (current === "BANNED") return ["BANNED", "ACTIVE"];
  if (current === "DELETE") return ["DELETE", "ACTIVE"];
  return ["SIGNUP_PENDING", "ACTIVE", "BANNED", "DELETE"];
}

// ✅ 전이 불가 옵션은 disabled가 아니라 "옵션 자체를 제거"해서 아예 안 보이게 함
function applyStatusTransitionRules(statusSelectEl, curr) {
  if (!statusSelectEl) return;

  const allowed = allowedNextStatuses(curr);

  // ✅ 원본 option(text) 백업 (처음 1회)
  if (!statusSelectEl.dataset.allOptionsJson) {
    const all = Array.from(statusSelectEl.options).map(o => ({
      value: o.value,
      text: o.textContent
    }));
    statusSelectEl.dataset.allOptionsJson = JSON.stringify(all);
  }

  const allOptions = JSON.parse(statusSelectEl.dataset.allOptionsJson);

  // ✅ 허용된 옵션만 다시 렌더링 (불가 옵션은 "목록에서 사라짐")
  statusSelectEl.innerHTML = allowed
    .map(v => {
      const found = allOptions.find(x => x.value === v);
      const text = found ? found.text : v;
      return `<option value="${v}">${text}</option>`;
    })
    .join("");

  // ✅ 기본 선택값은 항상 현재 상태
  statusSelectEl.value = curr;
}

function setStatusEditMode(tr, on) {
  tr.classList.toggle("status-editing", !!on);

  const sel = tr.querySelector(".status");
  const btn = tr.querySelector(".btn-status");
  const pendingBox = tr.querySelector(".pending-box");
  const nickname = tr.querySelector(".nickname");
  const phone = tr.querySelector(".phone");

  if (sel) sel.disabled = !on;
  if (nickname) nickname.disabled = !on;
  if (phone) phone.disabled = !on;

  if (!on) {
    if (pendingBox) pendingBox.style.display = "none";
    if (btn) btn.textContent = "수정";
  } else {
    if (btn) btn.textContent = "저장";
  }
}

function setRoleEditMode(tr, on) {
  tr.classList.toggle("role-editing", !!on);

  const roleSel = tr.querySelector(".role");
  const btn = tr.querySelector(".btn-role");

  const kw = tr.querySelector(".course-keyword");
  const courseSel = tr.querySelector(".course-select");
  const btnCourseSearch = tr.querySelector(".btn-course-search");
  const btnCourseAdd = tr.querySelector(".btn-course-add");

  if (roleSel) roleSel.disabled = !on;
  if (kw) kw.disabled = !on;
  if (courseSel) courseSel.disabled = !on;
  if (btnCourseSearch) btnCourseSearch.disabled = !on;
  if (btnCourseAdd) btnCourseAdd.disabled = !on;

  if (btn) btn.textContent = on ? "저장" : "수정";
}

/**
 * ✅ SUB_ADMIN 관리강의 태그 표시를 "courseId - title"로 통일
 */
function buildManagedTag(courseId, title) {
  const label = `${courseId} - ${title}`;
  return `
    <span class="tag" data-id="${courseId}" data-title="${escapeHtml(title)}">
      <span class="tag-text">${escapeHtml(label)}</span>
      <button type="button" class="tag-del" title="삭제" aria-label="삭제">×</button>
    </span>
  `;
}

async function loadUsers(page = currentPage) {
  currentPage = page;

  const type = document.getElementById("searchType")?.value || "email";
  const keyword = document.getElementById("searchKeyword")?.value?.trim() || "";

  const qs = new URLSearchParams();
  qs.set("type", type);
  qs.set("keyword", keyword);
  qs.set("page", String(page));
  qs.set("size", "7");
  if (statusFilters.length > 0) qs.set("statuses", statusFilters.join(","));
  if (roleFilters.length > 0) qs.set("roles", roleFilters.join(","));

  const data = await apiFetch(`/api/admin/users?${qs.toString()}`, { method: "GET" });

  // ✅ 서버 GROUP BY 결과로 필터 목록 생성 (전체 페이징 기준)
  renderFilterPopups(data.facets || {}, data.appliedFilters || {});
  renderAppliedChips();        // ✅ 상단 칩 표시
  updateFilterButtonState();   // ✅ 필터 버튼 강조

  renderUsers(data.items || []);
  renderPagination(data.page, data.totalPages);
  renderTotalCount(data.totalCount);
}

function closeAllFilterPopups() {
  document.querySelectorAll(".filter-popup").forEach(p => (p.style.display = "none"));
}

function toggleFilterPopup(which) {
  const popup = which === "status"
    ? document.getElementById("statusFilterPopup")
    : document.getElementById("roleFilterPopup");

  if (!popup) return;

  // 다른 팝업은 닫고
  document.querySelectorAll(".filter-popup").forEach(p => {
    if (p !== popup) p.style.display = "none";
  });

  popup.style.display = (popup.style.display === "block") ? "none" : "block";
}

function updateFilterButtonState() {
  const statusBtn = document.querySelector('.filter-btn[data-filter="status"]');
  const roleBtn = document.querySelector('.filter-btn[data-filter="role"]');

  if (statusBtn) statusBtn.classList.toggle("filter-on", statusFilters.length > 0);
  if (roleBtn) roleBtn.classList.toggle("filter-on", roleFilters.length > 0);
}

function renderAppliedChips() {
  const wrap = document.getElementById("appliedFilters");
  if (!wrap) return;

  const type = document.getElementById("searchType")?.value || "email";
  const keyword = document.getElementById("searchKeyword")?.value?.trim() || "";

  const chips = [];

  if (keyword) {
    const label = `검색(${type}): ${keyword}`;
    chips.push({
      key: "keyword",
      label,
      onDelete: () => {
        const kwEl = document.getElementById("searchKeyword");
        if (kwEl) kwEl.value = "";
        currentPage = 1;
        loadUsers(1);
      }
    });
  }

  statusFilters.forEach(v => {
    chips.push({
      key: `status:${v}`,
      label: `상태: ${v}`,
      onDelete: () => {
        statusFilters = statusFilters.filter(x => x !== v);
        currentPage = 1;
        loadUsers(1);
      }
    });
  });

  roleFilters.forEach(v => {
    chips.push({
      key: `role:${v}`,
      label: `권한: ${v}`,
      onDelete: () => {
        roleFilters = roleFilters.filter(x => x !== v);
        currentPage = 1;
        loadUsers(1);
      }
    });
  });

  if (chips.length === 0) {
    wrap.innerHTML = "";
    return;
  }

  wrap.innerHTML = chips.map(c => `
    <span class="filter-chip" data-key="${escapeHtml(c.key)}">
      <span>${escapeHtml(c.label)}</span>
      <button type="button" class="chip-del" aria-label="조건 제거">×</button>
    </span>
  `).join("");

  wrap.querySelectorAll(".filter-chip").forEach(el => {
    const key = el.getAttribute("data-key");
    const chip = chips.find(x => x.key === key);
    el.querySelector(".chip-del")?.addEventListener("click", () => chip?.onDelete?.());
  });
}

function renderFilterPopups(facets, applied) {
  // 서버가 내려준 appliedFilters 기준으로 체크 상태 복구
  if (Array.isArray(applied.statuses)) statusFilters = applied.statuses;
  if (Array.isArray(applied.roles)) roleFilters = applied.roles;

  renderSingleFilterPopup({
    popupEl: document.getElementById("statusFilterPopup"),
    title: "상태",
    items: Array.isArray(facets.statuses) ? facets.statuses : [],
    selected: statusFilters,
    onApply: (nextSelected) => {
      statusFilters = nextSelected;
      currentPage = 1;
      closeAllFilterPopups();
      loadUsers(1);
    },
    onClear: () => {
      statusFilters = [];
      currentPage = 1;
      closeAllFilterPopups();
      loadUsers(1);
    }
  });

  renderSingleFilterPopup({
    popupEl: document.getElementById("roleFilterPopup"),
    title: "권한",
    items: Array.isArray(facets.roles) ? facets.roles : [],
    selected: roleFilters,
    onApply: (nextSelected) => {
      roleFilters = nextSelected;
      currentPage = 1;
      closeAllFilterPopups();
      loadUsers(1);
    },
    onClear: () => {
      roleFilters = [];
      currentPage = 1;
      closeAllFilterPopups();
      loadUsers(1);
    }
  });
}

function renderSingleFilterPopup({ popupEl, title, items, selected, onApply, onClear }) {
  if (!popupEl) return;

  // items: [{value:'ACTIVE', cnt: 10}, ...]
  const normalized = items
    .map(it => ({
      value: String(it.value ?? ""),
      cnt: Number(it.cnt ?? 0)
    }))
    .filter(it => it.value);

  const selectedSet = new Set((selected || []).map(String));

  popupEl.innerHTML = `
    <div class="filter-title">${escapeHtml(title)} 필터</div>
    <div class="filter-list">
      ${normalized.map(it => {
        const id = `${popupEl.id}__${it.value}`;
        const checked = selectedSet.has(it.value) ? "checked" : "";
        return `
          <label for="${id}">
            <input id="${id}" type="checkbox" class="filter-check" value="${escapeHtml(it.value)}" ${checked}>
            <span>${escapeHtml(it.value)}</span>
            <span style="margin-left:auto; opacity:0.6; font-size:12px;">${it.cnt}</span>
          </label>
        `;
      }).join("")}
      ${normalized.length === 0 ? `<div style="font-size:12px; color:#777;">(데이터 없음)</div>` : ""}
    </div>
    <div class="filter-actions">
      <button type="button" class="filter-clear">초기화</button>
      <button type="button" class="filter-apply">적용</button>
    </div>
  `;

  popupEl.querySelector(".filter-apply")?.addEventListener("click", () => {
    const checked = [...popupEl.querySelectorAll(".filter-check:checked")]
      .map(el => el.value)
      .filter(Boolean);
    onApply(checked);
  });

  popupEl.querySelector(".filter-clear")?.addEventListener("click", () => {
    onClear();
  });
}

function renderUsers(users) {
  const tbody = document.getElementById("userTbody");
  if (!tbody) return;
  tbody.innerHTML = "";

  users.forEach(u => {
    const tr = document.createElement("tr");

    const managedHtml = (u.managedCourses || [])
      .map(c => buildManagedTag(c.courseId, c.title))
      .join(" ");

    tr.innerHTML = `
      <td>${u.userId}</td>
      <td>${escapeHtml(u.name || "")}</td>
      <td>${escapeHtml(u.email || "")}</td>

      <td>
        <div class="cell-line">
          <select class="status" disabled>
            ${["SIGNUP_PENDING","ACTIVE","BANNED","DELETE"].map(s =>
              `<option value="${s}" ${u.status===s?"selected":""}>${s}</option>`
            ).join("")}
          </select>
          <button class="btn-status" type="button">수정</button>
        </div>

        <div class="pending-box" style="display:none;">
          <input class="nickname" placeholder="닉네임" disabled />
          <input class="phone" placeholder="010-0000-0000" disabled />
          <div class="hint">소셜 SIGNUP_PENDING → ACTIVE는 닉네임/전화번호가 필요합니다.</div>
        </div>
      </td>

      <td>
        <div class="cell-line">
          <select class="role" disabled>
            ${["USER","SUB_ADMIN","ADMIN"].map(r =>
              `<option value="${r}" ${u.role===r?"selected":""}>${r}</option>`
            ).join("")}
          </select>
          <button class="btn-role" type="button">수정</button>
        </div>

        <div class="subadmin-box" style="display:${u.role==="SUB_ADMIN" ? "block" : "none"};">
          <div class="sub-line">
            <input class="course-keyword" placeholder="강의 검색" disabled />
            <button class="btn-course-search" type="button" disabled>검색</button>
          </div>
          <div class="sub-line">
            <select class="course-select" disabled></select>
            <button class="btn-course-add" type="button" disabled>+</button>
          </div>
          <div class="managed">${managedHtml}</div>
        </div>
      </td>
    `;

    tbody.appendChild(tr);

    // ✅ 전이 불가 옵션은 아예 안 보이게(옵션 제거)
    const statusSel = tr.querySelector(".status");
    applyStatusTransitionRules(statusSel, u.status);

    const pendingBox = tr.querySelector(".pending-box");
    const roleSel = tr.querySelector(".role");
    const subBox = tr.querySelector(".subadmin-box");

    roleSel?.addEventListener("change", () => {
      if (subBox) subBox.style.display = (roleSel.value === "SUB_ADMIN") ? "block" : "none";
    });

    // ✅ change에서 전이룰 재적용하면 선택값이 초기화되므로 여기서는 pending box만 제어
    statusSel?.addEventListener("change", () => {
      const isSocial = u.provider && String(u.provider).toLowerCase() !== "local";
      if (u.status === "SIGNUP_PENDING" && statusSel.value === "ACTIVE" && isSocial) {
        if (pendingBox) pendingBox.style.display = "block";
      } else {
        if (pendingBox) pendingBox.style.display = "none";
      }
    });

    // 기본은 보기모드(전부 비활성화)
    setStatusEditMode(tr, false);
    setRoleEditMode(tr, false);

    // ✅ 상태: 수정 -> 저장 토글
    tr.querySelector(".btn-status")?.addEventListener("click", async () => {
      const editing = tr.classList.contains("status-editing");
      if (!editing) {
        setStatusEditMode(tr, true);
        return;
      }
      await saveStatus(u, tr);
    });

    // ✅ 권한: 수정 -> 저장 토글
    tr.querySelector(".btn-role")?.addEventListener("click", async () => {
      const editing = tr.classList.contains("role-editing");
      if (!editing) {
        setRoleEditMode(tr, true);

        // ✅ UX: 수정 눌렀을 때 (SUB_ADMIN이면) 기본으로 전체 강의 로딩
        const roleValue = tr.querySelector(".role")?.value;
        if (roleValue === "SUB_ADMIN") {
          searchCourse(tr); // 빈칸이면 전체 호출됨
        }
        return;
      }
      await saveRole(u, tr);
    });

    tr.querySelector(".btn-course-search")?.addEventListener("click", () => {
      if (!tr.classList.contains("role-editing")) return;
      searchCourse(tr);
    });

    tr.querySelector(".btn-course-add")?.addEventListener("click", () => {
      if (!tr.classList.contains("role-editing")) return;
      addCourse(tr);
    });

    // ✅ SUB_ADMIN 태그 “삭제(×)” - 수정모드에서만 + 즉시 서버 반영
    tr.querySelector(".managed")?.addEventListener("click", (ev) => {
      const btn = ev.target.closest?.(".tag-del");
      if (!btn) return;
      if (!tr.classList.contains("role-editing")) return;

      const tag = btn.closest(".tag");
      const courseId = Number(tag?.dataset?.id);
      if (!courseId) return;

      const tags = tr.querySelectorAll(".managed .tag");
      if (tags.length <= 1) {
        alert("SUB_ADMIN은 최소 1개 이상의 관리 강의가 필요합니다.");
        return;
      }

      deleteManagedCourse(u.userId, courseId, tag);
    });
  });
}

async function saveStatus(u, tr) {
  const next = tr.querySelector(".status")?.value;
  if (!next || next === u.status) {
    setStatusEditMode(tr, false);
    return;
  }
  const isSocial = u.provider && String(u.provider).toLowerCase() !== "local";

  let nickname = null;
  let phone = null;

  if (u.status === "SIGNUP_PENDING" && next === "ACTIVE" && isSocial) {
    nickname = tr.querySelector(".nickname")?.value?.trim() || "";
    phone = tr.querySelector(".phone")?.value?.trim() || "";
    if (!nickname || !phone) {
      alert("닉네임/전화번호가 필요합니다.");
      return;
    }
  }

  try {
    await apiFetch(`/api/admin/users/${u.userId}/status`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ status: next, nickname, phone })
    });
    alert("상태 변경 완료");
    setStatusEditMode(tr, false);
    loadUsers();
  } catch (e) {
    alert(e.message || "상태 변경 실패");
  }
}

async function saveRole(u, tr) {
  const role = tr.querySelector(".role")?.value;
  const isSocial = u.provider && String(u.provider).toLowerCase() !== "local";

  if (isSocial && (role === "ADMIN" || role === "SUB_ADMIN")) {
    alert("소셜 가입 회원에게 ADMIN/SUB_ADMIN 권한을 부여할 수 없습니다.");
    return;
  }

  let courseIds = [];
  if (role === "SUB_ADMIN") {
    courseIds = [...tr.querySelectorAll(".managed .tag")]
      .map(s => Number(s.dataset.id))
      .filter(Boolean);

    if (courseIds.length < 1) {
      alert("SUB_ADMIN은 1개 이상의 관리 강의가 필요합니다.");
      return;
    }
  }

  try {
    await apiFetch(`/api/admin/users/${u.userId}/role`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ role, courseIds })
    });
    alert("권한 변경 완료");
    setRoleEditMode(tr, false);
    loadUsers();
  } catch (e) {
    alert(e.message || "권한 변경 실패");
  }
}

/**
 * ✅ 강의 검색
 * - 빈칸이면 전체 호출: /api/admin/courses
 * - 검색어 있으면: /api/admin/courses?keyword=...
 * - 응답이 배열(List) 또는 {items:[...]} 둘 다 대응
 * - 표시: course_id - 강의명
 */
async function searchCourse(tr) {
  const kw = tr.querySelector(".course-keyword")?.value?.trim() || "";

  try {
    const url = kw
      ? `/api/admin/courses?keyword=${encodeURIComponent(kw)}`
      : `/api/admin/courses`;

    const data = await apiFetch(url, { method: "GET" });

    const list = Array.isArray(data) ? data : (data.items || []);

    const sel = tr.querySelector(".course-select");
    if (!sel) return;

    sel.innerHTML = "";

    list.forEach(c => {
      const courseId = c.courseId ?? c.course_id ?? c.id;
      const title = c.title ?? c.name ?? "";

      if (!courseId) return;

      const opt = document.createElement("option");
      opt.value = String(courseId);

      // ✅ 표시: course_id - 강의명
      opt.textContent = `${courseId} - ${title}`;

      // ✅ 실제 title은 dataset에 보관 (addCourse에서 그대로 사용)
      opt.dataset.title = String(title);

      sel.appendChild(opt);
    });

  } catch (e) {
    alert(e.message || "강의 검색 실패");
  }
}

/**
 * ✅ 강의 추가
 * - option 표시(text)는 "id - title"
 * - 실제 title은 option.dataset.title에서 가져와 태그 생성
 */
function addCourse(tr) {
  const sel = tr.querySelector(".course-select");
  if (!sel || !sel.value) return;

  const list = tr.querySelector(".managed");
  if (!list) return;

  const courseId = String(sel.value);

  const exists = [...list.querySelectorAll(".tag")].some(t => t.dataset.id === courseId);
  if (exists) return;

  const title = sel.selectedOptions[0]?.dataset?.title || "";

  list.insertAdjacentHTML("beforeend", buildManagedTag(courseId, title));
}

async function deleteManagedCourse(userId, courseId, tagEl) {
  try {
    await apiFetch(`/api/admin/users/${userId}/sub-admin/courses/${courseId}`, { method: "DELETE" });
    tagEl?.remove();
  } catch (e) {
    alert(e.message || "삭제 실패");
  }
}

// ✅ 공지(/admin/notice)와 동일한 “5개 단위 페이지 블록” 페이징
function renderPagination(page, totalPages) {
  const el = document.getElementById("pagination");
  if (!el) return;

  el.innerHTML = "";
  if (!totalPages || totalPages <= 1) return;

  const safePage = Math.max(1, Number(page || 1));
  const safeTotal = Math.max(1, Number(totalPages || 1));

  const startPage = Math.floor((safePage - 1) / PAGE_BLOCK_SIZE) * PAGE_BLOCK_SIZE + 1;
  const endPage = Math.min(startPage + PAGE_BLOCK_SIZE - 1, safeTotal);

  // ✅ 이전(블록 단위)
  if (startPage > 1) {
    const prev = document.createElement("button");
    prev.type = "button";
    prev.className = "page-link";
    prev.textContent = "이전";
    prev.addEventListener("click", () => loadUsers(startPage - 1));
    el.appendChild(prev);
  }

  // ✅ 페이지 번호(블록)
  for (let p = startPage; p <= endPage; p++) {
    const b = document.createElement("button");
    b.type = "button";
    b.className = (p === safePage) ? "page-number active" : "page-number";
    b.textContent = String(p);
    b.disabled = (p === safePage);
    b.addEventListener("click", () => loadUsers(p));
    el.appendChild(b);
  }

  // ✅ 다음(블록 단위)
  if (endPage < safeTotal) {
    const next = document.createElement("button");
    next.type = "button";
    next.className = "page-link";
    next.textContent = "다음";
    next.addEventListener("click", () => loadUsers(endPage + 1));
    el.appendChild(next);
  }
}

function renderTotalCount(totalCount) {
  const el = document.getElementById("totalCount");
  if (!el) return;

  const n = Number(totalCount ?? 0);
  el.innerHTML = `총 <strong>${n.toLocaleString("ko-KR")}</strong>명`;
}
