/* eslint-disable react-refresh/only-export-components */
import { createContext, useEffect, useState } from "react";

export const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [authToken, setAuthToken] = useState(null);
  const [user, setUser] = useState(null);

  // Load token và user từ sessionStorage
  useEffect(() => {
    const token = sessionStorage.getItem("authToken");
    const userDataStr = sessionStorage.getItem("user");

    if (token) setAuthToken(token);

    if (userDataStr) {
        try {
        const userData = JSON.parse(userDataStr);
        setUser(userData);
        } catch (err) {
        console.error("Invalid user data in sessionStorage:", userDataStr, err);
        sessionStorage.removeItem("user");
        }
    }
    }, []);

  const login = (token, userData) => {
    sessionStorage.setItem("authToken", token);
    sessionStorage.setItem("user", JSON.stringify(userData));
    setAuthToken(token);
    setUser(userData);
  };

  const logout = () => {
    sessionStorage.removeItem("authToken");
    sessionStorage.removeItem("user");
    setAuthToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ authToken, user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}