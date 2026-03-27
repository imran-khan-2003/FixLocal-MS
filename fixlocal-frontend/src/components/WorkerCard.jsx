import { useNavigate } from "react-router-dom";

function WorkerCard({ worker }) {

  const navigate = useNavigate();

  return (
    <div className="p-5 border rounded-2xl shadow hover:shadow-xl transition">

      <h2 className="text-xl font-bold">
        {worker.name}
      </h2>

      <p className="text-gray-600">{worker.occupation}</p>

      <p>📍 {worker.workingCity}</p>

      <p>⭐ {worker.averageRating || 0}</p>

      <p>🧰 {worker.experience || 0} yrs exp</p>

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
        className="mt-4 bg-blue-600 text-white px-4 py-2 rounded-lg w-full"
      >
        View Profile
      </button>

    </div>
  );
}

export default WorkerCard;