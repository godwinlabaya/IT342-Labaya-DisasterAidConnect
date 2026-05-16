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

function getRoleStyle(role) {
  if (role === "admin") return { bg: "#ede9fe", color: "#5b21b6" };
  return                        { bg: "#f1f5f9", color: "#475569" };
}

export default function AdminUsers() {
  const { loading } = useAdminAuth();

  const [users,    setUsers]    = useState([]);
  const [search,   setSearch]   = useState("");
  const [fetching, setFetching] = useState(true);

  const fetchUsers = useCallback(async () => {
    setFetching(true);
    const { data, error } = await supabase
      .from("users")
      .select("*")
      .order("created_at", { ascending: false });
    if (!error) setUsers(data ?? []);
    setFetching(false);
  }, []);

  useEffect(() => { if (!loading) fetchUsers(); }, [loading, fetchUsers]);

  const filtered = users.filter((u) =>
    u.username?.toLowerCase().includes(search.toLowerCase()) ||
    u.email?.toLowerCase().includes(search.toLowerCase())
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
          <h1 className="admin-page-title">👥 Users</h1>
          <p className="admin-page-sub">View all registered users on the platform</p>
        </div>

        <div className="admin-card">
          <div className="admin-filter-bar">
            <div className="admin-search-box">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#94a3b8" strokeWidth="2.5">
                <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
              </svg>
              <input
                type="text"
                placeholder="Search by username or email…"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </div>
            <span style={{ fontSize: 13, color: "#94a3b8" }}>{filtered.length} users</span>
          </div>

          {fetching ? (
            <div className="admin-loading"><div className="admin-spinner" /><p>Loading…</p></div>
          ) : filtered.length === 0 ? (
            <div className="admin-empty"><p>No users found.</p></div>
          ) : (
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Avatar</th>
                  <th>Username</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th>Joined</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((u) => {
                  const roleStyle = getRoleStyle(u.role);
                  return (
                    <tr key={u.id}>
                      <td>
                        <div style={{
                          width: 34, height: 34, borderRadius: "50%",
                          background: u.role === "admin" ? "#4f46e5" : "#0a2942",
                          color: "white", display: "flex", alignItems: "center",
                          justifyContent: "center", fontWeight: 700, fontSize: 12,
                        }}>
                          {u.username?.slice(0, 2).toUpperCase() ?? "??"}
                        </div>
                      </td>
                      <td style={{ fontWeight: 600, color: "#1e1b4b" }}>{u.username}</td>
                      <td style={{ color: "#64748b" }}>{u.email}</td>
                      <td>
                        <span className="admin-badge-status" style={{ background: roleStyle.bg, color: roleStyle.color }}>
                          {u.role ?? "user"}
                        </span>
                      </td>
                      <td>{formatDate(u.created_at)}</td>
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