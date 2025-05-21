package com.gradence.ga.controller;

import com.gradence.ga.model.*;
import com.gradence.ga.repository.*;
import com.gradence.ga.security.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepo.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username is taken");
        }

        user.setPassword(encoder.encode(user.getPassword()));

        userRepo.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
        );

        String token = jwtUtils.generateToken(authRequest.getUsername());
        return ResponseEntity.ok(Collections.singletonMap("token", token));
    }
}