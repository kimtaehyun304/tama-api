package org.example.tamaapi.dto.requestDto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

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
