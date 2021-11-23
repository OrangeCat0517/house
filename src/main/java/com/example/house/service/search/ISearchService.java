package com.example.house.service.search;

/**
 * 检索接口
 */
public interface ISearchService {
    /**
     * 索引目标房源
     */
    void index(Long houseId);

    /**
     * 移除房源索引
     */
    void remove(Long houseId);

}
