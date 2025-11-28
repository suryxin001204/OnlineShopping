<template>
  <div class="admin-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">
        <i class="bi bi-tags"></i>
        分类管理
      </h1>
      <div class="action-buttons">
        <button class="modern-btn modern-btn-secondary" @click="fetchCategories">
          <i class="bi bi-arrow-clockwise"></i>
          刷新
        </button>
        <button class="modern-btn modern-btn-primary" @click="openAddModal">
          <i class="bi bi-plus-lg"></i>
          添加分类
        </button>
      </div>
    </div>

    <!-- 分类列表表格 -->
    <div class="content-card">
      <div class="table-responsive">
        <table class="modern-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>分类名称</th>
              <th>描述</th>
              <th>父分类</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="category in categories" :key="category.id">
              <td><span class="text-muted">#{{ category.id }}</span></td>
              <td>
                <div class="d-flex align-items-center gap-2">
                  <div class="category-icon">
                    <i class="bi bi-tag-fill"></i>
                  </div>
                  <span class="fw-bold">{{ category.name }}</span>
                </div>
              </td>
              <td><span class="text-muted">{{ category.description || '-' }}</span></td>
              <td>{{ category.parentName || '顶级分类' }}</td>
              <td>
                <span class="status-badge" :class="category.status ? 'status-active' : 'status-inactive'">
                  {{ category.status ? '启用' : '禁用' }}
                </span>
              </td>
              <td>
                <div class="d-flex gap-2">
                  <button class="action-btn action-btn-info" @click="viewProducts(category)" title="查看商品">
                    <i class="bi bi-box-seam"></i>
                    <span>商品</span>
                  </button>
                  <button class="action-btn action-btn-primary" @click="editCategory(category)" title="编辑">
                    <i class="bi bi-pencil"></i>
                    <span>编辑</span>
                  </button>
                  <button class="action-btn action-btn-danger" @click="confirmDelete(category)" title="删除">
                    <i class="bi bi-trash"></i>
                    <span>删除</span>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="categories.length === 0">
              <td colspan="6">
                <div class="empty-state">
                  <i class="bi bi-tags"></i>
                  <div>暂无分类数据</div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 编辑/添加分类模态框 -->
    <div class="modal fade" id="categoryModal" tabindex="-1" aria-hidden="true" ref="categoryModal">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">{{ isEditing ? '编辑分类' : '添加分类' }}</h5>
            <button type="button" class="btn-close" @click="closeModal"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="saveCategory">
              <div class="mb-3">
                <label class="form-label">分类名称</label>
                <input type="text" class="form-control" v-model="currentCategory.name" required>
              </div>
              
              <div class="mb-3">
                <label class="form-label">父分类</label>
                <select class="form-select" v-model="currentCategory.parentId">
                  <option :value="null">无 (顶级分类)</option>
                  <option v-for="cat in parentOptions" :key="cat.id" :value="cat.id">
                    {{ cat.name }}
                  </option>
                </select>
              </div>

              <div class="mb-3">
                <label class="form-label">描述</label>
                <textarea class="form-control" v-model="currentCategory.description" rows="3"></textarea>
              </div>

              <div class="mb-3 form-check">
                <input type="checkbox" class="form-check-input" id="statusCheck" v-model="currentCategory.status">
                <label class="form-check-label" for="statusCheck">启用</label>
              </div>
            </form>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeModal">取消</button>
            <button type="button" class="btn btn-primary" @click="saveCategory">保存</button>
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
  name: 'CategoryManagement',
  data() {
    return {
      categories: [],
      modalInstance: null,
      isEditing: false,
      currentCategory: {
        id: null,
        name: '',
        description: '',
        parentId: null,
        status: true
      }
    }
  },
  computed: {
    // 过滤掉当前编辑的分类及其子分类，防止循环引用（简单起见，只过滤自己）
    parentOptions() {
      if (!this.isEditing) return this.categories;
      return this.categories.filter(c => c.id !== this.currentCategory.id);
    }
  },
  mounted() {
    this.fetchCategories()
    this.modalInstance = new Modal(this.$refs.categoryModal)
  },
  methods: {
    async fetchCategories() {
      try {
        const response = await request.get('/categories/all')
        this.categories = response.data
      } catch (error) {
        console.error('获取分类列表失败:', error)
        const status = error.response ? error.response.status : '未知'
        const message = error.response?.data?.message || error.message
        alert(`获取分类列表失败 (状态码: ${status}): ${message}\n\n请确保后端服务已重启且数据库已初始化。`)
      }
    },
    openAddModal() {
      this.isEditing = false
      this.currentCategory = {
        name: '',
        description: '',
        parentId: null,
        status: true
      }
      this.modalInstance.show()
    },
    editCategory(category) {
      this.isEditing = true
      this.currentCategory = { ...category }
      // parentId might be missing in the object if it's null, ensure it's handled
      if (!this.currentCategory.parentId) this.currentCategory.parentId = null;
      this.modalInstance.show()
    },
    closeModal() {
      this.modalInstance.hide()
    },
    async saveCategory() {
      try {
        const payload = {
            name: this.currentCategory.name,
            description: this.currentCategory.description,
            status: this.currentCategory.status,
            parent: this.currentCategory.parentId ? { id: this.currentCategory.parentId } : null
        }

        if (this.isEditing) {
          payload.id = this.currentCategory.id;
          await request.put(`/categories/${this.currentCategory.id}`, payload)
        } else {
          await request.post('/categories', payload)
        }
        this.closeModal()
        this.fetchCategories()
        alert(this.isEditing ? '更新成功' : '添加成功')
      } catch (error) {
        console.error('保存分类失败:', error)
        alert('保存失败: ' + (error.response?.data?.message || error.message))
      }
    },
    async confirmDelete(category) {
      if (confirm(`确定要删除分类 "${category.name}" 吗？`)) {
        try {
          await request.delete(`/categories/${category.id}`)
          this.fetchCategories()
        } catch (error) {
          console.error('删除分类失败:', error)
          alert('删除失败')
        }
      }
    },
    viewProducts(category) {
      this.$router.push({
        path: '/admin/products',
        query: { categoryId: category.id }
      })
    },
  }
}
</script>

<style scoped>
@import '../../assets/admin-common.css';

.category-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1rem;
}
</style>
