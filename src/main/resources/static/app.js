// ===== CONFIG =====
const isLocal = window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1";
const API_BASE = isLocal
    ? "http://localhost:8089/api/v1/planner"
    : "https://planner.rkqkdrnportfolio.shop/api/v1/planner";

const $ = (s) => document.querySelector(s);

// ===== DATE UTILS =====
function toSunday(d) {
    const dt = new Date(d);
    dt.setDate(dt.getDate() - dt.getDay());
    dt.setHours(0, 0, 0, 0);
    return dt;
}
function addDays(d, n) { const r = new Date(d); r.setDate(r.getDate() + n); return r; }
function fmtDate(d) {
    const date = new Date(d);
    return new Date(date.getTime() - date.getTimezoneOffset() * 60000).toISOString().split('T')[0];
}

let currentStartDay = toSunday(new Date());

// ===== TOAST =====
function showToast(msg, type = 'error') {
    const container = document.getElementById('toast-container');
    const el = document.createElement('div');
    el.className = `toast toast-${type}`;
    el.textContent = msg;
    container.appendChild(el);
    requestAnimationFrame(() => el.classList.add('show'));
    setTimeout(() => {
        el.classList.remove('show');
        el.addEventListener('transitionend', () => el.remove(), { once: true });
    }, 3000);
}

// ===== API =====
async function api(path, options = {}) {
    const url = path.startsWith("http") ? path : API_BASE + path;
    const res = await fetch(url, { ...options, headers: { "Content-Type": "application/json", ...options.headers } });
    if (!res.ok) throw new Error(`${res.status}`);
    return res;
}

// ===== HTML ESCAPE =====
function esc(str) {
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

// ===== BOARD RENDER =====
function renderBoard(tasks) {
    const board = document.getElementById("board");
    board.innerHTML = "";
    board.classList.remove('loading');

    const dayNames = ["일", "월", "화", "수", "목", "금", "토"];
    const todayStr = fmtDate(new Date());

    for (let i = 0; i < 7; i++) {
        const date = addDays(currentStartDay, i);
        const dateStr = fmtDate(date);
        const isToday = dateStr === todayStr;
        const dayTasks = tasks.filter(t => (t.date || t.scheduledDate) === dateStr);

        const col = document.createElement("div");
        col.className = "column";
        col.innerHTML = `
            <div class="column-header${isToday ? ' today' : ''}">
                <span class="day-name">${dayNames[date.getDay()]}</span>
                <span class="day-date">
                    ${date.getMonth() + 1}.${date.getDate()}
                    ${isToday ? '<span class="today-badge">오늘</span>' : ''}
                </span>
            </div>
            <div class="task-list" data-date="${dateStr}"></div>
        `;
        board.appendChild(col);
        const listEl = col.querySelector(".task-list");

        dayTasks.forEach(t => {
            const isDone = t.status === 'DONE';
            const isSkipped = t.status === 'SKIPPED';
            const item = document.createElement("div");
            item.className = "task-card";

            const badgeClass = isDone ? 'done' : isSkipped ? 'skipped' : 'planned';
            const badgeText = isDone ? 'DONE' : isSkipped ? 'SKIPPED' : 'PLANNED';

            let buttonsHtml;
            if (isDone || isSkipped) {
                buttonsHtml = `
                    <button class="btn-pill" data-action="undo" data-id="${t.id}">undo</button>
                    <button class="btn-pill btn-danger" data-action="delete-ask" data-id="${t.id}">delete</button>
                `;
            } else {
                buttonsHtml = `
                    <button class="btn-pill btn-complete" data-action="complete" data-id="${t.id}">complete</button>
                    <button class="btn-pill btn-skip" data-action="skip" data-id="${t.id}">skip</button>
                    <button class="btn-pill btn-danger" data-action="delete-ask" data-id="${t.id}">delete</button>
                `;
            }

            item.innerHTML = `
                <div class="card-header">
                    <span class="card-title ${isDone || isSkipped ? 'done-text' : ''}"
                          data-action="edit" data-id="${t.id}">${esc(t.title)}</span>
                    <span class="badge ${badgeClass}">${badgeText}</span>
                </div>
                <div class="card-meta"><span>${dateStr}</span></div>
                <div class="card-actions">${buttonsHtml}</div>
            `;
            listEl.appendChild(item);
        });
    }
    updateHeader();
}

function updateHeader() {
    const el = document.getElementById("dateRange");
    if (el) el.innerText = `${fmtDate(currentStartDay)} ~ ${fmtDate(addDays(currentStartDay, 6))}`;
}

// ===== REFRESH =====
async function refresh() {
    const board = document.getElementById("board");
    board.classList.add('loading');
    const from = fmtDate(currentStartDay);
    const to = fmtDate(addDays(currentStartDay, 6));
    const status = $("#statusFilter")?.value || "";
    try {
        const res = await api(`/tasks?from=${from}&to=${to}${status ? `&status=${status}` : ''}`);
        const data = await res.json();
        renderBoard(Array.isArray(data) ? data : (data.data ?? []));
    } catch (e) {
        board.classList.remove('loading');
        showToast("데이터 로드 실패: " + e.message);
    }
}

// ===== INLINE TASK EDIT =====
function startEdit(titleEl, id) {
    const currentTitle = titleEl.textContent;
    const input = document.createElement('input');
    input.className = 'card-title-input';
    input.value = currentTitle;
    titleEl.replaceWith(input);
    input.focus();
    input.select();

    let saved = false;
    async function save() {
        if (saved) return;
        saved = true;
        const newTitle = input.value.trim();
        if (newTitle && newTitle !== currentTitle) {
            try {
                await api(`/tasks/${id}`, { method: "PUT", body: JSON.stringify({ title: newTitle }) });
                showToast("수정됐습니다", "success");
            } catch (e) {
                showToast("수정 실패: " + e.message);
            }
        }
        refresh();
    }
    input.addEventListener('blur', save);
    input.addEventListener('keydown', e => {
        if (e.key === 'Enter') { e.preventDefault(); input.blur(); }
        if (e.key === 'Escape') { saved = true; refresh(); }
    });
}

// ===== INLINE DELETE CONFIRM =====
function showDeleteConfirm(btn, id) {
    const actions = btn.closest('.card-actions');
    actions.innerHTML = `
        <span class="confirm-text">삭제할까요?</span>
        <button class="btn-pill btn-confirm" data-action="delete-confirm" data-id="${id}">확인</button>
        <button class="btn-pill" data-action="delete-cancel">취소</button>
    `;
}

// ===== TASK CREATE =====
async function handleCreateTask() {
    const titleEl = $("#quickTitle");
    const title = titleEl.value.trim();
    const date = $("#quickDate").value || fmtDate(new Date());
    const rule = $("#quickRule").value;
    if (!title) { showToast("제목을 입력하세요"); return; }
    try {
        if (rule === "NONE") {
            await api("/tasks", { method: "POST", body: JSON.stringify({ title, date }) });
        } else {
            await api("/templates", { method: "POST", body: JSON.stringify({ title, ruleType: rule, date, dayOfWeek: null }) });
        }
        titleEl.value = "";
        showToast("추가됐습니다", "success");
        refresh();
    } catch (e) {
        showToast("생성 실패: " + e.message);
    }
}

// ===== TEMPLATES =====
const DAY_LABEL = { SUNDAY: '일', MONDAY: '월', TUESDAY: '화', WEDNESDAY: '수', THURSDAY: '목', FRIDAY: '금', SATURDAY: '토' };

async function loadTemplates() {
    try {
        const res = await api("/templates");
        const list = await res.json();
        const data = Array.isArray(list) ? list : (list.data ?? []);
        const listEl = $("#templateList");
        listEl.innerHTML = "";

        const groups = data.reduce((acc, t) => {
            if (!acc[t.title]) acc[t.title] = { title: t.title, ids: [], days: [] };
            acc[t.title].ids.push(t.id);
            if (t.dayOfWeek) acc[t.title].days.push(t.dayOfWeek);
            return acc;
        }, {});

        if (Object.keys(groups).length === 0) {
            listEl.innerHTML = '<p class="empty-msg">템플릿이 없습니다</p>';
            return;
        }

        Object.values(groups).forEach(g => {
            const item = document.createElement("div");
            item.className = "template-item";
            const dayStr = g.days.map(d => DAY_LABEL[d] || d).join(', ');
            item.innerHTML = `
                <div class="template-info">
                    <span class="template-title" data-taction="edit-name" data-ids="${g.ids.join(',')}">${esc(g.title)}</span>
                    <span class="template-days">${dayStr || '반복 없음'}</span>
                </div>
                <div class="template-btns">
                    <button class="btn-pill btn-danger" data-taction="delete-ask" data-ids="${g.ids.join(',')}">삭제</button>
                </div>
            `;
            listEl.appendChild(item);
        });
    } catch (e) {
        showToast("템플릿 로드 실패");
    }
}

async function createCustomTemplate() {
    const title = $("#newTemplateName").value.trim();
    const checked = Array.from(document.querySelectorAll('#dayCheckboxes input:checked')).map(cb => cb.value);
    if (!title || checked.length === 0) { showToast("이름과 요일을 선택하세요"); return; }
    try {
        await Promise.all(checked.map(d => api("/templates", {
            method: "POST", body: JSON.stringify({ title, ruleType: "WEEKLY", dayOfWeek: d, date: fmtDate(new Date()) })
        })));
        showToast("템플릿이 생성됐습니다", "success");
        $("#newTemplateName").value = "";
        document.querySelectorAll('#dayCheckboxes input:checked').forEach(c => c.checked = false);
        loadTemplates();
        refresh();
    } catch (e) {
        showToast("오류: " + e.message);
    }
}

// ===== INLINE TEMPLATE EDIT =====
function startTemplateEdit(titleEl, ids) {
    const currentTitle = titleEl.textContent;
    const input = document.createElement('input');
    input.className = 'template-edit-input';
    input.value = currentTitle;
    titleEl.replaceWith(input);
    input.focus();
    input.select();

    let saved = false;
    async function save() {
        if (saved) return;
        saved = true;
        const newTitle = input.value.trim();
        if (newTitle && newTitle !== currentTitle) {
            try {
                await Promise.all(ids.map(id => api(`/templates/${id}`, { method: "PUT", body: JSON.stringify({ title: newTitle }) })));
                showToast("수정됐습니다", "success");
            } catch (e) {
                showToast("수정 실패");
            }
        }
        loadTemplates();
    }
    input.addEventListener('blur', save);
    input.addEventListener('keydown', e => {
        if (e.key === 'Enter') { e.preventDefault(); input.blur(); }
        if (e.key === 'Escape') { saved = true; loadTemplates(); }
    });
}

function showTemplateDeleteConfirm(btn, ids) {
    const btns = btn.closest('.template-btns');
    btns.innerHTML = `
        <span class="confirm-text">삭제?</span>
        <button class="btn-pill btn-confirm" data-taction="delete-confirm" data-ids="${ids}">확인</button>
        <button class="btn-pill" data-taction="delete-cancel">취소</button>
    `;
}

// ===== ADMIN BUTTON (인라인 확인) =====
function setupAdminBtn(id, label, action) {
    const btn = document.getElementById(id);
    if (!btn) return;
    btn.addEventListener('click', () => {
        if (btn.dataset.confirming === 'true') return;
        btn.dataset.confirming = 'true';
        const original = btn.textContent;
        btn.classList.add('confirming');
        btn.innerHTML = `${label}? <span class="confirm-yes">확인</span> <span class="confirm-no">취소</span>`;

        btn.querySelector('.confirm-yes').onclick = async (e) => {
            e.stopPropagation();
            try { await action(); } catch (err) { showToast("오류: " + err.message); }
            resetAdminBtn(btn, original);
        };
        btn.querySelector('.confirm-no').onclick = (e) => {
            e.stopPropagation();
            resetAdminBtn(btn, original);
        };
    });
}

function resetAdminBtn(btn, original) {
    btn.dataset.confirming = 'false';
    btn.classList.remove('confirming');
    btn.textContent = original;
}

// ===== INIT =====
document.addEventListener("DOMContentLoaded", () => {
    if ($("#quickDate")) $("#quickDate").value = fmtDate(new Date());

    $("#createTask").onclick = handleCreateTask;
    $("#refresh").onclick = refresh;
    $("#statusFilter").addEventListener('change', refresh);
    $("#prevWeek").onclick = () => { currentStartDay = addDays(currentStartDay, -7); refresh(); };
    $("#nextWeek").onclick = () => { currentStartDay = addDays(currentStartDay, 7); refresh(); };

    // 모달
    const modal = $("#modalBackdrop");
    $("#openTemplateModal").onclick = () => { modal.classList.remove("hidden"); loadTemplates(); };
    $("#closeModal").onclick = () => modal.classList.add("hidden");
    modal.addEventListener('click', e => { if (e.target === modal) modal.classList.add("hidden"); });
    $("#btnCreateTemplate").onclick = createCustomTemplate;

    // 관리 버튼 (인라인 확인)
    setupAdminBtn("dayCloseBtn", "오늘 마감", async () => {
        await api("/day-close", { method: "POST", body: JSON.stringify({ date: fmtDate(new Date()), carryOver: false }) });
        showToast("하루 마감 완료", "success");
        refresh();
    });
    setupAdminBtn("runSchedulerBtn", "강제 생성", async () => {
        await api("/admin/run-scheduler", { method: "POST" });
        showToast("일정 생성 완료", "success");
        refresh();
    });

    // 보드 이벤트 위임
    document.getElementById('board').addEventListener('click', async e => {
        const btn = e.target.closest('[data-action]');
        if (!btn) return;
        const { action, id } = btn.dataset;
        try {
            if (action === 'edit') { startEdit(btn, id); return; }
            if (action === 'delete-ask') { showDeleteConfirm(btn, id); return; }
            if (action === 'delete-cancel') { refresh(); return; }
            if (action === 'complete') { await api(`/tasks/${id}/complete`, { method: "POST" }); showToast("완료 처리됐습니다", "success"); }
            else if (action === 'skip') { await api(`/tasks/${id}/skip`, { method: "POST" }); }
            else if (action === 'undo') { await api(`/tasks/${id}/undo`, { method: "POST" }); showToast("되돌렸습니다", "success"); }
            else if (action === 'delete-confirm') { await api(`/tasks/${id}`, { method: "DELETE" }); showToast("삭제됐습니다", "success"); }
            refresh();
        } catch (e) {
            showToast("오류: " + e.message);
        }
    });

    // 템플릿 이벤트 위임
    $("#templateList").addEventListener('click', async e => {
        const btn = e.target.closest('[data-taction]');
        if (!btn) return;
        const { taction, ids } = btn.dataset;
        const idArr = ids.split(',').map(Number);

        try {
            if (taction === 'edit-name') { startTemplateEdit(btn, idArr); return; }
            if (taction === 'delete-ask') { showTemplateDeleteConfirm(btn, ids); return; }
            if (taction === 'delete-cancel') { loadTemplates(); return; }
            if (taction === 'delete-confirm') {
                await Promise.all(idArr.map(id => api(`/templates/${id}`, { method: "DELETE" })));
                showToast("삭제됐습니다", "success");
                loadTemplates();
            }
        } catch (e) {
            showToast("오류: " + e.message);
        }
    });

    // 메뉴 토글
    $("#menu-toggle").addEventListener('click', e => {
        e.stopPropagation();
        document.querySelector('.panel').classList.toggle('open');
    });
    document.addEventListener('click', e => {
        const panel = document.querySelector('.panel');
        if (panel.classList.contains('open') && !panel.contains(e.target) && e.target !== $("#menu-toggle")) {
            panel.classList.remove('open');
        }
    });

    refresh();
});
