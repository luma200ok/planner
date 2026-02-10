# 🗓️ Planner Project

> **Spring Boot 기반의 스마트 일정 관리 백엔드 서버**

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![SpringBoot](https://img.shields.io/badge/springboot-%236DB33F.svg?style=for-the-badge&logo=springboot&logoColor=white)
![MySQL](https://img.shields.io/badge/mysql-%2300f.svg?style=for-the-badge&logo=mysql&logoColor=white)
![AWS](https://img.shields.io/badge/AWS-%23FF9900.svg?style=for-the-badge&logo=amazon-aws&logoColor=white)
[![CI](https://github.com/luma200ok/planner/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/luma200ok/planner/actions/workflows/ci.yml)

---
## 🔗 Live Demo & API Docs
운영 중인 서버의 실시간 API 명세서를 바로 확인하실 수 있습니다.

* **Swagger UI**: [http://rkqkdrnportfolio.shop:8081/swagger-ui/index.html](http://rkqkdrnportfolio.shop:8081/swagger-ui/index.html)
* **Production Base URL**: `http://rkqkdrnportfolio.shop:8081`
---

## 📌 핵심 요약 (Key Highlights)

1. **복합 일정 관리**: 단순 CRUD를 넘어 템플릿 기반의 반복 생성 및 자정 마감(Day Close) 로직 구현
2. **안정성 확보**: `Idempotency-Key`를 통한 중복 요청 방지(멱등성) 처리
3. **배포 자동화**: AWS EC2 환경에서 쉘 스크립트를 활용한 무중단 재시작 프로세스 구축
4. **문제 해결**: 정적 리소스 핸들링 및 전역 예외 처리 범위 최적화 경험

---

### 🛠 Tech Stack

* **Framework**: Spring Boot 3.x
* **Database**: **"MySQL 8.0 (Local/Prod)"**
* **Security**: External Environment Variables (`.env`)
* **Documentation**: Swagger UI (SpringDoc)

---

## 🏗 Infrastructure & Deployment (운영 및 배포)

이 프로젝트는 실제 운영 환경에서의 관리 편의성과 보안을 최우선으로 고려했습니다.

### 1. 자동 실행 스크립트 (`run.sh`)

수동으로 명령어를 입력하는 번거로움을 줄이고 운영 실수를 방지하기 위해 제작되었습니다.

- **기존 프로세스 자동 종료**: 포트(`8081`) 점유 확인 후 기존 PID를 추적하여 안전하게 종료
- **환경 주입**: 외부 환경 변수(`.env`)를 로드하여 보안이 강화된 형태로 서버 실행
- **부팅 모니터링**: 서버 실행 후 PID를 즉시 확인하여 성공 여부 피드백 제공

```bash
# 어느 경로에서든 한 줄로 서버 재시작 가능
/home/ec2-user/planner/run.sh
```

### 2. 보안 환경 변수 관리
- `.env` 활용: DB 패스워드 등 민감한 정보를 소스코드와 분리하여 서버 내부 환경 변수로 관리
- Profile 분리: 로컬(`local`)은 MySQL를, 운영(`prod`)은 MySQL을 사용하도록 자동 전환 설정

---

## 🌟 Core Business Features
### 1) Task CRUD + 상태 관리
- * 상태 변경 이력(`Event`) 추적 기능 및 완료 취소(`undo`) 지원
- * PLANNED / DONE / SKIPPED 상태를 통한 명확한 일정 관리

### 2) Template 기반 반복 생성
- * 반복 규칙을 가진 Template에 여러 Item을 등록하여 특정 날짜에 할 일 일괄 생성
- * 동일 날짜/동일 아이템의 중복 생성 방지 가드 로직 포함

### 3) 멱등성(Idempotency) 처리
* 완료 처리(`POST /tasks/{id}/complete`) 시 네트워크 재시도 상황에서도 데이터의 일관성 보장

---

## 🛠 Troubleshooting Case Study

### 🚩 정적 리소스(favicon.ico) 500 오류 해결
- * 문제: 전역 예외 처리기가 정적 리소스 요청까지 가로채 에러 응답을 반환하는 현상
- * 해결: `@ControllerAdvice`의 범위를 `/api/**`로 한정하여 정적 리소스 핸들러의 정상 작동 보장

---


## 📁 상세 정보 (Details)

<details>
<summary>💻 로컬 실행 방법 (Local Setup) 보기</summary>

![consol](docs/images/start_consol.png)

### 1) DB 준비 (MySQL)

- DB 생성 후 아래 설정을 `application-local.yml` 또는 환경변수로 지정

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
</details>

<details>
<summary>⚙️ Configuration & Profiles</summary>

> **Spring Profile을 활용한 환경별 최적화 및 보안 강화**

실행 환경을 `default(Local)`와 `prod(EC2)`로 분리하여 관리하며, 각 환경에 맞는 DB 정책과 보안 전략을 적용합니다.

### 🌓 환경별 설정 비교
| 구분 | 💻 Local (개발) | 🚀 Prod (운영) |
| :--- | :--- | :--- |
| **Active Profile** | `default` (or `local`) | `prod` |
| **Database** | MySQL 8.0 | MySQL 8.0 (EC2) |
| **DDL Policy** | `ddl-auto: create` | **`ddl-auto: validate`** |
| **DataSource** | `application-local.yml` | **`.env` 기반 환경변수 주입** |

---

### 📂 설정 파일 구조
프로젝트의 유지보수성을 위해 설정을 계층적으로 분리했습니다.
* **`application.yml`**: 시스템 공통 설정 (Port, Multipart, 전역 스캔 경로 등)
* **`application-local.yml`**: 로컬 개발을 위한 MySQL 접속 정보 및 디버깅용 로그 설정
* **`application-prod.yml`**: 운영 환경을 위한 인프라 설정 (민감 정보 제외)

---

### 🛡️ 보안 및 환경변수 주입 (Security)
운영 환경(`prod`)에서의 보안을 최우선으로 고려하여 설계했습니다.

1. **민감 정보 격리**: DB Password, API Key 등은 소스 코드(YAML)에 직접 기록하지 않습니다.
2. **Runtime Injection**: 서버 내부에 위치한 `.env` 파일을 실행 시점에 로드하여 환경변수로 주입합니다.
3. **운영 정책**: `ddl-auto: validate` 설정을 통해 운영 DB의 스키마가 의도치 않게 변경되는 사고를 원천 차단합니다.

</details>

<details> 
<summary>📌 핵심 비즈니스 로직 (Core Logic)</summary>
### 1️⃣ 스마트 Task 관리 및 이력 추적
* Lifecycle: Task 생성부터 조회, 수정, 삭제까지 완벽한 CRUD 지원
* State Control: **PLANNED(계획) / DONE(완료) / SKIPPED(건너뜀)**의 명확한 상태 관리
* Undo System: 실수로 완료/스킵한 항목을 undo 명령 한 번으로 즉시 PLANNED 복구
* History Tracking: GET /tasks/{id}/events를 통해 상태가 변한 시점과 사유를 투명하게 기록

### 2️⃣ 데이터 무결성을 위한 멱등성(Idempotency) 보장
* Key-based: Idempotency-Key 헤더를 활용하여 중복 요청 철저히 차단
* Reliability: 네트워크 불안정으로 인한 재시도나 사용자의 더블 클릭 상황에서도 단 1회만 처리되도록 보장하여 데이터 오염 방지

### 3️⃣ 전략적 템플릿 시스템 (Repeatable Tasks)
* Flexible Rules: WEEKDAYS(평일), WEEKENDS(주말) 등 반복 규칙을 담은 Template 정의
* Batch Generation: generate API 호출 한 번으로 템플릿에 등록된 여러 아이템을 특정 날짜의 Task로 일괄 변환

### 4️⃣ 자정 마감(Day Close) 자동화
* Auto-Skip: 마감 시점까지 완료되지 않은 Task를 자동으로 SKIP 처리
* Smart Carry-over: 템플릿 규칙에 따라 다음 루틴의 Task를 자동으로 생성하거나 이월
* One-time Execution: 동일 날짜에 대한 중복 마감을 방지하는 가드 로직으로 운영 안정성 확보

### 5️⃣ 인사이트를 주는 통계 리포트
* 📅 Daily: 오늘 하루의 성과를 한눈에 파악하는 일별 집계
* 📊 Summary: 특정 기간의 성취도를 분석하는 요약 통계
* 📂 Categorized: 템플릿 기반 작업과 단발성(ONE-OFF) 작업의 비중을 분석하여 리포트 제공
</details>

<details>
<summary> 🔄 시스템 흐름 (Flow)</summary>

![template_post](docs/images/1.template_post.png)
![templates_item](docs/images/2.templates_item.png)
![template_generate](docs/images/3.template_generate.png)
![task_post](docs/images/4.task_post.png)
![task_done](docs/images/5.task_done.png)

#### 🛠️ Step 1. 반복 템플릿 생성

* API: POST /templates
* 설명: WEEKDAYS(평일) 또는 WEEKENDS(주말)와 같이 할 일들을 묶어줄 큰 틀을 먼저 만듭니다.
* 
#### 📥 Step 2. 템플릿 아이템 등록 (할 일 리스트 구성)
* API: POST /templates/{id}/items
* 설명: 생성된 템플릿 안에 실제 수행할 구체적인 할 일들을 담습니다. 여러 번 호출하여 리스트를 구성합니다.
  * 예시: 공부(1), 운동(2), 독서(3)

#### 🚀 Step 3. 특정 날짜에 할 일(Task) 일괄 생성
* API: POST /templates/{id}/generate?date=YYYY-MM-DD
* 설명: 지정한 날짜에 템플릿에 등록된 모든 아이템을 실제 Task로 변환합니다.
  * 중복 방지: 이미 생성된 날짜에 다시 요청해도 중복 생성되지 않으며, 결과 집계 정보만 반환됩니다.
</details>

<details>
<summary> 📋 상세 API Endpoints (수정본)</summary>

### 📝 할 일 관리 (Tasks)
* ➕ POST /tasks : 새로운 할 일 생성
* 🔍 GET /tasks : 날짜/기간/상태별 할 일 목록 검색
* 📖 GET /tasks/{id} : 할 일 상세 단건 조회
* ✏️ PATCH /tasks/{id} : 할 일 내용 및 설정 수정
* 🗑️ DELETE /tasks/{id} : 할 일 삭제
* ✅ POST /tasks/{id}/complete : 할 일 완료 처리 (멱등성 키 필요)
* ↩️ POST /tasks/{id}/undo : 완료 또는 스킵 취소 (PLANNED 상태 복귀)
* ⏩ POST /tasks/{id}/skip : 할 일 스킵(건너뛰기) 처리
* 📜 GET /tasks/{id}/events : 해당 할 일의 변경 이력(이벤트) 조회

### 📋 템플릿 관리 (Templates)
* ⚙️ POST /templates : 새로운 반복 템플릿 생성
* 🚀 POST /templates/{id}/generate : 특정 날짜에 템플릿 기반 Task 일괄 생성

### 📂 템플릿 아이템 (Template Items)
* 📑 GET /templates/{id}/items : 특정 템플릿에 등록된 아이템 목록 조회
* 📥 POST /templates/{id}/items : 템플릿에 새로운 할 일 아이템 추가
### 🏁 마감 처리 (Day Close)
* 🔒 POST /day-close : 특정 날짜 마감 및 미완료 작업 이월 처리
### 📊 통계 리포트 (Reports)
* 📅 GET /reports/daily : 일별 작업 통계 집계
* 📈 GET /reports/summary : 지정 기간 전체 요약 통계
* 🗂️ GET /reports/templates : 템플릿별 달성도 집계
### 🛠️ 관리자/개발용 (Admin/Dev)
* ⚡ POST /admin/jobs/day-close/run : 특정 날짜 마감 작업 강제 실행
* 🔄 POST /admin/jobs/day-close/run-yesterday : 어제 날짜 기준 마감 작업 실행

</details>
---
