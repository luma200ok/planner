const API_BASE = "http://rkqkdrnportfolio.shop:8081/api/v1/planner";
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

// 매주 반복(WEEKLY)일 때만 요일 선택창 보여주기
function toggleDaySelect() {
    const rule = $("#newTemplateRuleType").value;
    const daySelect = $("#newTemplateDay");
    // WEEKLY면 보이고(block), 아니면 숨김(none)
    daySelect.style.display = (rule === "WEEKLY") ? "block" : "none";
}

// API 호출 함수
async function api(path, options = {}) {
    const url = path.startsWith("http") ? path : API_BASE + path;
    const headers = {"Content-Type": "application/json", ...options.headers};
    const res = await fetch(url, {...options, headers});
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
                // ➖ 스킵됨
                btnHtml = `
                    <button class="btn-skip active" onclick="event.stopPropagation(); undoTask(${t.id})" 
                            title="되돌리기" style="${btnStyle}">
                       ➖
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
                        ☑️
                    </button>
                    <button class="btn-skip" onclick="event.stopPropagation(); skipTask(${t.id})" 
                            title="건너뛰기" style="${btnStyle}">
                        ➡️
                    </button>
                    <button class="btn-delete" onclick="event.stopPropagation(); deleteTask(${t.id})" 
                            title="삭제" style="${btnStyle}">
                        🗑️
                    </button>
                `;
            }

            item.innerHTML = `
                <div class="task-content">
                    <span class="task-title" style="${titleStyle}; cursor: pointer;" 
                        onclick="editTask(${t.id}, '${t.title}')">
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
    const title = $("#quickTitle").value;
    const date = $("#quickDate").value || fmtDate(new Date());
    const rule = $("#quickRule") ? $("#quickRule").value : "NONE";

    if (!title) return alert("할 일을 입력하세요");

    try {
        if (rule === "NONE") {
            // 🚩 1. 반복 없음: 기존처럼 단일 할 일 생성 API 호출
            await api("/tasks", {
                method: "POST",
                body: JSON.stringify({
                    title: title,
                    date: date
                })
            });
        } else {
            // 🚩 2. 반복 있음(DAILY, WEEKDAYS, WEEKENDS): 템플릿 생성 API 호출!
            // 이렇게 해야 백엔드 로직을 타고 이번 주 해당 요일들에 쫙 깔립니다.
            await api("/templates", {
                method: "POST",
                body: JSON.stringify({
                    title: title,
                    ruleType: rule,
                    date: date,
                    dayOfWeek: null // 단일 요일이 아니므로 null 전송
                })
            });
        }

        // 성공 시 UI 초기화
        $("#quickTitle").value = "";
        refresh(); // 보드 새로고침
    } catch (e) {
        alert("생성 실패: " + e.message);
    }
}

// 기능: 완료, 스킵, 되돌리기, 삭제
async function completeTask(id) {
    await api(`/tasks/${id}/complete`, {method: "POST"});
    refresh();
}

async function skipTask(id) {
    await api(`/tasks/${id}/skip`, {method: "POST"});
    refresh();
}

async function undoTask(id) {
    if (!confirm("상태를 초기화 하시겠습니까?")) return;
    await api(`/tasks/${id}/undo`, {method: "POST"});
    refresh();
}

async function deleteTask(id) {
    if (!confirm("삭제하시겠습니까?")) return;
    await api(`/tasks/${id}`, {method: "DELETE"});
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
                body: JSON.stringify({date: fmtDate(new Date()), carryOver: false})
            });
            refresh();
        };
    }

    if ($("#runSchedulerBtn")) {
        $("#runSchedulerBtn").onclick = async () => {
            if (!confirm("지금 바로 다음 주 일정을 생성하시겠습니까? (중복 체크 포함)")) return;
            try {
                await api("/admin/run-scheduler", { method: "POST" });
                alert("스케줄러가 성공적으로 실행되었습니다.");
                refresh();
            } catch (e) {
                alert("실행 실패: " + e.message);
            }
        };
    }

    if ($("#btnCreateTemplate")) {
        $("#btnCreateTemplate").onclick = createCustomTemplate;
    }

    if ($("#applyFilter")) {
        $("#applyFilter").onclick = refresh;
    }

    // 🚩 [여기서부터 추가] 모달 열기/닫기 이벤트 연결
    const modal = $("#modalBackdrop");

    // 1. 템플릿 관리 열기 버튼 클릭 시
    if ($("#openTemplateModal")) {
        $("#openTemplateModal").onclick = () => {
            modal.classList.remove("hidden");
            loadTemplates(); // 🚩 여기에 이 한 줄을 추가!
        };
    }

    // 2. 모달 내 닫기 버튼 클릭 시
    if ($("#closeModal")) {
        $("#closeModal").onclick = () => {
            modal.classList.add("hidden");
        };
    }

    // 3. 모달 바깥 배경 클릭 시 닫기
    if (modal) {
        modal.onclick = (e) => {
            if (e.target === modal) {
                modal.classList.add("hidden");
            }
        };
    }
    refresh(); // 시작 시 데이터 로드
});

// 🚩 템플릿 생성 API 호출 (다중 요일 지원)
async function createCustomTemplate() {
    const title = $("#newTemplateName").value;

    // 1. 체크된 체크박스들의 value(요일)를 배열로 모음 ['MONDAY', 'WEDNESDAY', ...]
    const checkedDays = Array.from(document.querySelectorAll('#dayCheckboxes input:checked')).map(cb => cb.value);

    // 2. 방어 로직
    if (!title) return alert("템플릿 이름을 입력하세요");
    if (checkedDays.length === 0) return alert("최소 하나의 요일을 선택하세요");

    try {
        // 3. 선택된 요일 개수만큼 백엔드로 POST 요청을 만듦
        // (백엔드 수정 없이, 기존 1요일 1템플릿 구조를 활용하는 프론트엔드 트릭)
        const promises = checkedDays.map(day => {
            return api("/templates", {
                method: "POST",
                body: JSON.stringify({
                    title: title,
                    ruleType: "WEEKLY", // 백엔드에는 무조건 주간 반복으로 전달
                    dayOfWeek: day,     // 각기 다른 요일 전달
                    date: fmtDate(new Date())
                })
            });
        });

        // 4. 병렬로 모든 요청을 한 번에 전송하고 기다림
        await Promise.all(promises);

        alert(`${checkedDays.length}개의 요일에 템플릿이 성공적으로 등록되었습니다!`);

        // 5. 성공 후 UI 초기화
        $("#newTemplateName").value = "";
        document.querySelectorAll('#dayCheckboxes input:checked').forEach(cb => cb.checked = false);
        refresh(); // 보드 새로고침

    } catch (e) {
        console.error(e);
        alert("템플릿 생성 중 오류가 발생했습니다.");
    }
}

async function editTask(id, oldTitle) {
    const newTitle = prompt("할 일 내용을 수정하시겠습니까?", oldTitle);

    // 취소를 누르거나 빈값이면 무시
    if (newTitle === null || newTitle.trim() === "" || newTitle === oldTitle) return;

    try {
        await api(`/tasks/${id}`, {
            method: "PUT",
            body: JSON.stringify({ title: newTitle.trim() })
        });
        refresh(); // 보드 새로고침
    } catch (e) {
        alert("수정 실패: " + e.message);
    }
}

async function loadTemplates() {
    try {
        const res = await api("/templates");
        const templates = await res.json();
        const listEl = $("#templateList");
        listEl.innerHTML = "";

        if (templates.length === 0) {
            listEl.innerHTML = `<div class="empty-msg">등록된 템플릿이 없습니다.</div>`;
            return;
        }

        // 1. 이름(title) 기준으로 그룹화
        const groups = templates.reduce((acc, t) => {
            if (!acc[t.title]) acc[t.title] = {title: t.title, ids: [], days: [], ruleType: t.ruleType};
            acc[t.title].ids.push(t.id);
            if (t.dayOfWeek) acc[t.title].days.push(t.dayOfWeek);
            return acc;
        }, {});

        // 2. 그룹별로 화면에 그리기
        Object.values(groups).forEach(g => {
            const item = document.createElement("div");
            item.className = "list-item";
            item.style = "display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; padding: 12px; border: 1px solid var(--line); border-radius: 8px;";

            const dayMap = {
                'MONDAY': '월',
                'TUESDAY': '화',
                'WEDNESDAY': '수',
                'THURSDAY': '목',
                'FRIDAY': '금',
                'SATURDAY': '토',
                'SUNDAY': '일'
            };
            // 요일 정렬 및 한글화
            const sortedDays = g.days.sort((a, b) => {
                const order = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];
                return order.indexOf(a) - order.indexOf(b);
            }).map(d => dayMap[d]).join(', ');

            const dayInfo = sortedDays ? `(${sortedDays})` : "";

            item.innerHTML = `
                <div>
                    <strong>${g.title}</strong> 
                    <span style="font-size: 11px; color: var(--muted);">| ${g.ruleType} ${dayInfo}</span>
                </div>
                <div style="display: flex; gap: 8px;">
                    <button onclick="editTemplateGroup('${g.title}', [${g.ids}])" style="border:none; background:none; cursor:pointer; font-size: 16px;">✏️</button>
                    <button onclick="deleteTemplateGroup([${g.ids}])" style="border:none; background:none; cursor:pointer; font-size: 16px;">🗑️</button>
                </div>
            `;
            listEl.appendChild(item);
        });
    } catch (e) {
        console.error("템플릿 로드 실패", e);
    }
}

// 🚩 그룹 삭제 (여러 ID를 동시에 삭제)
async function deleteTemplateGroup(ids) {
    if (!confirm("이 템플릿 그룹을 모두 삭제하시겠습니까?")) return;
    try {
        // 모든 ID에 대해 병렬로 삭제 요청
        await Promise.all(ids.map(id => api(`/templates/${id}`, {method: "DELETE"})));
        loadTemplates();
    } catch (e) {
        alert("삭제 실패");
    }
}

// 🚩 그룹 수정 (이름이 같은 모든 템플릿의 제목 변경)
async function editTemplateGroup(oldTitle, ids) {
    const newTitle = prompt("수정할 템플릿 이름을 입력하세요", oldTitle);
    if (!newTitle || newTitle.trim() === "" || newTitle === oldTitle) return;

    try {
        // 같은 그룹의 모든 템플릿 제목을 한꺼번에 수정
        await Promise.all(ids.map(id =>
            api(`/templates/${id}`, {
                method: "PUT",
                body: JSON.stringify({title: newTitle.trim()})
            })
        ));
        alert("성공적으로 수정되었습니다.");
        loadTemplates();
    } catch (e) {
        alert("수정 실패");
    }
}
