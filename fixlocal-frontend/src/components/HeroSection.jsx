import SearchBar from "./SearchBar";

function HeroSection() {
  return (
    <div
      className="bg-primary text-white py-24 text-center"
      style={{
        backgroundImage:
          "linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5)), url('https://images.unsplash.com/photo-1581578731548-c64695cc6952?ixlib=rb-1.2.1&auto=format&fit=crop&w=1350&q=80')",
        backgroundSize: "cover",
        backgroundPosition: "center",
      }}
    >
      <h1 className="text-5xl font-bold">
        Find Trusted Local Workers
      </h1>

      <p className="mt-4 text-lg text-gray-200">
        Electricians • Plumbers • Carpenters
      </p>

      <div className="mt-10">
        <SearchBar />
      </div>
    </div>
  );
}

export default HeroSection;
