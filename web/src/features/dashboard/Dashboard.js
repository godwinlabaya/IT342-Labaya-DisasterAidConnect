import { useEffect, useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import "./Dashboard.css";
import { supabase } from "../../supabaseClient";
import { useAuth } from "../auth/useAuth";
import disasterService from "../disaster/disasterService";
import Sidebar from "../../shared/components/Sidebar";

// ── Toast ─────────────────────────────────────────────────────────────────────
function Toast({ message, type }) {
  if (!message) return null;
  return (
    <div className={`toast toast-${type}`}>
      <i className={`ti ${type === "success" ? "ti-circle-check" : "ti-alert-circle"}`} aria-hidden="true" />
      <span>{message}</span>
    </div>
  );
}

// ── Helpers ───────────────────────────────────────────────────────────────────
function getSeverityConfig(level) {
  const map = {
    Critical: { bg: "#fef2f2", text: "#b91c1c", border: "#fecaca", dot: "#ef4444", icon: "ti-flame" },
    High:     { bg: "#fff7ed", text: "#c2410c", border: "#fed7aa", dot: "#f97316", icon: "ti-alert-triangle" },
    Medium:   { bg: "#fefce8", text: "#a16207", border: "#fde68a", dot: "#eab308", icon: "ti-alert-circle" },
    Low:      { bg: "#f0fdf4", text: "#15803d", border: "#bbf7d0", dot: "#22c55e", icon: "ti-info-circle" },
  };
  return map[level] ?? map.Low;
}

function getStatusConfig(status) {
  const map = {
    Active:     { bg: "#f0fdf4", text: "#15803d", dot: "#22c55e" },
    Monitoring: { bg: "#fffbeb", text: "#92400e", dot: "#f59e0b" },
    Resolved:   { bg: "#eff6ff", text: "#1d4ed8", dot: "#3b82f6" },
  };
  return map[status] ?? { bg: "#f9fafb", text: "#6b7280", dot: "#9ca3af" };
}

function getDonationStatusConfig(status) {
  const map = {
    Completed: { bg: "#f0fdf4", text: "#15803d", icon: "ti-circle-check" },
    Failed:    { bg: "#fef2f2", text: "#b91c1c", icon: "ti-circle-x" },
    Refunded:  { bg: "#eff6ff", text: "#1d4ed8", icon: "ti-refresh" },
    Pending:   { bg: "#fffbeb", text: "#92400e", icon: "ti-clock" },
  };
  return map[status] ?? map.Pending;
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

// ── Stat card ─────────────────────────────────────────────────────────────────
function StatCard({ icon, label, value, iconBg, iconColor }) {
  return (
    <div className="db-stat-card">
      <div className="db-stat-top">
        <div className="db-stat-icon" style={{ background: iconBg, color: iconColor }}>
          <i className={`ti ${icon}`} aria-hidden="true" />
        </div>
        <i className="ti ti-trending-up db-stat-trend" aria-hidden="true" />
      </div>
      <p className="db-stat-value">{value}</p>
      <p className="db-stat-label">{label}</p>
    </div>
  );
}

// ── Disaster row ──────────────────────────────────────────────────────────────
function DisasterRow({ d, onClick }) {
  const sev    = getSeverityConfig(d.severity_level);
  const status = getStatusConfig(d.status);
  return (
    <div className="db-row" onClick={onClick} role="button" tabIndex={0}
      onKeyDown={e => e.key === "Enter" && onClick()}>
      <div className="db-row-left">
        <div className="db-row-icon" style={{ background: sev.bg, color: sev.text }}>
          <i className={`ti ${sev.icon}`} aria-hidden="true" />
        </div>
        <div>
          <p className="db-row-title">{d.title}</p>
          <p className="db-row-meta">
            <i className="ti ti-calendar-event" aria-hidden="true" />
            {formatDate(d.created_at)}
          </p>
        </div>
      </div>
      <div className="db-row-right">
        <span className="db-badge" style={{ background: sev.bg, color: sev.text, border: `1px solid ${sev.border}` }}>
          <span className="db-badge-dot" style={{ background: sev.dot }} />{d.severity_level}
        </span>
        <span className="db-badge" style={{ background: status.bg, color: status.text }}>
          <span className="db-badge-dot" style={{ background: status.dot }} />{d.status}
        </span>
        <i className="ti ti-chevron-right db-row-arrow" aria-hidden="true" />
      </div>
    </div>
  );
}

// ── Donation row ──────────────────────────────────────────────────────────────
function DonationRow({ d }) {
  const cfg = getDonationStatusConfig(d.status);
  return (
    <div className="db-row">
      <div className="db-row-left">
        <div className="db-row-icon" style={{ background: cfg.bg, color: cfg.text }}>
          <i className="ti ti-heart" aria-hidden="true" />
        </div>
        <div>
          <p className="db-row-title">{formatAmount(d.amount)}</p>
          <p className="db-row-meta">
            <i className="ti ti-calendar-event" aria-hidden="true" />
            {formatDate(d.donated_at)}
          </p>
        </div>
      </div>
      <div className="db-row-right">
        <span className="db-badge" style={{ background: cfg.bg, color: cfg.text }}>
          <i className={`ti ${cfg.icon}`} aria-hidden="true" />{d.status}
        </span>
      </div>
    </div>
  );
}

// ── Main ──────────────────────────────────────────────────────────────────────
export default function Dashboard() {
  const navigate = useNavigate();
  const { username } = useAuth({ redirectIfUnauthenticated: true });

  const [currentUID,   setCurrentUID]   = useState(null);
  const [disasters,    setDisasters]    = useState([]);
  const [donations,    setDonations]    = useState([]);
  const [allDisasters, setAllDisasters] = useState([]);
  const [loading,      setLoading]      = useState(true);
  const [activeTab,    setActiveTab]    = useState("requests");
  const [toast,        setToast]        = useState({ message: "", type: "" });

  const showToast = (message, type = "success") => {
    setToast({ message, type });
    setTimeout(() => setToast({ message: "", type: "" }), 3000);
  };

  useEffect(() => {
    supabase.auth.getSession().then(({ data: { session } }) => {
      setCurrentUID(session?.user?.id ?? null);
    });
  }, []);

  const logout = async () => {
    showToast("Logged out successfully. See you soon!", "success");
    setTimeout(async () => {
      await supabase.auth.signOut();
      navigate("/");
    }, 2000);
  };

  const fetchData = useCallback(async () => {
    if (!currentUID) return;
    setLoading(true);
    try {
      const myDisasters = await disasterService.getByUser(currentUID);
      setDisasters(myDisasters.slice(0, 5));
      const all = await disasterService.getAll();
      setAllDisasters(all);
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

  const activeCount   = allDisasters.filter(d => d.status === "Active").length;
  const criticalCount = allDisasters.filter(d => d.severity_level === "Critical").length;
  const totalDonated  = donations
    .filter(d => d.status === "Completed")
    .reduce((sum, d) => sum + parseFloat(d.amount ?? 0), 0);

  const hour = new Date().getHours();
  const greeting = hour < 12 ? "Good morning" : hour < 17 ? "Good afternoon" : "Good evening";
  const initials = username ? username.slice(0, 2).toUpperCase() : "US";

  return (
    <div className="dashboard">
      <Toast message={toast.message} type={toast.type} />
      <Sidebar onLogout={logout} />

      <div className="main">

        {/* Header */}
        <div className="header">
          <div className="header-left">
            <p className="db-greeting">{greeting},</p>
            <h1>{username || "User"} 👋</h1>
            <p>Here's what's happening in your area.</p>
          </div>
          <div className="profile">
            <button className="notification-icon" aria-label="Notifications">
              <i className="ti ti-bell" aria-hidden="true" />
              <span className="notif-dot" />
            </button>
            <button className="notification-icon" aria-label="Refresh" onClick={fetchData}>
              <i className="ti ti-refresh" aria-hidden="true" />
            </button>
            <div className="avatar">{initials}</div>
          </div>
        </div>

        {/* Stat cards */}
        <div className="db-stats-grid">
          <StatCard icon="ti-clipboard-list"  label="My Reports"       value={disasters.length}           iconBg="#eff6ff" iconColor="#2563eb" />
          <StatCard icon="ti-flame"           label="Active Disasters" value={activeCount}                iconBg="#fef2f2" iconColor="#dc2626" />
          <StatCard icon="ti-alert-triangle"  label="Critical Alerts"  value={criticalCount}             iconBg="#fff7ed" iconColor="#ea580c" />
          <StatCard icon="ti-heart-handshake" label="Total Donated"    value={formatAmount(totalDonated)} iconBg="#f5f3ff" iconColor="#7c3aed" />
        </div>

        {/* Panel */}
        <div className="requests">
          <div className="requests-header">
            <div className="db-tabs">
              <button
                className={`db-tab ${activeTab === "requests" ? "db-tab-active" : ""}`}
                onClick={() => setActiveTab("requests")}
              >
                <i className="ti ti-clipboard-list" aria-hidden="true" />
                My Requests
                <span className="count">{disasters.length}</span>
              </button>
              <button
                className={`db-tab ${activeTab === "donations" ? "db-tab-active" : ""}`}
                onClick={() => setActiveTab("donations")}
              >
                <i className="ti ti-heart" aria-hidden="true" />
                Donations
                <span className="count">{donations.length}</span>
              </button>
            </div>
            <div className="actions">
              <button className="request-btn" onClick={() => navigate("/map")}>
                <i className="ti ti-map-pin" aria-hidden="true" />
                Add to Map
              </button>
              <button className="history-btn" onClick={() => navigate(activeTab === "requests" ? "/requests" : "/donations")}>
                View All
                <i className="ti ti-arrow-right" aria-hidden="true" />
              </button>
            </div>
          </div>

          <div className="db-divider" />

          {loading ? (
            <div className="db-loading">
              <div className="db-spinner" />
              <span>Loading your data…</span>
            </div>
          ) : activeTab === "requests" ? (
            disasters.length === 0 ? (
              <div className="db-empty">
                <div className="db-empty-icon">
                  <i className="ti ti-map-2" aria-hidden="true" />
                </div>
                <p className="db-empty-title">No disaster reports yet</p>
                <p className="db-empty-sub">Head to the map to log your first disaster point and help your community.</p>
                <button className="request-btn" style={{ marginTop: 20 }} onClick={() => navigate("/map")}>
                  <i className="ti ti-map-pin" aria-hidden="true" />Open Map
                </button>
              </div>
            ) : (
              <div className="db-list">
                {disasters.map(d => <DisasterRow key={d.id} d={d} onClick={() => navigate("/requests")} />)}
                <button className="db-view-all-btn" onClick={() => navigate("/requests")}>
                  View all requests <i className="ti ti-arrow-right" aria-hidden="true" />
                </button>
              </div>
            )
          ) : (
            donations.length === 0 ? (
              <div className="db-empty">
                <div className="db-empty-icon">
                  <i className="ti ti-heart" aria-hidden="true" />
                </div>
                <p className="db-empty-title">No donations yet</p>
                <p className="db-empty-sub">Support a relief effort and make a difference today.</p>
                <button className="request-btn" style={{ marginTop: 20 }} onClick={() => navigate("/donations")}>
                  <i className="ti ti-heart" aria-hidden="true" />Donate Now
                </button>
              </div>
            ) : (
              <div className="db-list">
                {donations.map(d => <DonationRow key={d.id} d={d} />)}
                <button className="db-view-all-btn" onClick={() => navigate("/donations")}>
                  View all donations <i className="ti ti-arrow-right" aria-hidden="true" />
                </button>
              </div>
            )
          )}
        </div>
      </div>
    </div>
  );
}