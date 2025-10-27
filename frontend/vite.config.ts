import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
      'pdfjs-dist': 'pdfjs-dist/build/pdf.min.js'
    }
  },
  optimizeDeps: {
    // 若豆包SDK为CommonJS格式，
    include: ['doubao-sdk'] // 替换为实际SDK包名
    },
  server: {
    port: 3000,
    proxy: {
      '/doubao-api': {
        target: 'https://api.doubao.com', // 豆包API基础地址（需确认官方文档）​
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/doubao-api/, '')
      },
      '/api/lenovo': {
        target: 'https://dsp.lenovo.com.cn',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api\/lenovo/, '')
      },
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/assignments': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/assignments/, '/api/assignments')
      },
      '/student-assignments': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/student-assignments/, '/api/student-assignments')
      },
      '/classes': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/classes/, '/api/classes')
      },
      '/chapters': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/chapters/, '/api/chapters')
      },
      '/textbooks': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/textbooks/, '/api/textbooks')
      },
      '/grades': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/grades/, '/api/grades')
      }
    }
  }
})
