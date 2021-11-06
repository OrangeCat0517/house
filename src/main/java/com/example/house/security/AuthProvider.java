package com.example.house.security;

import com.example.house.domain.User;
import com.example.house.service.users.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

//当Spring Security默认提供的实现类不能满足需求的时候可以扩展AuthenticationProvider
public class AuthProvider implements AuthenticationProvider {
    @Autowired
    private IUserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String userName = authentication.getName();//网页上的用户名
        String inputPassword = (String) authentication.getCredentials();//网页上的密码

        User user = userService.findUserByName(userName);
        if (user == null)
            throw new AuthenticationCredentialsNotFoundException("authError");

        if (passwordEncoder.matches(inputPassword, user.getPassword()))
            //此处有个坑，spring security 5变化很多，第一个参数是前端传来的，后一个才是数据库的秘文
            return new UsernamePasswordAuthenticationToken(user,
                    null, user.getAuthorities());

        throw new BadCredentialsException("authError");

    }

    @Override
    public boolean supports(Class<?> authentication) {
        //supports用来指明该Provider是否适用于该类型的认证
        //如果不合适，则寻找另一个Provider进行验证处理。
        return true;
    }
}
