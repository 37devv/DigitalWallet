import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import {
  AppBar,
  Box,
  Card,
  CardContent,
  CircularProgress,
  Container,
  Divider,
  IconButton,
  Tab,
  Tabs,
  Toolbar,
  Typography,
} from '@mui/material'
import ArrowBackIcon from '@mui/icons-material/ArrowBack'
import MonetizationOnIcon from '@mui/icons-material/MonetizationOn'
import { fetchAccount, fetchTransactions } from '../api'
import { formatChf, bankStyles } from '../utils'

export default function AccountDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [tab, setTab] = useState('ausgaben')
  const [account, setAccount] = useState(null)
  const [transactions, setTransactions] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    Promise.all([fetchAccount(id), fetchTransactions(id)])
      .then(([acc, txs]) => {
        setAccount(acc)
        setTransactions(txs)
      })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }, [id])

  const filtered = transactions.filter((t) => t.type === tab)

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: '#f5f5f5' }}>
      {/* Header */}
      <AppBar position="static" sx={{ bgcolor: '#c0392b' }}>
        <Toolbar>
          <IconButton color="inherit" edge="start" onClick={() => navigate('/')} sx={{ mr: 1 }}>
            <ArrowBackIcon />
          </IconButton>
          <Typography variant="h6" sx={{ fontWeight: 400 }}>
            Digitales Portemonnaie
          </Typography>
          <Divider orientation="vertical" flexItem sx={{ mx: 2, bgcolor: 'rgba(255,255,255,0.5)' }} />
          <Typography variant="h6" sx={{ fontWeight: 600 }}>
            Kontodetails
          </Typography>
        </Toolbar>

        <Tabs
          value={tab}
          onChange={(_, v) => setTab(v)}
          textColor="inherit"
          centered
          TabIndicatorProps={{ style: { backgroundColor: 'white', height: 3 } }}
        >
          <Tab label="Ausgaben" value="ausgaben" sx={{ fontWeight: tab === 'ausgaben' ? 700 : 400, opacity: 1 }} />
          <Tab label="Einkommen" value="einkommen" sx={{ fontWeight: tab === 'einkommen' ? 700 : 400, opacity: 1 }} />
        </Tabs>
      </AppBar>

      <Container maxWidth="md" sx={{ py: 5 }}>
        {loading && (
          <Box sx={{ textAlign: 'center', mt: 8 }}>
            <CircularProgress sx={{ color: '#c0392b' }} />
          </Box>
        )}

        {error && (
          <Typography sx={{ textAlign: 'center', color: '#c0392b', mt: 8 }}>
            {error}
          </Typography>
        )}

        {!loading && !error && account && (
          <>
            {/* Bank name & balance */}
            <Box sx={{ textAlign: 'center', mb: 4 }}>
              <Typography sx={{ mb: 1, ...bankStyles[account.logo], fontSize: '1.5rem' }}>
                {account.bank}
              </Typography>
              <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 1.5 }}>
                <MonetizationOnIcon sx={{ fontSize: '2.5rem', color: '#888' }} />
                <Typography variant="h3" sx={{ fontWeight: 500, color: account.balance < 0 ? '#c0392b' : '#222' }}>
                  {formatChf(account.balance)}&nbsp;
                  <Typography component="span" variant="h5" sx={{ color: '#888', fontWeight: 400 }}>
                    CHF
                  </Typography>
                </Typography>
              </Box>
              <Typography variant="body1" sx={{ color: '#999', mt: 0.5 }}>
                Valuta 15.12.2021
              </Typography>
            </Box>

            {/* Transaction list */}
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
              {filtered.length === 0 && (
                <Typography sx={{ textAlign: 'center', color: '#999', mt: 4 }}>
                  Keine Transaktionen vorhanden.
                </Typography>
              )}
              {filtered.map((tx) => (
                <Card key={tx.id} variant="outlined" sx={{ borderRadius: 1, boxShadow: 'none' }}>
                  <CardContent sx={{ py: 1.5, px: 3, '&:last-child': { pb: 1.5 } }}>
                    <Typography variant="caption" sx={{ color: '#999' }}>
                      {tx.date}
                    </Typography>
                    <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mt: 0.5 }}>
                      <Typography variant="body1">{tx.description}</Typography>
                      <Box sx={{ display: 'flex', alignItems: 'baseline', gap: 0.5 }}>
                        <Typography
                          variant="body1"
                          sx={{ fontWeight: 500, color: tx.amount < 0 ? '#c0392b' : '#222' }}
                        >
                          {formatChf(Math.abs(tx.amount))}
                        </Typography>
                        <Typography variant="body2" sx={{ color: '#999' }}>
                          CHF
                        </Typography>
                      </Box>
                    </Box>
                  </CardContent>
                </Card>
              ))}
            </Box>
          </>
        )}
      </Container>
    </Box>
  )
}
