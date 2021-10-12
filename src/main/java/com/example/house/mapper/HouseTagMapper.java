package com.example.house.mapper;

import com.example.house.domain.HouseTag;

import java.util.List;

public interface HouseTagMapper {
    HouseTag findByNameAndHouseId(String name, Long houseId);
    List<HouseTag> findAllByHouseId(Long id);
    List<HouseTag> findAllByHouseIdIn(List<Long> houseIds);
}
