import { createStore } from 'vuex'
import api from '../utils/request'

export default createStore({
  state: {
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user')) || null,
    cartItems: JSON.parse(localStorage.getItem('cartItems')) || []
  },
  mutations: {
    setToken(state, token) {
      state.token = token
      localStorage.setItem('token', token)
    },
    setUser(state, user) {
      state.user = user
      localStorage.setItem('user', JSON.stringify(user))
    },
    setCartItems(state, cartItems) {
      state.cartItems = cartItems
      localStorage.setItem('cartItems', JSON.stringify(cartItems))
    },
    addToCart(state, product) {
      const existingItem = state.cartItems.find(item => item.productId === product.id)
      if (existingItem) {
        existingItem.quantity += 1
      } else {
        state.cartItems.push({
          productId: product.id,
          productName: product.name,
          productPrice: product.price,
          productImage: product.imageUrl,
          quantity: 1
        })
      }
      localStorage.setItem('cartItems', JSON.stringify(state.cartItems))
    },
    removeFromCart(state, productId) {
      state.cartItems = state.cartItems.filter(item => item.productId !== productId)
      localStorage.setItem('cartItems', JSON.stringify(state.cartItems))
    },
    updateCartQuantity(state, { productId, quantity }) {
      const item = state.cartItems.find(item => item.productId === productId)
      if (item) {
        if (quantity <= 0) {
          state.cartItems = state.cartItems.filter(item => item.productId !== productId)
        } else {
          item.quantity = quantity
        }
      }
      localStorage.setItem('cartItems', JSON.stringify(state.cartItems))
    },
    clearAuth(state) {
      state.token = ''
      state.user = null
      state.cartItems = []
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      localStorage.removeItem('cartItems')
    }
  },
  actions: {
    login({ commit }, { token, user }) {
      commit('setToken', token)
      commit('setUser', user)
    },
    logout({ commit }) {
      commit('clearAuth')
    },
    async addToCart({ commit, state }, product) {
      // 先更新本地状态
      commit('addToCart', product)
      
      // 如果用户已登录，同步到后端
      if (state.token) {
        try {
          await api.post('/cart/items', null, {
            params: {
              productId: product.id,
              quantity: 1
            }
          })
          console.log('购物车已同步到后端')
        } catch (error) {
          console.error('同步购物车到后端失败:', error)
        }
      }
    },
    async removeFromCart({ commit, state }, productId) {
      commit('removeFromCart', productId)
      
      // 如果用户已登录，同步到后端
      if (state.token) {
        try {
          await api.delete(`/cart/items/${productId}`)
          console.log('购物车删除已同步到后端')
        } catch (error) {
          console.error('同步删除到后端失败:', error)
        }
      }
    },
    async updateCartQuantity({ commit, state }, payload) {
      commit('updateCartQuantity', payload)
      
      // 如果用户已登录，同步到后端
      if (state.token) {
        try {
          await api.put('/cart/items', null, {
            params: {
              productId: payload.productId,
              quantity: payload.quantity
            }
          })
          console.log('购物车数量已同步到后端')
        } catch (error) {
          console.error('同步数量到后端失败:', error)
        }
      }
    },
    async clearCart({ commit, state }) {
      commit('setCartItems', [])
      
      // 如果用户已登录，同步到后端
      if (state.token) {
        try {
          await api.delete('/cart/clear')
          console.log('购物车清空已同步到后端')
        } catch (error) {
          console.error('同步清空到后端失败:', error)
        }
      }
    },
    async loadCartFromBackend({ commit, state }) {
      if (state.token) {
        try {
          const response = await api.get('/cart/items')
          const cartItems = response.data.map(item => ({
            productId: item.productId,
            productName: item.productName,
            productPrice: item.productPrice,
            productImage: item.productImage,
            quantity: item.quantity
          }))
          commit('setCartItems', cartItems)
          console.log('从后端加载购物车成功:', cartItems)
        } catch (error) {
          console.error('从后端加载购物车失败:', error)
        }
      }
    },
    async syncLocalCartToBackend({ state }) {
      if (state.token && state.cartItems.length > 0) {
        try {
          console.log('开始同步本地购物车到后端...', state.cartItems)
          // 遍历本地购物车，将每个商品添加到后端
          for (const item of state.cartItems) {
            await api.post('/cart/items', null, {
              params: {
                productId: item.productId,
                quantity: item.quantity
              }
            })
          }
          console.log('购物车同步到后端成功')
        } catch (error) {
          console.error('同步购物车到后端失败:', error)
        }
      }
    }
  },
  getters: {
    isAuthenticated: state => !!state.token,
    isAdmin: state => state.user && (state.user.role === 'ADMIN' || state.user.role === 'ROLE_ADMIN'),
    currentUser: state => state.user,
    cartItems: state => state.cartItems,
    cartItemCount: state => state.cartItems.reduce((total, item) => total + item.quantity, 0),
    cartTotal: state => state.cartItems.reduce((total, item) => total + (item.productPrice * item.quantity), 0)
  }
})