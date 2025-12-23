<template>
  <div class="login-container min-vh-100 d-flex align-items-center justify-content-center bg-light">
    <div class="container">
      <div class="row justify-content-center">
        <div class="col-md-6 col-lg-4">
          <div class="card shadow">
            <div class="card-body p-5">
              <div class="text-center mb-4">
                <h2 class="card-title">用户登录</h2>
                <p class="text-muted">欢迎回到网上购物系统</p>
              </div>
              
              <form @submit.prevent="login">
                <div class="alert alert-info" role="alert">
                  演示账号：
                  <div class="small mt-2">
                    <div><strong>管理员</strong>：用户名 <code>admin</code>，密码 <code>admin123</code></div>
                    <div><strong>普通用户</strong>：用户名 <code>user1</code>，密码 <code>user123</code></div>
                    <button type="button" class="btn btn-sm btn-outline-primary mt-2 me-2" @click="fillAdmin">填充管理员</button>
                    <button type="button" class="btn btn-sm btn-outline-secondary mt-2" @click="fillUser">填充普通用户</button>
                  </div>
                </div>
                <div class="mb-3">
                  <label for="username" class="form-label">用户名</label>
                  <input 
                    v-model="form.username"
                    type="text" 
                    class="form-control" 
                    id="username"
                    required
                    placeholder="请输入用户名"
                  >
                </div>
                
                <div class="mb-4">
                  <label for="password" class="form-label">密码</label>
                  <input 
                    v-model="form.password"
                    type="password" 
                    class="form-control" 
                    id="password"
                    required
                    placeholder="请输入密码"
                  >
                </div>
                
                <button 
                  type="submit" 
                  class="btn btn-primary w-100 py-2"
                  :disabled="loading"
                >
                  <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
                  {{ loading ? '登录中...' : '登录' }}
                </button>
              </form>
              
              <div class="text-center mt-4">
                <p class="mb-0">
                  没有账号？
                  <router-link to="/register" class="text-decoration-none">立即注册</router-link>
                </p>
              </div>
              
              <div v-if="error" class="alert alert-danger mt-3 mb-0">
                {{ error }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapActions } from 'vuex'
import auth from '../utils/auth'

export default {
  name: 'Login',
  data() {
    return {
      form: {
        username: '',
        password: ''
      },
      loading: false,
      error: ''
    }
  },
  mounted() {
    console.log('Login view mounted')
    // 如果有残留的token但用户到了登录页，说明可能需要清理
    // 但router guard会阻止已登录用户访问登录页，除非手动清除localStorage
  },
  methods: {
    ...mapActions(['login']),
    fillAdmin() {
      this.form.username = 'admin'
      this.form.password = 'admin123'
      console.log('已填充管理员账号')
    },
    fillUser() {
      this.form.username = 'user1'
      this.form.password = 'user123'
      console.log('已填充普通用户账号')
    },
    async login() {
      if (!this.form.username || !this.form.password) {
        this.error = '请输入用户名和密码'
        return
      }
      
      this.loading = true
      this.error = ''
      
      try {
        console.log('开始登录...', this.form)
        
        const response = await auth.login({
          username: this.form.username,
          password: this.form.password
        })
        
        console.log('登录响应:', response)
        
        if (!response || !response.token) {
          throw new Error('登录失败：未返回token')
        }
        
        // 存储token
        localStorage.setItem('token', response.token)
        
        // 构造用户信息
        const user = {
          username: this.form.username,
          role: response.authorities && response.authorities.length > 0 
            ? response.authorities[0].authority 
            : (this.form.username === 'admin' ? 'ROLE_ADMIN' : 'ROLE_USER')
        }
        
        console.log('用户信息:', user)
        
        // 保存到Vuex
        this.$store.dispatch('login', {
          token: response.token,
          user: user
        })
        
        // 先同步本地购物车到后端
        await this.$store.dispatch('syncLocalCartToBackend')
        
        // 然后从后端加载购物车数据（确保数据一致）
        await this.$store.dispatch('loadCartFromBackend')
        
        // 根据用户角色跳转
        if (user.role === 'ROLE_ADMIN' || user.role === 'ADMIN') {
          this.$router.push('/admin')
        } else {
          this.$router.push('/user')
        }
        
      } catch (error) {
        console.error('登录错误详情:', error)
        console.error('错误响应:', error.response)
        
        let errorMessage = '登录失败'
        if (error.response) {
          errorMessage = error.response.data?.message || error.response.data?.error || `HTTP ${error.response.status}`
        } else if (error.message) {
          errorMessage = error.message
        }
        
        this.error = errorMessage
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.login-container {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.card {
  border: none;
  border-radius: 1rem;
}

.card-title {
  color: #333;
  font-weight: 600;
} 

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  font-weight: 500;
}

.form-control {
  border-radius: 0.5rem;
  padding: 0.75rem 1rem;
}

.form-control:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 0.2rem rgba(102, 126, 234, 0.25);
}
</style>