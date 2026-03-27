import { useState } from "react";
import { useNavigate } from "react-router-dom";

function SearchBar() {

  const [city, setCity] = useState("");
  const [service, setService] = useState("");

  const navigate = useNavigate();

  const handleSearch = () => {

    if (!city || !service) {
      alert("Please enter city and service");
      return;
    }

    navigate(`/search?city=${city}&service=${service}`);

  };

  return (

    <div className="flex justify-center gap-4">

      <input
        type="text"
        placeholder="Enter city"
        className="px-4 py-2 rounded text-black"
        value={city}
        onChange={(e) => setCity(e.target.value)}
      />

      <select
        className="px-4 py-2 rounded text-black"
        value={service}
        onChange={(e) => setService(e.target.value)}
      >

        <option value="">Select Service</option>
        <option value="electrician">Electrician</option>
        <option value="plumber">Plumber</option>
        <option value="carpenter">Carpenter</option>

      </select>

      <button
        onClick={handleSearch}
        className="bg-black text-white px-6 py-2 rounded"
      >
        Search
      </button>

    </div>

  );
}

export default SearchBar;