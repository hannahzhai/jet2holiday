<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const props = defineProps({
  open: { type: Boolean, default: false }
})
const emit = defineEmits(['close'])
const route = useRoute()

const navLinks = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/holdings', label: 'Holdings' }
]

const sidebarClass = computed(() => ['sidebar', props.open ? 'open' : ''])
</script>

<template>
  <aside :class="sidebarClass">
    <div class="title-row">
      <h1>Portfolio Management System</h1>
      <button class="mobile-close" @click="emit('close')">?</button>
    </div>
    <nav class="nav-list">
      <RouterLink
        v-for="link in navLinks"
        :key="link.to"
        :to="link.to"
        class="nav-item"
        :class="{ active: route.path === link.to }"
        @click="emit('close')"
      >
        {{ link.label }}
      </RouterLink>
    </nav>
  </aside>
</template>

<style scoped>
.sidebar {
  position: fixed;
  top: 0;
  left: -280px;
  width: 260px;
  height: 100vh;
  background: #0f1730;
  color: #fff;
  transition: left 0.22s ease;
  z-index: 70;
  padding: 1rem;
}

.sidebar.open {
  left: 0;
}

.title-row {
  display: flex;
  gap: 0.5rem;
  align-items: flex-start;
  justify-content: space-between;
}

.title-row h1 {
  font-size: 1.05rem;
  margin: 0;
  line-height: 1.25;
}

.mobile-close {
  border: 0;
  background: transparent;
  color: #fff;
  font-size: 1.1rem;
}

.nav-list {
  margin-top: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.nav-item {
  color: #d8def5;
  text-decoration: none;
  padding: 0.6rem 0.75rem;
  border-radius: 0.55rem;
}

.nav-item.active,
.nav-item:hover {
  background: #202d57;
  color: #fff;
}

@media (min-width: 992px) {
  .sidebar {
    position: sticky;
    left: 0;
    top: 0;
  }

  .mobile-close {
    display: none;
  }
}
</style>