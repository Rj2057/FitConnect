import { Navigate, Route, Routes } from 'react-router-dom'
import { AppLayout } from './layout/AppLayout'
import { useAuth } from './hooks/useAuth'
import { AuthPage } from './pages/AuthPage'
import { DashboardPage } from './pages/DashboardPage'
import { GymsPage } from './pages/GymsPage'
import { TrainersPage } from './pages/TrainersPage'
import { MembershipsPage } from './pages/MembershipsPage'
import { BookingsPage } from './pages/BookingsPage'
import { WorkoutsPage } from './pages/WorkoutsPage'
import { AttendancePage } from './pages/AttendancePage'
import { PaymentsPage } from './pages/PaymentsPage'
import { StreakPage } from './pages/StreakPage'
import { EquipmentPage } from './pages/EquipmentPage'
import { OwnerHubPage } from './pages/OwnerHubPage'
import { TrainerProfilePage } from './pages/TrainerProfilePage'
import { NotFoundPage } from './pages/NotFoundPage'

function ProtectedRoute({ children }) {
  const { isAuthenticated } = useAuth()

  if (!isAuthenticated) {
    return <Navigate to="/auth" replace />
  }

  return children
}

function HomeRedirect() {
  const { isAuthenticated } = useAuth()
  return <Navigate to={isAuthenticated ? '/dashboard' : '/auth'} replace />
}

function RoleGuard({ allowedRoles, children }) {
  const { user } = useAuth()

  if (!allowedRoles.includes(user?.role)) {
    return <Navigate to="/dashboard" replace />
  }

  return children
}

function App() {
  return (
    <Routes>
      <Route path="/" element={<HomeRedirect />} />
      <Route path="/auth" element={<AuthPage />} />
      <Route
        element={
          <ProtectedRoute>
            <AppLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/gyms" element={<GymsPage />} />
        <Route path="/trainers" element={<TrainersPage />} />
        <Route path="/memberships" element={<MembershipsPage />} />
        <Route path="/bookings" element={<BookingsPage />} />
        <Route
          path="/workouts"
          element={
            <RoleGuard allowedRoles={['GYM_USER']}>
              <WorkoutsPage />
            </RoleGuard>
          }
        />
        <Route
          path="/attendance"
          element={
            <RoleGuard allowedRoles={['GYM_USER']}>
              <AttendancePage />
            </RoleGuard>
          }
        />
        <Route path="/payments" element={<PaymentsPage />} />
        <Route
          path="/streak"
          element={
            <RoleGuard allowedRoles={['GYM_USER']}>
              <StreakPage />
            </RoleGuard>
          }
        />
        <Route path="/equipment" element={<EquipmentPage />} />
        <Route
          path="/owner-hub"
          element={
            <RoleGuard allowedRoles={['GYM_OWNER']}>
              <OwnerHubPage />
            </RoleGuard>
          }
        />
        <Route
          path="/trainer-profile"
          element={
            <RoleGuard allowedRoles={['GYM_TRAINER']}>
              <TrainerProfilePage />
            </RoleGuard>
          }
        />
      </Route>
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  )
}

export default App
