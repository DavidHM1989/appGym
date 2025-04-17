package appGym.controller;
import appGym.repository.RoleRepository;
import appGym.model.Role;
import appGym.model.User;
import appGym.repository.UserRepository;
import appGym.security.JwtUtil;
import appGym.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public AuthController(RoleRepository roleRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    /*@PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String token = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(Map.of("token", token));
    }*/

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String token = jwtUtil.generateToken(userDetails);

        // Obtener usuario desde el repositorio
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        // Extraer los roles en una lista de Strings
        List<String> roles = user.getRoles() != null ? user.getRoles().stream()
                .map(role -> role != null && role.getName() != null ? role.getName() : null)
                .filter(Objects::nonNull) // Eliminar posibles nulos resultantes del mapeo
                .toList() : Collections.emptyList();


        // Retornar token, username y roles
        return ResponseEntity.ok(Map.of(
                "token", token,
                "username", username,
                "roles", roles
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userService.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already taken");
        }
        if (userService.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already in use");
        }

        // Asignar rol por defecto (id = 1)
        Role defaultRole = roleRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Error: Role not found."));
        user.setRoles(Collections.singleton(defaultRole));

        User savedUser = userService.saveUser(user);
        return ResponseEntity.ok(savedUser);
    }

    /*@PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userService.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already taken");
        }
        if (userService.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already in use");
        }
        
        User savedUser = userService.saveUser(user);
        return ResponseEntity.ok(savedUser);
    }*/
}
