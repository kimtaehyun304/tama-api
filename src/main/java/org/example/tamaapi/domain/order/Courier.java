package org.example.tamaapi.domain.order;

import lombok.Getter;

@Getter
public enum Courier {

    POST_OFFICE("01", "우체국택배"),
    CJ_DAEHAN("04", "CJ대한통운"),
    HANJIN("05", "한진택배"),
    LOGEN("06", "로젠택배"),
    LOTTE("08", "롯데택배");

    private final String code;
    private final String kor;

    Courier(String code, String kor) {
        this.code = code;
        this.kor = kor;
    }

}
