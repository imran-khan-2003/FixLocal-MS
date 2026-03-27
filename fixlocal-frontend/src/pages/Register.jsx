import { useState } from "react";
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

  return (
    <div className="min-h-screen bg-slate-50 py-12">
      <div className="max-w-2xl mx-auto bg-white shadow-lg rounded-2xl p-8">
        <h1 className="text-2xl font-bold mb-6 text-center">Create an account</h1>
        {error && <p className="text-red-500 mb-4 text-sm">{error}</p>}
        <form className="grid grid-cols-1 md:grid-cols-2 gap-4" onSubmit={handleSubmit}>
          <input
            name="name"
            value={form.name}
            onChange={handleChange}
            placeholder="Full Name"
            className="border rounded-lg p-3"
            required
          />
          <input
            type="email"
            name="email"
            value={form.email}
            onChange={handleChange}
            placeholder="Email"
            className="border rounded-lg p-3"
            required
          />
          <input
            type="password"
            name="password"
            value={form.password}
            onChange={handleChange}
            placeholder="Password"
            className="border rounded-lg p-3"
            required
          />
          <div className="md:col-span-2">
            <label className="flex flex-col text-sm">
              <span className="text-slate-600 mb-1">Phone number</span>
              <PhoneInputComponent
                country={form.phone?.startsWith("+") ? undefined : "in"}
                value={form.phone}
                onChange={(value) =>
                  setForm((prev) => ({ ...prev, phone: value.startsWith("+") ? value : `+${value}` }))
                }
                inputProps={{ required: true, name: "phone" }}
                countryCodeEditable={false}
                enableSearch
                inputClass="!w-full !h-12 !text-base"
                buttonClass="!h-12"
                dropdownClass="!text-sm"
              />
            </label>
          </div>
          <select
            name="role"
            value={form.role}
            onChange={handleChange}
            className="border rounded-lg p-3"
          >
            {roles.map((role) => (
              <option key={role.value} value={role.value}>
                {role.label}
              </option>
            ))}
          </select>
          {isTradesperson && (
            <>
              <input
                name="workingCity"
                value={form.workingCity}
                onChange={handleChange}
                placeholder="City"
                className="border rounded-lg p-3"
                required
              />
              <input
                name="occupation"
                value={form.occupation}
                onChange={handleChange}
                placeholder="Occupation"
                className="border rounded-lg p-3"
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
                className="border rounded-lg p-3"
                required
              />
            </>
          )}
          <div className="md:col-span-2">
            <button
              type="submit"
              disabled={loading}
              className="w-full bg-blue-600 text-white py-3 rounded-lg"
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