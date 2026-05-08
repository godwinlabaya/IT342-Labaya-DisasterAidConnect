package edu.cit.labaya.disasteraidconnect.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.cit.labaya.disasteraidconnect.dto.DisasterRequestDTO;
import edu.cit.labaya.disasteraidconnect.dto.DisasterResponseDTO;
import edu.cit.labaya.disasteraidconnect.service.DisasterService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/disasters")
@CrossOrigin(origins = { "http://localhost:3000" })   // React dev server
public class DisasterController {

    private final DisasterService service;

    public DisasterController(DisasterService service) {
        this.service = service;
    }

    // GET /api/disasters — all disasters (used in Map.js)
    @GetMapping
    public ResponseEntity<List<DisasterResponseDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // GET /api/disasters/user/{userId} — only the caller's disasters (Requests.js)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DisasterResponseDTO>> getByUser(@PathVariable String userId) {
        return ResponseEntity.ok(service.getByUser(userId));
    }

    // GET /api/disasters/{id}
    @GetMapping("/{id}")
    public ResponseEntity<DisasterResponseDTO> getById(@PathVariable String id) {
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
            @PathVariable String id,
            @Valid @RequestBody DisasterRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    // DELETE /api/disasters/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}