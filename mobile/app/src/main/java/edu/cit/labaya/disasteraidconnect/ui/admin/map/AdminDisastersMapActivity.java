package edu.cit.labaya.disasteraidconnect.ui.admin.map;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import java.util.ArrayList;
import java.util.List;
import edu.cit.labaya.disasteraidconnect.R;
import edu.cit.labaya.disasteraidconnect.core.session.SessionManager;
import edu.cit.labaya.disasteraidconnect.data.model.Disaster;
import edu.cit.labaya.disasteraidconnect.ui.admin.AdminDashboardActivity;
import edu.cit.labaya.disasteraidconnect.ui.admin.disasters.AdminDisastersActivity;
import edu.cit.labaya.disasteraidconnect.ui.admin.donations.AdminDonationsActivity;
import edu.cit.labaya.disasteraidconnect.ui.admin.users.AdminUsersActivity;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AdminDisastersMapActivity extends AppCompatActivity {

    private static final String SUPABASE_REST =
            "https://wvwvxwkbjnvsrvnmrxsr.supabase.co/rest/v1/";
    private static final String ANON_KEY =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                    "eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Ind2d3Z4d2tiam52c3J2bm1yeHNyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzI3OTc3NjgsImV4cCI6MjA4ODM3Mzc2OH0." +
                    "HOpufPceHZLuH-Lxoa1RoP1oZXmw9CA_rOBXFKqECpg";

    private MapView mapView;
    private List<Disaster> allDisasters = new ArrayList<>();
    private List<Disaster> filteredDisasters = new ArrayList<>();
    private String activeFilter = "All";
    private String searchQuery = "";
    private LinearLayout disasterListPanel;
    private TextView tvPointCount;

    // If launched from disasters list with a specific disaster to highlight
    private String targetDisasterId;
    private double targetLat, targetLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_admin_map);

        mapView         = findViewById(R.id.mapView);
        tvPointCount    = findViewById(R.id.tvPointCount);
        disasterListPanel = findViewById(R.id.disasterListPanel);

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(13.0);
        // Default center: Cebu City
        mapView.getController().setCenter(new GeoPoint(10.3157, 123.8854));

        // Check if launched from Disasters list to jump to a specific point
        targetDisasterId = getIntent().getStringExtra("disasterId");
        targetLat        = getIntent().getDoubleExtra("lat", 0);
        targetLng        = getIntent().getDoubleExtra("lng", 0);

        setupFilterChips();
        setupSearch();
        setupBottomNav();
        loadDisasters();
    }

    private void setupFilterChips() {
        ChipGroup chipGroup = findViewById(R.id.chipGroupFilter);
        String[] filters = {"All", "Critical", "High", "Medium", "Low"};
        for (String f : filters) {
            Chip chip = new Chip(this);
            chip.setText(f);
            chip.setCheckable(true);
            chip.setChecked(f.equals("All"));
            chip.setChipBackgroundColorResource(R.color.chip_bg_selector);
            chip.setTextColor(getResources().getColorStateList(R.color.chip_text_selector, null));
            chip.setOnClickListener(v -> {
                activeFilter = f;
                // Uncheck others
                for (int i = 0; i < chipGroup.getChildCount(); i++) {
                    ((Chip) chipGroup.getChildAt(i)).setChecked(false);
                }
                chip.setChecked(true);
                applyFilter();
            });
            chipGroup.addView(chip);
        }
    }

    private void setupSearch() {
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { searchQuery = q; applyFilter(); return true; }
            @Override public boolean onQueryTextChange(String q) { searchQuery = q; applyFilter(); return true; }
        });
    }

    private void loadDisasters() {
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
                    allDisasters.clear();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject o = arr.getJSONObject(i);
                        Disaster d = new Disaster();
                        d.setId(o.optString("id"));
                        d.setTitle(o.optString("title"));
                        d.setDescription(o.optString("description"));
                        d.setSeverity(o.optString("severity"));
                        d.setStatus(o.optString("status"));
                        d.setLatitude(o.optDouble("latitude", 0));
                        d.setLongitude(o.optDouble("longitude", 0));
                        d.setCreatedAt(o.optString("created_at"));
                        d.setGcashNumber(o.optString("gcash_number"));
                        if (!o.isNull("users")) {
                            d.setReportedBy(o.getJSONObject("users").optString("username"));
                        }
                        allDisasters.add(d);
                    }
                    runOnUiThread(() -> {
                        tvPointCount.setText(allDisasters.size() + " points");
                        applyFilter();
                        // If launched to view specific disaster, jump to it
                        if (targetDisasterId != null && !targetDisasterId.isEmpty()) {
                            for (Disaster d : allDisasters) {
                                if (d.getId().equals(targetDisasterId)) {
                                    jumpToDisaster(d);
                                    break;
                                }
                            }
                        }
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Failed to load map data", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void applyFilter() {
        filteredDisasters.clear();
        for (Disaster d : allDisasters) {
            boolean matchFilter = activeFilter.equals("All") ||
                    (d.getSeverity() != null && d.getSeverity().equalsIgnoreCase(activeFilter));
            boolean matchSearch = searchQuery.isEmpty() ||
                    (d.getTitle() != null && d.getTitle().toLowerCase().contains(searchQuery.toLowerCase()));
            if (matchFilter && matchSearch) filteredDisasters.add(d);
        }
        renderMapMarkers();
        renderDisasterList();
    }

    private void renderMapMarkers() {
        mapView.getOverlays().clear();
        for (Disaster d : filteredDisasters) {
            if (d.getLatitude() == 0 && d.getLongitude() == 0) continue;
            Marker marker = new Marker(mapView);
            marker.setPosition(new GeoPoint(d.getLatitude(), d.getLongitude()));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(d.getTitle());
            marker.setSnippet(d.getSeverity());
            marker.setOnMarkerClickListener((m, mv) -> {
                InfoWindow.closeAllInfoWindowsOn(mapView);
                showDisasterDetailPanel(d);
                return true;
            });
            mapView.getOverlays().add(marker);
        }
        mapView.invalidate();
    }

    private void renderDisasterList() {
        disasterListPanel.removeAllViews();
        for (Disaster d : filteredDisasters) {
            View item = LayoutInflater.from(this)
                    .inflate(R.layout.item_map_disaster_row, disasterListPanel, false);

            TextView tvTitle    = item.findViewById(R.id.tvTitle);
            TextView tvReporter = item.findViewById(R.id.tvReporter);
            TextView tvSeverity = item.findViewById(R.id.tvSeverity);
            TextView tvDot      = item.findViewById(R.id.tvDot);

            tvTitle.setText(d.getTitle());
            String reporter = d.getReportedBy() != null ? d.getReportedBy() : "Unknown";
            String date = d.getCreatedAt() != null && d.getCreatedAt().length() >= 10
                    ? d.getCreatedAt().substring(0, 10) : "";
            tvReporter.setText(reporter + " · " + date);
            tvSeverity.setText(d.getSeverity());

            // Dot color by severity
            int dotColor = d.getSeverityColor();
            tvDot.setTextColor(dotColor);

            item.setOnClickListener(v -> {
                jumpToDisaster(d);
                showDisasterDetailPanel(d);
            });

            disasterListPanel.addView(item);
        }
    }

    private void jumpToDisaster(Disaster d) {
        mapView.getController().animateTo(new GeoPoint(d.getLatitude(), d.getLongitude()));
        mapView.getController().setZoom(16.0);
        showDisasterDetailPanel(d);
    }

    private void showDisasterDetailPanel(Disaster d) {
        View panel = findViewById(R.id.detailPanel);
        panel.setVisibility(View.VISIBLE);

        TextView tvTitle       = panel.findViewById(R.id.detailTitle);
        TextView tvSeverityBadge = panel.findViewById(R.id.detailSeverityBadge);
        TextView tvStatus      = panel.findViewById(R.id.detailStatus);
        TextView tvReporter    = panel.findViewById(R.id.detailReporter);
        TextView tvDescription = panel.findViewById(R.id.detailDescription);
        TextView tvCoords      = panel.findViewById(R.id.detailCoords);
        TextView tvGcash       = panel.findViewById(R.id.detailGcash);
        TextView tvDate        = panel.findViewById(R.id.detailDate);
        MaterialButton btnEdit   = panel.findViewById(R.id.btnEditDisaster);
        MaterialButton btnDelete = panel.findViewById(R.id.btnDeleteDisaster);

        tvTitle.setText(d.getTitle());
        tvSeverityBadge.setText(d.getSeverity() != null ? d.getSeverity().toUpperCase() : "—");
        tvStatus.setText(d.getStatus() != null ? d.getStatus() : "—");
        tvReporter.setText("Added by " + (d.getReportedBy() != null ? d.getReportedBy() : "Unknown"));
        tvDescription.setText(d.getDescription() != null ? d.getDescription() : "");
        tvCoords.setText(String.format("Coordinates: %.5f, %.5f", d.getLatitude(), d.getLongitude()));
        tvGcash.setText(d.getGcashNumber() != null ? d.getGcashNumber() : "—");
        tvDate.setText(d.getCreatedAt() != null && d.getCreatedAt().length() >= 16
                ? d.getCreatedAt().substring(0, 16).replace("T", " at ") : "");

        btnEdit.setOnClickListener(v -> showEditDialog(d));
        btnDelete.setOnClickListener(v -> confirmDeleteFromMap(d, panel));
    }

    private void showEditDialog(Disaster d) {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_edit_disaster, null);

        android.widget.EditText etTitle  = dialogView.findViewById(R.id.etTitle);
        android.widget.EditText etDesc   = dialogView.findViewById(R.id.etDescription);
        android.widget.Spinner spSev     = dialogView.findViewById(R.id.spinnerSeverity);
        android.widget.Spinner spStatus  = dialogView.findViewById(R.id.spinnerStatus);
        android.widget.EditText etGcash  = dialogView.findViewById(R.id.etGcash);

        etTitle.setText(d.getTitle());
        etDesc.setText(d.getDescription());
        etGcash.setText(d.getGcashNumber());

        // Set spinner selections
        String[] severities = {"Low", "Medium", "High", "Critical"};
        String[] statuses   = {"Active", "Monitoring", "Resolved"};
        for (int i = 0; i < severities.length; i++) {
            if (severities[i].equals(d.getSeverity())) { spSev.setSelection(i); break; }
        }
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equals(d.getStatus())) { spStatus.setSelection(i); break; }
        }

        new AlertDialog.Builder(this)
                .setTitle("Edit Disaster")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, w) -> {
                    String newTitle  = etTitle.getText().toString().trim();
                    String newDesc   = etDesc.getText().toString().trim();
                    String newSev    = spSev.getSelectedItem().toString();
                    String newStatus = spStatus.getSelectedItem().toString();
                    String newGcash  = etGcash.getText().toString().trim();
                    updateDisaster(d.getId(), newTitle, newDesc, newSev, newStatus, newGcash);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateDisaster(String id, String title, String desc,
                                String severity, String status, String gcash) {
        new Thread(() -> {
            try {
                String token = SessionManager.getInstance().getToken();
                OkHttpClient client = new OkHttpClient();
                JSONObject body = new JSONObject();
                body.put("title", title);
                body.put("description", desc);
                body.put("severity", severity);
                body.put("status", status);
                body.put("gcash_number", gcash);

                Request req = new Request.Builder()
                        .url(SUPABASE_REST + "disasters?id=eq." + id)
                        .header("apikey", ANON_KEY)
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .header("Prefer", "return=representation")
                        .patch(RequestBody.create(body.toString(),
                                MediaType.parse("application/json")))
                        .build();
                Response resp = client.newCall(req).execute();
                runOnUiThread(() -> {
                    if (resp.isSuccessful()) {
                        Toast.makeText(this, "Disaster updated", Toast.LENGTH_SHORT).show();
                        loadDisasters(); // refresh
                    } else {
                        Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void confirmDeleteFromMap(Disaster d, View panel) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Disaster")
                .setMessage("Delete \"" + d.getTitle() + "\"? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, w) -> deleteDisaster(d.getId(), panel))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteDisaster(String id, View panel) {
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
                runOnUiThread(() -> {
                    if (resp.isSuccessful()) {
                        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                        panel.setVisibility(View.GONE);
                        loadDisasters();
                    } else {
                        Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_disasters);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
                overridePendingTransition(0, 0);
            } else if (id == R.id.nav_donations) {
                startActivity(new Intent(this, AdminDonationsActivity.class));
                overridePendingTransition(0, 0);
            } else if (id == R.id.nav_users) {
                startActivity(new Intent(this, AdminUsersActivity.class));
                overridePendingTransition(0, 0);
            } else if (id == R.id.nav_map) {
            startActivity(new Intent(this, AdminDisastersMapActivity.class));
            overridePendingTransition(0, 0);
        }
            return true;
        });
    }

    @Override public void onResume() { super.onResume(); mapView.onResume(); }
    @Override public void onPause()  { super.onPause();  mapView.onPause();  }
}