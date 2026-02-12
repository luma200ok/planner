const API_BASE = "http://localhost:8081/api/v1/planner";
const $ = (s) => document.querySelector(s);

// 날짜 유틸리티
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

let currentStartDay = toSunday(new Date());

// API 공통 함수
async function api(path, options = {}) {
    const res = await fetch(`${API_BASE}${path}`, {
        ...options,
        headers: {"Content-Type": "application/json", ...options.headers}
    });
    if (!res.ok) throw new Error("API 요청 실패");
    return res;
}

// 보드 렌더링
function renderBoard(tasks) {
    const board = $("#board");
    if (!board) return;
    board.innerHTML = "";

    for (let i = 0; i < 7; i++) {
        const date = addDays(currentStartDay, i);
        const dateStr = fmtDate(date);
        const dayTasks = tasks.filter(t => t.date === dateStr);

        const col = document.createElement("div");
        col.className = "column"; // 🚩 클래스 부여 확인

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
            const isDone = t.status === 'DONE';
            const isSkipped = t.status === 'SKIPPED';

            item.className = `task-item ${t.status.toLowerCase()}`;

            const textStyle = isSkipped
                ? 'text-decoration: line-through; color: var(--muted); opacity: 0.6;'
                : isDone ? 'text-decoration: line-through; color: var(--muted);' : '';

            // 🚩 HTML 구조를 더 명확하게 정돈 (태그 닫힘 주의)
            item.innerHTML = `
            <div class="task-content" id="task-text-${t.id}" 
                 onclick='enableInlineEdit(${t.id}, ${JSON.stringify(t.title)})' 
                 style="cursor:pointer; flex:1; min-width: 0; ${textStyle}">
                <span class="task-title" style="display:block; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;">
                    ${t.title}
                </span>
            </div>
            <div class="task-btns" style="display:flex; gap:5px; flex-shrink:0;">
                <button class="btn-check ${isDone ? 'active' : ''} ${isSkipped ? 'skipped' : ''}" 
                        onclick="event.stopPropagation(); ${isSkipped ? '' : `completeTask(${t.id})`}"
                        style="background:none; border:none; cursor:pointer; font-size:18px;">
                    ${isSkipped ? '❎' : (isDone ? '✅' : '⬜')} 
                </button>
                <button class="btn-delete" onclick="event.stopPropagation(); deleteTask(${t.id})" 
                        style="color:var(--bad); background:none; border:none; cursor:pointer; font-size:16px;">✕</button>
            </div>
        `;
            listEl.appendChild(item);
        });
    }
    updateHeader();
}

async function refresh() {
    const from = fmtDate(currentStartDay);
    const to = fmtDate(addDays(currentStartDay, 6));
    try {
        const res = await api(`/tasks?from=${from}&to=${to}`);
        const tasks = await res.json();
        renderBoard(tasks);
    } catch (e) {
        console.error("데이터 로드 실패", e);
    }
}

function updateHeader() {
    const endDay = addDays(currentStartDay, 6);
    $("#weekRange").textContent = `${fmtDate(currentStartDay)} ~ ${fmtDate(endDay)}`;
}

// 할 일/템플릿 추가 핸들러
// app.js 의 handleCreateTask 함수 내부
async function handleCreateTask() {
    const title = $("#quickTitle").value;
    const rule = $("#quickRule").value; // 🚩 HTML에 id="quickRule"이 있어야 함
    const date = $("#quickDate").value;

    if (!title) return alert("제목을 입력하세요.");

    try {
        if (rule === "NONE") {
            // 날짜가 없으면 오늘 날짜로 기본값 설정
            await api("/tasks", {
                method: "POST",
                body: JSON.stringify({title, date: date || fmtDate(new Date())})
            });
        } else {
            // 🚩 템플릿 등록 (반복)
            await api("/templates", {
                method: "POST",
                body: JSON.stringify({
                    title: title,
                    ruleType: rule, // DAILY, WEEKDAYS 등
                    dayOfWeek: null,
                    date: date
                })
            });
            alert("반복 템플릿이 등록되었습니다!");
        }

        $("#quickTitle").value = "";
        refresh();
    } catch (e) {
        console.error(e);
        alert("등록 실패: 서버 연결을 확인하세요.");
    }
}

// 상태 토글
async function completeTask(id) {
    await api(`/tasks/${id}/complete`, {method: "POST"});
    refresh();
}

// 삭제
async function deleteTask(id) {
    if (!confirm("삭제하시겠습니까?")) return;
    await api(`/tasks/${id}`, {method: "DELETE"});
    refresh();
}

// 인라인 수정 활성화 (커서 해결 버전)
window.enableInlineEdit = (id, oldTitle) => {
    const container = document.getElementById(`task-text-${id}`);
    if (!container || container.querySelector('input')) return;

    const input = document.createElement('input');
    input.type = 'text';
    input.value = oldTitle;
    input.className = 'inline-edit-input';

    // 🚩 기존 텍스트 임시 저장 (취소 시 사용)
    const originalContent = container.innerHTML;

    container.innerHTML = '';
    container.appendChild(input);

    setTimeout(() => {
        input.focus();
        const length = input.value.length;
        input.setSelectionRange(length, length);
    }, 10);

    let isSaving = false; // 🚩 중복 실행 방지 플래그

    const save = async () => {
        if (isSaving) return;
        const newTitle = input.value.trim();

        if (newTitle && newTitle !== oldTitle) {
            isSaving = true;
            try {
                await api(`/tasks/${id}`, {
                    method: "PUT",
                    body: JSON.stringify({title: newTitle})
                });
                refresh();
            } catch (e) {
                console.error("수정 실패", e);
                container.innerHTML = originalContent; // 실패 시 원복
            }
        } else {
            container.innerHTML = originalContent; // 변경 없으면 원복
        }
    };

    input.onkeydown = (e) => {
        if (e.key === 'Enter') {
            input.onblur = null; // blur 중복 방지
            save();
        }
        if (e.key === 'Escape') {
            input.onblur = null;
            container.innerHTML = originalContent; // ESC 시 즉시 원복
        }
    };

    input.onblur = save;
    input.onclick = (e) => e.stopPropagation();
};

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
                body: JSON.stringify({date: fmtDate(new Date()), carryOver: false})
            });
            refresh();
        };
    }
    refresh();
});