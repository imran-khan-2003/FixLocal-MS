
import { useEffect, useState } from 'react';
import { getAllDisputes, updateDispute } from '../../api/adminService'; // Assuming you'll create this function

function Disputes() {
  const [disputes, setDisputes] = useState([]);

  useEffect(() => {
    getAllDisputes().then(setDisputes).catch(console.error);
  }, []);

  const handleUpdateStatus = (id, status) => {
    updateDispute(id, { status })
      .then(updatedDispute => {
        setDisputes(disputes.map(d => d.id === id ? updatedDispute : d));
      })
      .catch(console.error);
  };

  return (
    <div className="container mx-auto px-4 sm:px-8">
      <div className="py-8">
        <div>
          <h2 className="text-2xl font-semibold leading-tight">Disputes</h2>
        </div>
        <div className="-mx-4 sm:-mx-8 px-4 sm:px-8 py-4 overflow-x-auto">
          <div className="inline-block min-w-full shadow rounded-lg overflow-hidden">
            <table className="min-w-full leading-normal">
              <thead>
                <tr>
                  <th className="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Booking ID</th>
                  <th className="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Reason</th>
                  <th classNamepx-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Status</th>
                  <th className="px-5 py-3 border-b-2 border-gray-200 bg-gray-100"></th>
                </tr>
              </thead>
              <tbody>
                {disputes.map(dispute => (
                  <tr key={dispute.id}>
                    <td className="px-5 py-5 border-b border-gray-200 bg-white text-sm">{dispute.bookingId}</td>
                    <td className="px-5 py-5 border-b border-gray-200 bg-white text-sm">{dispute.reason}</td>
                    <td className="px-5 py-5 border-b border-gray-200 bg-white text-sm">{dispute.status}</td>
                    <td className="px-5 py-5 border-b border-gray-200 bg-white text-sm text-right">
                      <select 
                        value={dispute.status}
                        onChange={(e) => handleUpdateStatus(dispute.id, e.target.value)} 
                        className="py-2 px-3 border border-gray-300 bg-white rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                      >
                        <option value="OPEN">Open</option>
                        <option value="UNDER_REVIEW">Under Review</option>
                        <option value="RESOLVED">Resolved</option>
                        <option value="CLOSED">Closed</option>
                      </select>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Disputes;
