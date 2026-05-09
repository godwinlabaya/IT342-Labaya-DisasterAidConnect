import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Login from "./features/auth/Login";
import Register from "./features/auth/Register";
import Dashboard from "./features/dashboard/Dashboard";
import ProtectedRoute from "./shared/components/ProtectedRoute";
import MapPage from "./features/disaster/Map";
import RequestsPage from "./features/requests/Requests";
import DonationsPage from "./features/donations/Donations";
import AboutPage from "./shared/pages/About/About";
import HelpPage from "./shared/pages/Help/Help";


function App() {
  return (
    <Router>
      <Routes>
        {/* Public Routes */}
        <Route path="/" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/map" element={<MapPage />} />
        <Route path="/requests" element={<RequestsPage />} />
        <Route path="/donations" element={<DonationsPage />} />
        <Route path="/about" element={<AboutPage />} />
        <Route path="/help" element={<HelpPage />} />

        {/* Protected Route */}
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          }
        />
      </Routes>
    </Router>
  );
}

export default App;
