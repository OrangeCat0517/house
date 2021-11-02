package com.example.house.mapper;


import com.example.house.domain.SubwayStation;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
@Mapper
public interface SubwayStationMapper {
    List<SubwayStation> findAllBySubwayId(Long subwayId);
    SubwayStation findOne(Long subwayId);
}
