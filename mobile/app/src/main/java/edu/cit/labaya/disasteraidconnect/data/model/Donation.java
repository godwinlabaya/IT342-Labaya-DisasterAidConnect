package edu.cit.labaya.disasteraidconnect.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Mirrors the Supabase donations table:
 *   id, user_id, disaster_id, amount, status, donated_at
 *
 * Extra fields (disasterTitle, donorUsername) are set from join queries.
 */
public class Donation {

    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("disaster_id")
    private String disasterId;

    private double amount;
    private String status;

    @SerializedName("donated_at")
    private String donatedAt;

    // Set from join: disasters(title)
    private String disasterTitle;

    // Set from join: users(username) — admin view only
    private String donorUsername;

    public Donation() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getDisasterId() { return disasterId; }
    public void setDisasterId(String disasterId) { this.disasterId = disasterId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDonatedAt() { return donatedAt; }
    public void setDonatedAt(String donatedAt) { this.donatedAt = donatedAt; }

    public String getDisasterTitle() { return disasterTitle; }
    public void setDisasterTitle(String disasterTitle) { this.disasterTitle = disasterTitle; }

    public String getDonorUsername() { return donorUsername; }
    public void setDonorUsername(String donorUsername) { this.donorUsername = donorUsername; }

    // Add these two lines anywhere after the existing getDonorUsername() getter
    public String getDonorName() { return donorUsername; }
    public void setDonorName(String name) { this.donorUsername = name; }

    /** Formatted amount string e.g. "₱ 500.00" */
    public String getFormattedAmount() {
        return String.format("₱ %.2f", amount);
    }

    /** Short date from donated_at ISO string */
    public String getFormattedDate() {
        if (donatedAt == null || donatedAt.length() < 10) return "—";
        return donatedAt.substring(0, 10);
    }
}