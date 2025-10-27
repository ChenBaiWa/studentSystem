package com.studentsystem.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;


    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(Integer userId, Integer userRole, String realName, boolean rememberMe) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userRole", userRole);
        claims.put("realName", realName);

        long expireTime = expiration ;

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expireTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            // Token已过期也是一种有效的token，只是过期了
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Integer getUserIdFromToken(String token) {
        try {
            return parseToken(token).get("userId", Integer.class);
        } catch (ExpiredJwtException e) {
            // 即使token过期，我们仍然可以从中提取信息
            return e.getClaims().get("userId", Integer.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Integer getUserRoleFromToken(String token) {
        try {
            return parseToken(token).get("userRole", Integer.class);
        } catch (ExpiredJwtException e) {
            // 即使token过期，我们仍然可以从中提取信息
            return e.getClaims().get("userRole", Integer.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getRealNameFromToken(String token) {
        try {
            return parseToken(token).get("realName", String.class);
        } catch (ExpiredJwtException e) {
            // 即使token过期，我们仍然可以从中提取信息
            return e.getClaims().get("realName", String.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 添加从HTTP请求中获取用户ID的方法
    public Long getUserIdFromRequest(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token != null) { // 移除了validateToken检查，即使token过期也能提取信息
            Integer userId = getUserIdFromToken(token);
            return userId != null ? userId.longValue() : null;
        }
        return null;
    }

    // 添加从HTTP请求中获取用户角色的方法
    public Integer getUserRoleFromRequest(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token != null) { // 移除了validateToken检查，即使token过期也能提取信息
            return getUserRoleFromToken(token);
        }
        return null;
    }

    // 添加从HTTP请求中获取真实姓名的方法
    public String getRealNameFromRequest(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token != null) { // 移除了validateToken检查，即使token过期也能提取信息
            return getRealNameFromToken(token);
        }
        return null;
    }

    // 从请求头中提取token
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
