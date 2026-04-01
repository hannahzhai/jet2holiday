<script setup>
defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: 'Are you sure?' },
  message: { type: String, default: 'This action cannot be undone.' },
  confirmText: { type: String, default: 'Confirm' },
  cancelText: { type: String, default: 'Cancel' }
})

const emit = defineEmits(['update:modelValue', 'confirm'])

const close = () => emit('update:modelValue', false)
const confirm = () => {
  emit('confirm')
  close()
}
</script>

<template>
  <teleport to="body">
    <div v-if="modelValue" class="modal-backdrop" @click.self="close">
      <div class="modal-panel">
        <h3>{{ title }}</h3>
        <p>{{ message }}</p>
        <div class="actions">
          <button class="btn-secondary" type="button" @click="close">{{ cancelText }}</button>
          <button class="btn-danger" type="button" @click="confirm">{{ confirmText }}</button>
        </div>
      </div>
    </div>
  </teleport>
</template>

<style scoped>
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(9, 12, 20, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 99;
  padding: 1rem;
}

.modal-panel {
  background: #fff;
  border-radius: 0.9rem;
  padding: 1.1rem;
  width: min(420px, 100%);
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.6rem;
}

.btn-secondary,
.btn-danger {
  border: 0;
  border-radius: 0.5rem;
  padding: 0.45rem 0.75rem;
  cursor: pointer;
}

.btn-secondary {
  background: #e9edf7;
}

.btn-danger {
  background: #d24141;
  color: #fff;
}
</style>