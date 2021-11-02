package com.example.house.mapper;


import com.example.house.domain.SupportAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.parameters.P;

import java.util.List;

@Mapper
public interface SupportAddressMapper {
    List<SupportAddress> findAllByLevel(String level);
    SupportAddress findByEnNameAndLevel(@Param("enName") String enName, @Param("level")String level);
    SupportAddress findByEnNameAndBelongTo(@Param("enName")String enName, @Param("belongTo")String belongTo);
    List<SupportAddress> findAllByLevelAndBelongTo(@Param("level")String level, @Param("belongTo")String belongTo);
}
