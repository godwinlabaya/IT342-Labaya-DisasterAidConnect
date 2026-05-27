package edu.cit.labaya.disasteraidconnect.ui.dashboard;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import edu.cit.labaya.disasteraidconnect.core.session.SessionManager;
import edu.cit.labaya.disasteraidconnect.data.model.Disaster;
import edu.cit.labaya.disasteraidconnect.data.model.Donation;
import edu.cit.labaya.disasteraidconnect.data.model.Notification;
import org.json.JSONArray;
import org.json.JSONObject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DashboardViewModel extends ViewModel {

    private static final String SUPABASE_REST =
            "https://wvwvxwkbjnvsrvnmrxsr.supabase.co/rest/v1/";
    private static final String ANON_KEY =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                    "eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Ind2d3Z4d2tiam52c3J2bm1yeHNyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzI3OTc3NjgsImV4cCI6MjA4ODM3Mzc2OH0." +
                    "HOpufPceHZLuH-Lxoa1RoP1oZXmw9CA_rOBXFKqECpg";

    public final MutableLiveData<List<Disaster>>     disasters     = new MutableLiveData<>();
    public final MutableLiveData<List<Donation>>     donations     = new MutableLiveData<>();
    public final MutableLiveData<List<Notification>> notifications = new MutableLiveData<>();
    public final MutableLiveData<Integer>            unreadCount   = new MutableLiveData<>(0);
    public final MutableLiveData<Boolean>            isLoading     = new MutableLiveData<>(false);
    public final MutableLiveData<String>             error         = new MutableLiveData<>();

    public void loadDashboard() {
        isLoading.setValue(true);
        String userId = SessionManager.getInstance().getUserId();
        String token  = SessionManager.getInstance().getToken();
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                loadDisasters(client, token);
                loadDonations(client, userId, token);
                loadNotifications(client, userId, token);
                isLoading.postValue(false);
            } catch (Exception e) {
                error.postValue(e.getMessage());
                isLoading.postValue(false);
            }
        }).start();
    }

    private void loadDisasters(OkHttpClient client, String token) throws Exception {
        // Join with users to get reporter username
        Request req = new Request.Builder()
                .url(SUPABASE_REST + "disasters?select=*,users(username)&order=created_at.desc")
                .header("apikey", ANON_KEY)
                .header("Authorization", "Bearer " + token)
                .build();
        Response resp = client.newCall(req).execute();
        if (!resp.isSuccessful() || resp.body() == null) return;
        JSONArray arr = new JSONArray(resp.body().string());
        List<Disaster> list = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            Disaster d = new Disaster();
            d.setId(o.optString("id"));
            d.setTitle(o.optString("title"));
            d.setDescription(o.optString("description"));
            d.setSeverity(o.optString("severity_level")); // actual column name
            d.setStatus(o.optString("status"));
            d.setLatitude(o.optDouble("latitude", 0));
            d.setLongitude(o.optDouble("longitude", 0));
            d.setCreatedBy(o.optString("created_by"));
            d.setUserId(o.optString("created_by")); // map created_by → userId
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
            list.add(d);
        }
        disasters.postValue(list);
    }

    private void loadDonations(OkHttpClient client, String userId, String token) throws Exception {
        // User's own donations joined with disasters for title
        Request req = new Request.Builder()
                .url(SUPABASE_REST + "donations?select=*,disasters(title)&user_id=eq."
                        + userId + "&order=donated_at.desc")
                .header("apikey", ANON_KEY)
                .header("Authorization", "Bearer " + token)
                .build();
        Response resp = client.newCall(req).execute();
        if (!resp.isSuccessful() || resp.body() == null) return;
        JSONArray arr = new JSONArray(resp.body().string());
        List<Donation> list = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            Donation don = new Donation();
            don.setId(o.optString("id"));
            don.setUserId(o.optString("user_id"));
            don.setDisasterId(o.optString("disaster_id"));
            don.setAmount(o.optDouble("amount", 0));
            don.setStatus(o.optString("status"));
            don.setDonatedAt(o.optString("donated_at"));
            if (!o.isNull("disasters")) {
                don.setDisasterTitle(o.getJSONObject("disasters").optString("title"));
            }
            list.add(don);
        }
        donations.postValue(list);
    }

    private void loadNotifications(OkHttpClient client, String userId, String token) throws Exception {
        Request req = new Request.Builder()
                .url(SUPABASE_REST + "notifications?user_id=eq." + userId
                        + "&order=created_at.desc&limit=20")
                .header("apikey", ANON_KEY)
                .header("Authorization", "Bearer " + token)
                .build();
        Response resp = client.newCall(req).execute();
        if (!resp.isSuccessful() || resp.body() == null) return;
        JSONArray arr = new JSONArray(resp.body().string());
        List<Notification> list = new ArrayList<>();
        int unread = 0;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            Notification n = new Notification();
            n.setId(o.optString("id"));
            n.setUserId(o.optString("user_id"));
            n.setType(o.optString("type"));
            n.setTitle(o.optString("title"));
            n.setMessage(o.optString("message"));
            n.setRead(o.optBoolean("is_read", false));
            n.setCreatedAt(o.optString("created_at"));
            if (!n.isRead()) unread++;
            list.add(n);
        }
        notifications.postValue(list);
        unreadCount.postValue(unread);
    }

    public void markAllRead(String userId, String token) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                JSONObject body = new JSONObject();
                body.put("is_read", true);
                Request req = new Request.Builder()
                        .url(SUPABASE_REST + "notifications?user_id=eq." + userId
                                + "&is_read=eq.false")
                        .header("apikey", ANON_KEY)
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .patch(RequestBody.create(body.toString(),
                                MediaType.parse("application/json")))
                        .build();
                client.newCall(req).execute();
                unreadCount.postValue(0);
            } catch (Exception ignored) {}
        }).start();
    }
}