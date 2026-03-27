import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

function Navbar({ onToggleSidebar }) {
  const navigate = useNavigate();
  const { isAuthenticated, user, logout } = useAuth();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <nav className="bg-white shadow">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-4">
        <div className="flex items-center gap-3">
          <button
            type="button"
            className="rounded-lg border border-slate-200 p-2 text-slate-600 hover:bg-slate-100"
            onClick={onToggleSidebar || (() => {})}
            aria-label="Toggle navigation"
          >
            ☰
          </button>
          <Link to="/" className="flex items-center">
            <img src="/logo.png" alt="FixLocal logo" className="navbar-logo" />
          </Link>
        </div>

        <div className="flex flex-wrap items-center justify-end gap-4 text-sm font-medium text-gray-700">
          {isAuthenticated ? (
            <>
              <span className="text-gray-600">
                Signed in as <strong>{user?.name || "User"}</strong>
              </span>
              {user?.role === "TRADESPERSON" && (
                <Link to="/dashboard/tradesperson" className="hover:text-blue-600">
                  Tradesperson Console
                </Link>
              )}
              {user?.role === "TRADESPERSON" && (
                <Link to="/dashboard/tradesperson/ratings" className="hover:text-blue-600">
                  My Ratings
                </Link>
              )}
              {user?.role === "ADMIN" && (
                <Link to="/dashboard/admin" className="hover:text-blue-600">
                  Admin
                </Link>
              )}
              {user?.role === "USER" && (
                <Link to="/dashboard" className="hover:text-blue-600">
                  Dashboard
                </Link>
              )}
              <Link to="/profile" className="hover:text-blue-600">
                My Profile
              </Link>
              <button
                onClick={handleLogout}
                className="rounded-full bg-red-500 px-4 py-2 text-white transition hover:bg-red-600"
              >
                Logout
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="text-blue-700 hover:text-blue-900">
                Login
              </Link>
              <Link
                to="/register"
                className="rounded-full bg-blue-600 px-5 py-2 text-white transition hover:bg-blue-700"
              >
                Register
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}

export default Navbar;