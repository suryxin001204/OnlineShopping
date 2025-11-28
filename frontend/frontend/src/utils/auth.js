import api from './request'

export const auth = {
  // 登录
  async login(credentials) {
    const response = await api.post('/auth/login', credentials)
    return response.data
  },

  // 注册
  async register(userData) {
    const response = await api.post('/auth/register', userData)
    return response.data
  },

  // 获取当前用户信息
  async getCurrentUser() {
    const response = await api.get('/users/me')
    return response.data
  },

  // 更新用户信息
  async updateUser(userId, userData) {
    const response = await api.put(`/users/${userId}`, userData)
    return response.data
  }
}

export default auth