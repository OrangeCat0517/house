package com.example.house.security;

import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.house.util.LoginUserUtil;
import com.example.house.domain.User;
import com.example.house.service.users.ISmsService;
import com.example.house.service.users.IUserService;
import org.elasticsearch.common.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

//先尝试按用户名密码登陆，成功这个类结束
//失败？再看手机号对不对，如果不对程序也结束
//如果对，按照手机号去查用户，
//如果用户存在，校验验证码比对，后填入令牌
//如果用户不存在，为其注册成一个新用户，填入令牌
public class AuthFilter extends UsernamePasswordAuthenticationFilter {
    //UsernamePasswordAuthenticationFilter用户名-密码认证过滤器
    //以后只要用用户名和密码进行认证，就采用UsernamePasswordAuthenticationFilter
    @Autowired
    private IUserService userService;
    @Autowired
    private ISmsService smsService;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String name = super.obtainUsername(request);//obtainUsername从request中获取一个叫username属性的值
        if (name != null && !"".equals(name)) { //Strings.isNullOrEmpty(telephone)
            request.setAttribute("username", name);
            return super.attemptAuthentication(request, response);
            //attemptAuthentication进行用户名和密码的认证
        }

        String telephone = request.getParameter("telephone");
        if (Strings.isNullOrEmpty(telephone) || !LoginUserUtil.checkTelephone(telephone)) {
            throw new BadCredentialsException("Wrong telephone number");
        }

        //当代码能够运行到此处，一定是因为用户名和密码没有验证成功，或者手机号是非空的/合法的
        User user = userService.findUserByTelephone(telephone); //按照手机号去查找用户
        String inputCode = request.getParameter("smsCode"); //从网页上取得用户输入的验证码
        String sessionCode = smsService.getSmsCode(telephone); //从redis中取生成的验证码
        if (Objects.equals(inputCode, sessionCode)) { //两个验证码相同
            if (user == null) { // 如果这个手机号的用户不存在，则注册一个新用户
                user = userService.addUserByPhone(telephone);
            }
            //程序到这个地方，要么是用户已经验证码相符能登陆；
            //或者是验证码相符但是用户不存在，那就创建这个用户
            return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            //UsernamePasswordAuthenticationToken是一个用户已经登陆成功的令牌，供Spring Security后续使用
            //这个令牌需要准备2个内容，一个是用户，一个是用户的权限
        } else {
            throw new BadCredentialsException("smsCodeError");
        }
    }
}
