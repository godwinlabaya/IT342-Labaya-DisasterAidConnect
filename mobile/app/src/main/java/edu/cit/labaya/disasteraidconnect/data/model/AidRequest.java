package edu.cit.labaya.disasteraidconnect.data.model;

import com.google.gson.annotations.SerializedName;

public class AidRequest {
    private String id;

    @SerializedName("userId")
    private String userId;

    @SerializedName("disasterId")
    private String disasterId;

    private String description;

    @SerializedName("aidType")
    private String aidType;       // Food | Water | Medical | Shelter

    private String quantity;
    private String status;         // Pending | Approved | Fulfilled | Rejected

    @SerializedName("createdAt")
    private String createdAt;

    public AidRequest() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getDisasterId() { return disasterId; }
    public void setDisasterId(String disasterId) { this.disasterId = disasterId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAidType() { return aidType; }
    public void setAidType(String aidType) { this.aidType = aidType; }
    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
