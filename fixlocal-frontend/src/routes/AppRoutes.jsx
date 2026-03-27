import { useState } from "react";
import { BrowserRouter, Routes, Route, Navigate, Outlet } from "react-router-dom";

import Home from "../pages/Home";
import Login from "../pages/Login";
import Register from "../pages/Register";
import SearchResults from "../pages/SearchResults";
import WorkerProfile from "../pages/WorkerProfile";
import UserDashboard from "../pages/dashboard/UserDashboard";
import TradespersonDashboard from "../pages/dashboard/TradespersonDashboard";
import TradespersonRatings from "../pages/dashboard/TradespersonRatings";
import AdminDashboard from "../pages/dashboard/AdminDashboard";
import Profile from "../pages/Profile";
import { useAuth } from "../context/AuthContext";
import Navbar from "../components/Navbar";
import Sidebar from "../components/Sidebar";

function AppRoutes() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const { isAuthenticated, user } = useAuth();

  const ProtectedRoute = ({ allowedRoles, element }) => {
    if (!isAuthenticated) return <Navigate to="/login" replace />;
    if (allowedRoles && !allowedRoles.includes(user?.role)) {
      return <Navigate to="/" replace />;
    }
    return element;
  };

  const PublicLayout = () => (
    <div className="min-h-screen bg-slate-50">
      <Navbar onToggleSidebar={() => setSidebarOpen((prev) => !prev)} />
      <Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />
      <div className="pt-4 px-4">
        <Outlet />
      </div>
    </div>
  );

  return (
    <BrowserRouter>
      <Routes>
        <Route element={<PublicLayout />}>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/search" element={<SearchResults />} />
          <Route path="/worker/:id" element={<WorkerProfile />} />
        </Route>
        <Route path="/profile" element={<ProtectedRoute element={<Profile />} />} />
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute
              allowedRoles={["USER"]}
              element={<UserDashboard />}
            />
          }
        />
        <Route
          path="/dashboard/tradesperson"
          element={
            <ProtectedRoute
              allowedRoles={["TRADESPERSON"]}
              element={<TradespersonDashboard />}
            />
          }
        />
        <Route
          path="/dashboard/tradesperson/ratings"
          element={
            <ProtectedRoute
              allowedRoles={["TRADESPERSON"]}
              element={<TradespersonRatings />}
            />
          }
        />
        <Route
          path="/dashboard/admin"
          element={
            <ProtectedRoute
              allowedRoles={["ADMIN"]}
              element={<AdminDashboard />}
            />
          }
        />

      </Routes>
    </BrowserRouter>
  );
}

export default AppRoutes;