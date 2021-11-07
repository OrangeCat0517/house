package com.example.house.domain;

import lombok.Data;

@Data
public class HouseTag {
    private Long id;
    private Long houseId;
    private String name;

    public HouseTag(Long houseId, String name) {
        this.houseId = houseId;
        this.name = name;
    }

}
