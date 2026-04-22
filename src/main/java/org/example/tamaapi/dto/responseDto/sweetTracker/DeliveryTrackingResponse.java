package org.example.tamaapi.dto.responseDto.sweetTracker;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class DeliveryTrackingResponse {

    //커스텀 필드
    private String courierName;

    private String adUrl;
    private boolean complete;
    private String completeYN;
    private String estimate;

    private DeliveryTrackingDetail firstDetail;
    private DeliveryTrackingDetail lastDetail;
    private DeliveryTrackingDetail lastStateDetail;

    private String invoiceNo;
    private String itemImage;
    private String itemName;

    private int level;
    private String orderNumber;
    private String productInfo;

    private String receiverAddr;
    private String receiverName;
    private String recipient;

    private String result;
    private String senderName;

    private List<DeliveryTrackingDetail> trackingDetails;

    private String zipCode;

}
