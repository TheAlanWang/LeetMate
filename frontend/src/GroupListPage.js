import React, { useState, useMemo } from 'react';
import { ChevronDown, Search } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from './App';

const API_BASE = process.env.REACT_APP_API_BASE || 'http://localhost:8080';

const GroupListPage = () => {
  const navigate = useNavigate();
  const { token, user } = useAuth();
  
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [selectedSubcategory, setSelectedSubcategory] = useState('all');
  const [searchQuery, setSearchQuery] = useState('');
  const [toast, setToast] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [fetchedGroups, setFetchedGroups] = useState([]);
  const [joinedGroupIds, setJoinedGroupIds] = useState(new Set());

  // Fetch groups from backend
  React.useEffect(() => {
    const fetchGroups = async () => {
      setLoading(true);
      setError(null);
      try {
        const response = await fetch(`${API_BASE}/groups?page=0&size=50`);
        const data = await response.json().catch(() => ({}));
        if (!response.ok) {
          throw new Error(data.message || 'Failed to load groups');
        }
        setFetchedGroups(data.content || []);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };
    fetchGroups();
  }, []);

  // Fetch joined groups for current user to mark joined state
  React.useEffect(() => {
    if (!user || !token) {
      setJoinedGroupIds(new Set());
      return;
    }
    const fetchJoined = async () => {
      try {
        const response = await fetch(`${API_BASE}/groups/members/${user.id}`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        const data = await response.json().catch(() => ({}));
        if (!response.ok) {
          throw new Error(data.message || 'Failed to load joined groups');
        }
        const ids = new Set((data || []).map((g) => g.id));
        setJoinedGroupIds(ids);
      } catch {
        // silently ignore
      }
    };
    fetchJoined();
  }, [user, token]);

  const categories = [
    { value: 'all', label: 'All Categories' },
    { value: 'leetcode', label: 'LeetCode' },
    { value: 'system_design', label: 'System Design' }
  ];

  const subcategories = {
    leetcode: [
      { value: 'all', label: 'All Topics' },
      { value: 'Array', label: 'Array' },
      { value: 'Stack/Queue', label: 'Stack/Queue' },
      { value: 'Tree', label: 'Tree' },
      { value: 'Backtracking', label: 'Backtracking' },
      { value: 'Dynamic Programming', label: 'Dynamic Programming' },
      { value: 'Graph', label: 'Graph' }
    ],
    system_design: [],
    all: []
  };

  // Filter groups based on selected filters
  const filteredGroups = useMemo(() => {
    return (fetchedGroups || []).filter((group) => {
      const tagsLower = (group.tags || []).map((t) => t.toLowerCase());
      const searchLower = searchQuery.toLowerCase();
      const searchMatch =
        group.name.toLowerCase().includes(searchLower) ||
        group.description.toLowerCase().includes(searchLower) ||
        tagsLower.some((t) => t.includes(searchLower));
      const categoryMatch =
        selectedCategory === 'all' ||
        (group.category || '').toLowerCase() === selectedCategory ||
        group.tags?.map((t) => t.toLowerCase()).includes(selectedCategory);
      const subcategoryMatch =
        selectedSubcategory === 'all' ||
        group.tags?.map((t) => t.toLowerCase()).includes(selectedSubcategory);
      return categoryMatch && subcategoryMatch && searchMatch;
    });
  }, [fetchedGroups, selectedCategory, selectedSubcategory, searchQuery]);

  const handleJoinGroup = async (groupId) => {
    if (!token) {
      setToast('Please log in first.');
      setTimeout(() => setToast(null), 3000);
      return;
    }
    
    setToast('Successfully joined the group!');
    setTimeout(() => setToast(null), 3000);
    // 实际应用中可以在这里做 API 调用并跳转
    setTimeout(() => navigate(`/groups/${groupId}`), 500);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white border-b border-gray-200">
        <div className="max-w-6xl mx-auto px-4 py-6">
          <h1 className="text-4xl font-bold text-gray-900">Browse Groups</h1>
          <p className="text-gray-600 mt-2">Find and join study groups that match your interests</p>
        </div>
      </div>

      {/* Main Content */}
      <div className="max-w-6xl mx-auto px-4 py-8">
        {/* Toast */}
        {toast && (
          <div className="bg-emerald-50 border border-emerald-200 text-emerald-900 px-4 py-3 rounded-lg mb-6 text-sm">
            {toast}
          </div>
        )}
        {error && (
          <div className="bg-rose-50 border border-rose-200 text-rose-900 px-4 py-3 rounded-lg mb-6 text-sm">
            {error}
          </div>
        )}

        {/* Search Bar */}
        <div className="mb-8">
          <div className="relative">
            <Search className="absolute left-3 top-3.5 text-gray-400" size={20} />
            <input
              type="text"
              placeholder="Search groups by name or description..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg outline-none focus:ring-2 focus:ring-teal-400"
            />
          </div>
        </div>

        {/* Filters */}
        <div className="bg-white rounded-xl border border-gray-200 p-4 mb-6">
          <div className="flex items-center justify-between mb-3">
            <h3 className="text-base font-semibold text-gray-900">Filter by Category</h3>
            <button
              onClick={() => {
                setSelectedCategory('all');
                setSelectedSubcategory('all');
                setSearchQuery('');
              }}
              className="text-teal-600 hover:text-teal-700 font-medium text-sm"
            >
              Reset
            </button>
          </div>
          <div className="flex items-center gap-4 flex-wrap">
            <select
              value={selectedCategory}
              onChange={(e) => {
                setSelectedCategory(e.target.value);
                setSelectedSubcategory('all');
              }}
              className="border border-gray-300 rounded-lg px-3 py-2 outline-none focus:ring-2 focus:ring-teal-400 text-sm"
            >
              {categories.map((cat) => (
                <option key={cat.value} value={cat.value}>
                  {cat.label}
                </option>
              ))}
            </select>
            {selectedCategory === 'leetcode' && (
              <select
                value={selectedSubcategory}
                onChange={(e) => setSelectedSubcategory(e.target.value)}
                className="border border-gray-300 rounded-lg px-3 py-2 outline-none focus:ring-2 focus:ring-teal-400 text-sm"
              >
                {subcategories.leetcode.map((subcat) => (
                  <option key={subcat.value} value={subcat.value}>
                    {subcat.label}
                  </option>
                ))}
              </select>
            )}
          </div>
        </div>

        {/* Groups Grid */}
        <div>
          <h3 className="text-xl font-bold text-gray-900 mb-4">
            {loading ? 'Loading groups...' : `${filteredGroups.length} Group${filteredGroups.length !== 1 ? 's' : ''} Found`}
          </h3>
          
          {loading ? (
            <div className="bg-white rounded-xl border border-gray-200 p-12 text-center">
              <p className="text-gray-500 text-lg">Loading...</p>
            </div>
          ) : filteredGroups.length === 0 ? (
            <div className="bg-white rounded-xl border border-gray-200 p-12 text-center">
              <p className="text-gray-500 text-lg">No groups found matching your filters.</p>
            </div>
          ) : (
            <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
              {filteredGroups.map((group) => (
                <GroupCard
                  key={group.id}
                  group={group}
                  joined={joinedGroupIds.has(group.id)}
                  onJoin={() => handleJoinGroup(group.id)}
                  onViewDetails={() => navigate(`/groups/${group.id}`)}
                />
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

// Group Card Component
const GroupCard = ({ group, onJoin, onViewDetails, joined }) => {
  const tags = Array.from(new Set(group.tags || []));

  return (
    <div className="bg-white rounded-xl p-6 shadow-sm hover:shadow-lg transition-all duration-300 border border-gray-200 hover:border-teal-400">
      <div className="mb-4">
        <h3 
          className="text-lg font-bold text-gray-900 mb-2 hover:text-teal-500 transition cursor-pointer"
          onClick={onViewDetails}
        >
          {group.name}
        </h3>
        <p className="text-gray-600 text-sm mb-3">{group.description}</p>
        
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-4 text-sm text-gray-600">
            <span>👤 {group.memberCount} members</span>
            <span>🎓 {group.mentorName}</span>
          </div>
        </div>

        {/* Tags */}
        <div className="flex flex-wrap gap-2 mb-4">
          {tags.map((tag) => (
            <span key={tag} className="px-2 py-1 text-xs bg-gray-100 text-gray-700 rounded-full">
              {tag}
            </span>
          ))}
        </div>
      </div>

      <div className="flex gap-2">
        <button
          onClick={onViewDetails}
          className="flex-1 px-4 py-2 border border-teal-500 text-teal-600 rounded-lg hover:bg-teal-50 transition font-medium text-sm"
        >
          View Details
        </button>
        <button
          onClick={onJoin}
          disabled={joined}
          className={`flex-1 px-4 py-2 rounded-lg transition font-medium text-sm ${joined ? 'bg-teal-100 text-teal-600 cursor-default' : 'bg-teal-500 text-white hover:bg-teal-600'}`}
        >
          {joined ? 'Joined' : 'Join'}
        </button>
      </div>
    </div>
  );
};

export default GroupListPage;
