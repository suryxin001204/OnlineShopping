<template>
  <div class="user-dashboard">
    <!-- 轮播Banner区 -->
    <div class="row mb-4">
      <div class="col-12">
        <div id="mainCarousel" class="carousel slide shadow-lg" data-bs-ride="carousel">
          <div class="carousel-indicators">
            <button type="button" data-bs-target="#mainCarousel" data-bs-slide-to="0" class="active"></button>
            <button type="button" data-bs-target="#mainCarousel" data-bs-slide-to="1"></button>
            <button type="button" data-bs-target="#mainCarousel" data-bs-slide-to="2"></button>
          </div>
          <div class="carousel-inner rounded">
            <div class="carousel-item active">
              <div class="banner-slide bg-gradient-primary">
                <div class="banner-content">
                  <h2>🎉 限时优惠</h2>
                  <p>全场商品满299减50，满599减120</p>
                  <router-link to="/products" class="btn btn-light btn-lg">立即抢购</router-link>
                </div>
              </div>
            </div>
            <div class="carousel-item">
              <div class="banner-slide bg-gradient-success">
                <div class="banner-content">
                  <h2>📱 新品上市</h2>
                  <p>最新款智能手机，性能强劲价格优惠</p>
                  <router-link to="/products" class="btn btn-light btn-lg">查看详情</router-link>
                </div>
              </div>
            </div>
            <div class="carousel-item">
              <div class="banner-slide bg-gradient-warning">
                <div class="banner-content">
                  <h2>🎁 会员专享</h2>
                  <p>注册即送50元优惠券，更多福利等你来</p>
                  <router-link to="/products" class="btn btn-light btn-lg">了解更多</router-link>
                </div>
              </div>
            </div>
          </div>
          <button class="carousel-control-prev" type="button" data-bs-target="#mainCarousel" data-bs-slide="prev">
            <span class="carousel-control-prev-icon"></span>
          </button>
          <button class="carousel-control-next" type="button" data-bs-target="#mainCarousel" data-bs-slide="next">
            <span class="carousel-control-next-icon"></span>
          </button>
        </div>
      </div>
    </div>

    <div class="row">
      <!-- 统计卡片 -->
      <div class="col-md-3 mb-4">
        <div class="card text-white bg-primary h-100">
          <div class="card-body">
            <div class="d-flex align-items-center">
              <div class="flex-grow-1">
                <h4 class="card-title">{{ cartItemCount }}</h4>
                <p class="card-text">购物车商品</p>
              </div>
              <div class="flex-shrink-0">
                <i class="fs-1">🛒</i>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="col-md-3 mb-4">
        <div class="card text-white bg-success h-100">
          <div class="card-body">
            <div class="d-flex align-items-center">
              <div class="flex-grow-1">
                <h4 class="card-title">{{ orderCount }}</h4>
                <p class="card-text">我的订单</p>
              </div>
              <div class="flex-shrink-0">
                <i class="fs-1">📦</i>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="col-md-3 mb-4">
        <div class="card text-white bg-warning h-100">
          <div class="card-body">
            <div class="d-flex align-items-center">
              <div class="flex-grow-1">
                <h4 class="card-title">¥{{ cartTotal }}</h4>
                <p class="card-text">购物车总额</p>
              </div>
              <div class="flex-shrink-0">
                <i class="fs-1">💰</i>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="col-md-3 mb-4">
        <div class="card text-white bg-info h-100">
          <div class="card-body">
            <div class="d-flex align-items-center">
              <div class="flex-grow-1">
                <h4 class="card-title">{{ favoriteCount }}</h4>
                <p class="card-text">收藏商品</p>
              </div>
              <div class="flex-shrink-0">
                <i class="fs-1">❤️</i>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 推荐商品 -->
    <div class="row mt-4">
      <div class="col-12">
        <div class="card">
          <div class="card-header">
            <h5 class="card-title mb-0">🔥 热门推荐</h5>
          </div>
          <div class="card-body">
            <div class="row">
              <div class="col-md-3 mb-3" v-for="product in recommendedProducts" :key="product.id">
                <div class="card h-100 product-card">
                  <img :src="product.imageUrl || '/images/placeholder.jpg'" class="card-img-top" :alt="product.name" style="height: 200px; object-fit: cover;">
                  <div class="card-body d-flex flex-column">
                    <h6 class="card-title">{{ product.name }}</h6>
                    <p class="card-text text-muted small flex-grow-1">{{ product.description }}</p>
                    <div class="d-flex justify-content-between align-items-center">
                      <span class="h6 text-primary mb-0">¥{{ product.price }}</span>
                      <button @click="addToCart(product)" class="btn btn-sm btn-outline-primary">
                        加入购物车
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'
import api from '../../utils/request'

export default {
  name: 'UserDashboard',
  data() {
    return {
      recommendedProducts: [],
      orderCount: 0,
      favoriteCount: 0
    }
  },
  computed: {
    ...mapGetters(['currentUser', 'cartItemCount', 'cartTotal'])
  },
  async mounted() {
    await this.loadRecommendedProducts()
    await this.loadUserStats()
  },
  methods: {
    ...mapActions(['addToCart']),
    async loadRecommendedProducts() {
      try {
        const response = await api.get('/products')
        this.recommendedProducts = response.data.slice(0, 4)
      } catch (error) {
        console.error('加载推荐商品失败:', error)
      }
    },
    async loadUserStats() {
      try {
        const response = await api.get('/orders')
        this.orderCount = response.data.length
        // 这里可以添加获取收藏商品数量的逻辑
        this.favoriteCount = 3 // 示例数据
      } catch (error) {
        console.error('加载用户统计失败:', error)
      }
    }
  }
}
</script>

<style scoped>
.user-dashboard {
  padding: 0;
}

/* 轮播Banner样式 */
.banner-slide {
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
}

.bg-gradient-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.bg-gradient-success {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.bg-gradient-warning {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.banner-content {
  text-align: center;
  color: white;
  padding: 40px;
}

.banner-content h2 {
  font-size: 3rem;
  font-weight: bold;
  margin-bottom: 20px;
  text-shadow: 2px 2px 4px rgba(0,0,0,0.2);
}

.banner-content p {
  font-size: 1.5rem;
  margin-bottom: 30px;
  opacity: 0.95;
}

/* 统计卡片样式 */
.card {
  border: none;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  border-radius: 12px;
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 15px rgba(0, 0, 0, 0.2);
}

/* 商品卡片样式 */
.product-card {
  transition: all 0.3s ease;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  overflow: hidden;
}

.product-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
  border-color: #667eea;
}

.product-card img {
  transition: transform 0.3s ease;
}

.product-card:hover img {
  transform: scale(1.05);
}

/* 快捷操作按钮 */
.quick-actions .card {
  cursor: pointer;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.quick-actions .card:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .banner-slide {
    height: 300px;
  }
  
  .banner-content h2 {
    font-size: 2rem;
  }
  
  .banner-content p {
    font-size: 1rem;
  }
}
</style>