<template>
  <div class="admin-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">
        <i class="bi bi-receipt"></i>
        订单管理
      </h1>
      <div class="action-buttons">
        <button class="modern-btn modern-btn-secondary" @click="fetchOrders">
          <i class="bi bi-arrow-clockwise"></i>
          刷新
        </button>
      </div>
    </div>

    <!-- 订单列表表格 -->
    <div class="content-card">
      <div class="table-responsive">
        <table class="modern-table">
          <thead>
            <tr>
              <th>订单号</th>
              <th>用户</th>
              <th>金额</th>
              <th>状态</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in orders" :key="order.id">
              <td>
                <span class="order-number" style="font-family: monospace; font-weight: 600; color: #667eea;">
                  {{ order.orderNumber }}
                </span>
              </td>
              <td>
                <div class="d-flex align-items-center gap-2">
                  <div class="user-avatar" :style="{ background: getUserColor(order.username) }">
                    {{ order.username?.charAt(0).toUpperCase() }}
                  </div>
                  <span>{{ order.username }}</span>
                </div>
              </td>
              <td><span class="price-text">¥{{ order.totalAmount.toFixed(2) }}</span></td>
              <td>
                <span class="status-badge" :class="'status-' + getStatusClass(order.status)">
                  {{ formatStatus(order.status) }}
                </span>
              </td>
              <td>{{ formatDate(order.createTime) }}</td>
              <td>
                <div class="d-flex gap-2">
                  <button class="action-btn action-btn-primary" @click="viewOrder(order)">
                    <i class="bi bi-eye"></i>
                    <span>查看详情</span>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="orders.length === 0">
              <td colspan="6">
                <div class="empty-state">
                  <i class="bi bi-inbox"></i>
                  <div>暂无订单数据</div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 订单详情模态框 -->
    <div class="modal fade" id="orderModal" tabindex="-1" aria-hidden="true" ref="orderModal">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">订单详情 - {{ currentOrder.orderNumber }}</h5>
            <button type="button" class="btn-close" @click="closeModal"></button>
          </div>
          <div class="modal-body" v-if="currentOrder.id">
            <!-- 订单基本信息 -->
            <div class="row mb-4">
              <div class="col-md-6">
                <p><strong>用户:</strong> {{ currentOrder.username }}</p>
                <p><strong>创建时间:</strong> {{ formatDate(currentOrder.createTime) }}</p>
                <p><strong>支付方式:</strong> {{ currentOrder.paymentMethod || '在线支付' }}</p>
              </div>
              <div class="col-md-6">
                <p><strong>收货地址:</strong> {{ currentOrder.shippingAddress }}</p>
                <p><strong>当前状态:</strong> 
                  <span class="badge" :class="getStatusBadgeClass(currentOrder.status)">
                    {{ formatStatus(currentOrder.status) }}
                  </span>
                </p>
              </div>
            </div>

            <!-- 订单商品列表 -->
            <h6 class="border-bottom pb-2 mb-3">商品清单</h6>
            <div class="table-responsive mb-4">
              <table class="table table-sm table-bordered">
                <thead class="table-light">
                  <tr>
                    <th>商品名称</th>
                    <th>单价</th>
                    <th>数量</th>
                    <th>小计</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in currentOrder.orderItems" :key="item.id">
                    <td>{{ item.productName }}</td>
                    <td>¥{{ item.price.toFixed(2) }}</td>
                    <td>{{ item.quantity }}</td>
                    <td>¥{{ item.subtotal.toFixed(2) }}</td>
                  </tr>
                </tbody>
                <tfoot>
                  <tr>
                    <td colspan="3" class="text-end fw-bold">总计:</td>
                    <td class="text-danger fw-bold">¥{{ currentOrder.totalAmount.toFixed(2) }}</td>
                  </tr>
                </tfoot>
              </table>
            </div>

            <!-- 状态管理 -->
            <div class="card bg-light">
              <div class="card-body">
                <h6 class="card-title">更新状态</h6>
                <div class="d-flex gap-2">
                  <button v-if="currentOrder.status === 'PENDING'" class="btn btn-success btn-sm" @click="updateStatus('PAID')">
                    标记为已支付
                  </button>
                  <button v-if="currentOrder.status === 'PAID'" class="btn btn-primary btn-sm" @click="updateStatus('SHIPPED')">
                    发货
                  </button>
                  <button v-if="currentOrder.status === 'SHIPPED'" class="btn btn-info btn-sm text-white" @click="updateStatus('DELIVERED')">
                    完成订单
                  </button>
                  <button v-if="['PENDING', 'PAID'].includes(currentOrder.status)" class="btn btn-danger btn-sm" @click="updateStatus('CANCELLED')">
                    取消订单
                  </button>
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeModal">关闭</button>
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
  name: 'OrderManagement',
  data() {
    return {
      orders: [],
      modalInstance: null,
      currentOrder: {}
    }
  },
  mounted() {
    this.fetchOrders()
    this.modalInstance = new Modal(this.$refs.orderModal)
  },
  methods: {
    async fetchOrders() {
      try {
        console.log('开始获取订单列表...')
        console.log('当前用户:', this.$store.state.user)
        console.log('认证状态:', this.$store.getters.isAuthenticated)
        console.log('管理员状态:', this.$store.getters.isAdmin)
        
        const response = await request.get('/orders/all')
        console.log('订单数据响应:', response.data)
        this.orders = response.data || []
      } catch (error) {
        console.error('获取订单列表失败:', error)
        console.error('错误详情:', error.response)
        
        if (error.response?.status === 401) {
          alert('认证失败，请重新登录')
          this.$router.push('/login')
        } else if (error.response?.status === 403) {
          alert('没有权限访问订单数据')
        } else {
          alert('获取订单列表失败: ' + (error.response?.data?.message || error.message))
        }
      }
    },
    formatDate(dateStr) {
      if (!dateStr) return '-'
      return new Date(dateStr).toLocaleString()
    },
    formatStatus(status) {
      const statusMap = {
        'PENDING': '待支付',
        'PAID': '已支付',
        'SHIPPED': '已发货',
        'DELIVERED': '已完成',
        'CANCELLED': '已取消'
      }
      return statusMap[status] || status
    },
    getStatusBadgeClass(status) {
      const classMap = {
        'PENDING': 'bg-warning text-dark',
        'PAID': 'bg-info text-dark',
        'SHIPPED': 'bg-primary',
        'DELIVERED': 'bg-success',
        'CANCELLED': 'bg-secondary'
      }
      return classMap[status] || 'bg-secondary'
    },
    viewOrder(order) {
      this.currentOrder = { ...order }
      this.modalInstance.show()
    },
    closeModal() {
      this.modalInstance.hide()
    },
    async updateStatus(newStatus) {
      if (!confirm(`确定要将订单状态更新为 "${this.formatStatus(newStatus)}" 吗？`)) {
        return
      }
      try {
        const response = await request.put(`/orders/${this.currentOrder.id}/status`, null, {
          params: { status: newStatus }
        })
        this.currentOrder = response.data
        // 更新列表中的数据
        const index = this.orders.findIndex(o => o.id === this.currentOrder.id)
        if (index !== -1) {
          this.orders[index] = this.currentOrder
        }
        alert('状态更新成功')
      } catch (error) {
        console.error('更新状态失败:', error)
        alert('更新状态失败: ' + (error.response?.data?.message || error.message))
      }
    },
    getStatusClass(status) {
      const classMap = {
        'PENDING': 'warning',
        'PAID': 'info',
        'SHIPPED': 'primary',
        'DELIVERED': 'success',
        'CANCELLED': 'danger'
      }
      return classMap[status] || 'secondary'
    },
    getUserColor(username) {
      const colors = [
        'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
        'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
        'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
        'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
      ]
      const index = username?.charCodeAt(0) % colors.length || 0
      return colors[index]
    }
  }
}
</script>

<style scoped>
@import '../../assets/admin-common.css';

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 0.875rem;
}
</style>
