package edu.cit.labaya.disasteraidconnect.disaster;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DisasterRepository extends JpaRepository<Disaster, UUID> {

    List<Disaster> findByCreatedByOrderByCreatedAtDesc(UUID createdBy);

    List<Disaster> findAllByOrderByCreatedAtDesc();

    List<Disaster> findByStatusOrderByCreatedAtDesc(String status);
}