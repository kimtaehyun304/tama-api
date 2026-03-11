package org.example.tamaapi.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tamaapi.domain.item.Category;
import org.example.tamaapi.domain.item.ColorItem;
import org.example.tamaapi.domain.user.Member;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Table
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomerSupport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_support_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    //비회원을 고려해 null 가능
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

}
