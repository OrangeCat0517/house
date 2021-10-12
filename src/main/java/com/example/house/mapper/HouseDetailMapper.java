package com.example.house.mapper;

import java.util.List;

import com.example.house.domain.HouseDetail;

public interface HouseDetailMapper {
    HouseDetail findByHouseId(Long houseId);
    List<HouseDetail> findAllByHouseIdIn(List<Long> houseIds);
}
