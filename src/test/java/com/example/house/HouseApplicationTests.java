package com.example.house;

import com.example.house.service.ISmsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class HouseApplicationTests {

    @Autowired
    private ISmsService iSmsService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void contextLoads() {
        //iSmsService.sendSms("1923823832");
        System.out.println(passwordEncoder.encode("admin"));
        System.out.println(passwordEncoder.encode("123456"));
    }

}
