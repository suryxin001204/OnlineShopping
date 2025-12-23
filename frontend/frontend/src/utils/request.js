import axios from 'axios'
import store from '../store'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000 // 增加超时时间到10秒，支持文件上传
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    const token = store.state.token
    // 只有当 token 存在、非空字符串且长度大于20（JWT通常很长）时才添加 Authorization 头
    // 避免发送 "Bearer null" 或格式错误的短 token
    if (token && typeof token === 'string' && token.trim() !== '' && token.length > 20) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    return response
  },
  error => {
    if (error.response?.status === 401) {
      store.dispatch('logout')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default api