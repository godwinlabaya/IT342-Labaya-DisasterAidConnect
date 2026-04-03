import { useNavigate, useLocation } from "react-router-dom";
import { supabase } from "../supabaseClient";
import "./Sidebar.css";

const menuItems = [
  { icon: "🏠", label: "Dashboard", path: "/dashboard" },
  { icon: "🗺️", label: "Map", path: "/map" },
  { icon: "📝", label: "Requests", path: "/requests" },
  { icon: "🎁", label: "Donations", path: "/donations" },
  { icon: "👥", label: "About Us", path: "/about" },
  { icon: "❓", label: "Help & Support", path: "/help" },
];

export default function Sidebar() {
  const navigate = useNavigate();
  const location = useLocation();

  const logout = async () => {
    await supabase.auth.signOut();
    navigate("/");
  };

  return (
    <div className="sidebar">
      <div className="logo">
        <div className="logo-icon">🌐</div>
        <h2>Disaster Aid Connect</h2>
        <span>User Management</span>
      </div>

      <ul className="menu">
        {menuItems.map((item) => (
          <li
            key={item.path}
            className={location.pathname === item.path ? "active" : ""}
            onClick={() => navigate(item.path)}
          >
            <span>{item.icon}</span>
            <span>{item.label}</span>
          </li>
        ))}
      </ul>

      <div className="sidebar-footer">
        <button className="logout" onClick={logout}>
          <span>🚪</span>
          <span>Logout</span>
        </button>
      </div>
    </div>
  );
}