package edu.cit.labaya.disasteraidconnect.ui.admin.disasters;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;
import edu.cit.labaya.disasteraidconnect.R;
import edu.cit.labaya.disasteraidconnect.data.model.Disaster;
import edu.cit.labaya.disasteraidconnect.ui.admin.AdminDashboardActivity;
import edu.cit.labaya.disasteraidconnect.ui.admin.donations.AdminDonationsActivity;
import edu.cit.labaya.disasteraidconnect.ui.admin.map.AdminDisastersMapActivity;
import edu.cit.labaya.disasteraidconnect.ui.admin.users.AdminUsersActivity;
import edu.cit.labaya.disasteraidconnect.utils.Resource;

public class AdminDisastersActivity extends AppCompatActivity {

    private AdminDisastersViewModel viewModel;
    private DisasterAdminAdapter adapter;
    private List<Disaster> allDisasters = new ArrayList<>();
    private TextView tvPageTitle, tvPageSubtitle, tvRecordCount, tvShowingRange;
    private ProgressBar progressBar;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_list);

        tvPageTitle    = findViewById(R.id.tvPageTitle);
        tvPageSubtitle = findViewById(R.id.tvPageSubtitle);
        tvRecordCount  = findViewById(R.id.tvRecordCount);
        tvShowingRange = findViewById(R.id.tvShowingRange);
        progressBar    = findViewById(R.id.progressBar);
        searchView     = findViewById(R.id.searchView);

        tvPageTitle.setText("Disasters");
        tvPageSubtitle.setText("View and delete all reported disasters");

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new DisasterAdminAdapter(new ArrayList<>(),
                disaster -> confirmDelete(disaster),
                disaster -> {
                    // Jump to map at this disaster's location
                    Intent intent = new Intent(this, AdminDisastersMapActivity.class);
                    intent.putExtra("disasterId", disaster.getId());
                    intent.putExtra("lat", disaster.getLatitude());
                    intent.putExtra("lng", disaster.getLongitude());
                    startActivity(intent);
                }
        );

        viewModel = new ViewModelProvider(this).get(AdminDisastersViewModel.class);
        viewModel.disasters.observe(this, resource -> {
            if (resource.status == Resource.Status.LOADING) {
                progressBar.setVisibility(View.VISIBLE);
            } else if (resource.status == Resource.Status.ERROR) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
            } else {
                progressBar.setVisibility(View.GONE);
                allDisasters = resource.data != null ? resource.data : new ArrayList<>();
                updateList(allDisasters);
            }
        });
        viewModel.loadDisasters();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { filter(q); return true; }
            @Override public boolean onQueryTextChange(String q) { filter(q); return true; }
        });

        setupBottomNav(R.id.nav_disasters);
    }

    private void filter(String query) {
        List<Disaster> filtered = new ArrayList<>();
        for (Disaster d : allDisasters) {
            if (d.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    (d.getDescription() != null && d.getDescription().toLowerCase().contains(query.toLowerCase()))) {
                filtered.add(d);
            }
        }
        updateList(filtered);
    }

    private void updateList(List<Disaster> list) {
        adapter.updateData(list);
        tvRecordCount.setText(list.size() + " records");
        tvShowingRange.setText("Showing 1–" + list.size() + " of " + list.size());
    }

    private void confirmDelete(Disaster disaster) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Disaster")
                .setMessage("Delete \"" + disaster.getTitle() + "\"? This cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> viewModel.deleteDisaster(disaster.getId(), success -> {
                    runOnUiThread(() -> {
                        if (success) {
                            Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                            viewModel.loadDisasters();
                        } else {
                            Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show();
                        }
                    });
                }))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupBottomNav(int selectedId) {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(selectedId);
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
}