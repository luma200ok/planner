🎨 2. 객체지향 도메인 설계 (The Blueprint)
"계산은 설계도가, 판단은 제작자가"
* Enum (TemplateRuleType) - 설계도: DAILY, WEEKDAYS 등 각 규칙이 스스로 날짜를 판별하는 matches() 로직을 내포하여 비즈니스 로직을 캡슐화했습니다.
* Entity (Template) - 제작자: 자신의 데이터(targetDay)와 이넘 설계도를 결합하여 최종 판단을 내리며, 서비스의 짐을 덜어주는 **'풍부한 도메인 모델'**로 설계했습니다.


🛠️ 1. 시스템 아키텍처 (The Foundation)
"튼튼한 기초 위에 유연한 코드가 자란다"
* QueryDSL 통합: 자바 코드로 쿼리를 작성하여 컴파일 시점에 문법 오류를 잡고, 타입 안정성을 확보했습니다.
* 패키지 구조화: domain, application, repository, web, config, exception으로 계층을 분리하여 유지보수가 쉬운 환경을 구축했습니다.
  ⚙️ 3. 서비스 계층 리팩토링 (The Orchestrator)
  "명령만 내리는 얇은 서비스(Thin Service)"
* 책임의 분리: 복잡한 날짜 계산 로직을 도메인으로 밀어 넣어 서비스 코드가 극도로 간결해졌습니다.
* 지휘자 역할: 서비스는 전체적인 흐름(템플릿 로드 → 날짜 순회 → 일괄 저장)만 관리하는 본연의 역할에 집중합니다.

🚀 4. 성능 및 자동화 고도화 (The Optimization)
"DB의 화를 달래는 효율적인 공장"
* Batch 처리 (saveAll): 루프마다 DB를 찌르던 방식에서 벗어나, 리스트에 담아 한 번에 전송하는 트럭 배송 시스템을 도입하여 네트워크 비용을 절감했습니다.
* 스케줄러 시스템: @EnableScheduling과 별도 클래스(PlannerScheduler) 분리를 통해 매주 일요일 새벽(0 0 0 * * SUN)마다 자동으로 할 일을 생성하는 공장을 완성했습니다.

🐞 5. 결정적 버그 해결 (The Debugging)
"사용자의 의도를 정확히 파악하는 로직"
* 기준 날짜 동적화: LocalDate.now()에 고정되어 있던 로직을 파라미터(selectedDate) 기반으로 수정하여, 사용자가 선택한 **미래의 주(Next Week)**도 정확히 찾아가게 만들었습니다.
* DTO 매핑: 프론트엔드의 date 필드와 백엔드 DTO를 일치시켜 데이터가 끊김 없이 흐르도록 연결했습니다.


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
* **Production Base URL**: [http://rkqkdrnportfolio.shop:8081](http://rkqkdrnportfolio.shop:8081)

---

---
# 리팩토링 재공사중
---

### 🛠️ 1. 시스템 아키텍처 (The Foundation)
#### "튼튼한 기초 위에 유연한 코드가 자란다"
* QueryDSL 통합: 자바 코드로 쿼리를 작성하여 컴파일 시점에 문법 오류를 잡고, 타입 안정성을 확보했습니다.
* 패키지 구조화: domain, application, repository, web, config, exception으로 계층을 분리하여 유지보수가 쉬운 환경을 구축했습니다.

---

### 🎨 2. 객체지향 도메인 설계 (The Blueprint)
#### "계산은 설계도가, 판단은 제작자가"
* **Enum (TemplateRuleType)** - 설계도: `DAILY`, `WEEKDAYS` 등 각 규칙이 스스로 날짜를 판별하는 `matches()` 로직을 내포하여 비즈니스 로직을 캡슐화했습니다.
* **Entity (Template)** - 제작자: 자신의 데이터(`targetDay`)와 이넘 설계도를 결합하여 최종 판단을 내리며, 서비스의 짐을 덜어주는 **풍부한 도메인 모델**로 설계했습니다.

---

### ⚙️ 3. 서비스 계층 리팩토링 (The Orchestrator)
#### "명령만 내리는 얇은 서비스(Thin Service)"
* 책임의 분리: 복잡한 날짜 계산 로직을 도메인으로 밀어 넣어 서비스 코드가 극도로 간결해졌습니다.
* 휘자 역할: 서비스는 전체적인 흐름(템플릿 로드 → 날짜 순회 → 일괄 저장)만 관리하는 본연의 역할에 집중합니다.

---

### 🚀 4. 성능 및 자동화 고도화 (The Optimization)
#### "DB의 화를 달래는 효율적인 공장"
* Batch 처리 (`saveAll`): 루프마다 DB를 찌르던 방식에서 벗어나, 리스트에 담아 한 번에 전송하는 트럭 배송 시스템을 도입하여 네트워크 비용을 절감했습니다.
* 스케줄러 시스템: `@EnableScheduling`과 별도 클래스(`PlannerScheduler`) 분리를 통해 매주 일요일 새벽(`0 0 0 * * SUN`)마다 자동으로 할 일을 생성하는 공장을 완성했습니다.

---

### 🐞 5. 결정적 버그 해결 (The Debugging)
#### "사용자의 의도를 정확히 파악하는 로직"
* 기준 날짜 동적화: `LocalDate.now()`에 고정되어 있던 로직을 파라미터(`selectedDate`) 기반으로 수정하여, 사용자가 선택한 **미래의 주(Next Week)**도 정확히 찾아가게 만들었습니다.
* DTO 매핑: 프론트엔드의 `date` 필드와 백엔드 DTO를 일치시켜 데이터가 끊김 없이 흐르도록 연결했습니다.

---

### 🧪 6. 검증: JUnit & AssertJ (The Safety Net)
#### "내 코드는 틀리지 않았다"를 증명하는 안전망을 쳤습니다.

* G-W-T 패턴: `Given`(준비) - `When`(실행) - `Then`(검증) 구조를 통해 테스트 코드의 가독성을 높였습니다.
* AssertJ: `assertThat().isEqualTo()` 같은 문법을 써서 영어 문장처럼 읽히는 깔끔한 테스트 코드를 작성했습니다.

---
