import { useNavigate } from "react-router-dom";
import { formatPhoneForDisplay } from "../utils/phone";

function WorkerCard({ worker }) {

  const navigate = useNavigate();
  const workerPhone =
    worker?.phone || worker?.mobile || worker?.mobileNumber || worker?.contactNumber;
  const formattedPhone = workerPhone ? formatPhoneForDisplay(workerPhone) : "";
  const roundedRating = Number.isFinite(Number(worker.averageRating))
    ? Number(worker.averageRating).toFixed(1)
    : "0.0";

  return (
    <div className="relative p-5 bg-white border rounded-2xl shadow hover:shadow-xl transition">

      <img
        src="/tradesperson.png"
        alt="Tradesperson badge"
        className="absolute top-1 right-1 object-contain"
        style={{ height: "150px", width: "150px" }}
      />

      <h2 className="text-xl font-bold text-text-primary">
        {worker.name}
      </h2>

      <p className="text-text-secondary">{worker.occupation}</p>

      <p className="text-text-secondary">📍 {worker.workingCity}</p>

      <p className="text-text-secondary">⭐ {roundedRating}</p>

      <p className="text-text-secondary">🧰 {worker.experience || 0} yrs exp</p>

      <p className="text-text-secondary">📞 {formattedPhone || "Not provided"}</p>

      {/* ✅ Status Badge */}
      <span
        className={`inline-block mt-2 px-3 py-1 text-sm rounded-full ${
          worker.status === "AVAILABLE"
            ? "bg-green-200 text-green-800"
            : "bg-red-200 text-red-800"
        }`}
      >
        {worker.status}
      </span>

      {/* ✅ Verified */}
      {worker.verified && (
        <p className="text-blue-500 text-sm mt-1">✔ Verified</p>
      )}

      <button
        onClick={() => navigate(`/worker/${worker.id}`)}
        className="mt-4 bg-accent text-white px-4 py-2 rounded-lg w-full transition hover:bg-blue-800"
      >
        View Profile
      </button>

    </div>
  );
}

export default WorkerCard;