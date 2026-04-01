<script setup>
import { computed, ref } from 'vue'
import { RouterView } from 'vue-router'
import { usePortfolioStore } from './store/portfolioStore'
import AppSidebar from './components/layout/AppSidebar.vue'
import AppHeader from './components/layout/AppHeader.vue'

const sidebarOpen = ref(false)
const store = usePortfolioStore()
const notification = computed(() => store.notification)

const closeNotification = () => {
  store.clearNotification()
}
</script>

<template>
  <div class="app-shell">
    <AppSidebar :open="sidebarOpen" @close="sidebarOpen = false" />
    <div class="app-main">
      <AppHeader @toggle-sidebar="sidebarOpen = !sidebarOpen" />
      <main class="app-content">
        <RouterView />
      </main>
    </div>
    <transition name="fade">
      <div
        v-if="notification.message"
        class="toast"
        :class="notification.type === 'error' ? 'toast-error' : 'toast-success'"
      >
        <span>{{ notification.message }}</span>
        <button type="button" @click="closeNotification">?</button>
      </div>
    </transition>
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  background: var(--color-bg);
  display: flex;
}

.app-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.app-content {
  padding: 1rem;
}

.toast {
  position: fixed;
  right: 1rem;
  bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  max-width: 380px;
  padding: 0.75rem 0.9rem;
  border-radius: 0.75rem;
  box-shadow: 0 8px 24px rgba(14, 20, 36, 0.2);
  color: #fff;
  z-index: 90;
}

.toast-success {
  background: #0b8a53;
}

.toast-error {
  background: #d24141;
}

.toast button {
  background: transparent;
  border: 0;
  color: #fff;
  font-size: 1rem;
  cursor: pointer;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (min-width: 992px) {
  .app-content {
    padding: 1.5rem;
  }
}
</style>