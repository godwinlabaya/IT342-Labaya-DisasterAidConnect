package edu.cit.labaya.disasteraidconnect.disaster;

import java.util.List;
import java.util.UUID;

public interface DisasterService {

    List<DisasterResponseDTO> getAll();

    List<DisasterResponseDTO> getByUser(UUID userId);

    DisasterResponseDTO getById(UUID id);

    DisasterResponseDTO create(DisasterRequestDTO dto);

    DisasterResponseDTO update(UUID id, DisasterRequestDTO dto);

    void delete(UUID id);
}