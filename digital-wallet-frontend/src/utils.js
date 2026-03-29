export function todayFormatted() {
  const d = new Date()
  const day = String(d.getDate()).padStart(2, '0')
  const month = String(d.getMonth() + 1).padStart(2, '0')
  return `${day}.${month}.${d.getFullYear()}`
}

export function formatAmount(amount) {
  const rounded = Math.round(amount / 0.05) * 0.05
  const abs = Math.abs(rounded).toFixed(2)
  const [int, dec] = abs.split('.')
  const formatted = int.replace(/\B(?=(\d{3})+(?!\d))/g, "'")
  return `${rounded < 0 ? '-' : ''}${formatted}.${dec}`
}

export const bankStyles = {
  NW: { fontWeight: 700, fontSize: '1rem', color: '#542ea5' },
}
