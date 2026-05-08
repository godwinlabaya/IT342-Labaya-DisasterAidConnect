package edu.cit.labaya.disasteraidconnect.donation;

import java.util.List;
import java.util.UUID;

public interface DonationService {

    List<DonationResponseDTO> getAll();

    List<DonationResponseDTO> getByUser(UUID userId);

    List<DonationResponseDTO> getByDisaster(UUID disasterId);

    DonationResponseDTO getById(UUID id);

    DonationResponseDTO create(DonationRequestDTO dto);

    DonationResponseDTO updateStatus(UUID id, String status);
}