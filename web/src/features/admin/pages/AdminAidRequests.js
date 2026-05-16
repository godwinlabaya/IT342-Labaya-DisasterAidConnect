import { useEffect, useState, useCallback } from "react";
import { supabase } from "../../../supabaseClient";
import { useAdminAuth } from "../useAdminAuth";
import AdminSidebar from "../AdminSidebar";
import "../Admin.css";

function Toast({ message, type }) {
  if (!message) return null;
  return (
    <div className={`toast toast-${type}`}>
      <span className="toast-icon">{type === "success" ? "✅" : "❌"}</span>
      <span>{message}</span>
    </div>
  );
}

function getStatusStyle(status) {
  if (status === "Approved")  return { bg: "#dcfce7", color: "#166534" };
  if (status === "Fulfilled") return { bg: "#e0e7ff", color: "#3730a3" };
  if (status === "Rejected")  return { bg: "#fee2e2", color: "#991b1b" };
  return                             { bg: "#fef9c3", color: "#854d0e" }; // Pending
}

function formatDate(iso) {
  if (!iso) return "—";
  return new Date(iso).toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" });
}

const STATUS_OPTIONS = ["Pending", "Approved", "Fulfilled", "Rejected"];

export default function AdminAidRequests() {
  const { loading } = useAdminAuth();

  const [requests,  setRequests]  = useState([]);
  const [search,    setSearch]    = useState("");
  const [fetching,  setFetching]  = useState(true);
  const [activeTab, setActiveTab] = useState("All");
  const [toast,     setToast]     = useState({ message: "", type: "" });

  const showToast = (message, type = "success") => {
    setToast({ message, type });
    setTimeout(() => setToast({ message: "", type: "" }), 3000);
  };

  const fetchRequests = useCallback(async () => {
    setFetching(true);
    const { data, error } = await supabase
      .from("aid_requests")
      .select("*")
      .order("created_at", { ascending: false });
    if (!error) setRequests(data ?? []);
    setFetching(false);
  }, []);

  useEffect(() => { if (!loading) fetchRequests(); }, [loading, fetchRequests]);

  const updateStatus = async (id, status) => {
    const { error } = await supabase.from("aid_requests").update({ status }).eq("id", id);
    if (error) {
      showToast("Failed to update: " + error.message, "error");
    } else {
      setRequests((prev) => prev.map((r) => r.id === id ? { ...r, status } : r));
      showToast(`Request marked as ${status}.`, "success");
    }
  };

  const tabs = ["All", ...STATUS_OPTIONS];

  const filtered = requests.filter((r) => {
    const matchTab  = activeTab === "All" || r.status === activeTab;
    const matchSrch = !search || r.description?.toLowerCase().includes(search.toLowerCase()) || r.aid_type?.toLowerCase().includes(search.toLowerCase());
    return matchTab && matchSrch;
  });

  if (loading) return <div className="admin-layout"><div className="admin-loading"><div className="admin-spinner" /></div></div>;

  return (
    <div className="admin-layout">
      <Toast message={toast.message} type={toast.type} />
      <AdminSidebar />

      <div className="admin-main">
        <div className="admin-page-header">
          <h1 className="admin-page-title">📋 Aid Requests</h1>
          <p className="admin-page-sub">Approve or reject aid requests from users</p>
        </div>

        {/* Tabs */}
        <div style={{ display: "flex", gap: 8, marginBottom: 20, flexWrap: "wrap" }}>
          {tabs.map((tab) => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              style={{
                padding: "7px 16px",
                borderRadius: 20,
                border: "1px solid",
                borderColor: activeTab === tab ? "#4f46e5" : "#e2e8f0",
                background: activeTab === tab ? "#4f46e5" : "white",
                color: activeTab === tab ? "white" : "#64748b",
                fontSize: 13,
                fontWeight: 500,
                cursor: "pointer",
              }}
            >
              {tab}
              <span style={{
                marginLeft: 6,
                background: activeTab === tab ? "rgba(255,255,255,0.25)" : "#f1f5f9",
                color: activeTab === tab ? "white" : "#94a3b8",
                padding: "1px 7px",
                borderRadius: 10,
                fontSize: 11,
                fontWeight: 700,
              }}>
                {tab === "All" ? requests.length : requests.filter((r) => r.status === tab).length}
              </span>
            </button>
          ))}
        </div>

        <div className="admin-card">
          <div className="admin-filter-bar">
            <div className="admin-search-box">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#94a3b8" strokeWidth="2.5">
                <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
              </svg>
              <input
                type="text"
                placeholder="Search by aid type or description…"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </div>
            <span style={{ fontSize: 13, color: "#94a3b8" }}>{filtered.length} records</span>
          </div>

          {fetching ? (
            <div className="admin-loading"><div className="admin-spinner" /><p>Loading…</p></div>
          ) : filtered.length === 0 ? (
            <div className="admin-empty"><p>No aid requests found.</p></div>
          ) : (
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Aid Type</th>
                  <th>Description</th>
                  <th>Quantity</th>
                  <th>Status</th>
                  <th>Date</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((r) => {
                  const style = getStatusStyle(r.status);
                  return (
                    <tr key={r.id}>
                      <td style={{ fontWeight: 600, color: "#1e1b4b" }}>{r.aid_type}</td>
                      <td style={{ maxWidth: 200, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                        {r.description}
                      </td>
                      <td>{r.quantity ?? "—"}</td>
                      <td>
                        <span className="admin-badge-status" style={{ background: style.bg, color: style.color }}>
                          {r.status}
                        </span>
                      </td>
                      <td>{formatDate(r.created_at)}</td>
                      <td>
                        <div style={{ display: "flex", gap: 6 }}>
                          {r.status === "Pending" && (
                            <>
                              <button className="admin-btn admin-btn-success" onClick={() => updateStatus(r.id, "Approved")}>✅ Approve</button>
                              <button className="admin-btn admin-btn-danger"  onClick={() => updateStatus(r.id, "Rejected")}>❌ Reject</button>
                            </>
                          )}
                          {r.status === "Approved" && (
                            <button className="admin-btn admin-btn-primary" onClick={() => updateStatus(r.id, "Fulfilled")}>✔ Fulfill</button>
                          )}
                          {(r.status === "Fulfilled" || r.status === "Rejected") && (
                            <span style={{ fontSize: 12, color: "#94a3b8" }}>No actions</span>
                          )}
                        </div>
                      </td>
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