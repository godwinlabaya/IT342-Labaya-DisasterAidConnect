import { useEffect, useState, useCallback } from "react";
import { supabase } from "../../../supabaseClient";
import { useAdminAuth } from "../useAdminAuth";
import AdminSidebar from "../AdminSidebar";
import "../Admin.css";

function formatDate(iso) {
  if (!iso) return "—";
  return new Date(iso).toLocaleDateString("en-US", {
    month: "short", day: "numeric", year: "numeric",
  });
}

function formatAmount(amount) {
  return new Intl.NumberFormat("en-PH", {
    style: "currency", currency: "PHP", minimumFractionDigits: 0,
  }).format(amount);
}

function getStatusStyle(status) {
  if (status === "Completed") return { bg: "#dcfce7", color: "#166534" };
  if (status === "Failed")    return { bg: "#fee2e2", color: "#991b1b" };
  if (status === "Refunded")  return { bg: "#e0e7ff", color: "#3730a3" };
  return                             { bg: "#fef9c3", color: "#854d0e" };
}

export default function AdminDonations() {
  const { loading } = useAdminAuth();

  const [donations, setDonations] = useState([]);
  const [search,    setSearch]    = useState("");
  const [fetching,  setFetching]  = useState(true);

  const fetchDonations = useCallback(async () => {
    setFetching(true);
    const { data, error } = await supabase
      .from("donations")
      .select("*")
      .order("donated_at", { ascending: false });
    if (!error) setDonations(data ?? []);
    setFetching(false);
  }, []);

  useEffect(() => { if (!loading) fetchDonations(); }, [loading, fetchDonations]);

  const totalCompleted = donations
    .filter((d) => d.status === "Completed")
    .reduce((sum, d) => sum + parseFloat(d.amount ?? 0), 0);

  const filtered = donations.filter((d) =>
    d.status?.toLowerCase().includes(search.toLowerCase()) ||
    d.id?.toLowerCase().includes(search.toLowerCase())
  );

  if (loading) return (
    <div className="admin-layout">
      <div className="admin-loading"><div className="admin-spinner" /></div>
    </div>
  );

  return (
    <div className="admin-layout">
      <AdminSidebar />

      <div className="admin-main">
        <div className="admin-page-header">
          <h1 className="admin-page-title">💸 Donations</h1>
          <p className="admin-page-sub">View all donation records across the platform</p>
        </div>

        {/* Summary cards */}
        <div className="admin-stats-grid" style={{ marginBottom: 24 }}>
          <div className="admin-stat-card">
            <div className="admin-stat-icon" style={{ background: "#8b5cf620", color: "#8b5cf6" }}>💸</div>
            <div>
              <p className="admin-stat-value">{donations.length}</p>
              <p className="admin-stat-label">Total donations</p>
            </div>
          </div>
          <div className="admin-stat-card">
            <div className="admin-stat-icon" style={{ background: "#16a34a20", color: "#16a34a" }}>✅</div>
            <div>
              <p className="admin-stat-value">{formatAmount(totalCompleted)}</p>
              <p className="admin-stat-label">Total completed</p>
            </div>
          </div>
          <div className="admin-stat-card">
            <div className="admin-stat-icon" style={{ background: "#f9731620", color: "#f97316" }}>⏳</div>
            <div>
              <p className="admin-stat-value">{donations.filter((d) => d.status === "Pending").length}</p>
              <p className="admin-stat-label">Pending</p>
            </div>
          </div>
        </div>

        <div className="admin-card">
          <div className="admin-filter-bar">
            <div className="admin-search-box">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#94a3b8" strokeWidth="2.5">
                <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
              </svg>
              <input
                type="text"
                placeholder="Search by status or ID…"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </div>
            <span style={{ fontSize: 13, color: "#94a3b8" }}>{filtered.length} records</span>
          </div>

          {fetching ? (
            <div className="admin-loading"><div className="admin-spinner" /><p>Loading…</p></div>
          ) : filtered.length === 0 ? (
            <div className="admin-empty"><p>No donations found.</p></div>
          ) : (
            <table className="admin-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Amount</th>
                  <th>Status</th>
                  <th>Date</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((d) => {
                  const style = getStatusStyle(d.status);
                  return (
                    <tr key={d.id}>
                      <td style={{ fontFamily: "monospace", fontSize: 11, color: "#94a3b8" }}>
                        {d.id?.slice(0, 8)}…
                      </td>
                      <td style={{ fontWeight: 700, color: "#1e1b4b" }}>{formatAmount(d.amount)}</td>
                      <td>
                        <span className="admin-badge-status" style={{ background: style.bg, color: style.color }}>
                          {d.status}
                        </span>
                      </td>
                      <td>{formatDate(d.donated_at)}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
}