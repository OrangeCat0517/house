package com.example.house.service.house;

import com.example.house.base.HouseSubscribeStatus;
import com.example.house.base.ServiceMultiResult;
import com.example.house.base.ServiceResult;
import com.example.house.dto.HouseDTO;
import com.example.house.dto.HouseSubscribeDTO;
import com.example.house.form.DatatableSearch;
import com.example.house.form.HouseForm;
import com.example.house.form.MapSearch;
import com.example.house.form.RentSearch;
import org.springframework.data.util.Pair;

import java.time.LocalDateTime;

public interface IHouseService {

    ServiceResult<HouseDTO> save(HouseForm houseForm);

    ServiceResult update(HouseForm houseForm);

    ServiceResult<HouseDTO> findCompleteOne(Long id);

    ServiceResult removePhoto(Long id);

    ServiceResult updateCover(Long coverId, Long targetId);

    ServiceResult addTag(Long houseId, String tag);

    ServiceResult removeTag(Long houseId, String tag);

    ServiceResult updateStatus(Long id, int status);

    ServiceMultiResult<HouseDTO> adminQuery(DatatableSearch searchBody);

    ServiceMultiResult<HouseDTO> query(RentSearch rentSearch);





















    /**
     * 全地图查询
     * @param mapSearch
     * @return
     */
    ServiceMultiResult<HouseDTO> wholeMapQuery(MapSearch mapSearch);

    /**
     * 精确范围数据查询
     * @param mapSearch
     * @return
     */
    ServiceMultiResult<HouseDTO> boundMapQuery(MapSearch mapSearch);

    /**
     * 加入预约清单
     * @param houseId
     * @return
     */
    ServiceResult addSubscribeOrder(Long houseId);

    /**
     * 获取对应状态的预约列表
     */
    ServiceMultiResult<Pair<HouseDTO, HouseSubscribeDTO>> querySubscribeList(HouseSubscribeStatus status, int start, int size);

    /**
     * 预约看房时间
     * @param houseId
     * @param orderTime
     * @param telephone
     * @param desc
     * @return
     */
    ServiceResult subscribe(Long houseId, LocalDateTime orderTime, String telephone, String desc);

    /**
     * 取消预约
     * @param houseId
     * @return
     */
    ServiceResult cancelSubscribe(Long houseId);

    /**
     * 管理员查询预约信息接口
     * @param start
     * @param size
     */
    ServiceMultiResult<Pair<HouseDTO, HouseSubscribeDTO>> findSubscribeList(int start, int size);
}
