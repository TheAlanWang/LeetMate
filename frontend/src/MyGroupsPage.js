import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from './App';

const API_BASE = process.env.REACT_APP_API_BASE || 'http://localhost:8080';

const MyGroupsPage = () => {
  const { user, token } = useAuth();
  const navigate = useNavigate();

  const [groups, setGroups] = useState([]);
  const [groupError, setGroupError] = useState(null);
  const [loadingGroups, setLoadingGroups] = useState(true);

  useEffect(() => {
    if (!user || !token) {
      setLoadingGroups(false);
      return;
    }
    const fetchGroups = async () => {
      setLoadingGroups(true);
      setGroupError(null);
      try {
        const response = await fetch(`${API_BASE}/groups/members/${user.id}`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        const data = await response.json().catch(() => ({}));
        if (!response.ok) {
          throw new Error(data.message || 'Failed to load your groups');
        }
        setGroups(data || []);
      } catch (err) {
        setGroupError(err.message);
      } finally {
        setLoadingGroups(false);
      }
    };
    fetchGroups();
  }, [user, token]);

  if (!user || !token) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 text-center">
          <p className="text-gray-700 mb-4">Please log in to view your groups.</p>
          <button
            onClick={() => navigate('/login')}
            className="px-5 py-2 bg-teal-500 text-white rounded-lg hover:bg-teal-600 transition"
          >
            Go to Login
          </button>
        </div>
      </div>
    );
  }

  const renderGroupCard = (g, highlight = false) => (
    <div
      key={g.id}
      onClick={() => navigate(`/groups/${g.id}`)}
      className={`cursor-pointer bg-white border rounded-xl p-4 shadow-sm hover:shadow-md transition ${highlight ? 'border-teal-500 bg-teal-50' : 'border-gray-200'}`}
    >
      <div className="flex items-center justify-between gap-2">
        <h3 className="text-lg font-semibold text-gray-900">{g.name}</h3>
        <span className="px-2 py-1 bg-emerald-50 text-emerald-700 rounded-full text-[11px] font-semibold">
          Free
        </span>
      </div>
      <p className="text-sm text-gray-600 line-clamp-2">{g.description}</p>
      <div className="text-xs text-gray-500 mt-2 flex items-center justify-between">
        <span>{g.memberCount} members</span>
        <div className="flex items-center gap-2">
          {g.mentorName && <span>Mentor: {g.mentorName}</span>}
          {user && g.mentorId === user.id && (
            <span className="px-2 py-1 bg-teal-50 text-teal-700 rounded-full text-[11px] font-semibold">
              Created by me
            </span>
          )}
        </div>
      </div>
    </div>
  );

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-6xl mx-auto px-4 py-8">
        <div className="flex items-center justify-between mb-4">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">My Groups</h1>
            <p className="text-gray-600">Groups you have joined.</p>
          </div>
          <button
            onClick={() => navigate('/find-groups')}
            className="px-4 py-2 text-teal-600 hover:text-teal-700 font-medium"
          >
            Find more groups →
          </button>
        </div>

        <div className="bg-white border border-gray-200 rounded-xl p-6 shadow-sm">
          {groupError && <div className="text-sm text-rose-600 mb-3">{groupError}</div>}
          {loadingGroups ? (
            <div className="text-center text-gray-600 py-6">Loading groups...</div>
          ) : groups.length === 0 ? (
            <div className="text-center text-gray-600 py-6">You haven't joined any groups yet.</div>
          ) : (
            <div className="space-y-3">
              {groups.map((g, idx) => renderGroupCard(g, idx === 0))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default MyGroupsPage;
