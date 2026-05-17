package edu.cit.labaya.disasteraidconnect.payment;

import edu.cit.labaya.disasteraidconnect.donation.Donation;
import edu.cit.labaya.disasteraidconnect.donation.DonationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = { "http://localhost:3000" })
public class PaymentController {

    private final PayMongoService      payMongoService;
    private final DonationRepository   donationRepo;
    private final PaymentRepository    paymentRepo;

    @Value("${paymongo.webhook-secret}")
    private String webhookSecret;

    public PaymentController(
            PayMongoService payMongoService,
            DonationRepository donationRepo,
            PaymentRepository paymentRepo
    ) {
        this.payMongoService = payMongoService;
        this.donationRepo    = donationRepo;
        this.paymentRepo     = paymentRepo;
    }

    // ── POST /api/payments/create ─────────────────────────────────────────────
    // Called by frontend when user clicks DONATE and enters amount
    @PostMapping("/create")
    public ResponseEntity<?> createCheckout(@RequestBody PaymentRequestDTO dto) {
        try {
            // 1. Save donation record as Pending
            Donation donation = new Donation();
            donation.setUserId(dto.getUserId());
            donation.setDisasterId(dto.getDisasterId());
            donation.setAmount(dto.getAmount());
            donation.setStatus("Pending");
            Donation saved = donationRepo.save(donation);

            // 2. Get disaster title for checkout description
            String disasterTitle = saved.getDisasterId() != null
                ? saved.getDisasterId().toString()
                : "Disaster Relief";

            // 3. Create PayMongo checkout session
            Map<String, String> result = payMongoService.createGCashCheckout(
                dto.getAmount(),
                saved.getId().toString(),
                disasterTitle
            );

            // 4. Save payment record
            Payment payment = new Payment();
            payment.setDonationId(saved.getId());
            payment.setPaymentMethod("GCash");
            payment.setPaymentStatus("Pending");
            payment.setTotalAmount(dto.getAmount());
            payment.setProcessingFee(BigDecimal.ZERO);
            payment.setTransactionReference(result.get("sessionId"));
            paymentRepo.save(payment);

            // 5. Return checkout URL to frontend
            return ResponseEntity.ok(Map.of(
                "checkoutUrl", result.get("checkoutUrl"),
                "donationId",  saved.getId().toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── POST /api/payments/webhook ────────────────────────────────────────────
    // Called by PayMongo when payment is paid or failed
    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "Paymongo-Signature", required = false) String signature
    ) {
        try {
            Map<String, Object> data       = (Map<String, Object>) payload.get("data");
            Map<String, Object> attributes = (Map<String, Object>) data.get("attributes");
            String eventType               = (String) attributes.get("type");

            Map<String, Object> eventData  = (Map<String, Object>) attributes.get("data");
            Map<String, Object> eventAttrs = (Map<String, Object>) eventData.get("attributes");
            String referenceNumber         = (String) eventAttrs.get("reference_number");

            if (referenceNumber == null) return ResponseEntity.ok().build();

            UUID donationId = UUID.fromString(referenceNumber);

            // Update donation status based on event
            donationRepo.findById(donationId).ifPresent(donation -> {
                if ("payment.paid".equals(eventType)) {
                    donation.setStatus("Completed");
                } else if ("payment.failed".equals(eventType)) {
                    donation.setStatus("Failed");
                }
                donationRepo.save(donation);

                // Update payment record too
                paymentRepo.findByTransactionReference(referenceNumber).ifPresent(payment -> {
                    if ("payment.paid".equals(eventType)) {
                        payment.setPaymentStatus("Completed");
                    } else {
                        payment.setPaymentStatus("Failed");
                    }
                    paymentRepo.save(payment);
                });
            });

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Webhook error: " + e.getMessage());
            return ResponseEntity.ok().build(); // Always return 200 to PayMongo
        }
    }
}