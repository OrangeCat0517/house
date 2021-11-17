package com.example.house;

import com.example.house.domain.HouseTag;
import com.example.house.form.HouseForm;
import com.example.house.form.PhotoForm;
import com.example.house.mapper.HouseTagMapper;
import com.example.house.service.house.IAddressService;
import com.example.house.service.house.IHouseService;
import com.example.house.service.house.IQiNiuService;
import com.example.house.service.house.impl.AddressServiceImpl;
import com.example.house.service.users.ISmsService;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class HouseApplicationTests {

    @Autowired
    private ISmsService iSmsService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private IAddressService addressService;
    @Autowired
    private IQiNiuService qiNiuService;
    @Autowired
    private IHouseService houseService;
    @Autowired
    private HouseTagMapper houseTagMapper;

    @Test
    void saveTags() {
        List<HouseTag> houseTags = new ArrayList<>();
        houseTags.add(new HouseTag(15L, "基金提供怒"));
        houseTags.add(new HouseTag(15L, "基金提供怒1"));
        houseTags.add(new HouseTag(15L, "基金提供怒2"));
        houseTags.forEach(System.out::println);
        houseTagMapper.save(houseTags);
        houseTags.forEach(System.out::println);
    }

    @Test
    void saveHouse() {
        HouseForm houseForm = new HouseForm();
        houseForm.setTitle("title");
        houseForm.setCityEnName("bj");
        houseForm.setRegionEnName("dcq");
        houseForm.setStreet("这是一个街道");
        houseForm.setDistrict("小区");
        houseForm.setDetailAddress("这是一个地址 chaoyangqu ");
        houseForm.setRoom(2);
        houseForm.setParlour(2);
        houseForm.setFloor(10);
        houseForm.setTotalFloor(30);
        houseForm.setDirection(1);
        houseForm.setBuildYear(2020);
        houseForm.setArea(100);
        houseForm.setPrice(1000000);
        houseForm.setRentWay(0);
        houseForm.setSubwayLineId(1L);
        houseForm.setSubwayStationId(5L);
        houseForm.setDistanceToSubway(1000);
        houseForm.setLayoutDesc("fdsfads");
        houseForm.setRoundService("houseForm");
        houseForm.setTraffic("fdsfdsa");
        houseForm.setDescription("fdsfdsadsa");
        houseForm.setCover("fdsa");
        //houseForm.setTags(List.of(new HouseTag()));
        System.out.println(houseService.save(houseForm).getResult());
    }

    @Test
    void testUploadFile() {
        String fileName = "/Users/peter/Desktop/a.png";
        File file = new File(fileName);

        try {
            Response response = qiNiuService.uploadFile(file);
            System.out.println(response.isOK());
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }
    @Test
    void contextLoads() {
        //iSmsService.sendSms("1923823832");
        System.out.println(passwordEncoder.encode("admin"));
        System.out.println(passwordEncoder.encode("123456"));
    }

    @Test
    void addressService() {
        addressService.findAllCities().getResult().forEach(System.out::println);
    }
}
