<script setup>
import { reactive, watch } from 'vue'
import BaseButton from '../common/BaseButton.vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  currentBalance: { type: Number, default: 0 },
  loading: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'submit'])
const form = reactive({ cashBalance: 0, error: '' })

watch(
  () => props.modelValue,
  (open) => {
    if (open) {
      form.cashBalance = Number(props.currentBalance ?? 0)
      form.error = ''
    }
  }
)

const close = () => emit('update:modelValue', false)

const submit = () => {
  if (Number(form.cashBalance) < 0 || Number.isNaN(Number(form.cashBalance))) {
    form.error = 'Cash balance must be non-negative.'
    return
  }
  emit('submit', Number(form.cashBalance))
}
</script>

<template>
  <teleport to="body">
    <div v-if="modelValue" class="modal-backdrop" @click.self="close">
      <div class="modal-panel">
        <h3>Edit Cash Balance</h3>
        <label>
          Cash Balance
          <input v-model="form.cashBalance" type="number" step="0.0001" />
          <small v-if="form.error" class="error">{{ form.error }}</small>
        </label>
        <div class="actions">
          <BaseButton variant="secondary" @click="close">Cancel</BaseButton>
          <BaseButton :loading="loading" @click="submit">Save</BaseButton>
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
  justify-content: center;
  align-items: center;
  padding: 1rem;
  z-index: 99;
}

.modal-panel {
  background: #fff;
  width: min(420px, 100%);
  border-radius: 0.9rem;
  padding: 1rem;
}

label {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

input {
  border: 1px solid var(--color-border);
  border-radius: 0.5rem;
  padding: 0.45rem 0.55rem;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 0.9rem;
}

.error {
  color: #d24141;
}
</style>