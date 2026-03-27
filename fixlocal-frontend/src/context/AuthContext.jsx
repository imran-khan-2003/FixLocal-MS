import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import api from "../api/axios";

const STORAGE_KEY = "fixlocal_auth";

export const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [token, setToken] = useState("");
  const [user, setUser] = useState(null);

  useEffect(() => {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved) {
      const parsed = JSON.parse(saved);
      setToken(parsed.token || "");
      setUser(parsed.user || null);
    }
  }, []);

  useEffect(() => {
    if (token) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify({ token, user }));
    } else {
      localStorage.removeItem(STORAGE_KEY);
    }
  }, [token, user]);

  const logout = useCallback(() => {
    setToken("");
    setUser(null);
    localStorage.removeItem(STORAGE_KEY);
  }, []);

  const hydrateUser = useCallback(
    async (authToken = token) => {
      if (!authToken) return null;
      try {
        const { data } = await api.get("/users/me", {
          headers: { Authorization: `Bearer ${authToken}` },
        });
        setUser(data);
        return data;
      } catch (error) {
        console.error("Failed to hydrate user profile", error);
        logout();
        throw error;
      }
    },
    [token, logout]
  );

  const login = useCallback(
    async ({ token: newToken, user: profile }) => {
      if (!newToken) {
        throw new Error("Token is required to login");
      }

      setToken(newToken);

      if (profile) {
        setUser(profile);
        return profile;
      }

      return hydrateUser(newToken);
    },
    [hydrateUser]
  );

  useEffect(() => {
    if (!token) {
      setUser(null);
      return;
    }

    if (!user || !user.role) {
      hydrateUser(token).catch(() => {});
    }
  }, [token, user, hydrateUser]);

  const normalizedUser = useMemo(() => {
    if (!user) return null;
    if (user.role) return user;
    const role = user.roles?.[0] || user.authorities?.[0] || user.roleName;
    return { ...user, role };
  }, [user]);

  const value = useMemo(
    () => ({ token, user: normalizedUser, isAuthenticated: Boolean(token), login, logout }),
    [token, normalizedUser, login, logout]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}