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
흐름: 스프링 시큐리티 필터체인 → 토큰 인증 필터 → 컨트롤러 → 공통 예외 처리

스프링 시큐리티 인증 객체
<ul>
 <li>인증 객체 커스텀 - @AuthenticationPrincipal</li>
 <li>ex) @AuthenticationPrincipal Long memberId</li>
</ul>

스케줄러
<ul>
  <li>인기 상품 쿼리 로컬 캐싱</li>
  <li>
    <a href="https://github.com/kimtaehyun304/tama-api/blob/7e25461d15214a4566ed46ca02709cccf93e24ed/src/main/java/org/example/tamaapi/scheduler/batch/AutoOrderCompleteJobConfig.java#L43">
      배송완료 상품 자동 구매 확정 (스프링 배치)
    </a>
  </li>
</ul>

oauth2 로그인
- 문제: oauth2는 로그인하면 토큰을 url에 붙여 리다이렉트시킴 (보안 bad)
- 해결: 임시 문자를 url에 붙이고, 프론트에서 임시 문자로 jwt 요청 
- ㄴ응답 바디로 받으면 노출되지 않아서 안전

일반 회원가입
<ul>
  <li>서버는 인증문자를 로컬 캐시에 저장하고, 이메일로 전송</li>
  <li>사용자는 이메일로 인증문자를 확인하고 기입</li>
  <li>회원가입 시 웰컴 메일 전송 및 쿠폰 제공 (스프링 이벤트)</li>
</ul>

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

#### SQL
상품 상세 쿼리
<ul>
  <li>한번에 조인할 수 없어 여러번 쿼리하고 합침</li>
  <li>합치는 과정에 map 자료구조 사용</li>
  <li>java 8 스트림, 람다 문법으로 코드 줄임</li>
</ul>

<a href="https://github.com/kimtaehyun304/tama-api/blob/7e25461d15214a4566ed46ca02709cccf93e24ed/src/main/java/org/example/tamaapi/repository/item/query/ItemQueryRepository.java#L56"> 
상품 검색 쿼리
</a><br>
<ul>
  <li>검색 조건 제공 ex)가격, 색상, 성별, 품절, 이름</li>
  <li>동적 조건이라 queryDsl 사용 (BooleanExpression)</li>  
  <li>페이징, 정렬 옵션 제공</li>
  <li>페이징은 data jpa 페이징을 커스텀</li>
</ul>     

<a href="https://github.com/kimtaehyun304/tama-api/blob/3ceffeb519f348f45d99b6b03a8ec11bf9405803/src/main/java/org/example/tamaapi/Scheduler.java#L27"> 
인기 상품 쿼리
</a><br>
<ul>
  <li>그룹 합수 사용하니 rds cpu 70% → tps 14 기록</li>
  <li>스프링 로컬 캐시 적용 → tps 2400 기록</li>  
</ul>  

<a href="https://github.com/kimtaehyun304/tama-api/blob/284ee0e18267a9cc732b929609db6d79f176d203/src/main/java/org/example/tamaapi/service/ItemService.java#L33"> 
상품 저장 쿼리
</a><br>
연관관계: 상품 -< 색상 컬렉션 -< 색상별 사이즈
<ul>
  <li>색상 컬렉션 저장은 jdbcTemplate batch insert</li>
  <li>색상별 사이즈도 jdbcTemplate로 하려니 색상 컬렉션 pk를 몰라서 실패</li>
  <li>색상 컬렉션 정보를 가지고 색상 컬렉션 pk를 조회하고 넣어줌</li>  
</ul>  

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

<a href="https://github.com/kimtaehyun304/tama-api/blob/7e25461d15214a4566ed46ca02709cccf93e24ed/src/main/java/org/example/tamaapi/service/OrderService.java#L58"> 
주문 저장  
</a>

흐름: 재고 차감 → 쿠폰 차감 → 포인트 차감 → 포인트 적립 → 실패시 롤백 → 결제 취소 → 공통 예외 처리  

종류: 회원 주문, 비회원 주문, 무료 주문    
<ul>
  <li>모바일 주문을 위해, 주문 정보를 포트원 서버에 저장</li>
  <li>배송지 목록 저장 제공 (기본 배송지 설정)</li>
  <li>쇼핑백 기능 제공</li>
  <li>비회원은 주문 번호를 이메일로 발송 (비동기)</li>
</ul> 

주문 조회 
<ul>
  <li>비회원은 주문번호+주문자명으로 조회 (basic 인증)</li>
  <li>주문한 경우만 리뷰 작성 가능</li>
  <li>리뷰 작성했는지는 inner join으로 판별</li> 
</ul>  

#### 설정 파일

config 폴더
<ul>
  <li>json 이스케이프 (xss 예방)</li>
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

### 트러블 슈팅
인덱스로 쿼리 속도 개선
 <ul>
  <li>상황: 상품 row 총 700,000개 존재</li>
  <li>
    <a href="https://velog.io/@hyungman304/SQL-exists-vs-distinct">
     중복 row 제거 성능 비교 - exists vs distinct vs subQuery
    </a>
  </li>
  <li>pk 인덱스 사용하려고 order by 컬럼 변경 (created_at → item.id pk)</li>
  <li>import 후 느림 → analyze로 통계정보 최신화 </li>
  <li>explain 조인 순서가 비효율적 → 스트레이트 조인 힌트로 순서 강제</li>
 </ul>
</ul>

인덱스 지식
* pk 인덱스 사용하려고 order by 컬럼 변경 (created_at → item.id pk) 
* db 함수는 인덱스 미적용 → db 함수 안 써도 되게 컬럼 재설계 
* ex) colasecse(discounted_price, price) → now_price, original_price
* ㄴ할인 중이 아니면 discounted_price는 null / now_price는 현재 가격

<a href="https://github.com/kimtaehyun304/tama-api/blob/284ee0e18267a9cc732b929609db6d79f176d203/src/main/java/org/example/tamaapi/service/ItemService.java#L67">
db 동시 요청
</a>
<ul>
 <li>갱신 분실 방지를 위해, jpa 변경 감지 → 일반 update 변경 (배타 락)</li>
 <li>낙관, 비관 락 보다 간단하고 성능 좋음</li>
 <li>재고 음수 방지를 위해, update .. where stock >= quantity (updated row 수 체크)</li>
 <li>updated row 수 0이면 예외 던짐</li>
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
