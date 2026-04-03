import { useEffect, useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import "./Dashboard.css";
import { supabase } from "../../supabaseClient";

export default function Dashboard() {
  const navigate = useNavigate();
  const location = useLocation();
  const [user, setUser] = useState("");

  useEffect(() => {
    const getUser = async () => {
      const { data: { session } } = await supabase.auth.getSession();

      if (!session) {
        navigate("/");
        return;
      }

      const { data } = await supabase
        .from("users")
        .select("username")
        .eq("id", session.user.id)
        .single();

      if (data) setUser(data.username);
    };

    getUser();
  }, [navigate]);

  const logout = async () => {
    await supabase.auth.signOut();
    navigate("/");
  };

  const menuItems = [
    { icon: "🏠", label: "Dashboard", path: "/dashboard" },
    { icon: "🗺️", label: "Map", path: "/map" },
    { icon: "📝", label: "Requests", path: "/requests" },
    { icon: "🎁", label: "Donations", path: "/donations" },
    { icon: "👥", label: "About Us", path: "/about" },
    { icon: "❓", label: "Help & Support", path: "/help" },
  ];

  return (
    <div className="dashboard">

      {/* SIDEBAR */}
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

      {/* MAIN CONTENT */}
      <div className="main">

        {/* HEADER */}
        <div className="header">
          <div className="header-left">
            <h1>Welcome, {user || "Username"}!</h1>
            <p>Here's what's happening today</p>
          </div>

          <div className="profile">
            <div className="notification-icon">🔔</div>
            <div className="avatar">US</div>
          </div>
        </div>

        {/* REQUEST PANEL */}
        <div className="requests">

          <div className="requests-header">
            <h2>
              Recent Requests
              <span className="count">0 total</span>
            </h2>

            <div className="actions">
              <button className="request-btn">
                REQUEST/DONATE
              </button>

              <button className="history-btn">
                View History
              </button>
            </div>
          </div>

          <div className="request-cards">
            <div className="request-card">No requests yet</div>
            <div className="request-card">No requests yet</div>
          </div>

        </div>

      </div>

    </div>
  );
}