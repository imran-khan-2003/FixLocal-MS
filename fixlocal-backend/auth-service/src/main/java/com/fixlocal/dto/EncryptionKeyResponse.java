package com.fixlocal.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class EncryptionKeyResponse {
    String keyId;
    String algorithm;
    String publicKeyPem;
}
