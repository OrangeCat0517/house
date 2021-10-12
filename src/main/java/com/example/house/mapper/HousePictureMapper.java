package com.example.house.mapper;

import java.util.List;

import com.example.house.domain.HousePicture;

public interface HousePictureMapper {
    List<HousePicture> findAllByHouseId(Long id);
}
