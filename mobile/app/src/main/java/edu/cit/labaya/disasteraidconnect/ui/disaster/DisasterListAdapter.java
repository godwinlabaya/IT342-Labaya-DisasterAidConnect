package edu.cit.labaya.disasteraidconnect.ui.disaster;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import edu.cit.labaya.disasteraidconnect.R;
import edu.cit.labaya.disasteraidconnect.data.model.Disaster;

public class DisasterListAdapter extends RecyclerView.Adapter<DisasterListAdapter.VH> {

    public interface OnDisasterClick { void onClick(Disaster d); }

    private List<Disaster> items;
    private final OnDisasterClick listener;

    public DisasterListAdapter(List<Disaster> items, OnDisasterClick listener) {
        this.items = items;
        this.listener = listener;
    }

    public void updateList(List<Disaster> newList) {
        this.items = newList;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_map_disaster_row, parent, false);
        return new VH(v);
    }

    private void applySeverity(TextView tv, String s, View ctx) {
        if (s == null) return;
        switch (s.toLowerCase()) {
            case "critical":
                tv.setTextColor(ctx.getContext().getColor(R.color.colorSeverityCritical));
                tv.setBackgroundResource(R.drawable.bg_badge_critical); break;
            case "high":
                tv.setTextColor(ctx.getContext().getColor(R.color.colorSeverityHigh));
                tv.setBackgroundResource(R.drawable.bg_badge_high); break;
            case "medium":
                tv.setTextColor(ctx.getContext().getColor(R.color.colorSeverityMedium));
                tv.setBackgroundResource(R.drawable.bg_badge_medium); break;
            default:
                tv.setTextColor(ctx.getContext().getColor(R.color.colorSeverityLow));
                tv.setBackgroundResource(R.drawable.bg_badge_low);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int pos) {
        Disaster d = items.get(pos);
        holder.tvTitle.setText(d.getTitle());

        String dateStr = d.getCreatedAt() != null
                ? d.getCreatedAt().substring(0, Math.min(10, d.getCreatedAt().length())) : "—";
        holder.tvReporter.setText(
                (d.getReportedBy() != null ? d.getReportedBy() + " · " : "") + dateStr);

        holder.tvSeverity.setText(capitalize(d.getSeverity()));
        applySeverity(holder.tvSeverity, d.getSeverity(), holder.itemView);

        // Dot color via text color (item_map_disaster_row uses TextView not View)
        int dotColor;
        switch (d.getSeverity() == null ? "" : d.getSeverity().toLowerCase()) {
            case "critical": dotColor = holder.itemView.getContext().getColor(R.color.colorSeverityCritical); break;
            case "high":     dotColor = holder.itemView.getContext().getColor(R.color.colorSeverityHigh); break;
            case "medium":   dotColor = holder.itemView.getContext().getColor(R.color.colorSeverityMedium); break;
            default:         dotColor = holder.itemView.getContext().getColor(R.color.colorSeverityLow);
        }
        holder.tvDot.setTextColor(dotColor);

        holder.itemView.setOnClickListener(v -> listener.onClick(d));
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    @Override public int getItemCount() { return items == null ? 0 : items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvReporter, tvSeverity, tvDot;

        VH(View v) {
            super(v);
            tvTitle    = v.findViewById(R.id.tvTitle);
            tvReporter = v.findViewById(R.id.tvReporter); // was tvDate
            tvSeverity = v.findViewById(R.id.tvSeverity);
            tvDot      = v.findViewById(R.id.tvDot);      // was severityDot
        }
    }
}