package com.fixlocal.security;

import com.fixlocal.dto.EncryptionKeyResponse;
import com.fixlocal.exception.BadRequestException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class PayloadEncryptionService {

    private static final OAEPParameterSpec OAEP_SHA256 =
            new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);

    @Value("${auth.payload-encryption.enabled:true}")
    private boolean payloadEncryptionEnabled;

    @Value("${auth.payload-encryption.required:true}")
    private boolean payloadEncryptionRequired;

    @Value("${auth.payload-encryption.key-id:fixlocal-rsa-v1}")
    private String keyId;

    @Value("${auth.payload-encryption.public-key-pem:}")
    private String publicKeyPemConfig;

    @Value("${auth.payload-encryption.private-key-pem:}")
    private String privateKeyPemConfig;

    private PublicKey publicKey;
    private PrivateKey privateKey;
    private String publicKeyPem;

    @PostConstruct
    void init() {
        if (!payloadEncryptionEnabled) {
            return;
        }

        try {
            if (hasText(publicKeyPemConfig) && hasText(privateKeyPemConfig)) {
                this.publicKey = parsePublicKey(publicKeyPemConfig);
                this.privateKey = parsePrivateKey(privateKeyPemConfig);
                this.publicKeyPem = toPublicPem(this.publicKey);
                return;
            }

            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();
            this.publicKey = keyPair.getPublic();
            this.privateKey = keyPair.getPrivate();
            this.publicKeyPem = toPublicPem(this.publicKey);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize payload encryption", e);
        }
    }

    public EncryptionKeyResponse getEncryptionKey() {
        if (!payloadEncryptionEnabled || publicKey == null) {
            throw new BadRequestException("Payload encryption is disabled");
        }

        return EncryptionKeyResponse.builder()
                .keyId(keyId)
                .algorithm("RSA-OAEP-256")
                .publicKeyPem(publicKeyPem)
                .build();
    }

    public String resolvePassword(String plainText,
                                  String encrypted,
                                  String providedKeyId,
                                  String fieldName) {
        if (!payloadEncryptionEnabled) {
            return requirePlain(plainText, fieldName);
        }

        if (hasText(encrypted)) {
            validateKeyId(providedKeyId);
            return decrypt(encrypted, fieldName);
        }

        if (hasText(plainText)) {
            if (payloadEncryptionRequired) {
                throw new BadRequestException("Encrypted " + fieldName + " is required");
            }
            return plainText;
        }

        throw new BadRequestException(fieldName + " is required");
    }

    private String requirePlain(String plainText, String fieldName) {
        if (!hasText(plainText)) {
            throw new BadRequestException(fieldName + " is required");
        }
        return plainText;
    }

    private void validateKeyId(String providedKeyId) {
        if (!hasText(providedKeyId)) {
            throw new BadRequestException("encryptionKeyId is required");
        }
        if (!keyId.equals(providedKeyId)) {
            throw new BadRequestException("Invalid encryption key id");
        }
    }

    private String decrypt(String encrypted, String fieldName) {
        try {
            byte[] cipherBytes = Base64.getDecoder().decode(encrypted);
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey, OAEP_SHA256);
            String decrypted = new String(cipher.doFinal(cipherBytes), StandardCharsets.UTF_8);

            if (!hasText(decrypted)) {
                throw new BadRequestException(fieldName + " is required");
            }

            return decrypted;
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BadRequestException("Invalid encrypted payload for " + fieldName);
        }
    }

    private PublicKey parsePublicKey(String pem) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(stripPem(pem));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private PrivateKey parsePrivateKey(String pem) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(stripPem(pem));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private String stripPem(String pem) {
        return pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
    }

    private String toPublicPem(PublicKey key) {
        String base64 = Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(key.getEncoded());
        return "-----BEGIN PUBLIC KEY-----\n" + base64 + "\n-----END PUBLIC KEY-----";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
