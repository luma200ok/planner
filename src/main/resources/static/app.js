// app.js
// 사용 API
// GET    /tasks?from&to&status
// POST   /tasks
// POST   /tasks/{id}/complete | /undo | /skip
// DELETE /tasks/{id}
//
// GET    /reports/templates
// POST   /templates
// GET    /templates/{id}/items
// POST   /templates/{id}/items
// POST   /templates/{id}/generate  (쿼리/바디 어느 쪽이든 자동으로 맞춰 시도)
const $ = (s) => document.querySelector(s);

const prevWeekBtn = $("#prevWeek");
const nextWeekBtn = $("#nextWeek");
const refreshBtn = $("#refresh");
const weekRangeEl = $("#weekRange");
const weekMetaEl = $("#weekMeta");
const boardEl = $("#board");

const quickTitleEl = $("#quickTitle");
const quickDateEl = $("#quickDate");
const quickStatusEl = $("#quickStatus");
const createTaskBtn = $("#createTask");

const statusFilterEl = $("#statusFilter");
const applyFilterBtn = $("#applyFilter");

const templateSelectEl = $("#templateSelect");
const templateStartEl = $("#templateStart");
const weekNameEl = $("#weekName");
const loadTemplatesBtn = $("#loadTemplates");
const openTemplateModalBtn = $("#openTemplateModal");
const generateFromTemplateBtn = $("#generateFromTemplate");

const modalBackdrop = $("#modalBackdrop");
const closeModalBtn = $("#closeModal");

const newTemplateNameEl = $("#newTemplateName");
const newTemplateRuleTypeEl = $("#newTemplateRuleType");
const newTemplateDayOfWeekEl = $("#newTemplateDayOfWeek");
const createTemplateBtn = $("#createTemplate");

const itemTemplateSelectEl = $("#itemTemplateSelect");
const itemTitleEl = $("#itemTitle");
const itemRuleTypeEl = $("#itemRuleType");
const itemDayOfWeekEl = $("#itemDayOfWeek");
const addTemplateItemBtn = $("#addTemplateItem");

const itemsViewTemplateSelectEl = $("#itemsViewTemplateSelect");
const loadTemplateItemsBtn = $("#loadTemplateItems");
const templateItemsListEl = $("#templateItemsList");

let currentMonday = toMonday(new Date());
let currentStatusFilter = "ALL";
let templatesCache = [];

init();

function init() {
    quickDateEl.value = fmtDate(new Date());
    templateStartEl.value = fmtDate(currentMonday);
    if (weekNameEl) weekNameEl.value = makeDefaultWeekName(currentMonday);

    // 모달: WEEKLY일 때만 요일 선택 활성
    syncNewTemplateDayOfWeekUI();
    newTemplateRuleTypeEl?.addEventListener("change", syncNewTemplateDayOfWeekUI);

    // 모달: 아이템 RuleType이 WEEKLY일 때만 요일 선택 활성
    syncItemDayOfWeekUI();
    itemRuleTypeEl?.addEventListener("change", syncItemDayOfWeekUI);

    prevWeekBtn.addEventListener("click", () => {
        currentMonday = addDays(currentMonday, -7);
        templateStartEl.value = fmtDate(currentMonday);
        if (weekNameEl) weekNameEl.value = makeDefaultWeekName(currentMonday);
        refresh();
    });

    nextWeekBtn.addEventListener("click", () => {
        currentMonday = addDays(currentMonday, 7);
        templateStartEl.value = fmtDate(currentMonday);
        if (weekNameEl) weekNameEl.value = makeDefaultWeekName(currentMonday);
        refresh();
    });

    refreshBtn.addEventListener("click", refresh);

    applyFilterBtn.addEventListener("click", () => {
        currentStatusFilter = statusFilterEl.value;
        refresh();
    });

    createTaskBtn.addEventListener("click", onCreateTask);

    loadTemplatesBtn.addEventListener("click", loadTemplates);
    openTemplateModalBtn.addEventListener("click", () => openModal(true));
    closeModalBtn.addEventListener("click", () => openModal(false));
    modalBackdrop.addEventListener("click", (e) => {
        if (e.target === modalBackdrop) openModal(false);
    });

    createTemplateBtn.addEventListener("click", onCreateTemplate);
    addTemplateItemBtn.addEventListener("click", onAddTemplateItem);
    loadTemplateItemsBtn.addEventListener("click", onLoadTemplateItems);

    generateFromTemplateBtn.addEventListener("click", onGenerateFromTemplate);

    // 초기 로드
    loadTemplates().then(refresh);
}

async function refresh() {
    const { monday, sunday } = weekRange(currentMonday);

    weekRangeEl.textContent = `${fmtDate(monday)} ~ ${fmtDate(sunday)}`;
    weekMetaEl.textContent = `Week of ${fmtDate(monday)} (MON)`;

    const tasks = await fetchWeekTasks(monday, sunday, currentStatusFilter);
    renderBoard(monday, tasks);
}

async function fetchWeekTasks(monday, sunday, status) {
    const from = fmtDate(monday);
    const to = fmtDate(sunday);

    const q = new URLSearchParams();
    q.set("from", from);
    q.set("to", to);
    if (status && status !== "ALL") q.set("status", status);

    const res = await api(`/tasks?${q.toString()}`, { method: "GET" });
    if (!res.ok) {
        const text = await safeText(res);
        alert(`GET /tasks 실패\n${res.status}\n${text}`);
        return [];
    }
    return res.json();
}

function renderBoard(monday, tasks) {
    const byDate = new Map();
    for (let i = 0; i < 7; i++) {
        const d = fmtDate(addDays(monday, i));
        byDate.set(d, []);
    }
    (tasks || []).forEach(t => {
        const d = t.scheduledDate || t.date || t.day || null;
        if (d && byDate.has(d)) byDate.get(d).push(t);
    });

    boardEl.innerHTML = "";

    for (let i = 0; i < 7; i++) {
        const dateObj = addDays(monday, i);
        const date = fmtDate(dateObj);
        const dayName = dayKOR(dateObj);

        const day = document.createElement("section");
        day.className = "day";

        const head = document.createElement("div");
        head.className = "day-head";

        const left = document.createElement("div");
        left.innerHTML = `
      <div class="day-title">${dayName}</div>
      <div class="day-date">${date}</div>
    `;

        const count = document.createElement("div");
        count.className = "day-count";
        count.textContent = `${(byDate.get(date) || []).length}개`;

        head.appendChild(left);
        head.appendChild(count);

        const body = document.createElement("div");
        body.className = "day-body";

        const list = (byDate.get(date) || [])
            .slice()
            .sort((a, b) => (a.id ?? 0) - (b.id ?? 0));

        list.forEach(t => body.appendChild(renderTaskCard(t)));

        day.appendChild(head);
        day.appendChild(body);
        boardEl.appendChild(day);
    }
}

function renderTaskCard(t) {
    const status = (t.status || "PLANNED").toUpperCase();
    const badgeCls = status === "DONE" ? "done" : status === "SKIPPED" ? "skipped" : "planned";

    const el = document.createElement("div");
    el.className = "task";

    const title = escapeHtml(t.title ?? "(no-title)");
    const scheduledDate = t.scheduledDate ?? "-";
    const id = t.id;

    el.innerHTML = `
    <div class="task-top">
      <div class="task-title">${title}</div>
      <div class="badge ${badgeCls}">${status}</div>
    </div>
    <div class="task-meta">
      <span>#${id}</span>
      <span>${scheduledDate}</span>
      ${t.templateId != null ? `<span>templateId: ${t.templateId}</span>` : ""}
      ${t.ruleType ? `<span>rule: ${t.ruleType}</span>` : ""}
      ${t.templateDayOfWeek ? `<span>dow: ${t.templateDayOfWeek}</span>` : ""}
    </div>
    <div class="task-actions"></div>
  `;

    const actions = el.querySelector(".task-actions");

    const btnComplete = mkBtn("complete", async () => {
        await postAction(`/tasks/${id}/complete`);
    });

    const btnSkip = mkBtn("skip", async () => {
        await postAction(`/tasks/${id}/skip`);
    });

    const btnUndo = mkBtn("undo", async () => {
        await postAction(`/tasks/${id}/undo`);
    });

    const btnDelete = mkBtn("delete", async () => {
        if (!confirm(`삭제할까?\n#${id} ${t.title}`)) return;
        const res = await api(`/tasks/${id}`, { method: "DELETE" });
        if (!res.ok) {
            alert(`DELETE 실패\n${res.status}\n${await safeText(res)}`);
            return;
        }
        refresh();
    });

    if (status === "PLANNED") {
        actions.append(btnComplete, btnSkip, btnDelete);
    } else if (status === "DONE") {
        actions.append(btnUndo, btnDelete);
    } else if (status === "SKIPPED") {
        actions.append(btnUndo, btnDelete);
    } else {
        actions.append(btnDelete);
    }

    return el;

    function mkBtn(text, onClick) {
        const b = document.createElement("button");
        b.className = "btn ghost";
        b.textContent = text;
        b.addEventListener("click", onClick);
        return b;
    }

    async function postAction(path) {
        const res = await api(path, { method: "POST" });
        if (!res.ok) {
            alert(`POST ${path} 실패\n${res.status}\n${await safeText(res)}`);
            return;
        }
        refresh();
    }
}

async function onCreateTask() {
    const title = (quickTitleEl.value || "").trim();
    const scheduledDate = quickDateEl.value;

    if (!title) return alert("제목을 입력해줘");
    if (!scheduledDate) return alert("날짜를 선택해줘");

    const res = await api("/tasks", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ title, scheduledDate }),
    });

    if (!res.ok) {
        alert(`POST /tasks 실패\n${res.status}\n${await safeText(res)}`);
        return;
    }

    const created = await res.json().catch(() => null);

    const desired = quickStatusEl.value;
    if (created && created.id && desired && desired !== "PLANNED") {
        const id = created.id;
        if (desired === "DONE") await api(`/tasks/${id}/complete`, { method: "POST" });
        if (desired === "SKIPPED") await api(`/tasks/${id}/skip`, { method: "POST" });
    }

    quickTitleEl.value = "";
    currentMonday = toMonday(new Date(scheduledDate));
    templateStartEl.value = fmtDate(currentMonday);
    if (weekNameEl) weekNameEl.value = makeDefaultWeekName(currentMonday);
    refresh();
}

async function loadTemplates() {
    // 백엔드 라우팅이 /templates 또는 /reports/templates로 갈라진 경우를 대비
    let res = await api("/templates", { method: "GET" });
    if (!res.ok) {
        res = await api("/reports/templates", { method: "GET" });
    }

    if (!res.ok) {
        console.warn("GET templates 실패", res.status, await safeText(res));
        templatesCache = [];
        renderTemplateOptions([]);
        return;
    }

    const templates = await res.json();

    const normalized = (templates || []).map(t => ({
        id: t.id,
        title: t.title ?? t.name ?? `(no-title-${t.id})`,
        ruleType: t.ruleType,
        dayOfWeek: t.dayOfWeek,
    }));

    templatesCache = normalized;
    renderTemplateOptions(normalized);
}

function renderTemplateOptions(list) {
    templateSelectEl.innerHTML = "";
    itemTemplateSelectEl.innerHTML = "";
    itemsViewTemplateSelectEl.innerHTML = "";

    if (!list.length) {
        const opt1 = new Option("(templates 없음)", "");
        templateSelectEl.appendChild(opt1);
        templateSelectEl.disabled = true;

        const opt2 = new Option("(templates 없음)", "");
        itemTemplateSelectEl.appendChild(opt2);

        const opt3 = new Option("(templates 없음)", "");
        itemsViewTemplateSelectEl.appendChild(opt3);
        return;
    }

    templateSelectEl.disabled = false;

    templateSelectEl.appendChild(new Option("템플릿 선택", ""));
    itemTemplateSelectEl.appendChild(new Option("대상 템플릿 선택", ""));
    itemsViewTemplateSelectEl.appendChild(new Option("조회할 템플릿 선택", ""));

    for (const t of list) {
        const label = `${t.title} (${t.ruleType}${t.dayOfWeek ? `/${t.dayOfWeek}` : ""})`;
        templateSelectEl.appendChild(new Option(label, String(t.id)));
        itemTemplateSelectEl.appendChild(new Option(label, String(t.id)));
        itemsViewTemplateSelectEl.appendChild(new Option(label, String(t.id)));
    }
}

async function onCreateTemplate() {
    const title = (newTemplateNameEl.value || "").trim();
    if (!title) return alert("템플릿 이름을 입력해줘");

    const ruleType = (newTemplateRuleTypeEl?.value || "DAILY").trim();

    let dayOfWeek = null;
    if (ruleType === "WEEKLY") {
        dayOfWeek = (newTemplateDayOfWeekEl?.value || "").trim();
        if (!dayOfWeek) return alert("WEEKLY는 요일을 선택해줘");
    }

    const body = { title, ruleType, dayOfWeek };

    const res = await api("/templates", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
    });

    if (!res.ok) {
        alert(`POST /templates 실패\n${res.status}\n${await safeText(res)}`);
        return;
    }

    newTemplateNameEl.value = "";
    await loadTemplates();
    alert("템플릿 생성 완료");
}

async function onAddTemplateItem() {
    const templateId = itemTemplateSelectEl.value;
    if (!templateId) return alert("템플릿을 선택해줘");

    const title = (itemTitleEl.value || "").trim();
    if (!title) return alert("아이템 제목을 입력해줘");

    const ruleType = itemRuleTypeEl.value;
    const dayOfWeek = itemDayOfWeekEl.value || null;

    if (ruleType === "WEEKLY" && !dayOfWeek) {
        return alert("WEEKLY는 요일을 꼭 선택해줘");
    }

    const payload = {
        title,
        ruleType,
    };

    if (ruleType === "WEEKLY") {
        payload.dayOfWeek = itemDayOfWeekEl.value;
    }

    const res = await api(`/templates/${templateId}/items`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
    });

    if (!res.ok) {
        alert(`POST /templates/${templateId}/items 실패\n${res.status}\n${await safeText(res)}`);
        return;
    }

    itemTitleEl.value = "";
    alert("아이템 추가 완료");
}

const btnDeleteTemplateEl = document.getElementById("btnDeleteTemplate");
btnDeleteTemplateEl.addEventListener("click", onDeleteTemplate);

async function onDeleteTemplate() {
    const id =
        (itemTemplateSelectEl?.value || "").trim()
        || (templateSelectEl?.value || "").trim();

    if (!id) return alert("삭제할 템플릿을 선택해줘");

    const ok = confirm("정말 삭제할까? (템플릿 아이템도 같이 삭제돼)");
    if (!ok) return;

    const res = await api(`/templates/${id}`, { method: "DELETE" });

    if (!res.ok) {
        alert(`DELETE /templates/${id} 실패\n${res.status}\n${await safeText(res)}`);
        return;
    }

    await loadTemplates();
    alert("템플릿 삭제 완료");
}

async function onLoadTemplateItems() {
    const templateId = itemsViewTemplateSelectEl.value;
    if (!templateId) return alert("템플릿을 선택해줘");

    const res = await api(`/templates/${templateId}/items`, { method: "GET" });
    if (!res.ok) {
        alert(`GET /templates/${templateId}/items 실패\n${res.status}\n${await safeText(res)}`);
        return;
    }

    const items = await res.json();
    renderTemplateItems(items);
}

function renderTemplateItems(items) {
    templateItemsListEl.innerHTML = "";
    const list = Array.isArray(items) ? items : [];

    if (!list.length) {
        templateItemsListEl.innerHTML = `<div class="list-item">(아이템 없음)</div>`;
        return;
    }

    list.forEach(it => {
        const div = document.createElement("div");
        div.className = "list-item";
        div.textContent = `#${it.id ?? "-"} | ${it.title ?? "-"} | rule=${it.ruleType ?? "-"} | dow=${it.templateDayOfWeek ?? it.dayOfWeek ?? "-"}`;
        templateItemsListEl.appendChild(div);
    });
}

async function onGenerateFromTemplate() {
    const templateId = templateSelectEl.value;
    if (!templateId) {
        alert("템플릿을 선택해줘");
        return;
    }

    const date = templateStartEl.value;
    if (!date) {
        alert("기준 날짜를 선택해줘");
        return;
    }

    const res = await api(
        `/templates/${templateId}/generate?date=${encodeURIComponent(date)}`,
        { method: "POST" }
    );

    if (!res.ok) {
        alert(
            `generate 실패\n` +
            `status=${res.status}\n` +
            `${await safeText(res)}`
        );
        return;
    }

    // 주간 이동 & 갱신
    currentMonday = toMonday(new Date(date));
    templateStartEl.value = fmtDate(currentMonday);
    refresh();

    alert("주간 생성 완료");
}


// ===== UI 동기화 =====
function makeDefaultWeekName(mondayDate) {
    return `Week of ${fmtDate(mondayDate)}`;
}

function syncNewTemplateDayOfWeekUI() {
    if (!newTemplateRuleTypeEl || !newTemplateDayOfWeekEl) return;
    const ruleType = (newTemplateRuleTypeEl.value || "").toUpperCase();
    const enabled = ruleType === "WEEKLY";
    newTemplateDayOfWeekEl.disabled = !enabled;
    if (!enabled) {
        newTemplateDayOfWeekEl.value = "";
    } else {
        if (!newTemplateDayOfWeekEl.value) newTemplateDayOfWeekEl.value = "MONDAY";
    }
}

function syncItemDayOfWeekUI() {
    if (!itemRuleTypeEl || !itemDayOfWeekEl) return;
    const ruleType = (itemRuleTypeEl.value || "").toUpperCase();
    const enabled = ruleType === "WEEKLY";
    itemDayOfWeekEl.disabled = !enabled;
    if (!enabled) itemDayOfWeekEl.value = "";
}

function openModal(open) {
    modalBackdrop.classList.toggle("hidden", !open);
    if (open) {
        itemTemplateSelectEl.value = templateSelectEl.value;
        itemsViewTemplateSelectEl.value = templateSelectEl.value;
        syncNewTemplateDayOfWeekUI();
        syncItemDayOfWeekUI();
    }
}

async function api(path, options = {}) {
    const method = (options.method || "GET").toUpperCase();
    const headers = new Headers(options.headers || {});

    if (["POST", "PATCH", "PUT", "DELETE"].includes(method)) {
        if (!headers.has("Idempotency-Key")) {
            const uuid = ([1e7]+-1e3+-4e3+-8e3+-1e11).replace(/[018]/g, c =>
                (c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> c / 4).toString(16)
            );        }
    }

    return fetch(path, {
        credentials: "same-origin",
        ...options,
        headers,
    });
}

async function safeText(res) {
    try {
        return await res.text();
    } catch {
        return "";
    }
}

// 날짜 유틸
function fmtDate(d) {
    const dt = (d instanceof Date) ? d : new Date(d);
    const y = dt.getFullYear();
    const m = String(dt.getMonth() + 1).padStart(2, "0");
    const day = String(dt.getDate()).padStart(2, "0");
    return `${y}-${m}-${day}`;
}

function addDays(d, n) {
    const dt = new Date(d);
    dt.setDate(dt.getDate() + n);
    return dt;
}

function toMonday(d) {
    const dt = new Date(d);
    const day = dt.getDay(); // 0=일 1=월 ...
    const diff = (day === 0) ? -6 : (1 - day);
    dt.setDate(dt.getDate() + diff);
    dt.setHours(0, 0, 0, 0);
    return dt;
}

function weekRange(monday) {
    const m = new Date(monday);
    const s = addDays(m, 6);
    return { monday: m, sunday: s };
}

function dayKOR(d) {
    const names = ["일", "월", "화", "수", "목", "금", "토"];
    return `${names[d.getDay()]}요일`;
}

function escapeHtml(str) {
    return String(str)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}
