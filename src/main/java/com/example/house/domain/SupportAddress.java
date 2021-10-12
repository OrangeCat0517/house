package com.example.house.domain;


import lombok.Data;

@Data
public class SupportAddress {
    private Long id;
    private String belongTo;
    private String enName;
    private String cnName;
    private String level;
    private Double baiduMapLongitude;
    private Double baiduMapLatitude;
    public enum Level {
        CITY("city"),
        REGION("region");

        private String value;

        Level(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Level of(String value) {
            for (Level level : Level.values()) {
                if (level.getValue().equals(value)) {
                    return level;
                }
            }
            throw new IllegalArgumentException();
        }
    }
}
