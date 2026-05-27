package edu.cit.labaya.disasteraidconnect.data.repository;

import androidx.lifecycle.MutableLiveData;
import java.util.ArrayList;
import java.util.List;
import edu.cit.labaya.disasteraidconnect.core.session.SessionManager;
import edu.cit.labaya.disasteraidconnect.data.model.Disaster;
import edu.cit.labaya.disasteraidconnect.utils.Resource;
import org.json.JSONArray;
import org.json.JSONObject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DisasterRepository {

    private static final String SUPABASE_REST =
            "https://wvwvxwkbjnvsrvnmrxsr.supabase.co/rest/v1/";
    private static final String ANON_KEY =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                    "eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Ind2d3Z4d2tiam52c3J2bm1yeHNyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzI3OTc3NjgsImV4cCI6MjA4ODM3Mzc2OH0." +
                    "HOpufPceHZLuH-Lxoa1RoP1oZXmw9CA_rOBXFKqECpg";

    public interface ListCallback  { void onSuccess(List<Disaster> list); }
    public interface ErrorCallback { void onError(String message); }
    public interface DoneCallback  { void onDone(); }

    // ── Called by DashboardViewModel & DisasterMapViewModel ──────────────────

    /**
     * Fetch all disasters joined with users(username) for reporter name.
     * Mirrors: supabase.from("disasters").select("*,users(username)").order("created_at", {ascending:false})
     */
    public void getAllDisasters(String token, ListCallback onSuccess, ErrorCallback onError) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                Request req = new Request.Builder()
                        .url(SUPABASE_REST +
                                "disasters?select=*,users(username)&order=created_at.desc")
                        .header("apikey", ANON_KEY)
                        .header("Authorization", "Bearer " + token)
                        .build();
                Response resp = client.newCall(req).execute();
                if (resp.isSuccessful() && resp.body() != null) {
                    JSONArray arr = new JSONArray(resp.body().string());
                    List<Disaster> list = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        list.add(parseDisaster(arr.getJSONObject(i)));
                    }
                    onSuccess.onSuccess(list);
                } else {
                    String errBody = "";
                    try { errBody = resp.body() != null ? resp.body().string() : "null body"; }
                    catch (Exception ignored) {}
                    onError.onError("HTTP " + resp.code() + ": " + errBody);
                }
            } catch (Exception e) {
                onError.onError(e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }).start();
    }

    /**
     * Add a new disaster point.
     * Mirrors: supabase.from("disasters").insert({...})
     * NOTE: your Supabase column is "severity" not "severity_level" based on AdminDisastersViewModel
     */
    public void addDisaster(String title, String description, String severity,
                            String status, String gcashNumber,
                            double lat, double lon, String userId, String token,
                            DoneCallback onDone, ErrorCallback onError) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                JSONObject body = new JSONObject();
                body.put("title", title);
                body.put("description", description);
                body.put("severity_level", severity);
                body.put("status", status);
                body.put("gcash_number", gcashNumber);
                body.put("latitude", lat);
                body.put("longitude", lon);
                body.put("created_by", userId);

                Request req = new Request.Builder()
                        .url(SUPABASE_REST + "disasters")
                        .header("apikey", ANON_KEY)
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .header("Prefer", "return=minimal")
                        .post(RequestBody.create(body.toString(),
                                MediaType.parse("application/json")))
                        .build();
                Response resp = client.newCall(req).execute();
                if (resp.isSuccessful()) onDone.onDone();
                else onError.onError("Failed to add disaster: " +
                        (resp.body() != null ? resp.body().string() : ""));
            } catch (Exception e) {
                onError.onError(e.getMessage());
            }
        }).start();
    }

    /**
     * Update an existing disaster point.
     */
    public void updateDisaster(String id, String title, String description,
                               String severity, String status, String gcashNumber,
                               String token, DoneCallback onDone, ErrorCallback onError) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                JSONObject body = new JSONObject();
                body.put("title", title);
                body.put("description", description);
                body.put("severity_level", severity);
                body.put("status", status);
                body.put("gcash_number", gcashNumber);

                Request req = new Request.Builder()
                        .url(SUPABASE_REST + "disasters?id=eq." + id)
                        .header("apikey", ANON_KEY)
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .header("Prefer", "return=minimal")
                        .patch(RequestBody.create(body.toString(),
                                MediaType.parse("application/json")))
                        .build();
                Response resp = client.newCall(req).execute();
                if (resp.isSuccessful()) onDone.onDone();
                else onError.onError("Failed to update");
            } catch (Exception e) {
                onError.onError(e.getMessage());
            }
        }).start();
    }

    /**
     * Delete a disaster point by id.
     */
    public void deleteDisaster(String id, String token,
                               DoneCallback onDone, ErrorCallback onError) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                Request req = new Request.Builder()
                        .url(SUPABASE_REST + "disasters?id=eq." + id)
                        .header("apikey", ANON_KEY)
                        .header("Authorization", "Bearer " + token)
                        .delete()
                        .build();
                Response resp = client.newCall(req).execute();
                if (resp.isSuccessful()) onDone.onDone();
                else onError.onError("Failed to delete");
            } catch (Exception e) {
                onError.onError(e.getMessage());
            }
        }).start();
    }

    // ── Legacy Retrofit stub (keeps AdminDisastersViewModel compile) ──────────

    public void getAll(MutableLiveData<Resource<List<Disaster>>> result) {
        result.setValue(Resource.loading());
        String token = SessionManager.getInstance().getToken();
        getAllDisasters(token,
                list -> result.postValue(Resource.success(list)),
                err  -> result.postValue(Resource.error(err)));
    }

    // ── Shared parser ─────────────────────────────────────────────────────────

    private Disaster parseDisaster(JSONObject o) throws Exception {
        Disaster d = new Disaster();
        d.setId(o.optString("id"));
        d.setTitle(o.optString("title"));
        d.setDescription(o.optString("description"));
        // Column is "severity" in your DB (based on AdminDisastersViewModel usage)
        d.setSeverity(o.optString("severity_level"));
        d.setStatus(o.optString("status"));
        d.setLatitude(o.optDouble("latitude", 0));
        d.setLongitude(o.optDouble("longitude", 0));
        d.setCreatedBy(o.optString("created_by"));
        d.setUserId(o.optString("created_by"));
        d.setCreatedAt(o.optString("created_at"));
        d.setGcashNumber(o.optString("gcash_number"));
        d.setImageUrl1(o.optString("image_url_1"));
        d.setImageUrl2(o.optString("image_url_2"));
        d.setImageUrl3(o.optString("image_url_3"));
        if (!o.isNull("users")) {
            String uname = o.getJSONObject("users").optString("username");
            d.setCreatorUsername(uname);
            d.setReportedBy(uname);
        }
        return d;
    }
}