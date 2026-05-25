import { useEffect, useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import Sidebar from "../../shared/components/Sidebar";
import "./Requests.css";
import { supabase } from "../../supabaseClient";
import { getSeverityColor } from "../disaster/iconFactory";
import disasterService from "../disaster/disasterService";

const STATUS_TABS = ["All", "Active", "Monitoring", "Resolved"];
const PAGE_SIZE   = 6;

function getStatusStyle(status) {
  if (status === "Active")     return { bg: "#f0fdf4", text: "#166534", icon: "ti-circle-check" };
  if (status === "Monitoring") return { bg: "#fffbeb", text: "#92400e", icon: "ti-eye" };
  if (status === "Resolved")   return { bg: "#eff6ff", text: "#1d4ed8", icon: "ti-check" };
  return                              { bg: "#f3f4f6", text: "#6b7280", icon: "ti-clock" };
}

function formatDate(iso) {
  if (!iso) return "—";
  return new Date(iso).toLocaleString("en-US", {
    month: "short", day: "numeric", year: "numeric",
    hour: "numeric", minute: "2-digit", hour12: true,
  });
}

// ── Empty state ───────────────────────────────────────────────────────────────
function EmptyState({ onAdd }) {
  return (
    <div className="req-empty">
      <div className="req-empty-icon">
        <i className="ti ti-clipboard-list" aria-hidden="true" />
      </div>
      <h3 className="req-empty-title">No requests found</h3>
      <p className="req-empty-sub">We couldn't find any requests matching your filters.</p>
      <button className="req-go-map-btn" onClick={onAdd}>Add a Disaster Point on Map</button>
    </div>
  );
}

// ── Request card ──────────────────────────────────────────────────────────────
function RequestCard({ disaster, onView, onDelete, onResolve, resolving }) {
  const sev    = getSeverityColor(disaster.severity_level);
  const status = getStatusStyle(disaster.status);
  const isResolved = disaster.status === "Resolved";

  return (
    <div className="req-card" onClick={() => onView(disaster)}>
      <div className="req-card-top">
        <h3 className="req-card-title">{disaster.title}</h3>
        <div className="req-card-badges">
          <span className="req-badge"
            style={{ background: sev.bg, color: sev.text, border: `1px solid ${sev.border}` }}>
            {disaster.severity_level}
          </span>
          <span className="req-status-chip" style={{ background: status.bg, color: status.text }}>
            <i className={`ti ${status.icon}`} aria-hidden="true" />
            {disaster.status}
          </span>
        </div>
      </div>

      <p className="req-card-desc">{disaster.description}</p>

      <div className="req-card-meta">
        <span className="req-meta-item">
          <i className="ti ti-clock" aria-hidden="true" />
          {formatDate(disaster.created_at)}
        </span>
        <span className="req-meta-item">
          <i className="ti ti-map-pin" aria-hidden="true" />
          {disaster.latitude?.toFixed(4)}, {disaster.longitude?.toFixed(4)}
        </span>
      </div>

      <div className="req-card-footer" onClick={(e) => e.stopPropagation()}>
        <button className="req-view-btn" onClick={() => onView(disaster)}>View Details</button>
        {!isResolved && (
          <button
            className="req-resolve-btn"
            onClick={() => onResolve(disaster)}
            disabled={resolving === disaster.id}
          >
            <i className="ti ti-circle-check" aria-hidden="true" />
            {resolving === disaster.id ? "Resolving…" : "Resolve"}
          </button>
        )}
        <button className="req-delete-btn" onClick={() => onDelete(disaster)}>
          <i className="ti ti-trash" aria-hidden="true" />
          Delete
        </button>
      </div>
    </div>
  );
}

// ── Detail modal ──────────────────────────────────────────────────────────────
function DetailModal({ disaster, onClose, onDelete, onViewOnMap, onResolve, resolving }) {
  if (!disaster) return null;
  const sev    = getSeverityColor(disaster.severity_level);
  const status = getStatusStyle(disaster.status);
  const isResolved = disaster.status === "Resolved";

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
            <i className="ti ti-alert-triangle" aria-hidden="true" />
            {disaster.severity_level} Severity
          </span>
          <span className="req-status-chip" style={{ background: status.bg, color: status.text }}>
            <i className={`ti ${status.icon}`} aria-hidden="true" />
            {disaster.status}
          </span>
        </div>

        <div className="req-modal-section">
          <p className="req-modal-label">Description</p>
          <p className="req-modal-value">{disaster.description}</p>
        </div>

        <div className="req-modal-grid">
          <div className="req-modal-section">
            <p className="req-modal-label">
              <i className="ti ti-map-pin" aria-hidden="true" /> Coordinates
            </p>
            <p className="req-modal-value mono">
              {disaster.latitude?.toFixed(6)}, {disaster.longitude?.toFixed(6)}
            </p>
          </div>
          <div className="req-modal-section">
            <p className="req-modal-label">
              <i className="ti ti-clock" aria-hidden="true" /> Date Added
            </p>
            <p className="req-modal-value">{formatDate(disaster.created_at)}</p>
          </div>
        </div>

        <div className="req-modal-footer">
          <button className="req-map-btn" onClick={() => onViewOnMap(disaster)}>
            <i className="ti ti-map-pin" aria-hidden="true" />
            View on Map
          </button>
          {!isResolved && (
            <button
              className="req-resolve-modal-btn"
              onClick={() => onResolve(disaster)}
              disabled={resolving === disaster.id}
            >
              <i className="ti ti-circle-check" aria-hidden="true" />
              {resolving === disaster.id ? "Resolving…" : "Mark Resolved"}
            </button>
          )}
          <button className="req-modal-delete" onClick={() => onDelete(disaster)}>
            <i className="ti ti-trash" aria-hidden="true" />
            Delete Point
          </button>
        </div>
      </div>
    </div>
  );
}

// ── Confirm dialog ────────────────────────────────────────────────────────────
function ConfirmDialog({ disaster, onConfirm, onCancel, deleting }) {
  return (
    <div className="req-backdrop">
      <div className="req-confirm">
        <div className="req-confirm-icon">
          <i className="ti ti-trash" aria-hidden="true" />
        </div>
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

// ── Resolve confirm dialog ────────────────────────────────────────────────────
function ResolveConfirmDialog({ disaster, onConfirm, onCancel, resolving }) {
  return (
    <div className="req-backdrop">
      <div className="req-confirm">
        <div className="req-confirm-icon" style={{ color: "#15803d" }}>
          <i className="ti ti-circle-check" aria-hidden="true" />
        </div>
        <h3>Mark as Resolved?</h3>
        <p>
          "<strong>{disaster?.title}</strong>" will be marked as resolved and removed from the map. This cannot be undone.
        </p>
        <div className="req-confirm-actions">
          <button className="req-cancel-btn" onClick={onCancel} disabled={!!resolving}>Cancel</button>
          <button className="req-resolve-confirm-btn" onClick={onConfirm} disabled={!!resolving}>
            {resolving ? "Resolving…" : "Yes, Resolve"}
          </button>
        </div>
      </div>
    </div>
  );
}

// ── Pagination ────────────────────────────────────────────────────────────────
function Pagination({ currentPage, totalPages, onPageChange }) {
  if (totalPages <= 1) return null;
  const pages = [];
  for (let i = 1; i <= totalPages; i++) {
    if (i === 1 || i === totalPages || (i >= currentPage - 1 && i <= currentPage + 1)) {
      pages.push(i);
    } else if (i === currentPage - 2 || i === currentPage + 2) {
      pages.push("...");
    }
  }
  const dedupedPages = pages.filter((p, idx) => !(p === "..." && pages[idx - 1] === "..."));
  return (
    <div className="req-pagination">
      <button className="req-page-btn" onClick={() => onPageChange(currentPage - 1)} disabled={currentPage === 1}>
        ← Prev
      </button>
      <div className="req-page-numbers">
        {dedupedPages.map((p, idx) =>
          p === "..." ? (
            <span key={`ellipsis-${idx}`} className="req-page-ellipsis">…</span>
          ) : (
            <button key={p}
              className={`req-page-num ${currentPage === p ? "req-page-num-active" : ""}`}
              onClick={() => onPageChange(p)}>
              {p}
            </button>
          )
        )}
      </div>
      <button className="req-page-btn" onClick={() => onPageChange(currentPage + 1)} disabled={currentPage === totalPages}>
        Next →
      </button>
    </div>
  );
}

// ── Main page ─────────────────────────────────────────────────────────────────
export default function RequestsPage() {
  const navigate = useNavigate();

  const [disasters,        setDisasters]        = useState([]);
  const [loading,          setLoading]          = useState(true);
  const [currentUID,       setCurrentUID]       = useState(null);
  const [activeTab,        setActiveTab]        = useState("All");
  const [search,           setSearch]           = useState("");
  const [searchInput,      setSearchInput]      = useState("");
  const [dateFilter,       setDateFilter]       = useState("");
  const [activeDateFilter, setActiveDateFilter] = useState("");
  const [viewDisaster,     setViewDisaster]     = useState(null);
  const [toDelete,         setToDelete]         = useState(null);
  const [deleting,         setDeleting]         = useState(false);
  const [toResolve,        setToResolve]        = useState(null);  // pending resolve confirmation
  const [resolving,        setResolving]        = useState(false);
  const [currentPage,      setCurrentPage]      = useState(1);

  // Local resolved list — keeps resolved cards visible in the Resolved tab
  // after their DB row is deleted
  const [resolvedCache,    setResolvedCache]    = useState([]);

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
    try {
      await disasterService.remove(toDelete.id);
      setDisasters((prev) => prev.filter((d) => d.id !== toDelete.id));
      setResolvedCache((prev) => prev.filter((d) => d.id !== toDelete.id));
      setToDelete(null);
      setViewDisaster(null);
    } catch (err) {
      alert("Failed to delete: " + err.message);
    } finally {
      setDeleting(false);
    }
  };

  // ── Resolve: deletes the point from DB/map, keeps a local resolved copy ──
  const handleResolve = async () => {
    if (!toResolve) return;
    setResolving(true);
    try {
      // Delete from DB (removes from map)
      await disasterService.remove(toResolve.id);
      // Remove from live disasters list
      setDisasters((prev) => prev.filter((d) => d.id !== toResolve.id));
      // Add a resolved copy to local cache so it shows in the Resolved tab
      setResolvedCache((prev) => [
        { ...toResolve, status: "Resolved" },
        ...prev.filter((d) => d.id !== toResolve.id),
      ]);
      setToResolve(null);
      setViewDisaster(null);
    } catch (err) {
      alert("Failed to resolve: " + err.message);
    } finally {
      setResolving(false);
    }
  };

  const applyFilters = () => {
    setSearch(searchInput);
    setActiveDateFilter(dateFilter);
    setCurrentPage(1);
  };

  const resetFilters = () => {
    setSearch(""); setSearchInput("");
    setDateFilter(""); setActiveDateFilter("");
    setActiveTab("All");
    setCurrentPage(1);
  };

  const handleTabChange = (tab) => { setActiveTab(tab); setCurrentPage(1); };

  const handleViewOnMap = (disaster) => {
    setViewDisaster(null);
    navigate("/map", { state: { focusDisasterId: disaster.id } });
  };

  // Merge live disasters + resolved cache for display
  const allDisasters = [
    ...disasters,
    // Only include resolved cache entries not already in live list
    ...resolvedCache.filter((r) => !disasters.find((d) => d.id === r.id)),
  ];

  const filtered = allDisasters.filter((d) => {
    const matchTab  = activeTab === "All" || d.status === activeTab;
    const matchSrch = !search || (
      d.title?.toLowerCase().includes(search.toLowerCase()) ||
      d.description?.toLowerCase().includes(search.toLowerCase())
    );
    const matchDate = !activeDateFilter || d.created_at?.startsWith(activeDateFilter);
    return matchTab && matchSrch && matchDate;
  });

  const totalPages = Math.ceil(filtered.length / PAGE_SIZE);
  const startIndex = (currentPage - 1) * PAGE_SIZE;
  const paginated  = filtered.slice(startIndex, startIndex + PAGE_SIZE);

  const handlePageChange = (newPage) => {
    setCurrentPage(newPage);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const counts = STATUS_TABS.reduce((acc, tab) => {
    acc[tab] = tab === "All"
      ? allDisasters.length
      : allDisasters.filter((d) => d.status === tab).length;
    return acc;
  }, {});

  const showingLabel = (() => {
    if (!allDisasters.length) return null;
    const dates = allDisasters.map((d) => new Date(d.created_at));
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
            <input type="date" className="req-date-input" value={dateFilter}
              onChange={(e) => setDateFilter(e.target.value)} />
          </div>
          <div className="req-filter-col req-search-col">
            <label className="req-filter-label">SEARCH REQUESTS</label>
            <div className="req-search-box">
              <i className="ti ti-search" style={{ fontSize: 14, color: "#9ca3af" }} aria-hidden="true" />
              <input type="text" placeholder="Search by title or description…" value={searchInput}
                onChange={(e) => setSearchInput(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && applyFilters()} />
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
              <button key={tab}
                className={`req-tab ${activeTab === tab ? "req-tab-active" : ""}`}
                onClick={() => handleTabChange(tab)}>
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
            <>
              <div className="req-results-info">
                Showing {startIndex + 1}–{Math.min(startIndex + PAGE_SIZE, filtered.length)} of {filtered.length} request{filtered.length !== 1 ? "s" : ""}
              </div>
              <div className="req-grid">
                {paginated.map((d) => (
                  <RequestCard
                    key={d.id}
                    disaster={d}
                    onView={setViewDisaster}
                    onDelete={setToDelete}
                    onResolve={setToResolve}
                    resolving={resolving ? toResolve?.id : null}
                  />
                ))}
              </div>
              <Pagination currentPage={currentPage} totalPages={totalPages} onPageChange={handlePageChange} />
            </>
          )}
        </div>
      </div>

      {viewDisaster && (
        <DetailModal
          disaster={viewDisaster}
          onClose={() => setViewDisaster(null)}
          onDelete={(d) => { setViewDisaster(null); setToDelete(d); }}
          onViewOnMap={handleViewOnMap}
          onResolve={(d) => { setViewDisaster(null); setToResolve(d); }}
          resolving={resolving ? toResolve?.id : null}
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

      {toResolve && (
        <ResolveConfirmDialog
          disaster={toResolve}
          onConfirm={handleResolve}
          onCancel={() => setToResolve(null)}
          resolving={resolving}
        />
      )}
    </div>
  );
}