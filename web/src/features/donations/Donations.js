import { useEffect, useState, useCallback } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import Sidebar from "../../shared/components/Sidebar";
import { supabase } from "../../supabaseClient";
import donationService from "./DonationService";
import "../dashboard/Dashboard.css";
import "./Donations.css";

// ── Toast ─────────────────────────────────────────────────────────────────────
function Toast({ message, type }) {
  if (!message) return null;
  return (
    <div className={`toast toast-${type}`}>
      <span className="toast-icon">{type === "success" ? "✅" : "❌"}</span>
      <span>{message}</span>
    </div>
  );
}

// ── Helpers ───────────────────────────────────────────────────────────────────
function formatDate(iso) {
  if (!iso) return "—";
  return new Date(iso).toLocaleString("en-US", {
    month: "short", day: "numeric", year: "numeric",
    hour: "numeric", minute: "2-digit", hour12: true,
  });
}

function formatAmount(amount) {
  return new Intl.NumberFormat("en-PH", {
    style: "currency", currency: "PHP", minimumFractionDigits: 2,
  }).format(amount);
}

function getStatusStyle(status) {
  if (status === "Completed") return { bg: "#dcfce7", color: "#166534", icon: "✅" };
  if (status === "Failed")    return { bg: "#fee2e2", color: "#991b1b", icon: "❌" };
  if (status === "Refunded")  return { bg: "#e0e7ff", color: "#3730a3", icon: "↩️" };
  return                             { bg: "#fef9c3", color: "#854d0e", icon: "⏳" };
}

function getSeverityColor(level) {
  if (level === "Critical") return "#dc2626";
  if (level === "High")     return "#ea580c";
  if (level === "Medium")   return "#ca8a04";
  return "#16a34a";
}

// ── Donation card ─────────────────────────────────────────────────────────────
function DonationCard({ donation, username, onGoToMap }) {
  const status   = getStatusStyle(donation.status);
  const disaster = donation.disasters;

  return (
    <div className="don-card">
      <div className="don-card-header">
        <div className="don-card-left">
          <div className="don-avatar">💙</div>
          <div>
            <p className="don-amount">{formatAmount(donation.amount)}</p>
            <p className="don-donor">by {username ?? "You"}</p>
          </div>
        </div>
        <span className="don-status-chip" style={{ background: status.bg, color: status.color }}>
          {status.icon} {donation.status}
        </span>
      </div>

      <div className="don-divider" />

      {/* Disaster info */}
      {disaster && (
        <div className="don-disaster-row">
          <div className="don-disaster-info">
            <span className="don-label">Disaster</span>
            <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
              <span
                className="don-severity-dot"
                style={{ background: getSeverityColor(disaster.severity_level) }}
              />
              <span className="don-disaster-title">{disaster.title}</span>
            </div>
          </div>
          <button
            className="don-map-btn"
            onClick={() => onGoToMap(disaster)}
            title="View on map"
          >
            🗺️ View on Map
          </button>
        </div>
      )}

      <div className="don-meta-row">
        <div className="don-meta-item">
          <span className="don-label">Date &amp; Time</span>
          <span className="don-meta-value">{formatDate(donation.donated_at)}</span>
        </div>
        <div className="don-meta-item">
          <span className="don-label">Method</span>
          <span className="don-meta-value don-gcash">💳 GCash</span>
        </div>
      </div>
    </div>
  );
}

// ── Empty state ───────────────────────────────────────────────────────────────
function EmptyState({ onGoMap }) {
  return (
    <div className="don-empty">
      <div className="don-empty-icon">💙</div>
      <h3 className="don-empty-title">No donations yet</h3>
      <p className="don-empty-sub">
        Go to the map, select a disaster point, and click Donate to support relief efforts.
      </p>
      <button className="don-go-map-btn" onClick={onGoMap}>
        🗺️ Go to Map
      </button>
    </div>
  );
}

// ── Main page ─────────────────────────────────────────────────────────────────
export default function DonationsPage() {
  const navigate       = useNavigate();
  const [searchParams] = useSearchParams();

  const [donations,   setDonations]   = useState([]);
  const [username,    setUsername]    = useState("");
  const [currentUID,  setCurrentUID]  = useState(null);
  const [loading,     setLoading]     = useState(true);
  const [activeTab,   setActiveTab]   = useState("All");
  const [toast,       setToast]       = useState({ message: "", type: "" });

  const STATUS_TABS = ["All", "Completed", "Pending", "Failed"];

  const showToast = (message, type = "success") => {
    setToast({ message, type });
    setTimeout(() => setToast({ message: "", type: "" }), 4000);
  };

  // ── Handle PayMongo redirect back ─────────────────────────────────────────
  useEffect(() => {
    const status     = searchParams.get("status");
    const donationId = searchParams.get("donation_id");
    if (status === "success") {
      showToast("🎉 Donation successful! Thank you for your support.", "success");
    } else if (status === "cancelled") {
      showToast("Donation was cancelled.", "error");
    }
    // Clean URL params
    if (status) {
      navigate("/donations", { replace: true });
    }
  }, [searchParams, navigate]);

  // ── Auth & fetch ──────────────────────────────────────────────────────────
  useEffect(() => {
    supabase.auth.getSession().then(async ({ data: { session } }) => {
      if (!session) { navigate("/"); return; }
      setCurrentUID(session.user.id);

      const { data: userData } = await supabase
        .from("users")
        .select("username")
        .eq("id", session.user.id)
        .single();
      setUsername(userData?.username ?? "");
    });
  }, [navigate]);

  const fetchDonations = useCallback(async () => {
    if (!currentUID) return;
    setLoading(true);
    try {
      const data = await donationService.getByUser(currentUID);
      setDonations(data ?? []);
    } catch (err) {
      console.error("Failed to fetch donations:", err.message);
    } finally {
      setLoading(false);
    }
  }, [currentUID]);

  useEffect(() => { fetchDonations(); }, [fetchDonations]);

  // ── Go to map centered on disaster ────────────────────────────────────────
  const handleGoToMap = (disaster) => {
    navigate("/map", { state: { focusDisasterId: disaster.id } });
  };

  // ── Stats ─────────────────────────────────────────────────────────────────
  const totalDonated = donations
    .filter((d) => d.status === "Completed")
    .reduce((sum, d) => sum + parseFloat(d.amount ?? 0), 0);

  const filtered = donations.filter((d) =>
    activeTab === "All" || d.status === activeTab
  );

  const counts = STATUS_TABS.reduce((acc, tab) => {
    acc[tab] = tab === "All"
      ? donations.length
      : donations.filter((d) => d.status === tab).length;
    return acc;
  }, {});

  return (
    <div className="dashboard">
      <Toast message={toast.message} type={toast.type} />
      <Sidebar />

      <div className="main don-main">

        {/* ── Header ── */}
        <div className="don-page-header">
          <div>
            <h1 className="don-page-title">💙 My Donations</h1>
            <p className="don-page-sub">Track your contributions to disaster relief efforts</p>
          </div>
          <button className="don-go-map-btn" onClick={() => navigate("/map")}>
            🗺️ Donate on Map
          </button>
        </div>

        {/* ── Summary stats ── */}
        {!loading && donations.length > 0 && (
          <div className="don-stats-row">
            <div className="don-stat">
              <p className="don-stat-value">{donations.length}</p>
              <p className="don-stat-label">Total donations</p>
            </div>
            <div className="don-stat">
              <p className="don-stat-value" style={{ color: "#16a34a" }}>{formatAmount(totalDonated)}</p>
              <p className="don-stat-label">Total contributed</p>
            </div>
            <div className="don-stat">
              <p className="don-stat-value" style={{ color: "#f97316" }}>
                {donations.filter((d) => d.status === "Pending").length}
              </p>
              <p className="don-stat-label">Pending</p>
            </div>
            <div className="don-stat">
              <p className="don-stat-value">
                {[...new Set(donations.map((d) => d.disaster_id).filter(Boolean))].length}
              </p>
              <p className="don-stat-label">Disasters supported</p>
            </div>
          </div>
        )}

        {/* ── Tabs ── */}
        <div className="don-tabs">
          {STATUS_TABS.map((tab) => (
            <button
              key={tab}
              className={`don-tab ${activeTab === tab ? "don-tab-active" : ""}`}
              onClick={() => setActiveTab(tab)}
            >
              {tab}
              <span className="don-tab-count">{counts[tab]}</span>
            </button>
          ))}
        </div>

        {/* ── Content ── */}
        <div className="don-content">
          {loading ? (
            <div className="don-loading">
              <div className="don-spinner" />
              <p>Loading your donations…</p>
            </div>
          ) : filtered.length === 0 ? (
            <EmptyState onGoMap={() => navigate("/map")} />
          ) : (
            <div className="don-grid">
              {filtered.map((d) => (
                <DonationCard
                  key={d.id}
                  donation={d}
                  username={username}
                  onGoToMap={handleGoToMap}
                />
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}