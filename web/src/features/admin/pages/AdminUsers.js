import { useEffect, useState, useCallback } from "react";
import { supabase } from "../../../supabaseClient";
import { useAdminAuth } from "../useAdminAuth";
import AdminSidebar from "../AdminSidebar";
import "../Admin.css";

function formatDate(iso) {
  if (!iso) return "—";
  return new Date(iso).toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" });
}

function getRoleStyle(role) {
  if (role === "admin") return { bg: "#ede9fe", color: "#5b21b6" };
  return                        { bg: "#f3f4f6", color: "#374151" };
}

export default function AdminUsers() {
  const { loading, authed } = useAdminAuth();

  const [users,    setUsers]    = useState([]);
  const [search,   setSearch]   = useState("");
  const [fetching, setFetching] = useState(true);

  const fetchUsers = useCallback(async () => {
    setFetching(true);
    const { data, error } = await supabase.from("users").select("*").order("created_at", { ascending: false });
    if (!error) setUsers(data ?? []);
    setFetching(false);
  }, []);

  useEffect(() => { if (!loading) fetchUsers(); }, [loading, fetchUsers]);

  const filtered = users.filter((u) =>
    u.username?.toLowerCase().includes(search.toLowerCase()) ||
    u.email?.toLowerCase().includes(search.toLowerCase())
  );

  if (!authed) return null;

  return (
    <div className="admin-layout">
      <AdminSidebar />

      <div className="admin-main">
        <div className="admin-page-header">
          <h1 className="admin-page-title">Users</h1>
          <p className="admin-page-sub">View all registered users on the platform</p>
        </div>

        <div className="admin-card">
          <div className="admin-filter-bar">
            <div className="admin-search-box">
              <i className="ti ti-search" style={{ fontSize: 14, color: "#9ca3af", flexShrink: 0 }} aria-hidden="true" />
              <input
                type="text"
                placeholder="Search by username or email…"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </div>
            <span className="admin-record-count">{filtered.length} users</span>
          </div>

          {fetching ? (
            <div className="admin-loading"><div className="admin-spinner" /><p>Loading…</p></div>
          ) : filtered.length === 0 ? (
            <div className="admin-empty"><p>No users found.</p></div>
          ) : (
            <table className="admin-table">
              <thead>
                <tr>
                  <th>User</th>
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
                        <div className="admin-user-cell">
                          <div className="admin-user-avatar" style={{
                            background: u.role === "admin" ? "#6366f1" : "#111827",
                          }}>
                            {u.username?.slice(0, 2).toUpperCase() ?? "??"}
                          </div>
                          <span className="admin-td-bold">{u.username}</span>
                        </div>
                      </td>
                      <td className="admin-td-muted">{u.email}</td>
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