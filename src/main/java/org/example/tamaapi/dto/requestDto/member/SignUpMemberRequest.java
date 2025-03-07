package org.example.tamaapi.dto.requestDto.member;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.example.tamaapi.domain.Gender;
import org.example.tamaapi.domain.Member;

@Getter
public class SignUpMemberRequest {

    @NotNull @Email
    private String email;

    @NotNull
    private String phone;

    @NotNull
    private String password;

    @NotNull
    private String authString;

    @NotNull
    private String nickname;

    //Gender gender;

    //Integer height;

    //Integer weight;

}
