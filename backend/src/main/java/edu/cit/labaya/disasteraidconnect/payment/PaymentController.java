package edu.cit.labaya.disasteraidconnect.payment;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.cit.labaya.disasteraidconnect.donation.Donation;
import edu.cit.labaya.disasteraidconnect.donation.DonationRepository;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = { "http://localhost:3000", "https://evasion-collected-happening.ngrok-free.dev" })
public class PaymentController {

    private final PayMongoService    payMongoService;
    private final DonationRepository donationRepo;
    private final PaymentRepository  paymentRepo;

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

    @PostMapping("/create")
    public ResponseEntity<?> createCheckout(@RequestBody PaymentRequestDTO dto) {
        try {
            Donation donation = new Donation();
            donation.setUserId(dto.getUserId());
            donation.setDisasterId(dto.getDisasterId());
            donation.setAmount(dto.getAmount());
            donation.setStatus("Pending");
            Donation saved = donationRepo.save(donation);

            String disasterTitle = saved.getDisasterId() != null
                ? saved.getDisasterId().toString()
                : "Disaster Relief";

            Map<String, String> result = payMongoService.createGCashCheckout(
                dto.getAmount(),
                saved.getId().toString(),
                disasterTitle
            );

            Payment payment = new Payment();
            payment.setDonationId(saved.getId());
            payment.setPaymentMethod("GCash");
            payment.setPaymentStatus("Pending");
            payment.setTotalAmount(dto.getAmount());
            payment.setProcessingFee(BigDecimal.ZERO);
            payment.setTransactionReference(result.get("sessionId"));
            payment.setPaymentIntentId(result.get("paymentIntentId")); // ← store it
            paymentRepo.save(payment);

            return ResponseEntity.ok(Map.of(
                "checkoutUrl", result.get("checkoutUrl"),
                "donationId",  saved.getId().toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "Paymongo-Signature", required = false) String signature
    ) {
        try {
            System.out.println("=== WEBHOOK RECEIVED ===");

            Map<String, Object> data       = (Map<String, Object>) payload.get("data");
            Map<String, Object> attributes = (Map<String, Object>) data.get("attributes");
            String eventType               = (String) attributes.get("type");
            Map<String, Object> eventData  = (Map<String, Object>) attributes.get("data");
            Map<String, Object> eventAttrs = (Map<String, Object>) eventData.get("attributes");

            System.out.println("EVENT TYPE: " + eventType);

            String paymentIntentId = (String) eventAttrs.get("payment_intent_id");
            System.out.println("PAYMENT INTENT ID: " + paymentIntentId);

            if (paymentIntentId == null) {
                System.out.println("No payment_intent_id — skipping");
                return ResponseEntity.ok().build();
            }

            // Look up payment by payment_intent_id stored in our DB
            paymentRepo.findByPaymentIntentId(paymentIntentId).ifPresentOrElse(payment -> {
                System.out.println("PAYMENT FOUND for intent: " + paymentIntentId);

                payment.setPaymentStatus("payment.paid".equals(eventType) ? "Completed" : "Failed");
                paymentRepo.save(payment);
                System.out.println("PAYMENT UPDATED to: " + payment.getPaymentStatus());

                donationRepo.findById(payment.getDonationId()).ifPresentOrElse(donation -> {
                    donation.setStatus("payment.paid".equals(eventType) ? "Completed" : "Failed");
                    donationRepo.save(donation);
                    System.out.println("DONATION UPDATED to: " + donation.getStatus());
                }, () -> System.out.println("NO DONATION FOUND for id: " + payment.getDonationId()));

            }, () -> System.out.println("NO PAYMENT FOUND for intent: " + paymentIntentId));

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Webhook error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok().build();
        }
    }
}