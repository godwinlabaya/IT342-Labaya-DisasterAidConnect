import { useEffect, useState, useRef, useCallback } from "react";
import { MapContainer, TileLayer, Marker, Popup, useMap, useMapEvents } from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import Sidebar from "../../shared/components/Sidebar";
import "./Map.css";
import { supabase } from "../../supabaseClient";
import { createMarkerIcon, getSeverityColor } from "./iconFactory";
import disasterService from "./disasterService";
import donationService from "../donations/DonationService";
import { useAuth } from "../auth/useAuth";

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
const EMPTY_FORM      = { title: "", description: "", severity_level: "Medium", status: "Active" };
const MAX_IMAGES      = 3;

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

// ── Donation modal ────────────────────────────────────────────────────────────
function DonationModal({ disaster, currentUID, onClose }) {
  const [amount,   setAmount]   = useState("");
  const [loading,  setLoading]  = useState(false);
  const [error,    setError]    = useState("");

  const handleDonate = async () => {
    const parsed = parseFloat(amount);
    if (!amount || isNaN(parsed) || parsed <= 0) {
      setError("Please enter a valid amount greater than ₱0.");
      return;
    }
    if (parsed < 20) {
      setError("Minimum donation amount is ₱20.");
      return;
    }

    setLoading(true);
    setError("");

    try {
      const { checkoutUrl } = await donationService.createCheckout({
        userId:     currentUID,
        disasterId: disaster.id,
        amount:     parsed,
      });
      // Redirect to GCash checkout in same tab
      window.location.href = checkoutUrl;
    } catch (err) {
      setError("Payment failed: " + err.message);
      setLoading(false);
    }
  };

  return (
    <div className="confirm-backdrop" onClick={onClose}>
      <div className="confirm-box donation-modal" onClick={(e) => e.stopPropagation()}>
        <div className="donation-modal-header">
          <div className="donation-modal-icon">💙</div>
          <h3>Donate to this disaster</h3>
          <p className="donation-modal-sub">"{disaster.title}"</p>
        </div>

        <div className="donation-amount-section">
          <label className="donation-label">Enter amount (PHP)</label>
          <div className="donation-input-wrap">
            <span className="donation-currency">₱</span>
            <input
              type="number"
              className="donation-input"
              placeholder="e.g. 500"
              value={amount}
              min="20"
              onChange={(e) => setAmount(e.target.value)}
              autoFocus
            />
          </div>

          {/* Quick amount buttons */}
          <div className="donation-quick-btns">
            {[100, 250, 500, 1000].map((val) => (
              <button
                key={val}
                className={`donation-quick-btn ${amount === String(val) ? "donation-quick-active" : ""}`}
                onClick={() => setAmount(String(val))}
              >
                ₱{val.toLocaleString()}
              </button>
            ))}
          </div>
        </div>

        {error && <p className="donation-error">{error}</p>}

        <div className="donation-info">
          <span>💳</span>
          <span>You'll be redirected to GCash to complete payment</span>
        </div>

        <div className="confirm-actions">
          <button className="form-cancel" onClick={onClose} disabled={loading}>
            Cancel
          </button>
          <button
            className="donate-submit-btn"
            onClick={handleDonate}
            disabled={loading || !amount}
          >
            {loading ? "Redirecting…" : `Donate ${amount ? "₱" + parseFloat(amount).toLocaleString() : ""}`}
          </button>
        </div>
      </div>
    </div>
  );
}

// ── Image uploader ────────────────────────────────────────────────────────────
function ImageUploader({ images, setImages }) {
  const inputRef = useRef(null);

  const handleFiles = (e) => {
    const files = Array.from(e.target.files);
    const remaining = MAX_IMAGES - images.length;
    const toAdd = files.slice(0, remaining).map((file) => ({
      file,
      preview: URL.createObjectURL(file),
    }));
    setImages((prev) => [...prev, ...toAdd]);
    e.target.value = "";
  };

  const removeImage = (idx) => {
    setImages((prev) => {
      URL.revokeObjectURL(prev[idx].preview);
      return prev.filter((_, i) => i !== idx);
    });
  };

  return (
    <div className="img-uploader">
      <label className="form-field-label">
        Proof Images
        <span className="img-count-badge">{images.length}/{MAX_IMAGES}</span>
      </label>
      <div className="img-preview-row">
        {images.map((img, idx) => (
          <div key={idx} className="img-thumb-wrap">
            <img src={img.preview} alt={`proof-${idx}`} className="img-thumb" />
            <button type="button" className="img-remove-btn" onClick={() => removeImage(idx)}>✕</button>
          </div>
        ))}
        {images.length < MAX_IMAGES && (
          <button type="button" className="img-add-slot" onClick={() => inputRef.current?.click()}>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <rect x="3" y="3" width="18" height="18" rx="3"/>
              <path d="M12 8v8M8 12h8"/>
            </svg>
            <span>Add photo</span>
          </button>
        )}
      </div>
      <input ref={inputRef} type="file" accept="image/*" multiple style={{ display: "none" }} onChange={handleFiles} />
      <p className="img-hint">Up to {MAX_IMAGES} images as proof of the disaster</p>
    </div>
  );
}

async function uploadImages(imageObjs) {
  const urls = [];
  for (const { file } of imageObjs) {
    const ext      = file.name.split(".").pop();
    const filename = `${Date.now()}-${Math.random().toString(36).slice(2)}.${ext}`;
    const { error } = await supabase.storage
      .from("disaster-images")
      .upload(filename, file, { cacheControl: "3600", upsert: false });
    if (error) throw new Error("Image upload failed: " + error.message);
    const { data } = supabase.storage.from("disaster-images").getPublicUrl(filename);
    urls.push(data.publicUrl);
  }
  return urls;
}

// ── Add / Edit form ───────────────────────────────────────────────────────────
function DisasterForm({ title, coords, form, setForm, images, setImages, onSave, onCancel, saving, error, showImageUpload = true }) {
  return (
    <div className="add-form-overlay">
      <div className="add-form">
        <div className="add-form-header">
          <h3>{title}</h3>
          <button className="form-close" onClick={onCancel}>✕</button>
        </div>
        {coords && <div className="add-form-coords">📍 {coords.lat.toFixed(5)}, {coords.lng.toFixed(5)}</div>}
        <div className="form-field">
          <label>Title <span className="required">*</span></label>
          <input type="text" placeholder="e.g. Fire Breakout" value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} />
        </div>
        <div className="form-field">
          <label>Description <span className="required">*</span></label>
          <textarea placeholder="Describe the situation…" rows={3} value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
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
        {showImageUpload && <ImageUploader images={images} setImages={setImages} />}
        {error && <p className="form-error">{error}</p>}
        <div className="form-actions">
          <button className="form-cancel" onClick={onCancel}>Cancel</button>
          <button className="form-save" onClick={onSave} disabled={saving}>{saving ? "Saving…" : "Save Point"}</button>
        </div>
      </div>
    </div>
  );
}

// ── Proof images viewer ───────────────────────────────────────────────────────
function ProofImages({ disaster }) {
  const urls = [disaster.image_url_1, disaster.image_url_2, disaster.image_url_3].filter(Boolean);
  const [lightbox, setLightbox] = useState(null);
  if (urls.length === 0) return null;
  return (
    <>
      <div className="proof-images-section">
        <p className="proof-images-label">📸 Proof Images</p>
        <div className="proof-images-row">
          {urls.map((url, i) => (
            <img key={i} src={url} alt={`proof-${i + 1}`} className="proof-thumb" onClick={() => setLightbox(url)} title="Click to enlarge" />
          ))}
        </div>
      </div>
      {lightbox && (
        <div className="lightbox-backdrop" onClick={() => setLightbox(null)}>
          <div className="lightbox-box" onClick={(e) => e.stopPropagation()}>
            <button className="lightbox-close" onClick={() => setLightbox(null)}>✕</button>
            <img src={lightbox} alt="proof-full" className="lightbox-img" />
          </div>
        </div>
      )}
    </>
  );
}

// ─────────────────────────────────────────────────────────────────────────────
export default function MapPage() {
  const [disasters,     setDisasters]     = useState([]);
  const [selected,      setSelected]      = useState(null);
  const [currentUID,    setCurrentUID]    = useState(null);
  const [search,        setSearch]        = useState("");
  const [loading,       setLoading]       = useState(true);
  const [showDonation,  setShowDonation]  = useState(false);  // ← new
  const { username } = useAuth({});

  const [addMode,    setAddMode]    = useState(false);
  const [pendingPin, setPendingPin] = useState(null);
  const [showAdd,    setShowAdd]    = useState(false);
  const [addForm,    setAddForm]    = useState(EMPTY_FORM);
  const [addImages,  setAddImages]  = useState([]);
  const [addSaving,  setAddSaving]  = useState(false);
  const [addError,   setAddError]   = useState("");

  const [showEdit,   setShowEdit]   = useState(false);
  const [editForm,   setEditForm]   = useState(EMPTY_FORM);
  const [editSaving, setEditSaving] = useState(false);
  const [editError,  setEditError]  = useState("");

  const [confirmDelete, setConfirmDelete] = useState(false);
  const [deleting,      setDeleting]      = useState(false);

  useEffect(() => {
    supabase.auth.getSession().then(({ data: { session } }) => {
      setCurrentUID(session?.user?.id ?? null);
    });
  }, []);

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

  const handleMapClick = useCallback((latlng) => {
    setPendingPin({ lat: latlng.lat, lng: latlng.lng });
    setAddForm(EMPTY_FORM);
    setAddImages([]);
    setAddError("");
    setShowAdd(true);
  }, []);

  const handleAdd = async () => {
    if (!addForm.title.trim())       { setAddError("Title is required.");       return; }
    if (!addForm.description.trim()) { setAddError("Description is required."); return; }
    setAddSaving(true); setAddError("");
    try {
      const { data: { session } } = await supabase.auth.getSession();
      let imageUrls = [];
      if (addImages.length > 0) imageUrls = await uploadImages(addImages);
      const data = await disasterService.create({
        title: addForm.title.trim(), description: addForm.description.trim(),
        severity_level: addForm.severity_level, status: addForm.status,
        latitude: pendingPin.lat, longitude: pendingPin.lng,
        created_by: session?.user?.id ?? null,
        image_url_1: imageUrls[0] ?? null,
        image_url_2: imageUrls[1] ?? null,
        image_url_3: imageUrls[2] ?? null,
      });
      setDisasters((prev) => [data, ...prev]);
      setSelected(data);
      setPendingPin(null); setShowAdd(false); setAddMode(false); setAddImages([]);
    } catch (err) {
      setAddError("Failed to save: " + err.message);
    } finally {
      setAddSaving(false);
    }
  };

  const cancelAdd = () => {
    setPendingPin(null); setShowAdd(false);
    setAddMode(false); setAddError(""); setAddImages([]);
  };

  const openEdit = () => {
    setEditForm({ title: selected.title, description: selected.description, severity_level: selected.severity_level, status: selected.status });
    setEditError(""); setShowEdit(true);
  };

  const handleEdit = async () => {
    if (!editForm.title.trim())       { setEditError("Title is required.");       return; }
    if (!editForm.description.trim()) { setEditError("Description is required."); return; }
    setEditSaving(true); setEditError("");
    try {
      const data = await disasterService.update(selected.id, {
        title: editForm.title.trim(), description: editForm.description.trim(),
        severity_level: editForm.severity_level, status: editForm.status,
      });
      setDisasters((prev) => prev.map((d) => d.id === data.id ? data : d));
      setSelected(data); setShowEdit(false);
    } catch (err) {
      setEditError("Failed to update: " + err.message);
    } finally {
      setEditSaving(false);
    }
  };

  const handleDelete = async () => {
    setDeleting(true);
    try {
      await disasterService.remove(selected.id);
      const remaining = disasters.filter((d) => d.id !== selected.id);
      setDisasters(remaining); setSelected(remaining[0] ?? null); setConfirmDelete(false);
    } catch (err) {
      alert("Failed to delete: " + err.message);
    } finally {
      setDeleting(false);
    }
  };

  const filtered = disasters.filter((d) =>
    d.title?.toLowerCase().includes(search.toLowerCase()) ||
    d.description?.toLowerCase().includes(search.toLowerCase())
  );

  const flyPosition = selected?.latitude && selected?.longitude
    ? [selected.latitude, selected.longitude] : null;

  return (
    <div className="map-layout">
      <Sidebar />
      <div className="map-page">

        {/* ── TOP BAR ── */}
        <div className="map-topbar">
          <button className="back-btn" onClick={() => window.history.back()}>&#171;</button>
          <div className="map-search">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" strokeWidth="2.5" strokeLinecap="round">
              <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
            </svg>
            <input type="text" placeholder="Search disasters…" value={search} onChange={(e) => setSearch(e.target.value)} />
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
            <div className="avatar">{username ? username.slice(0, 2).toUpperCase() : "US"}</div>
          </div>
        </div>

        {addMode && !showAdd && (
          <div className="add-banner">📍 Click anywhere on the map to place a disaster point</div>
        )}

        <div className="map-body">
          {/* ── LEFT PANEL ── */}
          <div className="map-panel">
            {loading && <p className="panel-empty">Loading disasters…</p>}
            {!loading && filtered.length === 0 && <p className="panel-empty">No disasters found.</p>}

            {!loading && selected && (
              <div className="request-detail-card">
                <div className="card-top">
                  <h2 className="card-title">{selected.title}</h2>
                  <span className="severity-badge" style={{ background: getSeverityColor(selected.severity_level) }}>
                    {selected.severity_level} <span className="badge-dot">●</span>
                  </span>
                </div>

                <div className="card-status-row">
                  <span className={`status-chip status-${selected.status?.toLowerCase()}`}>{selected.status}</span>
                  {isOwner && (
                    <div className="owner-actions">
                      <button className="action-btn edit-btn" onClick={openEdit}>
                        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round">
                          <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                          <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                        </svg>
                        Edit
                      </button>
                      <button className="action-btn delete-btn" onClick={() => setConfirmDelete(true)} disabled={deleting}>
                        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round">
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
                  <p><span className="meta-label">Coordinates:</span> {selected.latitude?.toFixed(5)}, {selected.longitude?.toFixed(5)}</p>
                </div>

                <ProofImages disaster={selected} />

                {/* ── DONATE BUTTON — now opens modal ── */}
                <button className="donate-btn" onClick={() => setShowDonation(true)}>
                  💙 DONATE
                </button>
              </div>
            )}

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
                    <span className="rli-severity" style={{ color: getSeverityColor(d.severity_level) }}>{d.severity_level}</span>
                    {d.created_by === currentUID && <span className="rli-mine" title="You created this">✦</span>}
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* ── MAP ── */}
          <div className={`map-container ${addMode ? "map-crosshair" : ""}`}>
            <MapContainer center={DEFAULT_CENTER} zoom={DEFAULT_ZOOM} style={{ height: "100%", width: "100%" }} zoomControl={false}>
              <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" attribution='&copy; OpenStreetMap contributors' />
              <FlyTo position={flyPosition} />
              <ClickHandler addMode={addMode} onMapClick={handleMapClick} />
              {filtered.map((d) => d.latitude && d.longitude ? (
                <Marker key={d.id} position={[d.latitude, d.longitude]} icon={createMarkerIcon(d.severity_level)} eventHandlers={{ click: () => setSelected(d) }}>
                  <Popup className="osm-popup">
                    <div className="popup-inner">
                      <p className="popup-title">{d.title}</p>
                      <p className="popup-date">{formatDate(d.created_at)}</p>
                      <span className="popup-severity" style={{ color: getSeverityColor(d.severity_level) }}>{d.severity_level} ●</span>
                    </div>
                  </Popup>
                </Marker>
              ) : null)}
              {pendingPin && <Marker position={[pendingPin.lat, pendingPin.lng]} icon={createMarkerIcon("Low", { isTemp: true })} />}
            </MapContainer>

            {showAdd && (
              <DisasterForm title="New Disaster Point" coords={pendingPin} form={addForm} setForm={setAddForm}
                images={addImages} setImages={setAddImages} onSave={handleAdd} onCancel={cancelAdd}
                saving={addSaving} error={addError} showImageUpload={true} />
            )}
            {showEdit && (
              <DisasterForm title="Edit Disaster Point" coords={null} form={editForm} setForm={setEditForm}
                images={[]} setImages={() => {}} onSave={handleEdit} onCancel={() => setShowEdit(false)}
                saving={editSaving} error={editError} showImageUpload={false} />
            )}
            {confirmDelete && (
              <ConfirmDialog message={`Delete "${selected?.title}"? This cannot be undone.`}
                onConfirm={handleDelete} onCancel={() => setConfirmDelete(false)} />
            )}
          </div>
        </div>
      </div>

      {/* ── Donation modal — rendered outside map-body so it covers full screen ── */}
      {showDonation && selected && (
        <DonationModal
          disaster={selected}
          currentUID={currentUID}
          onClose={() => setShowDonation(false)}
        />
      )}
    </div>
  );
}