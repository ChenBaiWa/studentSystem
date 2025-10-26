package com.studentsystem.service;

import com.studentsystem.entity.SysUser;

import java.util.Map;

public interface AuthService {

    /**
     * 检查手机号是否已注册
     */
    boolean isPhoneRegistered(String phone);

    /**
     * 注册用户
     */
    SysUser registerUser(Integer userRole, String phone, String password, String realName);

    /**
     * 密码加密
     */
    String encryptPassword(String password);

    /**
     * 用户登录
     */
    Map<String, Object> login(String phone, String password);
}