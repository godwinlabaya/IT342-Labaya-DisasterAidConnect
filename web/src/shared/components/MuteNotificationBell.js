// web/src/shared/components/MuteNotificationBell.js
import { useState } from "react";
import { useMuteStatus } from "../../features/auth/useMuteStatus";
import "./MuteNotificationBell.css";

function formatDateTime(iso) {
  if (!iso) return null;
  return new Date(iso).toLocaleString("en-US", {
    month: "short", day: "numeric", year: "numeric",
    hour: "numeric", minute: "2-digit", hour12: true,
  });
}

export default function MuteNotificationBell({ uid }) {
  const { isMuted, muteUntil, muteReason } = useMuteStatus(uid);
  const [open, setOpen] = useState(false);

  return (
    <div className="mnb-wrap">
      {/* Bell — mirrors your existing .notification-icon style */}
      <button
        className="notification-icon mnb-bell"
        aria-label="Notifications"
        onClick={() => setOpen(o => !o)}
      >
        <i className="ti ti-bell" aria-hidden="true" />
        {isMuted && <span className="notif-dot mnb-dot" />}
      </button>

      {open && (
        <>
          <div className="mnb-backdrop" onClick={() => setOpen(false)} />

          <div className="mnb-panel">
            <div className="mnb-panel-header">
              <span className="mnb-panel-title">Notifications</span>
              <button className="mnb-close" onClick={() => setOpen(false)} aria-label="Close">
                <i className="ti ti-x" aria-hidden="true" />
              </button>
            </div>

            {isMuted ? (
              <div className="mnb-item mnb-item--mute">
                <div className="mnb-item-icon">
                  <i className="ti ti-ban" aria-hidden="true" />
                </div>
                <div className="mnb-item-body">
                  <p className="mnb-item-title">You have been muted by an admin</p>

                  {muteReason && (
                    <p className="mnb-item-reason">
                      <i className="ti ti-message-circle" aria-hidden="true" />
                      {muteReason}
                    </p>
                  )}

                  <p className="mnb-item-meta">
                    {muteUntil
                      ? <><span>Restriction lifts on </span><strong>{formatDateTime(muteUntil)}</strong></>
                      : <><span>Restriction is </span><strong>indefinite</strong><span> — contact an admin</span></>
                    }
                  </p>

                  <div className="mnb-item-notice">
                    <i className="ti ti-alert-triangle" aria-hidden="true" />
                    You cannot add new disaster points until this mute is lifted.
                  </div>
                </div>
              </div>
            ) : (
              <div className="mnb-empty">
                <i className="ti ti-bell-off" aria-hidden="true" />
                <p>No new notifications</p>
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
}