import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";
import "react-phone-input-2/lib/style.css";
import PhoneInput from "react-phone-input-2";
const roles = [
  { value: "USER", label: "Customer" },
  { value: "TRADESPERSON", label: "Tradesperson" },
  { value: "ADMIN", label: "Admin" },
];

function Register() {
  const PhoneInputComponent = PhoneInput.default || PhoneInput;
  const navigate = useNavigate();
  const [form, setForm] = useState({
    name: "",
    email: "",
    password: "",
    phone: "",
    role: "USER",
    occupation: "",
    workingCity: "",
    experience: "",
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [allCities, setAllCities] = useState([]);
  const [citySuggestions, setCitySuggestions] = useState([]);
  const [showCitySuggestions, setShowCitySuggestions] = useState(false);
  const cityDropdownRef = useRef(null);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
        const payload = {
          ...form,
          phone: form.phone,
          experience: form.experience ? Number(form.experience) : undefined,
        };
        if (!isTradesperson) {
          delete payload.workingCity;
          delete payload.occupation;
          delete payload.experience;
        }

        await api.post("/auth/register", payload);
      navigate("/login");
    } catch (err) {
      setError(err?.response?.data?.message || "Registration failed");
    } finally {
      setLoading(false);
    }
  };

  const isTradesperson = form.role === "TRADESPERSON";

  useEffect(() => {
    let cancelled = false;
    const controller = new AbortController();

    async function fetchCities() {
      try {
        const response = await fetch("https://countriesnow.space/api/v0.1/countries/cities", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ country: "India" }),
          signal: controller.signal,
        });
        const payload = await response.json();
        if (!cancelled && payload?.data) {
          const uniqueCities = Array.from(new Set(payload.data)).sort((a, b) => a.localeCompare(b));
          setAllCities(uniqueCities);
        }
      } catch (err) {
        if (!cancelled) {
          console.warn("Failed to load city list", err);
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
        setShowCitySuggestions(false);
      }
    }

    if (showCitySuggestions) {
      document.addEventListener("mousedown", handleClickOutside);
    }

    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [showCitySuggestions]);

  const handleWorkingCityInput = (value) => {
    setForm((prev) => ({ ...prev, workingCity: value }));
    if (!value.trim()) {
      setCitySuggestions([]);
      setShowCitySuggestions(false);
      return;
    }
    const prefix = value.toLowerCase();
    const matches = allCities.filter((city) => city.toLowerCase().startsWith(prefix));
    setCitySuggestions(matches);
    setShowCitySuggestions(matches.length > 0);
  };

  const handleSelectWorkingCity = (value) => {
    setForm((prev) => ({ ...prev, workingCity: value }));
    setShowCitySuggestions(false);
  };

  return (
    <div className="flex items-center justify-center py-12">
      <div className="max-w-2xl w-full bg-white shadow-lg rounded-2xl p-8">
        <h1 className="text-2xl font-bold mb-6 text-center text-text-primary">Create an account</h1>
        {error && <p className="text-red-500 mb-4 text-sm">{error}</p>}
        <form className="grid grid-cols-1 md:grid-cols-2 gap-4" onSubmit={handleSubmit}>
          <input
            name="name"
            value={form.name}
            onChange={handleChange}
            placeholder="Full Name"
            className="border rounded-lg p-3 w-full focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary"
            required
          />
          <input
            type="email"
            name="email"
            value={form.email}
            onChange={handleChange}
            placeholder="Email"
            className="border rounded-lg p-3 w-full focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary"
            required
          />
          <input
            type="password"
            name="password"
            value={form.password}
            onChange={handleChange}
            placeholder="Password"
            className="border rounded-lg p-3 w-full focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary"
            required
          />
          <div className="md:col-span-2">
            <label className="flex flex-col text-sm">
              <span className="text-text-secondary mb-1">Phone number</span>
              <PhoneInputComponent
                country={form.phone?.startsWith("+") ? undefined : "in"}
                value={form.phone}
                onChange={(value) =>
                  setForm((prev) => ({ ...prev, phone: value.startsWith("+") ? value : `+${value}` }))
                }
                inputProps={{ required: true, name: "phone" }}
                countryCodeEditable={false}
                enableSearch
                inputClass="!w-full !h-12 !text-base !border-gray-300 !rounded-lg focus:!border-primary focus:!outline-none focus:!ring-1 focus:!ring-primary"
                buttonClass="!h-12 !border-gray-300 !rounded-lg"
                dropdownClass="!text-sm"
              />
            </label>
          </div>
          <select
            name="role"
            value={form.role}
            onChange={handleChange}
            className="border rounded-lg p-3 w-full focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary"
          >
            {roles.map((role) => (
              <option key={role.value} value={role.value}>
                {role.label}
              </option>
            ))}
          </select>
          {isTradesperson && (
            <>
              <div className="relative" ref={cityDropdownRef}>
                <input
                  name="workingCity"
                  value={form.workingCity}
                  onChange={(e) => handleWorkingCityInput(e.target.value)}
                  onFocus={() =>
                    form.workingCity && setShowCitySuggestions(citySuggestions.length > 0)
                  }
                  placeholder="City"
                  autoComplete="off"
                  className="border rounded-lg p-3 w-full focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary"
                  required
                />
                {showCitySuggestions && citySuggestions.length > 0 && (
                  <div className="absolute z-20 mt-1 max-h-60 w-full overflow-y-auto rounded-lg border border-slate-200 bg-white shadow-lg">
                    {citySuggestions.map((city) => (
                      <button
                        key={city}
                        type="button"
                        className="block w-full px-4 py-2 text-left text-sm text-slate-700 hover:bg-slate-100"
                        onClick={() => handleSelectWorkingCity(city)}
                      >
                        {city}
                      </button>
                    ))}
                  </div>
                )}
              </div>
              <input
                name="occupation"
                value={form.occupation}
                onChange={handleChange}
                placeholder="Occupation"
                className="border rounded-lg p-3 w-full focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary"
                required
              />
              <input
                type="number"
                name="experience"
                value={form.experience}
                onChange={handleChange}
                placeholder="Experience (years)"
                min="0"
                max="60"
                className="border rounded-lg p-3 w-full focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary"
                required
              />
            </>
          )}
          <div className="md:col-span-2">
            <button
              type="submit"
              disabled={loading}
              className="w-full bg-primary text-white py-3 rounded-lg transition hover:bg-accent"
            >
              {loading ? "Creating account..." : "Register"}
            </button>
          </div>
        </form>
      </div>
      </div>
  );
}

export default Register;