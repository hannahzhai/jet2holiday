<script setup>
import { computed, reactive, ref, watch } from 'vue'
import BaseButton from '../common/BaseButton.vue'
import { formatCurrency } from '../../utils/formatters'
import { getLatestMarketData } from '../../api/marketDataApi'
import { normalizeApiError } from '../../api/httpClient'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  instrument: { type: Object, default: null },
  loading: { type: Boolean, default: false },
  defaultCurrency: { type: String, default: 'USD' }
})

const emit = defineEmits(['update:modelValue', 'submit'])

const form = reactive({
  shares: '',
  costBasis: ''
})
const errors = reactive({})
const latestPrice = ref(null)
const priceLoading = ref(false)
const priceError = ref('')
let priceRequestId = 0

const displayCurrency = computed(() => props.instrument?.currency || props.defaultCurrency || 'USD')
const currentPriceText = computed(() => {
  if (latestPrice.value == null) return '-'
  return formatCurrency(latestPrice.value, displayCurrency.value)
})

const reset = () => {
  form.shares = ''
  form.costBasis = ''
  latestPrice.value = null
  priceLoading.value = false
  priceError.value = ''
  Object.keys(errors).forEach((key) => delete errors[key])
}

watch(
  () => props.modelValue,
  async (open) => {
    if (!open) {
      reset()
      return
    }
    await loadLatestPrice()
  }
)

watch(
  () => props.instrument?.symbol,
  async () => {
    if (!props.modelValue) return
    await loadLatestPrice()
  }
)

const loadLatestPrice = async () => {
  const symbol = `${props.instrument?.symbol || ''}`.trim().toUpperCase()
  if (!symbol) {
    latestPrice.value = null
    priceError.value = ''
    return
  }

  const requestId = ++priceRequestId
  priceLoading.value = true
  priceError.value = ''
  latestPrice.value = null

  try {
    const rows = await getLatestMarketData([symbol])
    if (requestId !== priceRequestId) return
    const first = Array.isArray(rows) ? rows[0] : null
    if (first?.currentPrice == null) {
      priceError.value = 'No latest price available right now.'
      return
    }
    latestPrice.value = Number(first.currentPrice)
  } catch (error) {
    if (requestId !== priceRequestId) return
    priceError.value = normalizeApiError(error, 'Failed to load latest price.')
  } finally {
    if (requestId === priceRequestId) {
      priceLoading.value = false
    }
  }
}

const close = () => emit('update:modelValue', false)

const validate = () => {
  Object.keys(errors).forEach((key) => delete errors[key])
  if (Number(form.shares) <= 0 || Number.isNaN(Number(form.shares))) {
    errors.shares = 'Shares must be positive.'
  }
  if (Number(form.costBasis) <= 0 || Number.isNaN(Number(form.costBasis))) {
    errors.costBasis = 'Cost basis must be positive.'
  }
  return Object.keys(errors).length === 0
}

const submit = () => {
  if (!props.instrument || !validate()) return
  emit('submit', {
    shares: Number(form.shares),
    costBasis: Number(form.costBasis)
  })
}
</script>

<template>
  <teleport to="body">
    <div v-if="modelValue" class="modal-backdrop" @click.self="close">
      <div class="modal-panel">
        <h3>Add Into Holdings</h3>
        <p class="meta">
          {{ instrument?.symbol }} - {{ instrument?.companyName }}
        </p>
        <p class="meta">
          Current price:
          <span v-if="priceLoading">Loading...</span>
          <span v-else>{{ currentPriceText }}</span>
        </p>
        <p v-if="priceError" class="error-inline">{{ priceError }}</p>
        <form class="form-grid" @submit.prevent="submit">
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
          <div class="actions">
            <BaseButton variant="secondary" type="button" @click="close">Cancel</BaseButton>
            <BaseButton :loading="loading" type="submit">Add Holding</BaseButton>
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
  width: min(520px, 100%);
  border-radius: 0.9rem;
  padding: 1rem;
}

.meta {
  margin: 0.3rem 0;
  color: #495472;
  font-size: 0.9rem;
}

.form-grid {
  margin-top: 0.8rem;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.8rem;
}

label {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  font-size: 0.9rem;
}

input {
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

.error-inline {
  color: #d24141;
  margin: 0.2rem 0 0;
  font-size: 0.82rem;
}

@media (max-width: 768px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
