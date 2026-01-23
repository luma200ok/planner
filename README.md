# Planner (Study Planner Backend)

할 일(Task) + 반복 템플릿(Template) 기반으로 일정을 관리하고,
Day Close(자정 마감)로 미완료 처리/이월, 그리고 통계를 제공하는 백엔드 API 서버입니다.

- UI 없이 **Swagger(OpenAPI)** 로만 테스트/검증

---

## Tech Stack
- Java(21) / Spring Boot(3.5.x)
- Spring Web, Spring Data JPA
- Validation (jakarta.validation)
- MySQL
- Springdoc OpenAPI (Swagger UI)
- Gradle

---

## Swagger
- Swagger UI: `/swagger-ui/index.html`

(프로젝트 설정에 따라 경로가 다르면 Swagger UI에서 노출되는 주소 기준으로 맞추면 됩니다.)

---

## Local Setup

### 1) DB 준비 (MySQL)
- DB 생성 후 계정/비밀번호/URL을 `application-local.yml`(또는 환경변수)에 설정

예시)
- `spring.datasource.url=jdbc:mysql://localhost:3306/planner`
- `spring.datasource.username=...`
- `spring.datasource.password=...`

### 2) 실행
```bash
./gradlew bootRun
또는
PlannerApplication 실행
```

## Core Features
1) Task CRUD + 상태 관리
- PLANNED / DONE / SKIPPED
- DONE/SKIPPED → undo로 PLANNED 복귀 지원
- 이벤트 조회(`/tasks/{id}/events`)로 변경 이력 확인

2) 멱등성(Idempotency) 처리
- 완료 처리 시 `Idempotency-Key` 헤더 기반으로 중복 요청을 안전하게 처리

3) Template 기반 생성
- 템플릿을 만들고 특정 날짜에 작업 생성(`/templates/{id}/generate`)

4) Day Close(마감 처리)
- 특정 날짜를 마감하면서 미완료 처리(자동 SKIP 등) + 필요 시 이월(carry over)

5) Reports (통계)
- 일별/요약/템플릿별 집계

---

## API Endpoints
<details>
<summary>🔽 Api Endpoints</summary>

## Tasks
- `POST /tasks` : 할 일 생성
- `GET /tasks?from=&to=&status=` : 기간/상태 검색
- `GET /tasks/{id}` : 단건 조회
- `PATCH /tasks/{id}` : 수정
- `DELETE /tasks/{id}` : 삭제
- `POST /tasks/{id}/complete` : 완료 처리
    - Header: `Idempotency-Key: <string>`
- `POST /tasks/{id}/undo` : 완료/스킵 취소(PLANNED로 복귀)
    - Body(optional): `{ "reason": "..." }`
- `POST /tasks/{id}/skip` : 스킵 처리
    - Body(optional): `{ "reason": "..." }`
- `GET /tasks/{id}/events` : 이벤트(이력) 조회

### Templates
- `POST /templates` : 템플릿 생성
- `POST /templates/{id}/generate?date=YYYY-MM-DD` : 해당 날짜에 템플릿 기반 Task 생성

### Day Close
- `POST /day-close?date=YYYY-MM-DD&carryOver=true&carryTo=YYYY-MM-DD`
    - `carryOver` 기본값: `true`
    - `carryTo`는 선택(직접 이월할 날짜 지정)

### Reports
- `GET /reports/daily?from=YYYY-MM-DD&to=YYYY-MM-DD`
- `GET /reports/summary?from=YYYY-MM-DD&to=YYYY-MM-DD`
- `GET /reports/templates?from=YYYY-MM-DD&to=YYYY-MM-DD`

</details>
---
