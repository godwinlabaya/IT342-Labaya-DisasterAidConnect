import { useEffect, useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { supabase } from "../../../supabaseClient";
import { useAdminAuth } from "../useAdminAuth";
import AdminSidebar from "../AdminSidebar";
import "./AdminDashboard.css";

function Toast({ message, type }) {
  if (!message) return null;
  return (
    <div className={`toast toast-${type}`}>
      <span className="toast-icon">{type === "success" ? "✅" : "❌"}</span>
      <span>{message}</span>
    </div>
  );
}

function StatCard({ icon, label, value, color }) {
  return (
    <div className="admin-stat-card">
      <div className="admin-stat-icon" style={{ background: color + "20", color }}>
        {icon}
      </div>
      <div>
        <p className="admin-stat-value">{value}</p>
        <p className="admin-stat-label">{label}</p>
      </div>
    </div>
  );
}

export default function AdminDashboard() {
  const navigate = useNavigate();
  const { username, loading } = useAdminAuth();

  const [stats,  setStats]  = useState({ disasters: 0, aidRequests: 0, donations: 0, users: 0 });
  const [toast,  setToast]  = useState({ message: "", type: "" });

  const showToast = (message, type = "success") => {
    setToast({ message, type });
    setTimeout(() => setToast({ message: "", type: "" }), 3000);
  };

  const logout = async () => {
    await supabase.auth.signOut();
    showToast("Logged out successfully!", "success");
    setTimeout(() => navigate("/"), 1500);
  };

  const fetchStats = useCallback(async () => {
    const [
      { count: disasters },
      { count: aidRequests },
      { count: donations },
      { count: users },
    ] = await Promise.all([
      supabase.from("disasters").select("*",   { count: "exact", head: true }),
      supabase.from("aid_requests").select("*", { count: "exact", head: true }),
      supabase.from("donations").select("*",   { count: "exact", head: true }),
      supabase.from("users").select("*",       { count: "exact", head: true }),
    ]);
    setStats({ disasters, aidRequests, donations, users });
  }, []);

  useEffect(() => { if (!loading) fetchStats(); }, [loading, fetchStats]);

  if (loading) return (
    <div className="admin-layout">
      <div className="admin-loading"><div className="admin-spinner" /><p>Loading…</p></div>
    </div>
  );

  return (
    <div className="admin-layout">
      <Toast message={toast.message} type={toast.type} />
      <AdminSidebar onLogout={logout} />

      <div className="admin-main">
        <div className="admin-page-header">
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
            <div>
              <h1 className="admin-page-title">Welcome, {username}! 🛡️</h1>
              <p className="admin-page-sub">Here's an overview of the entire system</p>
            </div>
            <div className="admin-avatar">{username?.slice(0, 2).toUpperCase()}</div>
          </div>
        </div>

        {/* Stats */}
        <div className="admin-stats-grid">
          <StatCard icon="🗺️" label="Total Disasters"   value={stats.disasters}   color="#ef4444" />
          <StatCard icon="📋" label="Aid Requests"      value={stats.aidRequests} color="#f97316" />
          <StatCard icon="💸" label="Total Donations"   value={stats.donations}   color="#8b5cf6" />
          <StatCard icon="👥" label="Registered Users"  value={stats.users}       color="#3b82f6" />
        </div>

        {/* Quick nav cards */}
        <div className="admin-quick-grid">
          {[
            { icon: "🗺️", label: "Manage Disasters",    sub: "View and delete disaster reports",      path: "/admin/disasters",    color: "#ef4444" },
            { icon: "📋", label: "Aid Requests",         sub: "Approve or reject aid requests",        path: "/admin/aid-requests", color: "#f97316" },
            { icon: "💸", label: "Donations",            sub: "View all donation records",              path: "/admin/donations",    color: "#8b5cf6" },
            { icon: "👥", label: "Users",                sub: "View all registered users",              path: "/admin/users",        color: "#3b82f6" },
          ].map((item) => (
            <div
              key={item.path}
              className="admin-quick-card"
              onClick={() => navigate(item.path)}
            >
              <div className="admin-quick-icon" style={{ background: item.color + "20", color: item.color }}>
                {item.icon}
              </div>
              <div>
                <p className="admin-quick-label">{item.label}</p>
                <p className="admin-quick-sub">{item.sub}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}