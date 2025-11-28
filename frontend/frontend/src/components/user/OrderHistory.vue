<template>
  <div class="order-history">
    <div class="row mb-4">
      <div class="col-12">
        <h2>我的订单</h2>
        <p class="text-muted">查看您的订单历史</p>
      </div>
    </div>

    <div v-if="loading" class="loading-container">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">加载中...</span>
      </div>
      <p class="mt-3 text-muted">正在加载订单历史...</p>
    </div>

    <div v-else-if="error" class="alert alert-danger" role="alert">
      <h4 class="alert-heading">加载失败</h4>
      <p>{{ error }}</p>
      <button @click="loadOrders" class="btn btn-outline-danger">重试</button>
    </div>

    <div v-else-if="orders.length === 0" class="empty-state">
      <div class="empty-orders">
        <div class="mb-4">
          <span style="font-size: 4rem;">📦</span>
        </div>
        <h4 class="text-muted mb-3">暂无订单</h4>
        <p class="text-muted mb-4">快去选购一些商品吧！</p>
        <router-link to="/products" class="btn btn-primary btn-lg">
          🛍️ 去购物
        </router-link>
      </div>
    </div>

    <div v-else class="row">
      <div class="col-12">
        <div class="card shadow-sm">
          <div class="card-body p-0">
            <div v-for="order in orders" :key="order.id" class="order-item border-bottom p-4" :class="{'animate-fadeIn': true}">
              <div class="row mb-3">
                <div class="col-md-6">
                  <h5 class="mb-1">订单号: {{ order.orderNumber }}</h5>
                  <p class="text-muted mb-0">
                    下单时间: {{ new Date(order.createTime).toLocaleString() }}
                  </p>
                </div>
                <div class="col-md-6 text-md-end">
                  <span :class="getStatusBadgeClass(order.status)" class="badge">
                    {{ getStatusText(order.status) }}
                  </span>
                  <p class="h5 text-primary mb-0 mt-2">¥{{ order.totalAmount }}</p>
                </div>
              </div>

              <div class="order-items">
                <div v-for="item in order.orderItems" :key="item.id" class="order-item-product row align-items-center py-2">
                  <div class="col-md-1">
                    <img 
                      :src="item.imageUrl || 'https://via.placeholder.com/50'" 
                      :alt="item.productName" 
                      class="img-fluid rounded"
                      style="height: 50px; width: 50px; object-fit: cover; background: #f0f0f0;"
                      @error="e => e.target.src = 'https://via.placeholder.com/50?text=商品'"
                    >
                  </div>
                  <div class="col-md-5">
                    <h6 class="mb-0">{{ item.productName }}</h6>
                  </div>
                  <div class="col-md-2 text-center">
                    <span class="text-muted">¥{{ item.price }}</span>
                  </div>
                  <div class="col-md-2 text-center">
                    <span class="text-muted">×{{ item.quantity }}</span>
                  </div>
                  <div class="col-md-2 text-end">
                    <strong class="text-primary">¥{{ item.subtotal }}</strong>
                  </div>
                </div>
              </div>

              <div class="row mt-3">
                <div class="col-md-8">
                  <p class="mb-1"><strong>收货地址:</strong> {{ order.shippingAddress }}</p>
                  <p class="mb-0"><strong>支付方式:</strong> {{ order.paymentMethod }}</p>
                </div>
                <div class="col-md-4 text-md-end">
                  <button 
                    v-if="order.status === 'PENDING'" 
                    @click="payOrder(order.id)" 
                    class="btn btn-success btn-sm me-2"
                  >
                    💳 立即支付
                  </button>
                  <button v-if="order.status === 'PENDING'" @click="cancelOrder(order.id)" class="btn btn-outline-danger btn-sm">
                    取消订单
                  </button>
                  <button v-if="order.status === 'DELIVERED'" class="btn btn-outline-success btn-sm">
                    确认收货
                  </button>
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
import api from '../../utils/request'

export default {
  name: 'OrderHistory',
  data() {
    return {
      orders: [],
      loading: false,
      error: null
    }
  },
  async mounted() {
    console.log('OrderHistory component mounted')
    console.log('当前用户:', this.$store.state.user)
    console.log('是否管理员:', this.$store.getters.isAdmin)
    await this.loadOrders()
  },
  methods: {
    async loadOrders() {
      this.loading = true
      this.error = null
      try {
        console.log('开始加载订单历史')
        // 管理员使用 /all 端点获取所有订单，普通用户获取自己的订单
        const endpoint = this.$store.getters.isAdmin ? '/orders/all' : '/orders'
        console.log('使用端点:', endpoint)
        const response = await api.get(endpoint)
        console.log('订单数据:', response.data)
        this.orders = response.data || []
      } catch (error) {
        console.error('加载订单失败:', error)
        console.error('错误详情:', error.response)
        
        if (error.response && error.response.status === 401) {
          this.error = '请先登录'
          setTimeout(() => {
            this.$router.push('/login')
          }, 2000)
        } else if (error.response && error.response.status === 403) {
          this.error = '没有权限访问订单信息'
        } else if (error.response) {
          this.error = `服务器错误: ${error.response.status}`
        } else {
          this.error = '网络连接失败，请检查网络设置'
        }
      } finally {
        this.loading = false
      }
    },
    getStatusBadgeClass(status) {
      const statusClasses = {
        'PENDING': 'bg-warning',
        'PAID': 'bg-info',
        'SHIPPED': 'bg-primary',
        'DELIVERED': 'bg-success',
        'CANCELLED': 'bg-danger'
      }
      return statusClasses[status] || 'bg-secondary'
    },
    getStatusText(status) {
      const statusTexts = {
        'PENDING': '待支付',
        'PAID': '已支付',
        'SHIPPED': '已发货',
        'DELIVERED': '已送达',
        'CANCELLED': '已取消'
      }
      return statusTexts[status] || status
    },
    handleImageError(event) {
      // 图片加载失败时使用占位图
      event.target.src = '/images/placeholder.jpg'
    },
    async payOrder(orderId) {
      if (confirm('确认支付该订单？\n\n实际应用中会跳转到支付平台，这里模拟直接支付成功。')) {
        try {
          this.loading = true
          await api.post(`/orders/${orderId}/pay`)
          
          // 显示成功提示
          const toast = document.createElement('div')
          toast.className = 'payment-toast'
          toast.innerHTML = `
            <div class="toast-content">
              <span class="toast-icon">✅</span>
              <span class="toast-text">支付成功！</span>
            </div>
          `
          document.body.appendChild(toast)
          
          setTimeout(() => {
            toast.classList.add('show')
          }, 10)
          
          setTimeout(() => {
            toast.classList.remove('show')
            setTimeout(() => toast.remove(), 300)
          }, 2000)
          
          await this.loadOrders()
        } catch (error) {
          console.error('支付订单失败:', error)
          alert('支付失败: ' + (error.response?.data?.message || error.message || '未知错误'))
        } finally {
          this.loading = false
        }
      }
    },
    async cancelOrder(orderId) {
      if (confirm('确定要取消这个订单吗？')) {
        try {
          await api.put(`/orders/${orderId}/status?status=CANCELLED`)
          alert('订单取消成功')
          await this.loadOrders()
        } catch (error) {
          alert('取消订单失败')
        }
      }
    }
  }
}
</script>

<style scoped>
/* 防止内容闪烁 */
.order-history {
  min-height: 500px;
}

/* 加载状态 */
.loading-container {
  text-align: center;
  padding: 100px 0;
  min-height: 400px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  min-height: 400px;
}

/* 订单卡片 */
.card {
  border: none;
  border-radius: 12px;
  overflow: hidden;
}

/* 订单项动画 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fadeIn {
  animation: fadeIn 0.3s ease-in-out;
}

.order-item {
  transition: all 0.3s ease;
  background: white;
}

.order-item:hover {
  background-color: #f8f9fa;
}

.order-item:last-child {
  border-bottom: none !important;
}

/* 商品列表 */
.order-items {
  background: #fafafa;
  border-radius: 8px;
  padding: 15px;
  margin: 15px 0;
}

.order-item-product {
  border-bottom: 1px solid #e9ecef;
  padding: 10px 0;
  transition: background 0.2s;
}

.order-item-product:hover {
  background: white;
  border-radius: 6px;
  padding-left: 10px;
  padding-right: 10px;
}

.order-item-product:last-child {
  border-bottom: none;
}

/* 商品图片 */
.order-item-product img {
  border: 2px solid #e9ecef;
  transition: all 0.3s ease;
}

.order-item-product:hover img {
  border-color: #667eea;
  transform: scale(1.05);
}

/* 状态徽章 */
.badge {
  font-size: 0.85rem;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-weight: 600;
}

/* 空订单 */
.empty-orders {
  max-width: 400px;
  margin: 0 auto;
}

/* 按钮样式 */
.btn-outline-danger:hover,
.btn-outline-success:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

/* 支付成功提示 */
:global(.payment-toast) {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%) scale(0.8);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 30px 50px;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(102, 126, 234, 0.5);
  z-index: 9999;
  opacity: 0;
  transition: all 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

:global(.payment-toast.show) {
  opacity: 1;
  transform: translate(-50%, -50%) scale(1);
}

:global(.payment-toast .toast-content) {
  display: flex;
  align-items: center;
  gap: 15px;
}

:global(.payment-toast .toast-icon) {
  font-size: 2.5rem;
  animation: bounce 0.6s ease-in-out;
}

:global(.payment-toast .toast-text) {
  font-size: 1.5rem;
  font-weight: 600;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

/* 响应式 */
@media (max-width: 768px) {
  .order-item-product {
    font-size: 0.9rem;
  }
  
  .order-item-product img {
    height: 40px !important;
    width: 40px !important;
  }
}
</style>