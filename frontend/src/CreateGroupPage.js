import React, { useState } from 'react';
import { useAuth } from './App';
import TagSelector from './components/TagSelector';

const API_BASE = process.env.REACT_APP_API_BASE || 'http://localhost:8080';

const CreateGroupPage = () => {
  const { isMentor, token } = useAuth();
  const [groupForm, setGroupForm] = useState({ name: '', description: '', tags: [] });
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState(null);

  if (!isMentor) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="bg-white border border-gray-200 rounded-xl shadow-sm p-6 text-center">
          <p className="text-gray-700">Only mentors can create groups and challenges.</p>
        </div>
      </div>
    );
  }

  const handleGroupSubmit = async (e) => {
    e.preventDefault();
    if (!token) {
      setToast('Please log in first.');
      return;
    }
    setLoading(true);
    try {
      const payload = {
        name: groupForm.name,
        description: groupForm.description,
        tags: groupForm.tags
      };
      const response = await fetch(`${API_BASE}/groups`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify(payload)
      });
      const data = await response.json().catch(() => ({}));
      if (!response.ok) {
        throw new Error(data.message || 'Failed to create group');
      }
      setToast(`Group "${data.name}" created successfully!`);
      setGroupForm({ name: '', description: '', tags: [] });
    } catch (error) {
      setToast(error.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-6xl mx-auto px-4 py-10">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">Create a Study Group</h1>
        <p className="text-gray-600 mb-6">Set up a new study group with tags to help mentees discover it.</p>

        {toast && (
          <div className="bg-emerald-50 border border-emerald-200 text-emerald-900 px-4 py-3 rounded-lg mb-4 text-sm">
            {toast}
          </div>
        )}

        <div className="grid grid-cols-1 gap-6">
          <form onSubmit={handleGroupSubmit} className="bg-white border border-gray-200 rounded-2xl p-6 shadow-sm">
            <input
              type="text"
              value={groupForm.name}
              onChange={(e) => setGroupForm((prev) => ({ ...prev, name: e.target.value }))}
              placeholder="Group Name"
              className="w-full border border-gray-300 rounded-lg px-4 py-3 mb-3 outline-none focus:ring-2 focus:ring-teal-400"
              required
            />
            <textarea
              value={groupForm.description}
              onChange={(e) => setGroupForm((prev) => ({ ...prev, description: e.target.value }))}
              placeholder="Group Description"
              rows={3}
              className="w-full border border-gray-300 rounded-lg px-4 py-3 mb-3 outline-none focus:ring-2 focus:ring-teal-400"
              required
            />
            <div className="mb-4">
              <TagSelector
                value={groupForm.tags}
                onChange={(tags) => setGroupForm((prev) => ({ ...prev, tags }))}
                max={5}
              />
            </div>
            <button
              type="submit"
              disabled={loading}
              className="w-full bg-teal-500 text-white rounded-lg px-4 py-3 font-semibold hover:bg-teal-600 transition"
            >
              {loading ? 'Creating...' : 'Create Group'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default CreateGroupPage;
