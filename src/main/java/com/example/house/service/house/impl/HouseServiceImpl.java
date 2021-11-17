package com.example.house.service.house.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;


import com.example.house.base.HouseStatus;
import com.example.house.base.HouseSubscribeStatus;
import com.example.house.base.ServiceMultiResult;
import com.example.house.base.ServiceResult;
import com.example.house.domain.*;
import com.example.house.dto.HouseDTO;
import com.example.house.dto.HouseDetailDTO;
import com.example.house.dto.HousePictureDTO;
import com.example.house.dto.HouseSubscribeDTO;
import com.example.house.form.*;
import com.example.house.mapper.*;
import com.example.house.service.house.IQiNiuService;
import com.example.house.service.house.IHouseService;
import com.example.house.util.LoginUserUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.data.domain.*;
import org.springframework.data.util.Pair;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HouseServiceImpl implements IHouseService {
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private HouseMapper houseMapper;

    @Autowired
    private HouseDetailMapper houseDetailMapper;

    @Autowired
    private HousePictureMapper housePictureMapper;

    @Autowired
    private HouseTagMapper houseTagMapper;

    @Autowired
    private SubwayMapper subwayMapper;

    @Autowired
    private SubwayStationMapper subwayStationMapper;

    @Autowired
    private IQiNiuService qiNiuService;

    @Value("${qiniu.cdn.prefix}")
    private String cdnPrefix;


    @Override
    @Transactional
    public ServiceResult<HouseDTO> save(HouseForm houseForm) {
        System.out.println("house form:" + houseForm);
        HouseDetail detail = new HouseDetail();
        ServiceResult<HouseDTO> subwayValidationResult
                = wrapperDetailInfo(detail, houseForm);
        if (subwayValidationResult != null) {
            return subwayValidationResult;
        }

        House house = new House();
        modelMapper.map(houseForm, house);

        LocalDateTime now = LocalDateTime.now();
        house.setCreateTime(now);
        house.setLastUpdateTime(now);
        house.setAdminId(LoginUserUtil.getLoginUserId());
        houseMapper.save(house);

        detail.setHouseId(house.getId());
        houseDetailMapper.save(detail);

        List<HousePicture> pictures = generatePictures(houseForm, house.getId());
        housePictureMapper.save(pictures); //注意mysql batch

        HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);
        HouseDetailDTO houseDetailDTO = modelMapper.map(detail, HouseDetailDTO.class);

        houseDTO.setHouseDetail(houseDetailDTO);

        List<HousePictureDTO> pictureDTOS = new ArrayList<>();
        pictures.forEach(housePicture -> pictureDTOS.add(modelMapper.map(housePicture, HousePictureDTO.class)));
        houseDTO.setPictures(pictureDTOS);
        houseDTO.setCover(this.cdnPrefix + houseDTO.getCover());

        List<String> tags = houseForm.getTags();
        if (tags != null && !tags.isEmpty()) {
            List<HouseTag> houseTags = new ArrayList<>();
            for (String tag : tags) {
                houseTags.add(new HouseTag(house.getId(), tag));
            }
            houseTagMapper.save(houseTags);
            houseDTO.setTags(tags);
        }

        return ServiceResult.success(null, houseDTO);
    }

    @Override
    public ServiceResult update(HouseForm houseForm) {
        House house = this.houseMapper.findOne(houseForm.getId());
        if (house == null) {
            return ServiceResult.notFound();
        }

        HouseDetail detail = this.houseDetailMapper.findByHouseId(house.getId());
        if (detail == null) {
            return ServiceResult.notFound();
        }

        ServiceResult wrapperResult = wrapperDetailInfo(detail, houseForm);
        if (wrapperResult != null) {
            return wrapperResult;
        }

        houseDetailMapper.save(detail);

        List<HousePicture> pictures = generatePictures(houseForm, houseForm.getId());
        housePictureMapper.save(pictures);

        if (houseForm.getCover() == null) {
            houseForm.setCover(house.getCover());
        }

        modelMapper.map(houseForm, house);
        house.setLastUpdateTime(LocalDateTime.now());
        houseMapper.save(house);

        return ServiceResult.success();
    }

    @Override
    public ServiceResult<HouseDTO> findCompleteOne(Long id) {
        House house = houseMapper.findOne(id);
        if (house == null) {
            return ServiceResult.notFound();
        }

        HouseDetail detail = houseDetailMapper.findByHouseId(id);
        List<HousePicture> pictures = housePictureMapper.findAllByHouseId(id);

        HouseDetailDTO detailDTO = modelMapper.map(detail, HouseDetailDTO.class);
        List<HousePictureDTO> pictureDTOS = new ArrayList<>();
        for (HousePicture picture : pictures) {
            HousePictureDTO pictureDTO = modelMapper.map(picture, HousePictureDTO.class);
            pictureDTOS.add(pictureDTO);
        }


        List<HouseTag> tags = houseTagMapper.findAllByHouseId(id);
        List<String> tagList = new ArrayList<>();
        for (HouseTag tag : tags) {
            tagList.add(tag.getName());
        }

        HouseDTO result = modelMapper.map(house, HouseDTO.class);
        result.setHouseDetail(detailDTO);
        result.setPictures(pictureDTOS);
        result.setTags(tagList);

        return ServiceResult.of(result);
    }

    @Override
    public ServiceResult removePhoto(Long id) {
        HousePicture picture = housePictureMapper.findOne(id);
        if (picture == null) {
            return ServiceResult.notFound();
        }

        try {
            Response response = this.qiNiuService.delete(picture.getPath());
            if (response.isOK()) {
                housePictureMapper.delete(id);
                return ServiceResult.success();
            } else {
                return new ServiceResult(false, response.error);
            }
        } catch (QiniuException e) {
            e.printStackTrace();
            return new ServiceResult(false, e.getMessage());
        }
    }

    @Override
    public ServiceResult updateCover(Long coverId, Long targetId) {
        HousePicture cover = housePictureMapper.findOne(coverId);
        if (cover == null) {
            return ServiceResult.notFound();
        }

        houseMapper.updateCover(targetId, cover.getPath());
        return ServiceResult.success();
    }

    @Override
    public ServiceResult addTag(Long houseId, String tag) {
        House house = houseMapper.findOne(houseId);
        if (house == null) {
            return ServiceResult.notFound();
        }

        HouseTag houseTag = houseTagMapper.findByNameAndHouseId(tag, houseId);
        if (houseTag != null) {
            return new ServiceResult(false, "标签已存在");
        }
        houseTagMapper.save(List.of(new HouseTag(houseId, tag)));
        return ServiceResult.success();
    }

    @Override
    public ServiceResult removeTag(Long houseId, String tag) {
        House house = houseMapper.findOne(houseId);
        if (house == null) {
            return ServiceResult.notFound();
        }

        HouseTag houseTag = houseTagMapper.findByNameAndHouseId(tag, houseId);
        if (houseTag == null) {
            return new ServiceResult(false, "标签不存在");
        }

        houseTagMapper.delete(houseTag.getId());
        return ServiceResult.success();
    }

    @Override
    public ServiceResult updateStatus(Long id, int status) {
        House house = houseMapper.findOne(id);
        if (house == null) {
            return ServiceResult.notFound();
        }

        if (house.getStatus() == status) {
            return new ServiceResult(false, "状态没有发生变化");
        }

        if (house.getStatus() == HouseStatus.RENTED.getValue()) {
            return new ServiceResult(false, "已出租的房源不允许修改状态");
        }

        if (house.getStatus() == HouseStatus.DELETED.getValue()) {
            return new ServiceResult(false, "已删除的资源不允许操作");
        }

        houseMapper.updateStatus(id, status);
        return ServiceResult.success();
    }

    @Override
    public ServiceMultiResult<HouseDTO> adminQuery(DatatableSearch searchBody) {
        List<HouseDTO> houseDTOS = new ArrayList<>();
//        Sort sort = Sort.by(Sort.Direction.fromString(searchBody.getDirection()), searchBody.getOrderBy());
//        int page = searchBody.getStart() / searchBody.getLength();
//        Pageable pageable = PageRequest.of(page, searchBody.getLength(), sort);
        List<House> houses = houseMapper.findAll();
        houses.forEach(house -> {
            HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);
            houseDTO.setCover(this.cdnPrefix + house.getCover());
            houseDTOS.add(houseDTO);
        });

        return new ServiceMultiResult<>(houses.size(), houseDTOS);
    }

    @Override
    public ServiceMultiResult<HouseDTO> query(RentSearch rentSearch) {
        return null;
    }

    @Override
    public ServiceMultiResult<HouseDTO> wholeMapQuery(MapSearch mapSearch) {
        return null;
    }

    @Override
    public ServiceMultiResult<HouseDTO> boundMapQuery(MapSearch mapSearch) {
        return null;
    }

    @Override
    public ServiceResult addSubscribeOrder(Long houseId) {
        return null;
    }

    @Override
    public ServiceMultiResult<Pair<HouseDTO, HouseSubscribeDTO>> querySubscribeList(HouseSubscribeStatus status, int start, int size) {
        return null;
    }

    @Override
    public ServiceResult subscribe(Long houseId, LocalDateTime orderTime, String telephone, String desc) {
        return null;
    }

    @Override
    public ServiceResult cancelSubscribe(Long houseId) {
        return null;
    }

    @Override
    public ServiceMultiResult<Pair<HouseDTO, HouseSubscribeDTO>> findSubscribeList(int start, int size) {
        return null;
    }







    private void wrapperHouseList(List<Long> houseIds, Map<Long, HouseDTO> idToHouseMap) {
        List<HouseDetail> details = houseDetailMapper.findAllByHouseIdIn(houseIds);
        details.forEach(houseDetail -> {
            HouseDTO houseDTO = idToHouseMap.get(houseDetail.getHouseId());
            HouseDetailDTO detailDTO = modelMapper.map(houseDetail, HouseDetailDTO.class);
            houseDTO.setHouseDetail(detailDTO);
        });

        List<HouseTag> houseTags = houseTagMapper.findAllByHouseIdIn(houseIds);
        houseTags.forEach(houseTag -> {
            HouseDTO house = idToHouseMap.get(houseTag.getHouseId());
            house.getTags().add(houseTag.getName());
        });
    }

    private List<HousePicture> generatePictures(HouseForm form, Long houseId) {
        List<HousePicture> pictures = new ArrayList<>();
        if (form.getPhotos() == null || form.getPhotos().isEmpty()) {
            return pictures;
        }

        for (PhotoForm photoForm : form.getPhotos()) {
            HousePicture picture = new HousePicture();
            picture.setHouseId(houseId);
            picture.setCdnPrefix(cdnPrefix);
            picture.setPath(photoForm.getPath());
            picture.setWidth(photoForm.getWidth());
            picture.setHeight(photoForm.getHeight());
            pictures.add(picture);
        }
        return pictures;
    }


    private ServiceResult<HouseDTO> wrapperDetailInfo(HouseDetail houseDetail, HouseForm houseForm) {
        Subway subway = subwayMapper.findOne(houseForm.getSubwayLineId());
        System.out.println("test1.........................................");
        if (subway == null) {
            return ServiceResult.of(false, "Not valid subway line!");
        }
        System.out.println("test2.........................................");
        SubwayStation subwayStation = subwayStationMapper.findOne(houseForm.getSubwayStationId());
        if (subwayStation == null || subway.getId() != subwayStation.getSubwayId()) {
            return ServiceResult.of(false, "Not valid subway station!");
        }

        System.out.println("test3.........................................");
        houseDetail.setSubwayLineId(subway.getId());
        houseDetail.setSubwayLineName(subway.getName());

        houseDetail.setSubwayStationId(subwayStation.getId());
        houseDetail.setSubwayStationName(subwayStation.getName());

        houseDetail.setDescription(houseForm.getDescription());
        houseDetail.setDetailAddress(houseForm.getDetailAddress());
        houseDetail.setLayoutDesc(houseForm.getLayoutDesc());
        houseDetail.setRentWay(houseForm.getRentWay());
        houseDetail.setRoundService(houseForm.getRoundService());
        houseDetail.setTraffic(houseForm.getTraffic());
        return null;
    }
}
