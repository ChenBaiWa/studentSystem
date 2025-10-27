<template>
  <div class="parsed-question-display">
    <div class="question-item" v-for="(question, index) in questions" :key="index">
      <div class="question-header">
        <span class="question-number">第{{ index + 1 }}题</span>
        <el-tag :type="getQuestionTypeTag(question.type)">
          {{ getQuestionTypeName(question.type) }}
        </el-tag>
      </div>
      
      <div class="question-content">
        <div class="content-text">{{ question.content }}</div>
        
        <!-- 选择题选项 -->
        <div v-if="question.type === 'choice'" class="options-section">
          <div 
            v-for="(option, optIndex) in (question.options || [])" 
            :key="optIndex" 
            class="option-item"
            :class="{ 'correct-answer': isCorrectOption(question, optIndex) }"
          >
            <span class="option-label">{{ String.fromCharCode(65 + optIndex) }}.</span>
            <span class="option-content">{{ option.content }}</span>
          </div>
        </div>
        
        <!-- 填空题和解答题答案 -->
        <div v-if="question.type === 'fill' || question.type === 'solve'" class="answer-section">
          <div class="answer-label">答案：</div>
          <div class="answer-content">{{ question.answer }}</div>
        </div>
      </div>
      
      <div class="question-footer">
        <div class="score-section">
          <span>分值：</span>
          <el-input-number 
            v-model="question.score" 
            :min="1" 
            :max="getMaxScore(question.type)"
            size="small"
            controls-position="right"
          />
          <span class="score-unit">分</span>
        </div>
        
        <div class="action-section">
          <el-button size="small" @click="editQuestion(question)">编辑</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { defineProps, defineEmits } from 'vue'

interface Question {
  type: 'choice' | 'fill' | 'solve'
  content: string
  options?: Array<{ content: string }>
  answer: string
  score?: number
}

const props = defineProps<{
  questions: Question[]
}>()

const emit = defineEmits<{
  (e: 'edit', question: Question): void
}>()

// 获取题目类型名称
const getQuestionTypeName = (type: string) => {
  switch (type) {
    case 'choice': return '选择题'
    case 'fill': return '填空题'
    case 'solve': return '解答题'
    default: return '未知题型'
  }
}

// 获取题目类型标签
const getQuestionTypeTag = (type: string) => {
  switch (type) {
    case 'choice': return 'primary'
    case 'fill': return 'success'
    case 'solve': return 'warning'
    default: return 'info'
  }
}

// 判断是否为正确选项
const isCorrectOption = (question: Question, optionIndex: number) => {
  return question.type === 'choice' && 
         question.answer === String.fromCharCode(65 + optionIndex)
}

// 获取最大分值
const getMaxScore = (type: string) => {
  return type === 'solve' ? 20 : 10
}

// 编辑题目
const editQuestion = (question: Question) => {
  emit('edit', question)
}
</script>

<style scoped>
.parsed-question-display {
  margin-top: 20px;
}

.question-item {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 20px;
  margin-bottom: 20px;
  background-color: #fafafa;
}

.question-item:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.question-number {
  font-weight: bold;
  color: #409eff;
}

.question-content {
  margin-bottom: 20px;
}

.content-text {
  line-height: 1.6;
  margin-bottom: 15px;
  white-space: pre-wrap;
}

.options-section {
  margin-top: 10px;
}

.option-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 8px;
  padding: 8px;
  border-radius: 4px;
}

.option-item.correct-answer {
  background-color: #f0f9eb;
  border: 1px solid #67c23a;
}

.option-label {
  font-weight: bold;
  margin-right: 10px;
  min-width: 20px;
}

.option-content {
  flex: 1;
}

.answer-section {
  margin-top: 15px;
  padding: 10px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.answer-label {
  font-weight: bold;
  margin-bottom: 5px;
}

.answer-content {
  white-space: pre-wrap;
  line-height: 1.5;
}

.question-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
}

.score-section {
  display: flex;
  align-items: center;
  gap: 10px;
}

.score-unit {
  margin-left: 5px;
}

.action-section {
  display: flex;
  gap: 10px;
}
</style>