import { useEffect, useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { supabase } from "../../../supabaseClient";
import { useAdminAuth } from "../useAdminAuth";
import AdminSidebar from "../AdminSidebar";
import "../Admin.css";

const PAGE_SIZE = 10;

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
  }).format(amount ?? 0);
}

function getStatusStyle(status) {
  if (status === "Completed") return { bg: "#f0fdf4", color: "#166534" };
  if (status === "Failed")    return { bg: "#fef2f2", color: "#991b1b" };
  return                             { bg: "#fffbeb", color: "#92400e" }; // Pending
}

function Toast({ message, type }) {
  if (!message) return null;
  return (
    <div className={`toast toast-${type}`}>
      <i className={`ti ${type === "success" ? "ti-circle-check" : "ti-alert-circle"} toast-icon`} aria-hidden="true" />
      <span>{message}</span>
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
      >
        <i className="ti ti-arrow-left" aria-hidden="true" /> Prev
      </button>
      <span className="admin-page-info">Page {currentPage} of {totalPages}</span>
      <button
        className="admin-page-btn"
        onClick={() => onPageChange(currentPage + 1)}
        disabled={currentPage === totalPages}
      >
        Next <i className="ti ti-arrow-right" aria-hidden="true" />
      </button>
    </div>
  );
}

export default function AdminDonations() {
  const { loading, authed } = useAdminAuth();
  const navigate = useNavigate();

  const [donations,    setDonations]    = useState([]);
  const [search,       setSearch]       = useState("");
  const [statusFilter, setStatusFilter] = useState("All");
  const [fetching,     setFetching]     = useState(true);
  const [currentPage,  setCurrentPage]  = useState(1);
  const [toast,        setToast]        = useState({ message: "", type: "" });

  const showToast = (message, type = "success") => {
    setToast({ message, type });
    setTimeout(() => setToast({ message: "", type: "" }), 3000);
  };

  const fetchDonations = useCallback(async () => {
    setFetching(true);
    const { data, error } = await supabase
      .from("donations")
      .select(`
        *,
        users      ( username ),
        disasters  ( id, title, severity_level, status )
      `)
      .order("donated_at", { ascending: false });

    if (error) {
      showToast("Failed to load donations: " + error.message, "error");
    } else {
      setDonations(data ?? []);
    }
    setFetching(false);
  }, []);

  useEffect(() => { if (!loading) fetchDonations(); }, [loading, fetchDonations]);

  // ── Stats ──────────────────────────────────────────────────────────────────
  const totalCompleted = donations
    .filter(d => d.status === "Completed")
    .reduce((sum, d) => sum + parseFloat(d.amount ?? 0), 0);

  const pendingCount   = donations.filter(d => d.status === "Pending").length;
  const completedCount = donations.filter(d => d.status === "Completed").length;

  // ── Filter ─────────────────────────────────────────────────────────────────
  const STATUS_TABS = ["All", "Completed", "Pending", "Failed"];

  const filtered = donations.filter(d => {
    const matchStatus = statusFilter === "All" || d.status === statusFilter;
    const donor       = d.users?.username?.toLowerCase() ?? "";
    const disaster    = d.disasters?.title?.toLowerCase() ?? "";
    const matchSearch = !search ||
      donor.includes(search.toLowerCase()) ||
      disaster.includes(search.toLowerCase()) ||
      d.id?.toLowerCase().includes(search.toLowerCase());
    return matchStatus && matchSearch;
  });

  const totalPages = Math.ceil(filtered.length / PAGE_SIZE);
  const paginated  = filtered.slice((currentPage - 1) * PAGE_SIZE, currentPage * PAGE_SIZE);

  const handlePageChange = (page) => {
    setCurrentPage(page);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  // Reset to page 1 when filter/search changes
  useEffect(() => { setCurrentPage(1); }, [search, statusFilter]);

  if (!authed) return null;

  return (
    <div className="admin-layout">
      <Toast message={toast.message} type={toast.type} />
      <AdminSidebar />

      <div className="admin-main">
        <div className="admin-page-header">
          <h1 className="admin-page-title">Donations</h1>
          <p className="admin-page-sub">View all donation records across the platform</p>
        </div>

        {/* ── Summary cards ── */}
        <div className="admin-stats-grid">
          <div className="admin-stat-card">
            <div className="admin-stat-icon" style={{ background: "#f5f3ff", color: "#7c3aed" }}>
              <i className="ti ti-heart" aria-hidden="true" />
            </div>
            <div>
              <p className="admin-stat-value">{donations.length}</p>
              <p className="admin-stat-label">Total donations</p>
            </div>
          </div>
          <div className="admin-stat-card">
            <div className="admin-stat-icon" style={{ background: "#f0fdf4", color: "#16a34a" }}>
              <i className="ti ti-circle-check" aria-hidden="true" />
            </div>
            <div>
              <p className="admin-stat-value">{formatAmount(totalCompleted)}</p>
              <p className="admin-stat-label">Total completed</p>
            </div>
          </div>
          <div className="admin-stat-card">
            <div className="admin-stat-icon" style={{ background: "#f0fdf4", color: "#16a34a" }}>
              <i className="ti ti-trending-up" aria-hidden="true" />
            </div>
            <div>
              <p className="admin-stat-value">{completedCount}</p>
              <p className="admin-stat-label">Completed count</p>
            </div>
          </div>
          <div className="admin-stat-card">
            <div className="admin-stat-icon" style={{ background: "#fffbeb", color: "#ea580c" }}>
              <i className="ti ti-clock" aria-hidden="true" />
            </div>
            <div>
              <p className="admin-stat-value">{pendingCount}</p>
              <p className="admin-stat-label">Pending</p>
            </div>
          </div>
        </div>

        {/* ── Status tabs ── */}
        <div className="admin-tabs" style={{ marginBottom: 16 }}>
          {STATUS_TABS.map(tab => (
            <button
              key={tab}
              className={`admin-tab ${statusFilter === tab ? "admin-tab-active" : ""}`}
              onClick={() => setStatusFilter(tab)}
            >
              {tab}
              <span className="admin-tab-count">
                {tab === "All"
                  ? donations.length
                  : donations.filter(d => d.status === tab).length}
              </span>
            </button>
          ))}
        </div>

        <div className="admin-card">
          {/* ── Filter bar ── */}
          <div className="admin-filter-bar">
            <div className="admin-search-box">
              <i className="ti ti-search" style={{ fontSize: 14, color: "#9ca3af", flexShrink: 0 }} aria-hidden="true" />
              <input
                type="text"
                placeholder="Search by donor, disaster, or ID…"
                value={search}
                onChange={e => setSearch(e.target.value)}
              />
            </div>
            <span className="admin-record-count">{filtered.length} records</span>
          </div>

          {fetching ? (
            <div className="admin-loading"><div className="admin-spinner" /><p>Loading…</p></div>
          ) : filtered.length === 0 ? (
            <div className="admin-empty"><p>No donations found.</p></div>
          ) : (
            <>
              <p className="admin-showing">
                Showing {(currentPage - 1) * PAGE_SIZE + 1}–{Math.min(currentPage * PAGE_SIZE, filtered.length)} of {filtered.length}
              </p>

              <table className="admin-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Donor</th>
                    <th>Disaster</th>
                    <th>Amount</th>
                    <th>Status</th>
                    <th>Date</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {paginated.map(d => {
                    const style   = getStatusStyle(d.status);
                    const donor   = d.users?.username ?? "Unknown";
                    const disaster = d.disasters?.title ?? "—";
                    return (
                      <tr key={d.id}>
                        {/* ID */}
                        <td className="admin-td-mono">{d.id?.slice(0, 8)}…</td>

                        {/* Donor */}
                        <td>
                          <div className="admin-user-cell">
                            <div className="admin-user-avatar">
                              {donor.slice(0, 2).toUpperCase()}
                            </div>
                            <span className="admin-td-bold">{donor}</span>
                          </div>
                        </td>

                        {/* Disaster */}
                        <td>
                          {d.disasters ? (
                            <button
                              className="adon-disaster-link"
                              onClick={() => navigate("/admin/map", {
                                state: { focusDisasterId: d.disasters.id }
                              })}
                              title="View on map"
                            >
                              <i className="ti ti-map-pin" aria-hidden="true" />
                              {disaster}
                            </button>
                          ) : (
                            <span className="admin-td-muted">—</span>
                          )}
                        </td>

                        {/* Amount */}
                        <td className="admin-td-bold">{formatAmount(d.amount)}</td>

                        {/* Status */}
                        <td>
                          <span
                            className="admin-badge-status"
                            style={{ background: style.bg, color: style.color }}
                          >
                            {d.status}
                          </span>
                        </td>

                        {/* Date */}
                        <td>{formatDate(d.donated_at)}</td>

                        {/* Action */}
                        <td>
                          {d.disasters ? (
                            <button
                              className="admin-btn admin-btn-primary"
                              onClick={() => navigate("/admin/map", {
                                state: { focusDisasterId: d.disasters.id }
                              })}
                            >
                              <i className="ti ti-map-pin" aria-hidden="true" /> View on Map
                            </button>
                          ) : (
                            <span className="admin-no-action">—</span>
                          )}
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
    </div>
  );
}