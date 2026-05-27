package edu.cit.labaya.disasteraidconnect.ui.admin.users;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
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
import edu.cit.labaya.disasteraidconnect.data.model.User;
import edu.cit.labaya.disasteraidconnect.ui.admin.AdminDashboardActivity;
import edu.cit.labaya.disasteraidconnect.ui.admin.disasters.AdminDisastersActivity;
import edu.cit.labaya.disasteraidconnect.ui.admin.donations.AdminDonationsActivity;
import edu.cit.labaya.disasteraidconnect.ui.admin.map.AdminDisastersMapActivity;
import edu.cit.labaya.disasteraidconnect.utils.Resource;

public class AdminUsersActivity extends AppCompatActivity {

    private AdminUsersViewModel viewModel;
    private UserAdminAdapter adapter;
    private List<User> allUsers = new ArrayList<>();
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

        tvPageTitle.setText("Users");
        tvPageSubtitle.setText("View and manage all registered users on the platform");

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new UserAdminAdapter(new ArrayList<>(),
                user -> showMuteDialog(user),
                user -> confirmDeleteUser(user)
        );
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(AdminUsersViewModel.class);
        viewModel.users.observe(this, resource -> {
            if (resource.status == Resource.Status.LOADING) {
                progressBar.setVisibility(View.VISIBLE);
            } else if (resource.status == Resource.Status.ERROR) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
            } else {
                progressBar.setVisibility(View.GONE);
                allUsers = resource.data != null ? resource.data : new ArrayList<>();
                updateList(allUsers);
            }
        });
        viewModel.loadAllUsers();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { filter(q); return true; }
            @Override public boolean onQueryTextChange(String q) { filter(q); return true; }
        });

        setupBottomNav(R.id.nav_users);
    }

    private void filter(String query) {
        List<User> filtered = new ArrayList<>();
        for (User u : allUsers) {
            if ((u.getUsername() != null && u.getUsername().toLowerCase().contains(query.toLowerCase())) ||
                    (u.getEmail() != null && u.getEmail().toLowerCase().contains(query.toLowerCase()))) {
                filtered.add(u);
            }
        }
        updateList(filtered);
    }

    private void updateList(List<User> list) {
        adapter.updateData(list);
        tvRecordCount.setText(list.size() + " users");
        tvShowingRange.setText("Showing " + list.size() + " users");
    }

    private void showMuteDialog(User user) {
        if (user.isMuted()) {
            new AlertDialog.Builder(this)
                    .setTitle("Unmute " + user.getUsername())
                    .setMessage("Remove mute from this user?")
                    .setPositiveButton("Unmute", (d, w) ->
                            viewModel.unmuteUser(user.getId(), success ->
                                    runOnUiThread(() -> {
                                        if (success) {
                                            Toast.makeText(this, "User unmuted", Toast.LENGTH_SHORT).show();
                                            viewModel.loadAllUsers();
                                        } else {
                                            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
                                        }
                                    })))
                    .setNegativeButton("Cancel", null)
                    .show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_mute_user, null);
        TextView tvSubtitle  = dialogView.findViewById(R.id.tvMuteSubtitle);
        TextView tvExpires   = dialogView.findViewById(R.id.tvExpires);
        android.widget.EditText etDuration = dialogView.findViewById(R.id.etMuteDuration);
        android.widget.EditText etReason   = dialogView.findViewById(R.id.etMuteReason);

        tvSubtitle.setText("Restricting " + user.getUsername() + " from adding map points");

        // Preset buttons
        int[] presets = {1, 3, 7, 14, 30};
        int[] btnIds  = {R.id.btn1d, R.id.btn3d, R.id.btn7d, R.id.btn14d, R.id.btn30d};
        int[] selectedDays = {3}; // default

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault());

        Runnable updateExpiry = () -> {
            try {
                int days = Integer.parseInt(etDuration.getText().toString().trim());
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.add(java.util.Calendar.DAY_OF_YEAR, days);
                tvExpires.setText("Expires: " + sdf.format(cal.getTime()));
            } catch (Exception ignored) {}
        };
        updateExpiry.run();

        etDuration.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { updateExpiry.run(); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        // Highlight selected preset
        for (int i = 0; i < btnIds.length; i++) {
            final int days = presets[i];
            com.google.android.material.button.MaterialButton btn = dialogView.findViewById(btnIds[i]);
            if (days == selectedDays[0]) {
                btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFF59E0B));
                btn.setTextColor(0xFFFFFFFF);
            }
            btn.setOnClickListener(v -> {
                selectedDays[0] = days;
                etDuration.setText(String.valueOf(days));
                // Reset all presets
                for (int j = 0; j < btnIds.length; j++) {
                    com.google.android.material.button.MaterialButton b = dialogView.findViewById(btnIds[j]);
                    b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                            getResources().getColor(R.color.colorSurface, null)));
                    b.setTextColor(getResources().getColor(R.color.colorTextSecondary, null));
                }
                btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFF59E0B));
                btn.setTextColor(0xFFFFFFFF);
            });
        }

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Apply Mute", (d, w) -> {
                    String reason   = etReason.getText().toString().trim();
                    String daysStr  = etDuration.getText().toString().trim();
                    if (reason.isEmpty()) {
                        Toast.makeText(this, "Please enter a reason", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int days = 3;
                    try { days = Integer.parseInt(daysStr); } catch (Exception ignored) {}
                    final int finalDays = days;
                    viewModel.muteUser(user.getId(), reason, finalDays, success ->
                            runOnUiThread(() -> {
                                if (success) {
                                    Toast.makeText(this, user.getUsername() + " muted for " + finalDays + " days",
                                            Toast.LENGTH_SHORT).show();
                                    viewModel.loadAllUsers();
                                } else {
                                    Toast.makeText(this, "Failed to mute", Toast.LENGTH_SHORT).show();
                                }
                            }));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDeleteUser(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Delete \"" + user.getUsername() + "\"? This cannot be undone.")
                .setPositiveButton("Delete", (d, w) ->
                        viewModel.deleteUser(user.getId(), success ->
                                runOnUiThread(() -> {
                                    if (success) { Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show(); viewModel.loadAllUsers(); }
                                    else Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show();
                                })
                        ))
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
            } else if (id == R.id.nav_disasters) {
                startActivity(new Intent(this, AdminDisastersActivity.class));
                overridePendingTransition(0, 0);
            } else if (id == R.id.nav_donations) {
                startActivity(new Intent(this, AdminDonationsActivity.class));
                overridePendingTransition(0, 0);
            } else if (id == R.id.nav_map) {
            startActivity(new Intent(this, AdminDisastersMapActivity.class));
            overridePendingTransition(0, 0);
        }
            return true;
        });
    }
}