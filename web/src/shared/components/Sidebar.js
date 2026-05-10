import { useNavigate, useLocation } from "react-router-dom";
import { supabase } from "../../supabaseClient";
import "./Sidebar.css";

const menuItems = [
  { icon: "ti-layout-dashboard", label: "Dashboard",    path: "/dashboard" },
  { icon: "ti-map-2",            label: "Map",          path: "/map" },
  { icon: "ti-clipboard-list",   label: "Requests",     path: "/requests" },
  { icon: "ti-heart",            label: "Donations",    path: "/donations" },
  { icon: "ti-users",            label: "About Us",     path: "/about" },
  { icon: "ti-help-circle",      label: "Help & Support", path: "/help" },
];

export default function Sidebar({ onLogout }) {
  const navigate = useNavigate();
  const location = useLocation();

  const defaultLogout = async () => {
    await supabase.auth.signOut();
    navigate("/");
  };

  const handleLogout = onLogout ?? defaultLogout;

  return (
    <div className="sidebar">
      <div className="sidebar-logo">
        <div className="sidebar-logo-mark">
          <i className="ti ti-map-2" aria-hidden="true" />
        </div>
        <div>
          <h2 className="sidebar-app-name">Disaster Aid Connect</h2>
          <span className="sidebar-app-sub">Relief Coordination</span>
        </div>
      </div>

      <nav className="sidebar-nav">
        <p className="sidebar-section-label">Main</p>
        <ul className="sidebar-menu">
          {menuItems.slice(0, 4).map((item) => (
            <li key={item.path}>
              <button
                className={`sidebar-item ${location.pathname === item.path ? "active" : ""}`}
                onClick={() => navigate(item.path)}
              >
                <i className={`ti ${item.icon}`} aria-hidden="true" />
                <span>{item.label}</span>
              </button>
            </li>
          ))}
        </ul>

        <p className="sidebar-section-label">Info</p>
        <ul className="sidebar-menu">
          {menuItems.slice(4).map((item) => (
            <li key={item.path}>
              <button
                className={`sidebar-item ${location.pathname === item.path ? "active" : ""}`}
                onClick={() => navigate(item.path)}
              >
                <i className={`ti ${item.icon}`} aria-hidden="true" />
                <span>{item.label}</span>
              </button>
            </li>
          ))}
        </ul>
      </nav>

      <div className="sidebar-footer">
        <button className="sidebar-logout" onClick={handleLogout}>
          <i className="ti ti-logout" aria-hidden="true" />
          <span>Logout</span>
        </button>
      </div>
    </div>
  );
}