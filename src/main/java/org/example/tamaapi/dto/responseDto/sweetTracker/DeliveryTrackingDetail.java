package org.example.tamaapi.dto.responseDto.sweetTracker;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class DeliveryTrackingDetail {

    private String code;
    private String kind;
    private int level;

    private String manName;
    private String manPic;

    private String remark;

    private String telno;
    private String telno2;

    private long time;
    private String timeString;

    private String where;

}
