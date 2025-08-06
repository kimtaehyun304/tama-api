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

### 프로젝트 스킬 (스프링)
boot, mvc·rest api, valid, security, cache

### 기능
상품 API
<ul>
 <li>상품 검색</li>
 <li>인기 상품 조회</li>
 <li>장바구니에 담긴 상품 조회</li>
 <li>카테고리 조회</li>
 <li>색상 조회</li>
 <li>상품 등록·이미지 업로드</li>
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
  <li>인증 문자 이메일 전송</li>
  <li>관리자 확인</li>
</ul>

### 프로젝트로 얻은 경험
SQL 경험
<ul>
 <li>상품 검색 쿼리 속도 개선</li>
 <ul>
  <li>커버링 인덱스 적용하여 explain type 개선 (ALL→INDEX) </li>
  <li> 
   <a href="https://github.com/kimtaehyun304/tama-api/blob/8e9e5c00cd3b98b769b5a54c7f54720282eb3e59/src/test/java/org/example/tamaapi/TamaApiApplicationTests.java#L40">
    서브쿼리 vs 조인+groupBy 성능 비교 → 서브쿼리 선택 
   </a>
  </li>
 </ul>
 <li>상품 연관관계(1:N - 1:N) 조인하기 어려웠음 → 아래 방법들로 해결</li>
 <ul>
  <li>공통 정보 -&lt; 색상 -&lt; 사이즈·재고</li>
  <li>쿼리 여러번 나눠서 하기</li>
  <li>조인하고 groupBy하기</li>
  <li>서브쿼리 하기</li>
 </ul>
 <li>이너조인·아우터 조인의 테이블 결합 방향 차이를 알게 됨</li>
 <li>
  <a href="https://github.com/kimtaehyun304/tama-api/blob/master/src/main/java/org/example/tamaapi/repository/item/query/ItemQueryRepository.java#L116">
  동적 쿼리를 한눈에 볼 수 있게 queryDsl 사용
  </a>
 </li>
 
</ul>

aws 청구 요금 줄이기
<ul>
 <li>저장소 요금을 줄이기위해 CloudWatch로 수집한 로그를 주기적으로 S3로 옮김</li>
 <li>이미지 조회 요금을 줄이기 위해 S3 앞에 cloudFront 배치</li>
 <li>네트워크 요금을 줄이기 위해 ec2·rds 가용 영역 일치 시킴 </li>
 <li>네트워크 요금을 줄이기 위해 select절 필드를 최소화</li>
</ul>

https 인증서 자동 갱신 (Let`s Encrypt)
<ul>
  <li>certbot 타이머로 인증서 자동 갱신</li>
   <ul>
      <li>갱신 중 서비스가 중단되지 않게 하기 위해 인증 방식을 standalone → webroot로 변경</li>
      <li>webroot로 변경하기 위해 nginx 추가</li>
   </ul>
  <li>certbot reload hook으로 새로운 인증서 자동 적용</li>
   <ul>
      <li>certbot reload hook은 nginx를 재시작하는 기능</li>
   </ul>
</ul>

결제·주문 API
<ul>
 <li>토스페이먼츠·카카오페이·카드 등 PG사 이용</li>
 <li>결제가 올바로 됐는지 확인하고 주문 API 진행시킴</li>
 <li>pc·모바일 따로 주문 API 개발</li>
</ul>

기타
<ul>
 <li>빠른 로컬 개발을 위해 in-memory-db(h2) 사용</li>
 <li>로컬·배포 환경을 스위칭하기 위해 application.yml·application-prod.yml 사용</li>
 <li>코드 간소화를 위해 AOP 어노테이션으로 유저 권한 조회</li>
 <li>스프링 시큐리티 인증을 커스텀하기 위해 @AuthenticationPrincipal 사용</li>
 <li>소셜·일반 회원가입 중복 계정인지 검증</li>
</ul>

<h1>erd</h1>
<p align="center">
<img src="https://github.com/user-attachments/assets/69455699-3fa4-4dd0-9ee9-ce8ea3284cd4"/>
</p>
