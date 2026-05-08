package edu.cit.labaya.disasteraidconnect.donation;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/donations")
@CrossOrigin(origins = { "http://localhost:3000" })
public class DonationController {

    private final DonationService service;

    public DonationController(DonationService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<DonationResponseDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DonationResponseDTO>> getByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(service.getByUser(userId));
    }

    @GetMapping("/disaster/{disasterId}")
    public ResponseEntity<List<DonationResponseDTO>> getByDisaster(@PathVariable UUID disasterId) {
        return ResponseEntity.ok(service.getByDisaster(disasterId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DonationResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<DonationResponseDTO> create(@Valid @RequestBody DonationRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<DonationResponseDTO> updateStatus(
            @PathVariable UUID id,
            @RequestParam String status) {
        return ResponseEntity.ok(service.updateStatus(id, status));
    }
}