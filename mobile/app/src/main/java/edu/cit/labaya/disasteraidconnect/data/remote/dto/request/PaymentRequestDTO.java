package edu.cit.labaya.disasteraidconnect.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;

public class PaymentRequestDTO {

    @SerializedName("userId")
    private String userId;

    @SerializedName("disasterId")
    private String disasterId;

    private double amount;  // PHP amount (e.g. 100.00) — backend converts to centavos

    public PaymentRequestDTO(String userId, String disasterId, double amount) {
        this.userId     = userId;
        this.disasterId = disasterId;
        this.amount     = amount;
    }

    public String getUserId()     { return userId; }
    public String getDisasterId() { return disasterId; }
    public double getAmount()     { return amount; }
}
