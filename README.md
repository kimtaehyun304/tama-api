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

### 프로젝트로 얻은 경험

상품 검색 쿼리 속도 개선
 <ul>
  <li>로컬db 상품 row 십만개 넣고 진행</li>
  <a href="https://github.com/kimtaehyun304/tama-api/blob/7a5a44d62ad6b30551c4ee44c4728ddc22c83bfd/src/main/java/org/example/tamaapi/repository/item/query/ItemQueryRepository.java#L72">
   <li>row 중복 제거 방법 변경 (groupBy or distinct 0.8s → exists 0s)</li>
  </a>
  <ul>
   <li>인덱스 적용하려고 order by 필드 변경 (created_at → item.id)</li>
   <li>인덱스 적용하려고 함수 제거·order by 필드 변경</li>
   <li>ex) colasecse(disconted_price, price) → now_price</li>
  </ul>
  <a href="https://github.com/kimtaehyun304/tama-api/blob/cb50646c2ef04d401ab52845a18e1406d1cf00ed/src/main/java/org/example/tamaapi/repository/item/query/ItemQueryRepository.java#L93">
  </a>
</ul>

연관관계(1:N - 1:N) 조인 노하우 터득
 <ul>
  <li>ex) 상품 공통 정보 -&lt; 색상 -&lt; 사이즈·재고</li>
  <li>쿼리 여러번 나눠서 하기</li>
  <li>조인·groupBy 또는 서브쿼리 (성능 테스트 필요)</li>
  <li>이너·아우터 조인의 테이블 결합 차이를 알게 됨</li>
 </ul>
 
<a href="https://github.com/kimtaehyun304/tama-api/blob/6ba8cf6e1f71c04aef0b6cc8f0fe36355cf7788a/src/main/java/org/example/tamaapi/service/ItemService.java#L27"> 
 상품 저장 쿼리 개선 (insert 쿼리 수 ↓)
</a>
 <ul>
  <li>data jpa saveAll() → jdbcTemplate bulk insert</li>
  <li>bulk insert하면 객체에 pk가 없음 → db 조회하여 할당</li>
  <li>p.s) 참조 객체의 외래키 컬럼을 채우기위해 pk 할당</li>
 </ul>

기타
<ul>
 <li>
  <a href="https://github.com/kimtaehyun304/tama-api/blob/5a0433c9634e03ac5d25a37ba15553a9f8042b8d/src/main/java/org/example/tamaapi/config/aspect/PreAuthenticationAspect.java#L36">
   코드 간소화를 위해 AOP 어노테이션으로 유저 권한 조회
  </a>
 </li>
 <li>스프링 시큐리티 인증을 커스텀하기 위해 @AuthenticationPrincipal 사용</li>
 <li>빠른 로컬 개발을 위해 in-memory-db(h2) 사용</li>
 <li>로컬·배포 환경을 스위칭하기 위해 application.yml·application-prod.yml 사용</li>
 <li>동적 쿼리를 한눈에 볼 수 있게 queryDsl 사용</li>
</ul>

인프라

https 인증서 자동 갱신 (Let`s Encrypt)
1) 인증서 무중단 자동 갱신 (cerbot 타이머) <br>
   <ul>
      <li>갱신 중 서비스가 중단되지 않게 하기 위해 인증 방식을 standalone → webroot로 변경</li>
      <li>webroot로 변경하기 위해 nginx 추가</li>
   </ul>
2) 새로운 인증서 자동 적용 (certbot reload hook)<br>
   <ul>
      <li>certbot reload hook은 nginx를 재시작하는 기능</li>
   </ul>

aws 청구 요금 줄이기
<ul>
 <li>저장소 요금을 줄이기위해 CloudWatch로 수집한 로그를 주기적으로 S3로 옮김</li>
 <li>이미지 조회 요금을 줄이기 위해 S3 앞에 cloudFront 배치</li>
 <li>네트워크 요금을 줄이기 위해 ec2·rds 가용 영역 일치 시킴 & select절 필드 최소화 </li>
</ul>




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
 <li>상품 주문 (포트원 연동) (pc·모바일 API 분리)</li>
 <li>자주 쓰는 배송지 조회·등록</li>
</ul>

인증 API
<ul>
  <li>로그인·회원가입 (oauth2·jwt)</li>
  <li>회원가입에 필요한 인증 문자를 이메일로 전송</li>
  <li>관리자 확인</li>
</ul>


<h1>erd</h1>
<p align="center">
<img src="https://github.com/user-attachments/assets/69455699-3fa4-4dd0-9ee9-ce8ea3284cd4"/>
</p>
