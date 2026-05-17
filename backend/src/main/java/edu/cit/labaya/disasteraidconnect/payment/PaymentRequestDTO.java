package edu.cit.labaya.disasteraidconnect.payment;

import java.math.BigDecimal;
import java.util.UUID;

public class PaymentRequestDTO {

    private UUID       userId;
    private UUID       disasterId;
    private BigDecimal amount;      // in PHP

    public UUID       getUserId()                  { return userId; }
    public void       setUserId(UUID userId)        { this.userId = userId; }

    public UUID       getDisasterId()               { return disasterId; }
    public void       setDisasterId(UUID d)         { this.disasterId = d; }

    public BigDecimal getAmount()                   { return amount; }
    public void       setAmount(BigDecimal amount)  { this.amount = amount; }
}