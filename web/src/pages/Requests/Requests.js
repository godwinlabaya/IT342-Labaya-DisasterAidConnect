import { useEffect, useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import Sidebar from "../../components/Sidebar";
import "./Requests.css";
import { supabase } from "../../supabaseClient";
import { getSeverityStyle } from "../../utils/iconFactory";
import disasterService from "../../services/disasterService";

const STATUS_TABS = ["All", "Active", "Monitoring", "Resolved"];

function getStatusStyle(status) {
  if (status === "Active")     return { bg: "#dcfce7", text: "#166534", icon: "🟢" };
  if (status === "Monitoring") return { bg: "#fef3c7", text: "#92400e", icon: "🟡" };
  if (status === "Resolved")   return { bg: "#e0e7ff", text: "#3730a3", icon: "✅" };
  return                              { bg: "#f3f4f6", text: "#6b7280", icon: "⏳" };
}

function formatDate(iso) {
  if (!iso) return "—";
  return new Date(iso).toLocaleString("en-US", {
    month: "short", day: "numeric", year: "numeric",
    hour: "numeric", minute: "2-digit", hour12: true,
  });
}

function EmptyState({ onAdd }) {
  return (
    <div className="req-empty">
      <div className="req-empty-icon">
        <svg width="52" height="52" viewBox="0 0 24 24" fill="none" stroke="#d1d5db" strokeWidth="1.4">
          <path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2"/>
          <rect x="9" y="3" width="6" height="4" rx="1"/>
          <path d="M9 12h6M9 16h4"/>
        </svg>
      </div>
      <h3 className="req-empty-title">No requests found</h3>
      <p className="req-empty-sub">We couldn't find any requests matching your filters.</p>
      <button className="req-go-map-btn" onClick={onAdd}>Add a Disaster Point on Map</button>
    </div>
  );
}

function RequestCard({ disaster, onView, onDelete }) {
  const sev    = getSeverityColor(disaster.severity_level);
  const status = getStatusStyle(disaster.status);

  return (
    <div className="req-card" onClick={() => onView(disaster)}>
      <div className="req-card-top">
        <h3 className="req-card-title">{disaster.title}</h3>
        <div className="req-card-badges">
          <span className="req-badge"
            style={{ background: sev.bg, color: sev.text, border: `1px solid ${sev.border}` }}>
            {disaster.severity_level}
          </span>
          <span className="req-status-chip"
            style={{ background: status.bg, color: status.text }}>
            {status.icon} {disaster.status}
          </span>
        </div>
      </div>

      <p className="req-card-desc">{disaster.description}</p>

      <div className="req-card-meta">
        <span className="req-meta-item">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/>
          </svg>
          {formatDate(disaster.created_at)}
        </span>
        <span className="req-meta-item">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/>
          </svg>
          {disaster.latitude?.toFixed(4)}, {disaster.longitude?.toFixed(4)}
        </span>
      </div>

      <div className="req-card-footer" onClick={(e) => e.stopPropagation()}>
        <button className="req-view-btn" onClick={() => onView(disaster)}>View Details</button>
        <button className="req-delete-btn" onClick={() => onDelete(disaster)}>
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2">
            <polyline points="3 6 5 6 21 6"/>
            <path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/>
            <path d="M10 11v6M14 11v6M9 6V4a1 1 0 011-1h4a1 1 0 011 1v2"/>
          </svg>
          Delete
        </button>
      </div>
    </div>
  );
}

function DetailModal({ disaster, onClose, onDelete }) {
  if (!disaster) return null;
  const sev    = getSeverityColor(disaster.severity_level);
  const status = getStatusStyle(disaster.status);

  return (
    <div className="req-backdrop" onClick={onClose}>
      <div className="req-modal" onClick={(e) => e.stopPropagation()}>
        <div className="req-modal-header">
          <h2>{disaster.title}</h2>
          <button className="req-modal-close" onClick={onClose}>✕</button>
        </div>

        <div className="req-modal-badges">
          <span className="req-badge"
            style={{ background: sev.bg, color: sev.text, border: `1px solid ${sev.border}` }}>
            ⚠ {disaster.severity_level} Severity
          </span>
          <span className="req-status-chip"
            style={{ background: status.bg, color: status.text }}>
            {status.icon} {disaster.status}
          </span>
        </div>

        <div className="req-modal-section">
          <p className="req-modal-label">Description</p>
          <p className="req-modal-value">{disaster.description}</p>
        </div>

        <div className="req-modal-grid">
          <div className="req-modal-section">
            <p className="req-modal-label">📍 Coordinates</p>
            <p className="req-modal-value mono">
              {disaster.latitude?.toFixed(6)}, {disaster.longitude?.toFixed(6)}
            </p>
          </div>
          <div className="req-modal-section">
            <p className="req-modal-label">🕐 Date Added</p>
            <p className="req-modal-value">{formatDate(disaster.created_at)}</p>
          </div>
        </div>

        <div className="req-modal-footer">
          <button className="req-map-btn" onClick={() => { onClose(); window.location.href = "/map"; }}>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/>
            </svg>
            View on Map
          </button>
          <button className="req-modal-delete" onClick={() => onDelete(disaster)}>
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2">
              <polyline points="3 6 5 6 21 6"/>
              <path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/>
            </svg>
            Delete Point
          </button>
        </div>
      </div>
    </div>
  );
}

function ConfirmDialog({ disaster, onConfirm, onCancel, deleting }) {
  return (
    <div className="req-backdrop">
      <div className="req-confirm">
        <div className="req-confirm-icon">🗑️</div>
        <h3>Delete this request?</h3>
        <p>
          "<strong>{disaster?.title}</strong>" will be permanently removed from the map and cannot be recovered.
        </p>
        <div className="req-confirm-actions">
          <button className="req-cancel-btn" onClick={onCancel} disabled={deleting}>Cancel</button>
          <button className="req-del-btn" onClick={onConfirm} disabled={deleting}>
            {deleting ? "Deleting…" : "Yes, Delete"}
          </button>
        </div>
      </div>
    </div>
  );
}

export default function RequestsPage() {
  const navigate = useNavigate();

  const [disasters,    setDisasters]    = useState([]);
  const [loading,      setLoading]      = useState(true);
  const [currentUID,   setCurrentUID]   = useState(null);
  const [activeTab,    setActiveTab]    = useState("All");
  const [search,       setSearch]       = useState("");
  const [searchInput,  setSearchInput]  = useState("");
  const [dateFilter,   setDateFilter]   = useState("");
  const [activeDateFilter, setActiveDateFilter] = useState("");
  const [viewDisaster, setViewDisaster] = useState(null);
  const [toDelete,     setToDelete]     = useState(null);
  const [deleting,     setDeleting]     = useState(false);

  useEffect(() => {
    supabase.auth.getSession().then(({ data: { session } }) => {
      if (!session) { navigate("/"); return; }
      setCurrentUID(session.user.id);
    });
  }, [navigate]);

  const fetchDisasters = useCallback(async () => {
    if (!currentUID) return;
    setLoading(true);
    try {
      const data = await disasterService.getByUser(currentUID);
      setDisasters(data);
    } catch (err) {
      console.error("Failed to fetch:", err.message);
    }
    setLoading(false);
  }, [currentUID]);

  useEffect(() => { fetchDisasters(); }, [fetchDisasters]);

  const handleDelete = async () => {
    if (!toDelete) return;
    setDeleting(true);
    await disasterService.remove(toDelete.id);
    setDeleting(false);
    if (error) { alert("Failed to delete: " + error.message); return; }
    setDisasters((prev) => prev.filter((d) => d.id !== toDelete.id));
    setToDelete(null);
    setViewDisaster(null);
  };

  const applyFilters = () => {
    setSearch(searchInput);
    setActiveDateFilter(dateFilter);
  };

  const resetFilters = () => {
    setSearch(""); setSearchInput("");
    setDateFilter(""); setActiveDateFilter("");
    setActiveTab("All");
  };

  const filtered = disasters.filter((d) => {
    const matchTab  = activeTab === "All" || d.status === activeTab;
    const matchSrch = !search || (
      d.title?.toLowerCase().includes(search.toLowerCase()) ||
      d.description?.toLowerCase().includes(search.toLowerCase())
    );
    const matchDate = !activeDateFilter || d.created_at?.startsWith(activeDateFilter);
    return matchTab && matchSrch && matchDate;
  });

  const counts = STATUS_TABS.reduce((acc, tab) => {
    acc[tab] = tab === "All" ? disasters.length : disasters.filter((d) => d.status === tab).length;
    return acc;
  }, {});

  const showingLabel = (() => {
    if (!disasters.length) return null;
    const dates = disasters.map((d) => new Date(d.created_at));
    const min = new Date(Math.min(...dates));
    const max = new Date(Math.max(...dates));
    const fmt = (d) => d.toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" });
    return `Showing requests from ${fmt(min)} to ${fmt(max)}`;
  })();

  return (
    <div className="dashboard">
      <Sidebar />

      <div className="main req-main">

        {/* Header */}
        <div className="req-page-header">
          <div>
            <h1 className="req-page-title">My Requests</h1>
            <p className="req-page-sub">View and manage the disaster points you've added to the map.</p>
          </div>
          <button className="req-header-add-btn" onClick={() => navigate("/map")}>
            + Add on Map
          </button>
        </div>

        {/* Filters bar */}
        <div className="req-filters-bar">
          <div className="req-filter-col">
            <label className="req-filter-label">FILTER BY DATE</label>
            <input
              type="date"
              className="req-date-input"
              value={dateFilter}
              onChange={(e) => setDateFilter(e.target.value)}
            />
          </div>

          <div className="req-filter-col req-search-col">
            <label className="req-filter-label">SEARCH REQUESTS</label>
            <div className="req-search-box">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none"
                stroke="#9ca3af" strokeWidth="2.5" strokeLinecap="round">
                <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
              </svg>
              <input
                type="text"
                placeholder="Search by title or description…"
                value={searchInput}
                onChange={(e) => setSearchInput(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && applyFilters()}
              />
            </div>
          </div>

          <div className="req-filter-btns">
            <button className="req-apply-btn" onClick={applyFilters}>Apply Filters</button>
            <button className="req-reset-btn" onClick={resetFilters}>Reset</button>
          </div>
        </div>

        {/* Status tabs */}
        <div className="req-tabs-bar">
          <div className="req-tabs">
            {STATUS_TABS.map((tab) => (
              <button
                key={tab}
                className={`req-tab ${activeTab === tab ? "req-tab-active" : ""}`}
                onClick={() => setActiveTab(tab)}
              >
                {tab === "All"        && <span className="tab-icon">◉</span>}
                {tab === "Active"     && <span className="tab-icon">✦</span>}
                {tab === "Monitoring" && <span className="tab-icon">◎</span>}
                {tab === "Resolved"   && <span className="tab-icon">✓</span>}
                {tab}
                <span className="req-tab-count">{counts[tab]}</span>
              </button>
            ))}
          </div>
          {showingLabel && <p className="req-showing">{showingLabel}</p>}
        </div>

        {/* Content */}
        <div className="req-content">
          {loading ? (
            <div className="req-loading">
              <div className="req-spinner" />
              <p>Loading your requests…</p>
            </div>
          ) : filtered.length === 0 ? (
            <EmptyState onAdd={() => navigate("/map")} />
          ) : (
            <div className="req-grid">
              {filtered.map((d) => (
                <RequestCard
                  key={d.id}
                  disaster={d}
                  onView={setViewDisaster}
                  onDelete={setToDelete}
                />
              ))}
            </div>
          )}
        </div>
      </div>

      {viewDisaster && (
        <DetailModal
          disaster={viewDisaster}
          onClose={() => setViewDisaster(null)}
          onDelete={(d) => { setViewDisaster(null); setToDelete(d); }}
        />
      )}

      {toDelete && (
        <ConfirmDialog
          disaster={toDelete}
          onConfirm={handleDelete}
          onCancel={() => setToDelete(null)}
          deleting={deleting}
        />
      )}
    </div>
  );
}