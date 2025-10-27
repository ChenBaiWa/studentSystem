import axios from 'axios'
import { convertPdfToImage } from './pdfToImageApi'

// 豆包AI API配置
const DOUBAO_API_URL = 'https://ark.cn-beijing.volces.com/api/v3/chat/completions'
const DOUBAO_API_KEY = '5765d599-bfd9-4d21-a745-b092c80ef580' // 在实际项目中应该从环境变量获取

// 创建axios实例
const doubaoApi = axios.create({
  baseURL: DOUBAO_API_URL,
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${DOUBAO_API_KEY}`
  }
})

// AI图片解析函数
export const parseImageWithDoubao = async (imageUrl: string, question: string = "图片主要讲了什么?") => {
  try {
    // 验证图片URL
    if (!imageUrl) {
      throw new Error('图片URL不能为空')
    }
    
    const response = await doubaoApi.post('', {
      model: "doubao-seed-1-6-251015",
      messages: [
        {
          content: [
            {
              image_url: {
                url: imageUrl
              },
              type: "image_url"
            },
            {
              text: question,
              type: "text"
            }
          ],
          role: "user"
        }
      ]
    })

    return response.data
  } catch (error: any) {
    console.error('调用豆包API失败:', error.response?.data || error.message)
    
    if (error.response) {
      throw new Error(`豆包API调用失败 (${error.response.status}): ${error.response.statusText}`)
    } else if (error.request) {
      throw new Error('网络请求失败，请检查网络连接')
    } else {
      throw new Error(error.message || '调用豆包API失败')
    }
  }
}

// AI PDF解析函数
export const parsePdfWithDoubao = async (pdfUrl: string, question: string = "PDF文档主要讲了什么?") => {
  try {
    // 验证PDF URL
    if (!pdfUrl) {
      throw new Error('PDF URL不能为空')
    }
    
    const response = await doubaoApi.post('', {
      model: "doubao-seed-1-6-251015",
      messages: [
        {
          content: [
            {
              pdf_url: {
                url: pdfUrl
              },
              type: "pdf_url"
            },
            {
              text: question,
              type: "text"
            }
          ],
          role: "user"
        }
      ]
    })

    return response.data
  } catch (error: any) {
    console.error('调用豆包API失败:', error.response?.data || error.message)
    
    if (error.response) {
      throw new Error(`豆包API调用失败 (${error.response.status}): ${error.response.statusText}`)
    } else if (error.request) {
      throw new Error('网络请求失败，请检查网络连接')
    } else {
      throw new Error(error.message || '调用豆包API失败')
    }
  }
}

// 文件上传函数
export const uploadFileToDoubao = async (file: File) => {
  try {
    // 检查文件
    if (!file) {
      throw new Error('文件不能为空')
    }
    
    // 创建表单数据
    const formData = new FormData();
    formData.append('file', file);
    formData.append('fileName', file.name);

    // 使用fetch进行文件上传，因为axios处理文件上传可能有兼容性问题
    const response = await fetch('https://ark.cn-beijing.volces.com/api/v3/chat/completions', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${DOUBAO_API_KEY}`
        // 注意：不要设置Content-Type，让浏览器自动设置multipart/form-data
      },
      body: formData
    });

    if (!response.ok) {
      throw new Error(`上传失败: ${response.statusText}`);
    }

    const result = await response.json();
    return result;
  } catch (error: any) {
    console.error('文件上传失败:', error.message);
    throw new Error(error.message || '文件上传失败');
  }
}

/**
 * 调用豆包AI解析图片内容
 * @param imageUrl 图片URL
 * @param prompt 提示词
 * @returns AI解析结果
 */
export const analyzeImageWithDoubao = async (imageUrl: string, prompt: string) => {
  try {
    // 验证参数
    if (!imageUrl) {
      throw new Error('图片URL不能为空')
    }
    
    if (!prompt) {
      throw new Error('提示词不能为空')
    }
    
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
  } catch (error: any) {
    console.error('豆包AI解析失败:', error.response?.data || error.message)
    
    if (error.response) {
      throw new Error(`豆包AI解析失败 (${error.response.status}): ${error.response.statusText}`)
    } else if (error.request) {
      throw new Error('网络请求失败，请检查网络连接')
    } else {
      throw new Error(error.message || '豆包AI解析失败')
    }
  }
}

/**
 * 解析PDF内容并转换为题目格式（使用PDF转图片功能）
 * @param pdfUrl PDF文件URL
 * @returns 解析结果
 */
export const parsePdfToQuestions = async (pdfUrl: string) => {
  try {
    // 验证PDF URL
    if (!pdfUrl) {
      throw new Error('PDF URL不能为空')
    }
    
    // 1. 将PDF转换为图片
    const convertResult = await convertPdfToImage(pdfUrl)
    
    if (!convertResult.success) {
      throw new Error('PDF转图片失败')
    }
    
    // 2. 收集所有图片的分析结果
    const allQuestions = []
    
    // 3. 对每张图片使用豆包AI进行解析
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
    
    // 对每张图片进行分析
    for (const imageUrl of convertResult.fileUrls) {
      try {
        const aiResult = await analyzeImageWithDoubao(imageUrl, prompt)
        
        // 处理解析结果
        if (aiResult.choices && aiResult.choices.length > 0) {
          const content = aiResult.choices[0].message?.content || '[]'
          // 尝试解析AI返回的JSON
          try {
            const questions = JSON.parse(content)
            if (Array.isArray(questions)) {
              allQuestions.push(...questions)
            }
          } catch (parseError) {
            console.warn('单张图片JSON解析失败:', parseError)
            // 如果JSON解析失败，跳过这张图片
          }
        }
      } catch (imageError: any) {
        console.warn(`分析图片失败 (${imageUrl}):`, imageError.message)
        // 如果单张图片分析失败，继续处理下一张
      }
    }
    
    return {
      success: true,
      questions: allQuestions,
      imageUrls: convertResult.fileUrls
    }
  } catch (error: any) {
    console.error('PDF解析失败:', error.message)
    throw new Error(error.message || 'PDF解析失败')
  }
}

// 直接解析PDF并转换为题目（使用豆包的PDF解析功能）
export const parsePdfToQuestionsDirect = async (pdfUrl: string) => {
  try {
    // 验证PDF URL
    if (!pdfUrl) {
      throw new Error('PDF URL不能为空')
    }
    
    // 使用豆包AI直接解析PDF内容
    const prompt = `请识别PDF中的题目内容，并按以下JSON格式返回：
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

    const aiResult = await doubaoApi.post('', {
      model: "doubao-seed-1-6-251015",
      messages: [
        {
          content: [
            {
              pdf_url: {
                url: pdfUrl
              },
              type: "pdf_url"
            },
            {
              text: prompt,
              type: "text"
            }
          ],
          role: "user"
        }
      ]
    })

    // 处理解析结果
    if (aiResult.data.choices && aiResult.data.choices.length > 0) {
      const content = aiResult.data.choices[0].message?.content || '[]'
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
  } catch (error: any) {
    console.error('PDF解析失败:', error.response?.data || error.message)
    
    if (error.response) {
      throw new Error(`豆包API调用失败 (${error.response.status}): ${error.response.statusText}`)
    } else if (error.request) {
      throw new Error('网络请求失败，请检查网络连接')
    } else {
      throw new Error(error.message || 'PDF解析失败')
    }
  }
}