import axios from 'axios'
import http from './http'

// 豆包AI API配置
const DOUBAO_API_URL = 'https://ark.cn-beijing.volces.com/api/v3/chat/completions'
// 实际项目中应该从环境变量获取API密钥
const DOUBAO_API_KEY = '5765d599-bfd9-4d21-a745-b092c80ef580'

/**
 * 调用豆包AI解析图片内容
 * @param imageUrl 图片URL
 * @param prompt 提示词
 * @returns AI解析结果
 */
export const analyzeImageWithDoubao = async (imageUrl: string, prompt: string) => {
  try {
    const response = await axios.post(
      DOUBAO_API_URL,
      {
        model: 'doubao-seed-1-6-251015',
        messages: [
          {
            content: [
              {
                image_url: {
                  url: imageUrl
                },
                type: 'image_url'
              },
              {
                text: prompt,
                type: 'text'
              }
            ],
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
    console.error('豆包AI解析失败:', error)
    throw error
  }
}

/**
 * 上传PDF文件并转换为图片
 * @param file PDF文件
 * @returns 上传结果
 */
export const uploadPdfAndConvertToImage = async (file: File) => {
  try {
    // 创建FormData对象
    const formData = new FormData()
    formData.append('file', file)
    
    // 这里需要替换为实际的后端API地址
    // 目前返回模拟数据
    // 在实际应用中，你需要实现一个后端接口来处理PDF上传和转换
    
    // 模拟上传过程
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    return {
      success: true,
      imageUrl: 'https://ark-project.tos-cn-beijing.ivolces.com/images/view.jpeg',
      fileId: 'mock-file-id'
    }
  } catch (error) {
    console.error('PDF上传失败:', error)
    throw error
  }
}

/**
 * 解析PDF内容并转换为题目格式
 * @param file PDF文件
 * @returns 解析结果
 */
export const parsePdfToQuestions = async (file: File) => {
  try {
    // 1. 上传PDF并转换为图片
    const uploadResult = await uploadPdfAndConvertToImage(file)
    
    if (!uploadResult.success) {
      throw new Error('PDF上传失败')
    }
    
    // 2. 使用豆包AI解析图片内容
    const prompt = `请识别图片中的题目内容，并按以下JSON格式返回：
    [
      {
        "type": "single_choice", // 题目类型：single_choice(单选)、multiple_choice(多选)、true_false(判断)、fill_blank(填空)、essay(问答)
        "content": "题目题干内容",
        "options": ["选项A", "选项B", "选项C", "选项D"], // 如果是选择题则有此字段
        "answer": "正确答案",
        "explanation": "题目解析"
      }
    ]
    
    注意：
    1. 严格按照上述JSON格式返回
    2. 如果没有识别到题目，返回空数组
    3. 不要添加任何额外的文本，只返回JSON数组
    `
    
    const aiResult = await analyzeImageWithDoubao(uploadResult.imageUrl, prompt)
    
    // 3. 处理解析结果
    if (aiResult.choices && aiResult.choices.length > 0) {
      const content = aiResult.choices[0].message?.content || '[]'
      // 尝试解析AI返回的JSON
      try {
        const questions = JSON.parse(content)
        return {
          success: true,
          questions: questions,
          rawContent: content
        }
      } catch (parseError) {
        // 如果JSON解析失败，返回原始内容
        return {
          success: true,
          questions: [],
          rawContent: content
        }
      }
    } else {
      throw new Error('AI解析无结果')
    }
  } catch (error) {
    console.error('PDF解析失败:', error)
    throw error
  }
}