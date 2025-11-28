<template>
  <div class="register-container min-vh-100 d-flex align-items-center justify-content-center bg-light">
    <div class="container">
      <div class="row justify-content-center">
        <div class="col-md-6 col-lg-5">
          <div class="card shadow">
            <div class="card-body p-5">
              <div class="text-center mb-4">
                <h2 class="card-title">用户注册</h2>
                <p class="text-muted">创建您的购物账户</p>
              </div>
              
              <form @submit.prevent="register">
                <div class="mb-3">
                  <label for="username" class="form-label">用户名</label>
                  <input 
                    v-model="form.username"
                    type="text" 
                    class="form-control" 
                    required
                    placeholder="请输入用户名"
                  >
                </div>
                
                <div class="mb-3">
                  <label for="email" class="form-label">邮箱</label>
                  <input 
                    v-model="form.email"
                    type="email" 
                    class="form-control" 
                    required
                    placeholder="请输入邮箱"
                  >
                </div>
                
                <div class="mb-3">
                  <label for="phone" class="form-label">手机号</label>
                  <input 
                    v-model="form.phone"
                    type="tel" 
                    class="form-control" 
                    placeholder="请输入手机号"
                  >
                </div>
                
                <div class="mb-4">
                  <label for="password" class="form-label">密码</label>
                  <input 
                    v-model="form.password"
                    type="password" 
                    class="form-control" 
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
                  {{ loading ? '注册中...' : '注册' }}
                </button>
              </form>
              
              <div class="text-center mt-4">
                <p class="mb-0">
                  已有账号？
                  <router-link to="/login" class="text-decoration-none">立即登录</router-link>
                </p>
              </div>
              
              <div v-if="error" class="alert alert-danger mt-3 mb-0">
                {{ error }}
              </div>
              
              <div v-if="success" class="alert alert-success mt-3 mb-0">
                注册成功！正在跳转到登录页面...
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import auth from '../utils/auth'

export default {
  name: 'Register',
  data() {
    return {
      form: {
        username: '',
        email: '',
        phone: '',
        password: ''
      },
      loading: false,
      error: '',
      success: false
    }
  },
  methods: {
    async register() {
      this.loading = true
      this.error = ''
      this.success = false
      
      try {
        await auth.register(this.form)
        this.success = true
        
        // 2秒后跳转到登录页面
        setTimeout(() => {
          this.$router.push('/login')
        }, 2000)
        
      } catch (error) {
        this.error = error.response?.data?.message || '注册失败，请稍后重试'
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.register-container {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.card {
  border: none;
  border-radius: 1rem;
}

.btn-primary {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border: none;
}
</style>