import axios from "axios";

const RAW_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
const NORMALIZED_BASE_URL = RAW_BASE_URL.replace(/\/+$/, "");
const MONOLITH_BASE_URL = "http://localhost:8079";

if (
  NORMALIZED_BASE_URL === MONOLITH_BASE_URL ||
  NORMALIZED_BASE_URL.startsWith(`${MONOLITH_BASE_URL}/`)
) {
  throw new Error(
    "VITE_API_BASE_URL is pointing to the monolith backend (port 8079). Set it to the microservices API gateway (for example http://localhost:8080)."
  );
}

const BASE_URL = NORMALIZED_BASE_URL.endsWith("/api/v1")
  ? NORMALIZED_BASE_URL
  : `${NORMALIZED_BASE_URL}/api/v1`;

const api = axios.create({
  baseURL: BASE_URL,
});

api.interceptors.request.use((config) => {
  const stored = localStorage.getItem("fixlocal_auth");
  if (stored) {
    const { token } = JSON.parse(stored);
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  }
  return config;
});

export default api;