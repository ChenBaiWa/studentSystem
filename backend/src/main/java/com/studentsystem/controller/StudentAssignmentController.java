package com.studentsystem.controller;

import com.studentsystem.entity.Assignment;
import com.studentsystem.entity.Student;
import com.studentsystem.entity.StudentAssignment;
import com.studentsystem.entity.StudentClass;
import com.studentsystem.mapper.AssignmentMapper;
import com.studentsystem.mapper.StudentAssignmentMapper;
import com.studentsystem.mapper.StudentClassMapper;
import com.studentsystem.mapper.StudentMapper;
import com.studentsystem.util.JwtUtil;
import com.volcengine.ark.runtime.model.completion.chat.*;
import com.volcengine.ark.runtime.service.ArkService;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.Base64;

@RestController
@RequestMapping("/student-assignments")
@CrossOrigin
public class StudentAssignmentController {

    @Autowired
    private StudentAssignmentMapper studentAssignmentMapper;

    @Autowired
    private AssignmentMapper assignmentMapper;

    @Autowired
    private StudentClassMapper studentClassMapper;
    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private JwtUtil jwtUtil;

    @Value("${ark.api-key}")
    private String apiKey;

    @Value("${ark.base-url}")
    private String baseUrl;

    @Value("${ark.model-id:doubao-seed-1-6-251015}")
    private String modelId;

    private ArkService service;

    @PostConstruct
    public void init() {
        ConnectionPool connectionPool = new ConnectionPool(5, 1, TimeUnit.SECONDS);
        Dispatcher dispatcher = new Dispatcher();
        this.service = ArkService.builder()
                .dispatcher(dispatcher)
                .connectionPool(connectionPool)
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();
    }

    /**
     * 获取学生的所有作业，按状态分组
     * @param request HTTP请求
     * @return 按状态分组的作业列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getStudentAssignments(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Long studentId = jwtUtil.getUserIdFromRequest(request);
        if (studentId == null) {
            response.put("success", false);
            response.put("message", "未登录");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // 获取学生加入的所有班级ID
            List<StudentClass> studentClasses = studentClassMapper.selectByStudentId(studentId);
            List<Long> classIds = studentClasses.stream()
                    .map(StudentClass::getClassId)
                    .collect(Collectors.toList());

            if (classIds.isEmpty()) {
                // 如果学生没有加入任何班级，返回空列表
                Map<String, Object> emptyData = new HashMap<>();
                emptyData.put("pending", new ArrayList<>());
                emptyData.put("submitted", new ArrayList<>());
                emptyData.put("expired", new ArrayList<>());
                response.put("success", true);
                response.put("data", emptyData);
                return ResponseEntity.ok(response);
            }

            // 获取学生所在班级的所有作业
            List<Assignment> assignments = assignmentMapper.selectByClassIds(classIds);

            // 获取学生已提交的作业
            List<StudentAssignment> submittedAssignments = studentAssignmentMapper.selectByStudentId(studentId);

            // 按状态分组
            List<Map<String, Object>> pendingList = new ArrayList<>(); // 待提交
            List<Map<String, Object>> submittedList = new ArrayList<>(); // 已提交（待批改/已批改）
            List<Map<String, Object>> expiredList = new ArrayList<>(); // 已截止（未提交）

            LocalDateTime now = LocalDateTime.now();

            for (Assignment assignment : assignments) {
                // 查找该作业是否已提交
                StudentAssignment submission = submittedAssignments.stream()
                        .filter(sa -> sa.getAssignmentId().equals(assignment.getId()))
                        .findFirst()
                        .orElse(null);

                Map<String, Object> assignmentData = new HashMap<>();
                assignmentData.put("id", assignment.getId());
                assignmentData.put("title", assignment.getTitle());
                assignmentData.put("classId", assignment.getClassId());
                assignmentData.put("className", assignment.getClassName());
                assignmentData.put("chapterId", assignment.getChapterId());
                assignmentData.put("chapterName", assignment.getChapterName());
                assignmentData.put("content", assignment.getContent());
                assignmentData.put("totalScore", assignment.getTotalScore());
                assignmentData.put("deadline", assignment.getDeadline());
                assignmentData.put("createTime", assignment.getCreateTime());

                if (submission != null) {
                    // 已提交的作业
                    assignmentData.put("submitTime", submission.getSubmitTime());
                    assignmentData.put("status", submission.getStatus());
                    assignmentData.put("score", submission.getScore());
                    assignmentData.put("feedback", submission.getFeedback());
                    submittedList.add(assignmentData);
                } else {
                    // 未提交的作业
                    if (now.isAfter(assignment.getDeadline())) {
                        // 已截止
                        expiredList.add(assignmentData);
                    } else {
                        // 待提交
                        pendingList.add(assignmentData);
                    }
                }
            }

            Map<String, Object> data = new HashMap<>();
            data.put("pending", pendingList);
            data.put("submitted", submittedList);
            data.put("expired", expiredList);

            response.put("success", true);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "获取作业列表失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取学生作业提交详情
     * @param id 作业提交ID
     * @param request HTTP请求
     * @return 作业提交详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getStudentAssignment(
            @PathVariable Long id,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Long studentId = jwtUtil.getUserIdFromRequest(request);
        if (studentId == null) {
            response.put("success", false);
            response.put("message", "未登录");
            return ResponseEntity.status(401).body(response);
        }

        try {
            StudentAssignment studentAssignment = studentAssignmentMapper.findById(id);
            if (studentAssignment == null) {
                response.put("success", false);
                response.put("message", "作业提交记录不存在");
                return ResponseEntity.status(404).body(response);
            }

            // 获取作业详情
            Assignment assignment = assignmentMapper.findById(studentAssignment.getAssignmentId());
            if (assignment == null) {
                response.put("success", false);
                response.put("message", "作业不存在");
                return ResponseEntity.status(404).body(response);
            }

            // 组合返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("studentAssignment", studentAssignment);
            data.put("assignment", assignment);

            response.put("success", true);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取作业详情失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 提交作业
     * @param request HTTP请求
     * @param data 提交数据
     * @return 提交结果
     */
    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitAssignment(
            HttpServletRequest request,
            @RequestBody SubmitAssignmentRequest data) {
        Map<String, Object> response = new HashMap<>();
        Long studentId = jwtUtil.getUserIdFromRequest(request);
        if (studentId == null) {
            response.put("success", false);
            response.put("message", "未登录");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // 检查作业是否存在
            Assignment assignment = assignmentMapper.findById(data.getAssignmentId());
            if (assignment == null) {
                response.put("success", false);
                response.put("message", "作业不存在");
                return ResponseEntity.status(404).body(response);
            }

            // 检查是否已提交过该作业
            StudentAssignment existingSubmission = studentAssignmentMapper.selectByAssignmentIdAndStudentId(
                    data.getAssignmentId(), studentId);
            if (existingSubmission != null) {
                response.put("success", false);
                response.put("message", "作业已提交，不能重复提交");
                return ResponseEntity.status(400).body(response);
            }

            // 检查是否超过截止时间
            if (LocalDateTime.now().isAfter(assignment.getDeadline())) {
                response.put("success", false);
                response.put("message", "已超过作业截止时间，无法提交");
                return ResponseEntity.status(400).body(response);
            }

            // 创建作业提交记录
            StudentAssignment studentAssignment = new StudentAssignment();
            studentAssignment.setAssignmentId(data.getAssignmentId());
            studentAssignment.setStudentId(studentId);
            studentAssignment.setAnswer(data.getAnswer());
            studentAssignment.setStatus("submitted");
            Student student = studentMapper.selectByUserId(studentId);
            studentAssignment.setStudentName(student.getName());
            studentAssignment.setSubmitTime(LocalDateTime.now());
            studentAssignment.setCreateTime(LocalDateTime.now());
            studentAssignment.setUpdateTime(LocalDateTime.now());
            AiGradingResult gradingResult = gradeWithAI(data.getAnswer().split(","));
            studentAssignmentMapper.insert(studentAssignment);

            // 调用AI自动批改
            try {
                // 更新学生作业记录
                studentAssignment.setScore(gradingResult.getScore());
                studentAssignment.setFeedback(gradingResult.getFeedback());
                studentAssignment.setStatus("graded");
                studentAssignment.setGradeTime(LocalDateTime.now());
                studentAssignment.setUpdateTime(LocalDateTime.now());

                studentAssignmentMapper.grade(studentAssignment);
            } catch (Exception e) {
                // AI批改失败不应该影响作业提交
                System.err.println("AI自动批改失败: " + e.getMessage());
                e.printStackTrace();
            }

            response.put("success", true);
            response.put("message", "提交成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }

    /**
     * 调用AI接口对作业图片进行批改
     * @param imageUrls 作业图片URL数组
     * @return 批改结果
     */
    private AiGradingResult gradeWithAI(String[] imageUrls) throws Exception {
        // 构建消息内容
        List<ChatCompletionContentPart> contentParts = new ArrayList<>();

        // 添加文本指令部分
        contentParts.add(ChatCompletionContentPart.builder()
                .type("text")
                .text("你是一位严格的老师，请分析学生作业图片，给出评分和反馈意见。" +
                      "请按以下JSON格式返回结果：" +
                      "{ \"score\": 95, " +
                      "\"feedback\": \"学生的解答基本正确，但在第二题的计算过程中有一个小错误，扣5分。\" } " +
                      "评分范围为0-100分，请根据作业的正确性、完整性给出合理评分。" +
                      "反馈意见需要具体指出错误和改进建议。" +
                      "请严格按照上述JSON格式返回，不要包含其他内容。")
                .build());

        // 添加所有图片
        for (String imageUrl : imageUrls) {
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                String trimmedUrl = imageUrl.trim();
                String formattedImageUrl;
                
                // 检查是否为本地文件路径
                if (trimmedUrl.startsWith("/") || trimmedUrl.contains(File.separator)) {
                    // 尝试将本地文件转换为base64
                    formattedImageUrl = convertFileToBase64(trimmedUrl);
                } else {
                    // 假设已经是有效的URL
                    formattedImageUrl = trimmedUrl;
                }
                
                if (formattedImageUrl != null) {
                    contentParts.add(ChatCompletionContentPart.builder()
                            .type("image_url")
                            .imageUrl(new ChatCompletionContentPart.ChatCompletionContentPartImageURL(formattedImageUrl))
                            .build());
                }
            }
        }

        // 创建消息
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.builder()
                .role(ChatMessageRole.USER)
                .multiContent(contentParts)
                .build());

        // 构造请求
        ChatCompletionRequest req = ChatCompletionRequest.builder()
                .model(modelId)
                .messages(messages)
                .maxTokens(500)
                .build();

        // 调用AI服务
        ChatCompletionResult result = service.createChatCompletion(req);

        // 处理返回结果
        if (!result.getChoices().isEmpty()) {
            Object contentObj = result.getChoices().get(0).getMessage().getContent();
            String content = contentObj != null ? contentObj.toString() : "";

            // 解析AI返回的评分和反馈
            return parseGradingResult(content);
        } else {
            throw new Exception("AI未返回有效结果");
        }
    }

    /**
     * 将本地文件转换为base64格式的图片URL
     * @param filePath 文件路径
     * @return base64格式的图片URL
     */
    private String convertFileToBase64(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                System.err.println("文件不存在: " + filePath);
                return null;
            }
            
            String mimeType = Files.probeContentType(file.toPath());
            if (mimeType == null) {
                mimeType = "image/jpeg"; // 默认MIME类型
            }
            
            byte[] fileContent = Files.readAllBytes(file.toPath());
            String base64 = Base64.getEncoder().encodeToString(fileContent);
            return "data:" + mimeType + ";base64," + base64;
        } catch (Exception e) {
            System.err.println("转换文件到base64失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 解析AI返回的评分结果
     * @param content AI返回的内容
     * @return 评分结果对象
     */
    private AiGradingResult parseGradingResult(String content) {
        try {
            // 查找第一个{和最后一个}之间的内容
            int start = content.indexOf('{');
            int end = content.lastIndexOf('}');

            if (start != -1 && end != -1 && end > start) {
                String jsonStr = content.substring(start, end + 1);

                // 简单解析JSON（实际项目中建议使用Jackson等库）
                AiGradingResult result = new AiGradingResult();

                // 提取评分
                int scoreStart = jsonStr.indexOf("\"score\"");
                if (scoreStart != -1) {
                    int scoreValueStart = jsonStr.indexOf(":", scoreStart) + 1;
                    int scoreValueEnd = jsonStr.indexOf(",", scoreValueStart);
                    if (scoreValueEnd == -1) {
                        scoreValueEnd = jsonStr.indexOf("}", scoreValueStart);
                    }
                    String scoreStr = jsonStr.substring(scoreValueStart, scoreValueEnd).trim();
                    result.setScore(Integer.parseInt(scoreStr.replaceAll("[^0-9]", "")));
                }

                // 提取反馈
                int feedbackStart = jsonStr.indexOf("\"feedback\"");
                if (feedbackStart != -1) {
                    int feedbackValueStart = jsonStr.indexOf(":", feedbackStart) + 1;
                    // 判断是字符串还是其他类型
                    if (jsonStr.charAt(feedbackValueStart) == '"') {
                        // 字符串类型
                        int feedbackValueEnd = jsonStr.indexOf("\"", feedbackValueStart + 1);
                        // 查找下一个引号（考虑转义字符）
                        while (feedbackValueEnd > 0 && jsonStr.charAt(feedbackValueEnd - 1) == '\\') {
                            feedbackValueEnd = jsonStr.indexOf("\"", feedbackValueEnd + 1);
                        }
                        if (feedbackValueEnd > feedbackValueStart) {
                            result.setFeedback(jsonStr.substring(feedbackValueStart + 1, feedbackValueEnd));
                        }
                    } else {
                        // 非字符串类型
                        int feedbackValueEnd = jsonStr.indexOf(",", feedbackValueStart);
                        if (feedbackValueEnd == -1) {
                            feedbackValueEnd = jsonStr.indexOf("}", feedbackValueStart);
                        }
                        result.setFeedback(jsonStr.substring(feedbackValueStart, feedbackValueEnd).trim());
                    }
                }

                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 默认返回
        AiGradingResult result = new AiGradingResult();
        result.setScore(0);
        result.setFeedback("无法解析AI评分结果");
        return result;
    }

    /**
     * AI评分结果类
     */
    public static class AiGradingResult {
        private int score;
        private String feedback;

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public String getFeedback() {
            return feedback;
        }

        public void setFeedback(String feedback) {
            this.feedback = feedback;
        }
    }

    // 提交作业请求数据类
    static class SubmitAssignmentRequest {
        private Long assignmentId;
        private String answer; // 图片URL列表，用逗号分隔

        // Getters and setters
        public Long getAssignmentId() {
            return assignmentId;
        }

        public void setAssignmentId(Long assignmentId) {
            this.assignmentId = assignmentId;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }
    }
}
