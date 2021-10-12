package com.example.house.mapper;

import java.util.List;

import com.example.house.domain.Subway;


public interface SubwayMapper {
    List<Subway> findAllByCityEnName(String cityEnName);
}
