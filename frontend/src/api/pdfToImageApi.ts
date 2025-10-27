import axios from 'axios'

// PDF转图片API配置
const PDF_TO_IMAGE_API_URL = 'https://pdf-api.pdfai.cn/v1/pdf/pdf_to_image'
const APP_KEY = 'app_key_test' // 在实际项目中应该从环境变量获取

// 创建axios实例，设置更长的超时时间（例如30秒）
const pdfApi = axios.create({
  timeout: 30000 // 30秒超时
})

/**
 * 将PDF转换为图片
 * @param pdfUrl PDF文件URL（必须是公网可访问的URL）
 * @param page 页面范围，例如 '1-N' 表示所有页面，默认为 '1-N'
 * @param quality 图片质量，可选值: 'low', 'medium', 'high'，默认为 'high'
 * @returns 转换后的图片URL数组
 */
export const convertPdfToImage = async (
  pdfUrl: string, 
  page: string = '1-N', 
  quality: 'low' | 'medium' | 'high' = 'high'
) => {
  try {
    // 参数验证
    if (!pdfUrl) {
      throw new Error('PDF URL不能为空')
    }
    
    if (!isValidUrl(pdfUrl)) {
      throw new Error('PDF URL格式不正确')
    }
    
    if (!['low', 'medium', 'high'].includes(quality)) {
      throw new Error('图片质量参数必须是 low、medium 或 high 之一')
    }
    
    // 验证page参数格式
    if (!isValidPageFormat(page)) {
      throw new Error('页面范围格式不正确，应为如 "1-N" 或 "1,2,3" 的格式')
    }
    
    console.log('发送PDF转图片请求:', { pdfUrl, page, quality })
    
    const response = await pdfApi.post(
      PDF_TO_IMAGE_API_URL,
      {
        app_key: APP_KEY,
        input: pdfUrl,
        page: page,
        quality: quality
      },
      {
        headers: {
          'accept': 'application/json',
          'Content-Type': 'application/json'
        }
      }
    )

    console.log('PDF转图片响应:', response.data)
    
    if (response.data.code === 200) {
      return {
        success: true,
        fileUrls: response.data.data.file_url
      }
    } else {
      throw new Error(`${response.data.code}: ${response.data.code_msg || 'PDF转图片失败'}`)
    }
  } catch (error: any) {
    console.error('PDF转图片失败:', error.response?.data || error.message)
    
    if (error.response) {
      // 服务器返回了错误响应
      switch (error.response.status) {
        case 422:
          throw new Error('请求参数验证失败，请检查PDF URL、页面范围和图片质量参数')
        case 400:
          throw new Error('请求参数错误，请检查输入参数')
        case 401:
          throw new Error('认证失败，请检查app_key是否正确')
        case 404:
          throw new Error('PDF文件未找到，请检查URL是否正确')
        case 500:
          throw new Error('服务器内部错误，请稍后重试')
        default:
          throw new Error(`请求失败 (${error.response.status}): ${error.response.statusText}`)
      }
    } else if (error.request) {
      // 请求已发出但没有收到响应
      throw new Error('网络请求失败，请检查网络连接')
    } else {
      // 其他错误
      throw new Error(error.message || 'PDF转图片失败')
    }
  }
}

/**
 * 验证URL格式是否有效
 * @param url URL字符串
 * @returns 是否为有效URL
 */
function isValidUrl(url: string): boolean {
  try {
    new URL(url)
    return true
  } catch {
    return false
  }
}

/**
 * 验证页面范围格式是否有效
 * @param page 页面范围字符串
 * @returns 是否为有效格式
 */
function isValidPageFormat(page: string): boolean {
  // 支持的格式示例: "1-N", "1,2,3", "1-5", "1"
  const pageRegex = /^(\d+(-\d+)?)(,\d+(-\d+)?)*$|^(\d+-[Nn])$/
  return pageRegex.test(page)
}

export default {
  convertPdfToImage
}