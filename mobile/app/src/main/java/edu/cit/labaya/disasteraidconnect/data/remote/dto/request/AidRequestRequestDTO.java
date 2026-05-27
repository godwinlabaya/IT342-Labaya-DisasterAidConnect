package edu.cit.labaya.disasteraidconnect.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;

public class AidRequestRequestDTO {

    @SerializedName("userId")
    private String userId;

    @SerializedName("disasterId")
    private String disasterId;

    private String description;

    @SerializedName("aidType")
    private String aidType;   // Food | Water | Medical | Shelter

    private String quantity;

    public AidRequestRequestDTO(String userId, String disasterId,
                                 String description, String aidType, String quantity) {
        this.userId      = userId;
        this.disasterId  = disasterId;
        this.description = description;
        this.aidType     = aidType;
        this.quantity    = quantity;
    }

    public String getUserId()      { return userId; }
    public String getDisasterId()  { return disasterId; }
    public String getDescription() { return description; }
    public String getAidType()     { return aidType; }
    public String getQuantity()    { return quantity; }
}
