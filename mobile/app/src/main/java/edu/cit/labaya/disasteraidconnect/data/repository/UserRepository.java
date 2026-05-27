package edu.cit.labaya.disasteraidconnect.data.repository;

import edu.cit.labaya.disasteraidconnect.data.remote.api.SupabaseDbClient;

public class UserRepository {

    public interface MuteStatusCallback { void onResult(MuteStatus status); }
    public interface ErrorCallback      { void onError(String message); }

    public void getMuteStatus(String userId, String token,
                              MuteStatusCallback onSuccess,
                              ErrorCallback onError) {
        SupabaseDbClient.getMuteStatus(userId, token,
                (isMuted, muteUntil, muteReason) -> {
                    boolean expired = isMuted && muteUntil != null
                            && isExpired(muteUntil);
                    onSuccess.onResult(new MuteStatus(
                            isMuted && !expired,
                            muteUntil,
                            muteReason
                    ));
                },
                onError::onError
        );
    }

    private boolean isExpired(String iso) {
        try {
            java.text.SimpleDateFormat sdf =
                    new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",
                            java.util.Locale.getDefault());
            return sdf.parse(iso).before(new java.util.Date());
        } catch (Exception e) { return false; }
    }

    public static class MuteStatus {
        private final boolean isMuted;
        private final String  muteUntil;
        private final String  reason;

        public MuteStatus(boolean isMuted, String muteUntil, String reason) {
            this.isMuted   = isMuted;
            this.muteUntil = muteUntil;
            this.reason    = reason;
        }

        public boolean isMuted()      { return isMuted; }
        public String  getMuteUntil() { return muteUntil; }
        public String  getReason()    { return reason; }
    }
}