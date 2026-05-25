import { useEffect, useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { supabase } from "../../../supabaseClient";
import { useAdminAuth } from "../useAdminAuth";
import AdminSidebar from "../AdminSidebar";
import "./AdminDashboard.css";
import "../Admin.css";

function Toast({ message, type }) {
  if (!message) return null;
  return (
    <div className={`toast toast-${type}`}>
      <i className={`ti ${type === "success" ? "ti-circle-check" : "ti-alert-circle"} toast-icon`} aria-hidden="true" />
      <span>{message}</span>
    </div>
  );
}

function StatCard({ icon, label, value, iconBg, iconColor }) {
  return (
    <div className="admin-stat-card">
      <div className="admin-stat-icon" style={{ background: iconBg, color: iconColor }}>
        <i className={`ti ${icon}`} aria-hidden="true" />
      </div>
      <div>
        <p className="admin-stat-value">{value}</p>
        <p className="admin-stat-label">{label}</p>
      </div>
    </div>
  );
}

// ── "Aid Requests" replaced with "Map" ──────────────────────────────────────
const QUICK_ITEMS = [
  { icon: "ti-map-2",    label: "Manage Disasters", sub: "View and delete disaster reports",  path: "/admin/disasters", iconBg: "#fef2f2", iconColor: "#dc2626" },
  { icon: "ti-map-pin",  label: "Disaster Map",     sub: "View and manage map points",        path: "/admin/map",       iconBg: "#eff6ff", iconColor: "#2563eb" },
  { icon: "ti-heart",    label: "Donations",         sub: "View all donation records",         path: "/admin/donations", iconBg: "#f5f3ff", iconColor: "#7c3aed" },
  { icon: "ti-users",    label: "Users",             sub: "View all registered users",         path: "/admin/users",     iconBg: "#f0fdf4", iconColor: "#16a34a" },
];

export default function AdminDashboard() {
  const navigate = useNavigate();
  const { username, loading, authed } = useAdminAuth();

  const [stats, setStats] = useState({ disasters: 0, mapPoints: 0, donations: 0, users: 0 });
  const [toast, setToast] = useState({ message: "", type: "" });

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
      { count: mapPoints },
      { count: donations },
      { count: users },
    ] = await Promise.all([
      supabase.from("disasters").select("*",  { count: "exact", head: true }),
      supabase.from("disasters").select("*",  { count: "exact", head: true }),
      supabase.from("donations").select("*",  { count: "exact", head: true }),
      supabase.from("users").select("*",      { count: "exact", head: true }),
    ]);
    setStats({ disasters, mapPoints, donations, users });
  }, []);

  useEffect(() => { if (!loading) fetchStats(); }, [loading, fetchStats]);

  if (!authed) return null;

  return (
    <div className="admin-layout">
      <Toast message={toast.message} type={toast.type} />
      <AdminSidebar onLogout={logout} />

      <div className="admin-main">

        {/* Header */}
        <div className="admin-dash-header">
          <div>
            <p className="admin-dash-greeting">Admin Panel</p>
            <h1 className="admin-dash-title">Welcome, {username}!</h1>
            <p className="admin-dash-sub">Here's an overview of the entire system</p>
          </div>
          <div className="admin-avatar">{username?.slice(0, 2).toUpperCase()}</div>
        </div>

        {/* Stats — "Aid Requests" replaced with "Map Points" */}
        <div className="admin-stats-grid">
          <StatCard icon="ti-map-2"      label="Total Disasters"  value={stats.disasters}  iconBg="#fef2f2" iconColor="#dc2626" />
          <StatCard icon="ti-map-pin"    label="Map Points"       value={stats.mapPoints}  iconBg="#eff6ff" iconColor="#2563eb" />
          <StatCard icon="ti-heart"      label="Total Donations"  value={stats.donations}  iconBg="#f5f3ff" iconColor="#7c3aed" />
          <StatCard icon="ti-users"      label="Registered Users" value={stats.users}      iconBg="#f0fdf4" iconColor="#16a34a" />
        </div>

        {/* Quick nav */}
        <div className="admin-quick-grid">
          {QUICK_ITEMS.map((item) => (
            <div key={item.path} className="admin-quick-card" onClick={() => navigate(item.path)}>
              <div className="admin-quick-icon" style={{ background: item.iconBg, color: item.iconColor }}>
                <i className={`ti ${item.icon}`} aria-hidden="true" />
              </div>
              <div>
                <p className="admin-quick-label">{item.label}</p>
                <p className="admin-quick-sub">{item.sub}</p>
              </div>
              <i className="ti ti-chevron-right admin-quick-arrow" aria-hidden="true" />
            </div>
          ))}
        </div>

      </div>
    </div>
  );
}