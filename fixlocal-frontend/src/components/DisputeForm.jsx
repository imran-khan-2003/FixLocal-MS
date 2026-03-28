
import { useState } from 'react';

function DisputeForm({ bookingId, onSubmit, onCancel }) {
  const [reason, setReason] = useState('');
  const [desiredOutcome, setDesiredOutcome] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit({ bookingId, reason, desiredOutcome });
  };

  return (
    <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full flex items-center justify-center">
      <div className="bg-white p-8 rounded-lg shadow-xl w-full max-w-md">
        <h2 className="text-2xl font-bold mb-4">Report an Issue</h2>
        <form onSubmit={handleSubmit}>
          <div className="mb-4">
            <label htmlFor="reason" className="block text-sm font-medium text-gray-700">Reason for Dispute</label>
            <textarea
              id="reason"
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              className="mt-1 block w-full px-3 py-2 bg-white border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
              rows="4"
              required
            ></textarea>
          </div>
          <div className="mb-6">
            <label htmlFor="desiredOutcome" className="block text-sm font-medium text-gray-700">Desired Outcome</label>
            <input
              type="text"
              id="desiredOutcome"
              value={desiredOutcome}
              onChange={(e) => setDesiredOutcome(e.target.value)}
              className="mt-1 block w-full px-3 py-2 bg-white border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
              required
            />
          </div>
          <div className="flex justify-end gap-4">
            <button type="button" onClick={onCancel} className="px-4 py-2 bg-gray-200 text-gray-800 rounded-md hover:bg-gray-300">Cancel</button>
            <button type="submit" className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700">Submit Dispute</button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default DisputeForm;
