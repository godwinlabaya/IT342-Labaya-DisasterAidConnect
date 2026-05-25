import { useEffect, useState, useCallback } from "react";
import { supabase } from "../../../supabaseClient";
import { useAdminAuth } from "../useAdminAuth";
import AdminSidebar from "../AdminSidebar";
import "../Admin.css";
import "./AdminUsers.css";

// ── Helpers ───────────────────────────────────────────────────────────────────
function formatDate(iso) {
  if (!iso) return "—";
  return new Date(iso).toLocaleDateString("en-US", {
    month: "short", day: "numeric", year: "numeric",
  });
}

function formatDateTime(iso) {
  if (!iso) return "—";
  return new Date(iso).toLocaleString("en-US", {
    month: "short", day: "numeric", year: "numeric",
    hour: "numeric", minute: "2-digit", hour12: true,
  });
}

function getRoleStyle(role) {
  if (role === "admin") return { bg: "#ede9fe", color: "#5b21b6" };
  return { bg: "#f3f4f6", color: "#374151" };
}

function isMuteActive(user) {
  if (!user.is_muted) return false;
  if (!user.mute_until) return true;
  return new Date(user.mute_until) > new Date();
}

// ── Toast ─────────────────────────────────────────────────────────────────────
function Toast({ message, type, onDone }) {
  useEffect(() => {
    if (!message) return;
    const t = setTimeout(onDone, 3200);
    return () => clearTimeout(t);
  }, [message, onDone]);

  if (!message) return null;
  return (
    <div className={`toast toast-${type}`}>
      <i
        className={`ti ${type === "success" ? "ti-circle-check" : "ti-alert-circle"} toast-icon`}
        aria-hidden="true"
      />
      <span>{message}</span>
    </div>
  );
}

// ── Mute Modal ────────────────────────────────────────────────────────────────
const PRESETS = [1, 3, 7, 14, 30];

function MuteModal({ user, onConfirm, onCancel }) {
  const [days,   setDays]   = useState(3);
  const [reason, setReason] = useState("");
  const [error,  setError]  = useState("");

  const expiryLabel = () => {
    const d = Number(days);
    if (!d || d < 1) return "—";
    return new Date(Date.now() + d * 86400000).toLocaleDateString("en-US", {
      month: "short", day: "numeric", year: "numeric",
    });
  };

  const handleConfirm = () => {
    if (!reason.trim())      { setError("Please provide a reason for the mute."); return; }
    if (!days || Number(days) < 1) { setError("Duration must be at least 1 day."); return; }
    onConfirm({ days: Number(days), reason: reason.trim() });
  };

  return (
    <div className="admin-backdrop" onClick={onCancel}>
      <div className="au-modal" onClick={e => e.stopPropagation()}>

        {/* Header */}
        <div className="au-modal-header">
          <div className="au-modal-icon">
            <i className="ti ti-ban" aria-hidden="true" />
          </div>
          <div className="au-modal-titles">
            <h3 className="au-modal-title">Mute User</h3>
            <p className="au-modal-sub">
              Restricting <strong>{user.username}</strong> from adding map points
            </p>
          </div>
          <button className="au-modal-close" onClick={onCancel} aria-label="Close">
            <i className="ti ti-x" aria-hidden="true" />
          </button>
        </div>

        {/* Body */}
        <div className="au-modal-body">

          {/* Preset chips */}
          <div>
            <p className="au-label">Duration</p>
            <div className="au-presets">
              {PRESETS.map(d => (
                <button
                  key={d}
                  className={`au-preset-btn${days === d ? " au-preset-btn--active" : ""}`}
                  onClick={() => { setDays(d); setError(""); }}
                >
                  {d}d
                </button>
              ))}
            </div>
          </div>

          {/* Custom number */}
          <div className="au-field">
            <p className="au-label">Custom days</p>
            <div className="au-input-wrap">
              <i className="ti ti-calendar-time au-input-icon" aria-hidden="true" />
              <input
                type="number"
                min={1}
                max={365}
                value={days}
                onChange={e => { setDays(e.target.value); setError(""); }}
                className="au-input"
                placeholder="e.g. 7"
              />
              <span className="au-input-suffix">days</span>
            </div>
            <p className="au-field-hint">
              Expires: <strong>{expiryLabel()}</strong>
            </p>
          </div>

          {/* Reason */}
          <div className="au-field">
            <p className="au-label">
              Reason <span className="au-required">*</span>
            </p>
            <textarea
              className="au-textarea"
              rows={3}
              placeholder="e.g. Spamming false disaster reports…"
              value={reason}
              onChange={e => { setReason(e.target.value); setError(""); }}
            />
          </div>

          {error && (
            <div className="au-error-banner">
              <i className="ti ti-alert-circle" aria-hidden="true" /> {error}
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="au-modal-footer">
          <button className="admin-btn au-btn-ghost" onClick={onCancel}>Cancel</button>
          <button className="admin-btn au-btn-warn" onClick={handleConfirm}>
            <i className="ti ti-ban" aria-hidden="true" /> Apply Mute
          </button>
        </div>
      </div>
    </div>
  );
}

// ── Confirm Dialog ─────────────────────────────────────────────────────────────
function ConfirmDialog({ title, message, onConfirm, onCancel, danger }) {
  return (
    <div className="admin-backdrop" onClick={onCancel}>
      <div className="admin-confirm" onClick={e => e.stopPropagation()}>
        <div className={`admin-confirm-icon${danger ? " admin-confirm-icon--danger" : ""}`}>
          <i className={`ti ${danger ? "ti-trash" : "ti-alert-circle"}`} aria-hidden="true" />
        </div>
        <h3>{title}</h3>
        <p>{message}</p>
        <div className="admin-confirm-actions">
          <button className="admin-btn au-btn-ghost" onClick={onCancel}>Cancel</button>
          <button
            className={`admin-btn ${danger ? "admin-btn-danger" : "admin-btn-primary"}`}
            onClick={onConfirm}
          >
            {danger ? "Delete" : "Confirm"}
          </button>
        </div>
      </div>
    </div>
  );
}

// ── Main Component ─────────────────────────────────────────────────────────────
export default function AdminUsers() {
  const { loading, authed } = useAdminAuth();

  const [users,         setUsers]         = useState([]);
  const [search,        setSearch]        = useState("");
  const [fetching,      setFetching]      = useState(true);
  const [muteTarget,    setMuteTarget]    = useState(null);
  const [unmuteTarget,  setUnmuteTarget]  = useState(null);
  const [deleteTarget,  setDeleteTarget]  = useState(null);
  const [toast,         setToast]         = useState({ message: "", type: "success" });

  const showToast = (message, type = "success") => setToast({ message, type });

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

  // ── Mute ───────────────────────────────────────────────────────────────────
  const handleMuteConfirm = async ({ days, reason }) => {
    const user = muteTarget;
    setMuteTarget(null);
    const muteUntil = new Date(Date.now() + days * 86400000).toISOString();
    const { error } = await supabase
      .from("users")
      .update({ is_muted: true, mute_until: muteUntil, mute_reason: reason })
      .eq("id", user.id);
    if (error) { showToast("Failed to mute user.", "error"); return; }
    showToast(`${user.username} muted for ${days} day(s).`);
    fetchUsers();
  };

  // ── Unmute ─────────────────────────────────────────────────────────────────
  const handleUnmuteConfirm = async () => {
    const user = unmuteTarget;
    setUnmuteTarget(null);
    const { error } = await supabase
      .from("users")
      .update({ is_muted: false, mute_until: null, mute_reason: null })
      .eq("id", user.id);
    if (error) { showToast("Failed to unmute user.", "error"); return; }
    showToast(`${user.username} has been unmuted.`);
    fetchUsers();
  };

  // ── Delete ─────────────────────────────────────────────────────────────────
  const handleDeleteConfirm = async () => {
    const user = deleteTarget;
    setDeleteTarget(null);
    // FK on delete CASCADE handles auth.users + all related rows
    const { error } = await supabase.from("users").delete().eq("id", user.id);
    if (error) { showToast("Failed to delete user.", "error"); return; }
    showToast(`${user.username} permanently deleted.`);
    fetchUsers();
  };

  const filtered = users.filter(u =>
    u.username?.toLowerCase().includes(search.toLowerCase()) ||
    u.email?.toLowerCase().includes(search.toLowerCase())
  );

  if (!authed) return null;

  return (
    <div className="admin-layout">
      <AdminSidebar />

      <Toast
        message={toast.message}
        type={toast.type}
        onDone={() => setToast({ message: "", type: "success" })}
      />

      {muteTarget && (
        <MuteModal
          user={muteTarget}
          onConfirm={handleMuteConfirm}
          onCancel={() => setMuteTarget(null)}
        />
      )}

      {unmuteTarget && (
        <ConfirmDialog
          title="Unmute User"
          message={`Remove the mute from ${unmuteTarget.username}? They will be able to add map points again immediately.`}
          onConfirm={handleUnmuteConfirm}
          onCancel={() => setUnmuteTarget(null)}
        />
      )}

      {deleteTarget && (
        <ConfirmDialog
          danger
          title="Delete User"
          message={`Permanently delete ${deleteTarget.username}? This removes their account, disasters, and donations. This cannot be undone.`}
          onConfirm={handleDeleteConfirm}
          onCancel={() => setDeleteTarget(null)}
        />
      )}

      <div className="admin-main">
        <div className="admin-page-header">
          <h1 className="admin-page-title">Users</h1>
          <p className="admin-page-sub">View and manage all registered users on the platform</p>
        </div>

        <div className="admin-card">
          <div className="admin-filter-bar">
            <div className="admin-search-box">
              <i className="ti ti-search" style={{ fontSize: 14, color: "#9ca3af", flexShrink: 0 }} aria-hidden="true" />
              <input
                type="text"
                placeholder="Search by username or email…"
                value={search}
                onChange={e => setSearch(e.target.value)}
              />
            </div>
            <span className="admin-record-count">{filtered.length} users</span>
          </div>

          {fetching ? (
            <div className="admin-loading">
              <div className="admin-spinner" />
              <p>Loading…</p>
            </div>
          ) : filtered.length === 0 ? (
            <div className="admin-empty"><p>No users found.</p></div>
          ) : (
            <table className="admin-table">
              <thead>
                <tr>
                  <th>User</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th>Status</th>
                  <th>Joined</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map(u => {
                  const roleStyle = getRoleStyle(u.role);
                  const muted     = isMuteActive(u);

                  return (
                    <tr key={u.id} className={muted ? "au-row-muted" : ""}>

                      {/* User */}
                      <td>
                        <div className="admin-user-cell">
                          <div
                            className="admin-user-avatar"
                            style={{ background: u.role === "admin" ? "#6366f1" : "#111827" }}
                          >
                            {u.username?.slice(0, 2).toUpperCase() ?? "??"}
                          </div>
                          <span className="admin-td-bold">{u.username}</span>
                        </div>
                      </td>

                      {/* Email */}
                      <td className="admin-td-muted">{u.email}</td>

                      {/* Role */}
                      <td>
                        <span
                          className="admin-badge-status"
                          style={{ background: roleStyle.bg, color: roleStyle.color }}
                        >
                          {u.role ?? "user"}
                        </span>
                      </td>

                      {/* Mute status */}
                      <td>
                        {muted ? (
                          <div className="au-mute-info">
                            <span className="au-mute-badge">
                              <i className="ti ti-ban" aria-hidden="true" /> Muted
                            </span>
                            {u.mute_until && (
                              <span className="au-mute-expiry">
                                until {formatDateTime(u.mute_until)}
                              </span>
                            )}
                            {u.mute_reason && (
                              <span className="au-mute-reason" title={u.mute_reason}>
                                "{u.mute_reason.length > 32
                                  ? u.mute_reason.slice(0, 32) + "…"
                                  : u.mute_reason}"
                              </span>
                            )}
                          </div>
                        ) : (
                          <span className="au-active-badge">
                            <i className="ti ti-circle-check" aria-hidden="true" /> Active
                          </span>
                        )}
                      </td>

                      {/* Joined */}
                      <td>{formatDate(u.created_at)}</td>

                      {/* Actions */}
                      <td>
                        {u.role !== "admin" ? (
                          <div className="admin-action-btns">
                            {muted ? (
                              <button
                                className="admin-btn admin-btn-success au-action-btn"
                                onClick={() => setUnmuteTarget(u)}
                                title="Unmute user"
                              >
                                <i className="ti ti-volume" aria-hidden="true" /> Unmute
                              </button>
                            ) : (
                              <button
                                className="admin-btn admin-btn-warning au-action-btn"
                                onClick={() => setMuteTarget(u)}
                                title="Mute user"
                              >
                                <i className="ti ti-ban" aria-hidden="true" /> Mute
                              </button>
                            )}
                            <button
                              className="admin-btn admin-btn-danger au-action-btn"
                              onClick={() => setDeleteTarget(u)}
                              title="Delete user"
                            >
                              <i className="ti ti-trash" aria-hidden="true" />
                            </button>
                          </div>
                        ) : (
                          <span className="admin-no-action">—</span>
                        )}
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