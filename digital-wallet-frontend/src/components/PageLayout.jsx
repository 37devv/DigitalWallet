import { Box, CircularProgress, Container, Typography } from '@mui/material'

export default function PageLayout({ header, loading, error, children }) {
  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      {header}
      <Container maxWidth="md" sx={{ py: 5 }}>
        {loading && (
          <Box sx={{ textAlign: 'center', mt: 8 }}>
            <CircularProgress color="primary" />
          </Box>
        )}
        {error && (
          <Typography sx={{ textAlign: 'center', color: 'primary.main', mt: 8 }}>
            {error}
          </Typography>
        )}
        {!loading && !error && children}
      </Container>
    </Box>
  )
}
