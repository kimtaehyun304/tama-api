<h1>쇼핑몰 1인 개발 / 2024.12 ~ </h1>

#### 인프라
<p align="center">
<img src="https://github.com/user-attachments/assets/cd62fb35-4615-4a69-bf6a-197f93bc2a7e"/>
</p>
<p align="center">현재는 비용 때문에 ELB 지움</p>

<p align="center">
<img src="https://github.com/user-attachments/assets/831eec69-3ccc-462a-97be-e23b4810b380"/>
</p>

<p>https://dlta.kr</p>

#### 프로젝트 스킬 (스프링)
boot, mvc, valid, security, cache

#### 기능
상품 API
<ul>
 <li>검색 상품 조회</li>
 <li>검색 상품 조회</li>
 <li>인기 상품 조회</li>
 <li>장바구니 상품 조회</li>
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
  <li>로그인·회원가입 (oauth2, jwt)</li>
  <li>인증 문자 이메일 전송</li>
  <li>관리자 확인</li>
</ul>


### 어필
[oAuth2 회원가입 이슈](https://github.com/kimtaehyun304/tama-api/blob/7a61031cad7f6025516b17acbbbea24d252165f0/src/main/java/org/example/tamaapi/config/oauth2/OAuth2UserCustomService.java#L33)
<ul>
  <li>
    oAuth2는 인증만 하기 때문에 로그인, 회원가입을 구분할 수 없음 <br>
    → 이미 회원가입 했다고 예외 발생시키면, 로그인할 때도 같은 예외가 발생 <br>
    → 따라서 자사 계정으로 이미 가입된 경우에만 예외 발생 시키기 (로그인 API 따로 있어서 괜찮)<br>
    → 소셜 계정으로 이미 가입된 경우라면 그냥 로그인으로 넘기기
  </li>
  <li>oAuth2 예외는 공통 예외 처리 불가 → OAuth2FailureHandler 사용</li>
  <li>OAuth2FailureHandler는 js alert로 메시지 확인하게 하고 window.location.href </li>
</ul>

[스프링 컨버터](https://github.com/kimtaehyun304/tama-api/blob/7a61031cad7f6025516b17acbbbea24d252165f0/src/main/java/org/example/tamaapi/config/WebConfig.java#L14)
<ul>
  <li>정렬을 ?sort=price,asc 이렇게 요청하게하려고 @RequestPartm 컨버터 만듬 </li>
  <li>@RequestParam 컨버터는 CustomSort 객체와 바인딩</li>
  <li>@RequestParma은 @Valid 안되서 손수 검증함</li>
</ul>

#### Certbot 인증서 갱신 이슈
<ul>
  <li>80포트 포워딩 중이라 Certbot 갱신 실패 → Certbot 요청만 포워딩 제외하도록 수정</li>
  <li>standalone 방식(갱신 중 어플리케이션 중단) → webroot 방식으로 변경(nginx 도입)</li>
  <li>iptables → nginx 포워딩으로 변경(더 간단)</li>
  <li>Certbot은 3개월 주기로 타이머를 통해 자동 갱신</li>
  <li>갱신 반영 위해 nginx 재시작 필요 → 타이머에 reload hook 추가</li>
</ul>

#### CORS
<ul>
  <li>CORS 필터 → @Bean UrlBasedCorsConfigurationSource으로 바꿈 (이게 더 간단)</li>
  <li>CORS는 브라우저에서 응답을 버리는거라, 서버에서 로직은 실행되니 주의</li>
</ul>

[ItemQueryRepository](https://github.com/kimtaehyun304/tama-api/blob/9116c6e2d4c3ca8d2b05187e606c715407804c04/src/main/java/org/example/tamaapi/repository/item/query/ItemQueryRepository.java#L54)

<ul>
  <li>상품 조회(페이징 & 동적 쿼리)</li>
  <li>인기 상품 조회(주문 많은순 정렬 & 페이징)</li>
</ul>

[OrderQueryRepository](https://github.com/kimtaehyun304/tama-api/blob/9116c6e2d4c3ca8d2b05187e606c715407804c04/src/main/java/org/example/tamaapi/repository/order/query/OrderQueryRepository.java#L42)
<ul>
  <li>마이 페이지 주문 조회 (페이징 & 동적 쿼리)</li>
  <li>이너 조인은 테이블이 중앙에서 만나는 방식이고, 아우터 조인은 한쪽으로 쏠리는 방식</li>
  <li>아우터 조인만 left, right 구분 가능</li>
</ul>

<h1>erd</h1>
<p align="center">
<img src="https://github.com/user-attachments/assets/69455699-3fa4-4dd0-9ee9-ce8ea3284cd4"/>
</p>
