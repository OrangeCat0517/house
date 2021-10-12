package com.example.house.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class House {
    private Long id;
    private String title;
    private Long adminId;
    private Integer price;
    private Integer area;
    private Integer room;
    private Integer parlour;
    private Integer bathroom;
    private Integer floor;
    private Integer totalFloor;
    private Integer watchTimes;
    private Integer buildYear;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime lastUpdateTime;
    private String cityEnName;
    private String regionEnName;
    private String street;
    private String district;
    private Integer direction;
    private String cover;
    private Integer distanceToSubway;
}
