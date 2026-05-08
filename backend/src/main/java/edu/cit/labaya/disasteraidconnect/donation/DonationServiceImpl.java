package edu.cit.labaya.disasteraidconnect.donation;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DonationServiceImpl implements DonationService {

    private final DonationRepository repo;

    public DonationServiceImpl(DonationRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<DonationResponseDTO> getAll() {
        return repo.findAllByOrderByDonatedAtDesc()
                   .stream().map(DonationResponseDTO::from).toList();
    }

    @Override
    public List<DonationResponseDTO> getByUser(UUID userId) {
        return repo.findByUserIdOrderByDonatedAtDesc(userId)
                   .stream().map(DonationResponseDTO::from).toList();
    }

    @Override
    public List<DonationResponseDTO> getByDisaster(UUID disasterId) {
        return repo.findByDisasterIdOrderByDonatedAtDesc(disasterId)
                   .stream().map(DonationResponseDTO::from).toList();
    }

    @Override
    public DonationResponseDTO getById(UUID id) {
        Donation d = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Donation not found: " + id));
        return DonationResponseDTO.from(d);
    }

    @Override
    public DonationResponseDTO create(DonationRequestDTO dto) {
        Donation d = new Donation();
        d.setUserId(dto.getUserId());
        d.setDisasterId(dto.getDisasterId());
        d.setAmount(dto.getAmount());
        return DonationResponseDTO.from(repo.save(d));
    }

    @Override
    public DonationResponseDTO updateStatus(UUID id, String status) {
        Donation d = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Donation not found: " + id));
        d.setStatus(status);
        return DonationResponseDTO.from(repo.save(d));
    }
}