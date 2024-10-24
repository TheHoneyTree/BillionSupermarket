package com.billion.service;

import com.billion.dto.UserLoginDTO;
import com.billion.entity.User;

public interface UserService {

    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    User wxLogin(UserLoginDTO userLoginDTO);
}
