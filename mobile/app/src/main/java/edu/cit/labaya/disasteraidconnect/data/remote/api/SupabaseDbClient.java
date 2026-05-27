package edu.cit.labaya.disasteraidconnect.data.remote.api;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Direct Supabase PostgREST calls — mirrors the supabase.from() calls in the React code.
 */
public class SupabaseDbClient {

    private static final String SUPABASE_REST_URL =
        "https://wvwvxwkbjnvsrvnmrxsr.supabase.co/rest/v1/";

    private static final String SUPABASE_ANON_KEY =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
        "eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Ind2d3Z4d2tiam52c3J2bm1yeHNyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzI3OTc3NjgsImV4cCI6MjA4ODM3Mzc2OH0." +
        "HOpufPceHZLuH-Lxoa1RoP1oZXmw9CA_rOBXFKqECpg";

    private static final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public interface SuccessCallback { void onSuccess(); }
    public interface RoleCallback { void onRole(String role); }
    public interface ErrorCallback { void onError(String message); }

    /**
     * Mirrors Login.js: supabase.from("users").select("role").eq("id", userId)
     */
    public static void getUserRole(String userId, String token,
                                    RoleCallback onSuccess, ErrorCallback onError) {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                    .url(SUPABASE_REST_URL + "users?select=role&id=eq." + userId)
                    .header("apikey", SUPABASE_ANON_KEY)
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .get()
                    .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String body = response.body().string();
                    JSONArray arr = new JSONArray(body);
                    if (arr.length() > 0) {
                        String role = arr.getJSONObject(0).optString("role", "user");
                        onSuccess.onRole(role);
                    } else {
                        onSuccess.onRole("user");
                    }
                } else {
                    onError.onError("Failed to fetch role");
                }
            } catch (Exception e) {
                onError.onError(e.getMessage());
            }
        }).start();
    }

    /**
     * Mirrors Register.js: supabase.from("users").insert([{id, username, email, security_question, security_answer, role}])
     */
    public static void insertUser(String id, String username, String email,
                                   String securityQuestion, String securityAnswer,
                                   String token, SuccessCallback onSuccess, ErrorCallback onError) {
        new Thread(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("id", id);
                body.put("username", username);
                body.put("email", email);
                body.put("security_question", securityQuestion);
                body.put("security_answer", securityAnswer);
                body.put("role", "user");

                RequestBody requestBody = RequestBody.create(body.toString(), JSON);

                Request request = new Request.Builder()
                    .url(SUPABASE_REST_URL + "users")
                    .header("apikey", SUPABASE_ANON_KEY)
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .header("Prefer", "return=minimal")
                    .post(requestBody)
                    .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    onSuccess.onSuccess();
                } else {
                    String err = response.body() != null ? response.body().string() : "Unknown error";
                    onError.onError(err);
                }
            } catch (Exception e) {
                onError.onError(e.getMessage());
            }
        }).start();
    }

    /**
     * Mirrors useMuteStatus.js: supabase.from("users").select("is_muted, mute_until, mute_reason").eq("id", uid)
     */
    public static void getMuteStatus(String userId, String token,
                                      MuteCallback onSuccess, ErrorCallback onError) {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                    .url(SUPABASE_REST_URL + "users?select=is_muted,mute_until,mute_reason&id=eq." + userId)
                    .header("apikey", SUPABASE_ANON_KEY)
                    .header("Authorization", "Bearer " + token)
                    .get()
                    .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String body = response.body().string();
                    JSONArray arr = new JSONArray(body);
                    if (arr.length() > 0) {
                        JSONObject obj = arr.getJSONObject(0);
                        boolean isMuted = obj.optBoolean("is_muted", false);
                        String muteUntil = obj.optString("mute_until", null);
                        String muteReason = obj.optString("mute_reason", null);
                        onSuccess.onMuteStatus(isMuted, muteUntil, muteReason);
                    } else {
                        onSuccess.onMuteStatus(false, null, null);
                    }
                } else {
                    onError.onError("Failed to fetch mute status");
                }
            } catch (Exception e) {
                onError.onError(e.getMessage());
            }
        }).start();
    }

    public interface MuteCallback {
        void onMuteStatus(boolean isMuted, String muteUntil, String muteReason);
    }
}
