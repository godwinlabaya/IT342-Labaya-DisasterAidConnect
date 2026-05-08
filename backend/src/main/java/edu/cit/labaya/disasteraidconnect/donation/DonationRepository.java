package edu.cit.labaya.disasteraidconnect.donation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DonationRepository extends JpaRepository<Donation, UUID> {

    List<Donation> findByUserIdOrderByDonatedAtDesc(UUID userId);

    List<Donation> findByDisasterIdOrderByDonatedAtDesc(UUID disasterId);

    List<Donation> findAllByOrderByDonatedAtDesc();
}