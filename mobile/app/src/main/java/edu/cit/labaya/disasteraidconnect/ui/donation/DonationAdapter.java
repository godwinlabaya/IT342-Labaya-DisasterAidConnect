package edu.cit.labaya.disasteraidconnect.ui.donation;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import edu.cit.labaya.disasteraidconnect.R;
import edu.cit.labaya.disasteraidconnect.data.model.Donation;
import edu.cit.labaya.disasteraidconnect.ui.disaster.DisasterMapActivity;

/**
 * DonationAdapter — RecyclerView adapter for item_donation_card.xml
 *
 * Uses the EXACT view IDs from your existing item_donation_card.xml:
 *   tvDonationId, tvStatus, tvDonorInitial, tvDonor, tvAmount,
 *   tvDisasterName, tvDate, btnViewOnMap
 */
public class DonationAdapter extends RecyclerView.Adapter<DonationAdapter.ViewHolder> {

    private List<Donation> donations;
    private final String   username;

    public DonationAdapter(List<Donation> donations, String username) {
        this.donations = donations;
        this.username  = username;
    }

    public void updateList(List<Donation> newList) {
        this.donations = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_donation_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Donation d = donations.get(position);

        // ── Row 1: short ID + status badge ───────────────────────────────────
        String shortId = d.getId() != null && d.getId().length() >= 8
                ? d.getId().substring(0, 8) + "..." : (d.getId() != null ? d.getId() : "—");
        h.tvDonationId.setText(shortId);
        applyStatusBadge(h.tvStatus, d.getStatus());

        // ── Row 2: donor initials + name + amount ─────────────────────────────
        String uname = username != null && !username.isEmpty() ? username : "You";
        String initials = uname.length() >= 2
                ? uname.substring(0, 2).toUpperCase() : uname.toUpperCase();
        h.tvDonorInitial.setText(initials);
        h.tvDonor.setText(uname);
        h.tvAmount.setText(formatPeso(d.getAmount()));

        // ── Row 3: disaster name ──────────────────────────────────────────────
        h.tvDisasterName.setText(
                d.getDisasterTitle() != null ? d.getDisasterTitle() : "Unknown disaster");

        // ── Row 4: date + View on Map ─────────────────────────────────────────
        h.tvDate.setText(formatDate(d.getDonatedAt()));

        if (d.getDisasterId() != null && !d.getDisasterId().isEmpty()) {
            h.btnViewOnMap.setVisibility(View.VISIBLE);
            h.btnViewOnMap.setOnClickListener(v -> {
                // Navigate to map focused on this disaster
                // Mirrors web: navigate("/map", { state: { focusDisasterId: disaster.id } })
                Intent intent = new Intent(v.getContext(), DisasterMapActivity.class);
                intent.putExtra("focusDisasterId", d.getDisasterId());
                v.getContext().startActivity(intent);
            });
        } else {
            h.btnViewOnMap.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return donations != null ? donations.size() : 0; }

    // ── ViewHolder — matches item_donation_card.xml IDs exactly ──────────────

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView       tvDonationId, tvStatus, tvDonorInitial;
        TextView       tvDonor, tvAmount, tvDisasterName, tvDate;
        MaterialButton btnViewOnMap;

        ViewHolder(@NonNull View v) {
            super(v);
            tvDonationId  = v.findViewById(R.id.tvDonationId);
            tvStatus      = v.findViewById(R.id.tvStatus);
            tvDonorInitial = v.findViewById(R.id.tvDonorInitial);
            tvDonor       = v.findViewById(R.id.tvDonor);
            tvAmount      = v.findViewById(R.id.tvAmount);
            tvDisasterName = v.findViewById(R.id.tvDisasterName);
            tvDate        = v.findViewById(R.id.tvDate);
            btnViewOnMap  = v.findViewById(R.id.btnViewOnMap);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Status badge colors matching web getStatusStyle():
     *   Completed → green  | Failed → red
     *   Refunded  → blue   | Pending → amber
     */
    private void applyStatusBadge(TextView tv, String status) {
        if (status == null) status = "Pending";
        tv.setText(capitalize(status));
        switch (status) {
            case "Completed":
                tv.setBackgroundResource(R.drawable.bg_badge_active);
                tv.setTextColor(0xFF15803D); break;
            case "Failed":
                tv.setBackgroundResource(R.drawable.bg_badge_critical);
                tv.setTextColor(0xFFB91C1C); break;
            case "Refunded":
                tv.setBackgroundResource(R.drawable.bg_badge_low);
                tv.setTextColor(0xFF1D4ED8); break;
            default: // Pending — use bg_badge_orange if it exists, else bg_badge_medium
                tv.setBackgroundResource(R.drawable.bg_badge_medium);
                tv.setTextColor(0xFF92400E); break;
        }
    }

    private String formatPeso(double amount) {
        return String.format(Locale.getDefault(), "₱%.2f", amount);
    }

    private String formatDate(String iso) {
        if (iso == null || iso.isEmpty()) return "—";
        try {
            SimpleDateFormat in  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat out = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault());
            return out.format(in.parse(iso));
        } catch (Exception e) {
            return iso.length() >= 10 ? iso.substring(0, 10) : iso;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}