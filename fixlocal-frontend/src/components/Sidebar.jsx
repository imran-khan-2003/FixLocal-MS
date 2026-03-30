import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

function Sidebar({ open, onClose }) {
  const { isAuthenticated, user, logout } = useAuth();

  const navLinks = [
    { to: "/", label: "Home" },
    { to: "/search", label: "Find Pros" },
    ...(isAuthenticated
      ? [
          user?.role === "USER" && { to: "/dashboard", label: "Overview" },
          user?.role === "USER" && { to: "/dashboard/current", label: "Current Booking" },
          user?.role === "USER" && { to: "/dashboard/history", label: "History" },
          user?.role === "TRADESPERSON" && {
            to: "/dashboard/tradesperson",
            label: "Tradesperson Console",
          },
          user?.role === "TRADESPERSON" && {
            to: "/dashboard/tradesperson/current",
            label: "Current Booking",
          },
          user?.role === "TRADESPERSON" && {
            to: "/dashboard/tradesperson/history",
            label: "History",
          },
          user?.role === "TRADESPERSON" && {
            to: "/dashboard/tradesperson/ratings",
            label: "My Ratings",
          },
          user?.role === "TRADESPERSON" && {
            to: "/dashboard/tradesperson/disputes",
            label: "My Disputes",
          },
          user?.role === "USER" && {
            to: "/dashboard/disputes/mine",
            label: "My Disputes",
          },
          user?.role === "ADMIN" && { to: "/dashboard/admin", label: "Admin" },
          user?.role === "ADMIN" && { to: "/dashboard/disputes", label: "Disputes" },
          { to: "/profile", label: "Profile" },
        ]
      : [
          { to: "/login", label: "Login" },
          { to: "/register", label: "Register" },
        ]
    ).filter(Boolean),
  ];

  return (
    <div
      className={`fixed inset-y-0 left-0 z-40 w-64 bg-white shadow-xl transition-transform duration-200 ease-out ${
        open ? "translate-x-0" : "-translate-x-full"
      } ${open ? "visible" : "invisible"}`}
    >
      <div className="flex items-center justify-between px-4 py-4 border-b border-slate-100">
        <span className="text-lg font-semibold text-slate-800">Menu</span>
        <button
          type="button"
          className="rounded-lg border border-slate-200 p-2 text-slate-500 hover:bg-slate-50"
          onClick={onClose}
          aria-label="Close sidebar"
        >
          ✕
        </button>
      </div>
      <nav className="px-4 py-6 space-y-2 text-slate-700">
        {navLinks.map((link) => (
          <Link
            key={link.to}
            to={link.to}
            className="flex items-center rounded-lg px-3 py-2 text-sm font-medium hover:bg-slate-100"
            onClick={onClose}
          >
            {link.label}
          </Link>
        ))}
        {isAuthenticated && (
          <button
            type="button"
            className="mt-4 w-full rounded-lg border border-red-100 px-3 py-2 text-sm font-semibold text-red-600 hover:bg-red-50"
            onClick={() => {
              logout();
              onClose();
            }}
          >
            Logout
          </button>
        )}
      </nav>
    </div>
  );
}

export default Sidebar;
