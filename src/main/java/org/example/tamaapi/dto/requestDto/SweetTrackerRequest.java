package org.example.tamaapi.dto.requestDto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SweetTrackerRequest {

    //택배사 코드
    //@NotNull
    private String courier;

    //운송장 번호
    //@NotNull
    private String trackingNumber;

}
