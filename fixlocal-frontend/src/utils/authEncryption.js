import api from "../api/axios";

let cachedKeyResponse = null;
let cachedCryptoKey = null;

const textEncoder = new TextEncoder();

function pemToArrayBuffer(pem) {
  const base64 = pem
    .replace("-----BEGIN PUBLIC KEY-----", "")
    .replace("-----END PUBLIC KEY-----", "")
    .replace(/\s/g, "");

  const binary = atob(base64);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i += 1) {
    bytes[i] = binary.charCodeAt(i);
  }
  return bytes.buffer;
}

function arrayBufferToBase64(buffer) {
  const bytes = new Uint8Array(buffer);
  let binary = "";
  for (let i = 0; i < bytes.byteLength; i += 1) {
    binary += String.fromCharCode(bytes[i]);
  }
  return btoa(binary);
}

async function getEncryptionMaterial() {
  if (!window?.crypto?.subtle) {
    throw new Error("Secure browser crypto is unavailable. Use HTTPS or localhost.");
  }

  if (!cachedKeyResponse) {
    const { data } = await api.get("/auth/encryption-key");
    cachedKeyResponse = data;
  }

  if (!cachedCryptoKey) {
    cachedCryptoKey = await window.crypto.subtle.importKey(
      "spki",
      pemToArrayBuffer(cachedKeyResponse.publicKeyPem),
      {
        name: "RSA-OAEP",
        hash: "SHA-256",
      },
      false,
      ["encrypt"]
    );
  }

  return {
    keyId: cachedKeyResponse.keyId,
    cryptoKey: cachedCryptoKey,
  };
}

export async function encryptAuthFields(fields) {
  const { keyId, cryptoKey } = await getEncryptionMaterial();
  const encrypted = {};

  const entries = Object.entries(fields || {});
  for (const [fieldName, plainValue] of entries) {
    if (plainValue == null || plainValue === "") {
      encrypted[fieldName] = "";
      continue;
    }

    const encoded = textEncoder.encode(String(plainValue));
    const cipherBuffer = await window.crypto.subtle.encrypt(
      { name: "RSA-OAEP" },
      cryptoKey,
      encoded
    );
    encrypted[fieldName] = arrayBufferToBase64(cipherBuffer);
  }

  return {
    encryptionKeyId: keyId,
    encrypted,
  };
}
