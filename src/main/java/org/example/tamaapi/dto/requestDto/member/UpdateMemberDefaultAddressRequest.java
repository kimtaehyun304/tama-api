package org.example.tamaapi.dto.requestDto.member;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMemberDefaultAddressRequest {

    @NotNull
    private Long addressId;

}
