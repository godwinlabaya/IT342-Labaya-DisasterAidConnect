package edu.cit.labaya.disasteraidconnect.service;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.cit.labaya.disasteraidconnect.dto.DisasterRequestDTO;
import edu.cit.labaya.disasteraidconnect.dto.DisasterResponseDTO;
import edu.cit.labaya.disasteraidconnect.entity.Disaster;
import edu.cit.labaya.disasteraidconnect.repository.DisasterRepository;

@Service
public class DisasterServiceImpl implements DisasterService {

    private final DisasterRepository repo;

    public DisasterServiceImpl(DisasterRepository repo) {
        this.repo = repo;
    }

    // ── READ ───────────────────────────────────────────────────────────────────

    @Override
    public List<DisasterResponseDTO> getAll() {
        return repo.findAllByOrderByCreatedAtDesc()
                   .stream()
                   .map(DisasterResponseDTO::from)
                   .toList();
    }

    @Override
    public List<DisasterResponseDTO> getByUser(String userId) {
        return repo.findByCreatedByOrderByCreatedAtDesc(userId)
                   .stream()
                   .map(DisasterResponseDTO::from)
                   .toList();
    }

    @Override
    public DisasterResponseDTO getById(String id) {
        Disaster d = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Disaster not found: " + id));
        return DisasterResponseDTO.from(d);
    }

    // ── CREATE ─────────────────────────────────────────────────────────────────

    @Override
    public DisasterResponseDTO create(DisasterRequestDTO dto) {
        Disaster d = new Disaster();
        applyDto(dto, d);
        return DisasterResponseDTO.from(repo.save(d));
    }

    // ── UPDATE ─────────────────────────────────────────────────────────────────

    @Override
    public DisasterResponseDTO update(String id, DisasterRequestDTO dto) {
        Disaster d = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Disaster not found: " + id));
        applyDto(dto, d);
        return DisasterResponseDTO.from(repo.save(d));
    }

    // ── DELETE ─────────────────────────────────────────────────────────────────

    @Override
    public void delete(String id) {
        if (!repo.existsById(id)) throw new RuntimeException("Disaster not found: " + id);
        repo.deleteById(id);
    }

    // ── Helper ─────────────────────────────────────────────────────────────────

    private void applyDto(DisasterRequestDTO dto, Disaster d) {
        d.setTitle(dto.getTitle());
        d.setDescription(dto.getDescription());
        d.setSeverityLevel(dto.getSeverityLevel());
        d.setStatus(dto.getStatus());
        d.setLatitude(dto.getLatitude());
        d.setLongitude(dto.getLongitude());
        d.setCreatedBy(dto.getCreatedBy());
    }
}