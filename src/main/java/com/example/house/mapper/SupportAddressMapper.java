package com.example.house.mapper;


import com.example.house.domain.SupportAddress;

import java.util.List;

public interface SupportAddressMapper {
    List<SupportAddress> findAllByLevel(String level);
    SupportAddress findByEnNameAndLevel(String enName, String level);
    SupportAddress findByEnNameAndBelongTo(String enName, String belongTo);
    List<SupportAddress> findAllByLevelAndBelongTo(String level, String belongTo);
}
