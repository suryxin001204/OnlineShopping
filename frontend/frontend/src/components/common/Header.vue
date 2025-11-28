<template>
  <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="container">
      <router-link class="navbar-brand" to="/">
        🛍️ 网上购物系统
      </router-link>
      
      <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
        <span class="navbar-toggler-icon"></span>
      </button>
      
      <div class="collapse navbar-collapse" id="navbarNav">
        <ul class="navbar-nav me-auto">
          <li class="nav-item">
            <router-link class="nav-link" to="/">首页</router-link>
          </li>
          <li class="nav-item">
            <router-link class="nav-link" to="/products">商品</router-link>
          </li>
          <li v-if="isAuthenticated" class="nav-item">
            <router-link class="nav-link" to="/cart">
              购物车
              <span v-if="cartItemCount > 0" class="badge bg-danger ms-1">
                {{ cartItemCount }}
              </span>
            </router-link>
          </li>
          <li v-if="isAuthenticated" class="nav-item">
            <router-link class="nav-link" to="/orders">我的订单</router-link>
          </li>
          <li v-if="isAdmin" class="nav-item">
            <router-link class="nav-link" to="/admin/dashboard">管理后台</router-link>
          </li>
        </ul>
        
        <ul class="navbar-nav">
          <template v-if="isAuthenticated">
            <li class="nav-item dropdown" ref="userDropdown">
              <a class="nav-link dropdown-toggle" href="#" role="button" @click.prevent="toggleDropdown" style="cursor: pointer;">
                👤 {{ currentUser.username }}
              </a>
              <ul class="dropdown-menu" :class="{ show: dropdownOpen }">
                <li>
                  <router-link class="dropdown-item" to="/profile" @click="closeDropdown">个人中心</router-link>
                </li>
                <li><hr class="dropdown-divider"></li>
                <li>
                  <a class="dropdown-item" href="#" @click.prevent="handleLogout" style="cursor: pointer;">退出登录</a>
                </li>
              </ul>
            </li>
          </template>
          <template v-else>
            <li class="nav-item">
              <router-link class="nav-link" to="/login">登录</router-link>
            </li>
            <li class="nav-item">
              <router-link class="nav-link" to="/register">注册</router-link>
            </li>
          </template>
        </ul>
      </div>
    </div>
  </nav>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'

export default {
  name: 'Header',
  data() {
    return {
      dropdownOpen: false
    }
  },
  computed: {
    ...mapGetters(['isAuthenticated', 'isAdmin', 'currentUser', 'cartItemCount'])
  },
  mounted() {
    // 点击页面其他地方时关闭下拉菜单
    document.addEventListener('click', this.handleClickOutside)
  },
  beforeUnmount() {
    document.removeEventListener('click', this.handleClickOutside)
  },
  methods: {
    ...mapActions(['logout']),
    toggleDropdown(event) {
      event.stopPropagation()
      this.dropdownOpen = !this.dropdownOpen
      console.log('下拉菜单状态:', this.dropdownOpen)
    },
    closeDropdown() {
      this.dropdownOpen = false
    },
    handleClickOutside(event) {
      if (this.$refs.userDropdown && !this.$refs.userDropdown.contains(event.target)) {
        this.dropdownOpen = false
      }
    },
    handleLogout() {
      console.log('退出登录')
      this.closeDropdown()
      this.logout()
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.navbar-brand {
  font-size: 1.5rem;
  font-weight: bold;
}

.nav-link {
  font-weight: 500;
}

.dropdown-menu {
  border: none;
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.15);
}
</style>