package edu.cit.labaya.disasteraidconnect.ui.admin.disasters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.List;
import edu.cit.labaya.disasteraidconnect.R;
import edu.cit.labaya.disasteraidconnect.data.model.Disaster;

public class DisasterAdminAdapter extends RecyclerView.Adapter<DisasterAdminAdapter.VH> {

    public interface OnDeleteClick  { void onClick(Disaster d); }
    public interface OnMapClick     { void onClick(Disaster d); }

    private List<Disaster> items;
    private final OnDeleteClick onDelete;
    private final OnMapClick onMap;

    public DisasterAdminAdapter(List<Disaster> items, OnDeleteClick onDelete, OnMapClick onMap) {
        this.items = items;
        this.onDelete = onDelete;
        this.onMap = onMap;
    }

    public void updateData(List<Disaster> data) {
        this.items = data;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_disaster_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Disaster d = items.get(pos);
        h.tvTitle.setText(d.getTitle());
        h.tvSeverity.setText(d.getSeverity() != null ? d.getSeverity() : "—");
        h.tvStatus.setText(d.getStatus() != null ? d.getStatus() : "—");
        h.tvReporter.setText(d.getReportedBy() != null ? d.getReportedBy() : "Unknown");
        h.tvReporterInitial.setText(
                d.getReportedBy() != null && !d.getReportedBy().isEmpty()
                        ? d.getReportedBy().substring(0, Math.min(2, d.getReportedBy().length())).toUpperCase()
                        : "??"
        );

        String coords = "";
        if (d.getLatitude() != 0 && d.getLongitude() != 0) {
            coords = String.format("%.4f, %.4f", d.getLatitude(), d.getLongitude());
        }
        h.tvCoordinates.setText(coords);
        h.tvDate.setText(d.getCreatedAt() != null ? d.getCreatedAt().substring(0, 10) : "—");

        // Severity badge color
        setSeverityColor(h.tvSeverity, d.getSeverity());
        setStatusColor(h.tvStatus, d.getStatus());

        h.btnDelete.setOnClickListener(v -> onDelete.onClick(d));
        h.btnViewOnMap.setOnClickListener(v -> onMap.onClick(d));
    }

    private void setSeverityColor(TextView tv, String severity) {
        if (severity == null) return;
        switch (severity.toLowerCase()) {
            case "critical": tv.setBackgroundResource(R.drawable.bg_badge_red); break;
            case "high":     tv.setBackgroundResource(R.drawable.bg_badge_red); break;
            case "medium":   tv.setBackgroundResource(R.drawable.bg_badge_orange); break;
            case "low":      tv.setBackgroundResource(R.drawable.bg_badge_green); break;
        }
    }

    private void setStatusColor(TextView tv, String status) {
        if (status == null) return;
        switch (status.toLowerCase()) {
            case "active":   tv.setBackgroundResource(R.drawable.bg_badge_green); break;
            case "resolved": tv.setBackgroundResource(R.drawable.bg_badge_gray); break;
            default:         tv.setBackgroundResource(R.drawable.bg_badge_gray); break;
        }
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSeverity, tvStatus, tvReporter, tvReporterInitial, tvCoordinates, tvDate;
        MaterialButton btnViewOnMap, btnDelete;
        VH(View v) {
            super(v);
            tvTitle           = v.findViewById(R.id.tvTitle);
            tvSeverity        = v.findViewById(R.id.tvSeverity);
            tvStatus          = v.findViewById(R.id.tvStatus);
            tvReporter        = v.findViewById(R.id.tvReporter);
            tvReporterInitial = v.findViewById(R.id.tvReporterInitial);
            tvCoordinates     = v.findViewById(R.id.tvCoordinates);
            tvDate            = v.findViewById(R.id.tvDate);
            btnViewOnMap      = v.findViewById(R.id.btnViewOnMap);
            btnDelete         = v.findViewById(R.id.btnDelete);
        }
    }
}