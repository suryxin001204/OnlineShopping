<template>
  <div class="personal-center">
    <div class="row">
      <div class="col-12">
        <h2>个人中心</h2>
        <p class="text-muted">管理您的个人信息和账户设置</p>
      </div>
    </div>

    <div class="row">
      <!-- 左侧：头像和基本信息 -->
      <div class="col-lg-4">
        <div class="card mb-4">
          <div class="card-body text-center">
            <h5 class="card-title mb-4">我的头像</h5>
            
            <!-- 头像区域 -->
            <div class="avatar-section mb-4">
              <div class="avatar-wrapper">
                <img 
                  :src="avatarUrl" 
                  alt="用户头像" 
                  class="user-avatar"
                  @error="handleAvatarError"
                >
                <div class="avatar-overlay" @click="triggerFileInput">
                  <i class="bi bi-camera-fill"></i>
                  <span>更换头像</span>
                </div>
              </div>
              <input 
                ref="fileInput" 
                type="file" 
                accept="image/*" 
                style="display: none" 
                @change="handleFileChange"
              >
            </div>
            
            <p class="text-muted small">支持JPG、PNG格式<br>文件大小不超过2MB</p>
            
            <hr class="my-4">
            
            <div class="text-start">
              <p class="mb-2"><strong>用户名:</strong> {{ profile.username }}</p>
              <p class="mb-0"><strong>角色:</strong> {{ getRoleName() }}</p>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧：个人信息表单 -->
      <div class="col-lg-8">
        <div class="card">
          <div class="card-header">
            <h5 class="card-title mb-0">个人信息</h5>
          </div>
          <div class="card-body">
            <form @submit.prevent="updateProfile">
              <div class="row">
                <div class="col-md-6 mb-3">
                  <label for="username" class="form-label">用户名</label>
                  <input 
                    v-model="profile.username"
                    type="text" 
                    class="form-control" 
                    id="username"
                    required
                    disabled
                  >
                </div>
                <div class="col-md-6 mb-3">
                  <label for="email" class="form-label">邮箱</label>
                  <input 
                    v-model="profile.email"
                    type="email" 
                    class="form-control" 
                    id="email"
                    required
                  >
                </div>
              </div>

              <div class="row">
                <div class="col-md-6 mb-3">
                  <label for="phone" class="form-label">手机号</label>
                  <input 
                    v-model="profile.phone"
                    type="tel" 
                    class="form-control" 
                    id="phone"
                  >
                </div>
                <div class="col-md-6 mb-3">
                  <label for="address" class="form-label">地址</label>
                  <input 
                    v-model="profile.address"
                    type="text" 
                    class="form-control" 
                    id="address"
                  >
                </div>
              </div>

              <div class="mb-3">
                <label class="form-label">注册时间</label>
                <p class="form-control-plaintext">
                  {{ new Date(profile.createTime).toLocaleString() }}
                </p>
              </div>

              <button type="submit" class="btn btn-primary" :disabled="loading">
                <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
                {{ loading ? '保存中...' : '保存更改' }}
              </button>
            </form>
          </div>
        </div>
      </div>

      <!-- 账户统计 -->
      <div class="col-lg-4">
        <div class="card mb-4">
          <div class="card-header">
            <h5 class="card-title mb-0">账户统计</h5>
          </div>
          <div class="card-body">
            <div class="stats-item d-flex justify-content-between align-items-center mb-3">
              <span>订单总数</span>
              <strong class="text-primary">{{ orderCount }}</strong>
            </div>
            <div class="stats-item d-flex justify-content-between align-items-center mb-3">
              <span>购物车商品</span>
              <strong class="text-primary">{{ cartItemCount }}</strong>
            </div>
            <div class="stats-item d-flex justify-content-between align-items-center">
              <span>账户状态</span>
              <span class="badge bg-success">正常</span>
            </div>
          </div>
        </div>

        <div class="card">
          <div class="card-header">
            <h5 class="card-title mb-0">快捷操作</h5>
          </div>
          <div class="card-body">
            <div class="d-grid gap-2">
              <router-link to="/orders" class="btn btn-outline-primary">
                📦 查看订单
              </router-link>
              <router-link to="/cart" class="btn btn-outline-success">
                🛒 购物车
              </router-link>
              <router-link to="/products" class="btn btn-outline-info">
                🛍️ 继续购物
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import auth from '../../utils/auth'
import api from '../../utils/request'

export default {
  name: 'PersonalCenter',
  data() {
    return {
      profile: {
        username: '',
        email: '',
        phone: '',
        address: '',
        createTime: '',
        avatarUrl: ''
      },
      orderCount: 0,
      loading: false,
      avatarUrl: ''
    }
  },
  computed: {
    ...mapGetters(['currentUser', 'cartItemCount'])
  },
  async mounted() {
    await this.loadProfile()
    await this.loadOrderCount()
    // 从服务器加载最新的用户信息（包括头像）
    await this.reloadUserProfile()
  },
  methods: {
    getDefaultAvatar() {
      // 使用用户名首字母生成默认头像
      const initial = this.profile.username?.charAt(0).toUpperCase() || 'U'
      return `https://ui-avatars.com/api/?name=${initial}&background=667eea&color=fff&size=200`
    },
    handleAvatarError(event) {
      event.target.src = this.getDefaultAvatar()
    },
    triggerFileInput() {
      this.$refs.fileInput.click()
    },
    async handleFileChange(event) {
      const file = event.target.files[0]
      if (!file) return
      
      console.log('开始上传头像:', file.name, file.type, file.size)
      
      // 验证文件类型
      if (!file.type.startsWith('image/')) {
        alert('请选择图片文件')
        return
      }
      
      // 验证文件大小（2MB）
      if (file.size > 2 * 1024 * 1024) {
        alert('图片大小不能超过2MB')
        return
      }
      
      const formData = new FormData()
      formData.append('file', file)
      
      try {
        this.loading = true
        console.log('发送上传请求到 /api/upload/avatar')
        
        // 为文件上传设置更长的超时时间（30秒）
        const response = await api.post('/upload/avatar', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          },
          timeout: 30000  // 30秒超时
        })
        
        console.log('上传成功，响应数据:', response.data)
        
        const newAvatarUrl = response.data.avatarUrl
        console.log('新头像URL:', newAvatarUrl)
        
        this.avatarUrl = 'http://localhost:8080' + newAvatarUrl
        this.profile.avatarUrl = newAvatarUrl
        
        console.log('设置新头像URL为:', this.avatarUrl)
        
        // 重新加载用户信息以获取最新头像
        console.log('重新加载用户信息...')
        await this.reloadUserProfile()
        console.log('用户信息重新加载完成，当前avatarUrl:', this.avatarUrl)
        
        // 显示成功提示
        const toast = document.createElement('div')
        toast.className = 'upload-toast success'
        toast.textContent = '头像上传成功！'
        document.body.appendChild(toast)
        
        setTimeout(() => {
          toast.classList.add('show')
        }, 10)
        
        setTimeout(() => {
          toast.classList.remove('show')
          setTimeout(() => toast.remove(), 300)
        }, 2000)
        
        // 清空file input
        event.target.value = ''
        
      } catch (error) {
        console.error('上传失败:', error)
        console.error('错误详情:', error.response?.data)
        alert('头像上传失败: ' + (error.response?.data || error.message))
      } finally {
        this.loading = false
      }
    },
    getRoleName() {
      const role = this.currentUser?.role
      if (role === 'ROLE_ADMIN' || role === 'ADMIN') return '管理员'
      return '普通用户'
    },
    async loadProfile() {
      this.profile = { ...this.currentUser }
    },
    async reloadUserProfile() {
      try {
        const response = await api.get('/users/me')
        this.profile = response.data
        this.avatarUrl = response.data.avatarUrl 
          ? 'http://localhost:8080' + response.data.avatarUrl 
          : this.getDefaultAvatar()
        
        // 同时更新store中的用户信息
        this.$store.commit('setUser', response.data)
      } catch (error) {
        console.error('重新加载用户信息失败:', error)
      }
    },
    async loadOrderCount() {
      try {
        const response = await api.get('/orders')
        this.orderCount = response.data.length
      } catch (error) {
        console.error('加载订单数量失败:', error)
      }
    },
    async updateProfile() {
      this.loading = true
      try {
        await auth.updateUser(this.currentUser.id, this.profile)
        alert('个人信息更新成功')
        // 更新store中的用户信息
        this.$store.commit('setUser', this.profile)
      } catch (error) {
        alert('更新失败: ' + (error.response?.data?.message || '未知错误'))
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.stats-item {
  padding: 0.5rem 0;
  border-bottom: 1px solid #e9ecef;
}

.stats-item:last-child {
  border-bottom: none;
}

.card {
  border: none;
  box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
}

.btn {
  border-radius: 0.375rem;
}

/* 头像区域样式 */
.avatar-section {
  display: flex;
  justify-content: center;
  align-items: center;
}

.avatar-wrapper {
  position: relative;
  width: 150px;
  height: 150px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.avatar-wrapper:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

.user-avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  color: white;
  font-size: 0.9rem;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.avatar-overlay i {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

/* 上传成功提示 */
:global(.upload-toast) {
  position: fixed;
  top: 80px;
  right: 20px;
  background: #28a745;
  color: white;
  padding: 15px 25px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 9999;
  opacity: 0;
  transform: translateX(400px);
  transition: all 0.3s ease;
}

:global(.upload-toast.show) {
  opacity: 1;
  transform: translateX(0);
}

:global(.upload-toast.success) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
</style>