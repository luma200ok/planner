const API_BASE = "http://localhost:8081/api/v1/planner";
const $ = (s) => document.querySelector(s);

// 1. 날짜 유틸리티
function fmtDate(d) {
    const date = new Date(d);
    const offset = date.getTimezoneOffset() * 60000;
    return new Date(date.getTime() - offset).toISOString().split('T')[0];
}

function addDays(d, n) {
    const r = new Date(d);
    r.setDate(r.getDate() + n);
    return r;
}

function toSunday(d) {
    const dt = new Date(d);
    const day = dt.getDay();
    dt.setDate(dt.getDate() - day);
    dt.setHours(0, 0, 0, 0);
    return dt;
}

function updateHeader() {
    const endDay = addDays(currentStartDay, 6);
    const rangeText = `${fmtDate(currentStartDay)} ~ ${fmtDate(endDay)}`;

    const rangeEl = document.getElementById("dateRange");
    if (rangeEl) {
        rangeEl.innerText = rangeText;
    } else {
        console.warn("HTML에 id='dateRange'인 태그가 없습니다. 날짜를 표시할 곳을 만들어주세요!");
    }
}

// API 호출 함수
async function api(path, options = {}) {
    const url = path.startsWith("http") ? path : API_BASE + path;
    const headers = { "Content-Type": "application/json", ...options.headers };
    const res = await fetch(url, { ...options, headers });
    if (!res.ok) {
        throw new Error(`API 호출 실패: ${res.status}`);
    }
    return res;
}

let currentStartDay = toSunday(new Date());

// 메인: 보드 렌더링 함수 (핵심 UI 로직)
function renderBoard(tasks) {
    const board = document.getElementById("board");
    if (!board) return;
    board.innerHTML = "";

    for (let i = 0; i < 7; i++) {
        const date = addDays(currentStartDay, i);
        const dateStr = fmtDate(date);

        // 날짜 필드명 안전하게 체크
        const dayTasks = tasks.filter(t => (t.date || t.scheduledDate) === dateStr);

        const col = document.createElement("div");
        col.className = "column";

        const dayNames = ["일", "월", "화", "수", "목", "금", "토"];
        col.innerHTML = `
            <div class="column-header">
                <span class="day-name">${dayNames[date.getDay()]}</span>
                <span class="day-date">${date.getMonth() + 1}.${date.getDate()}</span>
            </div>
            <div class="task-list" id="list-${dateStr}"></div>
        `;
        board.appendChild(col);

        const listEl = col.querySelector(".task-list");

        dayTasks.forEach(t => {
            const item = document.createElement("div");
            item.className = `task-item ${t.status.toLowerCase()}`;

            const isDone = t.status === 'DONE';
            const isSkipped = t.status === 'SKIPPED';
            const isHandled = isDone || isSkipped;
            const titleStyle = isHandled ? 'text-decoration: line-through; color: #aaa;' : '';

            let btnHtml = '';

            // 🚩 [수정 1] 이상한 네모로 보이게 하던 text-shadow 꼼수 제거
            const btnStyle = "background:none; border:none; cursor:pointer; font-size:18px; margin-right: 2px;";

            if (isDone) {
                // ✅ 완료됨
                // 🚩 [수정 2] class="btn-check active" 로 'active'를 꼭 넣어줘야 CSS의 흑백 필터가 풀립니다!
                btnHtml = `
                    <button class="btn-check active" onclick="event.stopPropagation(); undoTask(${t.id})" 
                            title="되돌리기" style="${btnStyle}">
                        ✅
                    </button>
                    <button class="btn-delete" onclick="event.stopPropagation(); deleteTask(${t.id})" 
                            title="삭제" style="${btnStyle}">
                        🗑️
                    </button>
                `;
            } else if (isSkipped) {
                // ⏸️ 스킵됨
                btnHtml = `
                    <button class="btn-skip active" onclick="event.stopPropagation(); undoTask(${t.id})" 
                            title="되돌리기" style="${btnStyle}">
                        ⏸️
                    </button>
                    <button class="btn-delete" onclick="event.stopPropagation(); deleteTask(${t.id})" 
                            title="삭제" style="${btnStyle}">
                        🗑️
                    </button>
                `;
            } else {
                // ⬜ 할 일 (여기는 active가 없으므로 CSS에 의해 살짝 투명하고 회색으로 보이는 게 맞습니다)
                btnHtml = `
                    <button class="btn-check" onclick="event.stopPropagation(); completeTask(${t.id})" 
                            title="완료하기" style="${btnStyle}">
                        ⬜
                    </button>
                    <button class="btn-skip" onclick="event.stopPropagation(); skipTask(${t.id})" 
                            title="건너뛰기" style="${btnStyle}">
                        ⏭️
                    </button>
                    <button class="btn-delete" onclick="event.stopPropagation(); deleteTask(${t.id})" 
                            title="삭제" style="${btnStyle}">
                        🗑️
                    </button>
                `;
            }

            item.innerHTML = `
                <div class="task-content">
                    <span class="task-title" style="${titleStyle}">
                        ${t.title}
                    </span>
                </div>
                <div class="task-btns" style="display:flex; gap:5px;">
                    ${btnHtml}
                </div>
            `;
            listEl.appendChild(item);
        });
    }

    // 🚩 여기서 헤더 업데이트 호출!
    updateHeader();
}

async function refresh() {
    const from = fmtDate(currentStartDay);
    const to = fmtDate(addDays(currentStartDay, 6));
    const statusEl = $("#statusFilter");
    const status = statusEl ? statusEl.value : "";

    try {
        const url = `/tasks?from=${from}&to=${to}${status ? `&status=${status}` : ''}`;
        const res = await api(url);
        const tasks = await res.json();
        renderBoard(tasks);
    } catch (e) {
        console.error("데이터 로드 실패", e);
    }
}

async function handleCreateTask() {
    const title = $("#taskTitle").value;
    const date = $("#taskDate").value || fmtDate(new Date());
    if (!title) return alert("할 일을 입력하세요");

    try {
        await api("/tasks", {
            method: "POST",
            body: JSON.stringify({ title, date })
        });
        $("#taskTitle").value = "";
        refresh();
    } catch (e) {
        alert("생성 실패: " + e.message);
    }
}

// 기능: 완료, 스킵, 되돌리기, 삭제
async function completeTask(id) {
    await api(`/tasks/${id}/complete`, { method: "POST" });
    refresh();
}

async function skipTask(id) {
    await api(`/tasks/${id}/skip`, { method: "POST" });
    refresh();
}

async function undoTask(id) {
    if (!confirm("상태를 초기화 하시겠습니까?")) return;
    await api(`/tasks/${id}/undo`, { method: "POST" });
    refresh();
}

async function deleteTask(id) {
    if (!confirm("삭제하시겠습니까?")) return;
    await api(`/tasks/${id}`, { method: "DELETE" });
    refresh();
}

// 5. 초기화 및 이벤트 연결
document.addEventListener("DOMContentLoaded", () => {
    if ($("#quickDate")) $("#quickDate").value = fmtDate(new Date());
    if ($("#createTask")) $("#createTask").onclick = handleCreateTask;
    if ($("#refresh")) $("#refresh").onclick = refresh;

    if ($("#prevWeek")) $("#prevWeek").onclick = () => {
        currentStartDay = addDays(currentStartDay, -7);
        refresh();
    };
    if ($("#nextWeek")) $("#nextWeek").onclick = () => {
        currentStartDay = addDays(currentStartDay, 7);
        refresh();
    };

    if ($("#dayCloseBtn")) {
        $("#dayCloseBtn").onclick = async () => {
            if (!confirm("마감하시겠습니까? (미완료 항목은 스킵 처리됩니다)")) return;
            await api("/day-close", {
                method: "POST",
                body: JSON.stringify({ date: fmtDate(new Date()), carryOver: false })
            });
            refresh();
        };
    }

    if ($("#applyFilter")) {
        $("#applyFilter").onclick = refresh;
    }

    refresh(); // 시작 시 데이터 로드
});