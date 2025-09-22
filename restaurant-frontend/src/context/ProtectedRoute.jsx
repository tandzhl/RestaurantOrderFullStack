import { useContext } from "react";
import { Navigate } from "react-router-dom";
import { AuthContext } from "./AuthContext";

export default function ProtectedRoute({ children, role }) {
  const { authToken, user } = useContext(AuthContext);

  if (!authToken) return <Navigate to="/login" />;

  if (role && user?.role !== role) return <Navigate to="/" />;

  return children;
}
