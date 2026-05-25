import { useEffect, useState, useRef, useCallback } from "react";
import { useLocation } from "react-router-dom";
import { MapContainer, TileLayer, Marker, Popup, useMap } from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import { supabase } from "../../../supabaseClient";
import { useAdminAuth } from "../useAdminAuth";
import AdminSidebar from "../AdminSidebar";
import { createMarkerIcon, getSeverityColor } from "../../disaster/iconFactory";
import disasterService from "../../disaster/disasterService";
import "../Admin.css";
import "./AdminMap.css";

delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: require("leaflet/dist/images/marker-icon-2x.png"),
  iconUrl:       require("leaflet/dist/images/marker-icon.png"),
  shadowUrl:     require("leaflet/dist/images/marker-shadow.png"),
});

const DEFAULT_CENTER  = [10.3157, 123.8854];
const DEFAULT_ZOOM    = 13;
const SEVERITY_LEVELS = ["Low", "Medium", "High", "Critical"];
const STATUS_OPTIONS  = ["Active", "Monitoring", "Resolved"];
const EMPTY_FORM      = { title: "", description: "", severity_level: "Medium", status: "Active", gcash_number: "" };

function formatDate(iso) {
  if (!iso) return "";
  return new Date(iso).toLocaleString("en-US", {
    month: "long", day: "numeric", year: "numeric",
    hour: "numeric", minute: "2-digit", hour12: true,
  });
}

function FlyTo({ position }) {
  const map  = useMap();
  const prev = useRef(null);
  useEffect(() => {
    if (position && JSON.stringify(position) !== JSON.stringify(prev.current)) {
      prev.current = position;
      map.flyTo(position, 15, { duration: 1.2 });
    }
  }, [position, map]);
  return null;
}

// ── Toast ─────────────────────────────────────────────────────────────────────
function Toast({ message, type, onDone }) {
  useEffect(() => {
    if (!message) return;
    const t = setTimeout(onDone, 3000);
    return () => clearTimeout(t);
  }, [message, onDone]);
  if (!message) return null;
  return (
    <div className={`toast toast-${type}`}>
      <i className={`ti ${type === "success" ? "ti-circle-check" : "ti-alert-circle"} toast-icon`} aria-hidden="true" />
      <span>{message}</span>
    </div>
  );
}

// ── Confirm Dialog ────────────────────────────────────────────────────────────
function ConfirmDialog({ message, onConfirm, onCancel }) {
  return (
    <div className="am-backdrop" onClick={onCancel}>
      <div className="am-confirm" onClick={e => e.stopPropagation()}>
        <div className="am-confirm-icon">
          <i className="ti ti-trash" aria-hidden="true" />
        </div>
        <h3>Delete Disaster?</h3>
        <p>{message}</p>
        <div className="am-confirm-actions">
          <button className="am-btn am-btn-ghost" onClick={onCancel}>Cancel</button>
          <button className="am-btn am-btn-danger" onClick={onConfirm}>Yes, Delete</button>
        </div>
      </div>
    </div>
  );
}

// ── Edit Form ─────────────────────────────────────────────────────────────────
function EditForm({ disaster, onSave, onCancel, saving, error }) {
  const [form, setForm] = useState({
    title:          disaster.title          ?? "",
    description:    disaster.description    ?? "",
    severity_level: disaster.severity_level ?? "Medium",
    status:         disaster.status         ?? "Active",
    gcash_number:   disaster.gcash_number   ?? "",
  });

  return (
    <div className="am-edit-overlay">
      <div className="am-edit-form">
        <div className="am-edit-header">
          <h3>Edit Disaster Point</h3>
          <button className="am-edit-close" onClick={onCancel}>✕</button>
        </div>

        <div className="am-field">
          <label>Title <span className="am-required">*</span></label>
          <input
            type="text"
            value={form.title}
            onChange={e => setForm({ ...form, title: e.target.value })}
            placeholder="e.g. Fire Breakout"
          />
        </div>

        <div className="am-field">
          <label>Description <span className="am-required">*</span></label>
          <textarea
            rows={3}
            value={form.description}
            onChange={e => setForm({ ...form, description: e.target.value })}
            placeholder="Describe the situation…"
          />
        </div>

        <div className="am-form-row">
          <div className="am-field">
            <label>Severity</label>
            <select value={form.severity_level} onChange={e => setForm({ ...form, severity_level: e.target.value })}>
              {SEVERITY_LEVELS.map(s => <option key={s}>{s}</option>)}
            </select>
          </div>
          <div className="am-field">
            <label>Status</label>
            <select value={form.status} onChange={e => setForm({ ...form, status: e.target.value })}>
              {STATUS_OPTIONS.map(s => <option key={s}>{s}</option>)}
            </select>
          </div>
        </div>

        <div className="am-field">
          <label>GCash Number</label>
          <input
            type="text"
            value={form.gcash_number}
            onChange={e => setForm({ ...form, gcash_number: e.target.value })}
            placeholder="e.g. 09123456789"
          />
        </div>

        {error && <p className="am-error">{error}</p>}

        <div className="am-form-actions">
          <button className="am-btn am-btn-ghost" onClick={onCancel}>Cancel</button>
          <button
            className="am-btn am-btn-primary"
            onClick={() => onSave(form)}
            disabled={saving}
          >
            {saving ? "Saving…" : "Save Changes"}
          </button>
        </div>
      </div>
    </div>
  );
}

// ── Proof Images ──────────────────────────────────────────────────────────────
function ProofImages({ disaster }) {
  const urls = [disaster.image_url_1, disaster.image_url_2, disaster.image_url_3].filter(Boolean);
  const [lightbox, setLightbox] = useState(null);
  if (urls.length === 0) return null;
  return (
    <>
      <div className="am-proof-section">
        <p className="am-proof-label">📸 Proof Images</p>
        <div className="am-proof-row">
          {urls.map((url, i) => (
            <img key={i} src={url} alt={`proof-${i+1}`} className="am-proof-thumb"
              onClick={() => setLightbox(url)} title="Click to enlarge" />
          ))}
        </div>
      </div>
      {lightbox && (
        <div className="am-lightbox" onClick={() => setLightbox(null)}>
          <div className="am-lightbox-box" onClick={e => e.stopPropagation()}>
            <button className="am-lightbox-close" onClick={() => setLightbox(null)}>✕</button>
            <img src={lightbox} alt="proof-full" className="am-lightbox-img" />
          </div>
        </div>
      )}
    </>
  );
}

// ── Main Component ────────────────────────────────────────────────────────────
export default function AdminMap() {
  const { loading, authed } = useAdminAuth();
  const location = useLocation();

  const [disasters,      setDisasters]      = useState([]);
  const [selected,       setSelected]       = useState(null);
  const [search,         setSearch]         = useState("");
  const [fetching,       setFetching]       = useState(true);
  const [severityFilter, setSeverityFilter] = useState("All");

  const [showEdit,      setShowEdit]      = useState(false);
  const [editSaving,    setEditSaving]    = useState(false);
  const [editError,     setEditError]     = useState("");

  const [confirmDelete, setConfirmDelete] = useState(false);
  const [deleting,      setDeleting]      = useState(false);

  const [toast, setToast] = useState({ message: "", type: "success" });
  const showToast = (message, type = "success") => setToast({ message, type });

  const fetchDisasters = useCallback(async () => {
    setFetching(true);
    try {
      const data = await disasterService.getAll();
      setDisasters(data);
      if (data.length > 0) setSelected(prev => prev ?? data[0]);
    } catch (err) {
      showToast("Failed to load disasters.", "error");
    } finally {
      setFetching(false);
    }
  }, []);

  useEffect(() => { if (!loading) fetchDisasters(); }, [loading, fetchDisasters]);

  // ── Focus disaster from navigation state (e.g. from AdminDisasters "View on Map") ──
  useEffect(() => {
    const focusId = location.state?.focusDisasterId;
    if (!focusId || disasters.length === 0) return;
    const target = disasters.find(d => d.id === focusId);
    if (target) {
      setSelected(target);
      window.history.replaceState({}, ""); // clear state so it doesn't re-focus on refresh
    }
  }, [disasters, location.state]);

  // ── Edit ──────────────────────────────────────────────────────────────────
  const handleEditSave = async (form) => {
    if (!form.title.trim())       { setEditError("Title is required.");       return; }
    if (!form.description.trim()) { setEditError("Description is required."); return; }
    setEditSaving(true); setEditError("");
    try {
      const updated = await disasterService.update(selected.id, {
        title:          form.title.trim(),
        description:    form.description.trim(),
        severity_level: form.severity_level,
        status:         form.status,
        gcash_number:   form.gcash_number?.trim() || null,
      });
      setDisasters(prev => prev.map(d => d.id === updated.id ? updated : d));
      setSelected(updated);
      setShowEdit(false);
      showToast("Disaster updated successfully.");
    } catch (err) {
      setEditError("Failed to update: " + err.message);
    } finally {
      setEditSaving(false);
    }
  };

  // ── Delete ─────────────────────────────────────────────────────────────────
  const handleDelete = async () => {
    setDeleting(true);
    try {
      await disasterService.remove(selected.id);
      const remaining = disasters.filter(d => d.id !== selected.id);
      setDisasters(remaining);
      setSelected(remaining[0] ?? null);
      setConfirmDelete(false);
      showToast("Disaster deleted successfully.");
    } catch (err) {
      showToast("Failed to delete: " + err.message, "error");
    } finally {
      setDeleting(false);
    }
  };

  const severities = ["All", "Critical", "High", "Medium", "Low"];

  const filtered = disasters.filter(d => {
    const matchSearch   = d.title?.toLowerCase().includes(search.toLowerCase()) ||
                          d.description?.toLowerCase().includes(search.toLowerCase());
    const matchSeverity = severityFilter === "All" || d.severity_level === severityFilter;
    return matchSearch && matchSeverity;
  });

  const flyPosition = selected?.latitude && selected?.longitude
    ? [selected.latitude, selected.longitude]
    : null;

  if (!authed) return null;

  return (
    <div className="admin-layout">
      <AdminSidebar />

      <Toast
        message={toast.message}
        type={toast.type}
        onDone={() => setToast({ message: "", type: "success" })}
      />

      {confirmDelete && (
        <ConfirmDialog
          message={`Permanently delete "${selected?.title}"? This cannot be undone.`}
          onConfirm={handleDelete}
          onCancel={() => setConfirmDelete(false)}
        />
      )}

      {/* Full-height map layout inside admin-main */}
      <div className="am-page">

        {/* Top bar */}
        <div className="am-topbar">
          <div className="am-topbar-left">
            <h1 className="am-title">Disaster Map</h1>
            <span className="am-count">{filtered.length} points</span>
          </div>

          <div className="am-topbar-right">
            {/* Severity filter chips */}
            <div className="am-chips">
              {severities.map(s => (
                <button
                  key={s}
                  className={`am-chip ${severityFilter === s ? "am-chip-active" : ""}`}
                  onClick={() => setSeverityFilter(s)}
                  style={severityFilter === s && s !== "All" ? {
                    background: getSeverityColor(s) + "20",
                    color: getSeverityColor(s),
                    borderColor: getSeverityColor(s) + "60",
                  } : {}}
                >
                  {s}
                </button>
              ))}
            </div>

            {/* Search */}
            <div className="am-search">
              <i className="ti ti-search" aria-hidden="true" />
              <input
                type="text"
                placeholder="Search disasters…"
                value={search}
                onChange={e => setSearch(e.target.value)}
              />
            </div>
          </div>
        </div>

        {/* Body: panel + map */}
        <div className="am-body">

          {/* Left panel */}
          <div className="am-panel">
            {fetching && <p className="am-panel-empty">Loading disasters…</p>}
            {!fetching && filtered.length === 0 && <p className="am-panel-empty">No disasters found.</p>}

            {/* Selected detail card */}
            {!fetching && selected && (
              <div className="am-detail-card">
                <div className="am-detail-top">
                  <h2 className="am-detail-title">{selected.title}</h2>
                  <span
                    className="am-severity-badge"
                    style={{ background: getSeverityColor(selected.severity_level) }}
                  >
                    {selected.severity_level}
                  </span>
                </div>

                <div className="am-detail-row">
                  <span className={`am-status-chip am-status-${selected.status?.toLowerCase()}`}>
                    {selected.status}
                  </span>
                  {/* Admin always sees edit + delete */}
                  <div className="am-detail-actions">
                    <button className="am-action-btn am-edit-btn" onClick={() => { setEditError(""); setShowEdit(true); }}>
                      <i className="ti ti-pencil" aria-hidden="true" /> Edit
                    </button>
                    <button
                      className="am-action-btn am-delete-btn"
                      onClick={() => setConfirmDelete(true)}
                      disabled={deleting}
                    >
                      <i className="ti ti-trash" aria-hidden="true" />
                      {deleting ? "…" : "Delete"}
                    </button>
                  </div>
                </div>

                <p className="am-detail-date">{formatDate(selected.created_at)}</p>
                <p className="am-detail-creator">
                  👤 Added by <strong>{selected.creator_username ?? "Unknown"}</strong>
                </p>
                <p className="am-detail-desc">{selected.description}</p>

                <div className="am-detail-meta">
                  <span className="am-meta-label">Coordinates:</span>{" "}
                  {selected.latitude?.toFixed(5)}, {selected.longitude?.toFixed(5)}
                </div>

                {selected.gcash_number && (
                  <div className="am-gcash-row">
                    <i className="ti ti-device-mobile" aria-hidden="true" />
                    <span>{selected.gcash_number}</span>
                  </div>
                )}

                <ProofImages disaster={selected} />
              </div>
            )}

            {/* Disaster list */}
            <div className="am-list">
              {filtered.map(d => (
                <div
                  key={d.id}
                  className={`am-list-item ${selected?.id === d.id ? "am-list-item-selected" : ""}`}
                  onClick={() => setSelected(d)}
                >
                  <div className="am-list-left">
                    <span className="am-list-dot" style={{ background: getSeverityColor(d.severity_level) }} />
                    <div>
                      <p className="am-list-title">{d.title}</p>
                      <p className="am-list-meta">
                        {d.creator_username ?? "Unknown"} · {formatDate(d.created_at)}
                      </p>
                    </div>
                  </div>
                  <span className="am-list-sev" style={{ color: getSeverityColor(d.severity_level) }}>
                    {d.severity_level}
                  </span>
                </div>
              ))}
            </div>
          </div>

          {/* Map */}
          <div className="am-map-container">
            <MapContainer
              center={DEFAULT_CENTER}
              zoom={DEFAULT_ZOOM}
              style={{ height: "100%", width: "100%" }}
              zoomControl={false}
            >
              <TileLayer
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
              />
              <FlyTo position={flyPosition} />

              {filtered.map(d => d.latitude && d.longitude ? (
                <Marker
                  key={d.id}
                  position={[d.latitude, d.longitude]}
                  icon={createMarkerIcon(d.severity_level)}
                  eventHandlers={{ click: () => setSelected(d) }}
                >
                  <Popup className="osm-popup">
                    <div className="popup-inner">
                      <p className="popup-title">{d.title}</p>
                      <p className="popup-date">{formatDate(d.created_at)}</p>
                      <p className="popup-creator">👤 {d.creator_username ?? "Unknown"}</p>
                      <span className="popup-severity" style={{ color: getSeverityColor(d.severity_level) }}>
                        {d.severity_level} ●
                      </span>
                    </div>
                  </Popup>
                </Marker>
              ) : null)}
            </MapContainer>

            {/* Edit form floats over map */}
            {showEdit && selected && (
              <EditForm
                disaster={selected}
                onSave={handleEditSave}
                onCancel={() => setShowEdit(false)}
                saving={editSaving}
                error={editError}
              />
            )}
          </div>
        </div>
      </div>
    </div>
  );
}