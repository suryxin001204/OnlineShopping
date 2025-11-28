import { createRouter, createWebHistory } from 'vue-router'
import store from '../store'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { requiresGuest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { requiresGuest: true }
  },
  {
    path: '/admin',
    component: () => import('../views/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('../components/admin/AdminDashboard.vue')
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('../components/admin/UserManagement.vue')
      },
      {
        path: 'products',
        name: 'ProductManagement',
        component: () => import('../components/admin/ProductManagement.vue')
      },
      {
        path: 'categories',
        name: 'CategoryManagement',
        component: () => import('../components/admin/CategoryManagement.vue')
      },
      {
        path: 'orders',
        name: 'OrderManagement',
        component: () => import('../components/admin/OrderManagement.vue')
      },
      {
        path: '',
        redirect: '/admin/dashboard'
      }
    ]
  },
  {
    path: '/',
    component: () => import('../views/UserLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'UserDashboard',
        component: () => import('../components/user/UserDashboard.vue')
      },
      {
        path: 'products',
        name: 'ProductList',
        component: () => import('../components/user/ProductList.vue')
      },
      {
        path: 'products/:id',
        name: 'ProductDetail',
        component: () => import('../components/user/ProductDetail.vue'),
        props: true
      },
      {
        path: 'cart',
        name: 'ShoppingCart',
        component: () => import('../components/user/ShoppingCart.vue')
      },
      {
        path: 'orders',
        name: 'OrderHistory',
        component: () => import('../components/user/OrderHistory.vue')
      },
      {
        path: 'profile',
        name: 'PersonalCenter',
        component: () => import('../components/user/PersonalCenter.vue')
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const isAuthenticated = store.getters.isAuthenticated
  const isAdmin = store.getters.isAdmin

  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login')
  } else if (to.meta.requiresAdmin && !isAdmin) {
    next('/')
  } else if (to.meta.requiresGuest && isAuthenticated) {
    next(isAdmin ? '/admin/dashboard' : '/')
  } else {
    next()
  }
})

export default router