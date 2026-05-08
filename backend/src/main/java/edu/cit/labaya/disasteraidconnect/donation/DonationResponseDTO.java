package edu.cit.labaya.disasteraidconnect.donation;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class DonationResponseDTO {

    private UUID          id;
    private UUID          userId;
    private UUID          disasterId;
    private BigDecimal    amount;
    private String        status;
    private OffsetDateTime donatedAt;

    public static DonationResponseDTO from(Donation d) {
        DonationResponseDTO dto = new DonationResponseDTO();
        dto.id         = d.getId();
        dto.userId     = d.getUserId();
        dto.disasterId = d.getDisasterId();
        dto.amount     = d.getAmount();
        dto.status     = d.getStatus();
        dto.donatedAt  = d.getDonatedAt();
        return dto;
    }

    public UUID           getId()          { return id; }
    public UUID           getUserId()      { return userId; }
    public UUID           getDisasterId()  { return disasterId; }
    public BigDecimal     getAmount()      { return amount; }
    public String         getStatus()      { return status; }
    public OffsetDateTime getDonatedAt()   { return donatedAt; }
}