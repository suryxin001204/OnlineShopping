<template>
  <div class="admin-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">
        <i class="bi bi-people"></i>
        用户管理
      </h1>
      <div class="action-buttons">
        <button class="modern-btn modern-btn-secondary" @click="fetchUsers">
          <i class="bi bi-arrow-clockwise"></i>
          刷新
        </button>
      </div>
    </div>

    <!-- 用户列表表格 -->
    <div class="content-card">
      <div class="table-responsive">
        <table class="modern-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>用户信息</th>
              <th>邮箱</th>
              <th>电话</th>
              <th>角色</th>
              <th>注册时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in users" :key="user.id">
              <td><span class="text-muted">#{{ user.id }}</span></td>
              <td>
                <div class="d-flex align-items-center gap-2">
                  <div class="user-avatar" :style="{ background: getUserColor(user.username) }">
                    {{ user.username.charAt(0).toUpperCase() }}
                  </div>
                  <span class="fw-bold">{{ user.username }}</span>
                </div>
              </td>
              <td>{{ user.email || '-' }}</td>
              <td>{{ user.phone || '-' }}</td>
              <td>
                <span :class="user.role === 'ROLE_ADMIN' || user.role === 'ADMIN' ? 'role-badge-admin' : 'role-badge-user'">
                  {{ formatRole(user.role) }}
                </span>
              </td>
              <td>{{ formatDate(user.createTime) }}</td>
              <td>
                <div class="d-flex gap-2">
                  <button class="action-btn action-btn-primary" @click="editUser(user)">
                    <i class="bi bi-pencil"></i>
                    <span>编辑</span>
                  </button>
                  <button class="action-btn action-btn-danger" @click="confirmDelete(user)">
                    <i class="bi bi-trash"></i>
                    <span>删除</span>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="users.length === 0">
              <td colspan="7">
                <div class="empty-state">
                  <i class="bi bi-person-x"></i>
                  <div>暂无用户数据</div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 编辑用户模态框 -->
    <div class="modal fade" id="userModal" tabindex="-1" aria-hidden="true" ref="userModal">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">编辑用户</h5>
            <button type="button" class="btn-close" @click="closeModal"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="saveUser">
              <div class="mb-3">
                <label class="form-label">用户名</label>
                <input type="text" class="form-control" v-model="currentUser.username" disabled>
                <div class="form-text">用户名不可修改</div>
              </div>
              
              <div class="mb-3">
                <label class="form-label">邮箱</label>
                <input type="email" class="form-control" v-model="currentUser.email">
              </div>

              <div class="mb-3">
                <label class="form-label">电话</label>
                <input type="text" class="form-control" v-model="currentUser.phone">
              </div>

              <div class="mb-3">
                <label class="form-label">地址</label>
                <textarea class="form-control" v-model="currentUser.address" rows="2"></textarea>
              </div>

              <div class="mb-3">
                <label class="form-label">角色</label>
                <select class="form-select" v-model="currentUser.role">
                  <option value="ROLE_USER">普通用户</option>
                  <option value="ROLE_ADMIN">管理员</option>
                </select>
              </div>
            </form>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeModal">取消</button>
            <button type="button" class="btn btn-primary" @click="saveUser">保存</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import request from '../../utils/request'
import { Modal } from 'bootstrap'

export default {
  name: 'UserManagement',
  data() {
    return {
      users: [],
      modalInstance: null,
      currentUser: {
        id: null,
        username: '',
        email: '',
        phone: '',
        address: '',
        role: 'ROLE_USER'
      }
    }
  },
  mounted() {
    this.fetchUsers()
    this.modalInstance = new Modal(this.$refs.userModal)
  },
  methods: {
    async fetchUsers() {
      try {
        const response = await request.get('/users')
        this.users = response.data
      } catch (error) {
        console.error('获取用户列表失败:', error)
        alert('获取用户列表失败')
      }
    },
    formatRole(role) {
      const roleMap = {
        'ROLE_ADMIN': '管理员',
        'ROLE_USER': '普通用户',
        'ADMIN': '管理员',
        'USER': '普通用户'
      }
      return roleMap[role] || role
    },
    getRoleBadgeClass(role) {
      if (role === 'ROLE_ADMIN' || role === 'ADMIN') {
        return 'bg-danger'
      }
      return 'bg-info text-dark'
    },
    formatDate(dateStr) {
      if (!dateStr) return '-'
      return new Date(dateStr).toLocaleString()
    },
    editUser(user) {
      this.currentUser = { ...user }
      this.modalInstance.show()
    },
    closeModal() {
      this.modalInstance.hide()
    },
    async saveUser() {
      try {
        await request.put(`/users/${this.currentUser.id}`, this.currentUser)
        this.closeModal()
        this.fetchUsers()
        alert('更新成功')
      } catch (error) {
        console.error('保存用户失败:', error)
        alert('保存失败: ' + (error.response?.data?.message || error.message))
      }
    },
    async confirmDelete(user) {
      if (user.username === 'admin') {
        alert('不能删除超级管理员账号')
        return
      }
      if (confirm(`确定要删除用户 "${user.username}" 吗？此操作不可恢复！`)) {
        try {
          await request.delete(`/users/${user.id}`)
          this.fetchUsers()
        } catch (error) {
          console.error('删除用户失败:', error)
          alert('删除失败')
        }
      }
    },
    getUserColor(username) {
      const colors = [
        'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
        'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
        'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
        'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
      ]
      const index = username.charCodeAt(0) % colors.length
      return colors[index]
    }
  }
}
</script>

<style scoped>
@import '../../assets/admin-common.css';

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 1rem;
}
</style>
