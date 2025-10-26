package com.studentsystem.mapper;

import com.studentsystem.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysUserMapper {

    /**
     * 插入用户
     */
    int insert(SysUser user);

    /**
     * 根据手机号统计用户数量
     */
    int countByPhone(@Param("phone") String phone);

    /**
     * 根据ID查询用户
     */
    SysUser selectById(@Param("id") Integer id);
    
    /**
     * 根据手机号查询用户
     */
    SysUser selectByPhone(@Param("phone") String phone);
}