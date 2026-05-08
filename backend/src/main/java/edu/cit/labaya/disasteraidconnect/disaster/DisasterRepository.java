package edu.cit.labaya.disasteraidconnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.cit.labaya.disasteraidconnect.entity.Disaster;

@Repository
public interface DisasterRepository extends JpaRepository<Disaster, String> {

    // Mirrors disasterService.getByUser(currentUID) used in Requests.js
    List<Disaster> findByCreatedByOrderByCreatedAtDesc(String createdBy);

    // Mirrors disasterService.getAll() used in Map.js
    List<Disaster> findAllByOrderByCreatedAtDesc();

    // Filter by status (Active / Monitoring / Resolved)
    List<Disaster> findByStatusOrderByCreatedAtDesc(String status);
}