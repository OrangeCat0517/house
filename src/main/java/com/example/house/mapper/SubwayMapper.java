package com.example.house.mapper;

import java.util.List;

import com.example.house.domain.Subway;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SubwayMapper {
    List<Subway> findAllByCityEnName(String cityEnName);
    Subway findOne(Long subwayId);
}
