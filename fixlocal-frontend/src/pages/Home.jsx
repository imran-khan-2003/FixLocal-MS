import { useState } from "react";
import { useNavigate } from "react-router-dom";

const services = [
  "Electrician",
  "Plumber",
  "Carpenter",
  "Painter",
  "AC Repair",
  "Cleaning",
];

function Home() {
  const navigate = useNavigate();
  const [city, setCity] = useState("");
  const [query, setQuery] = useState("Electrician");

  const handleSearch = () => {
    if (!city.trim()) return;
    const url = new URLSearchParams({ city: city.trim() });
    if (query.trim()) url.append("occupation", query.trim());
    navigate(`/search?${url.toString()}`);
  };

  return (
    <>
      <section className="bg-gradient-to-r from-blue-600 to-purple-600 text-white py-20 px-6">
        <div className="max-w-4xl mx-auto text-center">
          <h1 className="text-4xl md:text-5xl font-bold mb-4">
            Book trusted pros for any job
          </h1>
          <p className="text-lg text-white/90 mb-8">
            Instant bookings, live tracking, secure payments, and in-app chat.
          </p>

          <div className="bg-white/10 backdrop-blur p-6 rounded-2xl flex flex-col md:flex-row gap-4">
            <input
              value={city}
              onChange={(e) => setCity(e.target.value)}
              placeholder="Enter city"
              className="flex-1 p-3 rounded-lg text-gray-900"
            />
            <select
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              className="flex-1 p-3 rounded-lg text-gray-900"
            >
              {services.map((service) => (
                <option key={service}>{service}</option>
              ))}
            </select>
            <button
              onClick={handleSearch}
              className="bg-white text-blue-600 font-semibold px-6 py-3 rounded-lg"
            >
              Search
            </button>
          </div>
        </div>
      </section>

      <section className="max-w-6xl mx-auto py-16 px-6 grid md:grid-cols-3 gap-6">
        {["Dynamic Pricing", "Real-time Chat", "Escrow Payments"].map((title) => (
          <div key={title} className="bg-white p-6 rounded-2xl shadow">
            <h3 className="text-xl font-semibold mb-2">{title}</h3>
            <p className="text-slate-600">
              {title === "Dynamic Pricing" &&
                "Negotiate offers and counter-offers until you lock the best price."}
              {title === "Real-time Chat" &&
                "Stay connected with tradespersons using secure in-app messaging."}
              {title === "Escrow Payments" &&
                "Funds remain in escrow until the job is completed to your satisfaction."}
            </p>
          </div>
        ))}
      </section>
    </>
  );
}

export default Home;
