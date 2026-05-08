package edu.cit.labaya.disasteraidconnect.aidrequest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AidRequestRepository extends JpaRepository<AidRequest, UUID> {

    List<AidRequest> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<AidRequest> findByDisasterIdOrderByCreatedAtDesc(UUID disasterId);

    List<AidRequest> findByStatusOrderByCreatedAtDesc(String status);

    List<AidRequest> findAllByOrderByCreatedAtDesc();
}