package com.example.house.service.search;

import com.example.house.base.ServiceMultiResult;
import com.example.house.base.ServiceResult;
import com.example.house.form.RentSearch;

import java.util.List;

/**
 * 检索接口，这个检索指的是在ES中检索
 */
public interface ISearchService {
    /**
     * 索引目标房源, 这个方法就是将mysql中的房源数据索引到es的里面
     */
    void index(Long houseId); //save to es

    /**
     * 移除房源索引
     */
    void remove(Long houseId);


    ServiceMultiResult<Long> query(RentSearch rentSearch);


//    ServiceResult<List<String>> suggest(String prefix);
//    ServiceResult<Long> aggregateDistrictHouse(String cityEnName, String regionEnName, String district);
}
