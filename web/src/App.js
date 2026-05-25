import { BrowserRouter as Router, Routes, Route } from "react-router-dom";

// ── User features ──────────────────────────────────────────────────────────────
import Login          from "./features/auth/Login";
import Register       from "./features/auth/Register";
import Dashboard      from "./features/dashboard/Dashboard";
import MapPage        from "./features/disaster/Map";
import RequestsPage   from "./features/requests/Requests";
import DonationsPage  from "./features/donations/Donations";
import ProtectedRoute from "./shared/components/ProtectedRoute";
import AboutPage      from "./shared/pages/About/About";
import HelpPage       from "./shared/pages/Help/Help";

// ── Admin features ─────────────────────────────────────────────────────────────
import AdminDashboard  from "./features/admin/pages/AdminDashboard";
import AdminDisasters  from "./features/admin/pages/AdminDisasters";
import AdminMap from "./features/admin/pages/AdminMap";
import AdminDonations  from "./features/admin/pages/AdminDonations";
import AdminUsers      from "./features/admin/pages/AdminUsers";

function App() {
  return (
    <Router>
      <Routes>

        {/* ── Public Routes ── */}
        <Route path="/"         element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* ── User Routes ── */}
        <Route path="/map"       element={<MapPage />} />
        <Route path="/requests"  element={<RequestsPage />} />
        <Route path="/donations" element={<DonationsPage />} />
        <Route path="/about"     element={<AboutPage />} />
        <Route path="/help"      element={<HelpPage />} />

        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          }
        />

        {/* ── Admin Routes ─────────────────────────────────────────────────────
            useAdminAuth inside each page handles the role check —
            non-admins get redirected to /dashboard automatically.
        ── */}
        <Route path="/admin/dashboard"    element={<AdminDashboard />} />
        <Route path="/admin/disasters"    element={<AdminDisasters />} />
        <Route path="/admin/map" element={<AdminMap />} />
        <Route path="/admin/donations"    element={<AdminDonations />} />
        <Route path="/admin/users"        element={<AdminUsers />} />

      </Routes>
    </Router>
  );
}

export default App;