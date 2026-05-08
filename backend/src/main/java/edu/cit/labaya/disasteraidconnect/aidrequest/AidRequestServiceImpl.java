package edu.cit.labaya.disasteraidconnect.aidrequest;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AidRequestServiceImpl implements AidRequestService {

    private final AidRequestRepository repo;

    public AidRequestServiceImpl(AidRequestRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<AidRequestResponseDTO> getAll() {
        return repo.findAllByOrderByCreatedAtDesc()
                   .stream()
                   .map(AidRequestResponseDTO::from)
                   .toList();
    }

    @Override
    public List<AidRequestResponseDTO> getByUser(UUID userId) {
        return repo.findByUserIdOrderByCreatedAtDesc(userId)
                   .stream()
                   .map(AidRequestResponseDTO::from)
                   .toList();
    }

    @Override
    public List<AidRequestResponseDTO> getByDisaster(UUID disasterId) {
        return repo.findByDisasterIdOrderByCreatedAtDesc(disasterId)
                   .stream()
                   .map(AidRequestResponseDTO::from)
                   .toList();
    }

    @Override
    public AidRequestResponseDTO getById(UUID id) {
        AidRequest a = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Aid request not found: " + id));
        return AidRequestResponseDTO.from(a);
    }

    @Override
    public AidRequestResponseDTO create(AidRequestRequestDTO dto) {
        AidRequest a = new AidRequest();
        a.setUserId(dto.getUserId());
        a.setDisasterId(dto.getDisasterId());
        a.setDescription(dto.getDescription());
        a.setAidType(dto.getAidType());
        a.setQuantity(dto.getQuantity());
        return AidRequestResponseDTO.from(repo.save(a));
    }

    @Override
    public AidRequestResponseDTO updateStatus(UUID id, String status) {
        AidRequest a = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Aid request not found: " + id));
        a.setStatus(status);
        return AidRequestResponseDTO.from(repo.save(a));
    }

    @Override
    public void delete(UUID id) {
        if (!repo.existsById(id)) throw new RuntimeException("Aid request not found: " + id);
        repo.deleteById(id);
    }
}