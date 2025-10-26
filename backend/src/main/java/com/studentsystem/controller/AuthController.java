package com.studentsystem.controller;

import com.studentsystem.entity.SysUser;
import com.studentsystem.service.AuthService;
import com.studentsystem.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. 接收请求并校验必传参数
            if (!request.containsKey("userRole") || !request.containsKey("phone")
                || !request.containsKey("password") || !request.containsKey("realName")) {
                response.put("success", false);
                response.put("message", "信息不完整");
                return response;
            }

            // 2. 合法性校验
            Integer userRole = (Integer) request.get("userRole");
            String phone = (String) request.get("phone");
            String password = (String) request.get("password");
            String realName = (String) request.get("realName");

            // 角色校验
            if (userRole == null || (userRole != 1 && userRole != 2)) {
                response.put("success", false);
                response.put("message", "角色选择错误");
                return response;
            }

            // 手机号格式校验
            if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
                response.put("success", false);
                response.put("message", "手机号格式错误");
                return response;
            }

            // 密码长度校验
            if (password == null || password.length() < 6 || password.length() > 16) {
                response.put("success", false);
                response.put("message", "密码格式错误");
                return response;
            }

            // 3. 手机号唯一性校验
            if (authService.isPhoneRegistered(phone)) {
                response.put("success", false);
                response.put("message", "手机号已注册，请直接登录");
                return response;
            }

            // 4. 密码加密处理和注册入库
            SysUser user = authService.registerUser(userRole, phone, password, realName);

            response.put("success", true);
            response.put("message", "注册成功");
            Map<String, Object> data = new HashMap<>();
            data.put("id", user.getId());
            data.put("phone", user.getPhone());
            data.put("realName", user.getRealName());
            data.put("userRole", user.getUserRole());
            response.put("data", data);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "系统错误，请稍后重试");
        }

        return response;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. 接收请求并校验必传参数
            if (!request.containsKey("phone") || !request.containsKey("password")) {
                response.put("success", false);
                response.put("message", "请输入手机号和密码");
                return response;
            }

            String phone = (String) request.get("phone");
            String password = (String) request.get("password");
            Boolean rememberMe = (Boolean) request.getOrDefault("rememberMe", false);

            // 2. 用户存在性校验
            Map<String, Object> loginResult = authService.login(phone, password);
            if (!(Boolean) loginResult.get("success")) {
                response.put("success", false);
                response.put("message", loginResult.get("message"));
                return response;
            }

            // 3. 登录成功，生成token
            SysUser user = (SysUser) loginResult.get("user");
            String token = jwtUtil.generateToken(user.getId(), user.getUserRole(), user.getRealName(), rememberMe);

            response.put("success", true);
            response.put("message", "登录成功");
            
            Map<String, Object> data = new HashMap<>();
            data.put("id", user.getId());
            data.put("phone", user.getPhone());
            data.put("realName", user.getRealName());
            data.put("userRole", user.getUserRole());
            data.put("token", token);
            response.put("data", data);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "系统错误，请稍后重试");
        }

        return response;
    }

    @GetMapping("/check-phone")
    public Map<String, Object> checkPhone(@RequestParam String phone) {
        Map<String, Object> response = new HashMap<>();
        boolean exists = authService.isPhoneRegistered(phone);
        response.put("exists", exists);
        return response;
    }
}