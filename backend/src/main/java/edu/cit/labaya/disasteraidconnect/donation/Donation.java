package edu.cit.labaya.disasteraidconnect.donation;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "donations")
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "disaster_id", columnDefinition = "uuid")
    private UUID disasterId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String status = "Pending";  // Pending | Completed | Failed | Refunded

    @Column(name = "donated_at", updatable = false,
            columnDefinition = "timestamp with time zone default now()")
    private OffsetDateTime donatedAt;

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public UUID getId()                              { return id; }
    public void setId(UUID id)                       { this.id = id; }

    public UUID getUserId()                          { return userId; }
    public void setUserId(UUID userId)               { this.userId = userId; }

    public UUID getDisasterId()                      { return disasterId; }
    public void setDisasterId(UUID disasterId)       { this.disasterId = disasterId; }

    public BigDecimal getAmount()                    { return amount; }
    public void       setAmount(BigDecimal amount)   { this.amount = amount; }

    public String getStatus()                        { return status; }
    public void   setStatus(String status)           { this.status = status; }

    public OffsetDateTime getDonatedAt()             { return donatedAt; }
    public void           setDonatedAt(OffsetDateTime t) { this.donatedAt = t; }
}