package appGym.controller;

import appGym.model.Role;
import appGym.model.User;
import appGym.repository.RoleRepository;
import appGym.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "http://localhost:4200")
public class RoleController {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleRepository.findAll());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Role> updateRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Optional<Role> roleOptional = roleRepository.findById(id);
        if (roleOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Role role = roleOptional.get();
        String newName = body.get("name"); // Extrae el nombre del JSON
        if (newName == null || newName.isEmpty()) {
            return ResponseEntity.badRequest().body(null); // Evita nombres vacíos
        }
        role.setName(newName);
        roleRepository.save(role);
        return ResponseEntity.ok(role);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Role> createRole(@RequestBody Role role) {
        if (roleRepository.findByName(role.getName()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // Evitar duplicados
        }
        Role newRole = roleRepository.save(role);
        return ResponseEntity.status(HttpStatus.CREATED).body(newRole);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/assign/{userId}")
    public ResponseEntity<?> assignRolesToUser(@PathVariable Long userId, @RequestBody Set<String> roleNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Set<Role> roles = roleNames.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol no encontrado: " + roleName)))
                .collect(Collectors.toSet());

        if (roles.isEmpty()) {
            return ResponseEntity.badRequest().body("No se encontraron roles válidos para asignar.");
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable Long id) {
        if (!roleRepository.existsById(id)) {
            return ResponseEntity.notFound().build(); // Si no encuentra el rol, devuelve 404
        }

        roleRepository.deleteById(id);
        return ResponseEntity.noContent().build(); // 204 No Content si se elimina correctamente
    }
}
