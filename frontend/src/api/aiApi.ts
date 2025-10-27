import axios from 'axios'

// 豆包AI API配置
const DOUBAO_API_URL = 'https://ark.cn-beijing.volces.com/api/v3/chat/completions'
// 实际项目中应该从环境变量获取API密钥
const DOUBAO_API_KEY = '5765d599-bfd9-4d21-a745-b092c80ef580'

/**
 * 调用豆包AI解析PDF内容
 * @param pdfUrl PDF文件URL
 * @returns AI解析结果
 */
export const parsePdfWithDoubao = async (pdfUrl: string) => {
  try {
    // 这里应该实现PDF内容提取和AI解析的逻辑
    // 由于前端无法直接处理PDF，实际应该由后端完成
    
    // 模拟AI解析结果
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    // 返回模拟的解析结果
    return {
      success: true,
      data: [
        {
          type: 'choice',
          content: '下列哪个城市是中国的首都？',
          options: ['上海', '广州', '北京', '深圳'],
          answer: '北京',
          explanation: '中国的首都是北京'
        },
        {
          type: 'fill',
          content: '中国的首都是______。',
          answer: '北京',
          explanation: '中国的首都是北京'
        },
        {
          type: 'subjective',
          content: '请简述你对人工智能的理解。',
          answer: '人工智能是计算机科学的一个分支，旨在创建能够执行通常需要人类智能的任务的系统。',
          explanation: '答案合理即可'
        }
      ]
    }
  } catch (error) {
    console.error('豆包AI解析失败:', error)
    throw error
  }
}

/**
 * 解析文本内容并识别题目
 * @param text 文本内容
 * @returns 解析后的题目列表
 */
export const parseTextToQuestions = async (text: string) => {
  try {
    const prompt = `请分析以下文本内容，识别其中的题目并按以下JSON格式返回：

文本内容：
${text}

请按以下格式返回题目列表：
[
  {
    "type": "choice", // 题目类型：choice(选择题)、fill(填空题)、subjective(主观题)
    "content": "题目题干内容",
    "options": ["选项A", "选项B", "选项C", "选项D"], // 仅选择题有此字段
    "answer": "正确答案",
    "score": 5 // 默认分值
  }
]

要求：
1. 严格按照上述JSON格式返回
2. 如果没有识别到题目，返回空数组[]
3. 不要添加任何额外的文本，只返回JSON数组
4. 选择题需要包含options字段
5. 每道题给出合理的默认分值（1-10之间）
`

    const response = await axios.post(
      DOUBAO_API_URL,
      {
        model: 'doubao-seed-1-6-251015',
        messages: [
          {
            content: prompt,
            role: 'user'
          }
        ]
      },
      {
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${DOUBAO_API_KEY}`
        }
      }
    )
    
    return response.data
  } catch (error) {
    console.error('文本解析失败:', error)
    throw error
  }
}