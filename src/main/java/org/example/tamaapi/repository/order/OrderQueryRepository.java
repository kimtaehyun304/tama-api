package org.example.tamaapi.repository.order;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.example.tamaapi.dto.responseDto.order.AdminOrderResponse;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderQueryRepository {

    private final JPAQueryFactory queryFactory;

    public OrderQueryRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }




}
