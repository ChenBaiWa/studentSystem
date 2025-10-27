package com.studentsystem.controller;

import com.studentsystem.dto.ExerciseSetChapterDTO;
import com.studentsystem.entity.ExerciseSet;
import com.studentsystem.entity.Question;
import com.studentsystem.entity.StudentExerciseAnswer;
import com.studentsystem.mapper.StudentExerciseAnswerMapper;
import com.studentsystem.service.ExerciseSetService;
import com.studentsystem.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/student/exercise-sets")
@CrossOrigin
public class StudentExerciseController {

    @Autowired
    private ExerciseSetService exerciseSetService;
    
    @Autowired
    private StudentExerciseAnswerMapper studentExerciseAnswerMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取所有已发布的习题集（供学生查看）
     * @param request HTTP请求
     * @return 习题集列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getPublishedExerciseSets(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Long studentId = jwtUtil.getUserIdFromRequest(request);
        if (studentId == null) {
            response.put("success", false);
            response.put("message", "未登录");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // 获取所有已发布的习题集，不区分创建人
            List<ExerciseSet> exerciseSets = exerciseSetService.getPublishedExerciseSets();
            response.put("success", true);
            response.put("data", exerciseSets);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取习题集列表失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取指定习题集的章节列表（已发布的章节）
     * @param exerciseSetId 习题集ID
     * @param request HTTP请求
     * @return 章节列表
     */
    @GetMapping("/{exerciseSetId}/chapters")
    public ResponseEntity<Map<String, Object>> getChapters(
            @PathVariable Long exerciseSetId,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Long studentId = jwtUtil.getUserIdFromRequest(request);
        if (studentId == null) {
            response.put("success", false);
            response.put("message", "未登录");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // 检查习题集是否存在且已发布
            ExerciseSet exerciseSet = exerciseSetService.getExerciseSetById(exerciseSetId);
            if (exerciseSet == null) {
                response.put("success", false);
                response.put("message", "习题集不存在");
                return ResponseEntity.status(404).body(response);
            }

            if (!"published".equals(exerciseSet.getStatus())) {
                response.put("success", false);
                response.put("message", "习题集未发布");
                return ResponseEntity.status(400).body(response);
            }

            // 获取该习题集下的所有题目
            List<Question> questions = exerciseSetService.getQuestionsByExerciseSetId(exerciseSetId);
            
            // 按章节分组统计章节信息
            Map<Long, List<Question>> questionsGroupedByChapter = questions.stream()
                    .filter(q -> q.getChapterId() != null)
                    .collect(Collectors.groupingBy(Question::getChapterId));
            
            // 构造章节列表
            List<ExerciseSetChapterDTO> chapters = new ArrayList<>();
            for (Map.Entry<Long, List<Question>> entry : questionsGroupedByChapter.entrySet()) {
                // 获取章节中的任意一道题来获取章节信息
                Question sampleQuestion = entry.getValue().get(0);
                
                ExerciseSetChapterDTO chapter = new ExerciseSetChapterDTO();
                chapter.setId(sampleQuestion.getChapterId());
                chapter.setName(sampleQuestion.getChapterName());
                chapter.setExerciseSetId(exerciseSetId);
                chapter.setExerciseSetName(exerciseSet.getName());
                chapter.setQuestionCount(entry.getValue().size()); // 题目数量
                
                // 计算总分
                int totalScore = entry.getValue().stream()
                        .mapToInt(Question::getScore)
                        .sum();
                chapter.setTotalScore(totalScore);
                
                chapter.setCreateTime(exerciseSet.getCreateTime());
                chapters.add(chapter);
            }
            
            // 如果没有关联章节的题目，但习题集有题目，则显示一个默认章节
            if (chapters.isEmpty() && !questions.isEmpty()) {
                ExerciseSetChapterDTO defaultChapter = new ExerciseSetChapterDTO();
                defaultChapter.setId(0L);
                defaultChapter.setName("默认章节");
                defaultChapter.setExerciseSetId(exerciseSetId);
                defaultChapter.setExerciseSetName(exerciseSet.getName());
                defaultChapter.setQuestionCount(questions.size());
                
                // 计算总分
                int totalScore = questions.stream()
                        .mapToInt(Question::getScore)
                        .sum();
                defaultChapter.setTotalScore(totalScore);
                
                defaultChapter.setCreateTime(exerciseSet.getCreateTime());
                chapters.add(defaultChapter);
            }
            
            response.put("success", true);
            response.put("data", chapters);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取章节列表失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取指定章节的题目列表
     * @param exerciseSetId 习题集ID
     * @param chapterId 章节ID
     * @param request HTTP请求
     * @return 题目列表
     */
    @GetMapping("/{exerciseSetId}/chapters/{chapterId}/questions")
    public ResponseEntity<Map<String, Object>> getQuestions(
            @PathVariable Long exerciseSetId,
            @PathVariable Long chapterId,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Long studentId = jwtUtil.getUserIdFromRequest(request);
        if (studentId == null) {
            response.put("success", false);
            response.put("message", "未登录");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // 检查习题集是否存在且已发布
            ExerciseSet exerciseSet = exerciseSetService.getExerciseSetById(exerciseSetId);
            if (exerciseSet == null) {
                response.put("success", false);
                response.put("message", "习题集不存在");
                return ResponseEntity.status(404).body(response);
            }

            if (!"published".equals(exerciseSet.getStatus())) {
                response.put("success", false);
                response.put("message", "习题集未发布");
                return ResponseEntity.status(400).body(response);
            }

            List<Question> questions;
            // 如果章节ID为0，表示获取所有未分配章节的题目
            if (chapterId == 0) {
                questions = exerciseSetService.getQuestionsByExerciseSetId(exerciseSetId)
                        .stream()
                        .filter(q -> q.getChapterId() == null)
                        .collect(Collectors.toList());
            } else {
                // 获取题目列表，按排序字段排序
                questions = exerciseSetService.getQuestionsByExerciseSetIdAndChapterId(exerciseSetId, chapterId);
            }
            
            response.put("success", true);
            response.put("data", questions);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取题目列表失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取习题集的所有题目列表（直接模式，跳过章节）
     * @param exerciseSetId 习题集ID
     * @param request HTTP请求
     * @return 题目列表
     */
    @GetMapping("/{exerciseSetId}/direct")
    public ResponseEntity<Map<String, Object>> getQuestionsDirect(
            @PathVariable Long exerciseSetId,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Long studentId = jwtUtil.getUserIdFromRequest(request);
        if (studentId == null) {
            response.put("success", false);
            response.put("message", "未登录");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // 检查习题集是否存在且已发布
            ExerciseSet exerciseSet = exerciseSetService.getExerciseSetById(exerciseSetId);
            if (exerciseSet == null) {
                response.put("success", false);
                response.put("message", "习题集不存在");
                return ResponseEntity.status(404).body(response);
            }

            if (!"published".equals(exerciseSet.getStatus())) {
                response.put("success", false);
                response.put("message", "习题集未发布");
                return ResponseEntity.status(400).body(response);
            }

            // 获取习题集下的所有题目
            List<Question> questions = exerciseSetService.getQuestionsByExerciseSetId(exerciseSetId);
            
            response.put("success", true);
            response.put("data", questions);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取题目列表失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 提交答题
     * @param exerciseSetId 习题集ID
     * @param chapterId 章节ID
     * @param answers 答题列表
     * @param request HTTP请求
     * @return 提交结果
     */
    @PostMapping("/{exerciseSetId}/chapters/{chapterId}/answers")
    public ResponseEntity<Map<String, Object>> submitAnswers(
            @PathVariable Long exerciseSetId,
            @PathVariable Long chapterId,
            @RequestBody List<Map<String, Object>> answers,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Long studentId = jwtUtil.getUserIdFromRequest(request);
        if (studentId == null) {
            response.put("success", false);
            response.put("message", "未登录");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // 检查习题集是否存在且已发布
            ExerciseSet exerciseSet = exerciseSetService.getExerciseSetById(exerciseSetId);
            if (exerciseSet == null) {
                response.put("success", false);
                response.put("message", "习题集不存在");
                return ResponseEntity.status(404).body(response);
            }

            if (!"published".equals(exerciseSet.getStatus())) {
                response.put("success", false);
                response.put("message", "习题集未发布");
                return ResponseEntity.status(400).body(response);
            }

            List<Question> questions;
            // 如果章节ID为0，表示获取所有未分配章节的题目
            if (chapterId == 0) {
                questions = exerciseSetService.getQuestionsByExerciseSetId(exerciseSetId)
                        .stream()
                        .filter(q -> q.getChapterId() == null)
                        .collect(Collectors.toList());
            } else {
                // 获取该章节下的所有题目
                questions = exerciseSetService.getQuestionsByExerciseSetIdAndChapterId(exerciseSetId, chapterId);
            }
            
            Set<Long> questionIds = questions.stream().map(Question::getId).collect(Collectors.toSet());
            
            // 校验题目归属
            for (Map<String, Object> answer : answers) {
                Long questionId = Long.valueOf(answer.get("questionId").toString());
                if (!questionIds.contains(questionId)) {
                    response.put("success", false);
                    response.put("message", "题目归属校验失败");
                    return ResponseEntity.status(400).body(response);
                }
            }
            
            // 构造要删除的题目ID列表
            String questionIdsStr = questionIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            
            // 清空该学生在该章节下的历史答题记录
            if (!questionIdsStr.isEmpty()) {
                studentExerciseAnswerMapper.deleteByStudentIdAndQuestionIds(studentId, questionIdsStr);
            }
            
            // 将每道题的答题信息写入学生习题答题表
            List<Long> answerIds = new ArrayList<>();
            for (Map<String, Object> answerData : answers) {
                StudentExerciseAnswer answer = new StudentExerciseAnswer();
                answer.setStudentId(studentId);
                answer.setQuestionId(Long.valueOf(answerData.get("questionId").toString()));
                answer.setAnswerType(Integer.valueOf(answerData.get("answerType").toString()));
                
                if (answerData.containsKey("answer")) {
                    answer.setAnswer(answerData.get("answer").toString());
                }
                
                if (answerData.containsKey("imageUrls")) {
                    // 将图片URL列表转换为JSON字符串存储
                    answer.setImageUrls(answerData.get("imageUrls").toString());
                }
                
                answer.setCreateTime(LocalDateTime.now());
                answer.setUpdateTime(LocalDateTime.now());
                
                studentExerciseAnswerMapper.insert(answer);
                answerIds.add(answer.getId());
            }
            
            response.put("success", true);
            response.put("message", "提交成功");
            response.put("data", answerIds);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "提交答题失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 直接提交习题集答题（跳过章节）
     * @param exerciseSetId 习题集ID
     * @param answers 答题列表
     * @param request HTTP请求
     * @return 提交结果
     */
    @PostMapping("/{exerciseSetId}/direct/answers")
    public ResponseEntity<Map<String, Object>> submitAnswersDirect(
            @PathVariable Long exerciseSetId,
            @RequestBody List<Map<String, Object>> answers,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Long studentId = jwtUtil.getUserIdFromRequest(request);
        if (studentId == null) {
            response.put("success", false);
            response.put("message", "未登录");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // 检查习题集是否存在且已发布
            ExerciseSet exerciseSet = exerciseSetService.getExerciseSetById(exerciseSetId);
            if (exerciseSet == null) {
                response.put("success", false);
                response.put("message", "习题集不存在");
                return ResponseEntity.status(404).body(response);
            }

            if (!"published".equals(exerciseSet.getStatus())) {
                response.put("success", false);
                response.put("message", "习题集未发布");
                return ResponseEntity.status(400).body(response);
            }

            // 获取习题集下的所有题目
            List<Question> questions = exerciseSetService.getQuestionsByExerciseSetId(exerciseSetId);
            Set<Long> questionIds = questions.stream().map(Question::getId).collect(Collectors.toSet());
            
            // 校验题目归属
            for (Map<String, Object> answer : answers) {
                Long questionId = Long.valueOf(answer.get("questionId").toString());
                if (!questionIds.contains(questionId)) {
                    response.put("success", false);
                    response.put("message", "题目归属校验失败");
                    return ResponseEntity.status(400).body(response);
                }
            }
            
            // 构造要删除的题目ID列表
            String questionIdsStr = questionIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            
            // 清空该学生在该习题集下的历史答题记录
            if (!questionIdsStr.isEmpty()) {
                studentExerciseAnswerMapper.deleteByStudentIdAndQuestionIds(studentId, questionIdsStr);
            }
            
            // 将每道题的答题信息写入学生习题答题表
            List<Long> answerIds = new ArrayList<>();
            for (Map<String, Object> answerData : answers) {
                StudentExerciseAnswer answer = new StudentExerciseAnswer();
                answer.setStudentId(studentId);
                answer.setQuestionId(Long.valueOf(answerData.get("questionId").toString()));
                answer.setAnswerType(Integer.valueOf(answerData.get("answerType").toString()));
                
                if (answerData.containsKey("answer")) {
                    answer.setAnswer(answerData.get("answer").toString());
                }
                
                if (answerData.containsKey("imageUrls")) {
                    // 将图片URL列表转换为JSON字符串存储
                    answer.setImageUrls(answerData.get("imageUrls").toString());
                }
                
                answer.setCreateTime(LocalDateTime.now());
                answer.setUpdateTime(LocalDateTime.now());
                
                studentExerciseAnswerMapper.insert(answer);
                answerIds.add(answer.getId());
            }
            
            response.put("success", true);
            response.put("message", "提交成功");
            response.put("data", answerIds);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "提交答题失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}