import { Dumbbell, LogOut, ShieldCheck, Sparkles } from 'lucide-react'
import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { roleLabel } from '../utils/format'
import { Button } from '../components/ui'

function linksForRole(role) {
  const common = [
    { to: '/dashboard', label: 'Overview' },
    { to: '/gyms', label: 'Gyms' },
    { to: '/trainers', label: 'Trainers' },
  ]

  if (role === 'GYM_USER') {
    return [
      ...common,
      { to: '/memberships', label: 'Memberships' },
      { to: '/bookings', label: 'Bookings' },
      { to: '/workouts', label: 'Workouts' },
      { to: '/attendance', label: 'Attendance' },
      { to: '/payments', label: 'Payments' },
      { to: '/streak', label: 'Streak' },
    ]
  }

  if (role === 'GYM_TRAINER') {
    return [
      ...common,
      { to: '/bookings', label: 'Client Sessions' },
      { to: '/trainer-profile', label: 'Trainer Profile' },
    ]
  }

  if (role === 'GYM_OWNER') {
    return [
      ...common,
      { to: '/owner-hub', label: 'Owner Hub' },
      { to: '/equipment', label: 'Equipment' },
      { to: '/payments', label: 'Payments' },
      { to: '/memberships', label: 'Memberships' },
    ]
  }

  return common
}

export function AppLayout() {
  const { user, logout } = useAuth()
  const links = linksForRole(user?.role)

  return (
    <div className="min-h-screen bg-[var(--canvas)] text-[var(--ink)]">
      <div className="mx-auto grid min-h-screen max-w-[1500px] grid-cols-1 gap-6 px-4 py-4 lg:grid-cols-[280px_minmax(0,1fr)] lg:px-6">
        <aside className="rounded-[32px] border border-[var(--line)] bg-[linear-gradient(180deg,rgba(255,247,236,0.96),rgba(243,239,230,0.92))] p-6 shadow-[var(--card-shadow)]">
          <div className="flex items-center gap-3">
            <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-[var(--accent)] text-white">
              <Dumbbell size={22} />
            </div>
            <div>
              <p className="text-xs font-semibold uppercase tracking-[0.24em] text-[var(--ink-soft)]">FitConnect</p>
              <h1 className="text-xl font-semibold text-[var(--ink)]">Control Center</h1>
            </div>
          </div>

          <div className="mt-8 rounded-[28px] bg-[var(--ink)]/95 p-5 text-white">
            <div className="flex items-center gap-2 text-xs uppercase tracking-[0.22em] text-white/60">
              <ShieldCheck size={14} />
              Signed In
            </div>
            <p className="mt-4 text-xl font-semibold">{user?.name}</p>
            <p className="mt-1 text-sm text-white/70">{user?.email}</p>
            <p className="mt-4 inline-flex rounded-full bg-white/10 px-3 py-1 text-xs font-semibold uppercase tracking-[0.2em] text-white/85">
              {roleLabel(user?.role)}
            </p>
          </div>

          <nav className="mt-8 space-y-2">
            {links.map((link) => (
              <NavLink
                key={link.to}
                to={link.to}
                className={({ isActive }) =>
                  `flex items-center justify-between rounded-2xl px-4 py-3 text-sm font-medium transition ${
                    isActive ? 'bg-white text-[var(--ink)] shadow-sm' : 'text-[var(--ink-soft)] hover:bg-white/70 hover:text-[var(--ink)]'
                  }`
                }
              >
                <span>{link.label}</span>
                <Sparkles size={14} />
              </NavLink>
            ))}
          </nav>

          <Button className="mt-8 w-full" variant="secondary" onClick={logout}>
            <LogOut size={16} />
            <span className="ml-2">Logout</span>
          </Button>
        </aside>

        <main className="rounded-[32px] border border-[var(--line)] bg-[rgba(255,252,247,0.75)] p-4 shadow-[var(--card-shadow)] backdrop-blur md:p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
