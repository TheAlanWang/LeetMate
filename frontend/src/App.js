import React, { useState, createContext, useContext } from 'react';
import { Search, Menu, X } from 'lucide-react';
// 导入 logo
import logo from './assets/logo.png';

// 认证上下文 - 管理用户登录状态
const AuthContext = createContext();

const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [userRole, setUserRole] = useState(null);

  const login = (email, role) => {
    setUser({ email, name: email.split('@')[0] });
    setUserRole(role);
  };

  const logout = () => {
    setUser(null);
    setUserRole(null);
  };

  return (
    <AuthContext.Provider value={{ user, userRole, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

// 导航栏组件 - Udemy 风格
const Navbar = () => {
  const { user, logout } = useContext(AuthContext);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  return (
    <nav className="bg-white border-b border-gray-300" style={{ boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)' }}>
      <div className="max-w-7xl mx-auto px-6 py-4">
        <div className="flex justify-between items-center">
          {/* Logo */}
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

          {/* 桌面端菜单 - 文字链接样式 */}
          <div className="hidden md:flex items-center space-x-8">
            <button className="text-gray-700 hover:text-teal-500 transition font-medium">
              Coding Group
            </button>
            <button className="text-gray-700 hover:text-teal-500 transition font-medium">
              Mock Interview
            </button>
            <button className="text-gray-700 hover:text-teal-500 transition font-medium">
              Resume Review
            </button>
            <button className="text-gray-700 hover:text-teal-500 transition font-medium">
              Your Mentor
            </button>
            <button className="text-gray-700 hover:text-teal-500 transition font-medium">
              Your Group
            </button>
            <button className="text-gray-700 hover:text-teal-500 transition font-medium">
              Community
            </button>
          </div>

          {/* 登录/注册按钮 */}
          <div className="hidden md:flex items-center space-x-4">
            {user ? (
              <>
                <span className="text-gray-700 font-medium">
                  欢迎, {user.name}
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
                <button className="px-5 py-2 text-gray-900 hover:text-teal-500 transition font-medium">
                  Log in
                </button>
                <button className="px-5 py-2 bg-gray-900 text-white rounded hover:bg-gray-800 transition font-medium">
                  Sign up
                </button>
              </>
            )}
          </div>

          {/* 移动端菜单按钮 */}
          <button
            className="md:hidden"
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          >
            {mobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
          </button>
        </div>

        {/* 移动端菜单 */}
        {mobileMenuOpen && (
          <div className="md:hidden mt-4 space-y-3 pb-4">
            <button className="block w-full text-left text-gray-700 hover:text-teal-500 transition font-medium py-2">
              Mentor List
            </button>
            <button className="block w-full text-left text-gray-700 hover:text-teal-500 transition font-medium py-2">
              Group List
            </button>
            <button className="block w-full text-left text-gray-700 hover:text-teal-500 transition font-medium py-2">
              Community
            </button>
            {!user && (
              <div className="space-y-2 pt-2">
                <button className="w-full px-5 py-2 text-gray-900 hover:text-teal-500 transition font-medium border border-gray-300 rounded">
                  Log in
                </button>
                <button className="w-full px-5 py-2 bg-gray-900 text-white rounded hover:bg-gray-800 transition font-medium">
                  Sign up
                </button>
              </div>
            )}
          </div>
        )}
      </div>
    </nav>
  );
};

// 搜索栏组件
const SearchBar = () => {
  const [searchQuery, setSearchQuery] = useState('');
  const [searchType, setSearchType] = useState('Explore');

  const handleSearch = () => {
    console.log('搜索类型:', searchType, '搜索内容:', searchQuery);
  };

  return (
    <div className="bg-gray-50 py-8">
      <div className="max-w-4xl mx-auto px-4">
        <div className="flex items-center bg-white rounded-full overflow-hidden border border-gray-300" style={{ boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)' }}>
          <select 
            value={searchType}
            onChange={(e) => setSearchType(e.target.value)}
            className="px-6 py-3 bg-white border-r border-gray-300 outline-none cursor-pointer hover:bg-gray-50 transition"
          >
            <option>Explore</option>
            <option>Groups</option>
            <option>Mentors</option>
            <option>Topics</option>
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

// 角色选择组件 - 打字机轮播效果
const RoleSelection = () => {
  const [currentTextIndex, setCurrentTextIndex] = useState(0);
  const [displayedText, setDisplayedText] = useState('');
  const [isDeleting, setIsDeleting] = useState(false);
  const texts = ['Coding Practice', 'Mock Interviews', 'System Design', 'Resume Review'];

  // 打字机效果
  React.useEffect(() => {
    const currentFullText = texts[currentTextIndex];
    
    const timeout = setTimeout(() => {
      if (!isDeleting) {
        // 正在打字
        if (displayedText.length < currentFullText.length) {
          setDisplayedText(currentFullText.slice(0, displayedText.length + 1));
        } else {
          // 打完了，等待2秒后开始删除
          setTimeout(() => setIsDeleting(true), 2000);
        }
      } else {
        // 正在删除
        if (displayedText.length > 0) {
          setDisplayedText(displayedText.slice(0, -1));
        } else {
          // 删完了，切换到下一个文字
          setIsDeleting(false);
          setCurrentTextIndex((prev) => (prev + 1) % texts.length);
        }
      }
    }, isDeleting ? 50 : 100);

    return () => clearTimeout(timeout);
  }, [displayedText, isDeleting, currentTextIndex]);

  const handleMentorClick = () => {
    console.log('成为 Mentor');
  };

  const handleMenteeClick = () => {
    console.log('成为 Mentee');
  };

  return (
    <div className="bg-gray-50 py-16">
      <div className="max-w-7xl mx-auto px-4">
        <div className="grid grid-cols-1 md:grid-cols-10 gap-8 items-center">
          {/* 左侧 - 大标语 (60%) */}
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

          {/* 右侧 - 两个按钮 (40%) */}
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

// 小组卡片组件
const GroupCard = ({ group }) => {
  const handleJoinGroup = () => {
    console.log('加入小组:', group.name);
  };

  return (
    <div 
      onClick={handleJoinGroup}
      className="bg-white rounded-xl p-6 shadow-sm hover:shadow-lg transition-all duration-300 cursor-pointer border border-gray-200 hover:border-teal-400"
    >
      <div className="flex justify-between items-start mb-4">
        <div className="flex-1">
          <h3 className="text-xl font-bold text-gray-900 mb-1 hover:text-teal-500 transition">
            {group.name}
          </h3>
          <p className="text-gray-600">
            <span className="font-medium">{group.mentor}</span>
            <span className="text-sm text-gray-500 ml-2">{group.company}</span>
          </p>
        </div>
        <span className={`px-4 py-1 ${group.price === 'Free' ? 'bg-orange-500' : 'bg-teal-500'} text-white rounded-full text-sm font-semibold whitespace-nowrap ml-4`}>
          {group.price}
        </span>
      </div>
      <div className="flex items-center justify-between">
        <div className="bg-teal-50 px-4 py-2 rounded-lg">
          <p className="text-2xl font-bold text-teal-600">{group.members}</p>
          <p className="text-sm text-gray-600">members</p>
        </div>
        <button 
          onClick={(e) => {
            e.stopPropagation();
            handleJoinGroup();
          }}
          className="px-6 py-2 bg-teal-500 text-white rounded-lg hover:bg-teal-600 transition font-medium"
        >
          Join Group
        </button>
      </div>
    </div>
  );
};

// AI 推荐小组卡片
const AIGroupCard = ({ name }) => {
  const handleClick = () => {
    console.log('点击 AI 推荐:', name);
  };

  return (
    <div 
      onClick={handleClick}
      className="bg-white rounded-lg p-4 shadow-sm hover:shadow-md transition cursor-pointer border border-gray-200 hover:border-teal-400"
    >
      <p className="font-medium text-gray-800">{name}</p>
    </div>
  );
};

// 主应用组件
function App() {
  // 示例数据
  const popularGroups = [
    {
      id: 1,
      name: 'Leetcode Daily Challenge',
      mentor: 'Alan Wang',
      company: 'Northeastern University',
      price: 'Free',
      members: 312
    },
    {
      id: 2,
      name: 'Dynamic Programming Master',
      mentor: 'ShiKun Yang',
      company: 'Amazon SDE Level 3',
      price: 'Free',
      members: 287
    },
    {
      id: 3,
      name: 'System Design Pro',
      mentor: 'Lisa Chen',
      company: 'Google L5',
      price: '$29/mo',
      members: 156
    },
    {
      id: 4,
      name: 'FAANG Interview Prep',
      mentor: 'Michael Zhang',
      company: 'Meta E5',
      price: '$49/mo',
      members: 203
    }
  ];

  const aiGroups = [
    { id: 1, name: 'Array & String' },
    { id: 2, name: 'Daily Question' },
    { id: 3, name: 'Binary Tree' },
    { id: 4, name: 'Graph Algorithms' },
    { id: 5, name: 'Two Pointers' },
    { id: 6, name: 'Sliding Window' }
  ];

  return (
    <AuthProvider>
      <div className="min-h-screen">
        <Navbar />
        <SearchBar />
        <RoleSelection />

        {/* 小组展示区域 - 绿色背景 */}
        <div className="bg-teal-50 py-12">
          <div className="max-w-7xl mx-auto px-4 pb-12">
            <div className="flex items-center justify-between mb-8">
              <h2 className="text-3xl font-bold text-gray-900">
                Popular Groups
              </h2>
              <button className="text-teal-600 hover:text-teal-700 font-medium hover:underline">
                View All →
              </button>
            </div>

            {/* 两列布局 - 小组卡片 */}
            <div className="grid md:grid-cols-2 gap-6 mb-12">
              {popularGroups.map((group) => (
                <GroupCard key={group.id} group={group} />
              ))}
            </div>

            {/* AI 推荐区域 - 移到底部 */}
            <div className="bg-white rounded-xl p-8 border border-gray-200 shadow-sm">
              <h3 className="text-2xl font-bold mb-6 text-gray-900 flex items-center">
                <span className="text-3xl mr-3">🤖</span>
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
    </AuthProvider>
  );
}

export default App;