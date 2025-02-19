package org.example.tamaapi.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "guest", uniqueConstraints = {
        @UniqueConstraint(name = "nickname_email_phone_unique",columnNames = {"nickname", "email", "phone"})
})
@Getter
public class Guest {

    @Id
    @Column(name = "guest_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nickname;
    private String email;
    private String phone;

}
