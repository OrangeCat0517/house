package com.example.house.service.users;

import com.example.house.base.ServiceResult;

public interface ISmsService {

    ServiceResult<String> sendSms(String telephone);
    //发送验证码，验证码和手机号码绑定在一起。之后进入Redis。

    String getSmsCode(String telephone);
    //获取Redis缓存中的验证码

    void remove(String telephone);
    //强制让验证码被删除，从Redis中删除
}
