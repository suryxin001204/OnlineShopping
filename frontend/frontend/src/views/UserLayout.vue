<template>
  <div class="user-layout d-flex flex-column min-vh-100">
    <Header />
    <main class="flex-grow-1 py-4">
      <div class="container">
        <router-view />
      </div>
    </main>
    <Footer />
  </div>
</template>

<script>
import Header from '../components/common/Header.vue'
import Footer from '../components/common/Footer.vue'

export default {
  name: 'UserLayout',
  components: {
    Header,
    Footer
  }
}
</script>

<style scoped>
.user-layout {
  background-color: #f8f9fa;
}
</style>