package com.example.house.mapper;

import com.example.house.domain.House;
import com.example.house.form.DatatableSearch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HouseMapper {
    void updateCover(@Param(value = "id") Long id, @Param(value = "cover") String cover);

    void updateStatus(@Param(value = "id") Long id, @Param(value = "status") int status);

    void updateWatchTimes(@Param(value = "id") Long houseId);

    void save(House house);

    House findOne(Long id);

    List<House> findAll();

    List<House> findbyDatatableSearch(DatatableSearch searchBody);
}
