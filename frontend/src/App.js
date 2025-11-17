import React, { useState, useEffect, useCallback, createContext, useContext, useRef } from 'react';
import { Search, Menu, X, ChevronDown } from 'lucide-react';
import { BrowserRouter as Router, Routes, Route, Link, useNavigate } from 'react-router-dom';
import logo from './assets/logo.png';
import GroupPage from './GroupPage';
import LoginPage from './LoginPage';
import GroupListPage from './GroupListPage';
import MyGroupsPage from './MyGroupsPage';
import CreateGroupPage from './CreateGroupPage';

const resolveApiBase = () => {
  const explicitBase = process.env.REACT_APP_API_BASE;
  if (explicitBase) {
    return explicitBase;
  }
  if (typeof window !== 'undefined') {
    const { protocol, hostname } = window.location;
    return `${protocol}//${hostname}:8080`;
  }
  return 'http://localhost:8080';
};

const API_BASE = resolveApiBase();

const AuthContext = createContext();
const useAuth = () => useContext(AuthContext);

const readCachedAuth = () => {
  if (typeof window === 'undefined') {
    return { user: null, token: null };
  }
  try {
    const cached = window.localStorage.getItem('leetmateAuth');
    return cached ? JSON.parse(cached) : { user: null, token: null };
  } catch (error) {
    console.warn('Failed to parse cached auth', error);
    return { user: null, token: null };
  }
};

const AuthProvider = ({ children }) => {
  const [initial] = useState(() => readCachedAuth());
  const [user, setUser] = useState(initial.user);
  const [token, setToken] = useState(initial.token);

  const persistAuth = useCallback(() => {
    if (typeof window === 'undefined') {
      return;
    }
    if (user && token) {
      window.localStorage.setItem('leetmateAuth', JSON.stringify({ user, token }));
    } else {
      window.localStorage.removeItem('leetmateAuth');
    }
  }, [user, token]);

  useEffect(() => {
    persistAuth();
  }, [persistAuth]);

  const performAuthRequest = async (path, payload) => {
    const response = await fetch(`${API_BASE}${path}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
      throw new Error(data.message || 'Request failed, please try again');
    }
    return data;
  };

  const handleAuthSuccess = (data) => {
    setUser(data.user);
    setToken(data.token);
  };

  const login = async (email, password) => {
    try {
      const data = await performAuthRequest('/auth/login', { email, password });
      handleAuthSuccess(data);
      return { success: true };
    } catch (error) {
      return { success: false, message: error.message };
    }
  };

  const register = async ({ name, email, password, role }) => {
    try {
      const data = await performAuthRequest('/auth/register', { name, email, password, role });
      handleAuthSuccess(data);
      return { success: true };
    } catch (error) {
      return { success: false, message: error.message };
    }
  };

  const requestPasswordReset = async (email) => {
    try {
      await performAuthRequest('/auth/password/forgot', { email });
      return { success: true };
    } catch (error) {
      return { success: false, message: error.message };
    }
  };

  const confirmPasswordReset = async ({ token, password }) => {
    try {
      await performAuthRequest('/auth/password/reset', { token, newPassword: password });
      return { success: true };
    } catch (error) {
      return { success: false, message: error.message };
    }
  };

  const logout = () => {
    setUser(null);
    setToken(null);
  };

  const value = {
    user,
    token,
    login,
    register,
    requestPasswordReset,
    confirmPasswordReset,
    logout,
    isMentor: user?.role === 'MENTOR',
    isMentee: user?.role === 'MENTEE'
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

const Navbar = () => {
  const { user, logout, isMentor } = useAuth();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [groupMenuOpen, setGroupMenuOpen] = useState(false);
  const groupMenuRef = useRef(null);
  const navigate = useNavigate();

  const handleNavClick = (item) => {
    switch (item) {
      case 'Coding Groups':
      case 'Find Group':
        navigate('/find-groups');
        break;
      case 'My Group':
        if (user) {
          navigate('/my-groups');
        } else {
          navigate('/login');
        }
        break;
      case 'Create Group':
        if (user && isMentor) {
          navigate('/create-group');
        } else {
          navigate('/login');
        }
        break;
      default:
        console.log('Navigate to:', item);
    }
    setMobileMenuOpen(false);
    setGroupMenuOpen(false);
  };

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (groupMenuRef.current && !groupMenuRef.current.contains(e.target)) {
        setGroupMenuOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  return (
    <nav className="bg-white border-b border-gray-300" style={{ boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)' }}>
      <div className="max-w-7xl mx-auto px-6 py-4">
        <div className="flex justify-between items-center">
          <div className="flex items-center space-x-3">
            <img
              src={logo}
              alt="LeetMate"
              className="h-12 w-auto cursor-pointer hover:opacity-80 transition"
              onClick={() => navigate('/')}
            />
            <h1 
              className="text-2xl font-bold text-gray-900 cursor-pointer hover:text-teal-500 transition"
              onClick={() => navigate('/')}
            >
              LeetMate
            </h1>
          </div>

          <div className="hidden md:flex items-center space-x-8">
            <div className="relative" ref={groupMenuRef}>
              <button
                onClick={() => setGroupMenuOpen((prev) => !prev)}
                className="text-gray-700 hover:text-teal-500 transition font-medium inline-flex items-center gap-1"
              >
                Group
                <ChevronDown size={16} />
              </button>
              {groupMenuOpen && (
                <div className="absolute mt-2 w-44 bg-white border border-gray-200 rounded-lg shadow-lg py-2 z-20">
                  <button
                    onClick={() => handleNavClick('My Group')}
                    className="block w-full text-left px-4 py-2 text-gray-700 hover:bg-gray-50"
                  >
                    My Group
                  </button>
                  <button
                    onClick={() => handleNavClick('Find Group')}
                    className="block w-full text-left px-4 py-2 text-gray-700 hover:bg-gray-50"
                  >
                    Find Group
                  </button>
                  {isMentor && (
                    <button
                      onClick={() => handleNavClick('Create Group')}
                      className="block w-full text-left px-4 py-2 text-gray-700 hover:bg-gray-50"
                    >
                      Create Group
                    </button>
                  )}
                </div>
              )}
            </div>
            {['Resume Review', 'My Mentor', 'Community']
              .map((item) => (
                <button 
                  key={item} 
                  onClick={() => handleNavClick(item)}
                  className="text-gray-700 hover:text-teal-500 transition font-medium"
                >
                  {item}
                </button>
              ))}
          </div>

          <div className="hidden md:flex items-center space-x-4">
            {user ? (
              <>
                <span className="text-gray-700 font-medium">
                  Welcome, {user.name} ({user.role})
                </span>
                <button
                  onClick={logout}
                  className="px-5 py-2 border border-gray-900 text-gray-900 rounded hover:bg-gray-900 hover:text-white transition font-medium"
                >
                  Logout
                </button>
              </>
            ) : (
              <>
                <Link
                  to="/login"
                  className="px-5 py-2 border border-gray-900 text-gray-900 rounded hover:bg-gray-900 hover:text-white transition font-medium"
                >
                  Log in
                </Link>
                <Link
                  to="/login"
                  className="px-5 py-2 bg-teal-500 text-white rounded hover:bg-teal-600 transition font-medium"
                >
                  Be a Mentor
                </Link>
              </>
            )}
          </div>

          <button
            className="md:hidden"
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          >
            {mobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
          </button>
        </div>

        {mobileMenuOpen && (
          <div className="md:hidden mt-4 space-y-3 pb-4">
            <button
              onClick={() => handleNavClick('My Group')}
              className="block w-full text-left px-4 py-2 text-gray-700 hover:bg-gray-50 rounded-lg border border-gray-100"
            >
              My Group
            </button>
            <button
              onClick={() => handleNavClick('Find Group')}
              className="block w-full text-left px-4 py-2 text-gray-700 hover:bg-gray-50 rounded-lg border border-gray-100"
            >
              Find Group
            </button>
            {['Resume Review', 'My Mentor', 'Community'].map((item) => (
              <button
                key={item}
                onClick={() => handleNavClick(item)}
                className="block w-full text-left px-4 py-2 text-gray-700 hover:bg-gray-50 rounded-lg border border-gray-100"
              >
                {item}
              </button>
            ))}
            {!user && (
              <Link
                to="/login"
                className="block text-center px-4 py-2 border border-gray-900 text-gray-900 rounded hover:bg-gray-900 hover:text-white transition font-medium"
              >
                Log in
              </Link>
            )}
          </div>
        )}
      </div>
    </nav>
  );
};

const SearchBar = () => {
  const [searchQuery, setSearchQuery] = useState('');
  const [searchType, setSearchType] = useState('Explore');

  const handleSearch = () => {
    console.log('Search type:', searchType, 'Search content:', searchQuery);
  };

  return (
    <div className="bg-gray-50 pt-6 pb-2">
      <div className="max-w-4xl mx-auto px-4">
        <div className="flex items-center bg-white rounded-full overflow-hidden border border-gray-300" style={{ boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)' }}>
          <select
            value={searchType}
            onChange={(e) => setSearchType(e.target.value)}
            className="px-6 py-3 bg-white border-r border-gray-300 outline-none cursor-pointer hover:bg-gray-50 transition"
          >
            {['Explore', 'Groups', 'Mentors', 'Topics'].map((option) => (
              <option key={option}>{option}</option>
            ))}
          </select>
          <input
            type="text"
            placeholder="What do you want to learn?"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
            className="flex-1 px-6 py-3 outline-none"
          />
          <button
            onClick={handleSearch}
            className="px-6 py-3 bg-teal-400 text-white hover:bg-teal-500 transition"
          >
            <Search size={20} />
          </button>
        </div>
      </div>
    </div>
  );
};

const RoleSelection = () => {
  const [currentTextIndex, setCurrentTextIndex] = useState(0);
  const [displayedText, setDisplayedText] = useState('');
  const [isDeleting, setIsDeleting] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const texts = ['Coding Practice', 'Mock Interviews', 'System Design', 'Resume Review'];
    const currentFullText = texts[currentTextIndex];
    const timeout = setTimeout(() => {
      if (!isDeleting) {
        if (displayedText.length < currentFullText.length) {
          setDisplayedText(currentFullText.slice(0, displayedText.length + 1));
        } else {
          setTimeout(() => setIsDeleting(true), 2000);
        }
      } else if (displayedText.length > 0) {
        setDisplayedText(displayedText.slice(0, -1));
      } else {
        setIsDeleting(false);
        setCurrentTextIndex((prev) => (prev + 1) % texts.length);
      }
    }, isDeleting ? 50 : 100);
    return () => clearTimeout(timeout);
  }, [displayedText, isDeleting, currentTextIndex]);

  return (
    <div className="bg-gradient-to-b from-teal-50 via-white to-white py-16 md:py-20 mt-0 border-b border-teal-100">
      <div className="max-w-[1200px] mx-auto px-4 flex flex-col md:flex-row items-center justify-between gap-10">
        <div className="text-center md:text-left max-w-2xl">
          <h1 className="text-6xl md:text-7xl font-bold text-gray-900 mb-4">
            Join Mentor's Group
          </h1>
          <h2 className="text-5xl md:text-6xl font-bold text-teal-500 mb-6 min-h-[1.2em]">
            Start {displayedText}
            <span className="animate-pulse">|</span>
          </h2>
          <p className="text-xl md:text-2xl text-gray-600 leading-relaxed">
            Ship real code, practice interviews, and design systems with mentors.
          </p>
        </div>
        <div className="w-full md:w-auto">
          <button
            onClick={() => navigate('/find-groups')}
            className="w-full md:w-72 bg-gradient-to-r from-teal-400 to-teal-500 text-white rounded-xl p-5 cursor-pointer hover:shadow-xl transform hover:scale-[1.02] transition-all duration-300 text-left"
          >
            <h3 className="text-2xl md:text-3xl font-bold mb-1">Find your Group</h3>
            <p className="text-base opacity-90">Browse and join study groups</p>
          </button>
        </div>
      </div>
    </div>
  );
};

const GroupCard = ({ group, onRefresh, onMessage }) => {
  const { token } = useAuth();
  const [joining, setJoining] = useState(false);
  const navigate = useNavigate();

  const joinGroup = async () => {
    if (!token) {
      onMessage?.('Please log in first.');
      return;
    }
    setJoining(true);
    try {
      const response = await fetch(`${API_BASE}/groups/${group.id}/join`, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      const data = await response.json().catch(() => ({}));
      if (!response.ok) {
        throw new Error(data.message || 'Failed to join group');
      }
      onMessage?.(`Successfully joined "${data.name}" group!`);
      onRefresh?.();
      navigate(`/groups/${group.id}`);
    } catch (error) {
      onMessage?.(error.message);
    } finally {
      setJoining(false);
    }
  };

  return (
    <div className="bg-white rounded-xl p-6 shadow-sm hover:shadow-lg transition-all duration-300 border border-gray-200 hover:border-teal-400">
      <div 
        className="flex justify-between items-start mb-4 cursor-pointer"
        onClick={() => navigate(`/groups/${group.id}`)}
      >
        <div className="flex-1">
          <h3 className="text-xl font-bold text-gray-900 mb-2 hover:text-teal-500 transition">
            {group.name}
          </h3>
          <p className="text-gray-600 text-sm mb-2">{group.description}</p>
          <p className="text-gray-700 text-sm">
            Mentor: <span className="font-semibold">{group.mentorName || 'TBD'}</span>
          </p>
        </div>
        <div className="bg-teal-50 px-4 py-2 rounded-lg text-right ml-4">
          <p className="text-2xl font-bold text-teal-600">{group.memberCount}</p>
          <p className="text-sm text-gray-600">members</p>
        </div>
      </div>
      <div className="flex flex-wrap gap-2 mb-4">
        {group.tags?.map((tag) => (
          <span key={tag} className="px-3 py-1 text-xs bg-gray-100 text-gray-700 rounded-full">
            {tag}
          </span>
        ))}
      </div>
      <button
        onClick={joinGroup}
        disabled={joining}
        className="w-full px-6 py-2 bg-teal-500 text-white rounded-lg hover:bg-teal-600 transition font-medium"
      >
        {joining ? 'Joining...' : 'Join Group'}
      </button>
    </div>
  );
};

const MentorActions = ({ onSuccess, onMessage }) => {
  const { isMentor, token } = useAuth();
  const [groupForm, setGroupForm] = useState({ name: '', description: '', tags: '' });
  const [challengeForm, setChallengeForm] = useState({
    groupId: '',
    title: '',
    description: '',
    language: 'java',
    difficulty: 'easy',
    starterCode: 'class Solution {}'
  });
  const [loading, setLoading] = useState({ group: false, challenge: false });

  if (!isMentor) {
    return null;
  }

  const handleGroupSubmit = async (e) => {
    e.preventDefault();
    if (!token) {
      onMessage?.('Please log in first.');
      return;
    }
    setLoading((prev) => ({ ...prev, group: true }));
    try {
      const payload = {
        name: groupForm.name,
        description: groupForm.description,
        tags: groupForm.tags.split(',').map((tag) => tag.trim()).filter(Boolean)
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
      onMessage?.(`Group "${data.name}" created successfully!`);
      setGroupForm({ name: '', description: '', tags: '' });
      onSuccess?.();
    } catch (error) {
      onMessage?.(error.message);
    } finally {
      setLoading((prev) => ({ ...prev, group: false }));
    }
  };

  const handleChallengeSubmit = async (e) => {
    e.preventDefault();
    if (!token) {
      onMessage?.('Please log in first.');
      return;
    }
    setLoading((prev) => ({ ...prev, challenge: true }));
    try {
      const { groupId, ...rest } = challengeForm;
      const response = await fetch(`${API_BASE}/groups/${groupId}/challenges`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify(rest)
      });
      const data = await response.json().catch(() => ({}));
      if (!response.ok) {
        throw new Error(data.message || 'Failed to create challenge');
      }
      onMessage?.(`Challenge "${data.title}" posted successfully!`);
      setChallengeForm((prev) => ({ ...prev, title: '', description: '', starterCode: 'class Solution {}' }));
    } catch (error) {
      onMessage?.(error.message);
    } finally {
      setLoading((prev) => ({ ...prev, challenge: false }));
    }
  };

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mt-10">
      <form onSubmit={handleGroupSubmit} className="bg-white border border-gray-200 rounded-2xl p-6 shadow-sm">
        <h3 className="text-xl font-semibold mb-4 text-gray-900">Create a Study Group</h3>
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
        <input
          type="text"
          value={groupForm.tags}
          onChange={(e) => setGroupForm((prev) => ({ ...prev, tags: e.target.value }))}
          placeholder="Tags (comma separated)"
          className="w-full border border-gray-300 rounded-lg px-4 py-3 mb-4 outline-none focus:ring-2 focus:ring-teal-400"
        />
        <button
          type="submit"
          disabled={loading.group}
          className="w-full bg-teal-500 text-white rounded-lg py-3 font-medium hover:bg-teal-600 transition"
        >
          {loading.group ? 'Creating...' : 'Create Group'}
        </button>
      </form>

      <form onSubmit={handleChallengeSubmit} className="bg-white border border-gray-200 rounded-2xl p-6 shadow-sm">
        <h3 className="text-xl font-semibold mb-4 text-gray-900">Post a Challenge</h3>
        <input
          type="text"
          value={challengeForm.groupId}
          onChange={(e) => setChallengeForm((prev) => ({ ...prev, groupId: e.target.value }))}
          placeholder="Group ID"
          className="w-full border border-gray-300 rounded-lg px-4 py-3 mb-3 outline-none focus:ring-2 focus:ring-teal-400"
          required
        />
        <input
          type="text"
          value={challengeForm.title}
          onChange={(e) => setChallengeForm((prev) => ({ ...prev, title: e.target.value }))}
          placeholder="Challenge Title"
          className="w-full border border-gray-300 rounded-lg px-4 py-3 mb-3 outline-none focus:ring-2 focus:ring-teal-400"
          required
        />
        <textarea
          value={challengeForm.description}
          onChange={(e) => setChallengeForm((prev) => ({ ...prev, description: e.target.value }))}
          placeholder="Challenge Description"
          rows={3}
          className="w-full border border-gray-300 rounded-lg px-4 py-3 mb-3 outline-none focus:ring-2 focus:ring-teal-400"
          required
        />
        <div className="grid grid-cols-2 gap-3 mb-3">
          <select
            value={challengeForm.language}
            onChange={(e) => setChallengeForm((prev) => ({ ...prev, language: e.target.value }))}
            className="border border-gray-300 rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-teal-400"
          >
            {['java', 'python', 'cpp', 'js'].map((lang) => (
              <option key={lang} value={lang}>{lang.toUpperCase()}</option>
            ))}
          </select>
          <select
            value={challengeForm.difficulty}
            onChange={(e) => setChallengeForm((prev) => ({ ...prev, difficulty: e.target.value }))}
            className="border border-gray-300 rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-teal-400"
          >
            {['easy', 'medium', 'hard'].map((level) => (
              <option key={level} value={level}>{level.toUpperCase()}</option>
            ))}
          </select>
        </div>
        <textarea
          value={challengeForm.starterCode}
          onChange={(e) => setChallengeForm((prev) => ({ ...prev, starterCode: e.target.value }))}
          placeholder="Starter Code"
          rows={3}
          className="w-full border border-gray-300 rounded-lg px-4 py-3 mb-4 outline-none focus:ring-2 focus:ring-teal-400 font-mono text-sm"
          required
        />
        <button
          type="submit"
          disabled={loading.challenge}
          className="w-full bg-gray-900 text-white rounded-lg py-3 font-medium hover:bg-gray-800 transition"
        >
          {loading.challenge ? 'Posting...' : 'Post Challenge'}
        </button>
      </form>
    </div>
  );
};

const LandingPage = () => {
  const [groups, setGroups] = useState([]);
  const [groupsLoading, setGroupsLoading] = useState(true);
  const [groupsError, setGroupsError] = useState(null);
  const [toast, setToast] = useState(null);

  const fetchGroups = useCallback(() => {
    setGroupsLoading(true);
    setGroupsError(null);
    fetch(`${API_BASE}/groups?page=0&size=6`)
      .then(async (response) => {
        const data = await response.json().catch(() => ({}));
        if (!response.ok) {
          throw new Error(data.message || 'Failed to load groups');
        }
        setGroups(data.content || []);
      })
      .catch((error) => setGroupsError(error.message))
      .finally(() => setGroupsLoading(false));
  }, []);

  useEffect(() => {
    fetchGroups();
  }, [fetchGroups]);

  useEffect(() => {
    if (!toast) {
      return;
    }
    const timeout = setTimeout(() => setToast(null), 4000);
    return () => clearTimeout(timeout);
  }, [toast]);

  const handleMessage = useCallback((message) => {
    if (message) {
      setToast(message);
    }
  }, []);

  return (
    <div className="min-h-screen">
      <div className="max-w-5xl mx-auto w-full px-4">
        {toast && (
          <div className="bg-emerald-50 border border-emerald-200 text-emerald-900 px-4 py-3 rounded-lg mt-4 text-sm">
            {toast}
          </div>
        )}
      </div>
      <RoleSelection />
      <div className="bg-teal-50 py-12">
      <div className="max-w-[1400px] mx-auto px-4 pb-12">
        <div className="flex items-center justify-between mb-8">
          <h2 className="text-3xl font-bold text-gray-900">
            Popular Groups
          </h2>
            <button
              onClick={fetchGroups}
              className="text-teal-600 hover:text-teal-700 font-medium hover:underline"
            >
              Refresh List →
            </button>
          </div>
          {groupsLoading ? (
            <div className="text-center text-gray-600 py-10">Loading...</div>
          ) : groupsError ? (
            <div className="text-center text-red-600 py-10">{groupsError}</div>
          ) : (
            <div className="grid md:grid-cols-2 gap-6 mb-12">
              {groups.length === 0 ? (
                <div className="col-span-2 text-center text-gray-500">
                  No groups available at the moment.
                </div>
              ) : (
                groups.map((group) => (
                  <GroupCard
                    key={group.id}
                    group={group}
                    onRefresh={fetchGroups}
                    onMessage={handleMessage}
                  />
                ))
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

function App() {
  return (
    <AuthProvider>
      <Router>
        <Navbar />
        <Routes>
          <Route path="/" element={<LandingPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/groups" element={<GroupListPage />} />
          <Route path="/find-groups" element={<GroupListPage />} />
          <Route path="/my-groups" element={<MyGroupsPage />} />
          <Route path="/create-group" element={<CreateGroupPage />} />
          <Route path="/groups/:groupId" element={<GroupPage />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;
export { useAuth };
