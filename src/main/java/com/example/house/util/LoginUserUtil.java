package com.example.house.util;

import java.util.regex.Pattern;

import com.example.house.domain.User;
import org.springframework.security.core.context.SecurityContextHolder;

public class LoginUserUtil {
    //这个类是一个工具类：手机，邮箱验证
    private static final String PHONE_REGEX = "^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(18[0,5-9]))\\d{8}$";
    private static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_REGEX);

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    public static boolean checkTelephone(String target) {
        return PHONE_PATTERN.matcher(target).matches();
    }

    public static boolean checkEmail(String target) {
            return EMAIL_PATTERN.matcher(target).matches();
    }


    private static User load() {
        //读取一个用户
        Object principal =
                SecurityContextHolder
                        .getContext().getAuthentication().getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }
        return null;
    }

    public static Long getLoginUserId() {
        //读取一个用户中的id
        User user = load();
        if (user == null) {
            return -1L; //此时表示用户不存在
        }
        return user.getId();
    }

}
