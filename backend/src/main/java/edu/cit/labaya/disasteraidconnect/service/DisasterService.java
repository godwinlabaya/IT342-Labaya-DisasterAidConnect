package edu.cit.labaya.disasteraidconnect.service;

import java.util.List;

import edu.cit.labaya.disasteraidconnect.dto.DisasterRequestDTO;
import edu.cit.labaya.disasteraidconnect.dto.DisasterResponseDTO;

public interface DisasterService {

    List<DisasterResponseDTO> getAll();

    List<DisasterResponseDTO> getByUser(String userId);

    DisasterResponseDTO getById(String id);

    DisasterResponseDTO create(DisasterRequestDTO dto);

    DisasterResponseDTO update(String id, DisasterRequestDTO dto);

    void delete(String id);
}