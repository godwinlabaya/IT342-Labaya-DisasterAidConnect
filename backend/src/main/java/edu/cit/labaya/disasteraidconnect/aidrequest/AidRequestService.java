package edu.cit.labaya.disasteraidconnect.aidrequest;

import java.util.List;
import java.util.UUID;

public interface AidRequestService {

    List<AidRequestResponseDTO> getAll();

    List<AidRequestResponseDTO> getByUser(UUID userId);

    List<AidRequestResponseDTO> getByDisaster(UUID disasterId);

    AidRequestResponseDTO getById(UUID id);

    AidRequestResponseDTO create(AidRequestRequestDTO dto);

    AidRequestResponseDTO updateStatus(UUID id, String status);

    void delete(UUID id);
}