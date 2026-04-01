<script setup>
defineProps({
  title: { type: String, default: '' }
})
</script>

<template>
  <section class="card">
    <header v-if="title" class="card-header">{{ title }}</header>
    <div class="card-content">
      <slot />
    </div>
  </section>
</template>

<style scoped>
.card {
  background: #fff;
  border-radius: 0.9rem;
  box-shadow: var(--shadow-card);
}

.card-header {
  border-bottom: 1px solid var(--color-border);
  padding: 0.85rem 1rem;
  font-weight: 700;
}

.card-content {
  padding: 1rem;
}
</style>