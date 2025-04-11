package org.example.tamaapi.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//내 주소 설정
@Entity
@Getter @Setter
@Table(name = "delvery_address", uniqueConstraints = {
        @UniqueConstraint(name = "member_zipcode_street_detail_unique",columnNames = {"member_id","zipCode", "street", "detail"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberAddress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_address_id")
    private Long id;

    //ex)우리집
    private String name;

    private String receiverNickName;

    private String receiverPhone;

    // 우편번호
    private String zipCode;

    // 도로명 주소
    private String street;

    // 상세 주소
    private String detail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private boolean isDefault;

    public MemberAddress(String name, String receiverNickName, String receiverPhone, String zipCode, String street, String detail, Member member, Boolean isDefault) {
        this.name = name;
        this.receiverNickName = receiverNickName;
        this.receiverPhone = receiverPhone;
        this.zipCode = zipCode;
        this.street = street;
        this.detail = detail;
        this.member = member;
        this.isDefault = isDefault;
    }

    public void updateIsDefault(Boolean isDefault){
        this.isDefault = isDefault;
    }
}


