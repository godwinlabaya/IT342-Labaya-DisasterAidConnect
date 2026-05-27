package edu.cit.labaya.disasteraidconnect.data.repository;

/**
 * MuteStatus — returned by UserRepository.getMuteStatus()
 *
 * Mirrors the useMuteStatus.js hook response:
 *   { isMuted, muteUntil, muteReason }
 *
 * Added getMuteUntil() so DisasterMapViewModel can pass it to the
 * bell notification panel (layout_mute_bell_panel.xml → tvMuteUntil).
 */
public class MuteStatus {

    private final boolean isMuted;
    private final String  muteUntil;
    private final String  reason;

    public MuteStatus(boolean isMuted, String muteUntil, String reason) {
        this.isMuted   = isMuted;
        this.muteUntil = muteUntil;
        this.reason    = reason;
    }

    /** Whether the user is currently muted (and mute has not expired). */
    public boolean isMuted() { return isMuted; }

    /**
     * ISO datetime string when the mute expires, or null for indefinite.
     * Matches Supabase mute_until column.
     */
    public String getMuteUntil() { return muteUntil; }

    /** Admin-provided reason shown in the bell notification panel. */
    public String getReason() { return reason; }
}