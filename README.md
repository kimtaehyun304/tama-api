<h1>쇼핑몰 1인 개발 / 2024.12 ~ </h1>

### 인프라
<p align="center">
<img src="https://github.com/user-attachments/assets/cd62fb35-4615-4a69-bf6a-197f93bc2a7e"/>
</p>
<p align="center">현재는 비용 때문에 ELB 지움</p>

<p align="center">
<img src="https://github.com/user-attachments/assets/831eec69-3ccc-462a-97be-e23b4810b380"/>
</p>

<p>https://dlta.kr</p>

### 기술
* 스프링 부트 3.4 (mvc, security, valid, aop, cache, batch)
* mysql 8, hibernate 6, data jpa, querydsl 5

### 구조

#### 흐름
스프링 시큐리티 필터체인 → 토큰 인증 필터 → 컨트롤러 → 공통 예외 처리

스케줄러
<ul>
  <li>인기 상품 쿼리 로컬 캐싱</li>
  <li>배송완료 상품 구매 확정</li>
</ul>

ouath2 로그인
<ul>
  <li>oauth2는 인증이 완료되면 토큰을 url로 전달 (보안에 안 좋다고 판단)</li>
  <li>임시토큰을 전달하고 api 호출로 진짜 토큰을 받도록 함</li>
</ul>

일반 회원가입
<ul>
  <li>서버는 인증문자를 로컬 캐시에 저장하고, 이메일로 전송</li>
  <li>사용자는 이메일로 인증문자를 확인하고 기입</li>
</ul>

#### SQL
상품 상세 쿼리
<ul>
  <li>한번에 조인할 수 없어 여러번 쿼리하고 합침</li>
  <li>합치는 과정에 map 자료구조 사용</li>
  <li>java 8 스트림, 람다 문법으로 코드 줄임</li>
</ul>

상품 검색 쿼리
<ul>
  <li>검색 조건 제공 ex)가격, 색상, 성별, 품절, 이름</li>
  <li>동적 조건이라 queryDsl 사용 (BooleanExpression)</li>  
  <li>페이징, 정렬 옵션 제공</li>
  <li>페이징은 data jpa 페이징을 커스텀</li>
</ul>     

인기 상품 쿼리
<ul>
  <li>그룹 합수 사용하니 rds cpu 70% → tps 14 기록</li>
  <li>스프링 로컬 캐시 적용 → tps 2400 기록</li>  
</ul>  

상품 저장 쿼리

#### 주문
흐름: 포트원 결제 → 주문 요청 → 주문 검증 → 주문 완료  

주문 검증
<ul>
  <li>주문 폼 누락 확인</li>
  <li>재사용된 결제번호가 아닌지 확인</li>
  <li>결제 금액이 조작된 게 아닌지 확인</li>
  <li>사용하려는 포인트를 그만큼 갖고 있는지 확인</li>
  <li>쿠폰 유효기간이 안 지났는지 확인</li>
</ul>  

주문 저장  
종류: 회원 주문, 비회원 주문, 무료 주문  
흐름: 재고 차감 → 쿠폰 차감 → 포인트 차감 → 포인트 적립 → 실패시 롤백 → 결제 취소 → 공통 예외 처리  

<ul>
  <li>모바일 주문을 위해, 주문 정보를 포트원 서버에 저장</li>
  <li>비회원은 주문 번호를 이메일로 발송</li>
  <li>배송지 목록 저장 제공 (기본 배송지 설정)</li>
  <li>쇼핑백 기능 제공</li>
</ul> 

주문 조회 
<ul>
  <li>비회원은 주문번호+주문자명으로 조회 (basic 인증)</li>
  <li>주문한 경우만 리뷰 작성 가능</li>
  <li>리뷰 작성했는지는 outer join으로 판별</li> 
</ul>  

#### 설정 파일

config 폴더
<ul>
  <li>json 이스케이프</li>
  <li>restClient 타임아웃 설정</li>
  <li>비동기 쓰레드 풀 설정</li>
  <li>cors 설정 (UrlBasedCorsConfigurationSource)</li>
</ul>

application.yml
<ul>
  <li>open-in-view: false</li>
  <li>배포엔 application-prod.yml</li>
</ul>

logback-spring.xml
<ul>
  <li>로그 파일 용도로 쓰다가 beanstalk 전환 후 필요 없어짐</li>
</ul>

### 아키텍처
필터 
-jwt 인증

서블릿
- 중앙 예외 처리
- 시큐리티 인증 객체
- 시큐리티 권한 전체 허용 / 권한 필요한 메서든 권한 검증

### 디자인 패턴
<ul>
  <li>도메인 주도 설계</li>
 
  <li>
    <a href="https://github.com/kimtaehyun304/tama-api/blob/5a0433c9634e03ac5d25a37ba15553a9f8042b8d/src/main/java/org/example/tamaapi/config/aspect/PreAuthenticationAspect.java#L36">
     AOP (인증, 로그)
    </a>
  </li>
  <li>빌더 패턴</li>
  <li>조기 종료 패턴</li>
 <li>
  <a href="https://github.com/kimtaehyun304/tama-api/blob/e35dfd1e6a51b00c042898593c88513ebc04ba88/src/main/java/org/example/tamaapi/domain/order/Order.java#L76">
   정적 팩토리 메소드 (회원·비회원 주문 구분)
  </a>
 </li>
  <li>
  <a href="https://github.com/kimtaehyun304/tama-api/blob/2b5e350c81cf7ae92ea829f930572a0133eb927b/src/test/java/org/example/tamaapi/controller/ItemApiControllerTest.java#L112">
  테스트 코드 작성
  </a>
 </li>
</ul>

### 프로젝트로 얻은 경험
 <a href="https://velog.io/@hyungman304/SQL-exists-vs-distinct">
상품 검색 쿼리 속도 개선
 </a>
 <ul>
  <li>상품 테이블 row 총 700,000만개 넣고 진행</li>
  <li>카운트 쿼리 중복 row 제거 방법 변경 (distinct → exists)</li>
  <li>정렬 쿼리 중복 row 제거 방법 변경 (exists → subQuery)</li>
  <li>기존 인덱스 재사용하려고 order by절 컬럼 변경 (created_at → item.id)</li>
  <li>인덱스 적용하려고 db 함수 제거하고 컬럼 변경</li>
  <li>ex) colasecse(disconted_price, price) → now_price</li>
 </ul>
</ul>

<a href="https://github.com/kimtaehyun304/tama-api/blob/0130e7c2b935cdd39a3afe7f908184db51f9b3f5/src/main/java/org/example/tamaapi/controller/ItemApiController.java#L126">
 인기 상품 API 응답 속도 개선 
</a>
<ul>
 <li>SQL SUM 함수를 사용하여 동시에 요청 오면 느림</li>
 <li>카페인 캐시에 저장하는 걸로 변경 
<a href="https://github.com/kimtaehyun304/tama-api/blob/3ceffeb519f348f45d99b6b03a8ec11bf9405803/src/main/java/org/example/tamaapi/Scheduler.java#L27">
  (스케줄러로 24시간 마다 교체)
</a>
 </li>
  <li>5분간 부하 평균 응답 5000ms → 80ms, TPS 15 → 2470</li>
</ul>

<a href="https://github.com/kimtaehyun304/tama-api/blob/284ee0e18267a9cc732b929609db6d79f176d203/src/main/java/org/example/tamaapi/service/ItemService.java#L33"> 
 bulk insert PK 누락 문제 해결
</a>
 <ul>
  <li>insert 쿼리를 줄이기 위해, data jpa saveAll() → jdbcTemplate bulk insert</li>
  <li>bulk insert는 PK가 안 채워짐 → 해당 PK를 외래키로 쓰는 엔티티 저장 시 문제 발생</li>
  <li>bulk insert 이후 DB 조회하여 PK 채움</li>
 </ul>

<a href="https://github.com/kimtaehyun304/tama-api/blob/284ee0e18267a9cc732b929609db6d79f176d203/src/main/java/org/example/tamaapi/service/ItemService.java#L67">
 상품 주문 동시성 문제 해결
</a>
<ul>
 <li>갱신 분실 방지를 위해, jpa 변경 감지 → 직접 update (배타 락을 통한 대기)</li>
 <li>재고 음수 방지를 위해, update .. where stock >= quantity (updated row 수 체크) </li>
</ul>

<a href="https://github.com/kimtaehyun304/tama-api/blob/b649db7ce5fda02504a65004ab4d1abdba8a6d7b/src/main/java/org/example/tamaapi/controller/OrderApiController.java#L159">
 이메일 전송을 비동기로 분리
</a>
<ul>
 <li>외부 이메일 서버 장애를 격리하기 위해</li>
 <li>주문 완료 응답 속도 개선 4초 → 0.4초</li>
</ul>

결제 API 개발
<ul>
  <li>
  <a href="https://github.com/kimtaehyun304/tama-api/blob/a3901e82a376ca438e13b546c6a82897b4eb1c1f/src/main/java/org/example/tamaapi/service/OrderService.java#L298">
  주문 저장 전에 검증 ex)결제 금액 조작, 입력 값 누락
  </a>
 </li>
 <li>
  <a href="https://github.com/kimtaehyun304/tama-api/blob/a3901e82a376ca438e13b546c6a82897b4eb1c1f/src/main/java/org/example/tamaapi/service/OrderService.java#L57">
  주문 中 예외 발생 → DB 롤백, 결제 취소
  </a>
 </li>
 <li>PC·모바일</li>
 <li>회원·비회원</li>
 <li>쿠폰·포인트</li>
</ul>

### 스프링 기능
<a href="https://github.com/kimtaehyun304/tama-api/blob/0efd407922c8d3281cdc5413517478f928e9a12c/src/main/java/org/example/tamaapi/event/SignedUpEventHandler.java#L33">
스프링 이벤트
</a>
<ul>
 <li>회원가입 시 웰컴 메일 전송 및 쿠폰 제공</li>
 <li>주요 기능인 회원가입 기능만 남기기 위해, 쿠폰 제공을 이벤트로 분리</li>
</ul>

<a href="https://github.com/kimtaehyun304/tama-api/blob/0efd407922c8d3281cdc5413517478f928e9a12c/src/main/java/org/example/tamaapi/config/batch/AutoOrderCompleteJobConfig.java#L41">
스프링 배치
</a>
<ul>
 <li>배송이 완료되고 7일째 되는 날, 자동으로 구매 확정 처리</li>
 <li>배치 중복 실행 예방과 실패시 재시도를 위해 스프링 배치 도입</li>
</ul>

스프링 시큐리티
<ul>
 <li>인증 객체 커스텀 - @AuthenticationPrincipal</li>
 <li>ouath2·jwt 기반 인증</li>
</ul>

### API 문서
상품 API
<ul>
 <li>상품 상세</li>
 <li>상품 검색</li>
 <li>상품 등록·이미지 업로드</li>
 <li>인기 상품 조회</li>
 <li>장바구니에 담긴 상품 조회</li>
 <li>카테고리 조회</li>
 <li>색상 조회</li>
 <li>리뷰 조회·등록</li>
</ul>

주문 API
<ul>
 <li>주문 조회</li>
 <li>상품 주문 (포트원 연동)</li>
 <li>자주 쓰는 배송지 조회·등록</li>
</ul>

인증 API
<ul>
  <li>로그인·회원가입 (oauth2·jwt)</li>
  <li>회원가입에 필요한 인증 문자를 이메일로 전송</li>
  <li>관리자인지 확인 (isAdmin)</li>
</ul>

### erd
<p align="center">
<img src="https://github.com/user-attachments/assets/69455699-3fa4-4dd0-9ee9-ce8ea3284cd4"/>
</p>
