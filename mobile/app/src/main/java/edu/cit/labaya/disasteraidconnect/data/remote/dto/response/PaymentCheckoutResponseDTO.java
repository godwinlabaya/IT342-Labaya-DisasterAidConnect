package edu.cit.labaya.disasteraidconnect.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;

public class PaymentCheckoutResponseDTO {

    @SerializedName("checkoutUrl")
    private String checkoutUrl;

    @SerializedName("donationId")
    private String donationId;

    public String getCheckoutUrl() { return checkoutUrl; }
    public String getDonationId()  { return donationId; }
}
