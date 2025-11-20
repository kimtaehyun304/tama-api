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

### 프로젝트 스킬
스프링 부트3 (mvc·rest api, valid, security, cache, hibernate), mysql

### 프로젝트로 얻은 경험

 <a href="https://github.com/kimtaehyun304/tama-api/blob/180628544c098dc074be2c34929a40bc9431f2d3/src/main/java/org/example/tamaapi/repository/item/query/ItemQueryRepository.java#L56">
상품 검색 쿼리 속도 개선
 </a>
 <ul>
  <li>상품 테이블 row 십만개 넣고 진행</li>
  <li>카운트 쿼리 중복 row 제거 방법 변경 (distinct 0.9s → exists 0.6s)</li>
  <li>
   <a href="https://velog.io/@hyungman304/%EC%B9%B4%EC%9A%B4%ED%8A%B8%EC%A0%95%EB%A0%AC-%EC%BF%BC%EB%A6%AC-%ED%8A%9C%EB%8B%9D-with-%EC%9D%B8%EB%8D%B1%EC%8A%A4#exists-06s-1">
   정렬 쿼리 row 제거 방법 변경 (exists 0.6s → subQuery 0s)
   </a>
  </li>
  <ul>
   <li>최신순) 기존 인덱스 쓰려고 order by 필드 변경 (created_at → item.id)</li>
   <li>가격순) 인덱스 적용을 위해, 함수 제거 및 테이블 컬럼 변경</li>
   <li>ex) colasecse(disconted_price, price) → now_price</li>
  </ul>
</ul>

<a href="https://github.com/kimtaehyun304/tama-api/blob/0130e7c2b935cdd39a3afe7f908184db51f9b3f5/src/main/java/org/example/tamaapi/controller/ItemApiController.java#L126">
 인기 상품 API 응답 속도 개선 
</a>
<ul>
 <li>SQL SUM 함수를 사용하므로, 동시에 요청 오면 느린 걸 확인</li>
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
 <li>재고 음수 방지를 위해, where c.stock >= :quantity & updated row 수 확인</li>
</ul>

<a href="https://github.com/kimtaehyun304/tama-api/blob/b649db7ce5fda02504a65004ab4d1abdba8a6d7b/src/main/java/org/example/tamaapi/controller/OrderApiController.java#L159">
 이메일 전송을 비동기로 분리
</a>
<ul>
 <li>외부 이메일 서버 장애를 격리하기 위해 분리</li>
 <li>주문 완료 응답 속도 개선 4000ms → 400ms</li>
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

<a href="https://github.com/kimtaehyun304/tama-api/blob/0efd407922c8d3281cdc5413517478f928e9a12c/src/main/java/org/example/tamaapi/event/SignedUpEventHandler.java#L33">
스프링 이벤트
</a>
<ul>
 <li>회원가입 시 웰컴 메일 전송 및 쿠폰 제공</li>
 <li>기존엔 내용이 회원가입 로직에 들어있었지만, 이벤트로 분리</li>
 <li>왜냐하면 회원가입 기능만 남기기 위해</li>
</ul>

<a href="https://github.com/kimtaehyun304/tama-api/blob/0efd407922c8d3281cdc5413517478f928e9a12c/src/main/java/org/example/tamaapi/config/batch/AutoOrderCompleteJobConfig.java#L41">
스프링 배치
</a>
<ul>
 <li>배송이 완료되고 7일째 되는 날 자동으로 구매 확정</li>
 <li>로직 중복 실행 예방 및 실패하면 재시도를 위해 스프링 배치 도입</li>
</ul>

패턴
<ul>
 <li>
  <a href="https://github.com/kimtaehyun304/tama-api/blob/e35dfd1e6a51b00c042898593c88513ebc04ba88/src/main/java/org/example/tamaapi/domain/order/Order.java#L76">
   회원·비회원 주문 구분을 위해 정적 팩토리 메소드 사용
  </a>
 </li>
  <li>조기 종료 패턴 사용</li>
  <li>빌더 패턴은 적용하고 생산성이 저하되는 걸 느끼고 지양</li>
  <li>DDD 설계</li>
</ul>

기타
<ul>
 <li>
  <a href="https://github.com/kimtaehyun304/tama-api/blob/5a0433c9634e03ac5d25a37ba15553a9f8042b8d/src/main/java/org/example/tamaapi/config/aspect/PreAuthenticationAspect.java#L36">
   코드 간소화를 위해 AOP 어노테이션으로 유저 권한 조회
  </a>
 </li>
 <li>스프링 시큐리티 인증을 커스텀하기 위해 @AuthenticationPrincipal 사용</li>
 <li>ouath2·jwt 기반 인증</li>
 <li>
  <a href="https://github.com/kimtaehyun304/tama-api/blob/2b5e350c81cf7ae92ea829f930572a0133eb927b/src/test/java/org/example/tamaapi/controller/ItemApiControllerTest.java#L112">
  테스트 코드 작성
  </a>
 </li>
</ul>


### API
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
