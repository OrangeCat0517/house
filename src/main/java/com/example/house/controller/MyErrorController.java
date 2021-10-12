package com.example.house.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.house.base.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

@Controller
public class MyErrorController implements ErrorController {
    private static final String ERROR_PATH = "/error";

    private final ErrorAttributes errorAttributes;

    //@Override
    public String getErrorPath() {
        return ERROR_PATH;
    }

    @Autowired
    public MyErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    /**
     * 和HTTP相关内容的错误处理
     */
    @RequestMapping(value = ERROR_PATH, produces = "text/html")
    public String errorPageHandler(HttpServletRequest request, HttpServletResponse response) {
        int status = response.getStatus();
        switch (status) {
            case 403:
                return "403";
            case 404:
                return "404";
            case 500:
                return "500";
        }
        return "index";
    }

    /**
     * 除Web页面外的错误处理，比如Json/XML等
     */
    @RequestMapping(value = ERROR_PATH)
    @ResponseBody
    public ApiResponse errorApiHandler(HttpServletRequest request) {
        WebRequest webRequest = new ServletWebRequest(request);

        Map<String, Object> attr = errorAttributes.getErrorAttributes(webRequest, ErrorAttributeOptions.defaults());
        int status = getStatus(request);

        return ApiResponse.ofMessage(status, String.valueOf(attr.getOrDefault("message", "error")));
    }

    private int getStatus(HttpServletRequest request) {
        Integer status = (Integer) request.getAttribute("javax.servlet.error.status_code");
        if (status != null) {
            return status;
        }
        return 500; //500一般是Java代码错误
    }
}
