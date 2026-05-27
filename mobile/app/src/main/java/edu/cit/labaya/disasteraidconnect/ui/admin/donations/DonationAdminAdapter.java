package edu.cit.labaya.disasteraidconnect.ui.admin.donations;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.List;
import edu.cit.labaya.disasteraidconnect.R;
import edu.cit.labaya.disasteraidconnect.data.model.Donation;

public class DonationAdminAdapter extends RecyclerView.Adapter<DonationAdminAdapter.VH> {

    private List<Donation> items;

    public DonationAdminAdapter(List<Donation> items) { this.items = items; }

    public void updateData(List<Donation> data) { this.items = data; notifyDataSetChanged(); }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_donation_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Donation d = items.get(pos);

        // Truncate ID
        String id = d.getId() != null ? d.getId() : "—";
        h.tvDonationId.setText(id.length() > 10 ? id.substring(0, 10) + "..." : id);

        String status = d.getStatus() != null ? d.getStatus() : "—";
        h.tvStatus.setText(capitalize(status));
        setStatusBadge(h.tvStatus, status);

        String donor = d.getDonorName() != null ? d.getDonorName() : "Unknown";
        h.tvDonor.setText(donor);
        h.tvDonorInitial.setText(donor.length() >= 2
                ? donor.substring(0, 2).toUpperCase() : donor.toUpperCase());

        h.tvAmount.setText(String.format("₱%.2f", d.getAmount()));
        h.tvDisasterName.setText(d.getDisasterTitle() != null ? d.getDisasterTitle() : "—");

        String date = d.getDonatedAt();
        h.tvDate.setText(date != null && date.length() >= 10 ? date.substring(0, 10) : "—");

        h.btnViewOnMap.setOnClickListener(v -> { /* open map at disaster location */ });
    }

    private void setStatusBadge(TextView tv, String status) {
        switch (status.toLowerCase()) {
            case "completed": tv.setBackgroundResource(R.drawable.bg_badge_green); break;
            case "pending":   tv.setBackgroundResource(R.drawable.bg_badge_orange); break;
            case "failed":    tv.setBackgroundResource(R.drawable.bg_badge_red); break;
            default:          tv.setBackgroundResource(R.drawable.bg_badge_gray); break;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDonationId, tvStatus, tvDonor, tvDonorInitial, tvAmount, tvDisasterName, tvDate;
        MaterialButton btnViewOnMap;
        VH(View v) {
            super(v);
            tvDonationId   = v.findViewById(R.id.tvDonationId);
            tvStatus       = v.findViewById(R.id.tvStatus);
            tvDonor        = v.findViewById(R.id.tvDonor);
            tvDonorInitial = v.findViewById(R.id.tvDonorInitial);
            tvAmount       = v.findViewById(R.id.tvAmount);
            tvDisasterName = v.findViewById(R.id.tvDisasterName);
            tvDate         = v.findViewById(R.id.tvDate);
            btnViewOnMap   = v.findViewById(R.id.btnViewOnMap);
        }
    }
}