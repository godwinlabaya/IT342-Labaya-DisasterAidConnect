package edu.cit.labaya.disasteraidconnect.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = { "http://localhost:3000" })
public class UserController {

    private final UserRepository repo;

    public UserController(UserRepository repo) {
        this.repo = repo;
    }

    // GET /api/users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getById(@PathVariable UUID id) {
        User u = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found: " + id));
        return ResponseEntity.ok(UserResponseDTO.from(u));
    }
}