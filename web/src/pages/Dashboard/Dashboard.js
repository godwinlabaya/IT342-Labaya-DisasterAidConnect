import { useEffect, useState, useCallback } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import "./Dashboard.css";
import { supabase } from "../../supabaseClient";
import { useAuth } from "../../hooks/useAuth";
import disasterService from "../../services/disasterService";

function getSeverityColor(level) {
  if (level === "Critical") return { bg: "#fee2e2", text: "#991b1b", border: "#fca5a5" };
  if (level === "High")     return { bg: "#ffedd5", text: "#9a3412", border: "#fdba74" };
  if (level === "Medium")   return { bg: "#fef9c3", text: "#854d0e", border: "#fde047" };
  return                           { bg: "#dcfce7", text: "#166534", border: "#86efac" };
}

function getStatusStyle(status) {
  if (status === "Active")     return { bg: "#dcfce7", text: "#166534", dot: "#16a34a" };
  if (status === "Monitoring") return { bg: "#fef3c7", text: "#92400e", dot: "#d97706" };
  if (status === "Resolved")   return { bg: "#e0e7ff", text: "#3730a3", dot: "#6366f1" };
  return                              { bg: "#f3f4f6", text: "#6b7280", dot: "#9ca3af" };
}

function formatDate(iso) {
  if (!iso) return "—";
  return new Date(iso).toLocaleString("en-US", {
    month: "short", day: "numeric", year: "numeric",
    hour: "numeric", minute: "2-digit", hour12: true,
  });
}

function formatAmount(amount) {
  return new Intl.NumberFormat("en-PH", {
    style: "currency", currency: "PHP", minimumFractionDigits: 0,
  }).format(amount);
}

function getDonationStatusStyle(status) {
  if (status === "Completed") return { bg: "#dcfce7", text: "#166534" };
  if (status === "Failed")    return { bg: "#fee2e2", text: "#991b1b" };
  if (status === "Refunded")  return { bg: "#e0e7ff", text: "#3730a3" };
  return                             { bg: "#fef9c3", text: "#854d0e" };
}

// ── Stat card ─────────────────────────────────────────────────────────────────
function StatCard({ icon, label, value, sub, color }) {
  return (
    <div className="db-stat-card">
      <div className="db-stat-icon" style={{ background: color + "20", color }}>
        {icon}
      </div>
      <div>
        <p className="db-stat-value">{value}</p>
        <p className="db-stat-label">{label}</p>
        {sub && <p className="db-stat-sub">{sub}</p>}
      </div>
    </div>
  );
}

// ── Disaster row ──────────────────────────────────────────────────────────────
function DisasterRow({ d, onClick }) {
  const sev    = getSeverityColor(d.severity_level);
  const status = getStatusStyle(d.status);
  return (
    <div className="db-row" onClick={onClick}>
      <div className="db-row-left">
        <span className="db-row-dot" style={{ background: status.dot }} />
        <div>
          <p className="db-row-title">{d.title}</p>
          <p className="db-row-meta">{formatDate(d.created_at)}</p>
        </div>
      </div>
      <div className="db-row-right">
        <span className="db-badge" style={{ background: sev.bg, color: sev.text, border: `1px solid ${sev.border}` }}>
          {d.severity_level}
        </span>
        <span className="db-badge" style={{ background: status.bg, color: status.text }}>
          {d.status}
        </span>
      </div>
    </div>
  );
}

// ── Donation row ──────────────────────────────────────────────────────────────
function DonationRow({ d }) {
  const style = getDonationStatusStyle(d.status);
  return (
    <div className="db-row">
      <div className="db-row-left">
        <div className="db-donation-icon">💸</div>
        <div>
          <p className="db-row-title">{formatAmount(d.amount)}</p>
          <p className="db-row-meta">{formatDate(d.donated_at)}</p>
        </div>
      </div>
      <div className="db-row-right">
        <span className="db-badge" style={{ background: style.bg, color: style.text }}>
          {d.status}
        </span>
      </div>
    </div>
  );
}

// ── Main component ────────────────────────────────────────────────────────────
export default function Dashboard() {
  const navigate  = useNavigate();
  const location  = useLocation();
  const { username } = useAuth({ redirectIfUnauthenticated: true });

  const [currentUID,  setCurrentUID]  = useState(null);
  const [disasters,   setDisasters]   = useState([]);
  const [donations,   setDonations]   = useState([]);
  const [allDisasters, setAllDisasters] = useState([]);
  const [loading,     setLoading]     = useState(true);
  const [activeTab,   setActiveTab]   = useState("requests"); // "requests" | "donations"

  const logout = async () => {
    await supabase.auth.signOut();
    navigate("/");
  };

  const menuItems = [
    { icon: "🏠", label: "Dashboard",    path: "/dashboard" },
    { icon: "🗺️", label: "Map",          path: "/map" },
    { icon: "📝", label: "Requests",     path: "/requests" },
    { icon: "🎁", label: "Donations",    path: "/donations" },
    { icon: "👥", label: "About Us",     path: "/about" },
    { icon: "❓", label: "Help & Support", path: "/help" },
  ];

  // ── Auth ──────────────────────────────────────────────────────────────────
  useEffect(() => {
    supabase.auth.getSession().then(({ data: { session } }) => {
      setCurrentUID(session?.user?.id ?? null);
    });
  }, []);

  // ── Fetch data ────────────────────────────────────────────────────────────
  const fetchData = useCallback(async () => {
    if (!currentUID) return;
    setLoading(true);
    try {
      // My disaster reports (recent 5)
      const myDisasters = await disasterService.getByUser(currentUID);
      setDisasters(myDisasters.slice(0, 5));

      // All disasters for stats
      const all = await disasterService.getAll();
      setAllDisasters(all);

      // My donations (recent 5)
      const { data: myDonations } = await supabase
        .from("donations")
        .select("*")
        .eq("user_id", currentUID)
        .order("donated_at", { ascending: false })
        .limit(5);
      setDonations(myDonations ?? []);
    } catch (err) {
      console.error("Dashboard fetch error:", err.message);
    } finally {
      setLoading(false);
    }
  }, [currentUID]);

  useEffect(() => { fetchData(); }, [fetchData]);

  // ── Derived stats ─────────────────────────────────────────────────────────
  const totalReports  = disasters.length;
  const activeCount   = allDisasters.filter((d) => d.status === "Active").length;
  const criticalCount = allDisasters.filter((d) => d.severity_level === "Critical").length;
  const totalDonated  = donations
    .filter((d) => d.status === "Completed")
    .reduce((sum, d) => sum + parseFloat(d.amount ?? 0), 0);

  return (
    <div className="dashboard">

      {/* ── SIDEBAR ── */}
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

      {/* ── MAIN ── */}
      <div className="main">

        {/* Header */}
        <div className="header">
          <div className="header-left">
            <h1>Welcome, {username || "User"}!</h1>
            <p>Here's what's happening today</p>
          </div>
          <div className="profile">
            <div className="notification-icon">🔔</div>
            <div className="avatar">
              {username ? username.slice(0, 2).toUpperCase() : "US"}
            </div>
          </div>
        </div>

        {/* ── Stat cards ── */}
        <div className="db-stats-grid">
          <StatCard icon="📋" label="My reports"       value={totalReports}             color="#3b82f6" />
          <StatCard icon="🔴" label="Active disasters" value={activeCount}              color="#ef4444" />
          <StatCard icon="⚠️" label="Critical alerts"  value={criticalCount}            color="#f97316" />
          <StatCard icon="💙" label="Total donated"    value={formatAmount(totalDonated)} color="#8b5cf6" />
        </div>

        {/* ── Panel ── */}
        <div className="requests">

          {/* Panel header */}
          <div className="requests-header">
            <div className="db-tabs">
              <button
                className={`db-tab ${activeTab === "requests" ? "db-tab-active" : ""}`}
                onClick={() => setActiveTab("requests")}
              >
                📋 My Requests
                <span className="count" style={{ marginLeft: 8 }}>{disasters.length}</span>
              </button>
              <button
                className={`db-tab ${activeTab === "donations" ? "db-tab-active" : ""}`}
                onClick={() => setActiveTab("donations")}
              >
                💸 Donation History
                <span className="count" style={{ marginLeft: 8 }}>{donations.length}</span>
              </button>
            </div>

            <div className="actions">
              <button className="request-btn" onClick={() => navigate("/map")}>
                + Add on Map
              </button>
              <button className="history-btn" onClick={() => navigate(activeTab === "requests" ? "/requests" : "/donations")}>
                View All
              </button>
            </div>
          </div>

          {/* Panel body */}
          {loading ? (
            <div className="db-loading">
              <div className="db-spinner" />
              <p>Loading…</p>
            </div>
          ) : activeTab === "requests" ? (
            disasters.length === 0 ? (
              <div className="db-empty">
                <p className="db-empty-icon">🗺️</p>
                <p className="db-empty-title">No disaster reports yet</p>
                <p className="db-empty-sub">Go to the map to add your first disaster point.</p>
                <button className="request-btn" style={{ marginTop: 16 }} onClick={() => navigate("/map")}>
                  Go to Map
                </button>
              </div>
            ) : (
              <div className="db-list">
                {disasters.map((d) => (
                  <DisasterRow
                    key={d.id}
                    d={d}
                    onClick={() => navigate("/requests")}
                  />
                ))}
                <button className="db-view-all-btn" onClick={() => navigate("/requests")}>
                  View all requests →
                </button>
              </div>
            )
          ) : (
            donations.length === 0 ? (
              <div className="db-empty">
                <p className="db-empty-icon">💙</p>
                <p className="db-empty-title">No donations yet</p>
                <p className="db-empty-sub">Visit the Donations page to support a disaster relief effort.</p>
                <button className="request-btn" style={{ marginTop: 16 }} onClick={() => navigate("/donations")}>
                  Donate Now
                </button>
              </div>
            ) : (
              <div className="db-list">
                {donations.map((d) => (
                  <DonationRow key={d.id} d={d} />
                ))}
                <button className="db-view-all-btn" onClick={() => navigate("/donations")}>
                  View all donations →
                </button>
              </div>
            )
          )}
        </div>

      </div>
    </div>
  );
}