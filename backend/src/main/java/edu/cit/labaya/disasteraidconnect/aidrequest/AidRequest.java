package edu.cit.labaya.disasteraidconnect.aidrequest;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "aid_requests")
public class AidRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "disaster_id", columnDefinition = "uuid")
    private UUID disasterId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "aid_type", nullable = false)
    private String aidType;         // Food | Water | Medical | Shelter

    @Column
    private String quantity;

    @Column(nullable = false)
    private String status = "Pending";  // Pending | Approved | Fulfilled | Rejected

    @Column(name = "created_at", updatable = false,
            columnDefinition = "timestamp with time zone default now()")
    private OffsetDateTime createdAt;

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public UUID getId()                              { return id; }
    public void setId(UUID id)                       { this.id = id; }

    public UUID getUserId()                          { return userId; }
    public void setUserId(UUID userId)               { this.userId = userId; }

    public UUID getDisasterId()                      { return disasterId; }
    public void setDisasterId(UUID disasterId)       { this.disasterId = disasterId; }

    public String getDescription()                   { return description; }
    public void   setDescription(String d)           { this.description = d; }

    public String getAidType()                       { return aidType; }
    public void   setAidType(String aidType)         { this.aidType = aidType; }

    public String getQuantity()                      { return quantity; }
    public void   setQuantity(String quantity)       { this.quantity = quantity; }

    public String getStatus()                        { return status; }
    public void   setStatus(String status)           { this.status = status; }

    public OffsetDateTime getCreatedAt()             { return createdAt; }
    public void           setCreatedAt(OffsetDateTime t) { this.createdAt = t; }
}