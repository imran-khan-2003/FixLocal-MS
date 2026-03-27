import { useCallback, useEffect, useMemo, useState } from "react";
import DashboardLayout from "../../components/DashboardLayout";
import api from "../../api/axios";
import { adminService } from "../../api/adminService";

const DEFAULT_PAGE = { content: [], number: 0, totalPages: 0, totalElements: 0 };

function usePaginatedList(fetcher) {
  const [data, setData] = useState(DEFAULT_PAGE);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [page, setPage] = useState(0);

  const load = async () => {
    setLoading(true);
    setError("");
    try {
      const response = await fetcher(page);
      setData(response.data || DEFAULT_PAGE);
    } catch (err) {
      setError("Failed to load data");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, [page, fetcher]);

  return {
    data,
    loading,
    error,
    reload: load,
    page,
    setPage,
  };
}

const tileConfig = [
  { label: "Total Users", key: "totalUsers" },
  { label: "Total Tradespersons", key: "totalTradespersons" },
  { label: "Bookings", key: "totalBookings" },
  { label: "Completed", key: "completedBookings" },
  { label: "Pending", key: "pendingBookings" },
  { label: "Cancelled", key: "cancelledBookings" },
  { label: "Rejected", key: "rejectedBookings" },
  { label: "Active Conversations", key: "activeConversations" },
  { label: "Pending Verifications", key: "pendingVerifications" },
  { label: "Blocked Accounts", key: "blockedAccounts" },
];

function AdminDashboard() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const fetchUsers = useCallback((page) => adminService.getUsers(page), []);
  const fetchTrades = useCallback((page) => adminService.getTradespersons(page), []);
  const usersState = usePaginatedList(fetchUsers);
  const tradesState = usePaginatedList(fetchTrades);
  const [actionError, setActionError] = useState("");

  const loadStats = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const { data } = await api.get("/admin/stats");
      setStats(data);
    } catch (err) {
      setError("Failed to load stats");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadStats();
  }, [loadStats]);

  const kpis = useMemo(
    () =>
      tileConfig.map((tile) => ({
        ...tile,
        value: stats?.[tile.key] ?? "-",
      })),
    [stats]
  );

  const handleBlockToggle = async (userId, blocked) => {
    try {
      setActionError("");
      if (blocked) {
        await adminService.unblockUser(userId);
      } else {
        await adminService.blockUser(userId);
      }
      await Promise.all([usersState.reload(), tradesState.reload(), loadStats()]);
    } catch (err) {
      setActionError("Failed to update user status");
    }
  };

  const renderTable = (title, pageData, state) => (
    <div className="bg-white rounded-2xl shadow border border-slate-100 p-4">
      <div className="flex justify-between items-center mb-3">
        <h3 className="text-lg font-semibold">{title}</h3>
        <div className="text-xs text-slate-500">
          Page {pageData.number + 1} of {Math.max(pageData.totalPages, 1)}
        </div>
      </div>
      {state.error && <p className="text-red-500 text-sm mb-2">{state.error}</p>}
      {state.loading ? (
        <p className="text-slate-500 text-sm">Loading...</p>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-slate-500 uppercase text-xs">
                <th className="py-2">Name</th>
                <th>Email</th>
                <th>Role</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {pageData.content.map((user) => (
                <tr key={user.id} className="border-t border-slate-100">
                  <td className="py-3">
                    <div className="font-semibold text-slate-800">{user.name}</div>
                    <div className="text-xs text-slate-500">
                      {user.occupation || "-"}
                    </div>
                  </td>
                  <td className="text-slate-600">{user.email}</td>
                  <td className="text-slate-600">{user.role}</td>
                  <td>
                    {user.blocked ? (
                      <span className="px-2 py-1 text-xs rounded-full bg-red-100 text-red-600">
                        Blocked
                      </span>
                    ) : (
                      <span className="px-2 py-1 text-xs rounded-full bg-green-100 text-green-600">
                        Active
                      </span>
                    )}
                  </td>
                  <td>
                    <button
                      className="text-xs font-semibold text-blue-600"
                      onClick={() => handleBlockToggle(user.id, user.blocked)}
                    >
                      {user.blocked ? "Unblock" : "Block"}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      <div className="flex justify-between items-center mt-3 text-xs">
        <button
          className="px-3 py-1 rounded border"
          disabled={state.page === 0}
          onClick={() => state.setPage((p) => Math.max(p - 1, 0))}
        >
          Previous
        </button>
        <button
          className="px-3 py-1 rounded border"
          disabled={pageData.number + 1 >= pageData.totalPages}
          onClick={() => state.setPage((p) => p + 1)}
        >
          Next
        </button>
      </div>
    </div>
  );

  return (
    <DashboardLayout title="Platform Insights" subtitle="Live health metrics">
      {error && <p className="text-red-500 mb-4">{error}</p>}
      {actionError && <p className="text-red-500 mb-4">{actionError}</p>}
      {loading ? (
        <p className="text-slate-600">Loading metrics...</p>
      ) : (
        <>
          <div className="grid md:grid-cols-3 lg:grid-cols-4 gap-4 mb-8">
            {kpis.map((tile) => (
              <div key={tile.key} className="bg-white p-4 rounded-2xl shadow border border-slate-100">
                <p className="text-xs uppercase tracking-wide text-slate-500">{tile.label}</p>
                <p className="text-2xl font-bold text-slate-900 mt-1">{tile.value}</p>
              </div>
            ))}
          </div>
          <div className="bg-white rounded-2xl shadow border border-slate-100 p-6">
            <h3 className="text-lg font-semibold mb-4">Operational Notes</h3>
            <ul className="space-y-2 text-sm text-slate-600">
              <li>• Pending verifications help prioritize KYC queue.</li>
              <li>• Active conversations indicates current load on support.</li>
              <li>• Blocked accounts highlight compliance actions.</li>
            </ul>
          </div>
          <div className="grid lg:grid-cols-2 gap-6 mt-6">
            {renderTable("Users", usersState.data, usersState)}
            {renderTable("Tradespersons", tradesState.data, tradesState)}
          </div>
        </>
      )}
    </DashboardLayout>
  );
}

export default AdminDashboard;