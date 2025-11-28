<template>
  <div class="product-list">
    <!-- 顶部筛选栏 -->
    <div class="filter-bar shadow-sm mb-4">
      <div class="container-fluid">
        <div class="row align-items-center py-3">
          <div class="col-md-3">
            <h4 class="mb-0">
              <span class="badge bg-primary">{{ filteredProducts.length }}</span> 件商品
            </h4>
          </div>
          <div class="col-md-9">
            <div class="d-flex gap-3 justify-content-end align-items-center">
              <!-- 分类筛选 -->
              <div class="filter-item">
                <select v-model="selectedCategory" class="form-select form-select-lg">
                  <option value="">全部分类</option>
                  <option v-for="category in categories" :key="category.id" :value="category.id">
                    {{ category.name }}
                  </option>
                </select>
              </div>
              
              <!-- 价格排序 -->
              <div class="filter-item">
                <select v-model="sortBy" class="form-select form-select-lg">
                  <option value="">默认排序</option>
                  <option value="price-asc">价格：低到高</option>
                  <option value="price-desc">价格：高到低</option>
                  <option value="stock-desc">库存最多</option>
                </select>
              </div>
              
              <!-- 搜索框 -->
              <div class="filter-item flex-grow-1" style="max-width: 400px;">
                <div class="input-group input-group-lg">
                  <input 
                    v-model="searchKeyword" 
                    type="text" 
                    class="form-control" 
                    placeholder="搜索商品名称..."
                    @keyup.enter="searchProducts"
                  >
                  <button class="btn btn-primary" type="button" @click="searchProducts">
                    搜索
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 商品网格 -->
    <div class="products-grid">
      <div class="row g-4">
        <div class="col-lg-3 col-md-4 col-sm-6" v-for="product in filteredProducts" :key="product.id">
          <div class="product-card-modern">
            <!-- 商品图片 -->
            <div class="product-image-wrapper" @click="$router.push(`/products/${product.id}`)">
              <img 
                :src="product.imageUrl || '/images/placeholder.jpg'" 
                :alt="product.name"
                class="product-image"
              >
              <div class="product-overlay">
                <button class="btn btn-light btn-sm">查看详情</button>
              </div>
              <!-- 缺货标签 -->
              <div v-if="product.stock === 0" class="out-of-stock-badge">
                售罄
              </div>
              <!-- 热销标签 -->
              <div v-else-if="product.stock < 20" class="hot-badge">
                HOT
              </div>
            </div>
            
            <!-- 商品信息 -->
            <div class="product-info">
              <div class="product-category">
                <span class="badge bg-light text-dark">{{ product.categoryName || '未分类' }}</span>
              </div>
              <h5 class="product-title" @click="$router.push(`/products/${product.id}`)">
                {{ product.name }}
              </h5>
              <p class="product-description">
                {{ product.description ? product.description.substring(0, 50) + '...' : '暂无描述' }}
              </p>
              
              <!-- 价格和操作 -->
              <div class="product-footer">
                <div class="price-section">
                  <span class="current-price">¥{{ product.price }}</span>
                  <span class="stock-info">库存: {{ product.stock }}</span>
                </div>
                <button 
                  @click="handleAddToCart(product)" 
                  class="btn-add-cart"
                  :class="{ 'disabled': product.stock === 0 }"
                  :disabled="product.stock === 0"
                >
                  {{ product.stock === 0 ? '售罄' : '加入购物车' }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="filteredProducts.length === 0" class="text-center py-5">
      <div class="text-muted">
        <h4>暂无商品</h4>
        <p>请尝试调整搜索条件或选择其他分类</p>
      </div>
    </div>
  </div>
</template>

<script>
import { mapActions } from 'vuex'
import api from '../../utils/request'

export default {
  name: 'ProductList',
  data() {
    return {
      products: [],
      categories: [],
      selectedCategory: '',
      searchKeyword: '',
      sortBy: ''
    }
  },
  computed: {
    filteredProducts() {
      let filtered = this.products

      // 按分类筛选
      if (this.selectedCategory) {
        filtered = filtered.filter(product => product.categoryId == this.selectedCategory)
      }

      // 按关键词搜索
      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase()
        filtered = filtered.filter(product => 
          product.name.toLowerCase().includes(keyword) ||
          (product.description && product.description.toLowerCase().includes(keyword))
        )
      }

      // 排序
      if (this.sortBy === 'price-asc') {
        filtered = filtered.sort((a, b) => a.price - b.price)
      } else if (this.sortBy === 'price-desc') {
        filtered = filtered.sort((a, b) => b.price - a.price)
      } else if (this.sortBy === 'stock-desc') {
        filtered = filtered.sort((a, b) => b.stock - a.stock)
      }

      return filtered
    }
  },
  async mounted() {
    await this.loadProducts()
    await this.loadCategories()
  },
  watch: {
    selectedCategory() {
      this.filterProducts()
    }
  },
  methods: {
    ...mapActions(['addToCart']),
    async loadProducts() {
      try {
        const response = await api.get('/products')
        this.products = response.data
      } catch (error) {
        console.error('加载商品失败:', error)
      }
    },
    async loadCategories() {
      try {
        const response = await api.get('/categories')
        this.categories = response.data
      } catch (error) {
        console.error('加载分类失败:', error)
      }
    },
    searchProducts() {
      // 搜索逻辑已经在计算属性中处理
    },
    filterProducts() {
      // 筛选逻辑已经在计算属性中处理
    },
    handleAddToCart(product) {
      this.addToCart(product)
      // 显示成功提示
      this.$nextTick(() => {
        const toast = document.createElement('div')
        toast.className = 'toast-notification'
        toast.textContent = `${product.name} 已加入购物车`
        document.body.appendChild(toast)
        setTimeout(() => {
          toast.classList.add('show')
        }, 10)
        setTimeout(() => {
          toast.classList.remove('show')
          setTimeout(() => toast.remove(), 300)
        }, 2000)
      })
    }
  }
}
</script>

<style scoped>
/* 筛选栏样式 */
.filter-bar {
  background: white;
  border-radius: 12px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.filter-item .form-select,
.filter-item .form-control {
  border: 2px solid #e9ecef;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.filter-item .form-select:focus,
.filter-item .form-control:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 0.2rem rgba(102, 126, 234, 0.25);
}

/* 现代商品卡片样式 */
.product-card-modern {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  height: 100%;
  display: flex;
  flex-direction: column;
}

.product-card-modern:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
}

/* 商品图片区域 */
.product-image-wrapper {
  position: relative;
  overflow: hidden;
  cursor: pointer;
  height: 280px;
  background: #f8f9fa;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.product-card-modern:hover .product-image {
  transform: scale(1.1);
}

.product-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.product-image-wrapper:hover .product-overlay {
  opacity: 1;
}

/* 标签 */
.out-of-stock-badge,
.hot-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 6px 12px;
  border-radius: 20px;
  font-weight: 600;
  font-size: 0.85rem;
  z-index: 2;
}

.out-of-stock-badge {
  background: rgba(255, 59, 48, 0.95);
  color: white;
}

.hot-badge {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(255, 107, 107, 0.3);
}

/* 商品信息区域 */
.product-info {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.product-category {
  margin-bottom: 10px;
}

.product-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 10px;
  cursor: pointer;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
  min-height: 2.8em;
}

.product-title:hover {
  color: #667eea;
}

.product-description {
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 15px;
  flex: 1;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 底部价格和按钮 */
.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
}

.price-section {
  display: flex;
  flex-direction: column;
}

.current-price {
  font-size: 1.5rem;
  font-weight: 700;
  color: #ff4757;
  line-height: 1;
}

.stock-info {
  font-size: 0.8rem;
  color: #999;
  margin-top: 4px;
}

.btn-add-cart {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 25px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
}

.btn-add-cart:hover:not(.disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.btn-add-cart.disabled {
  background: #ddd;
  cursor: not-allowed;
}

/* Toast 通知 */
:global(.toast-notification) {
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

:global(.toast-notification.show) {
  opacity: 1;
  transform: translateX(0);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .filter-bar .row {
    flex-direction: column;
  }
  
  .filter-item {
    width: 100% !important;
    max-width: none !important;
    margin-bottom: 10px;
  }
  
  .product-image-wrapper {
    height: 200px;
  }
}

.card-img-top {
  cursor: pointer;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>