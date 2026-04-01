export const formatCurrency = (value, currency = 'USD') => {
  const numericValue = Number(value ?? 0)
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency,
    maximumFractionDigits: 2
  }).format(numericValue)
}

export const formatPercent = (value) => {
  const numericValue = Number(value ?? 0)
  return `${numericValue.toFixed(2)}%`
}

export const formatDate = (dateValue) => {
  if (!dateValue) return '-'
  const date = new Date(dateValue)
  if (Number.isNaN(date.getTime())) return dateValue
  return new Intl.DateTimeFormat('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  }).format(date)
}

export const profitLossClass = (value) => {
  const numericValue = Number(value ?? 0)
  if (numericValue > 0) return 'positive'
  if (numericValue < 0) return 'negative'
  return 'neutral'
}