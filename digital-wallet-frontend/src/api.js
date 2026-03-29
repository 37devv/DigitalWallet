const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api'

// ─── TEST MOCK — set to true to test negative balance display ───────────────
const USE_MOCK = false

const MOCK_ACCOUNTS = [
  { id: 'acc-1', bank: 'NatWest', logo: 'NW', type: 'CurrentAccount', balance: -320.50, ownerName: 'Sydney Beard' },
  { id: 'acc-2', bank: 'NatWest', logo: 'NW', type: 'Savings',        balance: 200.00, ownerName: 'Sydney Beard' },
]

const MOCK_TRANSACTIONS = {
  'acc-1': [
    { id: 't1', date: '15.03.2026', description: 'Miete',               amount: -1500.00, type: 'debit'  },
    { id: 't2', date: '18.03.2026', description: 'Lohn',                amount:  1000.00, type: 'credit' },
    { id: 't3', date: '20.03.2026', description: 'Pizzeria Roma',       amount:   -72.14, type: 'debit'  },
    { id: 't4', date: '22.03.2026', description: 'Freelance',           amount:   251.64, type: 'credit' },
  ],
  'acc-2': [
    { id: 't5', date: '01.03.2026', description: 'Monatliche Einzahlung', amount: 250.00, type: 'credit' },
  ],
}
// ─────────────────────────────────────────────────────────────────────────────

export async function fetchAccounts() {
  if (USE_MOCK) return MOCK_ACCOUNTS
  const res = await fetch(`${BASE_URL}/accounts`)
  if (!res.ok) throw new Error('Fehler beim Laden der Konten')
  return res.json()
}

export async function fetchAccount(id) {
  if (USE_MOCK) return MOCK_ACCOUNTS.find((a) => a.id === id) ?? MOCK_ACCOUNTS[0]
  const res = await fetch(`${BASE_URL}/accounts/${id}`)
  if (!res.ok) throw new Error('Konto nicht gefunden')
  return res.json()
}

export async function fetchTransactions(id) {
  if (USE_MOCK) return MOCK_TRANSACTIONS[id] ?? []
  const res = await fetch(`${BASE_URL}/accounts/${id}/transactions`)
  if (!res.ok) throw new Error('Fehler beim Laden der Transaktionen')
  return res.json()
}
