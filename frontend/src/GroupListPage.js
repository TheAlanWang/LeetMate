import React, { useState, useMemo } from 'react';
import { ChevronDown, Search } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from './App';

const API_BASE = process.env.REACT_APP_API_BASE || 'http://localhost:8080';

const GroupListPage = () => {
  const navigate = useNavigate();
  const { user, token, isMentee } = useAuth();
  
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [selectedSubcategory, setSelectedSubcategory] = useState('all');
  const [searchQuery, setSearchQuery] = useState('');
  const [toast, setToast] = useState(null);

  // Demo Groups Data
  const allGroups = [
    {
      id: 1,
      name: 'Array Basics',
      description: 'Master array manipulation and common array problems',
      category: 'leetcode',
      subcategory: 'Array',
      mentorName: 'John Smith',
      memberCount: 24,
      tags: ['Easy', 'Beginner']
    },
    {
      id: 2,
      name: 'Stack & Queue Advanced',
      description: 'Deep dive into stack and queue data structures',
      category: 'leetcode',
      subcategory: 'Stack/Queue',
      mentorName: 'Sarah Chen',
      memberCount: 18,
      tags: ['Medium', 'Advanced']
    },
    {
      id: 3,
      name: 'Binary Tree Patterns',
      description: 'Learn all common binary tree traversal patterns',
      category: 'leetcode',
      subcategory: 'Tree',
      mentorName: 'Mike Johnson',
      memberCount: 32,
      tags: ['Medium', 'Important']
    },
    {
      id: 4,
      name: 'Backtracking Techniques',
      description: 'Master backtracking with N-Queens, Permutations, etc.',
      category: 'leetcode',
      subcategory: 'Backtracking',
      mentorName: 'Alice Wong',
      memberCount: 15,
      tags: ['Hard', 'Advanced']
    },
    {
      id: 5,
      name: 'DP Problem Solving',
      description: 'From 0-1 Knapsack to Complex DP Problems',
      category: 'leetcode',
      subcategory: 'Dynamic Programming',
      mentorName: 'David Lee',
      memberCount: 28,
      tags: ['Hard', 'Popular']
    },
    {
      id: 6,
      name: 'Graph Algorithms',
      description: 'BFS, DFS, Dijkstra, and more graph algorithms',
      category: 'leetcode',
      subcategory: 'Graph',
      mentorName: 'Emma Brown',
      memberCount: 22,
      tags: ['Hard', 'Important']
    },
    {
      id: 7,
      name: 'System Design Basics',
      description: 'Learn the fundamentals of system design',
      category: 'system_design',
      subcategory: null,
      mentorName: 'Robert Taylor',
      memberCount: 45,
      tags: ['Beginner', 'Popular']
    },
    {
      id: 8,
      name: 'Scalable Architecture',
      description: 'Design systems that scale to millions of users',
      category: 'system_design',
      subcategory: null,
      mentorName: 'Lisa Anderson',
      memberCount: 38,
      tags: ['Advanced', 'Popular']
    },
    {
      id: 9,
      name: 'Microservices & APIs',
      description: 'Building robust microservices and APIs',
      category: 'system_design',
      subcategory: null,
      mentorName: 'James Wilson',
      memberCount: 26,
      tags: ['Advanced']
    }
  ];

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
    return allGroups.filter((group) => {
      const categoryMatch = selectedCategory === 'all' || group.category === selectedCategory;
      const subcategoryMatch = selectedSubcategory === 'all' || group.subcategory === selectedSubcategory;
      const searchMatch = group.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
                         group.description.toLowerCase().includes(searchQuery.toLowerCase());
      
      return categoryMatch && subcategoryMatch && searchMatch;
    });
  }, [selectedCategory, selectedSubcategory, searchQuery]);

  const handleJoinGroup = async (groupId) => {
    if (!isMentee) {
      setToast('Please log in as a mentee to join a group.');
      setTimeout(() => setToast(null), 3000);
      return;
    }
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
        <div className="bg-white rounded-xl border border-gray-200 p-6 mb-8">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Filter by Category</h3>
          
          <div className="space-y-4">
            {/* Category Filter */}
            <div>
              <label className="text-sm font-medium text-gray-600 block mb-2">Category</label>
              <select
                value={selectedCategory}
                onChange={(e) => {
                  setSelectedCategory(e.target.value);
                  setSelectedSubcategory('all');
                }}
                className="w-full md:w-64 border border-gray-300 rounded-lg px-4 py-2 outline-none focus:ring-2 focus:ring-teal-400"
              >
                {categories.map((cat) => (
                  <option key={cat.value} value={cat.value}>
                    {cat.label}
                  </option>
                ))}
              </select>
            </div>

            {/* Subcategory Filter - only show for LeetCode */}
            {selectedCategory === 'leetcode' && (
              <div>
                <label className="text-sm font-medium text-gray-600 block mb-2">Topic</label>
                <select
                  value={selectedSubcategory}
                  onChange={(e) => setSelectedSubcategory(e.target.value)}
                  className="w-full md:w-64 border border-gray-300 rounded-lg px-4 py-2 outline-none focus:ring-2 focus:ring-teal-400"
                >
                  {subcategories.leetcode.map((subcat) => (
                    <option key={subcat.value} value={subcat.value}>
                      {subcat.label}
                    </option>
                  ))}
                </select>
              </div>
            )}

            {/* Reset Button */}
            <button
              onClick={() => {
                setSelectedCategory('all');
                setSelectedSubcategory('all');
                setSearchQuery('');
              }}
              className="text-teal-600 hover:text-teal-700 font-medium text-sm"
            >
              Reset Filters
            </button>
          </div>
        </div>

        {/* Groups Grid */}
        <div>
          <h3 className="text-xl font-bold text-gray-900 mb-4">
            {filteredGroups.length} Group{filteredGroups.length !== 1 ? 's' : ''} Found
          </h3>
          
          {filteredGroups.length === 0 ? (
            <div className="bg-white rounded-xl border border-gray-200 p-12 text-center">
              <p className="text-gray-500 text-lg">No groups found matching your filters.</p>
            </div>
          ) : (
            <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
              {filteredGroups.map((group) => (
                <GroupCard
                  key={group.id}
                  group={group}
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
const GroupCard = ({ group, onJoin, onViewDetails }) => {
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

        {/* Category Badge */}
        <div className="mb-3">
          <span className="inline-block px-2 py-1 text-xs bg-teal-50 text-teal-700 rounded-full font-medium">
            {group.category === 'leetcode' ? 'LeetCode' : 'System Design'}
            {group.subcategory && ` - ${group.subcategory}`}
          </span>
        </div>

        {/* Tags */}
        <div className="flex flex-wrap gap-2 mb-4">
          {group.tags.map((tag) => (
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
          className="flex-1 px-4 py-2 bg-teal-500 text-white rounded-lg hover:bg-teal-600 transition font-medium text-sm"
        >
          Join
        </button>
      </div>
    </div>
  );
};

export default GroupListPage;