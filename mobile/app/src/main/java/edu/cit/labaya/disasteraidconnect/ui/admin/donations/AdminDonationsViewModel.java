package edu.cit.labaya.disasteraidconnect.ui.admin.donations;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import edu.cit.labaya.disasteraidconnect.core.session.SessionManager;
import edu.cit.labaya.disasteraidconnect.data.model.Donation;
import edu.cit.labaya.disasteraidconnect.utils.Resource;
import org.json.JSONArray;
import org.json.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AdminDonationsViewModel extends ViewModel {

    public final MutableLiveData<Resource<List<Donation>>> donations = new MutableLiveData<>();

    private static final String SUPABASE_REST =
            "https://wvwvxwkbjnvsrvnmrxsr.supabase.co/rest/v1/";
    private static final String ANON_KEY =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                    "eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Ind2d3Z4d2tiam52c3J2bm1yeHNyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzI3OTc3NjgsImV4cCI6MjA4ODM3Mzc2OH0." +
                    "HOpufPceHZLuH-Lxoa1RoP1oZXmw9CA_rOBXFKqECpg";

    public void loadAllDonations() {
        donations.setValue(Resource.loading());
        new Thread(() -> {
            try {
                String token = SessionManager.getInstance().getToken();
                OkHttpClient client = new OkHttpClient();
                // Join with users and disasters for donor name + disaster title
                Request req = new Request.Builder()
                        .url(SUPABASE_REST + "donations?select=*,users(username),disasters(title)&order=donated_at.desc")
                        .header("apikey", ANON_KEY)
                        .header("Authorization", "Bearer " + token)
                        .build();
                Response resp = client.newCall(req).execute();
                if (resp.isSuccessful() && resp.body() != null) {
                    JSONArray arr = new JSONArray(resp.body().string());
                    List<Donation> list = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject o = arr.getJSONObject(i);
                        // Inside loadAllDonations() — replace the mapping block with this:
                        Donation don = new Donation();
                        don.setId(o.optString("id"));
                        don.setAmount(o.optDouble("amount", 0));
                        don.setStatus(o.optString("status"));
                        don.setDonatedAt(o.optString("donated_at")); // Supabase column
                        don.setDisasterId(o.optString("disaster_id")); // Supabase column
                        if (!o.isNull("users")) {
                            don.setDonorName(o.getJSONObject("users").optString("username"));
                        }
                        if (!o.isNull("disasters")) {
                            don.setDisasterTitle(o.getJSONObject("disasters").optString("title"));
                        }
                        list.add(don);
                    }
                    donations.postValue(Resource.success(list));
                } else {
                    donations.postValue(Resource.error("Failed to load donations"));
                }
            } catch (Exception e) {
                donations.postValue(Resource.error(e.getMessage()));
            }
        }).start();
    }
}