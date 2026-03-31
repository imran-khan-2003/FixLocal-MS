import { useEffect, useMemo, useState } from "react";
import DashboardLayout from "../components/DashboardLayout";
import { useAuth } from "../context/AuthContext";
import { dashboardService } from "../api/dashboardService";
import reviewService from "../api/reviewService";

function InfoRow({ label, value }) {
  return (
    <div className="flex flex-col">
      <span className="text-xs text-slate-500 uppercase tracking-wide">{label}</span>
      <span className="text-sm text-slate-800 font-semibold">{value || "—"}</span>
    </div>
  );
}

function Profile() {
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [userStats, setUserStats] = useState(null);
  const [tradespersonStats, setTradespersonStats] = useState(null);
  const [tradespersonReviews, setTradespersonReviews] = useState([]);

  useEffect(() => {
    let active = true;

    async function loadStats() {
      if (!user) return;
      setError("");
      setLoading(true);
      try {
        if (user.role === "USER") {
          const { data } = await dashboardService.getUserDashboard();
          if (active) setUserStats(data);
        } else if (user.role === "TRADESPERSON") {
          const { data } = await dashboardService.getTradespersonDashboard();
          if (active) setTradespersonStats(data);
          const reviewsRes = await reviewService.getTradespersonReviews(data?.profile?.id || user.id);
          if (active) setTradespersonReviews(reviewsRes.data || []);
        }
      } catch (err) {
        if (active) setError("Failed to load profile insights");
      } finally {
        if (active) setLoading(false);
      }
    }

    loadStats();

    return () => {
      active = false;
    };
  }, [user]);

  const metrics = useMemo(() => {
    if (user?.role === "USER" && userStats) {
      return [
        { label: "Upcoming Bookings", value: userStats.upcomingBookings },
        { label: "Active Bookings", value: userStats.activeBookings },
        { label: "Completed Bookings", value: userStats.completedBookings },
        { label: "Total Bookings", value: userStats.totalBookings },
      ];
    }
    if (user?.role === "TRADESPERSON" && tradespersonStats) {
      return [
        { label: "Pending Requests", value: tradespersonStats.pendingRequests },
        { label: "Active Jobs", value: tradespersonStats.activeBookings },
        { label: "Completed Jobs", value: tradespersonStats.completedBookings },
        { label: "Total Jobs", value: tradespersonStats.totalBookings },
        {
          label: "Average Rating",
          value: tradespersonStats.averageRating?.toFixed(1) ?? "0.0",
        },
        { label: "Total Reviews", value: tradespersonStats.totalReviews },
      ];
    }
    return [];
  }, [user?.role, userStats, tradespersonStats]);

  if (!user) {
    return (
      <DashboardLayout title="My Profile">
        <p className="text-slate-600">Loading profile...</p>
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout title="My Profile" subtitle="Manage your account details">
      <div className="bg-white rounded-2xl shadow border border-slate-100 p-6 space-y-6">
        <div className="flex flex-wrap gap-6 items-center">
          <div>
            <p className="text-xs uppercase text-slate-500">Name</p>
            <h2 className="text-2xl font-bold text-slate-900">{user.name}</h2>
            <p className="text-sm text-slate-500">{user.email}</p>
          </div>
          <span className="px-3 py-1 rounded-full bg-blue-50 text-blue-600 text-sm font-semibold">
            {user.role}
          </span>
        </div>
        {error && <p className="text-sm text-red-500">{error}</p>}
        {loading ? (
          <p className="text-slate-500 text-sm">Loading insights...</p>
        ) : (
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
            <InfoRow label="Occupation" value={user.occupation} />
            <InfoRow label="City" value={user.workingCity} />
            <InfoRow label="Mobile" value={user.phone} />
            <InfoRow label="Experience" value={user.experience ? `${user.experience} yrs` : "—"} />
            <InfoRow label="Status" value={user.status} />
            <InfoRow label="Verified" value={user.verified ? "Yes" : "No"} />
            <InfoRow label="Blocked" value={user.blocked ? "Yes" : "No"} />
            {metrics.map((metric) => (
              <InfoRow key={metric.label} label={metric.label} value={metric.value} />
            ))}
          </div>
        )}
        {(user.skillTags?.length || 0) > 0 && (
          <div>
            <p className="text-xs uppercase text-slate-500">Skills</p>
            <div className="flex flex-wrap gap-2 mt-2">
              {user.skillTags.map((tag) => (
                <span
                  key={tag}
                  className="px-3 py-1 text-xs rounded-full bg-slate-100 text-slate-700"
                >
                  {tag}
                </span>
              ))}
            </div>
          </div>
        )}
        {user.role === "TRADESPERSON" && tradespersonReviews.length > 0 && (
          <div>
            <p className="text-xs uppercase text-slate-500">Recent Feedback</p>
            <div className="mt-3 space-y-3">
              {tradespersonReviews.slice(0, 3).map((review) => (
                <div key={review.id} className="border border-slate-100 rounded-lg p-3">
                  <div className="flex justify-between text-sm">
                    <span className="font-semibold text-slate-800">{review.userName || "Client"}</span>
                    <span className="text-amber-500 font-semibold">{review.rating.toFixed(1)} ★</span>
                  </div>
                  <p className="text-sm text-slate-600 mt-1">{review.comment}</p>
                  <p className="text-xs text-slate-400 mt-1">
                    {new Date(review.createdAt).toLocaleDateString()}
                  </p>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </DashboardLayout>
  );
}

export default Profile;
