<template>
  <div class="table-wrap">
    <table class="table">
      <slot />
    </table>
  </div>
</template>

<style scoped>
.table-wrap {
  overflow-x: auto;
}

.table {
  width: 100%;
  border-collapse: collapse;
  min-width: 760px;
}

.table :deep(th),
.table :deep(td) {
  text-align: left;
  padding: 0.7rem 0.55rem;
  border-bottom: 1px solid var(--color-border);
}

.table :deep(th) {
  font-size: 0.8rem;
  color: #5e6885;
  text-transform: uppercase;
}
</style>