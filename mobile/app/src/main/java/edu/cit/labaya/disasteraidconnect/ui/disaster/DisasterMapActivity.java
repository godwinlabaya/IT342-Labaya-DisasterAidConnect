package edu.cit.labaya.disasteraidconnect.ui.disaster;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import java.util.ArrayList;
import java.util.List;
import edu.cit.labaya.disasteraidconnect.R;
import edu.cit.labaya.disasteraidconnect.core.session.SessionManager;
import edu.cit.labaya.disasteraidconnect.data.model.Disaster;
import edu.cit.labaya.disasteraidconnect.data.repository.DonationRepository;
import edu.cit.labaya.disasteraidconnect.ui.aidrequest.AidRequestActivity;
import edu.cit.labaya.disasteraidconnect.ui.dashboard.DashboardActivity;
import edu.cit.labaya.disasteraidconnect.ui.donation.DonationActivity;
import edu.cit.labaya.disasteraidconnect.ui.admin.AdminDashboardActivity;

/**
 * DisasterMapActivity — User-facing disaster map.
 *
 * Features (mirrors web MapPage.jsx exactly):
 *  ① OSMDroid map with color-coded severity markers
 *  ② Bottom sheet: disaster list + selected detail card
 *  ③ Add Point (long-press map or FAB) → BottomSheetDialog form
 *  ④ Edit/Delete own disasters
 *  ⑤ Donate flow:
 *       - Opens dialog_donate.xml (amount input + quick chips)
 *       - Records donation as "Completed" in Supabase donations table
 *       - Deep-links into GCash app with phone + amount
 *       - Donation visible on DonationActivity, AdminDonationsActivity, web donations page
 *  ⑥ Mute banner + bell notification panel (mirrors MuteNotificationBell.js)
 *  ⑦ Search filter
 */
public class DisasterMapActivity extends AppCompatActivity {

    // ── ViewModels & Repos ────────────────────────────────────────────────────
    private DisasterMapViewModel viewModel;
    private final DonationRepository donationRepo = new DonationRepository();

    // ── Map ───────────────────────────────────────────────────────────────────
    private MapView mapView;
    private MyLocationNewOverlay locationOverlay;

    // ── Bottom sheet ──────────────────────────────────────────────────────────
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private RecyclerView rvDisasters;
    private DisasterListAdapter adapter;

    // ── Top bar views ─────────────────────────────────────────────────────────
    private MaterialButton btnAddPoint;
    private TextView       btnBell;
    private View           bellDot;
    private TextView       tvAvatar;

    // ── Mute banner ───────────────────────────────────────────────────────────
    private LinearLayout muteBanner;
    private TextView     tvMuteMessage;

    // ── Detail panel views ────────────────────────────────────────────────────
    private LinearLayout disasterDetailPanel;
    private TextView     tvDisasterCount, tvPointCount;
    private TextView     detailTitle, detailDate, detailReporter;
    private TextView     detailDescription, detailCoords;
    private TextView     detailSeverityBadge, detailStatusBadge;
    private MaterialButton btnDonate, btnEditDisaster, btnDeleteDisaster;

    // ── Bottom nav ────────────────────────────────────────────────────────────
    private BottomNavigationView bottomNav;

    // ── State ─────────────────────────────────────────────────────────────────
    private Disaster selectedDisaster;
    private Double   pendingLat, pendingLon;
    private List<Disaster> allDisasters = new ArrayList<>();

    // Bell popup window reference (for dismissal)
    private PopupWindow bellPopup;

    private static final double DEFAULT_LAT = 10.3157;
    private static final double DEFAULT_LON = 123.8854;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_disaster_map);

        bindViews();
        setupMap();
        setupBottomSheet();
        setupSearch();
        setupBottomNav();
        setAvatarInitials();

        viewModel = new ViewModelProvider(this).get(DisasterMapViewModel.class);
        setupObservers();
        viewModel.loadDisasters();
        viewModel.checkMuteStatus();
    }

    // ── View Binding ──────────────────────────────────────────────────────────

    private void bindViews() {
        mapView             = findViewById(R.id.mapView);
        rvDisasters         = findViewById(R.id.rvDisasters);
        disasterDetailPanel = findViewById(R.id.disasterDetailPanel);
        muteBanner          = findViewById(R.id.muteBanner);
        tvMuteMessage       = findViewById(R.id.tvMuteMessage);
        tvDisasterCount     = findViewById(R.id.tvDisasterCount);
        tvPointCount        = findViewById(R.id.tvPointCount);
        detailTitle         = findViewById(R.id.detailTitle);
        detailDate          = findViewById(R.id.detailDate);
        detailReporter      = findViewById(R.id.detailReporter);
        detailDescription   = findViewById(R.id.detailDescription);
        detailCoords        = findViewById(R.id.detailCoords);
        detailSeverityBadge = findViewById(R.id.detailSeverityBadge);
        detailStatusBadge   = findViewById(R.id.detailStatusBadge);
        btnDonate           = findViewById(R.id.btnDonate);
        btnEditDisaster     = findViewById(R.id.btnEditDisaster);
        btnDeleteDisaster   = findViewById(R.id.btnDeleteDisaster);
        btnAddPoint         = findViewById(R.id.btnAddPoint);
        btnBell             = findViewById(R.id.btnBell);
        bellDot             = findViewById(R.id.bellDot);
        tvAvatar            = findViewById(R.id.tvAvatar);
        bottomNav           = findViewById(R.id.bottomNav);

        // ── Add Point ────────────────────────────────────────────────────────
        btnAddPoint.setOnClickListener(v -> {
            if (Boolean.TRUE.equals(viewModel.isMuted.getValue())) {
                Toast.makeText(this, "You are muted and cannot add disaster points.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            GeoPoint center = (GeoPoint) mapView.getMapCenter();
            pendingLat = center.getLatitude();
            pendingLon = center.getLongitude();
            showAddDisasterDialog(null);
        });

        // ── Donate ───────────────────────────────────────────────────────────
        // Opens amount-input dialog, then records + deep-links GCash
        btnDonate.setOnClickListener(v -> {
            if (selectedDisaster == null) return;
            showDonateDialog(selectedDisaster);
        });

        // ── Edit ─────────────────────────────────────────────────────────────
        btnEditDisaster.setOnClickListener(v -> {
            if (selectedDisaster != null) showAddDisasterDialog(selectedDisaster);
        });

        // ── Delete ───────────────────────────────────────────────────────────
        btnDeleteDisaster.setOnClickListener(v -> {
            if (selectedDisaster != null) confirmDelete(selectedDisaster);
        });

        // ── Bell icon: show/hide mute notification panel ─────────────────────
        btnBell.setOnClickListener(v -> showBellPanel(v));

        // ── My Location FAB ──────────────────────────────────────────────────
        findViewById(R.id.fabMyLocation).setOnClickListener(v -> {
            if (locationOverlay != null && locationOverlay.getMyLocation() != null) {
                mapView.getController().animateTo(locationOverlay.getMyLocation());
                mapView.getController().setZoom(16.0);
            } else {
                Toast.makeText(this, "Location not available yet", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Map Setup ─────────────────────────────────────────────────────────────

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(13.0);
        mapView.getController().setCenter(new GeoPoint(DEFAULT_LAT, DEFAULT_LON));

        locationOverlay = new MyLocationNewOverlay(
                new GpsMyLocationProvider(this), mapView);
        locationOverlay.enableMyLocation();
        mapView.getOverlays().add(locationOverlay);

        // Long-press to place new disaster point (blocked when muted)
        mapView.getOverlays().add(new org.osmdroid.views.overlay.MapEventsOverlay(
                new org.osmdroid.events.MapEventsReceiver() {
                    @Override
                    public boolean singleTapConfirmedHelper(GeoPoint p) { return false; }

                    @Override
                    public boolean longPressHelper(GeoPoint p) {
                        if (Boolean.TRUE.equals(viewModel.isMuted.getValue())) {
                            Toast.makeText(DisasterMapActivity.this,
                                    "You are muted and cannot add points.",
                                    Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        pendingLat = p.getLatitude();
                        pendingLon = p.getLongitude();
                        showAddDisasterDialog(null);
                        return true;
                    }
                }
        ));
    }

    // ── Bottom Sheet ──────────────────────────────────────────────────────────

    private void setupBottomSheet() {
        LinearLayout sheet = findViewById(R.id.bottomSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(sheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setPeekHeight(160);

        rvDisasters.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DisasterListAdapter(new ArrayList<>(), disaster -> {
            selectedDisaster = disaster;
            showDisasterDetail(disaster);
            mapView.getController().animateTo(
                    new GeoPoint(disaster.getLatitude(), disaster.getLongitude()));
            mapView.getController().setZoom(15.0);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });
        rvDisasters.setAdapter(adapter);
    }

    // ── Search ────────────────────────────────────────────────────────────────

    private void setupSearch() {
        EditText etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                filterDisasters(s.toString());
            }
        });
    }

    // ── Bottom Nav ────────────────────────────────────────────────────────────

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_map);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_map)       return true;
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, DashboardActivity.class)); finish(); return true;
            }
            if (id == R.id.nav_requests) {
                startActivity(new Intent(this, AidRequestActivity.class)); return true;
            }
            if (id == R.id.nav_donations) {
                startActivity(new Intent(this, DonationActivity.class)); return true;
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

    // ── Avatar initials from session ──────────────────────────────────────────

    private void setAvatarInitials() {
        String username = SessionManager.getInstance().getUsername();
        if (username != null && username.length() >= 2) {
            tvAvatar.setText(username.substring(0, 2).toUpperCase());
        }
    }

    // ── LiveData Observers ────────────────────────────────────────────────────

    private void setupObservers() {
        viewModel.disasters.observe(this, disasters -> {
            if (disasters == null) return;
            allDisasters = disasters;
            renderMarkers(disasters);
            adapter.updateList(disasters);
            tvPointCount.setText(disasters.size() + " points");
        });

        viewModel.isMuted.observe(this, muted -> {
            boolean isMuted = Boolean.TRUE.equals(muted);

            // Mute banner
            muteBanner.setVisibility(isMuted ? View.VISIBLE : View.GONE);

            // Bell dot indicator
            bellDot.setVisibility(isMuted ? View.VISIBLE : View.GONE);

            // Add Point button — visually muted + disabled
            btnAddPoint.setEnabled(!isMuted);
            btnAddPoint.setAlpha(isMuted ? 0.45f : 1.0f);
            btnAddPoint.setText(isMuted ? "Muted" : "+ Add");

            if (isMuted && viewModel.muteReason.getValue() != null) {
                tvMuteMessage.setText(
                        "You have been muted: " + viewModel.muteReason.getValue() +
                                ". Tap the 🔔 for details.");
            }
        });

        viewModel.operationResult.observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                viewModel.loadDisasters();
                viewModel.operationResult.setValue(null);
            }
        });
    }

    // ── Marker Rendering ─────────────────────────────────────────────────────

    private void renderMarkers(List<Disaster> disasters) {
        // Remove old markers, keep location overlay
        mapView.getOverlays().removeIf(o -> o instanceof Marker);

        for (Disaster d : disasters) {
            if (d.getLatitude() == 0 && d.getLongitude() == 0) continue;

            Marker marker = new Marker(mapView);
            marker.setPosition(new GeoPoint(d.getLatitude(), d.getLongitude()));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(d.getTitle());
            marker.setSnippet(d.getSeverity());
            marker.setIcon(getSeverityIcon(d.getSeverity()));

            marker.setOnMarkerClickListener((m, mapV) -> {
                selectedDisaster = d;
                showDisasterDetail(d);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                return true;
            });
            mapView.getOverlays().add(marker);
        }
        mapView.invalidate();
    }

    /**
     * Returns a tinted marker drawable per severity.
     * Matches web createMarkerIcon() / getSeverityColor() color scheme.
     */
    private android.graphics.drawable.Drawable getSeverityIcon(String severity) {
        android.graphics.drawable.Drawable icon =
                androidx.core.content.ContextCompat.getDrawable(
                        this, org.osmdroid.library.R.drawable.marker_default_focused_base);
        if (icon == null) return null;
        int color;
        if (severity == null) color = getColor(R.color.colorSeverityLow);
        else switch (severity.toLowerCase()) {
            case "critical": color = getColor(R.color.colorSeverityCritical); break;
            case "high":     color = getColor(R.color.colorSeverityHigh);     break;
            case "medium":   color = getColor(R.color.colorSeverityMedium);   break;
            default:         color = getColor(R.color.colorSeverityLow);
        }
        icon = icon.mutate();
        icon.setTint(color);
        return icon;
    }

    // ── Disaster Detail Card ──────────────────────────────────────────────────

    private void showDisasterDetail(Disaster d) {
        disasterDetailPanel.setVisibility(View.VISIBLE);

        detailTitle.setText(d.getTitle());
        detailDate.setText(formatDate(d.getCreatedAt()));
        detailReporter.setText("👤 Added by " +
                (d.getReportedBy() != null ? d.getReportedBy() : "Unknown"));
        detailDescription.setText(d.getDescription() != null ? d.getDescription() : "");
        detailCoords.setText(String.format("Coordinates: %.5f, %.5f",
                d.getLatitude(), d.getLongitude()));

        applySeverityBadge(detailSeverityBadge, d.getSeverity());
        applyStatusBadge(detailStatusBadge, d.getStatus());

        String userId = SessionManager.getInstance().getUserId();
        String role   = SessionManager.getInstance().getRole();
        boolean isOwn   = userId != null && userId.equals(d.getUserId());
        boolean isAdmin = "admin".equals(role);

        btnEditDisaster.setVisibility(isOwn || isAdmin ? View.VISIBLE : View.GONE);
        btnDeleteDisaster.setVisibility(isOwn || isAdmin ? View.VISIBLE : View.GONE);
    }

    // ── Filter ────────────────────────────────────────────────────────────────

    private void filterDisasters(String query) {
        if (query == null || query.trim().isEmpty()) {
            adapter.updateList(allDisasters);
            renderMarkers(allDisasters);
            return;
        }
        String q = query.toLowerCase().trim();
        List<Disaster> filtered = new ArrayList<>();
        for (Disaster d : allDisasters) {
            if ((d.getTitle() != null && d.getTitle().toLowerCase().contains(q)) ||
                    (d.getDescription() != null && d.getDescription().toLowerCase().contains(q))) {
                filtered.add(d);
            }
        }
        adapter.updateList(filtered);
        renderMarkers(filtered);
    }

    // ── Add / Edit Dialog ─────────────────────────────────────────────────────

    private void showAddDisasterDialog(Disaster existing) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_disaster, null);
        dialog.setContentView(view);

        TextView     dialogTitle  = view.findViewById(R.id.dialogTitle);
        TextView     dialogCoords = view.findViewById(R.id.dialogCoords);
        EditText     etTitle      = view.findViewById(R.id.etTitle);
        EditText     etDesc       = view.findViewById(R.id.etDescription);
        EditText     etGcash      = view.findViewById(R.id.etGcashNumber);
        Spinner      spSeverity   = view.findViewById(R.id.spSeverity);
        Spinner      spStatus     = view.findViewById(R.id.spStatus);
        ProgressBar  pb           = view.findViewById(R.id.progressBar);
        MaterialButton btnSave    = view.findViewById(R.id.btnSave);
        MaterialButton btnCancel  = view.findViewById(R.id.btnCancel);

        String[] severities = {"Low", "Medium", "High", "Critical"};
        String[] statuses   = {"Active", "Monitoring", "Resolved"};

        spSeverity.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, severities));
        spStatus.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, statuses));

        boolean isEdit = existing != null;
        dialogTitle.setText(isEdit ? "Edit Disaster Point" : "New Disaster Point");

        if (isEdit) {
            etTitle.setText(existing.getTitle());
            etDesc.setText(existing.getDescription());
            etGcash.setText(existing.getGcashNumber());
            pendingLat = existing.getLatitude();
            pendingLon = existing.getLongitude();
            for (int i = 0; i < severities.length; i++)
                if (severities[i].equalsIgnoreCase(existing.getSeverity()))
                    spSeverity.setSelection(i);
            for (int i = 0; i < statuses.length; i++)
                if (statuses[i].equalsIgnoreCase(existing.getStatus()))
                    spStatus.setSelection(i);
        }

        if (pendingLat != null && pendingLon != null) {
            dialogCoords.setText(String.format("📍 %.5f, %.5f", pendingLat, pendingLon));
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String title    = etTitle.getText().toString().trim();
            String desc     = etDesc.getText().toString().trim();
            String gcash    = etGcash.getText().toString().trim();
            String severity = spSeverity.getSelectedItem().toString();
            String status   = spStatus.getSelectedItem().toString();

            if (title.isEmpty()) {
                Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show(); return;
            }
            if (desc.isEmpty()) {
                Toast.makeText(this, "Description is required", Toast.LENGTH_SHORT).show(); return;
            }
            if (!isEdit && gcash.isEmpty()) {
                Toast.makeText(this, "GCash number is required", Toast.LENGTH_SHORT).show(); return;
            }
            if (pendingLat == null || pendingLon == null) {
                Toast.makeText(this, "Location not set", Toast.LENGTH_SHORT).show(); return;
            }

            pb.setVisibility(View.VISIBLE);
            btnSave.setEnabled(false);

            if (isEdit) {
                viewModel.updateDisaster(existing.getId(), title, desc,
                        severity, status, gcash, () -> {
                            runOnUiThread(() -> {
                                pb.setVisibility(View.GONE);
                                dialog.dismiss();
                            });
                        });
            } else {
                viewModel.addDisaster(title, desc, severity, status, gcash,
                        pendingLat, pendingLon, () -> {
                            runOnUiThread(() -> {
                                pb.setVisibility(View.GONE);
                                dialog.dismiss();
                                pendingLat = null;
                                pendingLon = null;
                            });
                        });
            }
        });

        dialog.show();
    }

    // ── Donate Dialog ─────────────────────────────────────────────────────────

    /**
     * Shows the donate bottom sheet with amount input.
     * On confirm:
     *   1. Records donation as "Completed" in Supabase (visible in DonationActivity + admin)
     *   2. Deep-links into GCash app with phone number + amount
     *
     * Mirrors web DonationModal but adds amount input for the mobile GCash deep-link.
     */
    private void showDonateDialog(Disaster disaster) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_donate, null);
        dialog.setContentView(view);

        // Bind views
        TextView     tvDisasterTitle = view.findViewById(R.id.tvDonateDisasterTitle);
        TextView     tvAddedBy       = view.findViewById(R.id.tvDonateAddedBy);
        TextView     tvGcashDisplay  = view.findViewById(R.id.tvGcashNumberDisplay);
        EditText     etAmount        = view.findViewById(R.id.etDonationAmount);
        TextView     tvError         = view.findViewById(R.id.tvDonateError);
        MaterialButton btnConfirm    = view.findViewById(R.id.btnDonateConfirm);
        MaterialButton btnCancel     = view.findViewById(R.id.btnDonateCancel);

        // Quick chip views
        TextView chip50  = view.findViewById(R.id.chip50);
        TextView chip100 = view.findViewById(R.id.chip100);
        TextView chip250 = view.findViewById(R.id.chip250);
        TextView chip500 = view.findViewById(R.id.chip500);

        // Populate disaster info
        tvDisasterTitle.setText("\"" + disaster.getTitle() + "\"");
        String reporter = disaster.getReportedBy() != null
                ? disaster.getReportedBy() : "Unknown";
        tvAddedBy.setText("Added by " + reporter);
        tvGcashDisplay.setText(
                disaster.getGcashNumber() != null ? disaster.getGcashNumber() : "Not provided");

        // If no GCash number, disable confirm
        if (disaster.getGcashNumber() == null || disaster.getGcashNumber().isEmpty()) {
            btnConfirm.setEnabled(false);
            btnConfirm.setAlpha(0.5f);
            tvError.setText("No GCash number provided for this disaster.");
            tvError.setVisibility(View.VISIBLE);
        }

        // ── Quick chip selection ──────────────────────────────────────────────
        View.OnClickListener chipClick = cv -> {
            String tag = (String) cv.getTag();
            etAmount.setText(tag);
            etAmount.setSelection(tag.length());
            // Reset chip styles
            resetChipStyle(chip50);
            resetChipStyle(chip100);
            resetChipStyle(chip250);
            resetChipStyle(chip500);
            // Highlight selected
            highlightChip((TextView) cv);
        };

        chip50.setTag("50");   chip50.setOnClickListener(chipClick);
        chip100.setTag("100"); chip100.setOnClickListener(chipClick);
        chip250.setTag("250"); chip250.setOnClickListener(chipClick);
        chip500.setTag("500"); chip500.setOnClickListener(chipClick);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // ── Confirm: save + deep-link ─────────────────────────────────────────
        btnConfirm.setOnClickListener(v -> {
            String amountStr = etAmount.getText().toString().trim();
            if (amountStr.isEmpty()) {
                tvError.setText("Please enter a donation amount.");
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                tvError.setText("Please enter a valid amount.");
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            if (amount <= 0) {
                tvError.setText("Amount must be greater than ₱0.");
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            tvError.setVisibility(View.GONE);
            btnConfirm.setEnabled(false);
            btnConfirm.setText("Recording...");

            // Step 1: Save as Completed in Supabase
            donationRepo.recordDonation(disaster.getId(), amount,
                    (donationId) -> runOnUiThread(() -> {
                        // Step 2: Deep-link into GCash
                        dialog.dismiss();
                        openGCashDeepLink(disaster.getGcashNumber(), amount);
                        // Refresh donations tab (if open)
                        Toast.makeText(this,
                                "Donation of ₱" + String.format("%.2f", amount) + " recorded! Opening GCash...",
                                Toast.LENGTH_LONG).show();
                    }),
                    (err) -> runOnUiThread(() -> {
                        btnConfirm.setEnabled(true);
                        btnConfirm.setText("💙  Donate via GCash");
                        tvError.setText("Failed to record donation. Please try again.");
                        tvError.setVisibility(View.VISIBLE);
                    })
            );
        });

        dialog.show();
    }

    /**
     * GCash deep-link format:
     *   gcash://send?phone=09XXXXXXXXX&amount=100
     *
     * If GCash is not installed, falls back to Play Store.
     * On mobile, this navigates directly into the recipient's GCash profile —
     * the key advantage over web (web can only copy the number).
     */
    private void openGCashDeepLink(String gcashNumber, double amount) {
        if (gcashNumber == null || gcashNumber.isEmpty()) {
            Toast.makeText(this, "No GCash number available.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Format amount as integer string if it's a whole number
        String amountStr = amount == Math.floor(amount)
                ? String.valueOf((int) amount)
                : String.format("%.2f", amount);

        String deepLinkUrl = "gcash://send?phone=" + gcashNumber + "&amount=" + amountStr;

        try {
            Intent gcashIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(deepLinkUrl));
            gcashIntent.setPackage("com.globe.gcash.android");

            if (gcashIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(gcashIntent);
                return;
            }
        } catch (Exception ignored) {}

        // Fallback: try without package restriction
        try {
            Intent fallbackIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(deepLinkUrl));
            if (fallbackIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(fallbackIntent);
                return;
            }
        } catch (Exception ignored) {}

        // Last resort: open Play Store for GCash
        Toast.makeText(this,
                "GCash app not found. Donation recorded. GCash number: " + gcashNumber,
                Toast.LENGTH_LONG).show();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=com.globe.gcash.android")));
        } catch (Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=com.globe.gcash.android")));
        }
    }

    // ── Bell Notification Panel ───────────────────────────────────────────────

    /**
     * Shows the mute notification panel as a PopupWindow anchored below the bell icon.
     * Mirrors web MuteNotificationBell.js panel.
     */
    private void showBellPanel(View anchor) {
        if (bellPopup != null && bellPopup.isShowing()) {
            bellPopup.dismiss();
            return;
        }

        View panelView = LayoutInflater.from(this)
                .inflate(R.layout.layout_mute_panel, null);

        boolean isMuted = Boolean.TRUE.equals(viewModel.isMuted.getValue());

        LinearLayout layoutMuteItem = panelView.findViewById(R.id.layoutMuteItem);
        LinearLayout layoutEmpty    = panelView.findViewById(R.id.layoutBellEmpty);

        if (isMuted) {
            layoutMuteItem.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);

            // Mute reason
            LinearLayout layoutReason = panelView.findViewById(R.id.layoutMuteReason);
            TextView     tvReason     = panelView.findViewById(R.id.tvMuteReasonText);
            String reason = viewModel.muteReason.getValue();
            if (reason != null && !reason.isEmpty()) {
                layoutReason.setVisibility(View.VISIBLE);
                tvReason.setText(reason);
            }

            // Mute until
            TextView tvUntil = panelView.findViewById(R.id.tvMuteUntil);
            String muteUntil = viewModel.muteUntil.getValue();
            if (muteUntil != null && !muteUntil.isEmpty()) {
                tvUntil.setText("Restriction lifts on " + formatDate(muteUntil));
            } else {
                tvUntil.setText("Restriction is indefinite — contact an admin.");
            }
        } else {
            layoutMuteItem.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        }

        // Close button inside panel
        TextView tvClose = panelView.findViewById(R.id.tvBellPanelClose);

        int widthPx = (int) (280 * getResources().getDisplayMetrics().density);
        bellPopup = new PopupWindow(panelView,
                widthPx, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        bellPopup.setElevation(16f);
        bellPopup.setOutsideTouchable(true);
        bellPopup.setFocusable(true);

        tvClose.setOnClickListener(v -> bellPopup.dismiss());

        bellPopup.showAsDropDown(anchor, 0, 8, Gravity.END);
    }

    // ── Delete Confirm ────────────────────────────────────────────────────────

    private void confirmDelete(Disaster d) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Disaster Point")
                .setMessage("Delete \"" + d.getTitle() + "\"? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteDisaster(d.getId());
                    disasterDetailPanel.setVisibility(View.GONE);
                    selectedDisaster = null;
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Style Helpers ─────────────────────────────────────────────────────────

    private void applySeverityBadge(TextView tv, String s) {
        if (s == null) return;
        tv.setText(capitalize(s));
        switch (s.toLowerCase()) {
            case "critical":
                tv.setTextColor(getColor(R.color.colorSeverityCritical));
                tv.setBackgroundResource(R.drawable.bg_badge_critical); break;
            case "high":
                tv.setTextColor(0xFFFFFFFF);
                tv.setBackgroundResource(R.drawable.bg_badge_high); break;
            case "medium":
                tv.setTextColor(getColor(R.color.colorSeverityMedium));
                tv.setBackgroundResource(R.drawable.bg_badge_medium); break;
            default:
                tv.setTextColor(getColor(R.color.colorSeverityLow));
                tv.setBackgroundResource(R.drawable.bg_badge_low);
        }
    }

    private void applyStatusBadge(TextView tv, String s) {
        if (s == null) return;
        tv.setText(capitalize(s));
        switch (s.toLowerCase()) {
            case "active":
                tv.setTextColor(getColor(R.color.colorSuccess));
                tv.setBackgroundResource(R.drawable.bg_badge_active); break;
            case "resolved":
                tv.setTextColor(getColor(R.color.colorPrimary));
                tv.setBackgroundResource(R.drawable.bg_badge_green); break;
            default:
                tv.setTextColor(getColor(R.color.colorTextMuted));
                tv.setBackgroundResource(R.drawable.bg_badge_gray);
        }
    }

    private void resetChipStyle(TextView chip) {
        chip.setBackgroundResource(R.drawable.bg_card);
        chip.setTextColor(getColor(R.color.colorTextSecondary));
    }

    private void highlightChip(TextView chip) {
        chip.setBackgroundResource(R.drawable.bg_btn_navy);
        chip.setTextColor(0xFFFFFFFF);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    private String formatDate(String iso) {
        if (iso == null || iso.isEmpty()) return "—";
        try {
            java.text.SimpleDateFormat in =
                    new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",
                            java.util.Locale.getDefault());
            java.text.SimpleDateFormat out =
                    new java.text.SimpleDateFormat("MMM d, yyyy 'at' h:mm a",
                            java.util.Locale.getDefault());
            return out.format(in.parse(iso));
        } catch (Exception e) {
            return iso.length() >= 10 ? iso.substring(0, 10) : iso;
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override public void onResume() {
        super.onResume();
        mapView.onResume();
        // Refresh mute status every time user returns to map
        viewModel.checkMuteStatus();
    }

    @Override public void onPause() {
        super.onPause();
        mapView.onPause();
        if (bellPopup != null && bellPopup.isShowing()) bellPopup.dismiss();
    }
}