<template>
  <div class="admin-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="d-flex align-items-center gap-3">
        <h1 class="page-title">
          <i class="bi bi-box-seam"></i>
          商品管理
        </h1>
        <div v-if="filterCategoryId" class="filter-badge">
          <i class="bi bi-funnel"></i>
          当前筛选: {{ getCategoryName(filterCategoryId) }}
          <button class="btn-close btn-close-sm ms-2" @click="clearFilter" style="font-size: 0.7rem;"></button>
        </div>
      </div>
      <div class="action-buttons">
        <button class="modern-btn modern-btn-secondary" @click="fetchProducts">
          <i class="bi bi-arrow-clockwise"></i>
          刷新
        </button>
        <button class="modern-btn modern-btn-primary" @click="openAddModal">
          <i class="bi bi-plus-lg"></i>
          添加商品
        </button>
      </div>
    </div>

    <!-- 商品列表表格 -->
    <div class="content-card">
      <div class="table-responsive">
        <table class="modern-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>图片</th>
              <th>商品信息</th>
              <th>分类</th>
              <th>价格</th>
              <th>库存</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="product in products" :key="product.id">
              <td><span class="text-muted">#{{ product.id }}</span></td>
              <td>
                <img :src="product.imageUrl || 'https://via.placeholder.com/50'" alt="商品图片" class="table-img">
              </td>
              <td>
                <div class="fw-bold mb-1">{{ product.name }}</div>
                <div class="product-desc">{{ product.description || '暂无描述' }}</div>
              </td>
              <td>{{ getCategoryName(product.categoryId) }}</td>
              <td><span class="price-text">¥{{ product.price.toFixed(2) }}</span></td>
              <td>
                <span :class="product.stock < 10 ? 'stock-low' : 'stock-normal'">
                  {{ product.stock }}
                </span>
              </td>
              <td>
                <span class="status-badge" :class="product.status ? 'status-active' : 'status-inactive'">
                  {{ product.status ? '上架' : '下架' }}
                </span>
              </td>
              <td>
                <div class="d-flex gap-2">
                  <button class="action-btn action-btn-primary" @click="editProduct(product)">
                    <i class="bi bi-pencil"></i>
                    <span>编辑</span>
                  </button>
                  <button class="action-btn action-btn-danger" @click="confirmDelete(product)">
                    <i class="bi bi-trash"></i>
                    <span>删除</span>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="products.length === 0">
              <td colspan="8">
                <div class="empty-state">
                  <i class="bi bi-inbox"></i>
                  <div>暂无商品数据</div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 编辑/添加商品模态框 -->
    <div class="modal fade" id="productModal" tabindex="-1" aria-hidden="true" ref="productModal">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">{{ isEditing ? '编辑商品' : '添加商品' }}</h5>
            <button type="button" class="btn-close" @click="closeModal"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="saveProduct">
              <div class="row mb-3">
                <div class="col-md-6">
                  <label class="form-label">商品名称</label>
                  <input type="text" class="form-control" v-model="currentProduct.name" required>
                </div>
                <div class="col-md-6">
                  <label class="form-label">所属分类</label>
                  <select class="form-select" v-model="currentProduct.categoryId" required>
                    <option value="" disabled>请选择分类</option>
                    <option v-for="category in categories" :key="category.id" :value="category.id">
                      {{ category.name }}
                    </option>
                  </select>
                </div>
              </div>

              <div class="row mb-3">
                <div class="col-md-6">
                  <label class="form-label">价格 (¥)</label>
                  <input type="number" class="form-control" v-model="currentProduct.price" step="0.01" min="0" required>
                </div>
                <div class="col-md-6">
                  <label class="form-label">库存数量</label>
                  <input type="number" class="form-control" v-model="currentProduct.stock" min="0" required>
                </div>
              </div>

              <div class="mb-3">
                <label class="form-label">商品图片URL</label>
                <input type="text" class="form-control" v-model="currentProduct.imageUrl" placeholder="/images/example.jpg">
                <div class="form-text">请输入图片路径，例如 /images/iphone14.jpg</div>
              </div>

              <div class="mb-3">
                <label class="form-label">商品描述</label>
                <textarea class="form-control" v-model="currentProduct.description" rows="3"></textarea>
              </div>

              <div class="mb-3 form-check">
                <input type="checkbox" class="form-check-input" id="statusCheck" v-model="currentProduct.status">
                <label class="form-check-label" for="statusCheck">立即上架</label>
              </div>
            </form>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeModal">取消</button>
            <button type="button" class="btn btn-primary" @click="saveProduct">保存</button>
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
  name: 'ProductManagement',
  data() {
    return {
      products: [],
      categories: [],
      modalInstance: null,
      isEditing: false,
      currentProduct: {
        id: null,
        name: '',
        description: '',
        price: 0,
        stock: 0,
        imageUrl: '',
        categoryId: null,
        status: true
      },
      filterCategoryId: null
    }
  },
  async mounted() {
    await this.fetchCategories()
    
    // 检查路由参数是否有分类ID
    const categoryId = this.$route.query.categoryId
    if (categoryId) {
      this.filterCategoryId = parseInt(categoryId)
    }
    
    this.fetchProducts()
    this.modalInstance = new Modal(this.$refs.productModal)
  },
  methods: {
    async fetchProducts() {
      try {
        let url = '/products'
        if (this.filterCategoryId) {
          url = `/products/category/${this.filterCategoryId}`
        }
        const response = await request.get(url)
        this.products = response.data
      } catch (error) {
        console.error('获取商品列表失败:', error)
        alert('获取商品列表失败')
      }
    },
    clearFilter() {
      this.filterCategoryId = null
      this.$router.replace({ query: {} })
      this.fetchProducts()
    },
    async fetchCategories() {
      try {
        const response = await request.get('/categories')
        this.categories = response.data
      } catch (error) {
        console.error('获取分类列表失败:', error)
      }
    },
    getCategoryName(categoryId) {
      const category = this.categories.find(c => c.id === categoryId)
      return category ? category.name : '未知分类'
    },
    openAddModal() {
      this.isEditing = false
      this.currentProduct = {
        name: '',
        description: '',
        price: 0,
        stock: 0,
        imageUrl: '',
        categoryId: this.categories.length > 0 ? this.categories[0].id : null,
        status: true
      }
      this.modalInstance.show()
    },
    editProduct(product) {
      this.isEditing = true
      this.currentProduct = { ...product }
      this.modalInstance.show()
    },
    closeModal() {
      this.modalInstance.hide()
    },
    async saveProduct() {
      try {
        if (this.isEditing) {
          await request.put(`/products/${this.currentProduct.id}`, this.currentProduct)
        } else {
          await request.post('/products', this.currentProduct)
        }
        this.closeModal()
        this.fetchProducts()
        alert(this.isEditing ? '更新成功' : '添加成功')
      } catch (error) {
        console.error('保存商品失败:', error)
        alert('保存失败: ' + (error.response?.data?.message || error.message))
      }
    },
    async confirmDelete(product) {
      if (confirm(`确定要删除商品 "${product.name}" 吗？`)) {
        try {
          await request.delete(`/products/${product.id}`)
          this.fetchProducts()
        } catch (error) {
          console.error('删除商品失败:', error)
          alert('删除失败')
        }
      }
    }
  }
}
</script>

<style scoped>
@import '../../assets/admin-common.css';
</style>
