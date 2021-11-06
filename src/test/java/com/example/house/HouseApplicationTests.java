package com.example.house;

import com.example.house.service.house.IAddressService;
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

import java.io.File;

@SpringBootTest
class HouseApplicationTests {

    @Autowired
    private ISmsService iSmsService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private IAddressService iAddressService;

    @Autowired
    private IQiNiuService qiNiuService;

    @Test
    public void testUploadFile() {
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
        iAddressService.findAllCities().getResult().forEach(System.out::println);
    }
}
