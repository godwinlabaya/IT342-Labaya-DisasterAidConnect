package edu.cit.labaya.disasteraidconnect.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PayMongoService {

    @Value("${paymongo.secret-key}")
    private String secretKey;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.backend-url}")
    private String backendUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String PAYMONGO_BASE = "https://api.paymongo.com/v1";

    public Map<String, String> createGCashCheckout(
            BigDecimal amount,
            String donationId,
            String disasterTitle
    ) {
        long amountCentavos = amount.multiply(BigDecimal.valueOf(100)).longValue();

        Map<String, Object> lineItem = new HashMap<>();
        lineItem.put("currency", "PHP");
        lineItem.put("amount", amountCentavos);
        lineItem.put("name", "Donation for: " + disasterTitle);
        lineItem.put("quantity", 1);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("billing", Map.of(
            "name", "Donor",
            "email", "donor@disasteraidconnect.com"
        ));
        attributes.put("line_items", List.of(lineItem));
        attributes.put("payment_method_types", List.of("gcash"));
        attributes.put("send_email_receipt", false);
        attributes.put("show_description", true);
        attributes.put("show_line_items", true);
        attributes.put("description", "Disaster Aid Connect Donation");
        attributes.put("reference_number", donationId);
        attributes.put("success_url",
            frontendUrl + "/donations?status=success&donation_id=" + donationId);
        attributes.put("cancel_url",
            frontendUrl + "/donations?status=cancelled&donation_id=" + donationId);

        Map<String, Object> data = new HashMap<>();
        data.put("attributes", attributes);
        Map<String, Object> body = new HashMap<>();
        body.put("data", data);

        HttpHeaders headers = buildHeaders();
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
            PAYMONGO_BASE + "/checkout_sessions",
            HttpMethod.POST,
            request,
            Map.class
        );

        Map responseData      = (Map) response.getBody().get("data");
        Map responseAttrs     = (Map) responseData.get("attributes");
        Map paymentIntentData = (Map) responseAttrs.get("payment_intent");

        String checkoutUrl     = (String) responseAttrs.get("checkout_url");
        String sessionId       = (String) responseData.get("id");
        String paymentIntentId = paymentIntentData != null ? (String) paymentIntentData.get("id") : null;

        System.out.println("CHECKOUT SESSION ID: " + sessionId);
        System.out.println("PAYMENT INTENT ID: " + paymentIntentId);

        return Map.of(
            "checkoutUrl",     checkoutUrl,
            "sessionId",       sessionId,
            "paymentIntentId", paymentIntentId != null ? paymentIntentId : ""
        );
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String encoded = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());
        headers.set("Authorization", "Basic " + encoded);
        return headers;
    }
}