package org.example.tamaapi.dto.requestDto.member;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UpdateMemberDefaultAddressRequest {

    @NotNull
    private Long addressId;

}
