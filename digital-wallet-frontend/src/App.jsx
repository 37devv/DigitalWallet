import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Overview from './pages/Overview'
import AccountDetail from './pages/AccountDetail'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Overview />} />
        <Route path="/account/:id" element={<AccountDetail />} />
      </Routes>
    </BrowserRouter>
  )
}
