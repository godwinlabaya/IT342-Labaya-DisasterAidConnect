package edu.cit.labaya.disasteraidconnect.ui.disaster;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import edu.cit.labaya.disasteraidconnect.core.session.SessionManager;
import edu.cit.labaya.disasteraidconnect.data.model.Disaster;
import edu.cit.labaya.disasteraidconnect.data.repository.DisasterRepository;
import edu.cit.labaya.disasteraidconnect.data.repository.UserRepository;

/**
 * DisasterMapViewModel
 *
 * Exposes:
 *   disasters       — list of all disasters for map + list
 *   isMuted         — whether the current user is muted
 *   muteReason      — reason text for mute (shown in bell panel)
 *   muteUntil       — ISO datetime when mute expires (shown in bell panel)
 *   operationResult — one-shot toast messages
 *   isLoading       — loading indicator
 *
 * Matches useMuteStatus.js behaviour: polls on load + refresh.
 */
public class DisasterMapViewModel extends ViewModel {

    private final DisasterRepository disasterRepo = new DisasterRepository();
    private final UserRepository     userRepo     = new UserRepository();

    public final MutableLiveData<List<Disaster>> disasters       = new MutableLiveData<>();
    public final MutableLiveData<Boolean>        isMuted         = new MutableLiveData<>(false);
    public final MutableLiveData<String>         muteReason      = new MutableLiveData<>();
    public final MutableLiveData<String>         muteUntil       = new MutableLiveData<>();
    public final MutableLiveData<String>         operationResult = new MutableLiveData<>();
    public final MutableLiveData<Boolean>        isLoading       = new MutableLiveData<>(false);

    // ── Disasters ─────────────────────────────────────────────────────────────

    public void loadDisasters() {
        String token = SessionManager.getInstance().getToken();
        isLoading.setValue(true);
        disasterRepo.getAllDisasters(token,
                list -> {
                    disasters.postValue(list);
                    isLoading.postValue(false);
                },
                err -> {
                    operationResult.postValue("Failed to load disasters: " + err);
                    isLoading.postValue(false);
                });
    }

    // ── Mute Status ───────────────────────────────────────────────────────────

    /**
     * Mirrors useMuteStatus.js:
     *   supabase.from("users").select("is_muted, mute_until, mute_reason").eq("id", uid)
     *
     * Also auto-clears expired mutes (same as web hook).
     */
    public void checkMuteStatus() {
        String userId = SessionManager.getInstance().getUserId();
        String token  = SessionManager.getInstance().getToken();
        if (userId == null || userId.isEmpty()) return;

        userRepo.getMuteStatus(userId, token,
                status -> {
                    isMuted.postValue(status.isMuted());
                    muteReason.postValue(status.getReason());
                    muteUntil.postValue(status.getMuteUntil());
                },
                err -> isMuted.postValue(false));
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    public void addDisaster(String title, String desc, String severity, String status,
                            String gcash, double lat, double lon, Runnable onDone) {
        String userId = SessionManager.getInstance().getUserId();
        String token  = SessionManager.getInstance().getToken();
        disasterRepo.addDisaster(title, desc, severity, status, gcash, lat, lon,
                userId, token,
                () -> {
                    operationResult.postValue("Disaster point added!");
                    if (onDone != null) onDone.run();
                },
                err -> {
                    operationResult.postValue("Failed: " + err);
                    if (onDone != null) onDone.run();
                });
    }

    public void updateDisaster(String id, String title, String desc, String severity,
                               String status, String gcash, Runnable onDone) {
        String token = SessionManager.getInstance().getToken();
        disasterRepo.updateDisaster(id, title, desc, severity, status, gcash, token,
                () -> {
                    operationResult.postValue("Disaster updated!");
                    if (onDone != null) onDone.run();
                },
                err -> {
                    operationResult.postValue("Failed: " + err);
                    if (onDone != null) onDone.run();
                });
    }

    public void deleteDisaster(String id) {
        String token = SessionManager.getInstance().getToken();
        disasterRepo.deleteDisaster(id, token,
                () -> operationResult.postValue("Disaster deleted."),
                err -> operationResult.postValue("Delete failed: " + err));
    }
}