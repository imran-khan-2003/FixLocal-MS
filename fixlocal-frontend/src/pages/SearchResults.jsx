import { useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import api from "../api/axios";
import WorkerCard from "../components/WorkerCard";
import SearchBar from "../components/SearchBar";

function SearchResults() {
  const location = useLocation();
  const params = new URLSearchParams(location.search);
  const navigate = useNavigate();
  const cityParam = params.get("city") ?? "";
  const serviceParam = params.get("service") ?? params.get("occupation") ?? "";
  const [city, setCity] = useState(cityParam);
  const [service, setService] = useState(serviceParam);
  const [priceCap, setPriceCap] = useState("");
  const [minRating, setMinRating] = useState(0);
  const [sortBy, setSortBy] = useState("relevance");

  const [workers, setWorkers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function fetchWorkers() {
      if (!city) {
        setWorkers([]);
        return;
      }
      setLoading(true);
      setError("");
      try {
        const query = new URLSearchParams({ city });
        if (service) {
          query.append("occupation", service);
        }
        if (priceCap) {
          query.append("maxPrice", priceCap);
        }
        if (minRating) {
          query.append("minRating", String(minRating));
        }
        if (sortBy && sortBy !== "relevance") {
          query.append("sort", sortBy);
        }

        const url = `/tradespersons/search?${query.toString()}`;
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
  }, [city, service, priceCap, minRating, sortBy]);

  useEffect(() => {
    const filtered = workers
      .filter((worker) => {
        const basePrice = worker.basePrice ?? worker.price;
        if (priceCap && basePrice && Number(basePrice) > Number(priceCap)) {
          return false;
        }
        if (minRating && worker.averageRating && worker.averageRating < minRating) {
          return false;
        }
        return true;
      })
      .sort((a, b) => {
        const priceA = a.basePrice ?? a.price;
        const priceB = b.basePrice ?? b.price;
        if (sortBy === "price" && priceA != null && priceB != null) {
          return priceA - priceB;
        }
        if (sortBy === "rating") {
          const ratingA = a.averageRating ?? 0;
          const ratingB = b.averageRating ?? 0;
          return ratingB - ratingA;
        }
        return 0;
      });
    setVisible(filtered);
  }, [workers, priceCap, minRating, sortBy]);

  const [visible, setVisible] = useState([]);

  const handleSearch = ({ city: nextCity, service: nextService }) => {
    setCity(nextCity);
    setService(nextService);

    const params = new URLSearchParams({ city: nextCity });
    if (nextService) {
      params.append("service", nextService);
    }

    navigate(`/search?${params.toString()}`);
  };

  return (
    <div className="max-w-6xl mx-auto py-12 px-6 space-y-8">
      <div className="space-y-6">
        <SearchBar initialCity={city} initialService={service} onSearch={handleSearch} />
        <div className="flex flex-wrap gap-4 bg-white border border-slate-200 rounded-2xl p-4 shadow-sm">
          <div>
            <p className="text-xs text-slate-500 uppercase">Max price (₹)</p>
            <input
              type="number"
              className="mt-2 border rounded-lg px-3 py-2 text-sm"
              value={priceCap}
              onChange={(e) => setPriceCap(e.target.value)}
            />
          </div>
          <div>
            <p className="text-xs text-slate-500 uppercase">Minimum rating</p>
            <select
              className="mt-2 border rounded-lg px-3 py-2 text-sm"
              value={minRating}
              onChange={(e) => setMinRating(Number(e.target.value))}
            >
              {[0, 3, 3.5, 4, 4.5].map((rating) => (
                <option key={rating} value={rating}>
                  {rating === 0 ? "Any" : `${rating}+ ★`}
                </option>
              ))}
            </select>
          </div>
          <div>
            <p className="text-xs text-slate-500 uppercase">Sort by</p>
            <select
              className="mt-2 border rounded-lg px-3 py-2 text-sm"
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value)}
            >
              <option value="relevance">Relevance</option>
              <option value="price">Lowest price</option>
              <option value="rating">Highest rating</option>
            </select>
          </div>
        </div>
      </div>
      <h1 className="text-3xl font-bold mb-2 text-text-primary">
        {service ? `${service} in ${city || "your area"}` : `Tradespersons in ${city || "your area"}`}
      </h1>
      {loading && <p className="text-text-secondary">Loading workers...</p>}
      {error && <p className="text-red-500 mb-4">{error}</p>}
      {!loading && !error && visible.length === 0 && (
        <p className="text-text-secondary">No tradespersons found for this search.</p>
      )}
      <div className="grid gap-6 md:grid-cols-3">
        {visible.map((worker) => (
          <WorkerCard key={worker.id} worker={worker} />
        ))}
      </div>
    </div>
  );
}

export default SearchResults;
