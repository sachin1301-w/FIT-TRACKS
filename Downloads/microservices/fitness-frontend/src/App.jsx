
import { useEffect, useState } from 'react';
import {
  BrowserRouter as Router,
  Navigate,
  Route,
  Routes,
} from 'react-router-dom';
import Button from '@mui/material/Button';
import Box from '@mui/material/Box';
import { useDispatch } from 'react-redux';
import { useAuthContext } from 'react-oauth2-code-pkce';
import { setCrediantls } from './store/authSlice';
import ActivityForm from './components/ActivityForm';
import ActivityList from './components/ActivityList';
import ActivityDetail from './components/ActivityDetail';

const ActivitiesPage= () => {
  const [activitiesVersion, setActivitiesVersion] = useState(0);

  return (
    <Box
      className="page-enter"
      sx={{
        p: { xs: 2, sm: 4 },
        minHeight: 'calc(100vh - 32px)',
        textAlign: 'left',
      }}
    >
      <Box sx={{ mb: 4 }}>
        <Box sx={{ color: '#2563eb', fontSize: 13, fontWeight: 700, letterSpacing: 1.5, textTransform: 'uppercase' }}>
          Fitness dashboard
        </Box>
        <Box component="h1" sx={{ fontSize: { xs: 32, sm: 42 }, fontWeight: 800, letterSpacing: '-1px', mb: 1, mt: 1 }}>
          Keep moving forward.
        </Box>
        <Box sx={{ color: '#667085', fontSize: 16 }}>
          Track your progress and get personal insights from your activities.
        </Box>
      </Box>
      <ActivityForm onActivitiesAdded={() => setActivitiesVersion((version) => version + 1)}/>
      <ActivityList refreshKey={activitiesVersion} />
    </Box>
  );
};
function App() {
  const { token, tokenData, logIn, logOut } = useAuthContext();
  const dispatch = useDispatch();

  useEffect(() => {
    if (token && tokenData) {
      dispatch(setCrediantls({ token, user: tokenData }));
    }
  }, [token, tokenData, dispatch]);

  return (
    <Router>
      {!token ? (
      <Box className="page-enter" sx={{ minHeight: '100vh', display: 'grid', placeItems: 'center', p: 3 }}>
        <Box sx={{ width: '100%', maxWidth: 460, p: { xs: 4, sm: 6 }, textAlign: 'center', border: '1px solid #e7eaf0', borderRadius: 5, boxShadow: '0 24px 70px rgba(37, 99, 235, 0.12)', background: 'rgba(255,255,255,0.9)' }}>
          <Box sx={{ display: 'inline-flex', p: 1.5, mb: 2, borderRadius: 3, background: '#eaf2ff', color: '#2563eb', fontWeight: 800 }}>FIT</Box>
          <Box component="h1" sx={{ fontSize: 36, fontWeight: 800, mb: 1, mt: 0 }}>Welcome back</Box>
          <Box sx={{ color: '#667085', mb: 4 }}>Your smarter fitness journey starts here.</Box>
          <Button fullWidth size="large" variant="contained" onClick={() => logIn()} sx={{ py: 1.5, borderRadius: 2, textTransform: 'none', fontSize: 16, fontWeight: 700 }}>Continue to dashboard</Button>
        </Box>
      </Box>
      ) : (
        <Box
          component="main"
          className="page-enter"
          sx={{
            p: { xs: 2, sm: 3 },
            minHeight: '100vh',
            boxSizing: 'border-box',
            textAlign: 'left',
          }}
        >
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Box sx={{ fontWeight: 800, color: '#172033', fontSize: 20 }}>Fit<span style={{ color: '#2563eb' }}>Track</span></Box>
            <Button variant="outlined" onClick={() => logOut()} sx={{ borderRadius: 2, textTransform: 'none' }}>Log out</Button>
          </Box>
          <Routes>
            <Route path="/activities" element={<ActivitiesPage />} />
            <Route path="/activities/:id" element={<ActivityDetail />} />
            <Route path="/" element={<Navigate to="/activities" replace />} />
          </Routes>
        </Box>
      )}
    </Router>
  );
}

export default App;
