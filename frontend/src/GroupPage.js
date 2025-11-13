import React, { useState, useEffect, useCallback } from 'react';
import { Send, Heart, MessageCircle, ArrowLeft } from 'lucide-react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from './App';

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

const GroupPage = () => {
  const { groupId } = useParams();
  const navigate = useNavigate();
  const { user, token } = useAuth();
  
  const [group, setGroup] = useState(null);
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [newPostContent, setNewPostContent] = useState('');
  const [newPostLanguage, setNewPostLanguage] = useState('python');
  const [newPostCode, setNewPostCode] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [toast, setToast] = useState(null);

  // Demo 数据
  const demoGroup = {
    id: 'demo',
    name: 'Daily Challenge',
    description: 'Practice coding problems and get mentoring feedback',
    memberCount: 42,
    mentorName: 'John Smith'
  };

  const demoPosts = [
    {
      id: 1,
      authorName: 'Alice Chen',
      authorRole: 'MENTEE',
      content: 'Just solved the Two Sum problem! Here\'s my solution:',
      language: 'python',
      code: 'def twoSum(nums, target):\n    seen = {}\n    for i, num in enumerate(nums):\n        if target - num in seen:\n            return [seen[target - num], i]\n        seen[num] = i\n    return []',
      likes: 5,
      liked: false,
      createdAt: new Date(Date.now() - 3600000).toISOString(),
      replies: [
        {
          id: 11,
          authorName: 'John Smith',
          authorRole: 'MENTOR',
          content: 'Great solution! Time complexity is O(n) which is optimal. Consider adding edge case handling for empty input.',
          createdAt: new Date(Date.now() - 1800000).toISOString()
        }
      ]
    },
    {
      id: 2,
      authorName: 'Bob Wang',
      authorRole: 'MENTEE',
      content: 'Having trouble with the recursion approach. Can someone explain?',
      language: 'java',
      code: 'public int fibonacci(int n) {\n    if (n <= 1) return n;\n    return fibonacci(n-1) + fibonacci(n-2);\n}',
      likes: 3,
      liked: false,
      createdAt: new Date(Date.now() - 7200000).toISOString(),
      replies: []
    }
  ];

  // 获取 Group 信息
  useEffect(() => {
    const fetchGroup = async () => {
      try {
        if (groupId === 'demo') {
          setGroup(demoGroup);
          setPosts(demoPosts);
          setLoading(false);
          return;
        }
        
        const response = await fetch(`${API_BASE}/groups/${groupId}`);
        const data = await response.json().catch(() => ({}));
        if (!response.ok) {
          throw new Error(data.message || 'Failed to load group');
        }
        setGroup(data);
      } catch (err) {
        setError(err.message);
      }
    };
    fetchGroup();
  }, [groupId]);

  // 获取 Posts
  const fetchPosts = useCallback(() => {
    if (groupId === 'demo') {
      return;
    }
    setLoading(true);
    fetch(`${API_BASE}/groups/${groupId}/posts`)
      .then(async (response) => {
        const data = await response.json().catch(() => ({}));
        if (!response.ok) {
          throw new Error(data.message || 'Failed to load posts');
        }
        setPosts(data || []);
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [groupId]);

  useEffect(() => {
    fetchPosts();
  }, [fetchPosts]);

  // 发送新 Post
  const handlePostSubmit = async (e) => {
    e.preventDefault();
    if (!token) {
      setToast('Please log in first');
      return;
    }
    if (!newPostContent.trim()) {
      setToast('Please enter content');
      return;
    }

    setSubmitting(true);
    try {
      const response = await fetch(`${API_BASE}/groups/${groupId}/posts`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify({
          content: newPostContent,
          language: newPostLanguage,
          code: newPostCode || null
        })
      });
      const data = await response.json().catch(() => ({}));
      if (!response.ok) {
        throw new Error(data.message || 'Failed to post');
      }
      setToast('Post published successfully!');
      setNewPostContent('');
      setNewPostCode('');
      setNewPostLanguage('python');
      fetchPosts();
    } catch (err) {
      setToast(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  // 点赞
  const handleLike = async (postId) => {
    if (!token) {
      setToast('Please log in first');
      return;
    }
    try {
      const response = await fetch(`${API_BASE}/groups/${groupId}/posts/${postId}/like`, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      if (!response.ok) {
        throw new Error('Failed to like');
      }
      fetchPosts();
    } catch (err) {
      setToast(err.message);
    }
  };

  // 添加回复
  const handleReply = async (postId, replyContent) => {
    if (!token) {
      setToast('Please log in first');
      return;
    }
    if (!replyContent.trim()) {
      setToast('Please enter reply');
      return;
    }

    try {
      const response = await fetch(`${API_BASE}/groups/${groupId}/posts/${postId}/replies`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify({ content: replyContent })
      });
      const data = await response.json().catch(() => ({}));
      if (!response.ok) {
        throw new Error(data.message || 'Failed to reply');
      }
      setToast('Reply posted!');
      fetchPosts();
    } catch (err) {
      setToast(err.message);
    }
  };

  useEffect(() => {
    if (!toast) return;
    const timeout = setTimeout(() => setToast(null), 3000);
    return () => clearTimeout(timeout);
  }, [toast]);

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white border-b border-gray-200">
        <div className="max-w-4xl mx-auto px-4 py-6">
          <h1 className="text-4xl font-bold text-gray-900">
            {group?.name || 'Loading...'}
          </h1>
          <p className="text-gray-600 mt-2">{group?.description}</p>
          <div className="flex gap-4 mt-4">
            <span className="text-sm text-gray-600">
              Members: <strong>{group?.memberCount || 0}</strong>
            </span>
            <span className="text-sm text-gray-600">
              Mentor: <strong>{group?.mentorName || 'TBD'}</strong>
            </span>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="max-w-4xl mx-auto px-4 py-8">
        {/* Toast */}
        {toast && (
          <div className="bg-emerald-50 border border-emerald-200 text-emerald-900 px-4 py-3 rounded-lg mb-6 text-sm">
            {toast}
          </div>
        )}

        {/* Error */}
        {error && !group && (
          <div className="bg-red-50 border border-red-200 text-red-900 px-4 py-3 rounded-lg mb-6 text-sm">
            {error}
          </div>
        )}

        {/* New Post Form */}
        {user && (
          <div className="bg-white rounded-xl border border-gray-200 p-6 mb-8">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">
              Share Your Progress
            </h3>
            <form onSubmit={handlePostSubmit} className="space-y-4">
              <textarea
                value={newPostContent}
                onChange={(e) => setNewPostContent(e.target.value)}
                placeholder="What's on your mind? Share your solution, question, or progress..."
                rows={3}
                className="w-full border border-gray-300 rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-teal-400 resize-none"
              />
              
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <label className="text-sm font-medium text-gray-600 block mb-2">
                    Language
                  </label>
                  <select
                    value={newPostLanguage}
                    onChange={(e) => setNewPostLanguage(e.target.value)}
                    className="w-full border border-gray-300 rounded-lg px-4 py-2 outline-none focus:ring-2 focus:ring-teal-400"
                  >
                    {['python', 'java', 'cpp', 'js'].map((lang) => (
                      <option key={lang} value={lang}>
                        {lang.toUpperCase()}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <textarea
                value={newPostCode}
                onChange={(e) => setNewPostCode(e.target.value)}
                placeholder="(Optional) Paste your code here..."
                rows={4}
                className="w-full border border-gray-300 rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-teal-400 font-mono text-sm resize-none"
              />

              <button
                type="submit"
                disabled={submitting}
                className="w-full bg-teal-500 text-white rounded-lg py-3 font-medium hover:bg-teal-600 transition disabled:bg-gray-400"
              >
                {submitting ? 'Posting...' : 'Post'}
              </button>
            </form>
          </div>
        )}

        {/* Posts List */}
        {loading ? (
          <div className="text-center text-gray-600 py-10">Loading posts...</div>
        ) : posts.length === 0 ? (
          <div className="text-center text-gray-500 py-10">
            No posts yet. Be the first to share!
          </div>
        ) : (
          <div className="space-y-6">
            {posts.map((post) => (
              <Post
                key={post.id}
                post={post}
                currentUser={user}
                onLike={() => handleLike(post.id)}
                onReply={(content) => handleReply(post.id, content)}
                groupId={groupId}
                token={token}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

// Post 组件
const Post = ({ post, currentUser, onLike, onReply, groupId, token }) => {
  const [showReplyForm, setShowReplyForm] = useState(false);
  const [replyContent, setReplyContent] = useState('');
  const [replying, setReplying] = useState(false);
  const [liked, setLiked] = useState(post.liked || false);

  const handleReplySubmit = async (e) => {
    e.preventDefault();
    setReplying(true);
    try {
      await onReply(replyContent);
      setReplyContent('');
      setShowReplyForm(false);
    } finally {
      setReplying(false);
    }
  };

  const handleLikeClick = () => {
    setLiked(!liked);
    onLike();
  };

  return (
    <div className="bg-white rounded-xl border border-gray-200 p-6">
      {/* Post Header */}
      <div className="flex items-start justify-between mb-4">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-full bg-teal-100 flex items-center justify-center">
            <span className="text-teal-600 font-semibold">
              {post.authorName?.charAt(0).toUpperCase() || 'U'}
            </span>
          </div>
          <div>
            <p className="font-semibold text-gray-900">{post.authorName}</p>
            <p className="text-xs text-gray-500">{new Date(post.createdAt).toLocaleString()}</p>
            {post.authorRole && (
              <span className="inline-block mt-1 px-2 py-1 text-xs bg-teal-50 text-teal-700 rounded-full">
                {post.authorRole}
              </span>
            )}
          </div>
        </div>
      </div>

      {/* Post Content */}
      <p className="text-gray-800 mb-4 whitespace-pre-wrap">{post.content}</p>

      {/* Code Block */}
      {post.code && (
        <div className="bg-gray-900 rounded-lg p-4 mb-4 overflow-x-auto">
          <div className="flex justify-between items-center mb-2">
            <span className="text-xs text-gray-400 font-semibold">
              {post.language?.toUpperCase()}
            </span>
          </div>
          <pre className="text-gray-200 text-sm font-mono">
            <code>{post.code}</code>
          </pre>
        </div>
      )}

      {/* Post Actions */}
      <div className="flex items-center gap-6 mb-4 text-gray-600 border-t border-b border-gray-100 py-3">
        <button
          onClick={handleLikeClick}
          className={`flex items-center gap-2 transition ${
            liked ? 'text-red-500' : 'hover:text-red-500'
          }`}
        >
          <Heart size={18} fill={liked ? 'currentColor' : 'none'} />
          <span className="text-sm">{post.likes || 0}</span>
        </button>
        <button
          onClick={() => setShowReplyForm(!showReplyForm)}
          className="flex items-center gap-2 hover:text-teal-500 transition"
        >
          <MessageCircle size={18} />
          <span className="text-sm">{post.replies?.length || 0}</span>
        </button>
      </div>

      {/* Reply Form */}
      {showReplyForm && (
        <form onSubmit={handleReplySubmit} className="mb-4 space-y-3">
          <textarea
            value={replyContent}
            onChange={(e) => setReplyContent(e.target.value)}
            placeholder="Write a reply..."
            rows={3}
            className="w-full border border-gray-300 rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-teal-400 resize-none text-sm"
          />
          <div className="flex gap-2">
            <button
              type="submit"
              disabled={replying}
              className="px-4 py-2 bg-teal-500 text-white rounded-lg text-sm font-medium hover:bg-teal-600 transition disabled:bg-gray-400 flex items-center gap-2"
            >
              <Send size={16} />
              Reply
            </button>
            <button
              type="button"
              onClick={() => setShowReplyForm(false)}
              className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg text-sm font-medium hover:bg-gray-50 transition"
            >
              Cancel
            </button>
          </div>
        </form>
      )}

      {/* Replies */}
      {post.replies && post.replies.length > 0 && (
        <div className="mt-4 space-y-4 border-t border-gray-100 pt-4">
          {post.replies.map((reply) => (
            <div key={reply.id} className="pl-4 border-l-2 border-gray-200">
              <div className="flex items-start gap-3">
                <div className="w-8 h-8 rounded-full bg-gray-200 flex items-center justify-center flex-shrink-0">
                  <span className="text-gray-600 font-semibold text-sm">
                    {reply.authorName?.charAt(0).toUpperCase() || 'U'}
                  </span>
                </div>
                <div className="flex-1 min-w-0">
                  <p className="font-semibold text-sm text-gray-900">
                    {reply.authorName}
                    {reply.authorRole && (
                      <span className="ml-2 text-xs bg-teal-50 text-teal-700 px-2 py-1 rounded">
                        {reply.authorRole}
                      </span>
                    )}
                  </p>
                  <p className="text-xs text-gray-500 mb-1">
                    {new Date(reply.createdAt).toLocaleString()}
                  </p>
                  <p className="text-gray-800 text-sm whitespace-pre-wrap">
                    {reply.content}
                  </p>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default GroupPage;