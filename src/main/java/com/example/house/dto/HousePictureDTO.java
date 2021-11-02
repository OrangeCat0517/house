package com.example.house.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class HousePictureDTO {
    private Long id;
    @JsonProperty(value = "house_id")
    private Long houseId;
    private String path;
    @JsonProperty(value = "cdn_prefix")
    private String cdnPrefix;
    private int width;
    private int height;

    @Override
    public String toString() {
        return "HousePictureDTO{" +
                "id=" + id +
                ", houseId=" + houseId +
                ", path='" + path + '\'' +
                ", cdnPrefix='" + cdnPrefix + '\'' +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
