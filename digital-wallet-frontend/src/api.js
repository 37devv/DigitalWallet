const BASE_URL = 'http://localhost:8080/api'

export async function fetchAccounts() {
  const res = await fetch(`${BASE_URL}/accounts`)
  if (!res.ok) throw new Error('Fehler beim Laden der Konten')
  return res.json()
}

export async function fetchAccount(id) {
  const res = await fetch(`${BASE_URL}/accounts/${id}`)
  if (!res.ok) throw new Error('Konto nicht gefunden')
  return res.json()
}

export async function fetchTransactions(id) {
  const res = await fetch(`${BASE_URL}/accounts/${id}/transactions`)
  if (!res.ok) throw new Error('Fehler beim Laden der Transaktionen')
  return res.json()
}
