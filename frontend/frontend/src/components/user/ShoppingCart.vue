<template>
  <div class="shopping-cart">
    <div class="row">
      <div class="col-12">
        <h2>购物车</h2>
        <p class="text-muted">查看和管理您的购物车商品</p>
      </div>
    </div>

    <div v-if="cartItems.length === 0" class="text-center py-5">
      <div class="empty-cart">
        <div class="mb-4">
          <span style="font-size: 4rem;">🛒</span>
        </div>
        <h4 class="text-muted mb-3">购物车为空</h4>
        <p class="text-muted mb-4">快去添加一些心仪的商品吧！</p>
        <router-link to="/products" class="btn btn-primary btn-lg">
          🛍️ 去购物
        </router-link>
      </div>
    </div>

    <div v-else class="row">
      <!-- 购物车商品列表 -->
      <div class="col-lg-8">
        <div class="card">
          <div class="card-header">
            <h5 class="card-title mb-0">购物车商品 ({{ cartItems.length }})</h5>
          </div>
          <div class="card-body p-0">
            <div v-for="item in cartItems" :key="item.productId" class="cart-item border-bottom p-3">
              <div class="row align-items-center">
                <div class="col-md-2">
                  <img 
                    :src="item.productImage || '/images/placeholder.jpg'" 
                    :alt="item.productName" 
                    class="img-fluid rounded"
                    style="height: 80px; object-fit: cover;"
                  >
                </div>
                <div class="col-md-4">
                  <h6 class="mb-1">{{ item.productName }}</h6>
                  <p class="text-muted small mb-0">¥{{ item.productPrice }}</p>
                </div>
                <div class="col-md-3">
                  <div class="quantity-controls d-flex align-items-center">
                    <button 
                      @click="updateQuantity(item.productId, item.quantity - 1)" 
                      class="btn btn-outline-secondary btn-sm"
                      :disabled="item.quantity <= 1"
                    >
                      -
                    </button>
                    <span class="mx-3">{{ item.quantity }}</span>
                    <button 
                      @click="updateQuantity(item.productId, item.quantity + 1)" 
                      class="btn btn-outline-secondary btn-sm"
                    >
                      +
                    </button>
                  </div>
                </div>
                <div class="col-md-2">
                  <strong class="text-primary">¥{{ (item.productPrice * item.quantity).toFixed(2) }}</strong>
                </div>
                <div class="col-md-1">
                  <button 
                    @click="removeItem(item.productId)" 
                    class="btn btn-outline-danger btn-sm"
                  >
                    🗑️
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="mt-3">
          <button @click="clearCart" class="btn btn-outline-secondary">
            清空购物车
          </button>
        </div>
      </div>

      <!-- 订单摘要 -->
      <div class="col-lg-4">
        <div class="card">
          <div class="card-header">
            <h5 class="card-title mb-0">订单摘要</h5>
          </div>
          <div class="card-body">
            <div class="d-flex justify-content-between mb-2">
              <span>商品总价:</span>
              <span>¥{{ cartTotal.toFixed(2) }}</span>
            </div>
            <div class="d-flex justify-content-between mb-2">
              <span>运费:</span>
              <span>¥{{ shippingFee.toFixed(2) }}</span>
            </div>
            <hr>
            <div class="d-flex justify-content-between mb-3">
              <strong>总计:</strong>
              <strong class="text-primary">¥{{ (cartTotal + shippingFee).toFixed(2) }}</strong>
            </div>
            <button @click="checkout" class="btn btn-success w-100 py-2">
              💳 立即结算
            </button>
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
  name: 'ShoppingCart',
  data() {
    return {
      shippingFee: 0 // 运费
    }
  },
  created() {
    this.$axios = api
  },
  computed: {
    ...mapGetters(['cartItems', 'cartTotal'])
  },
  methods: {
    ...mapActions(['updateCartQuantity', 'removeFromCart', 'clearCart']),
    updateQuantity(productId, quantity) {
      this.updateCartQuantity({ productId, quantity })
    },
    removeItem(productId) {
      if (confirm('确定要从购物车中移除这个商品吗？')) {
        this.removeFromCart(productId)
      }
    },
    async checkout() {
      if (this.cartItems.length === 0) {
        alert('购物车为空，无法结算')
        return
      }

      const shippingAddress = prompt('请输入收货地址:', '北京市朝阳区建国门外大街1号')
      if (!shippingAddress) return

      const paymentMethod = prompt('请选择支付方式:', '支付宝')
      if (!paymentMethod) return

      try {
        // 在创建订单前，先同步购物车到后端
        console.log('同步购物车到后端...')
        await this.$store.dispatch('syncLocalCartToBackend')
        
        // 调用后端API创建订单
        const response = await this.$axios.post('/orders', null, {
          params: {
            shippingAddress: shippingAddress,
            paymentMethod: paymentMethod
          }
        })

        console.log('订单创建成功:', response.data)
        
        alert(`订单提交成功！\n订单号: ${response.data.orderNumber}\n收货地址: ${shippingAddress}\n支付方式: ${paymentMethod}\n总金额: ¥${response.data.totalAmount}`)
        
        // 清空购物车
        this.clearCart()
        
        // 跳转到订单页面
        this.$router.push('/orders')
      } catch (error) {
        console.error('创建订单失败:', error)
        alert('订单提交失败: ' + (error.response?.data?.message || error.message || '未知错误'))
      }
    }
  }
}
</script>

<style scoped>
.cart-item {
  transition: background-color 0.2s;
}

.cart-item:hover {
  background-color: #f8f9fa;
}

.quantity-controls {
  max-width: 120px;
}

.quantity-controls button {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-cart {
  max-width: 400px;
  margin: 0 auto;
}

.btn {
  border-radius: 0.375rem;
}
</style>