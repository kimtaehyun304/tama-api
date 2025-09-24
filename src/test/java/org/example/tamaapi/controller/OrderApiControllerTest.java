package org.example.tamaapi.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
// 테스트 끝나면 롤백 (auto_increment는 롤백 안됨)
@Transactional
class OrderApiControllerTest {

    //브라우저에서 클릭 방식으로만 결제 가능 -> @BeforeEach로 결제 생성 못함
    @Test
    void saveMemberOrder() {

    }

    @Test
    void cancelMemberOrder() {
    }

    @Test
    void saveGuestOrder() {
    }

    @Test
    void cancelGuestOrder() {
    }
}