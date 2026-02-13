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

### 🛠️ 1. 시스템 아키텍처 (The Foundation)
#### "튼튼한 기초 위에 유연한 코드가 자란다"
* QueryDSL 통합: 자바 코드로 쿼리를 작성하여 컴파일 시점에 문법 오류를 잡고, 타입 안정성을 확보했습니다.
* 패키지 구조화: `domain`, `application`, `repository`, `web`, `config`, `exception`으로 계층을 분리하여 유지보수가 쉬운 환경을 구축했습니다.

---

### 🎨 2. 객체지향 도메인 설계 (The Blueprint)
#### "계산은 설계도가, 판단은 제작자가"
* **Template (규칙 도메인)** : 반복 규칙(`DAILY`, `WEEKDAYS` 등)을 보유한 `설계도` 로서, 특정 날짜가 자신의 규칙에 부합하는지 스스로 계산(`matches()`)합니다.
* **Task (실행 도메인)** : 설계도에 의해 생성된 실체인 `제작자`로서, 할 일의 완료/스킵 여부 등 자신의 상태를 스스로 판단하고 변경(`complete`, `skip`)합니다.
* **Thin Service Architecture**: 복잡한 비즈니스 로직을 위 도메인 객체들로 이관하여 서비스 계층을 가볍게 유지하고, 가독성과 테스트 용이성을 확보했습니다.
---

### 🚀 3. 성능 및 지연 생성 전략 (The Optimization)
#### "DB의 화를 달래는 효율적인 공장"
* 지연 생성(Lazy Generation): 템플릿 생성 시 미래 데이터를 한 번에 생성하지 않고, 당장 필요한 7일치 데이터만 우선 생성하여 초기 DB 부하를 최소화했습니다.
* 스케줄러 배치 자동화:`@Scheduled`를 활용해 매주 일요일 0시(`0 0 0 * * SUN`)마다 차주 일정을 자동으로 생성하는 시스템을 구축했습니다.
* 멱등성(Idempotency) 보장: 중복 생성 방지 로직(`isPresent`)을 통해 배치가 중복 실행되어도 데이터 무결성을 유지합니다.
---

### 🐞 4. 문제 해결(The Debugging)
#### "사용자의 의도를 정확히 파악하는 로직"
* UI/API 정합성 확보: 빠른 추가 기능의 `id` 불일치 에러를 해결하고, 반복 설정 여부에 따라 `/tasks`와 `/templates` API를 유연하게 분기 처리했습니다.
* 비동기 병렬 처리: 다중 요일 선택 시 `Promise.all`을 활용하여 백엔드 수정 없이 기존 단일 생성 API를 재사용하는 효율적인 통신 구조를 설계했습니다.
---

### 🧪 5. 검증: JUnit & AssertJ (The Safety Net)
#### "내 코드는 틀리지 않았다"를 증명하는 안전망을 쳤습니다.

* G-W-T 패턴: `Given`(준비) - `When`(실행) - `Then`(검증) 구조를 통해 테스트 코드의 가독성을 높였습니다.
* AssertJ: `assertThat().isEqualTo()` 같은 문법을 써서 영어 문장처럼 읽히는 깔끔한 테스트 코드를 작성했습니다.

---
update 26.02.13 
branch teest