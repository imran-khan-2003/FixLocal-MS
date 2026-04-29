function PageLoader() {
  return (
    <div className="fixed inset-0 z-[9999] flex items-center justify-center bg-slate-950/75 backdrop-blur-md">
      <div className="relative flex flex-col items-center justify-center gap-4">
        <div className="relative flex h-36 w-36 items-center justify-center">
          <span className="absolute inset-0 rounded-full border-4 border-indigo-200/40 border-t-indigo-500 animate-spin" />
          <span className="absolute inset-3 rounded-full border border-cyan-300/50 animate-pulse" />
          <span className="absolute inset-6 rounded-full border border-fuchsia-300/30 animate-ping" />
          <img
            src="/logo.png"
            alt="FixLocal loading"
            className="relative z-10 h-20 w-20 object-contain animate-soft-float"
          />
        </div>
        <p className="text-gradient-fire text-sm font-semibold tracking-wide">Crafting your experience...</p>
      </div>
    </div>
  );
}

export default PageLoader;