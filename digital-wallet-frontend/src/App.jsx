import {
  AppBar,
  Box,
  Card,
  CardContent,
  Container,
  Divider,
  List,
  Toolbar,
  Typography,
} from '@mui/material'
import MonetizationOnIcon from '@mui/icons-material/MonetizationOn'

const accounts = [
  { bank: 'UBS', type: 'Privatkonto', balance: 11117.60, logo: 'UBS' },
  { bank: 'Schaffhauser Kantonalbank', type: 'Privatkonto', balance: 6127.45, logo: 'SKB' },
  { bank: 'Credit Suisse', type: 'Sparkonto', balance: 4000.00, logo: 'CS' },
  { bank: 'Raiffeisen', type: 'Privatkonto', balance: -979.10, logo: 'RF' },
  { bank: 'Raiffeisen', type: 'Sparkonto', balance: 3000.00, logo: 'RF' },
]

const totalBalance = accounts.reduce((sum, a) => sum + a.balance, 0)

function formatChf(amount) {
  const abs = Math.abs(amount).toFixed(2)
  const [int, dec] = abs.split('.')
  const formatted = int.replace(/\B(?=(\d{3})+(?!\d))/g, "'")
  return `${amount < 0 ? '-' : ''}${formatted}.${dec}`
}

const bankStyles = {
  UBS: { fontWeight: 900, fontSize: '1.1rem', letterSpacing: 1, color: '#c00' },
  SKB: { fontWeight: 700, fontSize: '0.85rem', color: '#333' },
  CS: { fontWeight: 700, fontSize: '0.9rem', fontStyle: 'italic', color: '#222' },
  RF: { fontWeight: 900, fontSize: '1rem', letterSpacing: 2, color: '#222' },
}

function BankLabel({ logo, bank }) {
  return (
    <Typography sx={{ minWidth: 200, ...bankStyles[logo] }}>
      {bank}
    </Typography>
  )
}

function App() {
  return (
    <Box sx={{ minHeight: '100vh', bgcolor: '#f5f5f5' }}>
      {/* Header */}
      <AppBar position="static" sx={{ bgcolor: '#c0392b' }}>
        <Toolbar>
          <Typography variant="h6" sx={{ fontWeight: 600 }}>
            Oepfelbaum
          </Typography>
          <Divider orientation="vertical" flexItem sx={{ mx: 2, bgcolor: 'rgba(255,255,255,0.5)' }} />
          <Typography variant="h6" sx={{ fontWeight: 400 }}>
            Digitales Portemonnaie
          </Typography>
        </Toolbar>
      </AppBar>

      <Container maxWidth="md" sx={{ py: 5 }}>
        {/* Total Balance */}
        <Box sx={{ textAlign: 'center', mb: 4 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 1.5 }}>
            <MonetizationOnIcon sx={{ fontSize: '3rem', color: '#888' }} />
            <Typography variant="h3" sx={{ fontWeight: 500, color: '#222' }}>
              {formatChf(totalBalance)}&nbsp;
              <Typography component="span" variant="h5" sx={{ color: '#888', fontWeight: 400 }}>
                CHF
              </Typography>
            </Typography>
          </Box>
          <Typography variant="body1" sx={{ color: '#999', mt: 0.5 }}>
            Valuta 15.12.2021
          </Typography>
        </Box>

        {/* Account List */}
        <List disablePadding sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
          {accounts.map((account, index) => (
            <Card
              key={index}
              variant="outlined"
              sx={{ borderRadius: 1, boxShadow: 'none' }}
            >
              <CardContent sx={{ py: 2, px: 3, '&:last-child': { pb: 2 } }}>
                <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                  <BankLabel logo={account.logo} bank={account.bank} />
                  <Typography variant="body1" sx={{ color: '#999', flex: 1, px: 2 }}>
                    {account.type}
                  </Typography>
                  <Box sx={{ display: 'flex', alignItems: 'baseline', gap: 0.5 }}>
                    <Typography
                      variant="h6"
                      sx={{ fontWeight: 500, color: account.balance < 0 ? '#c0392b' : '#222' }}
                    >
                      {formatChf(account.balance)}
                    </Typography>
                    <Typography variant="body1" sx={{ color: '#999' }}>
                      CHF
                    </Typography>
                  </Box>
                </Box>
              </CardContent>
            </Card>
          ))}
        </List>
      </Container>
    </Box>
  )
}

export default App
