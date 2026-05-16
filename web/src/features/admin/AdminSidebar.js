import { useNavigate, useLocation } from "react-router-dom";
import { supabase } from "../../supabaseClient";
import "./Admin.css";

const menuItems = [
  { icon: "ti-layout-dashboard", label: "Dashboard",    path: "/admin/dashboard" },
  { icon: "ti-map-2",            label: "Disasters",    path: "/admin/disasters" },
  { icon: "ti-clipboard-list",   label: "Aid Requests", path: "/admin/aid-requests" },
  { icon: "ti-heart",            label: "Donations",    path: "/admin/donations" },
  { icon: "ti-users",            label: "Users",        path: "/admin/users" },
];

export default function AdminSidebar({ onLogout }) {
  const navigate  = useNavigate();
  const location  = useLocation();

  const defaultLogout = async () => {
    await supabase.auth.signOut();
    navigate("/");
  };

  const handleLogout = onLogout ?? defaultLogout;

  return (
    <div className="admin-sidebar">

      {/* Logo */}
      <div className="admin-logo">
        <div className="admin-logo-icon">
          <i className="ti ti-shield-check" aria-hidden="true" />
        </div>
        <h2>DisasterAidConnect</h2>
        <span className="admin-badge">Admin Panel</span>
      </div>

      {/* Nav */}
      <ul className="admin-menu">
        {menuItems.map((item) => (
          <li
            key={item.path}
            className={location.pathname === item.path ? "admin-active" : ""}
            onClick={() => navigate(item.path)}
          >
            <i className={`ti ${item.icon}`} aria-hidden="true" />
            <span>{item.label}</span>
          </li>
        ))}
      </ul>

      {/* Footer */}
      <div className="admin-sidebar-footer">
        <button className="admin-logout" onClick={handleLogout}>
          <i className="ti ti-logout" aria-hidden="true" />
          <span>Logout</span>
        </button>
      </div>
    </div>
  );
}