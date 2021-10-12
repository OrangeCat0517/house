package com.example.house.mapper;

import com.example.house.domain.HouseSubscribe;
import org.apache.ibatis.annotations.Param;


public interface HouseSubscribeMapper {
    HouseSubscribe findByHouseIdAndUserId
            (Long houseId, Long loginUserId);
//    Page<HouseSubscribe> findAllByUserIdAndStatus
//            (Long userId, int status, Pageable pageable);
//    Page<HouseSubscribe> findAllByAdminIdAndStatus
//            (Long adminId, int status, Pageable pageable);
    HouseSubscribe findByHouseIdAndAdminId
            (Long houseId, Long adminId);
    void updateStatus(@Param(value = "id") Long id,
                      @Param(value = "status") int status);
}
