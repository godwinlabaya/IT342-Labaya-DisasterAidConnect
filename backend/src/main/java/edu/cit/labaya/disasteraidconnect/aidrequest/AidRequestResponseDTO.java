package edu.cit.labaya.disasteraidconnect.aidrequest;

import java.time.OffsetDateTime;
import java.util.UUID;

public class AidRequestResponseDTO {

    private UUID          id;
    private UUID          userId;
    private UUID          disasterId;
    private String        description;
    private String        aidType;
    private String        quantity;
    private String        status;
    private OffsetDateTime createdAt;

    // ── Factory ────────────────────────────────────────────────────────────────

    public static AidRequestResponseDTO from(AidRequest a) {
        AidRequestResponseDTO dto = new AidRequestResponseDTO();
        dto.id          = a.getId();
        dto.userId      = a.getUserId();
        dto.disasterId  = a.getDisasterId();
        dto.description = a.getDescription();
        dto.aidType     = a.getAidType();
        dto.quantity    = a.getQuantity();
        dto.status      = a.getStatus();
        dto.createdAt   = a.getCreatedAt();
        return dto;
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    public UUID           getId()          { return id; }
    public UUID           getUserId()      { return userId; }
    public UUID           getDisasterId()  { return disasterId; }
    public String         getDescription() { return description; }
    public String         getAidType()     { return aidType; }
    public String         getQuantity()    { return quantity; }
    public String         getStatus()      { return status; }
    public OffsetDateTime getCreatedAt()   { return createdAt; }
}