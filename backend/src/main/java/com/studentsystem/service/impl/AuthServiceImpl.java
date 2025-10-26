package com.studentsystem.service.impl;

import com.studentsystem.entity.SysUser;
import com.studentsystem.mapper.SysUserMapper;
import com.studentsystem.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public boolean isPhoneRegistered(String phone) {
        return sysUserMapper.countByPhone(phone) > 0;
    }

    @Override
    @Transactional
    public SysUser registerUser(Integer userRole, String phone, String password, String realName) {
        // 密码加密
        String encryptedPassword = encryptPassword(password);

        // 创建用户对象
        SysUser user = new SysUser();
        user.setUserRole(userRole);
        user.setPhone(phone);
        user.setPassword(encryptedPassword);
        user.setRealName(realName);
        user.setCreateTime(new Date());

        // 插入数据库
        sysUserMapper.insert(user);

        return user;
    }

    @Override
    public String encryptPassword(String password) {
        try {
            // 使用MD5加密（实际项目中建议使用更安全的加密方式如BCrypt）
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(password.getBytes());

            // 转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }

    @Override
    public Map<String, Object> login(String phone, String password) {
        Map<String, Object> result = new HashMap<>();
        
        // 根据手机号查询用户信息
        SysUser user = sysUserMapper.selectByPhone(phone);
        
        // 用户不存在
        if (user == null) {
            result.put("success", false);
            result.put("message", "手机号未注册");
            return result;
        }
        
        // 验证密码
        String encryptedPassword = encryptPassword(password);
        if (!encryptedPassword.equals(user.getPassword())) {
            result.put("success", false);
            result.put("message", "密码错误");
            return result;
        }
        
        // 登录成功
        result.put("success", true);
        result.put("user", user);
        return result;
    }
}