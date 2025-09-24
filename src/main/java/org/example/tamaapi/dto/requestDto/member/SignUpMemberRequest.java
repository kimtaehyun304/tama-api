package org.example.tamaapi.dto.requestDto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
