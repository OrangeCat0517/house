package com.example.house.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.house.util.LoginUserUtil;
import com.example.house.domain.Role;
import com.example.house.domain.User;
import com.example.house.dto.UserDTO;
import com.example.house.mapper.RoleMapper;
import com.example.house.mapper.UserMapper;
import com.example.house.service.IUserService;
import com.example.house.base.ServiceResult;
import com.google.common.collect.Lists;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User findUserByName(String userName) {
        User user = userMapper.findByName(userName);
        if(user == null) return null;

        List<Role> roles = roleMapper.findRolesByUserId(user.getId());//ADMIN  USER
        if (roles == null || roles.isEmpty()) {
            throw new DisabledException("权限非法");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        roles.forEach(role -> authorities.add(
                new SimpleGrantedAuthority("ROLE_" + role.getName())));
//        for (Role role : roles)
//            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
        user.setAuthorityList(authorities);
        return user;
    }

    @Override
    public ServiceResult<UserDTO> findById(Long userId) {
        User user = userMapper.findOne(userId);
        if (user == null)
            return ServiceResult.notFound();
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        return ServiceResult.of(userDTO);
    }

    @Override
    public User findUserByTelephone(String telephone) {
        User user = userMapper.findUserByPhoneNumber(telephone);
        if (user == null) {
            return null;
        }
        List<Role> roles = roleMapper.findRolesByUserId(user.getId());
        if (roles == null || roles.isEmpty()) {
            throw new DisabledException("权限非法");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        roles.forEach(
                role ->
                        authorities.add(
                                new SimpleGrantedAuthority("ROLE_" + role.getName())));
        user.setAuthorityList(authorities);
        return user;
    }

    @Override
    @Transactional //声明式事务，事务处理
    public User addUserByPhone(String telephone) {
        User user = new User();
        user.setPhoneNumber(telephone);
        user.setName(telephone.substring(0, 3) + "****" +
                telephone.substring(7));
        user.setStatus(0); //0-启用 1-禁用
        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setLastLoginTime(now);
        user.setLastUpdateTime(now);
        userMapper.save(user);

        Role role = new Role();
        role.setName("USER");
        role.setUserId(user.getId());
        roleMapper.save(role);
        //user.setAuthorityList(List.of(new SimpleGrantedAuthority("ROLE_USER")));
        user.setAuthorityList(
                Lists.newArrayList(
                        new SimpleGrantedAuthority("ROLE_USER")));
        return user;
    }

    @Override
    @Transactional
    public ServiceResult modifyUserProfile(String profile, String value) {
        Long userId = LoginUserUtil.getLoginUserId();
        if (profile == null || profile.isEmpty()) {
            return  ServiceResult.of(false, "属性不可以为空");
        }
        switch (profile) {
            case "name":
                userMapper.updateUsername(userId, value);
                break;
            case "email":
                userMapper.updateEmail(userId, value);
                break;
            case "password":
                //userMapper.updatePassword(userId, passwordEncoder.encodePassword(value, userId)); spring security老版本的写法
                userMapper.updatePassword(userId, passwordEncoder.encode(value));
                break;
            default:
                return ServiceResult.of(false, "不支持的属性");
        }
        return ServiceResult.success();
    }
}
