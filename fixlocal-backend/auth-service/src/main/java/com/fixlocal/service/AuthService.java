package com.fixlocal.service;

import com.fixlocal.dto.*;
import com.fixlocal.entity.*;
import com.fixlocal.enums.*;
import com.fixlocal.repository.UserRepository;
import com.fixlocal.security.JwtService;
import com.fixlocal.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

public interface AuthService {
    public AuthResponse register(RegisterRequest request);
    public AuthResponse login(LoginRequest request);
    public void forgotPassword(ForgotPasswordRequest request);
}
