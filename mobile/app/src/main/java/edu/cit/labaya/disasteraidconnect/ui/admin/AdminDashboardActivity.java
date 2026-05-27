package edu.cit.labaya.disasteraidconnect.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import org.json.JSONArray;
import org.json.JSONObject;
import edu.cit.labaya.disasteraidconnect.R;
import edu.cit.labaya.disasteraidconnect.core.session.SessionManager;
import edu.cit.labaya.disasteraidconnect.ui.admin.disasters.AdminDisastersActivity;
import edu.cit.labaya.disasteraidconnect.ui.admin.donations.AdminDonationsActivity;
import edu.cit.labaya.disasteraidconnect.ui.admin.map.AdminDisastersMapActivity;
import edu.cit.labaya.disasteraidconnect.ui.admin.users.AdminUsersActivity;
import edu.cit.labaya.disasteraidconnect.ui.auth.login.LoginActivity;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AdminDashboardActivity extends AppCompatActivity {

    private static final String SUPABASE_REST =
            "https://wvwvxwkbjnvsrvnmrxsr.supabase.co/rest/v1/";
    private static final String ANON_KEY =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                    "eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Ind2d3Z4d2tiam52c3J2bm1yeHNyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzI3OTc3NjgsImV4cCI6MjA4ODM3Mzc2OH0." +
                    "HOpufPceHZLuH-Lxoa1RoP1oZXmw9CA_rOBXFKqECpg";

    private TextView tvTotalDisasters, tvMapPoints, tvTotalDonations, tvRegisteredUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        tvTotalDisasters  = findViewById(R.id.tvTotalDisasters);
        tvMapPoints       = findViewById(R.id.tvMapPoints);
        tvTotalDonations  = findViewById(R.id.tvTotalDonations);
        tvRegisteredUsers = findViewById(R.id.tvRegisteredUsers);

        // Nav cards
        LinearLayout cardDisasters = findViewById(R.id.cardDisasters);
        LinearLayout cardMap       = findViewById(R.id.cardMap);
        LinearLayout cardDonations = findViewById(R.id.cardDonations);
        LinearLayout cardUsers     = findViewById(R.id.cardUsers);

        cardDisasters.setOnClickListener(v ->
                startActivity(new Intent(this, AdminDisastersActivity.class)));
        cardMap.setOnClickListener(v ->
                startActivity(new Intent(this, AdminDisastersMapActivity.class))); // reuse disasters map tab
        cardDonations.setOnClickListener(v ->
                startActivity(new Intent(this, AdminDonationsActivity.class)));
        cardUsers.setOnClickListener(v ->
                startActivity(new Intent(this, AdminUsersActivity.class)));

        // Logout
        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            SessionManager.getInstance().clearSession();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // Bottom nav
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_dashboard);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_disasters) {
                startActivity(new Intent(this, AdminDisastersActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_donations) {
                startActivity(new Intent(this, AdminDonationsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_users) {
                startActivity(new Intent(this, AdminUsersActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return true;
        });

        loadStats();
    }

    private void loadStats() {
        new Thread(() -> {
            try {
                String token = SessionManager.getInstance().getToken();
                OkHttpClient client = new OkHttpClient();

                // disasters count
                int disasters = countFromTable(client, token, "disasters");
                // donations count
                int donations = countFromTable(client, token, "donations");
                // users count
                int users = countFromTable(client, token, "users");

                runOnUiThread(() -> {
                    tvTotalDisasters.setText(String.valueOf(disasters));
                    tvMapPoints.setText(String.valueOf(disasters)); // map points = disasters
                    tvTotalDonations.setText(String.valueOf(donations));
                    tvRegisteredUsers.setText(String.valueOf(users));
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    tvTotalDisasters.setText("—");
                    tvMapPoints.setText("—");
                    tvTotalDonations.setText("—");
                    tvRegisteredUsers.setText("—");
                });
            }
        }).start();
    }

    private int countFromTable(OkHttpClient client, String token, String table) {
        try {
            Request req = new Request.Builder()
                    .url(SUPABASE_REST + table + "?select=id")
                    .header("apikey", ANON_KEY)
                    .header("Authorization", "Bearer " + token)
                    .header("Prefer", "count=exact")
                    .build();
            Response resp = client.newCall(req).execute();
            if (resp.isSuccessful() && resp.body() != null) {
                // Parse count from Content-Range header e.g. "0-21/22"
                String contentRange = resp.header("Content-Range");
                if (contentRange != null && contentRange.contains("/")) {
                    return Integer.parseInt(contentRange.split("/")[1].trim());
                }
                // fallback: count array
                String body = resp.body().string();
                return new JSONArray(body).length();
            }
        } catch (Exception ignored) {}
        return 0;
    }
}