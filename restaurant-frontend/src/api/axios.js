import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080", // URL backend Spring Boot
  headers: {
    "Content-Type": "application/json",
  },
});

// Tự động thêm JWT token vào header nếu có
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token"); // lưu token ở localStorage
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;