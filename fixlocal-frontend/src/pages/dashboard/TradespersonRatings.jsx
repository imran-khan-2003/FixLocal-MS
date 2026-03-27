import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import DashboardLayout from "../../components/DashboardLayout";
import reviewService from "../../api/reviewService";
import { useAuth } from "../../context/AuthContext";

function RatingCard({ review }) {
  return (
    <div className="border border-slate-100 rounded-2xl p-4 bg-white shadow-sm">
      <div className="flex justify-between items-center">
        <div>
          <p className="text-sm font-semibold text-slate-800">
            {review.userName || "Client"}
          </p>
          <p className="text-xs text-slate-500">Booking #{review.bookingId.slice(-6)}</p>
        </div>
        <div className="text-right">
          <p className="text-lg font-bold text-amber-500">
            {review.rating.toFixed(1)} ★
          </p>
          <p className="text-xs text-slate-400">
            {new Date(review.createdAt).toLocaleDateString()}
          </p>
        </div>
      </div>
      {review.comment && (
        <p className="text-sm text-slate-600 mt-3 leading-relaxed">{review.comment}</p>
      )}
    </div>
  );
}

function TradespersonRatings() {
  const { user } = useAuth();
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!user?.id) return;
    let active = true;
    setLoading(true);
    setError("");
    reviewService
      .getTradespersonReviews(user.id)
      .then(({ data }) => {
        if (active) setReviews(data || []);
      })
      .catch(() => {
        if (active) setError("Failed to load your ratings");
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [user?.id]);

  const aggregates = useMemo(() => {
    if (!reviews.length) {
      return { average: 0, total: 0 };
    }
    const total = reviews.length;
    const sum = reviews.reduce((acc, r) => acc + r.rating, 0);
    return { average: sum / total, total };
  }, [reviews]);

  return (
    <DashboardLayout
      title="My Ratings"
      subtitle="See every review customers have left for your work"
      actions={
        <Link
          to="/dashboard/tradesperson"
          className="text-sm font-semibold text-blue-600 hover:underline"
        >
          Back to console
        </Link>
      }
    >
      {error && <p className="text-red-500 mb-4 text-sm">{error}</p>}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-6">
        <div className="bg-white border border-slate-100 rounded-2xl p-4 shadow-sm">
          <p className="text-xs uppercase text-slate-500">Average Rating</p>
          <p className="text-3xl font-bold text-amber-500 mt-2">
            {aggregates.average.toFixed(1)} ★
          </p>
        </div>
        <div className="bg-white border border-slate-100 rounded-2xl p-4 shadow-sm">
          <p className="text-xs uppercase text-slate-500">Total Reviews</p>
          <p className="text-3xl font-bold text-slate-900 mt-2">{aggregates.total}</p>
        </div>
      </div>
      {loading ? (
        <p className="text-slate-600">Loading ratings...</p>
      ) : reviews.length === 0 ? (
        <p className="text-slate-600">No reviews yet. Complete bookings to gather feedback.</p>
      ) : (
        <div className="space-y-4">
          {reviews.map((review) => (
            <RatingCard key={review.id} review={review} />
          ))}
        </div>
      )}
    </DashboardLayout>
  );
}

export default TradespersonRatings;