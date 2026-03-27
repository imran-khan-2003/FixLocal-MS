import Navbar from "./Navbar";

function DashboardLayout({ title, subtitle, actions, children }) {
  return (
    <div className="min-h-screen bg-slate-50">
      <Navbar />
      <header className="max-w-6xl mx-auto px-6 py-8 flex flex-wrap gap-4 justify-between items-center">
        <div>
          {subtitle && <p className="text-sm text-slate-500">{subtitle}</p>}
          <h1 className="text-3xl font-bold text-slate-800">{title}</h1>
        </div>
        {actions && <div className="flex gap-2">{actions}</div>}
      </header>
      <main className="max-w-6xl mx-auto px-6 pb-16">{children}</main>
    </div>
  );
}

export default DashboardLayout;