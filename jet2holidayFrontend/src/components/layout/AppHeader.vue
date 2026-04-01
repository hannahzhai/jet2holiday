<script setup>
defineEmits(['toggle-sidebar'])
</script>

<template>
  <header class="header">
    <button type="button" class="menu-button" @click="$emit('toggle-sidebar')">?</button>
    <h2>Portfolio Dashboard</h2>
  </header>
</template>

<style scoped>
.header {
  height: 60px;
  border-bottom: 1px solid var(--color-border);
  background: #fff;
  padding: 0 1rem;
  display: flex;
  align-items: center;
  gap: 0.8rem;
}

.menu-button {
  border: 1px solid var(--color-border);
  background: #fff;
  border-radius: 0.45rem;
  padding: 0.3rem 0.55rem;
}

.header h2 {
  font-size: 1.05rem;
  margin: 0;
}

@media (min-width: 992px) {
  .menu-button {
    display: none;
  }
}
</style>