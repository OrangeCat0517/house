package com.example.house.service.users;


import com.example.house.base.ServiceResult;
import com.example.house.domain.User;
import com.example.house.dto.UserDTO;

public interface IUserService {
    User findUserByName(String userName);
    User findUserByTelephone(String telephone);
    ServiceResult<UserDTO> findById(Long userId);
    User addUserByPhone(String telephone); //添加用户
    ServiceResult modifyUserProfile(String profile, String value);
}