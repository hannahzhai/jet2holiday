<script setup>
defineProps({
  title: { type: String, default: 'No data available' },
  description: { type: String, default: '' }
})
</script>

<template>
  <div class="empty-state">
    <h3>{{ title }}</h3>
    <p v-if="description">{{ description }}</p>
  </div>
</template>

<style scoped>
.empty-state {
  border: 1px dashed #c9d2e8;
  border-radius: 0.8rem;
  background: #fbfcff;
  text-align: center;
  padding: 1.5rem 1rem;
  color: #5b6687;
}

.empty-state h3 {
  margin: 0;
  color: #2f3958;
}

.empty-state p {
  margin: 0.5rem 0 0;
}
</style>