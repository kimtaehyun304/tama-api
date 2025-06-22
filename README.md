<h1>쇼핑몰 1인 개발 / 2024.12 ~ </h1>
<p align="center">
<img src="https://github.com/user-attachments/assets/b160556b-07c2-4af6-a10c-65b0fb57e5c1" width="80%" height="80%"/>
</p>
<p align="center">초기엔 ELB와 ACM을 썼지만, 비용 문제로 지우고 Certbot으로 전환</p>

[프론트 주소(https://dlta.kr)](https://dlta.kr/)  & [API 서버 주소(https://dldm.kr)](https://dldm.kr/)  

### 어필
[스프링 시큐리티 oAuth2 경험](https://github.com/kimtaehyun304/tama-api/blob/7a61031cad7f6025516b17acbbbea24d252165f0/src/main/java/org/example/tamaapi/config/oauth2/OAuth2UserCustomService.java#L33)
<ul>
  <li>oAuth2는 인증만 하는거라 로그인, 회원가입 구분 불가 <br>
    → 이미 가입돼있다고 예외 던지면 로그인을 의도한 경우에도 똑같은 예외가 발생 <br>
    → 그래서 Provider가 local이 아니라면 예외 없이 통과시켜야 함<br>
    → local은 로그인, 회원가입 api 따로 있어서 괜찮음
  </li>
    <li>oAuth2 예외는 공통 예외 처리 불가 → OAuth2FailureHandler 사용</li>
</ul>

CORS
<ul>
  <li>CORS 필터 → @Bean UrlBasedCorsConfigurationSource으로 바꿈 (cors 맞춤이라 더 간단)</li>
</ul>

[스프링 컨버터](https://github.com/kimtaehyun304/tama-api/blob/7a61031cad7f6025516b17acbbbea24d252165f0/src/main/java/org/example/tamaapi/config/WebConfig.java#L14)
<ul>
  <li>정렬을 ?sort=price,asc 이렇게 data jpa처럼 받으려고 컨버터 만듬</li>
  <li>@RequestParma으로 받고 컨버터 연결되게 했음</li>
  <li>@RequestParma은 @Valid 안되서 손수 검증함</li>
</ul>

[ItemQueryRepository](https://github.com/kimtaehyun304/tama-api/blob/9116c6e2d4c3ca8d2b05187e606c715407804c04/src/main/java/org/example/tamaapi/repository/item/query/ItemQueryRepository.java#L54)

<ul>
  <li>상품 조회(페이징 & 동적 쿼리)</li>
  <li>인기 상품 조회(주문 많은순 정렬 & 페이징)</li>
</ul>

[OrderQueryRepository](https://github.com/kimtaehyun304/tama-api/blob/9116c6e2d4c3ca8d2b05187e606c715407804c04/src/main/java/org/example/tamaapi/repository/order/query/OrderQueryRepository.java#L42)
<ul>
  <li>주문 조회 (페이징 & 동적 쿼리)</li>
</ul>




<h1>tama-api erd</h1>
<p align="center">
<img src="https://github.com/user-attachments/assets/69455699-3fa4-4dd0-9ee9-ce8ea3284cd4" width="90%" height="90%"/>
</p>
