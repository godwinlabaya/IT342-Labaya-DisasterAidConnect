import { useEffect, useState, useCallback } from "react";
import { supabase } from "../../../supabaseClient";
import { useAdminAuth } from "../useAdminAuth";
import AdminSidebar from "../AdminSidebar";
import "../Admin.css";

const PAGE_SIZE = 7;

function Toast({ message, type }) {
  if (!message) return null;
  return (
    <div className={`toast toast-${type}`}>
      <span className="toast-icon">{type === "success" ? "✅" : "❌"}</span>
      <span>{message}</span>
    </div>
  );
}

function ConfirmDialog({ title, onConfirm, onCancel }) {
  return (
    <div className="admin-backdrop">
      <div className="admin-confirm">
        <div className="admin-confirm-icon">🗑️</div>
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
      <button
        className="admin-page-btn"
        onClick={() => onPageChange(currentPage - 1)}
        disabled={currentPage === 1}
      >← Prev</button>
      <span className="admin-page-info">Page {currentPage} of {totalPages}</span>
      <button
        className="admin-page-btn"
        onClick={() => onPageChange(currentPage + 1)}
        disabled={currentPage === totalPages}
      >Next →</button>
    </div>
  );
}

function getSeverityStyle(level) {
  if (level === "Critical") return { bg: "#fee2e2", color: "#991b1b" };
  if (level === "High")     return { bg: "#ffedd5", color: "#9a3412" };
  if (level === "Medium")   return { bg: "#fef9c3", color: "#854d0e" };
  return                           { bg: "#dcfce7", color: "#166534" };
}

function getStatusStyle(status) {
  if (status === "Active")     return { bg: "#dcfce7", color: "#166534" };
  if (status === "Monitoring") return { bg: "#fef3c7", color: "#92400e" };
  if (status === "Resolved")   return { bg: "#e0e7ff", color: "#3730a3" };
  return                              { bg: "#f3f4f6", color: "#6b7280" };
}

function formatDate(iso) {
  if (!iso) return "—";
  return new Date(iso).toLocaleDateString("en-US", {
    month: "short", day: "numeric", year: "numeric",
  });
}

export default function AdminDisasters() {
  const { loading } = useAdminAuth();

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
      // Fetch usernames for all creators
      const uids = [...new Set(data.map((d) => d.created_by).filter(Boolean))];
      if (uids.length > 0) {
        const { data: userData } = await supabase
          .from("users")
          .select("id, username")
          .in("id", uids);
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

  // ── Filter ────────────────────────────────────────────────────────────────
  const filtered = disasters.filter((d) => {
    const matchSearch = !search ||
      d.title?.toLowerCase().includes(search.toLowerCase()) ||
      d.description?.toLowerCase().includes(search.toLowerCase());
    const matchDate = !dateFilter || d.created_at?.startsWith(dateFilter);
    return matchSearch && matchDate;
  });

  // ── Pagination ────────────────────────────────────────────────────────────
  const totalPages = Math.ceil(filtered.length / PAGE_SIZE);
  const paginated  = filtered.slice((currentPage - 1) * PAGE_SIZE, currentPage * PAGE_SIZE);

  const handlePageChange = (page) => {
    setCurrentPage(page);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  useEffect(() => { setCurrentPage(1); }, [search, dateFilter]);

  if (loading) return (
    <div className="admin-layout">
      <div className="admin-loading"><div className="admin-spinner" /></div>
    </div>
  );

  return (
    <div className="admin-layout">
      <Toast message={toast.message} type={toast.type} />
      <AdminSidebar />

      <div className="admin-main">
        <div className="admin-page-header">
          <h1 className="admin-page-title">🗺️ Disasters</h1>
          <p className="admin-page-sub">View and delete all reported disasters</p>
        </div>

        <div className="admin-card">
          {/* ── Filter bar ── */}
          <div className="admin-filter-bar">
            <div className="admin-search-box">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#94a3b8" strokeWidth="2.5">
                <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
              </svg>
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
              style={{
                border: "1px solid #e2e8f0", borderRadius: 10,
                padding: "8px 12px", fontSize: 13,
                color: "#334155", background: "#f8fafc", outline: "none",
              }}
            />

            {dateFilter && (
              <button className="admin-btn admin-btn-primary" onClick={() => setDateFilter("")}>
                Clear
              </button>
            )}

            <span style={{ fontSize: 13, color: "#94a3b8", marginLeft: "auto" }}>
              {filtered.length} records
            </span>
          </div>

          {fetching ? (
            <div className="admin-loading"><div className="admin-spinner" /><p>Loading…</p></div>
          ) : filtered.length === 0 ? (
            <div className="admin-empty"><p>No disasters found.</p></div>
          ) : (
            <>
              <p style={{ fontSize: 12, color: "#94a3b8", margin: "0 0 12px" }}>
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
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {paginated.map((d) => {
                    const sev     = getSeverityStyle(d.severity_level);
                    const status  = getStatusStyle(d.status);
                    const creator = usersMap[d.created_by] ?? "Unknown";
                    return (
                      <tr key={d.id}>
                        <td style={{ fontWeight: 600, color: "#1e1b4b" }}>{d.title}</td>
                        <td>
                          <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                            <div style={{
                              width: 28, height: 28, borderRadius: "50%",
                              background: "#0a2942", color: "white",
                              display: "flex", alignItems: "center",
                              justifyContent: "center", fontSize: 11, fontWeight: 700,
                              flexShrink: 0,
                            }}>
                              {creator.slice(0, 2).toUpperCase()}
                            </div>
                            <span style={{ fontSize: 13, color: "#334155" }}>{creator}</span>
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
                        <td style={{ fontFamily: "monospace", fontSize: 12 }}>
                          {d.latitude?.toFixed(4)}, {d.longitude?.toFixed(4)}
                        </td>
                        <td>{formatDate(d.created_at)}</td>
                        <td>
                          <button
                            className="admin-btn admin-btn-danger"
                            onClick={() => setToDelete(d)}
                            disabled={deleting}
                          >
                            🗑 Delete
                          </button>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>

              <Pagination
                currentPage={currentPage}
                totalPages={totalPages}
                onPageChange={handlePageChange}
              />
            </>
          )}
        </div>
      </div>

      {toDelete && (
        <ConfirmDialog
          title={toDelete.title}
          onConfirm={handleDelete}
          onCancel={() => setToDelete(null)}
        />
      )}
    </div>
  );
}