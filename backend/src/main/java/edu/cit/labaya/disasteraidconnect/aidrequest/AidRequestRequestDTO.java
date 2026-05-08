package edu.cit.labaya.disasteraidconnect.aidrequest;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public class AidRequestRequestDTO {

    private UUID userId;
    private UUID disasterId;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Aid type is required")
    private String aidType;

    private String quantity;

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public UUID getUserId()                      { return userId; }
    public void setUserId(UUID userId)           { this.userId = userId; }

    public UUID getDisasterId()                  { return disasterId; }
    public void setDisasterId(UUID disasterId)   { this.disasterId = disasterId; }

    public String getDescription()               { return description; }
    public void   setDescription(String d)       { this.description = d; }

    public String getAidType()                   { return aidType; }
    public void   setAidType(String aidType)     { this.aidType = aidType; }

    public String getQuantity()                  { return quantity; }
    public void   setQuantity(String quantity)   { this.quantity = quantity; }
}