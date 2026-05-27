package edu.cit.labaya.disasteraidconnect.data.model;

import com.google.gson.annotations.SerializedName;

public class Payment {
    private String id;

    @SerializedName("donationId")
    private String donationId;

    @SerializedName("paymentDate")
    private String paymentDate;

    @SerializedName("paymentMethod")
    private String paymentMethod;

    @SerializedName("paymentStatus")
    private String paymentStatus;   // Pending | Completed | Failed

    @SerializedName("processingFee")
    private String processingFee;

    @SerializedName("totalAmount")
    private String totalAmount;

    @SerializedName("transactionReference")
    private String transactionReference;

    @SerializedName("paymentIntentId")
    private String paymentIntentId;

    public Payment() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDonationId() { return donationId; }
    public void setDonationId(String donationId) { this.donationId = donationId; }
    public String getPaymentDate() { return paymentDate; }
    public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getProcessingFee() { return processingFee; }
    public void setProcessingFee(String processingFee) { this.processingFee = processingFee; }
    public String getTotalAmount() { return totalAmount; }
    public void setTotalAmount(String totalAmount) { this.totalAmount = totalAmount; }
    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }
    public String getPaymentIntentId() { return paymentIntentId; }
    public void setPaymentIntentId(String paymentIntentId) { this.paymentIntentId = paymentIntentId; }
}
