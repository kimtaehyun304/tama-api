package org.example.tamaapi.cache;

import lombok.Getter;

@Getter
public enum MyCacheType {

    TOKEN("token", 60*3, 10000),
    AUTHSTRING("AUTHSTRING", 60*3, 10000);

    private final String name;
    private final int expireAfterWrite;
    private final int maximumSize;

    MyCacheType(String name, int expireAfterWrite, int maximumSize) {
        this.name = name;
        this.expireAfterWrite = expireAfterWrite;
        this.maximumSize = maximumSize;
    }

    /*
    static class ConstConfig {
        static final int DEFAULT_TTL_SEC = 180;
        static final int DEFAULT_MAX_SIZE = 10000;
    }
    */
}
