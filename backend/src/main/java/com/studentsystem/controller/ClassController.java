package com.studentsystem.controller;

import com.studentsystem.entity.Class;
import com.studentsystem.entity.Student;
import com.studentsystem.service.ClassService;
import com.studentsystem.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/classes")
public class ClassController {

    @Autowired
    private ClassService classService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取班级列表
     * @param request HTTP请求
     * @return 班级列表
     */
    @GetMapping
    public Map<String, Object> getClassList(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long creatorId = jwtUtil.getUserIdFromRequest(request);
            List<Class> classes = classService.findByCreatorId(creatorId);
            result.put("success", true);
            result.put("data", classes);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 创建班级
     * @param clazz 班级信息
     * @param request HTTP请求
     * @return 创建结果
     */
    @PostMapping
    public Map<String, Object> createClass(@RequestBody Class clazz, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long creatorId = jwtUtil.getUserIdFromRequest(request);
            clazz.setCreatorId(creatorId);
            // creatorName应该从JWT中获取或者通过其他方式获取
            Class createdClass = classService.createClass(clazz);
            result.put("success", true);
            result.put("data", createdClass);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 更新班级
     * @param clazz 班级信息
     * @param request HTTP请求
     * @return 更新结果
     */
    @PutMapping
    public Map<String, Object> updateClass(@RequestBody Class clazz, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            Class updatedClass = classService.updateClass(clazz);
            result.put("success", true);
            result.put("data", updatedClass);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 删除班级
     * @param id 班级ID
     * @param request HTTP请求
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteClass(@PathVariable Long id, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            classService.deleteClass(id);
            result.put("success", true);
            result.put("message", "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 学生加入班级
     * @param params 请求参数 {classCode: 班级号, verificationCode: 验证码}
     * @param request HTTP请求
     * @return 加入结果
     */
    @PostMapping("/join")
    public Map<String, Object> joinClass(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String classCode = params.get("classCode");
            String verificationCode = params.get("verificationCode");
            Long studentId = jwtUtil.getUserIdFromRequest(request);
            boolean success = classService.joinClass(classCode, verificationCode, studentId);
            result.put("success", true);
            result.put("message", "加入班级成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 获取学生已加入的班级列表
     * @param request HTTP请求
     * @return 班级列表
     */
    @GetMapping("/joined")
    public Map<String, Object> getJoinedClasses(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long studentId = jwtUtil.getUserIdFromRequest(request);
            List<Class> classes = classService.findJoinedClassesByStudentId(studentId);
            result.put("success", true);
            result.put("data", classes);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 获取班级下的学生列表
     * @param classId 班级ID
     * @param request HTTP请求
     * @return 学生列表
     */
    @GetMapping("/{classId}/students")
    public Map<String, Object> getClassStudents(@PathVariable Long classId, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Student> students = classService.findStudentsByClassId(classId);
            result.put("success", true);
            result.put("data", students);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }
}