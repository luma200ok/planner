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

---

### 🏛️ 1. 시스템 아키텍처 (The Foundation)
#### "튼튼한 기초 위에 유연한 코드가 자란다"
<details> 
<summary>아키텍처 상세 보기</summary>

* QueryDSL 통합: 자바 코드로 쿼리를 작성하여 컴파일 시점에 문법 오류를 잡고, 타입 안정성을 확보했습니다.
* 계층 분리: `domain`, `application`, `repository`, `web`으로 명확히 계층을 나누어 유지보수성을 높였습니다.
* Soft Delete: `SQLDelete`와 `SQLRestriction`을 활용하여 데이터 삭제 시 물리적 삭제 대신 플래그를 통한 논리적 삭제를 구현했습니다.
</details>

---📜

### 🎨 2. 객체지향 도메인 설계 (The Blueprint)
#### "계산은 설계도가, 판단은 제작자가"
<details> <summary>도메인 모델링 상세 보기</summary>
* Template (설계도): 반복 규칙(`DAILY`, `WEEKLY` 등)을 보유하며, 특정 날짜가 자신의 규칙에 부합하는지 스스로 계산(`matches()`)합니다.
* Task (제작물): 설계도에 의해 생성된 실체로서, 완료/스킵 여부 등 자신의 상태를 스스로 판단하고 변경합니다.
* Thin Service: 비즈니스 로직을 도메인 객체로 이관하여 서비스 계층을 가볍게 유지하고 가독성을 확보했습니다.
</details>

---

### 🚀 3. 성능 및 지연 생성 전략 (The Optimization)
#### "설계도가 없어진다고 해서 지어진 건물까지 부술 필요는 없다"
* 연관 관계 해제(Set Null): 템플릿 삭제 시 기존 Task들과의 연결 고리만 끊는 `disconnectTemplate()` 로직을 구현했습니다.
    * 사용자가 과거에 수행했던 완료 기록은 소중한 자산이기에 `CascadeType.REMOVE`를 지양하고 데이터를 보존합니다.
* 소급 변조 방지: 템플릿 수정 시 기존 데이터는 유지하고, 수정 이후 시점부터 새로운 규칙이 적용되도록 설계하여 데이터 정합성을 확보했습니다.


--- 

### 🚀 4. 성능 및 지연 생성 전략 (The Optimization)
#### "DB의 화를 달래는 효율적인 공장"
<details> <summary>삭제 및 수정 전략 상세 보기</summary>

* Set Null 삭제 전략: 템플릿 삭제 시 연관된 Task들과의 연결 고리를 끊는 disconnectTemplate() 로직을 구현했습니다. 
* 데이터 자산 보호: 사용자가 과거에 수행했던 완료 기록은 소중한 자산이기에 CascadeType.REMOVE를 지양하고 참조만 해제하여 데이터를 보존합니다. 
* 수정 이력 유지: 템플릿 수정 시 기존 데이터는 그대로 유지하고, 수정 이후 시점부터 새로운 규칙이 적용되도록 하여 데이터 소급 변조를 방지했습니다. 
</details>

---

### ⚡ 5. 성능 최적화 및 배치 자동화 (Optimization)
#### "DB 부하를 감소시킨 지연 생성 전략"
<details> <summary>최적화 기법 상세 보기</summary>

* 지연 생성(Lazy Generation): 템플릿 생성 시 미래 데이터를 한꺼번에 생성하지 않고, 당장 필요한 7일치 데이터만 우선 생성하여 초기 DB 부하를 최소화했습니다.
* 스케줄러 배치 자동화: @Scheduled를 활용해 매주 일요일 0시마다 차주 일정을 자동 생성하며, 멱등성 보장 로직을 통해 안정성을 확보했습니다.
</details>

---

### 🐞 6. 문제 해결 및 UX 개선 (Problem Solving)
#### "사용자의 의도를 파악하고 UI 정합성을 확보하다"
<details> <summary>트러블슈팅 사례 보기</summary>

* 템플릿 그룹화 UI: 요일별로 생성된 개별 템플릿들을 프론트엔드에서 이름별로 그룹화하여 가독성을 높이고 일괄 관리 기능을 구현했습니다.
* 팝업 기반 정합성 확보: 인라인 수정의 불안정성을 배제하기 위해 prompt 방식의 일관된 인터페이스를 채택하여 데이터 변경의 신뢰성을 확보했습니다.
* 비동기 병렬 처리: 다중 요일 선택 시 Promise.all을 활용하여 기존 단일 생성 API를 효율적으로 재사용했습니다.
</details>

---

### 7.🧪 검증: JUnit & AssertJ (The Safety Net)
#### "안전망 위에서 코드는 더 자유로워진다"
<details> <summary>테스트 전략 상세 보기 </summary>

* G-W-T 패턴: Given-When-Then 구조를 통해 테스트 코드의 목적을 명확히 하고 가독성을 높였습니다.
* 도메인 단위 테스트: 템플릿 규칙에 따른 날짜 매칭 로직 등 핵심 도메인 비즈니스를 철저히 검증했습니다.
</details>
---

최근 업데이트: 2026.02.14 - 운영 도구(스케줄러 수동 실행) 및 데이터 보존 전략 반영