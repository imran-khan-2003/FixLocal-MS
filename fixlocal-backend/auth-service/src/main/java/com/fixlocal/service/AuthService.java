package com.fixlocal.service;

import com.fixlocal.dto.*;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    EncryptionKeyResponse getEncryptionKey();
}
