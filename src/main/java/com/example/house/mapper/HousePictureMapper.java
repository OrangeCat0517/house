package com.example.house.mapper;

import java.util.List;

import com.example.house.domain.HousePicture;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HousePictureMapper {
    List<HousePicture> findAllByHouseId(Long id);
    int save(List<HousePicture> housePictures);
}
