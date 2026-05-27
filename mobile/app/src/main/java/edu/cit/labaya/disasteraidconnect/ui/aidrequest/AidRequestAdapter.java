package edu.cit.labaya.disasteraidconnect.ui.aidrequest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import edu.cit.labaya.disasteraidconnect.R;
import edu.cit.labaya.disasteraidconnect.data.model.AidRequest;

/**
 * AidRequestAdapter — displays the user's aid requests in AidRequestActivity.
 *
 * Uses item_aid_request_row.xml (create this layout — see below).
 * Each card shows: aid type badge, description, status badge, date.
 */
public class AidRequestAdapter extends RecyclerView.Adapter<AidRequestAdapter.VH> {

    private final List<AidRequest> items;

    public AidRequestAdapter(List<AidRequest> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_aid_request_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        AidRequest r = items.get(pos);

        // Aid type — Food | Water | Medical | Shelter
        h.tvAidType.setText(r.getAidType() != null ? r.getAidType() : "Request");

        // Description
        h.tvDescription.setText(r.getDescription() != null ? r.getDescription() : "");

        // Status badge
        applyStatusBadge(h.tvStatus, r.getStatus());

        // Date
        h.tvDate.setText(formatDate(r.getCreatedAt()));

        // Quantity (optional)
        if (r.getQuantity() != null && !r.getQuantity().isEmpty()) {
            h.tvQuantity.setVisibility(View.VISIBLE);
            h.tvQuantity.setText("Qty: " + r.getQuantity());
        } else {
            h.tvQuantity.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return items != null ? items.size() : 0; }

    // ── ViewHolder ────────────────────────────────────────────────────────────

    static class VH extends RecyclerView.ViewHolder {
        TextView tvAidType, tvDescription, tvStatus, tvDate, tvQuantity;

        VH(View v) {
            super(v);
            tvAidType     = v.findViewById(R.id.tvAidType);
            tvDescription = v.findViewById(R.id.tvDescription);
            tvStatus      = v.findViewById(R.id.tvStatus);
            tvDate        = v.findViewById(R.id.tvDate);
            tvQuantity    = v.findViewById(R.id.tvQuantity);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Status badge colors:
     *   Pending   → amber
     *   Approved  → blue
     *   Fulfilled → green
     *   Rejected  → red
     */
    private void applyStatusBadge(TextView tv, String status) {
        if (status == null) status = "Pending";
        tv.setText(status);
        switch (status) {
            case "Approved":
                tv.setTextColor(0xFF1D4ED8);
                tv.setBackgroundResource(R.drawable.bg_badge_low); break;
            case "Fulfilled":
                tv.setTextColor(0xFF15803D);
                tv.setBackgroundResource(R.drawable.bg_badge_active); break;
            case "Rejected":
                tv.setTextColor(0xFFB91C1C);
                tv.setBackgroundResource(R.drawable.bg_badge_critical); break;
            default: // Pending
                tv.setTextColor(0xFF92400E);
                tv.setBackgroundResource(R.drawable.bg_badge_medium); break;
        }
    }

    private String formatDate(String iso) {
        if (iso == null || iso.isEmpty()) return "—";
        try {
            SimpleDateFormat in  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat out = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            return out.format(in.parse(iso));
        } catch (Exception e) {
            return iso.length() >= 10 ? iso.substring(0, 10) : iso;
        }
    }
}