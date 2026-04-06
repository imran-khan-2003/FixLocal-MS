package com.fixlocal.service;

import com.fixlocal.dto.*;
import com.fixlocal.model.*;
import com.fixlocal.repository.UserRepository;
import com.fixlocal.security.JwtService;
import com.fixlocal.exception.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserService userService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already exists");
        }

        if (request.getRole() == null) {
            throw new BadRequestException("Role is required");
        }

        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new BadRequestException("Password must be at least 6 characters");
        }

        if (request.getRole() == Role.TRADESPERSON) {
            if (request.getOccupation() == null || request.getWorkingCity() == null) {
                throw new BadRequestException(
                        "Occupation and working city required for tradesperson"
                );
            }
        }

        User user = User.builder()
                .name(request.getName())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(request.getRole())
                .occupation(request.getOccupation())
                .workingCity(request.getWorkingCity())
                .experience(request.getExperience() != null ? request.getExperience() : 0)
                .status(request.getRole() == Role.TRADESPERSON ? Status.AVAILABLE : null)
                .createdAt(LocalDateTime.now())
                .blocked(false)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(email);

        log.info("New user registered: {}", email);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .user(userService.mapToDTO(user))
                .build();
    }

    public AuthResponse login(LoginRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (user.isBlocked()) {
            throw new UnauthorizedException("Your account is blocked");
        }

        if (request.getPassword() == null ||
                !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String token = jwtService.generateToken(email);

        log.info("User logged in: {}", email);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .user(userService.mapToDTO(user))
                .build();
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password must match");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password reset completed for {}", email);
    }
}