package com.example.house.mapper;


import com.example.house.domain.SubwayStation;

import java.util.List;

public interface SubwayStationMapper {
    List<SubwayStation> findAllBySubwayId(Long subwayId);
}
