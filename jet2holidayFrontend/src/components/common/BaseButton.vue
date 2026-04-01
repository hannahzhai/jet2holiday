<script setup>
import { computed } from 'vue'

const props = defineProps({
  variant: { type: String, default: 'primary' },
  type: { type: String, default: 'button' },
  loading: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false }
})

const classes = computed(() => ['base-btn', `base-btn-${props.variant}`])
</script>

<template>
  <button :type="type" :class="classes" :disabled="disabled || loading">
    <span v-if="loading" class="dot-loader">???</span>
    <slot v-else />
  </button>
</template>

<style scoped>
.base-btn {
  border: none;
  border-radius: 0.55rem;
  padding: 0.55rem 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.base-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.base-btn-primary {
  background: var(--color-primary);
  color: #fff;
}

.base-btn-secondary {
  background: #e9edf7;
  color: var(--color-text);
}

.base-btn-danger {
  background: #d24141;
  color: #fff;
}

.base-btn:hover:not(:disabled) {
  transform: translateY(-1px);
}
</style>