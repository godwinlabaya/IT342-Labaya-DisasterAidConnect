package edu.cit.labaya.disasteraidconnect.ui.admin.users;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.List;
import edu.cit.labaya.disasteraidconnect.R;
import edu.cit.labaya.disasteraidconnect.data.model.User;

public class UserAdminAdapter extends RecyclerView.Adapter<UserAdminAdapter.VH> {

    public interface OnMuteClick   { void onClick(User u); }
    public interface OnDeleteClick { void onClick(User u); }

    private List<User> items;
    private final OnMuteClick onMute;
    private final OnDeleteClick onDelete;

    public UserAdminAdapter(List<User> items, OnMuteClick onMute, OnDeleteClick onDelete) {
        this.items = items;
        this.onMute = onMute;
        this.onDelete = onDelete;
    }

    public void updateData(List<User> data) { this.items = data; notifyDataSetChanged(); }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        User u = items.get(pos);

        String name = u.getUsername() != null ? u.getUsername() : "Unknown";
        h.tvUsername.setText(name);
        h.tvEmail.setText(u.getEmail() != null ? u.getEmail() : "—");
        h.tvInitial.setText(name.length() >= 2
                ? name.substring(0, 2).toUpperCase() : name.toUpperCase());

        // Role badge
        String role = u.getRole() != null ? u.getRole() : "user";
        h.tvRole.setText(role);
        h.tvRole.setBackgroundResource(
                role.equals("admin") ? R.drawable.bg_badge_purple : R.drawable.bg_badge_gray);

        // Status
        boolean muted = u.isMuted();
        h.tvStatus.setText(muted ? "Muted" : "Active");
        h.tvStatus.setBackgroundResource(muted ? R.drawable.bg_badge_orange : R.drawable.bg_badge_green);

        // Joined date
        String joined = u.getCreatedAt();
        h.tvJoined.setText("Joined " + (joined != null && joined.length() >= 10
                ? joined.substring(0, 10) : "—"));

        // Mute info
        if (muted) {
            h.layoutMuteInfo.setVisibility(View.VISIBLE);
            h.tvMuteReason.setText("Reason: " + (u.getMuteReason() != null ? u.getMuteReason() : "—"));
            String until = u.getMuteUntil();
            h.tvMuteUntil.setText("Until: " + (until != null && until.length() >= 10
                    ? until.substring(0, 10) : "—"));
        } else {
            h.layoutMuteInfo.setVisibility(View.GONE);
        }

        // Hide mute/delete for admin role
        if (role.equals("admin")) {
            h.btnMute.setVisibility(View.GONE);
            h.btnDeleteUser.setVisibility(View.GONE);
        } else {
            h.btnMute.setVisibility(View.VISIBLE);
            h.btnDeleteUser.setVisibility(View.VISIBLE);
            h.btnMute.setText(muted ? "Unmute" : "Mute");
            h.btnMute.setOnClickListener(v -> onMute.onClick(u));
            h.btnDeleteUser.setOnClickListener(v -> onDelete.onClick(u));
        }
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvInitial, tvUsername, tvEmail, tvRole, tvStatus, tvJoined, tvMuteReason, tvMuteUntil;
        LinearLayout layoutMuteInfo;
        MaterialButton btnMute, btnDeleteUser;
        VH(View v) {
            super(v);
            tvInitial      = v.findViewById(R.id.tvInitial);
            tvUsername     = v.findViewById(R.id.tvUsername);
            tvEmail        = v.findViewById(R.id.tvEmail);
            tvRole         = v.findViewById(R.id.tvRole);
            tvStatus       = v.findViewById(R.id.tvStatus);
            tvJoined       = v.findViewById(R.id.tvJoined);
            tvMuteReason   = v.findViewById(R.id.tvMuteReason);
            tvMuteUntil    = v.findViewById(R.id.tvMuteUntil);
            layoutMuteInfo = v.findViewById(R.id.layoutMuteInfo);
            btnMute        = v.findViewById(R.id.btnMute);
            btnDeleteUser  = v.findViewById(R.id.btnDeleteUser);
        }
    }
}