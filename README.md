<h1>쇼핑몰 1인 개발 / 2024.12 ~ </h1>
<p align="center">
<img src="https://github.com/user-attachments/assets/b160556b-07c2-4af6-a10c-65b0fb57e5c1" width="80%" height="80%"/>
</p>
<p align="center">초기엔 ELB와 ACM을 썼지만, 비용 문제로 지우고 Certbot으로 전환</p>


프론트 주소  
[https://dlta.kr](https://dlta.kr/)  

API 서버 주소  
[https://dldm.kr](https://dldm.kr/)  

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
