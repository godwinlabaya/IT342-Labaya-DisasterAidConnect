import { useEffect, useState, useCallback } from "react";
import { supabase } from "../../../supabaseClient";
import { useAdminAuth } from "../useAdminAuth";
import AdminSidebar from "../AdminSidebar";
import "../Admin.css";

function Toast({ message, type }) {
  if (!message) return null;
  return (
    <div className={`toast toast-${type}`}>
      <i className={`ti ${type === "success" ? "ti-circle-check" : "ti-alert-circle"} toast-icon`} aria-hidden="true" />
      <span>{message}</span>
    </div>
  );
}

function getStatusStyle(status) {
  if (status === "Approved")  return { bg: "#f0fdf4", color: "#166534" };
  if (status === "Fulfilled") return { bg: "#eff6ff", color: "#1d4ed8" };
  if (status === "Rejected")  return { bg: "#fef2f2", color: "#991b1b" };
  return                             { bg: "#fffbeb", color: "#92400e" };
}

function formatDate(iso) {
  if (!iso) return "—";
  return new Date(iso).toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" });
}

const STATUS_OPTIONS = ["Pending", "Approved", "Fulfilled", "Rejected"];

export default function AdminAidRequests() {
  const { loading, authed } = useAdminAuth();

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
    const { data, error } = await supabase.from("aid_requests").select("*").order("created_at", { ascending: false });
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

  if (!authed) return null;

  return (
    <div className="admin-layout">
      <Toast message={toast.message} type={toast.type} />
      <AdminSidebar />

      <div className="admin-main">
        <div className="admin-page-header">
          <h1 className="admin-page-title">Aid Requests</h1>
          <p className="admin-page-sub">Approve or reject aid requests from users</p>
        </div>

        {/* Tabs */}
        <div className="admin-tabs">
          {tabs.map((tab) => (
            <button
              key={tab}
              className={`admin-tab ${activeTab === tab ? "admin-tab-active" : ""}`}
              onClick={() => setActiveTab(tab)}
            >
              {tab}
              <span className="admin-tab-count">
                {tab === "All" ? requests.length : requests.filter((r) => r.status === tab).length}
              </span>
            </button>
          ))}
        </div>

        <div className="admin-card">
          <div className="admin-filter-bar">
            <div className="admin-search-box">
              <i className="ti ti-search" style={{ fontSize: 14, color: "#9ca3af", flexShrink: 0 }} aria-hidden="true" />
              <input
                type="text"
                placeholder="Search by aid type or description…"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </div>
            <span className="admin-record-count">{filtered.length} records</span>
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
                      <td className="admin-td-bold">{r.aid_type}</td>
                      <td className="admin-td-truncate">{r.description}</td>
                      <td>{r.quantity ?? "—"}</td>
                      <td>
                        <span className="admin-badge-status" style={{ background: style.bg, color: style.color }}>
                          {r.status}
                        </span>
                      </td>
                      <td>{formatDate(r.created_at)}</td>
                      <td>
                        <div className="admin-action-btns">
                          {r.status === "Pending" && (
                            <>
                              <button className="admin-btn admin-btn-success" onClick={() => updateStatus(r.id, "Approved")}>
                                <i className="ti ti-check" aria-hidden="true" /> Approve
                              </button>
                              <button className="admin-btn admin-btn-danger" onClick={() => updateStatus(r.id, "Rejected")}>
                                <i className="ti ti-x" aria-hidden="true" /> Reject
                              </button>
                            </>
                          )}
                          {r.status === "Approved" && (
                            <button className="admin-btn admin-btn-primary" onClick={() => updateStatus(r.id, "Fulfilled")}>
                              <i className="ti ti-circle-check" aria-hidden="true" /> Fulfill
                            </button>
                          )}
                          {(r.status === "Fulfilled" || r.status === "Rejected") && (
                            <span className="admin-no-action">No actions</span>
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