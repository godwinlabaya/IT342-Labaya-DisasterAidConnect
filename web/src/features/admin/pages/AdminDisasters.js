import { useEffect, useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { supabase } from "../../../supabaseClient";
import { useAdminAuth } from "../useAdminAuth";
import AdminSidebar from "../AdminSidebar";
import "../Admin.css";

const PAGE_SIZE = 7;

function Toast({ message, type }) {
  if (!message) return null;
  return (
    <div className={`toast toast-${type}`}>
      <i className={`ti ${type === "success" ? "ti-circle-check" : "ti-alert-circle"} toast-icon`} aria-hidden="true" />
      <span>{message}</span>
    </div>
  );
}

function ConfirmDialog({ title, onConfirm, onCancel }) {
  return (
    <div className="admin-backdrop">
      <div className="admin-confirm">
        <div className="admin-confirm-icon">
          <i className="ti ti-trash" aria-hidden="true" />
        </div>
        <h3>Delete this disaster?</h3>
        <p>"{title}" will be permanently removed. This cannot be undone.</p>
        <div className="admin-confirm-actions">
          <button className="admin-btn admin-btn-primary" onClick={onCancel}>Cancel</button>
          <button className="admin-btn admin-btn-danger"  onClick={onConfirm}>Yes, Delete</button>
        </div>
      </div>
    </div>
  );
}

function Pagination({ currentPage, totalPages, onPageChange }) {
  if (totalPages <= 1) return null;
  return (
    <div className="admin-pagination">
      <button className="admin-page-btn" onClick={() => onPageChange(currentPage - 1)} disabled={currentPage === 1}>
        <i className="ti ti-arrow-left" aria-hidden="true" /> Prev
      </button>
      <span className="admin-page-info">Page {currentPage} of {totalPages}</span>
      <button className="admin-page-btn" onClick={() => onPageChange(currentPage + 1)} disabled={currentPage === totalPages}>
        Next <i className="ti ti-arrow-right" aria-hidden="true" />
      </button>
    </div>
  );
}

function getSeverityStyle(level) {
  if (level === "Critical") return { bg: "#fef2f2", color: "#991b1b" };
  if (level === "High")     return { bg: "#fff7ed", color: "#9a3412" };
  if (level === "Medium")   return { bg: "#fefce8", color: "#854d0e" };
  return                           { bg: "#f0fdf4", color: "#166534" };
}

function getStatusStyle(status) {
  if (status === "Active")     return { bg: "#f0fdf4", color: "#166534" };
  if (status === "Monitoring") return { bg: "#fffbeb", color: "#92400e" };
  if (status === "Resolved")   return { bg: "#eff6ff", color: "#1d4ed8" };
  return                              { bg: "#f3f4f6", color: "#6b7280" };
}

function formatDate(iso) {
  if (!iso) return "—";
  return new Date(iso).toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" });
}

export default function AdminDisasters() {
  const { loading, authed } = useAdminAuth();
  const navigate = useNavigate();

  const [disasters,   setDisasters]   = useState([]);
  const [usersMap,    setUsersMap]    = useState({});
  const [search,      setSearch]      = useState("");
  const [dateFilter,  setDateFilter]  = useState("");
  const [fetching,    setFetching]    = useState(true);
  const [toDelete,    setToDelete]    = useState(null);
  const [deleting,    setDeleting]    = useState(false);
  const [toast,       setToast]       = useState({ message: "", type: "" });
  const [currentPage, setCurrentPage] = useState(1);

  const showToast = (message, type = "success") => {
    setToast({ message, type });
    setTimeout(() => setToast({ message: "", type: "" }), 3000);
  };

  const fetchDisasters = useCallback(async () => {
    setFetching(true);
    const { data, error } = await supabase
      .from("disasters")
      .select("*")
      .order("created_at", { ascending: false });

    if (!error && data) {
      setDisasters(data);
      const uids = [...new Set(data.map((d) => d.created_by).filter(Boolean))];
      if (uids.length > 0) {
        const { data: userData } = await supabase.from("users").select("id, username").in("id", uids);
        if (userData) {
          const map = {};
          userData.forEach((u) => { map[u.id] = u.username; });
          setUsersMap(map);
        }
      }
    }
    setFetching(false);
  }, []);

  useEffect(() => { if (!loading) fetchDisasters(); }, [loading, fetchDisasters]);

  const handleDelete = async () => {
    if (!toDelete) return;
    setDeleting(true);
    const { error } = await supabase.from("disasters").delete().eq("id", toDelete.id);
    if (error) {
      showToast("Failed to delete: " + error.message, "error");
    } else {
      setDisasters((prev) => prev.filter((d) => d.id !== toDelete.id));
      showToast("Disaster deleted successfully.", "success");
    }
    setToDelete(null);
    setDeleting(false);
  };

  // ── Navigate to admin map and focus this disaster ──────────────────────────
  const handleViewOnMap = (disaster) => {
    navigate("/admin/map", { state: { focusDisasterId: disaster.id } });
  };

  const filtered = disasters.filter((d) => {
    const matchSearch = !search || d.title?.toLowerCase().includes(search.toLowerCase()) || d.description?.toLowerCase().includes(search.toLowerCase());
    const matchDate   = !dateFilter || d.created_at?.startsWith(dateFilter);
    return matchSearch && matchDate;
  });

  const totalPages = Math.ceil(filtered.length / PAGE_SIZE);
  const paginated  = filtered.slice((currentPage - 1) * PAGE_SIZE, currentPage * PAGE_SIZE);

  const handlePageChange = (page) => { setCurrentPage(page); window.scrollTo({ top: 0, behavior: "smooth" }); };

  useEffect(() => { setCurrentPage(1); }, [search, dateFilter]);

  if (!authed) return null;

  return (
    <div className="admin-layout">
      <Toast message={toast.message} type={toast.type} />
      <AdminSidebar />

      <div className="admin-main">
        <div className="admin-page-header">
          <h1 className="admin-page-title">Disasters</h1>
          <p className="admin-page-sub">View and delete all reported disasters</p>
        </div>

        <div className="admin-card">
          <div className="admin-filter-bar">
            <div className="admin-search-box">
              <i className="ti ti-search" style={{ fontSize: 14, color: "#9ca3af", flexShrink: 0 }} aria-hidden="true" />
              <input
                type="text"
                placeholder="Search by title or description…"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </div>

            <input
              type="date"
              value={dateFilter}
              onChange={(e) => setDateFilter(e.target.value)}
              className="admin-date-input"
            />

            {dateFilter && (
              <button className="admin-btn admin-btn-primary" onClick={() => setDateFilter("")}>Clear</button>
            )}

            <span className="admin-record-count">{filtered.length} records</span>
          </div>

          {fetching ? (
            <div className="admin-loading"><div className="admin-spinner" /><p>Loading…</p></div>
          ) : filtered.length === 0 ? (
            <div className="admin-empty"><p>No disasters found.</p></div>
          ) : (
            <>
              <p className="admin-showing">
                Showing {(currentPage - 1) * PAGE_SIZE + 1}–{Math.min(currentPage * PAGE_SIZE, filtered.length)} of {filtered.length}
              </p>

              <table className="admin-table">
                <thead>
                  <tr>
                    <th>Title</th>
                    <th>Reported by</th>
                    <th>Severity</th>
                    <th>Status</th>
                    <th>Coordinates</th>
                    <th>Date</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {paginated.map((d) => {
                    const sev     = getSeverityStyle(d.severity_level);
                    const status  = getStatusStyle(d.status);
                    const creator = usersMap[d.created_by] ?? "Unknown";
                    return (
                      <tr key={d.id}>
                        <td className="admin-td-bold">{d.title}</td>
                        <td>
                          <div className="admin-user-cell">
                            <div className="admin-user-avatar">
                              {creator.slice(0, 2).toUpperCase()}
                            </div>
                            <span>{creator}</span>
                          </div>
                        </td>
                        <td>
                          <span className="admin-badge-status" style={{ background: sev.bg, color: sev.color }}>
                            {d.severity_level}
                          </span>
                        </td>
                        <td>
                          <span className="admin-badge-status" style={{ background: status.bg, color: status.color }}>
                            {d.status}
                          </span>
                        </td>
                        <td className="admin-td-mono">{d.latitude?.toFixed(4)}, {d.longitude?.toFixed(4)}</td>
                        <td>{formatDate(d.created_at)}</td>
                        <td>
                          <div className="admin-action-btns">
                            {/* ── View on Map ── */}
                            <button
                              className="admin-btn admin-btn-primary"
                              onClick={() => handleViewOnMap(d)}
                              title="View this point on the admin map"
                            >
                              <i className="ti ti-map-pin" aria-hidden="true" /> View on Map
                            </button>
                            {/* ── Delete ── */}
                            <button
                              className="admin-btn admin-btn-danger"
                              onClick={() => setToDelete(d)}
                              disabled={deleting}
                              title="Delete this disaster"
                            >
                              <i className="ti ti-trash" aria-hidden="true" /> Delete
                            </button>
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>

              <Pagination currentPage={currentPage} totalPages={totalPages} onPageChange={handlePageChange} />
            </>
          )}
        </div>
      </div>

      {toDelete && (
        <ConfirmDialog title={toDelete.title} onConfirm={handleDelete} onCancel={() => setToDelete(null)} />
      )}
    </div>
  );
}