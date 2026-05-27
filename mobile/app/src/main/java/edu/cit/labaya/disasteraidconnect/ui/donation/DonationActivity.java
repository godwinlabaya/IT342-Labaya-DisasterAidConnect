package edu.cit.labaya.disasteraidconnect.ui.donation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import edu.cit.labaya.disasteraidconnect.R;
import edu.cit.labaya.disasteraidconnect.core.session.SessionManager;
import edu.cit.labaya.disasteraidconnect.data.model.Donation;
import edu.cit.labaya.disasteraidconnect.data.repository.DonationRepository;
import edu.cit.labaya.disasteraidconnect.ui.aidrequest.AidRequestActivity;
import edu.cit.labaya.disasteraidconnect.ui.dashboard.DashboardActivity;
import edu.cit.labaya.disasteraidconnect.ui.disaster.DisasterMapActivity;
import edu.cit.labaya.disasteraidconnect.ui.admin.AdminDashboardActivity;

/**
 * DonationActivity — User donation history page.
 *
 * Matches web DonationsPage.jsx exactly:
 *   ① 4-stat summary row (total, contributed, pending, disasters supported)
 *   ② Filter tabs: All / Completed / Pending / Failed  (with counts)
 *   ③ RecyclerView of donation cards via DonationAdapter
 *   ④ "View on Map" per card → DisasterMapActivity with focusDisasterId extra
 *   ⑤ "Donate on Map" button → DisasterMapActivity
 *   ⑥ Empty state (tvEmpty) when no donations exist
 *
 * All view IDs match activity_donation.xml exactly.
 */
public class DonationActivity extends AppCompatActivity {

    private final DonationRepository repo = new DonationRepository();

    // Views — IDs match activity_donation.xml
    private ProgressBar          progressBar;
    private TextView             tvTotalDonations, tvTotalContributed;
    private TextView             tvPending, tvDisastersSupported;
    private TextView             tvEmpty;
    private TextView             tabAll, tabCompleted, tabPending, tabFailed;
    private RecyclerView         rvDonations;
    private MaterialButton       btnDonateOnMap;
    private BottomNavigationView bottomNav;

    private DonationAdapter adapter;
    private List<Donation>  allDonations = new ArrayList<>();
    private String          activeFilter = "All";
    private String          username     = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation);

        bindViews();
        setupRecyclerView();
        setupFilterTabs();
        setupBottomNav();

        username = SessionManager.getInstance().getUsername();
        if (username == null || username.isEmpty()) username = "You";

        loadDonations();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDonations();
    }

    private void bindViews() {
        progressBar          = findViewById(R.id.progressBar);
        tvTotalDonations     = findViewById(R.id.tvTotalDonations);
        tvTotalContributed   = findViewById(R.id.tvTotalContributed);
        tvPending            = findViewById(R.id.tvPending);
        tvDisastersSupported = findViewById(R.id.tvDisastersSupported);
        tvEmpty              = findViewById(R.id.tvEmpty);
        tabAll               = findViewById(R.id.tabAll);
        tabCompleted         = findViewById(R.id.tabCompleted);
        tabPending           = findViewById(R.id.tabPending);
        tabFailed            = findViewById(R.id.tabFailed);
        rvDonations          = findViewById(R.id.rvDonations);
        btnDonateOnMap       = findViewById(R.id.btnDonateOnMap);
        bottomNav            = findViewById(R.id.bottomNav);

        btnDonateOnMap.setOnClickListener(v ->
                startActivity(new Intent(this, DisasterMapActivity.class)));
    }

    private void setupRecyclerView() {
        adapter = new DonationAdapter(new ArrayList<>(), username);
        rvDonations.setLayoutManager(new LinearLayoutManager(this));
        rvDonations.setAdapter(adapter);
        rvDonations.setNestedScrollingEnabled(false);
    }

    private void setupFilterTabs() {
        tabAll.setOnClickListener(v       -> selectTab("All"));
        tabCompleted.setOnClickListener(v -> selectTab("Completed"));
        tabPending.setOnClickListener(v   -> selectTab("Pending"));
        tabFailed.setOnClickListener(v    -> selectTab("Failed"));
    }

    private void selectTab(String filter) {
        activeFilter = filter;
        setTabInactive(tabAll);
        setTabInactive(tabCompleted);
        setTabInactive(tabPending);
        setTabInactive(tabFailed);
        switch (filter) {
            case "All":       setTabActive(tabAll);       break;
            case "Completed": setTabActive(tabCompleted); break;
            case "Pending":   setTabActive(tabPending);   break;
            case "Failed":    setTabActive(tabFailed);    break;
        }
        adapter.updateList(filteredList());
        updateEmptyState();
    }

    private void setTabActive(TextView tab) {
        tab.setBackgroundResource(R.drawable.bg_tab_active);
        tab.setTextColor(getColor(R.color.colorTextPrimary));
        tab.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void setTabInactive(TextView tab) {
        tab.setBackgroundResource(R.drawable.bg_tab_inactive);
        tab.setTextColor(getColor(R.color.colorTextSecondary));
        tab.setTypeface(null, android.graphics.Typeface.NORMAL);
    }

    /**
     * Appends count to each tab: "All  6", "Completed  1" etc.
     * Mirrors web don-tab-count badge.
     */
    private void updateTabCounts() {
        tabAll.setText("All  " + allDonations.size());
        tabCompleted.setText("Completed  " + countByStatus("Completed"));
        tabPending.setText("Pending  " + countByStatus("Pending"));
        tabFailed.setText("Failed  " + countByStatus("Failed"));
        // Re-apply active style after setText (resets typeface)
        selectTab(activeFilter);
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_donations);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_donations) return true;
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, DashboardActivity.class)); finish(); return true;
            }
            if (id == R.id.nav_map) {
                startActivity(new Intent(this, DisasterMapActivity.class)); return true;
            }
            if (id == R.id.nav_requests) {
                startActivity(new Intent(this, AidRequestActivity.class)); return true;
            }
            if (id == R.id.nav_admin) {
                if ("admin".equals(SessionManager.getInstance().getRole())) {
                    startActivity(new Intent(this, AdminDashboardActivity.class));
                } else {
                    Toast.makeText(this, "Admin access only", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });
    }

    private void loadDonations() {
        showLoading(true);
        repo.getDonationsByUser(
                list -> runOnUiThread(() -> {
                    allDonations = list != null ? list : new ArrayList<>();
                    showLoading(false);
                    updateStats();
                    updateTabCounts();
                    // updateTabCounts calls selectTab which calls adapter.updateList
                    updateEmptyState();
                }),
                err -> runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(this, "Failed to load: " + err, Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                })
        );
    }

    private void updateStats() {
        tvTotalDonations.setText(String.valueOf(allDonations.size()));

        double total = 0;
        for (Donation d : allDonations) {
            if ("Completed".equals(d.getStatus())) total += d.getAmount();
        }
        tvTotalContributed.setText(formatPeso(total));

        tvPending.setText(String.valueOf(countByStatus("Pending")));

        Set<String> uniqueDisasters = new HashSet<>();
        for (Donation d : allDonations) {
            if (d.getDisasterId() != null && !d.getDisasterId().isEmpty()) {
                uniqueDisasters.add(d.getDisasterId());
            }
        }
        tvDisastersSupported.setText(String.valueOf(uniqueDisasters.size()));
    }

    private void updateEmptyState() {
        List<Donation> filtered = filteredList();
        if (filtered.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvDonations.setVisibility(View.GONE);
            if (allDonations.isEmpty()) {
                tvEmpty.setText("No donations yet.\nDonate on the map to get started! 💙");
            } else {
                tvEmpty.setText("No " + activeFilter.toLowerCase() + " donations.");
            }
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvDonations.setVisibility(View.VISIBLE);
        }
    }

    private List<Donation> filteredList() {
        if ("All".equals(activeFilter)) return allDonations;
        List<Donation> result = new ArrayList<>();
        for (Donation d : allDonations) {
            if (activeFilter.equals(d.getStatus())) result.add(d);
        }
        return result;
    }

    private int countByStatus(String status) {
        int count = 0;
        for (Donation d : allDonations) {
            if (status.equals(d.getStatus())) count++;
        }
        return count;
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading) {
            rvDonations.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.GONE);
        }
    }

    private String formatPeso(double amount) {
        return String.format(Locale.getDefault(), "₱%.2f", amount);
    }
}