import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

function Navbar({ onToggleSidebar }) {
  const navigate = useNavigate();
  const { isAuthenticated, user, logout } = useAuth();
  const [isNarrowScreen, setIsNarrowScreen] = useState(() => {
    if (typeof window === "undefined") return false;
    return window.innerWidth < 600;
  });

  useEffect(() => {
    const handleResize = () => setIsNarrowScreen(window.innerWidth < 600);
    handleResize();
    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, []);

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  const authButtonClass =
    "rounded-full bg-accent px-5 py-2 text-white transition hover:bg-blue-800";

  return (
    <nav className="bg-primary shadow-md">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3">
        <div className="flex items-center gap-3">
          <button
            type="button"
            className="rounded-lg border border-slate-200 p-2 text-white hover:bg-accent"
            onClick={onToggleSidebar || (() => {})}
            aria-label="Toggle navigation"
          >
            ☰
          </button>
          <Link to="/" className="flex items-center">
            <img src="/logo.png" alt="FixLocal logo" className="navbar-logo" />
          </Link>
        </div>

        <div className="flex flex-wrap items-center justify-end gap-4 text-sm font-medium text-gray-200">
          {isAuthenticated ? (
            <>
              {!isNarrowScreen && (
                <>
                  <span className="text-white">
                    Signed in as <strong>{user?.name || "User"}</strong>
                  </span>
                  <Link to="/profile" className="hover:text-white">
                    My Profile
                  </Link>
                </>
              )}
              <button
                onClick={handleLogout}
                className="rounded-full bg-accent px-4 py-2 text-white transition hover:bg-blue-800"
              >
                Logout
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className={authButtonClass}>
                Login
              </Link>
              <Link
                to="/register"
                className={authButtonClass}
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
