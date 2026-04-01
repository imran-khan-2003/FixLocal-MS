import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import testimonialApi from "../api/testimonialService";

export const services = [
  { value: "electrician", label: "Electrician" },
  { value: "plumber", label: "Plumber" },
  { value: "carpenter", label: "Carpenter" },
  { value: "painter", label: "Painter" },
  { value: "cleaning", label: "Cleaning" },
  { value: "ac-repair", label: "AC Repair" },
  { value: "appliance-repair", label: "Appliance Repair" },
  { value: "pest-control", label: "Pest Control" },
  { value: "waterproofing", label: "Waterproofing" },
];

const trustBadges = [
  {
    title: "ID & Document Check",
    copy: "Every pro uploads KYC documents that are revalidated quarterly.",
  },
  {
    title: "Insurance Verified",
    copy: "We request liability cover proof for high-risk trades like roofing.",
  },
  {
    title: "Real Reviews Only",
    copy: "Ratings are tied to completed FixLocal bookings for authenticity.",
  },
];

const highlights = [
  {
    label: "Trades screened",
    value: "2,400+",
    detail: "interviewed & identity verified",
  },
  {
    label: "Jobs completed",
    value: "18,000+",
    detail: "with escrow protection",
  },
  {
    label: "Avg. rating",
    value: "4.8/5",
    detail: "across all categories",
  },
];

const howItWorks = [
  {
    step: "1",
    title: "Search by city & trade",
    copy: "Filter by service, price band, rating, and availability in seconds.",
  },
  {
    step: "2",
    title: "Chat & confirm",
    copy: "Send photos, discuss requirements, and lock pricing inside the app.",
  },
  {
    step: "3",
    title: "Track & pay securely",
    copy: "Follow live ETA, release escrow only when you mark the job complete.",
  },
];

function Home() {
  const navigate = useNavigate();
  const { isAuthenticated, user } = useAuth();
  const [city, setCity] = useState("");
  const [allCities, setAllCities] = useState([]);
  const [citySuggestions, setCitySuggestions] = useState([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const cityDropdownRef = useRef(null);
  const [query, setQuery] = useState("Electrician");
  const [testimonials, setTestimonials] = useState([]);
  const [testimonialError, setTestimonialError] = useState("");
  const [testimonialForm, setTestimonialForm] = useState({
    name: "",
    city: "",
    role: "Homeowner",
    quote: "",
  });
  const [testimonialSubmitting, setTestimonialSubmitting] = useState(false);
  const [testimonialSuccess, setTestimonialSuccess] = useState("");
  const [testimonialFormError, setTestimonialFormError] = useState("");

  const handleSearch = () => {
    if (!city.trim()) return;
    const url = new URLSearchParams({ city: city.trim() });
    if (query.trim()) url.append("occupation", query.trim());
    navigate(`/search?${url.toString()}`);
  };

  useEffect(() => {
    testimonialApi
      .list(6)
      .then(({ data }) => setTestimonials(data || []))
      .catch(() => setTestimonialError("Unable to load community stories"));
  }, []);

  useEffect(() => {
    let cancelled = false;
    const controller = new AbortController();

    async function fetchCities() {
      try {
        const response = await fetch(
          "https://countriesnow.space/api/v0.1/countries/cities",
          {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ country: "India" }),
            signal: controller.signal,
          }
        );
        const payload = await response.json();
        if (!cancelled && payload?.data) {
          setAllCities(Array.from(new Set(payload.data)).sort((a, b) => a.localeCompare(b)));
        }
      } catch (error) {
        if (!cancelled) {
          console.warn("Could not load city list", error);
        }
      }
    }

    fetchCities();

    return () => {
      cancelled = true;
      controller.abort();
    };
  }, []);

  useEffect(() => {
    function handleClickOutside(event) {
      if (cityDropdownRef.current && !cityDropdownRef.current.contains(event.target)) {
        setShowSuggestions(false);
      }
    }

    if (showSuggestions) {
      document.addEventListener("mousedown", handleClickOutside);
    }

    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [showSuggestions]);

  const handleCityInput = (value) => {
    setCity(value);
    if (!value.trim()) {
      setCitySuggestions([]);
      setShowSuggestions(false);
      return;
    }
    const prefix = value.toLowerCase();
    const matches = allCities.filter((name) => name.toLowerCase().startsWith(prefix));
    setCitySuggestions(matches);
    setShowSuggestions(matches.length > 0);
  };

  const selectCity = (value) => {
    setCity(value);
    setShowSuggestions(false);
  };

  useEffect(() => {
    if (user) {
      setTestimonialForm((prev) => ({
        ...prev,
        name: user.name || prev.name,
        role: user.role === "TRADESPERSON" ? "Tradesperson" : "Homeowner",
      }));
    }
  }, [user]);

  const handleTestimonialChange = (field, value) => {
    setTestimonialForm((prev) => ({ ...prev, [field]: value }));
  };

  const handleTestimonialSubmit = async (event) => {
    event.preventDefault();
    setTestimonialFormError("");
    setTestimonialSuccess("");
    const payload = {
      name: testimonialForm.name.trim(),
      city: testimonialForm.city.trim(),
      role: testimonialForm.role.trim(),
      quote: testimonialForm.quote.trim(),
    };
    if (!payload.name || !payload.city || !payload.quote) {
      setTestimonialFormError("Please fill in your name, city, and testimonial.");
      return;
    }
    if (payload.quote.length < 20) {
      setTestimonialFormError("Tell us a bit more (minimum 20 characters).");
      return;
    }
    try {
      setTestimonialSubmitting(true);
      const { data } = await testimonialApi.submit(payload);
      setTestimonials((prev) => [data, ...prev].slice(0, 6));
      setTestimonialSuccess("Thanks for sharing your FixLocal story!");
      setTestimonialForm((prev) => ({ ...prev, quote: "" }));
    } catch (error) {
      setTestimonialFormError("Unable to save testimonial. Please try again.");
    } finally {
      setTestimonialSubmitting(false);
    }
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
            <div className="relative flex-1" ref={cityDropdownRef}>
              <input
                value={city}
                onChange={(e) => handleCityInput(e.target.value)}
                onFocus={() => city && setShowSuggestions(citySuggestions.length > 0)}
                placeholder="Enter city"
                className="w-full p-3 rounded-lg text-gray-900"
                autoComplete="off"
              />
              {showSuggestions && citySuggestions.length > 0 && (
                <div className="absolute z-20 mt-2 max-h-64 w-full overflow-y-auto rounded-xl border border-slate-200 bg-white text-left shadow-lg">
                  {citySuggestions.map((option) => (
                    <button
                      key={option}
                      type="button"
                      className="block w-full px-4 py-2 text-sm text-slate-700 hover:bg-primary/10"
                      onClick={() => selectCity(option)}
                    >
                      {option}
                    </button>
                  ))}
                </div>
              )}
            </div>
            <select
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              className="flex-1 p-3 rounded-lg text-gray-900"
            >
              <option value="">All Services</option>
              {services.map((service) => (
                <option key={service.value} value={service.value}>
                  {service.label}
                </option>
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

      <section className="max-w-6xl mx-auto py-14 px-6 grid gap-6 md:grid-cols-3">
        {trustBadges.map((badge) => (
          <div key={badge.title} className="bg-white p-6 rounded-2xl shadow border border-slate-100">
            <span className="text-2xl">🔒</span>
            <h3 className="text-xl font-semibold mt-3">{badge.title}</h3>
            <p className="text-slate-600 mt-2">{badge.copy}</p>
          </div>
        ))}
      </section>

      <section className="bg-slate-900 text-white">
        <div className="max-w-5xl mx-auto py-14 px-6 grid gap-6 md:grid-cols-3">
          {highlights.map((item) => (
            <div key={item.label} className="text-center">
              <p className="text-4xl font-bold">{item.value}</p>
              <p className="text-sm uppercase tracking-wide mt-2 text-white/70">{item.label}</p>
              <p className="text-white/80 mt-1">{item.detail}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="max-w-6xl mx-auto py-14 px-6">
        <h2 className="text-3xl font-bold text-slate-900 text-center mb-8">
          Why homeowners love FixLocal
        </h2>
        {testimonialError && (
          <p className="text-center text-sm text-red-500 mb-4">{testimonialError}</p>
        )}
        <div className="grid gap-6 md:grid-cols-3">
          {testimonials.map((t) => (
            <div key={t.id || t.name} className="bg-white rounded-2xl p-6 shadow border border-slate-100">
              <p className="text-slate-700 italic">“{t.quote}”</p>
              <div className="mt-4 text-sm text-slate-500">
                <p className="font-semibold text-slate-900">{t.name}</p>
                <p>{t.city} · {t.role}</p>
              </div>
            </div>
          ))}
        </div>
        <div className="mt-10">
          {isAuthenticated ? (
            <form
              onSubmit={handleTestimonialSubmit}
              className="bg-white rounded-2xl p-6 shadow border border-slate-100 grid gap-4 md:grid-cols-2"
            >
              <div className="col-span-1">
                <label className="block text-sm font-semibold text-slate-700">Name</label>
                <input
                  type="text"
                  className="mt-2 w-full border border-slate-200 rounded-lg px-3 py-2"
                  value={testimonialForm.name}
                  onChange={(e) => handleTestimonialChange("name", e.target.value)}
                />
              </div>
              <div className="col-span-1">
                <label className="block text-sm font-semibold text-slate-700">City</label>
                <input
                  type="text"
                  className="mt-2 w-full border border-slate-200 rounded-lg px-3 py-2"
                  value={testimonialForm.city}
                  onChange={(e) => handleTestimonialChange("city", e.target.value)}
                />
              </div>
              <div>
                <label className="block text-sm font-semibold text-slate-700">I’m a</label>
                <select
                  className="mt-2 w-full border border-slate-200 rounded-lg px-3 py-2"
                  value={testimonialForm.role}
                  onChange={(e) => handleTestimonialChange("role", e.target.value)}
                >
                  <option value="Homeowner">Homeowner</option>
                  <option value="Tradesperson">Tradesperson</option>
                </select>
              </div>
              <div className="md:col-span-2">
                <label className="block text-sm font-semibold text-slate-700">Share your FixLocal experience</label>
                <textarea
                  className="mt-2 w-full border border-slate-200 rounded-lg px-3 py-2"
                  rows={4}
                  value={testimonialForm.quote}
                  onChange={(e) => handleTestimonialChange("quote", e.target.value)}
                  placeholder="What did you book and how did it go?"
                />
              </div>
              {testimonialFormError && (
                <p className="text-sm text-red-500 md:col-span-2">{testimonialFormError}</p>
              )}
              {testimonialSuccess && (
                <p className="text-sm text-emerald-600 md:col-span-2">{testimonialSuccess}</p>
              )}
              <div className="md:col-span-2 flex justify-end">
                <button
                  type="submit"
                  className="bg-primary text-white px-5 py-2 rounded-full disabled:opacity-60"
                  disabled={testimonialSubmitting}
                >
                  {testimonialSubmitting ? "Saving..." : "Submit testimonial"}
                </button>
              </div>
            </form>
          ) : (
            <p className="text-center text-sm text-slate-500">
              Sign in to share your FixLocal experience.
            </p>
          )}
        </div>
      </section>

      <section className="bg-gradient-to-r from-slate-100 to-white border-y border-slate-200">
        <div className="max-w-6xl mx-auto py-16 px-6 grid gap-6 md:grid-cols-3">
          {howItWorks.map((item) => (
            <div key={item.step} className="bg-white rounded-2xl p-6 shadow-sm border border-slate-100">
              <div className="w-10 h-10 rounded-full bg-primary/10 text-primary font-semibold flex items-center justify-center">
                {item.step}
              </div>
              <h3 className="text-xl font-semibold mt-4">{item.title}</h3>
              <p className="text-slate-600 mt-2">{item.copy}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="max-w-6xl mx-auto py-16 px-6 grid gap-6 md:grid-cols-2">
        <div className="bg-white p-6 rounded-2xl shadow border border-slate-100">
          <h3 className="text-2xl font-bold text-slate-900">Popular categories</h3>
          <p className="text-slate-600 mt-2">
            Trending FixLocal requests across metros — tap to explore specialists instantly.
          </p>
          <div className="grid grid-cols-2 gap-3 mt-4">
            {services.map((item) => (
              <button
                key={item.value}
                onClick={() =>
                  navigate(`/search?city=${encodeURIComponent(city || "Bengaluru")}&service=${encodeURIComponent(item.value)}`)
                }
                className="text-left px-3 py-2 rounded-lg border border-slate-200 hover:bg-primary/5 text-sm"
              >
                {item.label}
              </button>
            ))}
          </div>
        </div>
        <div className="bg-primary text-white rounded-2xl p-6 shadow-lg">
          <h3 className="text-2xl font-bold">FixLocal Assurance</h3>
          <p className="text-white/80 mt-2">
            Every booking includes ₹25,000 workmanship cover plus dispute mediation. Need help choosing a pro? Chat with FixLocal concierge on
            WhatsApp or phone — we’ll shortlist the best matches for you.
          </p>
          <ul className="mt-4 space-y-2 text-white/90">
            <li>• Escrow-backed payments</li>
            <li>• Mandatory background re-check every 6 months</li>
            <li>• Instant rebooking if a pro cancels</li>
          </ul>
          <button
            onClick={() => navigate("/register")}
            className="mt-5 inline-flex items-center justify-center rounded-full bg-white text-primary font-semibold px-6 py-2"
          >
            Become a verified pro
          </button>
        </div>
      </section>
    </>
  );
}

export default Home;
