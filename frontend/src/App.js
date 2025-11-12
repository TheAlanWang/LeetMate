import React, { useState, useEffect, useCallback, createContext, useContext } from 'react';
import { Search, Menu, X } from 'lucide-react';
import { BrowserRouter as Router, Routes, Route, Link, useNavigate } from 'react-router-dom';
import logo from './assets/logo.png';

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
      throw new Error(data.message || '请求失败，请稍后重试');
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

  const logout = () => {
    setUser(null);
    setToken(null);
  };

  const value = {
    user,
    token,
    login,
    register,
    logout,
    isMentor: user?.role === 'MENTOR',
    isMentee: user?.role === 'MENTEE'
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

const Navbar = () => {
  const { user, logout } = useAuth();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  return (
    <nav className="bg-white border-b border-gray-300" style={{ boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)' }}>
      <div className="max-w-7xl mx-auto px-6 py-4">
        <div className="flex justify-between items-center">
          <div className="flex items-center space-x-3">
            <img
              src={logo}
              alt="LeetMate"
              className="h-12 w-auto cursor-pointer hover:opacity-80 transition"
            />
            <h1 className="text-2xl font-bold text-gray-900 cursor-pointer hover:text-teal-500 transition">
              LeetMate
            </h1>
          </div>

          <div className="hidden md:flex items-center space-x-8">
            {['Coding Group', 'Mock Interview', 'Resume Review', 'Your Mentor', 'Your Group', 'Community']
              .map((item) => (
                <button key={item} className="text-gray-700 hover:text-teal-500 transition font-medium">
                  {item}
                </button>
              ))}
          </div>

          <div className="hidden md:flex items-center space-x-4">
            {user ? (
              <>
                <span className="text-gray-700 font-medium">
                  欢迎, {user.name} ({user.role})
                </span>
                <button
                  onClick={logout}
                  className="px-5 py-2 border border-gray-900 text-gray-900 rounded hover:bg-gray-900 hover:text-white transition font-medium"
                >
                  Logout
                </button>
              </>
            ) : (
              <Link
                to="/login"
                className="px-5 py-2 border border-gray-900 text-gray-900 rounded hover:bg-gray-900 hover:text-white transition font-medium"
              >
                Log in
              </Link>
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
            {['Mentor List', 'Group List', 'Community'].map((item) => (
              <button
                key={item}
                className="block w-full text-left text-gray-700 hover:text-teal-500 transition font-medium py-2"
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
    console.log('搜索类型:', searchType, '搜索内容:', searchQuery);
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

const AuthPanel = ({ onMessage, onSuccess, standalone = true }) => {
  const { user, login, register, logout, isMentor, isMentee } = useAuth();
  const [mode, setMode] = useState('login');
  const [form, setForm] = useState({
    name: '',
    email: '',
    password: '',
    role: 'MENTEE'
  });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const handleChange = (field, value) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    const payload = {
      name: form.name.trim(),
      email: form.email.trim(),
      password: form.password,
      role: form.role
    };
    try {
      const result = mode === 'login'
        ? await login(payload.email, payload.password)
        : await register(payload);
      if (!result.success) {
        setError(result.message);
      } else {
        onMessage?.(mode === 'login' ? '登录成功，欢迎回来！' : '注册成功，已自动登录。');
        onSuccess?.(mode);
        setForm((prev) => ({ ...prev, password: '' }));
      }
    } finally {
      setSubmitting(false);
    }
  };

  if (user) {
    return (
      <div className="bg-white rounded-2xl shadow-md border border-gray-200 p-6 mt-6">
        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          <div>
            <p className="text-gray-700 font-semibold">
              当前登录: {user.name} ({user.role})
            </p>
            <p className="text-gray-500 text-sm mt-1">
              {isMentor ? '您可以创建学习小组和挑战题目。' : isMentee ? '您可以加入小组并提交代码。' : ''}
            </p>
          </div>
          <button
            onClick={logout}
            className="px-5 py-2 border border-gray-900 text-gray-900 rounded hover:bg-gray-900 hover:text-white transition font-medium"
          >
            Logout
          </button>
        </div>
      </div>
    );
  }

  const containerClass = standalone ? 'mt-10' : '-mt-10 relative z-10';

  return (
    <div className={`bg-white rounded-2xl shadow-md border border-gray-200 p-6 ${containerClass}`}>
      <div className="flex space-x-4 mb-4">
        {['login', 'register'].map((value) => (
          <button
            key={value}
            className={`px-4 py-2 rounded-full font-medium ${mode === value ? 'bg-teal-500 text-white' : 'bg-gray-100 text-gray-700'}`}
            onClick={() => setMode(value)}
          >
            {value === 'login' ? '登录' : '注册'}
          </button>
        ))}
      </div>
      <form onSubmit={handleSubmit} className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {mode === 'register' && (
          <input
            type="text"
            value={form.name}
            onChange={(e) => handleChange('name', e.target.value)}
            placeholder="姓名 (用于展示)"
            className="border border-gray-300 rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-teal-400"
            required
          />
        )}
        <input
          type="email"
          value={form.email}
          onChange={(e) => handleChange('email', e.target.value)}
          placeholder="Email"
          className="border border-gray-300 rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-teal-400 col-span-1 md:col-span-2"
          required
        />
        <input
          type="password"
          value={form.password}
          onChange={(e) => handleChange('password', e.target.value)}
          placeholder="密码"
          className="border border-gray-300 rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-teal-400 col-span-1 md:col-span-2"
          required
        />
        {mode === 'register' && (
          <div className="col-span-1 md:col-span-2">
            <label className="text-sm font-medium text-gray-600 mb-2 block">注册角色</label>
            <div className="flex space-x-4">
              {['MENTOR', 'MENTEE'].map((role) => (
                <label key={role} className={`px-4 py-2 rounded-full cursor-pointer border ${form.role === role ? 'bg-teal-500 text-white border-teal-500' : 'border-gray-300 text-gray-700'}`}>
                  <input
                    type="radio"
                    value={role}
                    checked={form.role === role}
                    onChange={(e) => handleChange('role', e.target.value)}
                    className="hidden"
                  />
                  {role === 'MENTOR' ? 'Mentor' : 'Mentee'}
                </label>
              ))}
            </div>
          </div>
        )}
        {error && (
          <div className="col-span-1 md:col-span-2 text-sm text-red-600">
            {error}
          </div>
        )}
        <button
          type="submit"
          disabled={submitting}
          className="col-span-1 md:col-span-2 bg-gray-900 text-white rounded-lg py-3 hover:bg-gray-800 transition font-medium"
        >
          {submitting ? '请稍候...' : mode === 'login' ? '登录' : '注册并登录'}
        </button>
      </form>
    </div>
  );
};

const RoleSelection = () => {
  const [currentTextIndex, setCurrentTextIndex] = useState(0);
  const [displayedText, setDisplayedText] = useState('');
  const [isDeleting, setIsDeleting] = useState(false);
  const texts = ['Coding Practice', 'Mock Interviews', 'System Design', 'Resume Review'];

  useEffect(() => {
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
  }, [displayedText, isDeleting, currentTextIndex, texts]);

  const handleMentorClick = () => console.log('成为 Mentor');
  const handleMenteeClick = () => console.log('成为 Mentee');

  return (
    <div className="bg-gray-50 py-16 mt-0">
      <div className="max-w-7xl mx-auto px-4">
        <div className="grid grid-cols-1 md:grid-cols-10 gap-8 items-center">
          <div className="md:col-span-6">
            <h1 className="text-5xl md:text-6xl font-bold text-gray-900 mb-6">
              Join Mentor's Group
            </h1>
            <h2 className="text-4xl md:text-5xl font-bold text-teal-500 mb-6 min-h-[1.2em]">
              Start {displayedText}
              <span className="animate-pulse">|</span>
            </h2>
            <p className="text-xl md:text-2xl text-gray-600 leading-relaxed">
              Ship real code, practice interviews, and design systems with mentors.
            </p>
          </div>
          <div className="md:col-span-4 space-y-4">
            <div
              onClick={handleMentorClick}
              className="bg-gradient-to-r from-teal-400 to-teal-500 text-white rounded-2xl p-6 cursor-pointer hover:shadow-xl transform hover:scale-105 transition-all duration-300"
            >
              <h2 className="text-2xl font-bold mb-2">BE a Mentor</h2>
              <p className="text-base opacity-90">Create a group and guide students</p>
            </div>
            <div
              onClick={handleMenteeClick}
              className="bg-gradient-to-r from-teal-500 to-teal-600 text-white rounded-2xl p-6 cursor-pointer hover:shadow-xl transform hover:scale-105 transition-all duration-300"
            >
              <h2 className="text-2xl font-bold mb-2">BE a Mentee</h2>
              <p className="text-base opacity-90">Join a group and start learning</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

const GroupCard = ({ group, onRefresh, onMessage }) => {
  const { token, isMentee } = useAuth();
  const [joining, setJoining] = useState(false);

  const joinGroup = async () => {
    if (!isMentee) {
      onMessage?.('请以 mentee 身份登录后再加入小组。');
      return;
    }
    if (!token) {
      onMessage?.('请先登录再执行该操作。');
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
        throw new Error(data.message || '加入失败');
      }
      onMessage?.(`成功加入「${data.name}」小组！`);
      onRefresh?.();
    } catch (error) {
      onMessage?.(error.message);
    } finally {
      setJoining(false);
    }
  };

  return (
    <div className="bg-white rounded-xl p-6 shadow-sm hover:shadow-lg transition-all duration-300 border border-gray-200 hover:border-teal-400">
      <div className="flex justify-between items-start mb-4">
        <div className="flex-1">
          <h3 className="text-xl font-bold text-gray-900 mb-2 hover:text-teal-500 transition">
            {group.name}
          </h3>
          <p className="text-gray-600 text-sm mb-2">{group.description}</p>
          <p className="text-gray-700 text-sm">
            Mentor: <span className="font-semibold">{group.mentorName || '待定'}</span>
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
        {joining ? '加入中...' : 'Join Group'}
      </button>
    </div>
  );
};

const AIGroupCard = ({ name }) => (
  <div className="bg-white rounded-lg p-4 shadow-sm hover:shadow-md transition cursor-pointer border border-gray-200 hover:border-teal-400">
    <p className="font-medium text-gray-800">{name}</p>
  </div>
);

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
      onMessage?.('请先登录。');
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
        throw new Error(data.message || '创建小组失败');
      }
      onMessage?.(`已创建小组「${data.name}」`);
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
      onMessage?.('请先登录。');
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
        throw new Error(data.message || '创建挑战失败');
      }
      onMessage?.(`已向小组发布挑战「${data.title}」`);
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
        <h3 className="text-xl font-semibold mb-4 text-gray-900">Mentor 创建小组</h3>
        <input
          type="text"
          value={groupForm.name}
          onChange={(e) => setGroupForm((prev) => ({ ...prev, name: e.target.value }))}
          placeholder="小组名称"
          className="w-full border border-gray-300 rounded-lg px-4 py-3 mb-3 outline-none focus:ring-2 focus:ring-teal-400"
          required
        />
        <textarea
          value={groupForm.description}
          onChange={(e) => setGroupForm((prev) => ({ ...prev, description: e.target.value }))}
          placeholder="小组简介"
          rows={3}
          className="w-full border border-gray-300 rounded-lg px-4 py-3 mb-3 outline-none focus:ring-2 focus:ring-teal-400"
          required
        />
        <input
          type="text"
          value={groupForm.tags}
          onChange={(e) => setGroupForm((prev) => ({ ...prev, tags: e.target.value }))}
          placeholder="标签 (使用逗号分隔)"
          className="w-full border border-gray-300 rounded-lg px-4 py-3 mb-4 outline-none focus:ring-2 focus:ring-teal-400"
        />
        <button
          type="submit"
          disabled={loading.group}
          className="w-full bg-teal-500 text-white rounded-lg py-3 font-medium hover:bg-teal-600 transition"
        >
          {loading.group ? '创建中...' : '创建小组'}
        </button>
      </form>

      <form onSubmit={handleChallengeSubmit} className="bg-white border border-gray-200 rounded-2xl p-6 shadow-sm">
        <h3 className="text-xl font-semibold mb-4 text-gray-900">发布挑战题目</h3>
        <input
          type="text"
          value={challengeForm.groupId}
          onChange={(e) => setChallengeForm((prev) => ({ ...prev, groupId: e.target.value }))}
          placeholder="小组 ID"
          className="w-full border border-gray-300 rounded-lg px-4 py-3 mb-3 outline-none focus:ring-2 focus:ring-teal-400"
          required
        />
        <input
          type="text"
          value={challengeForm.title}
          onChange={(e) => setChallengeForm((prev) => ({ ...prev, title: e.target.value }))}
          placeholder="题目标题"
          className="w-full border border-gray-300 rounded-lg px-4 py-3 mb-3 outline-none focus:ring-2 focus:ring-teal-400"
          required
        />
        <textarea
          value={challengeForm.description}
          onChange={(e) => setChallengeForm((prev) => ({ ...prev, description: e.target.value }))}
          placeholder="题目描述"
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
          placeholder="起始代码"
          rows={3}
          className="w-full border border-gray-300 rounded-lg px-4 py-3 mb-4 outline-none focus:ring-2 focus:ring-teal-400 font-mono text-sm"
          required
        />
        <button
          type="submit"
          disabled={loading.challenge}
          className="w-full bg-gray-900 text-white rounded-lg py-3 font-medium hover:bg-gray-800 transition"
        >
          {loading.challenge ? '发布中...' : '发布挑战'}
        </button>
      </form>
    </div>
  );
};

const MenteeActions = ({ onMessage }) => {
  const { isMentee, token } = useAuth();
  const [form, setForm] = useState({ challengeId: '', language: 'java', code: '' });
  const [submitting, setSubmitting] = useState(false);
  const [review, setReview] = useState(null);

  if (!isMentee) {
    return null;
  }

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!token) {
      onMessage?.('请登录后再提交代码。');
      return;
    }
    setSubmitting(true);
    setReview(null);
    try {
      const response = await fetch(`${API_BASE}/challenges/${form.challengeId}/submissions`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify({ language: form.language, code: form.code })
      });
      const data = await response.json().catch(() => ({}));
      if (!response.ok) {
        throw new Error(data.message || '提交失败');
      }
      setReview(data.review);
      onMessage?.('提交成功，AI 已完成评审。');
    } catch (error) {
      onMessage?.(error.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="bg-white border border-gray-200 rounded-2xl p-6 shadow-sm mt-6">
      <h3 className="text-xl font-semibold mb-4 text-gray-900">Mentee 提交代码并触发 AI 评审</h3>
      <form onSubmit={handleSubmit} className="space-y-4">
        <input
          type="text"
          value={form.challengeId}
          onChange={(e) => setForm((prev) => ({ ...prev, challengeId: e.target.value }))}
          placeholder="Challenge ID"
          className="w-full border border-gray-300 rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-teal-400"
          required
        />
        <select
          value={form.language}
          onChange={(e) => setForm((prev) => ({ ...prev, language: e.target.value }))}
          className="w-full border border-gray-300 rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-teal-400"
        >
          {['java', 'python', 'cpp', 'js'].map((lang) => (
            <option key={lang} value={lang}>{lang.toUpperCase()}</option>
          ))}
        </select>
        <textarea
          value={form.code}
          onChange={(e) => setForm((prev) => ({ ...prev, code: e.target.value }))}
          placeholder="在此粘贴或输入代码"
          rows={6}
          className="w-full border border-gray-300 rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-teal-400 font-mono text-sm"
          required
        />
        <button
          type="submit"
          disabled={submitting}
          className="w-full bg-teal-500 text-white rounded-lg py-3 font-medium hover:bg-teal-600 transition"
        >
          {submitting ? '提交中...' : '提交并触发 AI 评审'}
        </button>
      </form>
      {review && (
        <div className="mt-6 bg-teal-50 border border-teal-200 rounded-xl p-4">
          <h4 className="text-lg font-semibold text-teal-800 mb-2">AI Review</h4>
          <p className="text-sm text-gray-700 mb-2"><strong>Summary:</strong> {review.summary}</p>
          <p className="text-sm text-gray-700 mb-2"><strong>Complexity:</strong> {review.complexity}</p>
          {review.suggestions?.length > 0 && (
            <ul className="list-disc list-inside text-sm text-gray-700 space-y-1">
              {review.suggestions.map((suggestion, index) => (
                <li key={index}>{suggestion}</li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  );
};

const LoginPage = () => {
  const [toast, setToast] = useState(null);
  const navigate = useNavigate();

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

  const handleSuccess = useCallback((kind) => {
    setToast(kind === 'login' ? 'Login successful, redirecting...' : 'Registration successful, redirecting...');
    setTimeout(() => navigate('/'), 1500);
  }, [navigate]);

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-3xl mx-auto px-4 pt-10 pb-16">
        <h2 className="text-3xl font-bold text-gray-900 mb-6">Log in or create an account</h2>
        <AuthPanel standalone onMessage={handleMessage} onSuccess={handleSuccess} />
        {toast && (
          <div className="bg-emerald-50 border border-emerald-200 text-emerald-900 px-4 py-3 rounded-lg mt-4 text-sm">
            {toast}
          </div>
        )}
        <div className="text-center mt-6">
          <Link to="/" className="text-teal-600 hover:text-teal-700 font-medium underline">
            ← Back to homepage
          </Link>
        </div>
      </div>
    </div>
  );
};

const LandingPage = () => {
  const [groups, setGroups] = useState([]);
  const [groupsLoading, setGroupsLoading] = useState(true);
  const [groupsError, setGroupsError] = useState(null);
  const [toast, setToast] = useState(null);

  const aiGroups = [
    { id: 1, name: 'Array & String' },
    { id: 2, name: 'Daily Question' },
    { id: 3, name: 'Binary Tree' },
    { id: 4, name: 'Graph Algorithms' },
    { id: 5, name: 'Two Pointers' },
    { id: 6, name: 'Sliding Window' }
  ];

  const fetchGroups = useCallback(() => {
    setGroupsLoading(true);
    setGroupsError(null);
    fetch(`${API_BASE}/groups?page=0&size=6`)
      .then(async (response) => {
        const data = await response.json().catch(() => ({}));
        if (!response.ok) {
          throw new Error(data.message || '加载小组失败');
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
      <Navbar />
      <SearchBar />
      <div className="max-w-5xl mx-auto w-full px-4">
        {toast && (
          <div className="bg-emerald-50 border border-emerald-200 text-emerald-900 px-4 py-3 rounded-lg mt-4 text-sm">
            {toast}
          </div>
        )}
      </div>
      <RoleSelection />
      <div className="max-w-5xl mx-auto px-4">
        <MentorActions onSuccess={fetchGroups} onMessage={handleMessage} />
        <MenteeActions onMessage={handleMessage} />
      </div>
      <div className="bg-teal-50 py-12 mt-10">
        <div className="max-w-7xl mx-auto px-4 pb-12">
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
            <div className="text-center text-gray-600 py-10">加载中...</div>
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
          <div className="bg-white rounded-xl p-8 border border-gray-200 shadow-sm">
            <h3 className="text-2xl font-bold mb-6 text-gray-900 flex items-center">
              <span className="text-3xl mr-3"></span>
              AI Recommended Topics
            </h3>
            <div className="grid sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
              {aiGroups.map((group) => (
                <AIGroupCard key={group.id} name={group.name} />
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route path="/" element={<LandingPage />} />
          <Route path="/login" element={<LoginPage />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;
