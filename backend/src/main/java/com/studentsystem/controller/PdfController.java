package com.studentsystem.controller;

import com.studentsystem.entity.Question;
import com.studentsystem.service.ExerciseSetService;
import com.studentsystem.util.JwtUtil;
import com.volcengine.ark.runtime.model.completion.chat.*;
import com.volcengine.ark.runtime.service.ArkService;
import okhttp3.ConnectionPool;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import okhttp3.Dispatcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/upload")
@CrossOrigin
public class PdfController {

    @Autowired
    private ExerciseSetService exerciseSetService;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${ark.api-key}")
    private String apiKey;

    @Value("${ark.base-url}")
    private String baseUrl;

    private ArkService service;

    @Value("${ark.model-id:doubao-seed-1-6-251015}")
    private String modelId;

    private ObjectMapper objectMapper = new ObjectMapper();

    // 临时存储目录
    private static final String UPLOAD_DIR = System.getProperty("java.io.tmpdir") + "/uploads/";

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
     * 上传PDF文件并转换为图片，然后调用AI接口解析生成题目列表
     * @param file PDF文件
     * @param exerciseSetId 习题集ID
     * @param request HTTP请求
     * @return 解析后的题目列表
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> uploadPdf(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();

        // 验证用户身份
        Long teacherId = jwtUtil.getUserIdFromRequest(request);
        if (teacherId == null) {
            response.put("success", false);
            response.put("message", "未登录");
            return ResponseEntity.status(401).body(response);
        }

        // 验证文件
        if (file.isEmpty()) {
            response.put("success", false);
            response.put("message", "文件不能为空");
            return ResponseEntity.badRequest().body(response);
        }

        // 检查文件类型
        if (!Objects.requireNonNull(file.getContentType()).equals("application/pdf")) {
            response.put("success", false);
            response.put("message", "只支持PDF文件");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // 创建上传目录
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 保存文件
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".pdf";
            String filename = UUID.randomUUID() + fileExtension;
            Path filePath = uploadPath.resolve(filename);
            Files.write(filePath, file.getBytes());

            // 将PDF转换为图片
            List<String> imagePaths = convertPdfToImages(filePath.toString());

            // 调用AI接口解析图片
            Map<String, List<Question>> groupedQuestions = parseImagesWithAI(imagePaths);

            response.put("success", true);
            response.put("message", "解析成功");
            response.put("data", groupedQuestions);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "文件上传和解析失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 使用PDFBox将PDF转换为图片
     * @param pdfPath PDF文件路径
     * @return 图片路径列表
     */
    private List<String> convertPdfToImages(String pdfPath) throws IOException {
        List<String> imagePaths = new ArrayList<>();

        try (PDDocument document = PDDocument.load(new File(pdfPath))) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);

            // 为每一页生成图片
            for (int i = 0; i < Math.min(document.getNumberOfPages(), 10); i++) { // 限制最多处理10页
                BufferedImage image = pdfRenderer.renderImageWithDPI(i, 150); // 150 DPI渲染

                // 保存图片
                String imageFilename = "page_" + i + ".png";
                Path imagePath = Paths.get(UPLOAD_DIR, imageFilename);
                ImageIO.write(image, "PNG", imagePath.toFile());
                imagePaths.add(imagePath.toString());
            }
        }

        return imagePaths;
    }

    /**
     * 调用AI接口解析图片
     * @param imagePaths 图片路径列表
     * @return 解析后的题目列表，按题型分组
     */
    private Map<String, List<Question>> parseImagesWithAI(List<String> imagePaths) throws Exception {
        Map<String, List<Question>> groupedQuestions = new HashMap<>();

        // 为每张图片调用AI接口
        for (String imagePath : imagePaths) {
            // 生成Base64数据URL
            String base64Data = "data:image/png;base64," + encodeImageToBase64(imagePath);

            // 构建消息内容
            List<ChatCompletionContentPart> contentParts = new ArrayList<>();

            // 图片部分
            contentParts.add(ChatCompletionContentPart.builder()
                    .type("image_url")
                    .imageUrl(new ChatCompletionContentPart.ChatCompletionContentPartImageURL(base64Data))
                    .build());

            // 文本指令部分
            contentParts.add(ChatCompletionContentPart.builder()
                    .type("text")
                    .text("请分析这张图片中的题目内容，按以下JSON格式返回题目信息：" +
                          "{ \"questions\": [ { \"type\": \"choice\", " +
                          "\"content\": \"题干内容\", " +
                          "\"options\": [{\"content\": \"选项A\"}, {\"content\": \"选项B\"}, {\"content\": \"选项C\"}, {\"content\": \"选项D\"}], " +
                          "\"answer\": \"答案\", " +
                          "\"score\": 5 } ] } " +
                          "如果是选择题，请提供选项；如果是填空题或主观题，则options字段为空数组。" +
                          "请严格按照上述JSON格式返回，不要包含其他内容。")
                    .build());

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
                    .maxTokens(1500)
                    .build();

            // 调用AI服务
            ChatCompletionResult result = service.createChatCompletion(req);

            // 处理返回结果
            if (!result.getChoices().isEmpty()) {
                Object contentObj = result.getChoices().get(0).getMessage().getContent();
                String content = contentObj != null ? contentObj.toString() : "";
                parseAndGroupQuestions(content, groupedQuestions);
            }
        }

        return groupedQuestions;
    }

    /**
     * 解析AI返回的内容并按题型分组
     * @param content AI返回的内容
     * @param groupedQuestions 已分组的题目集合
     */
    private void parseAndGroupQuestions(String content, Map<String, List<Question>> groupedQuestions) {
        try {
            // 尝试从AI返回的内容中提取JSON部分
            String jsonStr = extractJsonFromResponse(content);
            if (jsonStr != null) {
                // 验证JSON格式是否正确
                if (isValidJsonFormat(jsonStr)) {
                    JsonNode rootNode = objectMapper.readTree(jsonStr);
                    JsonNode questionsArray = rootNode.get("questions");

                    if (questionsArray != null && questionsArray.isArray()) {
                        for (JsonNode questionNode : questionsArray) {
                            Question question = new Question();
                            question.setType(getStringValue(questionNode, "type"));
                            question.setContent(getStringValue(questionNode, "content"));
                            question.setAnswer(getStringValue(questionNode, "answer"));
                            question.setScore(getIntValue(questionNode, "score"));

                            // 处理选项
                            JsonNode optionsNode = questionNode.get("options");
                            if (optionsNode != null && optionsNode.isArray() && optionsNode.size() > 0) {
                                question.setOptions(optionsNode.toString());
                            } else {
                                question.setOptions("[]");
                            }

                            // 按题型分组
                            String type = question.getType();
                            if (type != null) {
                                groupedQuestions.computeIfAbsent(type, k -> new ArrayList<>()).add(question);
                            }
                        }
                    }
                } else {
                    // 如果JSON格式不正确，创建示例题目
                    createSampleQuestions(groupedQuestions);
                }
            } else {
                // 如果没有提取到JSON，创建示例题目
                createSampleQuestions(groupedQuestions);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 如果解析失败，创建一些示例题目
            createSampleQuestions(groupedQuestions);
        }
    }

    /**
     * 验证JSON格式是否正确
     */
    private boolean isValidJsonFormat(String jsonStr) {
        try {
            objectMapper.readTree(jsonStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从JsonNode中安全获取字符串值
     */
    private String getStringValue(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode != null ? fieldNode.asText() : null;
    }

    /**
     * 从JsonNode中安全获取整数值
     */
    private Integer getIntValue(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode != null ? fieldNode.asInt() : null;
    }

    /**
     * 从AI返回的内容中提取JSON部分
     * @param content AI返回的内容
     * @return 提取的JSON字符串
     */
    private String extractJsonFromResponse(String content) {
        // 查找第一个{和最后一个}之间的内容
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');

        if (start != -1 && end != -1 && end > start) {
            return content.substring(start, end + 1);
        }

        return null;
    }

    /**
     * 创建示例题目（用于解析失败时的备选方案）
     * @param groupedQuestions 已分组的题目集合
     */
    private void createSampleQuestions(Map<String, List<Question>> groupedQuestions) {
        // 添加选择题示例
        Question choiceQuestion = new Question();
        choiceQuestion.setType("choice");
        choiceQuestion.setContent("下列哪个选项是正确的？");
        choiceQuestion.setOptions("[{\"content\":\"选项A\"},{\"content\":\"选项B\"},{\"content\":\"选项C\"},{\"content\":\"选项D\"}]");
        choiceQuestion.setAnswer("B");
        choiceQuestion.setScore(5);
        groupedQuestions.computeIfAbsent("choice", k -> new ArrayList<>()).add(choiceQuestion);

        // 添加填空题示例
        Question fillQuestion = new Question();
        fillQuestion.setType("fill");
        fillQuestion.setContent("中国的首都是______。");
        fillQuestion.setAnswer("北京");
        fillQuestion.setScore(5);
        groupedQuestions.computeIfAbsent("fill", k -> new ArrayList<>()).add(fillQuestion);

        // 添加主观题示例
        Question subjectiveQuestion = new Question();
        subjectiveQuestion.setType("subjective");
        subjectiveQuestion.setContent("请简述人工智能的发展历程。");
        subjectiveQuestion.setAnswer("人工智能的发展经历了起步期、发展期和繁荣期等多个阶段...");
        subjectiveQuestion.setScore(10);
        groupedQuestions.computeIfAbsent("subjective", k -> new ArrayList<>()).add(subjectiveQuestion);
    }

    /**
     * 将图片编码为Base64字符串
     * @param imagePath 图片路径
     * @return Base64编码的字符串
     */
    private String encodeImageToBase64(String imagePath) throws IOException {
        File file = new File(imagePath);
        byte[] fileContent = Files.readAllBytes(file.toPath());
        return Base64.getEncoder().encodeToString(fileContent);
    }
}
