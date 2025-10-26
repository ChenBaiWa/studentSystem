package com.studentsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studentsystem.entity.Student;
import com.studentsystem.mapper.StudentMapper;
import com.studentsystem.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {
    
    private final StudentMapper studentMapper;
    
    @Override
    public List<Student> getAllStudents() {
        return studentMapper.selectList(new LambdaQueryWrapper<Student>().orderByDesc(Student::getCreateTime));
    }
    
    @Override
    public Student getStudentById(Long id) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new RuntimeException("学生不存在，ID: " + id);
        }
        return student;
    }
    
    @Override
    public Student createStudent(Student student) {
        studentMapper.insert(student);
        return student;
    }
    
    @Override
    public Student updateStudent(Long id, Student student) {
        Student existingStudent = getStudentById(id);
        existingStudent.setName(student.getName());
        existingStudent.setGender(student.getGender());
        existingStudent.setAge(student.getAge());
        existingStudent.setEmail(student.getEmail());
        existingStudent.setPhone(student.getPhone());
        existingStudent.setAddress(student.getAddress());
        
        studentMapper.updateById(existingStudent);
        return existingStudent;
    }
    
    @Override
    public void deleteStudent(Long id) {
        Student student = getStudentById(id);
        studentMapper.deleteById(id);
    }
}
