package org.example.tamaapi.dto.requestDto.order;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
//로그 남기는 로직있어서 @ToString 사용
public class CancelMemberOrderRequest {

    @NotNull
    //클라이어언트에서 조작해서 보내도 상관x
    private Boolean isFreeOrder = false;

    private String reason = "구매자 취소 요청";

}
