import { useEffect, useState, useRef, useCallback } from "react";
import { MapContainer, TileLayer, Marker, Popup, useMap, useMapEvents } from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import Sidebar from "../../components/Sidebar";
import "./Map.css";
import { supabase } from "../../supabaseClient";
import { createMarkerIcon, getSeverityColor } from "../../utils/iconFactory";
import disasterService from "../../services/disasterService";

// Fix broken default marker icons in webpack/CRA
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: require("leaflet/dist/images/marker-icon-2x.png"),
  iconUrl:       require("leaflet/dist/images/marker-icon.png"),
  shadowUrl:     require("leaflet/dist/images/marker-shadow.png"),
});

const DEFAULT_CENTER   = [10.3157, 123.8854];
const DEFAULT_ZOOM     = 13;
const SEVERITY_LEVELS  = ["Low", "Medium", "High", "Critical"];
const STATUS_OPTIONS   = ["Active", "Monitoring", "Resolved"];
const EMPTY_FORM       = { title: "", description: "", severity_level: "Medium", status: "Active" };

function formatDate(iso) {
  if (!iso) return "";
  return new Date(iso).toLocaleString("en-US", {
    month: "long", day: "numeric", year: "numeric",
    hour: "numeric", minute: "2-digit", hour12: true,
  });
}

function FlyTo({ position }) {
  const map = useMap();
  const prev = useRef(null);
  useEffect(() => {
    if (position && JSON.stringify(position) !== JSON.stringify(prev.current)) {
      prev.current = position;
      map.flyTo(position, 15, { duration: 1.2 });
    }
  }, [position, map]);
  return null;
}

function ClickHandler({ addMode, onMapClick }) {
  useMapEvents({ click(e) { if (addMode) onMapClick(e.latlng); } });
  return null;
}

// ── Confirm dialog ────────────────────────────────────────────────────────────
function ConfirmDialog({ message, onConfirm, onCancel }) {
  return (
    <div className="confirm-backdrop">
      <div className="confirm-box">
        <p className="confirm-msg">{message}</p>
        <div className="confirm-actions">
          <button className="form-cancel" onClick={onCancel}>Cancel</button>
          <button className="delete-confirm-btn" onClick={onConfirm}>Yes, Delete</button>
        </div>
      </div>
    </div>
  );
}

// ── Add / Edit form ───────────────────────────────────────────────────────────
function DisasterForm({ title, coords, form, setForm, onSave, onCancel, saving, error }) {
  return (
    <div className="add-form-overlay">
      <div className="add-form">
        <div className="add-form-header">
          <h3>{title}</h3>
          <button className="form-close" onClick={onCancel}>✕</button>
        </div>

        {coords && (
          <div className="add-form-coords">
            📍 {coords.lat.toFixed(5)}, {coords.lng.toFixed(5)}
          </div>
        )}

        <div className="form-field">
          <label>Title <span className="required">*</span></label>
          <input
            type="text"
            placeholder="e.g. Fire Breakout"
            value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })}
          />
        </div>

        <div className="form-field">
          <label>Description <span className="required">*</span></label>
          <textarea
            placeholder="Describe the situation…"
            rows={3}
            value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })}
          />
        </div>

        <div className="form-row">
          <div className="form-field">
            <label>Severity</label>
            <select value={form.severity_level} onChange={(e) => setForm({ ...form, severity_level: e.target.value })}>
              {SEVERITY_LEVELS.map((s) => <option key={s}>{s}</option>)}
            </select>
          </div>
          <div className="form-field">
            <label>Status</label>
            <select value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
              {STATUS_OPTIONS.map((s) => <option key={s}>{s}</option>)}
            </select>
          </div>
        </div>

        {error && <p className="form-error">{error}</p>}

        <div className="form-actions">
          <button className="form-cancel" onClick={onCancel}>Cancel</button>
          <button className="form-save" onClick={onSave} disabled={saving}>
            {saving ? "Saving…" : "Save Point"}
          </button>
        </div>
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────────────────────
export default function MapPage() {
  const [disasters,  setDisasters]  = useState([]);
  const [selected,   setSelected]   = useState(null);
  const [currentUID, setCurrentUID] = useState(null);
  const [search,     setSearch]     = useState("");
  const [loading,    setLoading]    = useState(true);

  // Add flow
  const [addMode,    setAddMode]    = useState(false);
  const [pendingPin, setPendingPin] = useState(null);
  const [showAdd,    setShowAdd]    = useState(false);
  const [addForm,    setAddForm]    = useState(EMPTY_FORM);
  const [addSaving,  setAddSaving]  = useState(false);
  const [addError,   setAddError]   = useState("");

  // Edit flow
  const [showEdit,   setShowEdit]   = useState(false);
  const [editForm,   setEditForm]   = useState(EMPTY_FORM);
  const [editSaving, setEditSaving] = useState(false);
  const [editError,  setEditError]  = useState("");

  // Delete flow
  const [confirmDelete, setConfirmDelete] = useState(false);
  const [deleting,      setDeleting]      = useState(false);

  // ── Auth ────────────────────────────────────────────────────────────────────
  useEffect(() => {
    supabase.auth.getSession().then(({ data: { session } }) => {
      setCurrentUID(session?.user?.id ?? null);
    });
  }, []);

  // ── Fetch ───────────────────────────────────────────────────────────────────
    const fetchDisasters = useCallback(async () => {
    setLoading(true);
    try {
      const data = await disasterService.getAll();
      setDisasters(data);
      if (data.length > 0) setSelected((prev) => prev ?? data[0]);
    } catch (err) {
      console.error("Failed to fetch disasters:", err.message);
    } finally {
      setLoading(false);
    }
  }, []);
  useEffect(() => { fetchDisasters(); }, [fetchDisasters]);

  const isOwner = selected?.created_by === currentUID;

  // ── Add ─────────────────────────────────────────────────────────────────────
  const handleMapClick = useCallback((latlng) => {
    setPendingPin({ lat: latlng.lat, lng: latlng.lng });
    setAddForm(EMPTY_FORM);
    setAddError("");
    setShowAdd(true);
  }, []);

    const handleAdd = async () => {
    if (!addForm.title.trim())       { setAddError("Title is required.");       return; }
    if (!addForm.description.trim()) { setAddError("Description is required."); return; }
    setAddSaving(true); setAddError("");

    try {
      const { data: { session } } = await supabase.auth.getSession();
      const data = await disasterService.create({
        title:          addForm.title.trim(),
        description:    addForm.description.trim(),
        severity_level: addForm.severity_level,
        status:         addForm.status,
        latitude:       pendingPin.lat,
        longitude:      pendingPin.lng,
        created_by:     session?.user?.id ?? null,
      });
      setDisasters((prev) => [data, ...prev]);
      setSelected(data);
      setPendingPin(null);
      setShowAdd(false);
      setAddMode(false);
    } catch (err) {
      setAddError("Failed to save: " + err.message);
    } finally {
      setAddSaving(false);
    }
  };

  const cancelAdd = () => {
    setPendingPin(null); setShowAdd(false);
    setAddMode(false);   setAddError("");
  };

  // ── Edit ────────────────────────────────────────────────────────────────────
  const openEdit = () => {
    setEditForm({
      title:          selected.title,
      description:    selected.description,
      severity_level: selected.severity_level,
      status:         selected.status,
    });
    setEditError("");
    setShowEdit(true);
  };

const handleEdit = async () => {
  if (!editForm.title.trim())       { setEditError("Title is required.");       return; }
  if (!editForm.description.trim()) { setEditError("Description is required."); return; }
  setEditSaving(true); setEditError("");

  try {
    const data = await disasterService.update(selected.id, {
      title:          editForm.title.trim(),
      description:    editForm.description.trim(),
      severity_level: editForm.severity_level,
      status:         editForm.status,
    });
    setDisasters((prev) => prev.map((d) => d.id === data.id ? data : d));
    setSelected(data);
    setShowEdit(false);
  } catch (err) {
    setEditError("Failed to update: " + err.message);
  } finally {
    setEditSaving(false);
  }
};

  // ── Delete ──────────────────────────────────────────────────────────────────
  const handleDelete = async () => {
  setDeleting(true);

  try {
    await disasterService.remove(selected.id);
    const remaining = disasters.filter((d) => d.id !== selected.id);
    setDisasters(remaining);
    setSelected(remaining[0] ?? null);
    setConfirmDelete(false);
  } catch (err) {
    alert("Failed to delete: " + err.message);
  } finally {
    setDeleting(false);
  }
};

  // ── Filtered list ────────────────────────────────────────────────────────────
  const filtered = disasters.filter(
    (d) =>
      d.title?.toLowerCase().includes(search.toLowerCase()) ||
      d.description?.toLowerCase().includes(search.toLowerCase())
  );

  const flyPosition =
    selected?.latitude && selected?.longitude
      ? [selected.latitude, selected.longitude]
      : null;

  return (
    <div className="map-layout">
      <Sidebar />

      <div className="map-page">

        {/* ── TOP BAR ── */}
        <div className="map-topbar">
          <button className="back-btn" onClick={() => window.history.back()}>&#171;</button>

          <div className="map-search">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none"
              stroke="#9ca3af" strokeWidth="2.5" strokeLinecap="round">
              <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
            </svg>
            <input
              type="text"
              placeholder="Search disasters…"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>

          <div className="map-topbar-right">
            <button
              className={`add-point-btn ${addMode ? "add-point-active" : ""}`}
              onClick={() => { setAddMode((v) => !v); if (addMode) cancelAdd(); }}
            >
              {addMode ? (
                <><svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round"><path d="M18 6 6 18M6 6l12 12"/></svg> Cancel</>
              ) : (
                <><svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round"><circle cx="12" cy="12" r="10"/><path d="M12 8v8M8 12h8"/></svg> Add Point</>
              )}
            </button>

            <div className="notif-btn">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#6b7280" strokeWidth="2" strokeLinecap="round">
                <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
                <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
              </svg>
            </div>
            <div className="avatar">US</div>
          </div>
        </div>

        {addMode && !showAdd && (
          <div className="add-banner">
            📍 Click anywhere on the map to place a disaster point
          </div>
        )}

        <div className="map-body">

          {/* ── LEFT PANEL ── */}
          <div className="map-panel">
            {loading && <p className="panel-empty">Loading disasters…</p>}
            {!loading && filtered.length === 0 && (
              <p className="panel-empty">No disasters found.</p>
            )}

            {/* ── Detail card ── */}
            {!loading && selected && (
              <div className="request-detail-card">
                <div className="card-top">
                  <h2 className="card-title">{selected.title}</h2>
                  <span className="severity-badge"
                    style={{ background: getSeverityColor(selected.severity_level) }}>
                    {selected.severity_level} <span className="badge-dot">●</span>
                  </span>
                </div>

                <div className="card-status-row">
                  <span className={`status-chip status-${selected.status?.toLowerCase()}`}>
                    {selected.status}
                  </span>
                  {/* Edit / Delete — only visible to the owner */}
                  {isOwner && (
                    <div className="owner-actions">
                      <button className="action-btn edit-btn" onClick={openEdit} title="Edit">
                        <svg width="13" height="13" viewBox="0 0 24 24" fill="none"
                          stroke="currentColor" strokeWidth="2.2" strokeLinecap="round">
                          <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                          <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                        </svg>
                        Edit
                      </button>
                      <button
                        className="action-btn delete-btn"
                        onClick={() => setConfirmDelete(true)}
                        disabled={deleting}
                        title="Delete"
                      >
                        <svg width="13" height="13" viewBox="0 0 24 24" fill="none"
                          stroke="currentColor" strokeWidth="2.2" strokeLinecap="round">
                          <polyline points="3 6 5 6 21 6"/>
                          <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/>
                          <path d="M10 11v6M14 11v6"/>
                          <path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>
                        </svg>
                        {deleting ? "Deleting…" : "Delete"}
                      </button>
                    </div>
                  )}
                </div>

                <p className="card-date">{formatDate(selected.created_at)}</p>
                <p className="card-description">{selected.description}</p>
                <div className="card-meta">
                  <p>
                    <span className="meta-label">Coordinates:</span>{" "}
                    {selected.latitude?.toFixed(5)}, {selected.longitude?.toFixed(5)}
                  </p>
                </div>

                <button className="donate-btn">DONATE</button>
              </div>
            )}

            {/* Scrollable list */}
            <div className="request-list">
              {filtered.map((d) => (
                <div
                  key={d.id}
                  className={`request-list-item ${selected?.id === d.id ? "rli-selected" : ""}`}
                  onClick={() => setSelected(d)}
                >
                  <div className="rli-left">
                    <span className="rli-dot" style={{ background: getSeverityColor(d.severity_level) }}/>
                    <div>
                      <p className="rli-title">{d.title}</p>
                      <p className="rli-date">{formatDate(d.created_at)}</p>
                    </div>
                  </div>
                  <div className="rli-right">
                    <span className="rli-severity" style={{ color: getSeverityColor(d.severity_level) }}>
                      {d.severity_level}
                    </span>
                    {/* Small owner indicator */}
                    {d.created_by === currentUID && (
                      <span className="rli-mine" title="You created this">✦</span>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* ── MAP ── */}
          <div className={`map-container ${addMode ? "map-crosshair" : ""}`}>
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
              <ClickHandler addMode={addMode} onMapClick={handleMapClick} />

              {filtered.map((d) =>
                d.latitude && d.longitude ? (
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
                        <span className="popup-severity" style={{ color: getSeverityColor(d.severity_level) }}>
                          {d.severity_level} ●
                        </span>
                      </div>
                    </Popup>
                  </Marker>
                ) : null
              )}

              {pendingPin && (
                <Marker
                  position={[pendingPin.lat, pendingPin.lng]}
                  icon={createMarkerIcon("Low", { isTemp: true })}
                />
              )}
            </MapContainer>

            {/* Add form */}
            {showAdd && (
              <DisasterForm
                title="New Disaster Point"
                coords={pendingPin}
                form={addForm}
                setForm={setAddForm}
                onSave={handleAdd}
                onCancel={cancelAdd}
                saving={addSaving}
                error={addError}
              />
            )}

            {/* Edit form */}
            {showEdit && (
              <DisasterForm
                title="Edit Disaster Point"
                coords={null}
                form={editForm}
                setForm={setEditForm}
                onSave={handleEdit}
                onCancel={() => setShowEdit(false)}
                saving={editSaving}
                error={editError}
              />
            )}

            {/* Delete confirm */}
            {confirmDelete && (
              <ConfirmDialog
                message={`Delete "${selected?.title}"? This cannot be undone.`}
                onConfirm={handleDelete}
                onCancel={() => setConfirmDelete(false)}
              />
            )}
          </div>
        </div>
      </div>
    </div>
  );
}