package edu.cit.labaya.disasteraidconnect.donation;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public class DonationRequestDTO {

    private UUID userId;
    private UUID disasterId;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public UUID getUserId()                    { return userId; }
    public void setUserId(UUID userId)         { this.userId = userId; }

    public UUID getDisasterId()                { return disasterId; }
    public void setDisasterId(UUID d)          { this.disasterId = d; }

    public BigDecimal getAmount()              { return amount; }
    public void       setAmount(BigDecimal a)  { this.amount = a; }
}