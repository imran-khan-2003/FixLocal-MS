import { useEffect, useState } from "react";
import { useLocation } from "react-router-dom";
import api from "../api/axios";
import WorkerCard from "../components/WorkerCard";

function SearchResults() {
  const location = useLocation();
  const params = new URLSearchParams(location.search);
  const city = params.get("city") ?? "";
  const occupation = params.get("occupation") ?? "";

  const [workers, setWorkers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function fetchWorkers() {
      if (!city) return;
      setLoading(true);
      setError("");
      try {
        const url = occupation
          ? `/tradespersons/search?city=${encodeURIComponent(city)}&occupation=${encodeURIComponent(
              occupation
            )}`
          : `/tradespersons/search?city=${encodeURIComponent(city)}`;
        const { data } = await api.get(url);
        const results = data.content || data || [];
        setWorkers(results);
      } catch (err) {
        setError("Failed to load tradespersons. Please try again.");
      } finally {
        setLoading(false);
      }
    }
    fetchWorkers();
  }, [city, occupation]);

  return (
    <div className="max-w-6xl mx-auto py-12 px-6">
      <h1 className="text-3xl font-bold mb-6">
        {occupation ? `${occupation} in ${city}` : `Tradespersons in ${city}`}
      </h1>
      {loading && <p className="text-gray-500">Loading workers...</p>}
      {error && <p className="text-red-500 mb-4">{error}</p>}
      {!loading && !error && workers.length === 0 && (
        <p className="text-slate-600">No tradespersons found for this search.</p>
      )}
      <div className="grid gap-6 md:grid-cols-3">
        {workers.map((worker) => (
          <WorkerCard key={worker.id} worker={worker} />
        ))}
      </div>
    </div>
  );
}

export default SearchResults;