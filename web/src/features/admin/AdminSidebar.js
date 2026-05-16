import { useNavigate, useLocation } from "react-router-dom";
import { supabase } from "../../supabaseClient";
import "./Admin.css";

const menuItems = [
  { icon: "📊", label: "Dashboard",    path: "/admin/dashboard" },
  { icon: "🗺️", label: "Disasters",    path: "/admin/disasters" },
  { icon: "📋", label: "Aid Requests", path: "/admin/aid-requests" },
  { icon: "💸", label: "Donations",    path: "/admin/donations" },
  { icon: "👥", label: "Users",        path: "/admin/users" },
];

export default function AdminSidebar({ onLogout }) {
  const navigate = useNavigate();
  const location = useLocation();

  const defaultLogout = async () => {
    await supabase.auth.signOut();
    navigate("/");
  };

  const handleLogout = onLogout ?? defaultLogout;

  return (
    <div className="admin-sidebar">
      <div className="admin-logo">
        <div className="admin-logo-icon">🛡️</div>
        <h2>DisasterAidConnect</h2>
        <span className="admin-badge">Admin Panel</span>
      </div>

      <ul className="admin-menu">
        {menuItems.map((item) => (
          <li
            key={item.path}
            className={location.pathname === item.path ? "admin-active" : ""}
            onClick={() => navigate(item.path)}
          >
            <span>{item.icon}</span>
            <span>{item.label}</span>
          </li>
        ))}
      </ul>

      <div className="admin-sidebar-footer">
        <button className="admin-logout" onClick={handleLogout}>
          <span>🚪</span>
          <span>Logout</span>
        </button>
      </div>
    </div>
  );
}