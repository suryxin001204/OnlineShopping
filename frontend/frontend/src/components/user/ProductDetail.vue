<template>
  <div class="container py-4">
    <h1 class="h4 mb-3">商品详情</h1>
    <p>这里是商品详情页面的占位组件。ID: {{ id }}</p>
  </div>
</template>

<script setup>
import { defineProps } from 'vue'
const props = defineProps({
  id: { type: String, required: true }
})
</script>
