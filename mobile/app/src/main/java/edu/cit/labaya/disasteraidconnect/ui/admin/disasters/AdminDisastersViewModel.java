package edu.cit.labaya.disasteraidconnect.ui.admin.disasters;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
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

public class AdminDisastersViewModel extends ViewModel {

    public final MutableLiveData<Resource<List<Disaster>>> disasters = new MutableLiveData<>();

    private static final String SUPABASE_REST =
            "https://wvwvxwkbjnvsrvnmrxsr.supabase.co/rest/v1/";
    private static final String ANON_KEY =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                    "eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Ind2d3Z4d2tiam52c3J2bm1yeHNyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzI3OTc3NjgsImV4cCI6MjA4ODM3Mzc2OH0." +
                    "HOpufPceHZLuH-Lxoa1RoP1oZXmw9CA_rOBXFKqECpg";

    public void loadDisasters() {
        disasters.setValue(Resource.loading());
        new Thread(() -> {
            try {
                String token = SessionManager.getInstance().getToken();
                OkHttpClient client = new OkHttpClient();
                Request req = new Request.Builder()
                        .url(SUPABASE_REST + "disasters?select=*,users(username)&order=created_at.desc")
                        .header("apikey", ANON_KEY)
                        .header("Authorization", "Bearer " + token)
                        .build();
                Response resp = client.newCall(req).execute();
                if (resp.isSuccessful() && resp.body() != null) {
                    JSONArray arr = new JSONArray(resp.body().string());
                    List<Disaster> list = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject o = arr.getJSONObject(i);
                        // Inside loadDisasters() — replace the mapping block with this:
                        Disaster d = new Disaster();
                        d.setId(o.optString("id"));
                        d.setTitle(o.optString("title"));
                        d.setDescription(o.optString("description"));
                        d.setSeverity(o.optString("severity"));      // Supabase column name
                        d.setStatus(o.optString("status"));
                        d.setLatitude(o.optDouble("latitude", 0));
                        d.setLongitude(o.optDouble("longitude", 0));
                        d.setCreatedAt(o.optString("created_at"));
// Reporter username from join: disasters?select=*,users(username)
                        if (!o.isNull("users")) {
                            JSONObject u = o.getJSONObject("users");
                            d.setReportedBy(u.optString("username"));
                        }
                        list.add(d);
                    }
                    disasters.postValue(Resource.success(list));
                } else {
                    disasters.postValue(Resource.error("Failed to load"));
                }
            } catch (Exception e) {
                disasters.postValue(Resource.error(e.getMessage()));
            }
        }).start();
    }

    public interface DeleteCallback { void onResult(boolean success); }

    public void deleteDisaster(String id, DeleteCallback cb) {
        new Thread(() -> {
            try {
                String token = SessionManager.getInstance().getToken();
                OkHttpClient client = new OkHttpClient();
                Request req = new Request.Builder()
                        .url(SUPABASE_REST + "disasters?id=eq." + id)
                        .header("apikey", ANON_KEY)
                        .header("Authorization", "Bearer " + token)
                        .delete()
                        .build();
                Response resp = client.newCall(req).execute();
                cb.onResult(resp.isSuccessful());
            } catch (Exception e) {
                cb.onResult(false);
            }
        }).start();
    }
}