<template>
  <div class="sidebar bg-light border-end" style="width: 250px; min-height: calc(100vh - 56px);">
    <div class="p-3">
      <h5 class="text-muted">管理菜单</h5>
    </div>
    <nav class="nav flex-column">
      <router-link class="nav-link" to="/admin/dashboard">
        📊 仪表板
      </router-link>
      <router-link class="nav-link" to="/admin/users">
        👥 用户管理
      </router-link>
      <router-link class="nav-link" to="/admin/categories">
        📁 分类管理
      </router-link>
      <router-link class="nav-link" to="/admin/products">
        🛒 商品管理
      </router-link>
      <router-link class="nav-link" to="/admin/orders">
        📦 订单管理
      </router-link>
    </nav>
  </div>
</template>

<script>
export default {
  name: 'Sidebar'
}
</script>

<style scoped>
.sidebar {
  position: fixed;
  top: 56px;
  left: 0;
  z-index: 100;
}

.nav-link {
  padding: 0.75rem 1.5rem;
  color: #495057;
  border-left: 3px solid transparent;
  transition: all 0.3s;
}

.nav-link:hover {
  background-color: #e9ecef;
  color: #007bff;
}

.nav-link.router-link-active {
  background-color: #e3f2fd;
  color: #007bff;
  border-left-color: #007bff;
  font-weight: 500;
}
</style>