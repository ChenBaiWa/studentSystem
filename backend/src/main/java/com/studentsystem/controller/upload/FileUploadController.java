package com.studentsystem.controller.upload;

import com.studentsystem.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/upload")
@CrossOrigin
public class FileUploadController {

    @Value("${upload.homework.path:/upload/homework/}")
    private String homeworkUploadPath;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 上传作业图片
     * @param images 作业图片文件列表
     * @param request HTTP请求
     * @return 上传结果，包含图片URL列表
     */
    @PostMapping("/homework")
    public ResponseEntity<Map<String, Object>> uploadHomeworkImages(
            @RequestParam("images") List<MultipartFile> images,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Long userId = jwtUtil.getUserIdFromRequest(request);
        if (userId == null) {
            response.put("success", false);
            response.put("message", "未登录");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // 检查上传目录是否存在，如果不存在则创建
            Path uploadPath = Paths.get(homeworkUploadPath);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            List<String> urls = new ArrayList<>();
            
            // 处理每个上传的文件
            for (MultipartFile image : images) {
                if (image.isEmpty()) {
                    continue;
                }
                
                // 检查文件类型
                String contentType = image.getContentType();
                if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
                    response.put("success", false);
                    response.put("message", "只支持JPG/PNG格式的图片");
                    return ResponseEntity.badRequest().body(response);
                }
                
                // 检查文件大小 (5MB)
                if (image.getSize() > 5 * 1024 * 1024) {
                    response.put("success", false);
                    response.put("message", "图片大小不能超过5MB");
                    return ResponseEntity.badRequest().body(response);
                }
                
                // 生成唯一的文件名
                String originalFilename = image.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                String uniqueFilename = userId + "_" + timestamp + "_" + System.currentTimeMillis() + extension;
                
                // 保存文件
                Path filePath = uploadPath.resolve(uniqueFilename);
                Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                
                // 构造可访问的URL路径
                String url = "/upload/homework/" + uniqueFilename;
                urls.add(url);
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("urls", urls);
            
            response.put("success", true);
            response.put("message", "上传成功");
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "文件上传失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "上传过程中发生错误: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}