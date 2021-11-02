package com.example.house.mapper;

import java.util.List;

import com.example.house.domain.HouseDetail;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HouseDetailMapper {
    HouseDetail findByHouseId(Long houseId);
    List<HouseDetail> findAllByHouseIdIn(List<Long> houseIds);
}
