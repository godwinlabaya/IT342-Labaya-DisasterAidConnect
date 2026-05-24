package edu.cit.labaya.disasteraidconnect.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByDonationIdOrderByPaymentDateDesc(UUID donationId);

    Optional<Payment> findByTransactionReference(String transactionReference);

    Optional<Payment> findByDonationId(UUID donationId);

    Optional<Payment> findByPaymentIntentId(String paymentIntentId);
}