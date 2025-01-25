package org.example.tamaapi.dto.requestDto;

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
@AllArgsConstructor
public class SignUpMemberRequest {

    @NotNull @Email
    String email;

    @NotNull
    String password;

    @NotNull
    String authString;

    //Gender gender;

    //Integer height;

    //Integer weight;

}
