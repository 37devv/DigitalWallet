export function formatChf(amount) {
  // Round to 5 Rappen
  const rounded = Math.round(amount / 0.05) * 0.05
  const abs = Math.abs(rounded).toFixed(2)
  const [int, dec] = abs.split('.')
  const formatted = int.replace(/\B(?=(\d{3})+(?!\d))/g, "'")
  return `${rounded < 0 ? '-' : ''}${formatted}.${dec}`
}

export const bankStyles = {
  UBS: { fontWeight: 900, fontSize: '1.1rem', letterSpacing: 1, color: '#c00' },
  SKB: { fontWeight: 700, fontSize: '0.85rem', color: '#333' },
  CS: { fontWeight: 700, fontSize: '0.9rem', fontStyle: 'italic', color: '#222' },
  RF: { fontWeight: 900, fontSize: '1rem', letterSpacing: 2, color: '#222' },
}
