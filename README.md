# 🗓️ Planner Project

> **Spring Boot 기반의 스마트 일정 관리 백엔드 서버**  
> "데이터는 자산입니다. 설계도는 사라져도 건물은 남겨야 합니다."

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
### 리펙토링 완료후 서버 재가동 예정
### 🏛️ 1. 시스템 아키텍처 (The Foundation)
#### "튼튼한 기초 위에 유연한 코드가 자란다"
<details> 
<summary>아키텍처 상세 보기</summary>

* **QueryDSL 통합:** 자바 코드로 쿼리를 작성하여 컴파일 시점에 타입 안정성을 확보하고, 동적 쿼리 대응력을 높였습니다.
* **DDD 기반 계층 분리:** `domain`, `application`, `repository`, `web`으로 계층을 엄격히 나누어 각 클래스의 책임과 역할을 명확히 했습니다.
* **Soft Delete:** 데이터의 물리적 삭제 대신 `SQLDelete`와 `SQLRestriction`을 활용한 논리적 삭제를 구현하여 운영 데이터의 안전성을 높였습니다.
</details>

---

### 🎨 2. 객체지향 도메인 설계 (The Blueprint)
#### "계산은 설계도가, 판단은 제작자가"
<details> <summary>도메인 모델링 상세 보기</summary>

* **Template (설계도):** 반복 규칙(`DAILY`, `WEEKLY` 등)을 관리하며, 특정 날짜가 규칙에 부합하는지 스스로 계산(`matches()`)하는 능동적 객체입니다.
* **Task (제작물):** 설계도에 의해 생성된 실체로, 완료/스킵 등 자신의 상태 변경에 대한 책임을 직접 가집니다.
* **Thin Service:** 비즈니스 로직을 도메인 객체로 과감히 이관하여 서비스 계층을 가볍게 유지하고 가독성을 확보했습니다.
</details>

---

### 🚀 3. 데이터 보존 및 무결성 전략 (Data Integrity)
#### "데이터는 자산이다: 설계도가 사라져도 기록은 보존되어야 한다"
<details> <summary>삭제 및 수정 전략 상세 보기</summary>

* **Set Null 삭제 전략:** 템플릿 삭제 시 연관된 Task들과의 연결 고리만 끊는 `disconnectTemplate()` 로직을 통해 데이터 간 의존성을 관리합니다.
* **데이터 자산 보호:** 과거의 수행 기록을 보존하기 위해 `CascadeType.REMOVE`를 지양하고 참조만 해제하는 방식을 채택했습니다.
* **소급 변조 방지:** 템플릿 수정 시 기존 데이터의 역사적 정합성을 유지하고, 수정 이후 시점부터 새로운 규칙이 적용되도록 설계했습니다.
</details>

---

### ⚡ 4. 성능 최적화 및 배치 자동화 (Performance & Automation)
#### "DB 부하를 감소시킨 지연 생성 전략"
<details> <summary>최적화 기법 상세 보기</summary>

* **지연 생성(Lazy Generation):** 템플릿 생성 시 미래 데이터를 전수 생성하지 않고, 당장 필요한 7일치 데이터만 우선 생성하여 초기 부하를 최소화했습니다.
* **스케줄러 배치 자동화:** `@Scheduled`를 활용해 매주 일요일 0시마다 차주 일정을 자동 생성하며, 멱등성이 보장된 로직으로 운영 안정성을 확보했습니다.
</details>

---

### 🐞 5. 문제 해결 및 UX 개선 (Problem Solving)
#### "사용자의 의도를 파악하고 인터페이스 정합성을 확보하다"
<details> <summary>트러블슈팅 사례 보기</summary>

* **UI 그룹화 로직:** 요일별 개별 템플릿을 이름별로 그룹화하여 전달하는 API 로직을 통해 관리 효율성을 높였습니다.
* **상태 관리 정합성:** 인라인 수정의 불안정성을 배제하기 위해 일관된 팝업 인터페이스를 채택하여 데이터 변경의 신뢰도를 높였습니다.
* **비동기 병렬 처리:** 다중 요일 선택 생성 시 `Promise.all`을 활용하여 기존 단일 생성 API의 효율적 재사용과 응답 속도를 개선했습니다.
</details>

---

### 6. 🧪 검증: JUnit & AssertJ (The Safety Net)
#### "안전망 위에서 코드는 더 자유로워진다"
<details> <summary>테스트 전략 상세 보기 </summary>

* **G-W-T 패턴:** Given-When-Then 구조를 일관되게 적용하여 테스트 코드 자체를 명세서처럼 읽히도록 작성했습니다.
* **핵심 로직 검증:** 템플릿 규칙 매칭 및 상태 변경 로직 등 비즈니스의 핵심이 되는 도메인 단위 테스트를 철저히 수행했습니다.
</details>

---

최근 업데이트: 2026.02.15 - 오타 수정 및 검수