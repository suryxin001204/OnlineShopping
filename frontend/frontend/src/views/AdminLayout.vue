<template>
  <div class="admin-layout">
    <Header />
    <div class="d-flex">
      <Sidebar />
      <main class="flex-grow-1 p-4" style="margin-left: 250px; margin-top: 56px; min-height: calc(100vh - 56px);">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script>
import Header from '../components/common/Header.vue'
import Sidebar from '../components/common/Sidebar.vue'

export default {
  name: 'AdminLayout',
  components: {
    Header,
    Sidebar
  }
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  background-color: #f8f9fa;
}
</style>