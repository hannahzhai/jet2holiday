<script setup>
import { computed, reactive, watch } from 'vue'
import BaseButton from '../common/BaseButton.vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  initialData: { type: Object, default: null },
  loading: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'submit'])

const assetTypes = ['STOCK', 'BOND', 'CRYPTO', 'CASH']

const emptyForm = {
  symbol: '',
  companyName: '',
  assetType: 'STOCK',
  shares: '',
  costBasis: '',
  currency: 'USD'
}

const form = reactive({ ...emptyForm })
const errors = reactive({})
const isEdit = computed(() => Boolean(props.initialData?.id))

const resetForm = () => {
  Object.assign(form, emptyForm)
  Object.keys(errors).forEach((key) => delete errors[key])
}

watch(
  () => props.modelValue,
  (open) => {
    if (!open) {
      resetForm()
      return
    }
    if (props.initialData) {
      Object.assign(form, {
        symbol: props.initialData.symbol || '',
        companyName: props.initialData.companyName || '',
        assetType: props.initialData.assetType || 'STOCK',
        shares: props.initialData.shares ?? '',
        costBasis: props.initialData.costBasis ?? '',
        currency: props.initialData.currency || 'USD'
      })
    }
  }
)

const validate = () => {
  Object.keys(errors).forEach((key) => delete errors[key])
  if (!form.symbol.trim()) errors.symbol = 'Symbol is required.'
  if (!form.companyName.trim()) errors.companyName = 'Company name is required.'
  if (!form.assetType) errors.assetType = 'Asset type is required.'
  if (Number(form.shares) <= 0 || Number.isNaN(Number(form.shares))) errors.shares = 'Shares must be positive.'
  if (Number(form.costBasis) < 0 || Number.isNaN(Number(form.costBasis))) errors.costBasis = 'Cost basis must be non-negative.'
  if (!form.currency.trim()) errors.currency = 'Currency is required.'
  return Object.keys(errors).length === 0
}

const close = () => emit('update:modelValue', false)

const submit = () => {
  if (!validate()) return
  emit('submit', {
    symbol: form.symbol.trim().toUpperCase(),
    companyName: form.companyName.trim(),
    assetType: form.assetType,
    shares: Number(form.shares),
    costBasis: Number(form.costBasis),
    currency: form.currency.trim().toUpperCase()
  })
}
</script>

<template>
  <teleport to="body">
    <div v-if="modelValue" class="modal-backdrop" @click.self="close">
      <div class="modal-panel">
        <h3>{{ isEdit ? 'Edit Holding' : 'Add Holding' }}</h3>
        <form class="form-grid" @submit.prevent="submit">
          <label>
            Symbol
            <input v-model="form.symbol" type="text" />
            <small v-if="errors.symbol" class="error">{{ errors.symbol }}</small>
          </label>
          <label>
            Company Name
            <input v-model="form.companyName" type="text" />
            <small v-if="errors.companyName" class="error">{{ errors.companyName }}</small>
          </label>
          <label>
            Asset Type
            <select v-model="form.assetType">
              <option v-for="type in assetTypes" :key="type" :value="type">{{ type }}</option>
            </select>
            <small v-if="errors.assetType" class="error">{{ errors.assetType }}</small>
          </label>
          <label>
            Shares
            <input v-model="form.shares" step="0.00000001" type="number" />
            <small v-if="errors.shares" class="error">{{ errors.shares }}</small>
          </label>
          <label>
            Cost Basis
            <input v-model="form.costBasis" step="0.0001" type="number" />
            <small v-if="errors.costBasis" class="error">{{ errors.costBasis }}</small>
          </label>
          <label>
            Currency
            <input v-model="form.currency" type="text" maxlength="6" />
            <small v-if="errors.currency" class="error">{{ errors.currency }}</small>
          </label>
          <div class="actions">
            <BaseButton variant="secondary" type="button" @click="close">Cancel</BaseButton>
            <BaseButton :loading="loading" type="submit">
              {{ isEdit ? 'Update Holding' : 'Create Holding' }}
            </BaseButton>
          </div>
        </form>
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
  width: min(560px, 100%);
  border-radius: 0.9rem;
  padding: 1rem;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.8rem;
}

label {
  display: flex;
  flex-direction: column;
  font-size: 0.9rem;
  gap: 0.35rem;
}

input,
select {
  border: 1px solid var(--color-border);
  border-radius: 0.5rem;
  padding: 0.45rem 0.55rem;
}

.actions {
  grid-column: 1 / -1;
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
}

.error {
  color: #d24141;
  font-size: 0.78rem;
}

@media (max-width: 768px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>