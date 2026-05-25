package edu.cit.labaya.disasteraidconnect.payment;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "donation_id", columnDefinition = "uuid")
    private UUID donationId;

    @Column(name = "payment_date",
            columnDefinition = "timestamp with time zone default now()")
    private OffsetDateTime paymentDate;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @Column(name = "payment_status", nullable = false)
    private String paymentStatus = "Pending";

    @Column(name = "processing_fee", precision = 10, scale = 2)
    private BigDecimal processingFee = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "transaction_reference", unique = true)
    private String transactionReference;

    @Column(name = "payment_intent_id")
    private String paymentIntentId;

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public UUID getId()                                        { return id; }
    public void setId(UUID id)                                 { this.id = id; }

    public UUID getDonationId()                                { return donationId; }
    public void setDonationId(UUID donationId)                 { this.donationId = donationId; }

    public OffsetDateTime getPaymentDate()                     { return paymentDate; }
    public void           setPaymentDate(OffsetDateTime t)     { this.paymentDate = t; }

    public String getPaymentMethod()                           { return paymentMethod; }
    public void   setPaymentMethod(String m)                   { this.paymentMethod = m; }

    public String getPaymentStatus()                           { return paymentStatus; }
    public void   setPaymentStatus(String s)                   { this.paymentStatus = s; }

    public BigDecimal getProcessingFee()                       { return processingFee; }
    public void       setProcessingFee(BigDecimal fee)         { this.processingFee = fee; }

    public BigDecimal getTotalAmount()                         { return totalAmount; }
    public void       setTotalAmount(BigDecimal totalAmount)   { this.totalAmount = totalAmount; }

    public String getTransactionReference()                    { return transactionReference; }
    public void   setTransactionReference(String ref)          { this.transactionReference = ref; }

    public String getPaymentIntentId()                         { return paymentIntentId; }
    public void   setPaymentIntentId(String paymentIntentId)   { this.paymentIntentId = paymentIntentId; }
}