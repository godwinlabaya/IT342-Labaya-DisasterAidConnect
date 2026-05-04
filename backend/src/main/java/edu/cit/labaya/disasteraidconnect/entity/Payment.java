package edu.cit.labaya.disasteraidconnect.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "donation_id", columnDefinition = "uuid")
    private UUID donationId;        // FK → donations(id), CASCADE on delete

    @Column(name = "payment_date",
            columnDefinition = "timestamp with time zone default now()")
    private OffsetDateTime paymentDate;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;   // e.g. GCash, PayMaya, Credit Card

    @Column(name = "payment_status", nullable = false)
    private String paymentStatus = "Pending";  // Pending | Completed | Failed | Refunded

    @Column(name = "processing_fee", precision = 10, scale = 2)
    private BigDecimal processingFee = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;  // must be > 0

    @Column(name = "transaction_reference", unique = true)
    private String transactionReference;

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public UUID getId()                                        { return id; }
    public void setId(UUID id)                                 { this.id = id; }

    public UUID getDonationId()                                { return donationId; }
    public void setDonationId(UUID donationId)                 { this.donationId = donationId; }

    public OffsetDateTime getPaymentDate()                     { return paymentDate; }
    public void           setPaymentDate(OffsetDateTime t)     { this.paymentDate = t; }

    public String getPaymentMethod()                           { return paymentMethod; }
    public void   setPaymentMethod(String paymentMethod)       { this.paymentMethod = paymentMethod; }

    public String getPaymentStatus()                           { return paymentStatus; }
    public void   setPaymentStatus(String paymentStatus)       { this.paymentStatus = paymentStatus; }

    public BigDecimal getProcessingFee()                       { return processingFee; }
    public void       setProcessingFee(BigDecimal fee)         { this.processingFee = fee; }

    public BigDecimal getTotalAmount()                         { return totalAmount; }
    public void       setTotalAmount(BigDecimal totalAmount)   { this.totalAmount = totalAmount; }

    public String getTransactionReference()                    { return transactionReference; }
    public void   setTransactionReference(String ref)          { this.transactionReference = ref; }
}