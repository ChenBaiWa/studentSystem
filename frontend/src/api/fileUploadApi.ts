import http from './http'

/**
 * 上传文件到服务器
 * @param file 要上传的文件
 * @returns 上传结果，包含文件访问URL
 */
export const uploadFile = async (file: File) => {
  try {
    // 创建FormData对象
    const formData = new FormData()
    formData.append('file', file)
    
    // 发送上传请求
    const response = await http.post('/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    return response.data
  } catch (error: any) {
    console.error('文件上传失败:', error)
    throw new Error(error.response?.data?.message || error.message || '文件上传失败')
  }
}

/**
 * 上传PDF文件并获取公网访问URL
 * @param file PDF文件
 * @returns 文件访问URL
 */
export const uploadPdfFile = async (file: File) => {
  try {
    // 验证文件类型
    if (file.type !== 'application/pdf') {
      throw new Error('只能上传PDF文件')
    }
    
    // 验证文件大小（10MB限制）
    if (file.size > 10 * 1024 * 1024) {
      throw new Error('文件大小不能超过10MB')
    }
    
    // 上传文件
    const result = await uploadFile(file)
    
    // 返回文件访问URL
    // 检查后端是否返回了有效的响应
    if (result && result.success) {
      // 对于PDF上传解析功能，我们直接返回结果，由调用方处理后续业务流程
      return result;
    } else {
      throw new Error(result?.message || '文件上传失败')
    }
  } catch (error: any) {
    console.error('PDF文件上传失败:', error)
    throw new Error(error.message || 'PDF文件上传失败')
  }
}

export default {
  uploadFile,
  uploadPdfFile
}