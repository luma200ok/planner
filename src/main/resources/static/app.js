// 1. 환경 설정 및 전역 변수 (순서 중요! 맨 위에 있어야 에러 안 남)
const isLocal = window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1";
const API_BASE = isLocal
    ? "http://localhost:8082/api/v1/planner"
    : "http://rkqkdrnportfolio.shop:8082/api/v1/planner";

const $ = (s) => document.querySelector(s);

// 날짜 유틸리티
function toSunday(d) {
    const dt = new Date(d);
    const day = dt.getDay();
    dt.setDate(dt.getDate() - day);
    dt.setHours(0, 0, 0, 0);
    return dt;
}

function addDays(d, n) {
    const r = new Date(d);
    r.setDate(r.getDate() + n);
    return r;
}

function fmtDate(d) {
    const date = new Date(d);
    const offset = date.getTimezoneOffset() * 60000;
    return new Date(date.getTime() - offset).toISOString().split('T')[0];
}

// ⭐ 전역 변수 선언 (여기 있어야 'not defined' 에러가 안 납니다)
let currentStartDay = toSunday(new Date());

// 2. 화면 렌더링 함수 (카드 디자인 적용됨)
function renderBoard(tasks) {
    const board = document.getElementById("board");
    if (!board) return;
    board.innerHTML = "";

    const dayNames = ["일", "월", "화", "수", "목", "금", "토"];

    for (let i = 0; i < 7; i++) {
        const date = addDays(currentStartDay, i);
        const dateStr = fmtDate(date);

        // 날짜 필터링
        const dayTasks = tasks.filter(t => (t.date || t.scheduledDate) === dateStr);

        // 컬럼 생성
        const col = document.createElement("div");
        col.className = "column";
        col.innerHTML = `
            <div class="column-header">
                <span class="day-name">${dayNames[date.getDay()]}</span>
                <span class="day-date">${date.getMonth() + 1}.${date.getDate()}</span>
            </div>
            <div class="task-list"></div>
        `;
        board.appendChild(col);
        const listEl = col.querySelector(".task-list");

        // ✨ 카드 생성 로직
        dayTasks.forEach(t => {
            const item = document.createElement("div");
            item.className = "task-card"; // CSS 클래스 적용

            const isDone = t.status === 'DONE';
            const isSkipped = t.status === 'SKIPPED';

            // 뱃지 설정
            let badgeHtml = '<span class="badge planned">PLANNED</span>';
            if (isDone) badgeHtml = '<span class="badge done">DONE</span>';
            if (isSkipped) badgeHtml = '<span class="badge skipped">SKIPPED</span>';

            // 버튼 설정 (텍스트 알약 버튼)
            const stopProp = 'onclick="event.stopPropagation();';
            let buttonsHtml = '';

            if (isDone || isSkipped) {
                // 완료/스킵 상태 -> 되돌리기(undo), 삭제(delete)
                buttonsHtml = `
                    <button class="btn-pill" ${stopProp} undoTask(${t.id})">undo</button>
                    <button class="btn-pill" ${stopProp} deleteTask(${t.id})">delete</button>
                `;
            } else {
                // 예정 상태 -> 완료(complete), 스킵(skip), 삭제(delete)
                buttonsHtml = `
                    <button class="btn-pill" ${stopProp} completeTask(${t.id})">complete</button>
                    <button class="btn-pill" ${stopProp} skipTask(${t.id})">skip</button>
                    <button class="btn-pill" ${stopProp} deleteTask(${t.id})">delete</button>
                `;
            }

            // HTML 조립
            item.innerHTML = `
                <div class="card-header">
                    <span class="card-title ${isDone || isSkipped ? 'done-text' : ''}" 
                          onclick="editTask(${t.id}, '${t.title}')">${t.title}</span>
                    ${badgeHtml}
                </div>
                <div class="card-meta">
                    <span>#${t.id} ${dateStr}</span>
                    ${t.rule ? `<span>Rule: ${t.rule}</span>` : ''}
                </div>
                <div class="card-actions">
                    ${buttonsHtml}
                </div>
            `;
            listEl.appendChild(item);
        });
    }
    updateHeader();
}

function updateHeader() {
    const endDay = addDays(currentStartDay, 6);
    const rangeEl = document.getElementById("dateRange");
    if (rangeEl) rangeEl.innerText = `${fmtDate(currentStartDay)} ~ ${fmtDate(endDay)}`;
}

// 3. API 통신 및 핸들러
async function api(path, options = {}) {
    const url = path.startsWith("http") ? path : API_BASE + path;
    const headers = {"Content-Type": "application/json", ...options.headers};
    const res = await fetch(url, {...options, headers});
    if (!res.ok) throw new Error(`API 호출 실패: ${res.status}`);
    return res;
}

async function refresh() {
    const from = fmtDate(currentStartDay);
    const to = fmtDate(addDays(currentStartDay, 6));
    const status = $("#statusFilter") ? $("#statusFilter").value : "";

    try {
        const url = `/tasks?from=${from}&to=${to}${status ? `&status=${status}` : ''}`;
        const res = await api(url);
        const tasks = await res.json();
        renderBoard(tasks);
    } catch (e) {
        console.error("데이터 로드 실패", e);
    }
}

// 버튼 액션 핸들러들
async function handleCreateTask() {
    const title = $("#quickTitle").value;
    const date = $("#quickDate").value || fmtDate(new Date());
    const rule = $("#quickRule") ? $("#quickRule").value : "NONE";

    if (!title) return alert("내용을 입력하세요");

    try {
        if (rule === "NONE") {
            await api("/tasks", { method: "POST", body: JSON.stringify({ title, date }) });
        } else {
            await api("/templates", { method: "POST", body: JSON.stringify({ title, ruleType: rule, date, dayOfWeek: null }) });
        }
        $("#quickTitle").value = "";
        refresh();
    } catch (e) { alert("생성 실패: " + e.message); }
}

async function completeTask(id) { await api(`/tasks/${id}/complete`, {method: "POST"}); refresh(); }
async function skipTask(id) { await api(`/tasks/${id}/skip`, {method: "POST"}); refresh(); }
async function undoTask(id) { if(confirm("되돌리시겠습니까?")) { await api(`/tasks/${id}/undo`, {method: "POST"}); refresh(); } }
async function deleteTask(id) { if(confirm("삭제하시겠습니까?")) { await api(`/tasks/${id}`, {method: "DELETE"}); refresh(); } }
async function editTask(id, old) {
    const val = prompt("수정:", old);
    if(val && val !== old) { await api(`/tasks/${id}`, {method:"PUT", body:JSON.stringify({title:val})}); refresh(); }
}

// 템플릿 관리
async function createCustomTemplate() {
    const title = $("#newTemplateName").value;
    const checked = Array.from(document.querySelectorAll('#dayCheckboxes input:checked')).map(cb => cb.value);
    if (!title || checked.length === 0) return alert("입력 확인");

    try {
        await Promise.all(checked.map(d => api("/templates", {
            method: "POST", body: JSON.stringify({ title, ruleType: "WEEKLY", dayOfWeek: d, date: fmtDate(new Date()) })
        })));
        alert("생성 완료");
        $("#newTemplateName").value = "";
        document.querySelectorAll('#dayCheckboxes input:checked').forEach(c => c.checked = false);
        refresh();
    } catch (e) { alert("오류 발생"); }
}

async function loadTemplates() {
    try {
        const res = await api("/templates");
        const list = await res.json();
        const listEl = $("#templateList");
        listEl.innerHTML = "";

        // 그룹화
        const groups = list.reduce((acc, t) => {
            if (!acc[t.title]) acc[t.title] = {title: t.title, ids: [], days: []};
            acc[t.title].ids.push(t.id);
            if (t.dayOfWeek) acc[t.title].days.push(t.dayOfWeek);
            return acc;
        }, {});

        Object.values(groups).forEach(g => {
            const item = document.createElement("div");
            item.style = "padding:10px; border-bottom:1px solid #333; display:flex; justify-content:space-between;";
            item.innerHTML = `
                <div>${g.title} <span style="font-size:11px; color:#888">(${g.days.join(',')})</span></div>
                <div>
                    <button onclick="editTemplateGroup('${g.title}', [${g.ids}])">✏️</button>
                    <button onclick="deleteTemplateGroup([${g.ids}])">🗑️</button>
                </div>`;
            listEl.appendChild(item);
        });
    } catch (e) { console.error(e); }
}

async function editTemplateGroup(old, ids) {
    const val = prompt("수정:", old);
    if(!val || val===old) return;
    await Promise.all(ids.map(id => api(`/templates/${id}`, {method:"PUT", body:JSON.stringify({title:val})})));
    loadTemplates();
}
async function deleteTemplateGroup(ids) {
    if(!confirm("삭제하시겠습니까?")) return;
    await Promise.all(ids.map(id => api(`/templates/${id}`, {method:"DELETE"})));
    loadTemplates();
}

// 4. 초기화 (이벤트 연결)
document.addEventListener("DOMContentLoaded", () => {
    if ($("#quickDate")) $("#quickDate").value = fmtDate(new Date());
    $("#createTask").onclick = handleCreateTask;
    $("#refresh").onclick = refresh;

    // ✨ 추가된 부분: 필터 버튼과 실행 함수(refresh) 연결
    const applyFilterBtn = document.getElementById("applyFilter");
    if (applyFilterBtn) {
        applyFilterBtn.onclick = refresh;
    }
    $("#prevWeek").onclick = () => { currentStartDay = addDays(currentStartDay, -7); refresh(); };
    $("#nextWeek").onclick = () => { currentStartDay = addDays(currentStartDay, 7); refresh(); };

    // 모달
    const modal = $("#modalBackdrop");
    if($("#openTemplateModal")) $("#openTemplateModal").onclick = () => { modal.classList.remove("hidden"); loadTemplates(); };
    if($("#closeModal")) $("#closeModal").onclick = () => { modal.classList.add("hidden"); };
    if($("#btnCreateTemplate")) $("#btnCreateTemplate").onclick = createCustomTemplate;

    // 운영툴
    if($("#dayCloseBtn")) $("#dayCloseBtn").onclick = async () => { if(confirm("마감하시겠습니까?")) { await api("/day-close", {method:"POST", body:JSON.stringify({date:fmtDate(new Date()), carryOver:false})}); refresh(); }};
    if($("#runSchedulerBtn")) $("#runSchedulerBtn").onclick = async () => { if(confirm("강제 생성?")) { await api("/admin/run-scheduler", {method:"POST"}); refresh(); }};

    refresh();
});

// ✨ 추가된 부분: 메뉴 토글 기능
document.getElementById('menu-toggle').addEventListener('click', () => {
    document.querySelector('.panel').classList.toggle('open');
});

// 외부 클릭 시 닫기 기능 (편의성)
document.addEventListener('click', (e) => {
    const panel = document.querySelector('.panel');
    const menuBtn = document.getElementById('menu-toggle');
    if (panel.classList.contains('open') && !panel.contains(e.target) && e.target !== menuBtn) {
        panel.classList.remove('open');
    }
});