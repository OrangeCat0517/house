package com.example.house.service.house;

import com.example.house.base.ServiceMultiResult;
import com.example.house.base.ServiceResult;
import com.example.house.dto.HouseDTO;
import com.example.house.dto.HouseSubscribeDTO;
import com.example.house.form.DatatableSearch;
import com.example.house.form.HouseForm;
import com.example.house.form.MapSearch;
import com.example.house.form.RentSearch;

import java.util.Date;

public interface IHouseService {

    ServiceResult<HouseDTO> save(HouseForm houseForm);

    ServiceResult update(HouseForm houseForm);

    ServiceMultiResult<HouseDTO> adminQuery(DatatableSearch searchBody);

    ServiceResult<HouseDTO> findCompleteOne(Long id);

    ServiceResult removePhoto(Long id);

    ServiceResult updateCover(Long coverId, Long targetId);

    ServiceResult addTag(Long houseId, String tag);

    ServiceResult removeTag(Long houseId, String tag);

    ServiceResult updateStatus(Long id, int status);

    ServiceMultiResult<HouseDTO> query(RentSearch rentSearch);
}
