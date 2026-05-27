package edu.cit.labaya.disasteraidconnect.ui.dashboard;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import edu.cit.labaya.disasteraidconnect.R;
import edu.cit.labaya.disasteraidconnect.core.session.SessionManager;
import edu.cit.labaya.disasteraidconnect.data.model.Disaster;
import edu.cit.labaya.disasteraidconnect.data.model.Donation;
import edu.cit.labaya.disasteraidconnect.data.model.Notification;
import edu.cit.labaya.disasteraidconnect.ui.aidrequest.AidRequestActivity;
import edu.cit.labaya.disasteraidconnect.ui.admin.AdminDashboardActivity;
import edu.cit.labaya.disasteraidconnect.ui.auth.login.LoginActivity;
import edu.cit.labaya.disasteraidconnect.ui.disaster.DisasterMapActivity;
import edu.cit.labaya.disasteraidconnect.ui.donation.DonationActivity;

public class DashboardActivity extends AppCompatActivity {

    private DashboardViewModel viewModel;
    private TextView tvGreeting, tvUsername, tvAvatarInitials;
    private TextView tvMyReports, tvActiveDisasters, tvCriticalAlerts, tvTotalDonated;
    private TextView tabRequests, tabDonations, tvViewAll, tvEmpty, btnAddToMap;
    private LinearLayout listContainer;
    private ProgressBar progressBarList;
    private BottomNavigationView bottomNav;

    // Notification UI
    private FrameLayout btnBell;
    private TextView tvBellBadge;
    private View notificationPanel;
    private LinearLayout notificationList;
    private LinearLayout tvNotifEmpty;

    private boolean showingRequests = true;
    private boolean notifPanelOpen  = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        bindViews();
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        setupGreeting();
        setupTabs();
        setupBottomNav();
        setupObservers();
        setupNotificationBell();

        viewModel.loadDashboard();
    }

    private void bindViews() {
        tvGreeting        = findViewById(R.id.tvGreeting);
        tvUsername        = findViewById(R.id.tvUsername);
        tvAvatarInitials  = findViewById(R.id.tvAvatarInitials);
        tvMyReports       = findViewById(R.id.tvMyReports);
        tvActiveDisasters = findViewById(R.id.tvActiveDisasters);
        tvCriticalAlerts  = findViewById(R.id.tvCriticalAlerts);
        tvTotalDonated    = findViewById(R.id.tvTotalDonated);
        tabRequests       = findViewById(R.id.tabRequests);
        tabDonations      = findViewById(R.id.tabDonations);
        tvViewAll         = findViewById(R.id.tvViewAll);
        tvEmpty           = findViewById(R.id.tvEmpty);
        btnAddToMap       = findViewById(R.id.btnAddToMap);
        listContainer     = findViewById(R.id.listContainer);
        progressBarList   = findViewById(R.id.progressBarList);
        bottomNav         = findViewById(R.id.bottomNav);
        btnBell           = findViewById(R.id.btnBell);
        tvBellBadge       = findViewById(R.id.tvBellBadge);
        notificationPanel = findViewById(R.id.notificationPanel);
        notificationList  = findViewById(R.id.notificationList);
        tvNotifEmpty = findViewById(R.id.tvNotifEmpty);

        findViewById(R.id.btnRefresh).setOnClickListener(v -> viewModel.loadDashboard());
        btnAddToMap.setOnClickListener(v ->
                startActivity(new Intent(this, DisasterMapActivity.class)));

        // Close notif panel when tapping outside
        findViewById(R.id.notificationOverlay).setOnClickListener(v -> closeNotifPanel());

        findViewById(R.id.tvLogout).setOnClickListener(v -> {
            SessionManager.getInstance().clearSession();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupNotificationBell() {
        btnBell.setOnClickListener(v -> {
            if (notifPanelOpen) {
                closeNotifPanel();
            } else {
                openNotifPanel();
            }
        });

        // Close button inside panel
        findViewById(R.id.btnCloseNotif).setOnClickListener(v -> closeNotifPanel());
    }

    private void openNotifPanel() {
        notifPanelOpen = true;
        notificationPanel.setVisibility(View.VISIBLE);
        findViewById(R.id.notificationOverlay).setVisibility(View.VISIBLE);
        // Mark all as read
        String userId = SessionManager.getInstance().getUserId();
        String token  = SessionManager.getInstance().getToken();
        viewModel.markAllRead(userId, token);
        tvBellBadge.setVisibility(View.GONE);
    }

    private void closeNotifPanel() {
        notifPanelOpen = false;
        notificationPanel.setVisibility(View.GONE);
        findViewById(R.id.notificationOverlay).setVisibility(View.GONE);
    }

    private void setupGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting = hour < 12 ? "GOOD MORNING,"
                : hour < 17 ? "GOOD AFTERNOON," : "GOOD EVENING,";
        tvGreeting.setText(greeting);

        String username = SessionManager.getInstance().getUsername();
        if (username != null && !username.isEmpty()) {
            tvUsername.setText(username);
            tvAvatarInitials.setText(username.length() >= 2
                    ? username.substring(0, 2).toUpperCase()
                    : username.toUpperCase());
        }
    }

    private void setupTabs() {
        tabRequests.setOnClickListener(v -> switchTab(true));
        tabDonations.setOnClickListener(v -> switchTab(false));

        tvViewAll.setOnClickListener(v -> {
            if (showingRequests) startActivity(new Intent(this, AidRequestActivity.class));
            else startActivity(new Intent(this, DonationActivity.class));
        });
    }

    private void switchTab(boolean requests) {
        showingRequests = requests;
        tabRequests.setBackgroundResource(requests
                ? R.drawable.bg_tab_active : R.drawable.bg_tab_inactive);
        tabRequests.setTextColor(getColor(requests
                ? R.color.colorTextPrimary : R.color.colorTextSecondary));
        tabDonations.setBackgroundResource(requests
                ? R.drawable.bg_tab_inactive : R.drawable.bg_tab_active);
        tabDonations.setTextColor(getColor(requests
                ? R.color.colorTextSecondary : R.color.colorTextPrimary));

        if (requests) {
            tvViewAll.setText("View all my requests →");
            renderRequests(viewModel.disasters.getValue());
        } else {
            tvViewAll.setText("View all donations →");
            renderDonations(viewModel.donations.getValue());
        }
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return true;
            if (id == R.id.nav_map) {
                startActivity(new Intent(this, DisasterMapActivity.class)); return true;
            }
            if (id == R.id.nav_requests) {
                startActivity(new Intent(this, AidRequestActivity.class)); return true;
            }
            if (id == R.id.nav_donations) {
                startActivity(new Intent(this, DonationActivity.class)); return true;
            }
            if (id == R.id.nav_admin) {
                String role = SessionManager.getInstance().getRole();
                if ("admin".equals(role)) {
                    startActivity(new Intent(this, AdminDashboardActivity.class));
                } else {
                    Toast.makeText(this, "Admin access only", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });
    }

    private void setupObservers() {
        viewModel.disasters.observe(this, disasters -> {
            if (disasters == null) return;
            String userId = SessionManager.getInstance().getUserId();

            // My reports = disasters created_by this user
            long myCount = disasters.stream()
                    .filter(d -> userId != null && userId.equals(d.getUserId()))
                    .count();
            tvMyReports.setText(String.valueOf(myCount));

            long active = disasters.stream()
                    .filter(d -> "Active".equalsIgnoreCase(d.getStatus()))
                    .count();
            tvActiveDisasters.setText(String.valueOf(active));

            long critical = disasters.stream()
                    .filter(d -> "Active".equalsIgnoreCase(d.getStatus()) &&
                            ("High".equalsIgnoreCase(d.getSeverity()) ||
                                    "Critical".equalsIgnoreCase(d.getSeverity())))
                    .count();
            tvCriticalAlerts.setText(String.valueOf(critical));

            if (showingRequests) renderRequests(disasters);
        });

        viewModel.donations.observe(this, donations -> {
            if (donations == null) return;
            double total = donations.stream()
                    .filter(d -> "Completed".equalsIgnoreCase(d.getStatus()))
                    .mapToDouble(Donation::getAmount)
                    .sum();
            tvTotalDonated.setText(total == Math.floor(total)
                    ? "₱" + (long) total
                    : String.format("₱%.2f", total));

            if (!showingRequests) renderDonations(donations);
        });

        viewModel.notifications.observe(this, notifs -> {
            if (notifs == null) return;
            renderNotifications(notifs);
        });

        viewModel.unreadCount.observe(this, count -> {
            if (count != null && count > 0) {
                tvBellBadge.setVisibility(View.VISIBLE);
                tvBellBadge.setText(count > 9 ? "9+" : String.valueOf(count));
            } else {
                tvBellBadge.setVisibility(View.GONE);
            }
        });

        viewModel.isLoading.observe(this, loading ->
                progressBarList.setVisibility(loading ? View.VISIBLE : View.GONE));
    }

    private void renderNotifications(List<Notification> notifs) {
        notificationList.removeAllViews();
        if (notifs.isEmpty()) {
            tvNotifEmpty.setVisibility(View.VISIBLE);
            return;
        }
        tvNotifEmpty.setVisibility(View.GONE);
        for (Notification n : notifs) {
            View item = LayoutInflater.from(this)
                    .inflate(R.layout.item_notification, notificationList, false);

            TextView tvTitle   = item.findViewById(R.id.tvNotifTitle);
            TextView tvMessage = item.findViewById(R.id.tvNotifMessage);
            TextView tvDate    = item.findViewById(R.id.tvNotifDate);
            View     unreadDot = item.findViewById(R.id.unreadDot);

            tvTitle.setText(n.getTitle());
            tvMessage.setText(n.getMessage());
            tvDate.setText(formatDate(n.getCreatedAt()));
            unreadDot.setVisibility(n.isRead() ? View.GONE : View.VISIBLE);

            // Color by type
            if ("mute".equals(n.getType())) {
                tvTitle.setTextColor(getColor(R.color.colorSeverityMedium));
            }

            notificationList.addView(item);
        }
    }

    private void renderRequests(List<Disaster> disasters) {
        listContainer.removeAllViews();
        if (disasters == null || disasters.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("No disaster reports yet.");
            return;
        }
        tvEmpty.setVisibility(View.GONE);
        String userId = SessionManager.getInstance().getUserId();
        // Filter to only this user's reports
        List<Disaster> myReports = new java.util.ArrayList<>();
        for (Disaster d : disasters) {
            if (userId != null && userId.equals(d.getUserId())) myReports.add(d);
        }
        if (myReports.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("No disaster reports yet.");
            return;
        }
        int count = Math.min(myReports.size(), 5);
        for (int i = 0; i < count; i++) {
            Disaster d = myReports.get(i);
            View row = LayoutInflater.from(this)
                    .inflate(R.layout.item_disaster_row, listContainer, false);

            ((TextView) row.findViewById(R.id.tvTitle)).setText(d.getTitle());
            ((TextView) row.findViewById(R.id.tvDate)).setText(formatDate(d.getCreatedAt()));

            TextView tvSev = row.findViewById(R.id.tvSeverity);
            tvSev.setText(d.getSeverity());
            applySeverityStyle(tvSev, d.getSeverity());

            TextView tvStat = row.findViewById(R.id.tvStatus);
            tvStat.setText(d.getStatus());
            applyStatusStyle(tvStat, d.getStatus());

            listContainer.addView(row);
            if (i < count - 1) addDivider();
        }
    }

    private void renderDonations(List<Donation> donations) {
        listContainer.removeAllViews();
        if (donations == null || donations.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("No donations yet.");
            return;
        }
        tvEmpty.setVisibility(View.GONE);
        int count = Math.min(donations.size(), 5);
        for (int i = 0; i < count; i++) {
            Donation don = donations.get(i);
            View row = LayoutInflater.from(this)
                    .inflate(R.layout.item_dashboard_row, listContainer, false);

            String disasterName = don.getDisasterTitle() != null
                    ? don.getDisasterTitle() : "Unknown disaster";
            ((TextView) row.findViewById(R.id.tvTitle))
                    .setText(don.getFormattedAmount() + " — " + disasterName);
            ((TextView) row.findViewById(R.id.tvDate))
                    .setText(formatDate(don.getDonatedAt()));

            TextView tvSev = row.findViewById(R.id.tvSeverity);
            tvSev.setText("GCash");
            tvSev.setTextColor(getColor(R.color.colorPrimary));
            tvSev.setBackgroundResource(R.drawable.bg_badge_low);

            TextView tvStat = row.findViewById(R.id.tvStatus);
            tvStat.setText(don.getStatus());
            applyStatusStyle(tvStat, don.getStatus());

            listContainer.addView(row);
            if (i < count - 1) addDivider();
        }
    }

    private void addDivider() {
        View divider = new View(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        lp.setMarginStart(28);
        divider.setLayoutParams(lp);
        divider.setBackgroundColor(getColor(R.color.colorBorder));
        listContainer.addView(divider);
    }

    private void applySeverityStyle(TextView tv, String severity) {
        if (severity == null) return;
        switch (severity.toLowerCase()) {
            case "critical":
                tv.setTextColor(getColor(R.color.colorSeverityCritical));
                tv.setBackgroundResource(R.drawable.bg_badge_critical); break;
            case "high":
                tv.setTextColor(getColor(R.color.colorSeverityHigh));
                tv.setBackgroundResource(R.drawable.bg_badge_high); break;
            case "medium":
                tv.setTextColor(getColor(R.color.colorSeverityMedium));
                tv.setBackgroundResource(R.drawable.bg_badge_medium); break;
            default:
                tv.setTextColor(getColor(R.color.colorSeverityLow));
                tv.setBackgroundResource(R.drawable.bg_badge_low);
        }
    }

    private void applyStatusStyle(TextView tv, String status) {
        if (status == null) return;
        switch (status.toLowerCase()) {
            case "active":
            case "completed":
                tv.setTextColor(getColor(R.color.colorSuccess));
                tv.setBackgroundResource(R.drawable.bg_badge_active); break;
            case "resolved":
                tv.setTextColor(getColor(R.color.colorPrimary));
                tv.setBackgroundResource(R.drawable.bg_badge_resolved); break;
            case "pending":
                tv.setTextColor(getColor(R.color.colorSeverityMedium));
                tv.setBackgroundResource(R.drawable.bg_badge_medium); break;
            default:
                tv.setTextColor(getColor(R.color.colorTextSecondary));
                tv.setBackgroundResource(R.drawable.bg_badge_low);
        }
    }

    private int getSeverityColor(String severity) {
        if (severity == null) return getColor(R.color.colorSeverityLow);
        switch (severity.toLowerCase()) {
            case "critical": return getColor(R.color.colorSeverityCritical);
            case "high":     return getColor(R.color.colorSeverityHigh);
            case "medium":   return getColor(R.color.colorSeverityMedium);
            default:         return getColor(R.color.colorSeverityLow);
        }
    }

    private String formatDate(String iso) {
        if (iso == null || iso.isEmpty()) return "—";
        try {
            SimpleDateFormat in  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault());
            SimpleDateFormat out = new SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.getDefault());
            return out.format(in.parse(iso));
        } catch (Exception e) {
            try {
                SimpleDateFormat in2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat out = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
                return out.format(in2.parse(iso));
            } catch (Exception ex) {
                return iso.substring(0, Math.min(10, iso.length()));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.setSelectedItemId(R.id.nav_home);
        viewModel.loadDashboard();
    }
}