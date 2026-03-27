import SearchBar from "./SearchBar";

function HeroSection() {
  return (
    <div className="bg-blue-600 text-white py-24 text-center">

      <h1 className="text-5xl font-bold">
        Find Trusted Local Workers
      </h1>

      <p className="mt-4">
        Electricians • Plumbers • Carpenters
      </p>

      <div className="mt-10">
        <SearchBar />
      </div>

    </div>
  );
}

export default HeroSection;