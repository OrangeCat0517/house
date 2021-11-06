package com.example.house.service.house;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;

import java.io.File;
import java.io.InputStream;



public interface IQiNiuService {
    Response uploadFile(File file) throws QiniuException;
    Response uploadFile(InputStream inputStream) throws QiniuException;
    Response delete(String key) throws QiniuException;
}
