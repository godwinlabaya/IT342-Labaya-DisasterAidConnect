package edu.cit.labaya.disasteraidconnect.aidrequest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/aid-requests")
@CrossOrigin(origins = { "http://localhost:3000" })
public class AidRequestController {

    private final AidRequestService service;

    public AidRequestController(AidRequestService service) {
        this.service = service;
    }

    // GET /api/aid-requests
    @GetMapping
    public ResponseEntity<List<AidRequestResponseDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // GET /api/aid-requests/user/{userId}
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AidRequestResponseDTO>> getByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(service.getByUser(userId));
    }

    // GET /api/aid-requests/disaster/{disasterId}
    @GetMapping("/disaster/{disasterId}")
    public ResponseEntity<List<AidRequestResponseDTO>> getByDisaster(@PathVariable UUID disasterId) {
        return ResponseEntity.ok(service.getByDisaster(disasterId));
    }

    // GET /api/aid-requests/{id}
    @GetMapping("/{id}")
    public ResponseEntity<AidRequestResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // POST /api/aid-requests
    @PostMapping
    public ResponseEntity<AidRequestResponseDTO> create(@Valid @RequestBody AidRequestRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    // PATCH /api/aid-requests/{id}/status
    @PatchMapping("/{id}/status")
    public ResponseEntity<AidRequestResponseDTO> updateStatus(
            @PathVariable UUID id,
            @RequestParam String status) {
        return ResponseEntity.ok(service.updateStatus(id, status));
    }

    // DELETE /api/aid-requests/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}