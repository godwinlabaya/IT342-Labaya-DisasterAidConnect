package edu.cit.labaya.disasteraidconnect.disaster;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/disasters")
@CrossOrigin(origins = { "http://localhost:3000" })
public class DisasterController {

    private final DisasterService service;

    public DisasterController(DisasterService service) {
        this.service = service;
    }

    // GET /api/disasters
    @GetMapping
    public ResponseEntity<List<DisasterResponseDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // GET /api/disasters/user/{userId}
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DisasterResponseDTO>> getByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(service.getByUser(userId));
    }

    // GET /api/disasters/{id}
    @GetMapping("/{id}")
    public ResponseEntity<DisasterResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // POST /api/disasters
    @PostMapping
    public ResponseEntity<DisasterResponseDTO> create(@Valid @RequestBody DisasterRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    // PUT /api/disasters/{id}
    @PutMapping("/{id}")
    public ResponseEntity<DisasterResponseDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody DisasterRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    // DELETE /api/disasters/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}