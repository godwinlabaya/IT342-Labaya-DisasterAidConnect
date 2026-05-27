package edu.cit.labaya.disasteraidconnect.ui.admin.donations;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;
import edu.cit.labaya.disasteraidconnect.R;
import edu.cit.labaya.disasteraidconnect.data.model.Donation;
import edu.cit.labaya.disasteraidconnect.ui.admin.AdminDashboardActivity;
import edu.cit.labaya.disasteraidconnect.ui.admin.disasters.AdminDisastersActivity;
import edu.cit.labaya.disasteraidconnect.ui.admin.map.AdminDisastersMapActivity;
import edu.cit.labaya.disasteraidconnect.ui.admin.users.AdminUsersActivity;
import edu.cit.labaya.disasteraidconnect.utils.Resource;

public class AdminDonationsActivity extends AppCompatActivity {

    private AdminDonationsViewModel viewModel;
    private DonationAdminAdapter adapter;
    private List<Donation> allDonations = new ArrayList<>();
    private String activeFilter = "all";
    private ProgressBar progressBar;
    private TextView tvRecordCount, tvShowingRange;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_list);

        TextView tvPageTitle    = findViewById(R.id.tvPageTitle);
        TextView tvPageSubtitle = findViewById(R.id.tvPageSubtitle);
        tvRecordCount  = findViewById(R.id.tvRecordCount);
        tvShowingRange = findViewById(R.id.tvShowingRange);
        progressBar    = findViewById(R.id.progressBar);
        searchView     = findViewById(R.id.searchView);

        tvPageTitle.setText("Donations");
        tvPageSubtitle.setText("View all donation records across the platform");

        // Show tab filter row for donations
        LinearLayout tabContainer = findViewById(R.id.tabContainer);
        // Use HorizontalScrollView visibility
        View tabScrollView = findViewById(R.id.tabContainer);
        tabScrollView.setVisibility(View.VISIBLE);

        // Build tabs dynamically
        LinearLayout tabRow = findViewById(R.id.tabRow);
        addTab(tabRow, "All", "all");
        addTab(tabRow, "Completed", "completed");
        addTab(tabRow, "Pending", "pending");
        addTab(tabRow, "Failed", "failed");

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new DonationAdminAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(AdminDonationsViewModel.class);
        viewModel.donations.observe(this, resource -> {
            if (resource.status == Resource.Status.LOADING) {
                progressBar.setVisibility(View.VISIBLE);
            } else if (resource.status == Resource.Status.ERROR) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
            } else {
                progressBar.setVisibility(View.GONE);
                allDonations = resource.data != null ? resource.data : new ArrayList<>();
                applyFilter();
            }
        });
        viewModel.loadAllDonations();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { applyFilter(); return true; }
            @Override public boolean onQueryTextChange(String q) { applyFilter(); return true; }
        });

        setupBottomNav(R.id.nav_donations);
    }

    private void addTab(LinearLayout row, String label, String filter) {
        TextView tab = new TextView(this);
        tab.setText(label);
        tab.setTextSize(13f);
        tab.setPadding(32, 16, 32, 16);
        tab.setTextColor(getResources().getColor(
                filter.equals(activeFilter) ? R.color.colorPrimary : R.color.colorTextMuted, null));
        tab.setOnClickListener(v -> {
            activeFilter = filter;
            applyFilter();
            // Reset all tab colors
            for (int i = 0; i < row.getChildCount(); i++) {
                ((TextView) row.getChildAt(i)).setTextColor(
                        getResources().getColor(R.color.colorTextMuted, null));
            }
            tab.setTextColor(getResources().getColor(R.color.colorPrimary, null));
        });
        row.addView(tab);
    }

    private void applyFilter() {
        String query = searchView.getQuery() != null ? searchView.getQuery().toString().toLowerCase() : "";
        List<Donation> filtered = new ArrayList<>();
        for (Donation d : allDonations) {
            boolean matchesFilter = activeFilter.equals("all") ||
                    (d.getStatus() != null && d.getStatus().toLowerCase().equals(activeFilter));
            boolean matchesSearch = query.isEmpty() ||
                    (d.getId() != null && d.getId().toLowerCase().contains(query)) ||
                    (d.getDonorName() != null && d.getDonorName().toLowerCase().contains(query)) ||
                    (d.getDisasterTitle() != null && d.getDisasterTitle().toLowerCase().contains(query));
            if (matchesFilter && matchesSearch) filtered.add(d);
        }
        adapter.updateData(filtered);
        tvRecordCount.setText(allDonations.size() + " records");
        tvShowingRange.setText("Showing 1–" + filtered.size() + " of " + filtered.size());
    }

    private void setupBottomNav(int selectedId) {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(selectedId);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
                overridePendingTransition(0, 0);
            } else if (id == R.id.nav_disasters) {
                startActivity(new Intent(this, AdminDisastersActivity.class));
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
}