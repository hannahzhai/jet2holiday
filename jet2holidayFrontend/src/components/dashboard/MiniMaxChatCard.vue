<script setup>
import { ref } from 'vue'
import BaseCard from '../common/BaseCard.vue'
import BaseButton from '../common/BaseButton.vue'

const props = defineProps({
  loading: { type: Boolean, default: false },
  answer: { type: Object, default: null },
  range: { type: String, default: '1M' }
})

const emit = defineEmits(['ask'])
const question = ref('')

const submit = () => {
  const trimmed = `${question.value || ''}`.trim()
  if (!trimmed) return
  emit('ask', { question: trimmed, range: props.range })
}
</script>

<template>
  <BaseCard title="MiniMax Q&A">
    <div class="chat-grid">
      <div class="input-row">
        <textarea
          v-model="question"
          rows="3"
          placeholder="Ask about your portfolio, risk, allocation, or next steps..."
        />
        <BaseButton :loading="loading" :disabled="!question.trim()" @click="submit">
          Ask MiniMax
        </BaseButton>
      </div>

      <div v-if="answer?.answer" class="answer-box">
        <pre>{{ answer.answer }}</pre>
        <div class="meta">
          <span>Model: {{ answer.model || 'N/A' }}</span>
          <span v-if="answer.fallbackUsed" class="fallback">Fallback used</span>
        </div>
      </div>
      <p v-else class="hint">No answer yet. Ask one question to get context-aware feedback.</p>
    </div>
  </BaseCard>
</template>

<style scoped>
.chat-grid {
  display: grid;
  gap: 0.8rem;
}

.input-row {
  display: grid;
  gap: 0.6rem;
}

textarea {
  width: 100%;
  resize: vertical;
  border: 1px solid var(--color-border);
  border-radius: 0.55rem;
  padding: 0.6rem 0.7rem;
  font: inherit;
  min-height: 80px;
}

.answer-box {
  border: 1px solid var(--color-border);
  border-radius: 0.55rem;
  padding: 0.65rem 0.75rem;
  background: #fafcff;
}

pre {
  margin: 0;
  white-space: pre-wrap;
  font-family: inherit;
  line-height: 1.4;
}

.meta {
  margin-top: 0.55rem;
  font-size: 0.82rem;
  color: #6a7390;
  display: flex;
  gap: 0.75rem;
}

.fallback {
  color: #b56a00;
  font-weight: 600;
}

.hint {
  margin: 0;
  color: #6a7390;
  font-size: 0.9rem;
}
</style>
