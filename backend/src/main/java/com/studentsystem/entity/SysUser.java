package com.studentsystem.entity;

import java.util.Date;

public class SysUser {
    private Integer id;
    private Integer userRole; // 1:老师, 2:学生
    private String phone;
    private String password;
    private String realName;
    private Date createTime;
    private Date updateTime;

    // Getter和Setter方法
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserRole() { return userRole; }
    public void setUserRole(Integer userRole) { this.userRole = userRole; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
