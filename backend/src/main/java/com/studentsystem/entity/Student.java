package com.studentsystem.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Student {
    
    private Long id;
    
    private String name;
    
    private String gender;
    
    private Integer age;
    
    private String email;
    
    private String phone;
    
    private String address;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
