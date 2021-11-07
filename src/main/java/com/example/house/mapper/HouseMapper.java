package com.example.house.mapper;

import com.example.house.domain.House;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface HouseMapper {
    void updateCover(@Param(value = "id") Long id, @Param(value = "cover") String cover);

    void updateStatus(@Param(value = "id") Long id, @Param(value = "status") int status);

    void updateWatchTimes(@Param(value = "id") Long houseId);

    void save(House house);

    House findOne(Long id);
}
