import api from "./axios";

export const userService = {
  updateProfile: (payload) => api.put("/users/me", payload),
};

export default userService;