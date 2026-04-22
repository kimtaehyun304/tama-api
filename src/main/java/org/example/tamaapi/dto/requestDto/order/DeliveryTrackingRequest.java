package org.example.tamaapi.dto.requestDto.order;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tamaapi.domain.order.Courier;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryTrackingRequest {

    @NotNull
    private Courier courier;

    @NotNull
    private String trackingNumber;

}