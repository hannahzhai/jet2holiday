<template>
  <div class="spinner-wrap">
    <div class="spinner"></div>
  </div>
</template>

<style scoped>
.spinner-wrap {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 120px;
}

.spinner {
  width: 30px;
  height: 30px;
  border-radius: 999px;
  border: 3px solid #dce3f4;
  border-top-color: var(--color-primary);
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>