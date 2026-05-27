package edu.cit.labaya.disasteraidconnect.ui.admin.users;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import edu.cit.labaya.disasteraidconnect.core.session.SessionManager;
import edu.cit.labaya.disasteraidconnect.data.model.User;
import edu.cit.labaya.disasteraidconnect.utils.Resource;
import org.json.JSONArray;
import org.json.JSONObject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class AdminUsersViewModel extends ViewModel {

    public final MutableLiveData<Resource<List<User>>> users = new MutableLiveData<>();

    private static final String SUPABASE_REST =
            "https://wvwvxwkbjnvsrvnmrxsr.supabase.co/rest/v1/";
    private static final String ANON_KEY =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                    "eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Ind2d3Z4d2tiam52c3J2bm1yeHNyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzI3OTc3NjgsImV4cCI6MjA4ODM3Mzc2OH0." +
                    "HOpufPceHZLuH-Lxoa1RoP1oZXmw9CA_rOBXFKqECpg";

    public interface ActionCallback { void onResult(boolean success); }

    public void loadAllUsers() {
        users.setValue(Resource.loading());
        new Thread(() -> {
            try {
                String token = SessionManager.getInstance().getToken();
                OkHttpClient client = new OkHttpClient();
                Request req = new Request.Builder()
                        .url(SUPABASE_REST + "users?select=*&order=created_at.desc")
                        .header("apikey", ANON_KEY)
                        .header("Authorization", "Bearer " + token)
                        .build();
                Response resp = client.newCall(req).execute();
                if (resp.isSuccessful() && resp.body() != null) {
                    JSONArray arr = new JSONArray(resp.body().string());
                    List<User> list = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject o = arr.getJSONObject(i);
                        User u = new User();
                        u.setId(o.optString("id"));
                        u.setUsername(o.optString("username"));
                        u.setEmail(o.optString("email"));
                        u.setRole(o.optString("role", "user"));
                        u.setMuted(o.optBoolean("is_muted", false));
                        u.setMuteUntil(o.isNull("mute_until") ? null : o.optString("mute_until"));
                        u.setMuteReason(o.isNull("mute_reason") ? null : o.optString("mute_reason"));
                        u.setCreatedAt(o.optString("created_at"));
                        list.add(u);
                    }
                    users.postValue(Resource.success(list));
                } else {
                    users.postValue(Resource.error("Failed to load users"));
                }
            } catch (Exception e) {
                users.postValue(Resource.error(e.getMessage()));
            }
        }).start();
    }

    public void muteUser(String userId, String reason, int durationDays, ActionCallback cb) {
        new Thread(() -> {
            try {
                String token = SessionManager.getInstance().getToken();
                OkHttpClient client = new OkHttpClient();
                String muteUntil = Instant.now().plus(durationDays, ChronoUnit.DAYS).toString();
                JSONObject body = new JSONObject();
                body.put("is_muted", true);
                body.put("mute_reason", reason);
                body.put("mute_until", muteUntil);
                Request req = new Request.Builder()
                        .url(SUPABASE_REST + "users?id=eq." + userId)
                        .header("apikey", ANON_KEY)
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .patch(RequestBody.create(body.toString(),
                                MediaType.parse("application/json")))
                        .build();
                Response resp = client.newCall(req).execute();
                cb.onResult(resp.isSuccessful());
            } catch (Exception e) { cb.onResult(false); }
        }).start();
    }

    public void unmuteUser(String userId, ActionCallback cb) {
        new Thread(() -> {
            try {
                String token = SessionManager.getInstance().getToken();
                OkHttpClient client = new OkHttpClient();
                JSONObject body = new JSONObject();
                body.put("is_muted", false);
                body.put("mute_reason", JSONObject.NULL);
                body.put("mute_until", JSONObject.NULL);
                Request req = new Request.Builder()
                        .url(SUPABASE_REST + "users?id=eq." + userId)
                        .header("apikey", ANON_KEY)
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .patch(RequestBody.create(body.toString(),
                                MediaType.parse("application/json")))
                        .build();
                Response resp = client.newCall(req).execute();
                cb.onResult(resp.isSuccessful());
            } catch (Exception e) { cb.onResult(false); }
        }).start();
    }

    public void deleteUser(String userId, ActionCallback cb) {
        new Thread(() -> {
            try {
                String token = SessionManager.getInstance().getToken();
                OkHttpClient client = new OkHttpClient();
                Request req = new Request.Builder()
                        .url(SUPABASE_REST + "users?id=eq." + userId)
                        .header("apikey", ANON_KEY)
                        .header("Authorization", "Bearer " + token)
                        .delete()
                        .build();
                Response resp = client.newCall(req).execute();
                cb.onResult(resp.isSuccessful());
            } catch (Exception e) { cb.onResult(false); }
        }).start();
    }
}