package edu.cit.labaya.disasteraidconnect.data.repository;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import edu.cit.labaya.disasteraidconnect.core.session.SessionManager;
import edu.cit.labaya.disasteraidconnect.data.model.Donation;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Handles all donation-related Supabase REST calls.
 *
 * Mirrors the web donations flow:
 *   supabase.from("donations").insert({user_id, disaster_id, amount, status: "Completed"})
 *
 * Status is saved as "Completed" immediately (Option A — deep-link + record).
 * This ensures donations appear in:
 *   - User's DonationActivity (mobile)
 *   - Admin's AdminDonationsActivity (mobile)
 *   - Web donations page (reads same Supabase table)
 */
public class DonationRepository {

    private static final String SUPABASE_REST =
            "https://wvwvxwkbjnvsrvnmrxsr.supabase.co/rest/v1/";
    private static final String ANON_KEY =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                    "eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Ind2d3Z4d2tiam52c3J2bm1yeHNyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzI3OTc3NjgsImV4cCI6MjA4ODM3Mzc2OH0." +
                    "HOpufPceHZLuH-Lxoa1RoP1oZXmw9CA_rOBXFKqECpg";

    private static final OkHttpClient client = new OkHttpClient();

    public interface DoneCallback  { void onDone(String donationId); }
    public interface ErrorCallback { void onError(String message); }
    public interface ListCallback  { void onSuccess(List<Donation> list); }

    /**
     * Record a donation as "Completed" in Supabase.
     *
     * Called right before opening GCash deep-link so the record exists
     * regardless of what happens in the GCash app.
     *
     * Maps to:
     *   donations table: user_id, disaster_id, amount, status = "Completed"
     */
    public void recordDonation(String disasterId, double amount,
                               DoneCallback onDone, ErrorCallback onError) {
        String userId = SessionManager.getInstance().getUserId();
        String token  = SessionManager.getInstance().getToken();

        new Thread(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("user_id",     userId);
                body.put("disaster_id", disasterId);
                body.put("amount",      amount);
                body.put("status",      "Completed");

                Request req = new Request.Builder()
                        .url(SUPABASE_REST + "donations")
                        .header("apikey",        ANON_KEY)
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type",  "application/json")
                        .header("Prefer",        "return=representation")
                        .post(RequestBody.create(body.toString(),
                                MediaType.parse("application/json")))
                        .build();

                Response resp = client.newCall(req).execute();
                if (resp.isSuccessful() && resp.body() != null) {
                    String raw = resp.body().string();
                    // Parse returned id from array response
                    String donationId = null;
                    try {
                        JSONArray arr = new JSONArray(raw);
                        if (arr.length() > 0) {
                            donationId = arr.getJSONObject(0).optString("id");
                        }
                    } catch (Exception ignored) {}
                    final String finalId = donationId;
                    onDone.onDone(finalId);
                } else {
                    String errBody = resp.body() != null ? resp.body().string() : "Unknown error";
                    onError.onError("Failed to record donation: " + errBody);
                }
            } catch (Exception e) {
                onError.onError(e.getMessage());
            }
        }).start();
    }

    /**
     * Fetch all donations for the current user, joined with disaster title.
     * Mirrors web DonationActivity data fetch.
     */
    public void getDonationsByUser(ListCallback onSuccess, ErrorCallback onError) {
        String userId = SessionManager.getInstance().getUserId();
        String token  = SessionManager.getInstance().getToken();

        new Thread(() -> {
            try {
                Request req = new Request.Builder()
                        .url(SUPABASE_REST +
                                "donations?select=*,disasters(title)&user_id=eq." + userId +
                                "&order=donated_at.desc")
                        .header("apikey",        ANON_KEY)
                        .header("Authorization", "Bearer " + token)
                        .build();

                Response resp = client.newCall(req).execute();
                if (resp.isSuccessful() && resp.body() != null) {
                    JSONArray arr = new JSONArray(resp.body().string());
                    List<Donation> list = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        list.add(parseDonation(arr.getJSONObject(i)));
                    }
                    onSuccess.onSuccess(list);
                } else {
                    onError.onError("Failed to load donations");
                }
            } catch (Exception e) {
                onError.onError(e.getMessage());
            }
        }).start();
    }

    /**
     * Fetch all donations (admin view), joined with disaster title and user username.
     */
    public void getAllDonations(ListCallback onSuccess, ErrorCallback onError) {
        String token = SessionManager.getInstance().getToken();

        new Thread(() -> {
            try {
                Request req = new Request.Builder()
                        .url(SUPABASE_REST +
                                "donations?select=*,disasters(title),users(username)&order=donated_at.desc")
                        .header("apikey",        ANON_KEY)
                        .header("Authorization", "Bearer " + token)
                        .build();

                Response resp = client.newCall(req).execute();
                if (resp.isSuccessful() && resp.body() != null) {
                    JSONArray arr = new JSONArray(resp.body().string());
                    List<Donation> list = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        list.add(parseDonation(arr.getJSONObject(i)));
                    }
                    onSuccess.onSuccess(list);
                } else {
                    onError.onError("Failed to load donations");
                }
            } catch (Exception e) {
                onError.onError(e.getMessage());
            }
        }).start();
    }

    // ── Parser ────────────────────────────────────────────────────────────────

    private Donation parseDonation(JSONObject o) throws Exception {
        Donation d = new Donation();
        d.setId(o.optString("id"));
        d.setUserId(o.optString("user_id"));
        d.setDisasterId(o.optString("disaster_id"));
        d.setAmount(o.optDouble("amount", 0));
        d.setStatus(o.optString("status", "Completed"));
        d.setDonatedAt(o.optString("donated_at"));

        if (!o.isNull("disasters")) {
            d.setDisasterTitle(o.getJSONObject("disasters").optString("title"));
        }
        if (!o.isNull("users")) {
            d.setDonorUsername(o.getJSONObject("users").optString("username"));
        }
        return d;
    }
}