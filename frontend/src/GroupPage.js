import React, { useState, useEffect, useCallback } from 'react';
import { Send, ArrowLeft, MoreHorizontal } from 'lucide-react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from './App';
import TagSelector from './components/TagSelector';

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
  const [threads, setThreads] = useState([]);
  const [messagesByThread, setMessagesByThread] = useState({});
  const [replyDrafts, setReplyDrafts] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [threadsError, setThreadsError] = useState(null);
  const [toast, setToast] = useState(null);
  const [deleteError, setDeleteError] = useState(null);
  const [joining, setJoining] = useState(false);
  const [joined, setJoined] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [editForm, setEditForm] = useState({ name: '', description: '', tags: [] });
  const [threadForm, setThreadForm] = useState({ title: '', description: '' });
  const [expandedMessages, setExpandedMessages] = useState({});
  const [threadMessageDrafts, setThreadMessageDrafts] = useState({});
  const [threadComposerOpen, setThreadComposerOpen] = useState({});
  const [openReplyForms, setOpenReplyForms] = useState({});
  const [messageMenuOpen, setMessageMenuOpen] = useState({});
  const [editingMessage, setEditingMessage] = useState({});
  const [editDrafts, setEditDrafts] = useState({});
  const [showThreadForm, setShowThreadForm] = useState(false);

  const demoGroup = {
    id: 'demo',
    name: 'Daily Challenge',
    description: 'Practice coding problems and get mentoring feedback',
    memberCount: 42,
    mentorName: 'John Smith'
  };

  useEffect(() => {
    const fetchGroup = async () => {
      try {
        if (groupId === 'demo') {
          setGroup(demoGroup);
          setThreads([]);
          setLoading(false);
          return;
        }

        const response = await fetch(`${API_BASE}/groups/${groupId}`);
        const data = await response.json().catch(() => ({}));
        if (!response.ok) {
          throw new Error(data.message || 'Failed to load group');
        }
        setGroup(data);
        setEditForm({ name: data.name, description: data.description, tags: data.tags || [] });
        try {
          await fetchThreads(data.id);
        } catch (err) {
          setThreadsError(err.message);
        }
      } catch (err) {
        setError(err.message);
      }
    };
    fetchGroup();
  }, [groupId]);

  // Check if current user already joined this group
  useEffect(() => {
    if (!user || !token || !groupId) {
      setJoined(false);
      return;
    }
    const checkJoined = async () => {
      try {
        const response = await fetch(`${API_BASE}/groups/members/${user.id}`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        const data = await response.json().catch(() => ({}));
        if (!response.ok) {
          return;
        }
        const isMember = Array.isArray(data) && data.some((g) => g.id === groupId);
        setJoined(isMember);
      } catch {
        setJoined(false);
      }
    };
    checkJoined();
  }, [user, token, groupId]);

  const fetchThreads = useCallback(async (gid) => {
    if (gid === 'demo') return;
    setDeleteError(null);
    const response = await fetch(`${API_BASE}/groups/${gid}/threads?page=0&size=20`, {
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    });
    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
      throw new Error(data.message || 'Failed to load threads');
    }
    setThreadsError(null);
    setThreads(data.content || []);
    // Fetch messages for all threads to show expanded discussions
    const messagesMap = {};
    for (const t of data.content || []) {
      try {
        const resp = await fetch(`${API_BASE}/threads/${t.id}/messages?page=0&size=100`, {
          headers: token ? { Authorization: `Bearer ${token}` } : {}
        });
        const msgs = await resp.json().catch(() => ({}));
        if (resp.ok) {
          messagesMap[t.id] = msgs.content || [];
        }
      } catch {
        // ignore per-thread errors
      }
    }
    setMessagesByThread(messagesMap);
  }, [token, groupId]);

  const renderMarkdown = (content, messageId) => {
    if (!content) return null;

    const highlightCodeBlock = (lang, rawCode) => {
      const keywords = [
        'const', 'let', 'var', 'function', 'return', 'if', 'else', 'for', 'while', 'class', 'async', 'await',
        'try', 'catch', 'finally', 'throw', 'import', 'from', 'export', 'new', 'switch', 'case', 'break', 'continue',
        'def', 'lambda', 'yield', 'with', 'as', 'elif', 'in', 'not', 'and', 'or', 'public', 'private', 'protected',
        'static', 'void', 'int', 'float', 'double', 'boolean', 'string'
      ];
      const keywordRegex = new RegExp(`\\b(${keywords.join('|')})\\b`, 'g');

      const escapeHtmlToken = (str) =>
        str
          .replace(/&/g, '&amp;')
          .replace(/</g, '&lt;')
          .replace(/>/g, '&gt;')
          .replace(/"/g, '&quot;')
          .replace(/'/g, '&#039;');

      const tokenizer =
        /(\/\/[^\n]*|\/\*[\s\S]*?\*\/|#[^\n]*|"(?:[^"\\]|\\.)*"|'(?:[^'\\]|\\.)*'|`(?:[^`\\]|\\.)*`|\b\d+(?:\.\d+)?\b|\b[a-zA-Z_][\w]*\b)/g;

      let result = '';
      let lastIndex = 0;
      let match;
      while ((match = tokenizer.exec(rawCode)) !== null) {
        const [token] = match;
        result += escapeHtmlToken(rawCode.slice(lastIndex, match.index));

        const isComment = token.startsWith('//') || token.startsWith('/*') || token.startsWith('#');
        const isString = token.startsWith('"') || token.startsWith("'") || token.startsWith('`');
        const isNumber = /^\d/.test(token);
        const isKeyword = keywordRegex.test(token);

        let color = '#111827';
        if (isComment) color = '#6b7280';
        else if (isString) color = '#b45309';
        else if (isNumber) color = '#16a34a';
        else if (isKeyword) color = '#2563eb';

        result += `<span style="color:${color}">${escapeHtmlToken(token)}</span>`;
        lastIndex = match.index + token.length;
        keywordRegex.lastIndex = 0;
      }
      result += escapeHtmlToken(rawCode.slice(lastIndex));
      return result;
    };
    const escapeHtml = (str) =>
      str
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');

    const codeBlocks = [];
    // Extract fenced code blocks with optional language before escaping.
    const withPlaceholders = content.replace(/```(\w+)?\n([\s\S]*?)```/g, (_match, lang, code) => {
      const idx = codeBlocks.length;
      const highlightedCode = highlightCodeBlock(lang, (code || '').trimEnd());
      const languageClass = lang ? ` class="language-${lang.toLowerCase()}"` : '';
      codeBlocks.push(
        `<pre style="background-color:#f3f4f6;color:#1f2937;" class="rounded-lg p-4 text-sm overflow-x-auto mb-3"><code${languageClass}>${highlightedCode}</code></pre>`
      );
      return `__CODE_BLOCK_${idx}__`;
    });

    const isLong = content.length > 300;
    const expanded = expandedMessages[messageId];
    const displayText = isLong && !expanded ? `${withPlaceholders.slice(0, 300)}...` : withPlaceholders;

    let html = escapeHtml(displayText);
    // Inline code
    html = html.replace(/`([^`]+)`/g, '<code class="bg-gray-100 px-1 rounded text-sm">$1</code>');
    // Line breaks
    html = html.replace(/\n/g, '<br/>');
    // Restore code blocks
    codeBlocks.forEach((block, idx) => {
      const placeholder = escapeHtml(`__CODE_BLOCK_${idx}__`);
      html = html.replace(placeholder, block);
    });

    return (
      <div className="text-gray-800 leading-relaxed">
        <div dangerouslySetInnerHTML={{ __html: html }} />
        {isLong && (
          <button
            type="button"
            onClick={() => setExpandedMessages((prev) => ({ ...prev, [messageId]: !expanded }))}
            className="mt-2 text-sm text-teal-600 hover:text-teal-700"
          >
            {expanded ? 'Show less' : 'Show more'}
          </button>
        )}
      </div>
    );
  };

  const handleReply = async (parentId, threadId) => {
    const content = replyDrafts[parentId] || '';
    if (!content.trim()) {
      setToast('Please enter a reply.');
      return;
    }
    try {
      const response = await fetch(`${API_BASE}/threads/${threadId}/messages`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify({ content, parentMessageId: parentId })
      });
      const data = await response.json().catch(() => ({}));
      if (!response.ok) {
        throw new Error(data.message || 'Failed to reply');
      }
      setReplyDrafts((prev) => ({ ...prev, [parentId]: '' }));
      setOpenReplyForms((prev) => ({ ...prev, [parentId]: false }));
      await fetchThreads(groupId);
    } catch (err) {
      setToast(err.message);
    }
  };

  useEffect(() => {
    if (!toast) {
      return;
    }
    const timeout = setTimeout(() => setToast(null), 4000);
    return () => clearTimeout(timeout);
  }, [toast]);

  const handleNavigateBack = () => navigate('/my-groups');

  const isOwner = user && group && group.mentorId === user.id;
  const [ownerMenuOpen, setOwnerMenuOpen] = useState(false);

  const handleJoinGroup = async () => {
    if (!token) {
      setToast('Please log in first.');
      return;
    }
    setJoining(true);
    try {
      const response = await fetch(`${API_BASE}/groups/${groupId}/join`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${token}` }
      });
      const data = await response.json().catch(() => ({}));
      if (!response.ok) {
        throw new Error(data.message || 'Failed to join group');
      }
      setToast(`Joined "${data.name}"`);
      setGroup(data);
      setJoined(true);
      await fetchThreads(groupId);
    } catch (err) {
      setToast(err.message);
    } finally {
      setJoining(false);
    }
  };

  const handleSaveGroup = async () => {
    if (!token) {
      setToast('Please log in first.');
      return;
    }
    if (!editForm.name.trim()) {
      setToast('Name is required.');
      return;
    }
    const tags = Array.from(new Set((editForm.tags || []).map((t) => t.trim()).filter(Boolean)));
    if (tags.length === 0) {
      setToast('Please add at least one tag.');
      return;
    }
    setEditForm((prev) => ({ ...prev, tags }));
    try {
      const response = await fetch(`${API_BASE}/groups/${groupId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify({ ...editForm, tags })
      });
      const data = await response.json().catch(() => ({}));
      if (!response.ok) {
        throw new Error(data.message || 'Failed to update group');
      }
      setGroup(data);
      setEditMode(false);
      setToast('Group updated');
    } catch (err) {
      setToast(err.message);
    }
  };

  const postMessage = async (threadId, content, parentId = null) => {
    if (!token) {
      setToast('Please log in first.');
      return false;
    }
    if (!content.trim()) {
      setToast('Please enter content.');
      return false;
    }
    try {
      const response = await fetch(`${API_BASE}/threads/${threadId}/messages`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify({ content, parentMessageId: parentId })
      });
      const data = await response.json().catch(() => ({}));
      if (!response.ok) {
        throw new Error(data.message || 'Failed to post message');
      }
      await fetchThreads(groupId);
      return true;
    } catch (err) {
      setToast(err.message);
      return false;
    }
  };

  const handleCreateThread = async () => {
    if (!token) {
      setToast('Please log in first.');
      return;
    }
    if (!joined) {
      setToast('Join this group to start a discussion.');
      return;
    }
    if (!threadForm.title.trim()) {
      setToast('Please enter a thread title.');
      return;
    }
    try {
      const response = await fetch(`${API_BASE}/groups/${groupId}/threads`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify({ title: threadForm.title, description: threadForm.description })
      });
      const data = await response.json().catch(() => ({}));
      if (!response.ok) {
        throw new Error(data.message || 'Failed to create thread');
      }
      setToast('Thread created');
      setThreadForm({ title: '', description: '' });
      await fetchThreads(groupId);
    } catch (err) {
      setToast(err.message);
    }
  };

  const handleDeleteMessage = async (threadId, messageId) => {
    if (!token) {
      setToast('Please log in first.');
      return;
    }
    try {
      const response = await fetch(`${API_BASE}/threads/${threadId}/messages/${messageId}`, {
        method: 'DELETE',
        headers: { Authorization: `Bearer ${token}` }
      });
      if (!response.ok) {
        throw new Error('Failed to delete message');
      }
      setMessageMenuOpen((prev) => ({ ...prev, [messageId]: false }));
      await fetchThreads(groupId);
    } catch (err) {
      setToast(err.message);
    }
  };

  const handleSaveEdit = async (threadId, messageId) => {
    if (!token) {
      setToast('Please log in first.');
      return;
    }
    const content = editDrafts[messageId] || '';
    if (!content.trim()) {
      setToast('Please enter content.');
      return;
    }
    try {
      const response = await fetch(`${API_BASE}/threads/${threadId}/messages/${messageId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify({ content })
      });
      const data = await response.json().catch(() => ({}));
      if (!response.ok) {
        throw new Error(data.message || 'Failed to update message');
      }
      setEditingMessage((prev) => ({ ...prev, [messageId]: false }));
      setMessageMenuOpen((prev) => ({ ...prev, [messageId]: false }));
      setEditDrafts((prev) => ({ ...prev, [messageId]: '' }));
      await fetchThreads(groupId);
    } catch (err) {
      setToast(err.message);
    }
  };

  if (loading && !group) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="flex items-center space-x-3 text-teal-600">
          <div className="w-4 h-4 rounded-full bg-teal-500 animate-pulse" />
          <div className="w-4 h-4 rounded-full bg-teal-500 animate-pulse" />
          <div className="w-4 h-4 rounded-full bg-teal-500 animate-pulse" />
          <span className="text-lg font-medium">Loading group...</span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="bg-white shadow-md rounded-xl p-6 max-w-md text-center">
          <p className="text-rose-600 font-semibold mb-2">Error loading group</p>
          <p className="text-gray-700 mb-4">{error}</p>
          <button
            onClick={() => navigate('/groups')}
            className="px-6 py-2 bg-teal-500 text-white rounded-lg hover:bg-teal-600 transition"
          >
            Back to Groups
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-5xl mx-auto px-4 py-6">
        <button
          onClick={handleNavigateBack}
          className="flex items-center space-x-2 text-teal-600 hover:text-teal-700 mb-4"
        >
          <ArrowLeft size={18} />
          <span>Back to Groups</span>
        </button>

        {toast && (
          <div className="bg-emerald-50 border border-emerald-200 text-emerald-900 px-4 py-3 rounded-lg mb-4 text-sm">
            {toast}
          </div>
        )}
        {deleteError && (
          <div className="bg-rose-50 border border-rose-200 text-rose-900 px-4 py-3 rounded-lg mb-4 text-sm">
            {deleteError}
          </div>
        )}

        <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-6 mb-6 relative">
          {isOwner && (
            <div className="absolute top-4 right-4">
              <button
                onClick={() => setOwnerMenuOpen((prev) => !prev)}
                className="p-2 rounded-full hover:bg-gray-100 text-gray-600"
                aria-label="Group actions"
              >
                <MoreHorizontal size={18} />
              </button>
              {ownerMenuOpen && (
                <div className="absolute right-0 mt-2 w-40 bg-white border border-gray-200 rounded-lg shadow-lg py-2 z-20">
                  <button
                    onClick={() => {
                      setOwnerMenuOpen(false);
                      setEditMode(true);
                    }}
                    className="block w-full text-left px-4 py-2 text-gray-700 hover:bg-gray-50 text-sm"
                  >
                    Edit group
                  </button>
                  <button
                    onClick={() => {
                      setOwnerMenuOpen(false);
                      setToast('Delete group coming soon.');
                    }}
                    className="block w-full text-left px-4 py-2 text-rose-600 hover:bg-rose-50 text-sm"
                  >
                    Delete group
                  </button>
                </div>
              )}
            </div>
          )}
          {editMode ? (
            <div className="space-y-3">
              <input
                type="text"
                value={editForm.name}
                onChange={(e) => setEditForm((prev) => ({ ...prev, name: e.target.value }))}
                className="w-full border border-gray-300 rounded-lg px-4 py-2 outline-none focus:ring-2 focus:ring-teal-400"
              />
              <textarea
                value={editForm.description}
                onChange={(e) => setEditForm((prev) => ({ ...prev, description: e.target.value }))}
                rows={3}
                className="w-full border border-gray-300 rounded-lg px-4 py-2 outline-none focus:ring-2 focus:ring-teal-400"
              />
              <TagSelector
                value={editForm.tags}
                onChange={(tags) => setEditForm((prev) => ({ ...prev, tags }))}
                max={5}
              />
              <div className="flex items-center gap-3">
                <button
                  onClick={handleSaveGroup}
                  className="px-5 py-2 bg-teal-500 text-white rounded-lg hover:bg-teal-600 transition"
                >
                  Save
                </button>
                <button
                  onClick={() => {
                    setEditMode(false);
                    setEditForm({ name: group.name, description: group.description, tags: group.tags || [] });
                  }}
                  className="px-5 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition"
                >
                  Cancel
                </button>
              </div>
            </div>
          ) : (
            <>
          <h1 className="text-3xl font-bold text-gray-900 mb-1">{group?.name}</h1>
          <p className="text-gray-600 mb-3">{group?.description}</p>
          {group?.tags && (
            <div className="flex flex-wrap gap-2 mb-3">
              {group.tags.map((tag) => (
                <span key={tag} className="px-3 py-1 bg-gray-100 text-gray-700 rounded-full text-xs">{tag}</span>
              ))}
            </div>
          )}
          <div className="flex items-center space-x-4 text-gray-700 mb-3">
            <span className="font-semibold text-teal-600">{group?.memberCount} members</span>
            {group?.mentorName && <span>Mentor: {group.mentorName}</span>}
          </div>
          <div className="flex items-center gap-3 justify-end">
            <button
              onClick={() => fetchThreads(groupId).catch((err) => setThreadsError(err.message))}
              className="px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:border-teal-400 transition"
            >
              Refresh Threads
            </button>
            <button
              onClick={handleJoinGroup}
              disabled={joining || joined}
              className={`px-5 py-2 rounded-lg transition ${joined ? 'bg-teal-100 text-teal-600 cursor-default' : 'bg-teal-500 text-white hover:bg-teal-600'}`}
            >
              {joined ? 'Joined' : joining ? 'Joining...' : 'Join Group'}
            </button>
          </div>
            </>
          )}
        </div>

        <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-6">
          <div className="flex items-center justify-between mb-6">
            <span className="text-lg font-semibold text-gray-900">Discussion</span>
          {threads.length === 0 && <span className="text-sm text-gray-500">No threads yet.</span>}
            <div className="flex items-center gap-3">
              {(joined || isOwner) && (
                <button
                  type="button"
                  onClick={() => setShowThreadForm((prev) => !prev)}
                  className="px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:border-teal-400 transition text-sm"
                >
                  {showThreadForm ? 'Hide form' : 'New Thread'}
                </button>
              )}
            </div>
          </div>

          {(joined || isOwner) && showThreadForm && (
            <div className="bg-gray-50 border border-gray-200 rounded-xl p-4 mb-5">
              <h4 className="text-md font-semibold text-gray-900 mb-3">Create a discussion thread</h4>
              <div className="space-y-3">
                <input
                  type="text"
                  value={threadForm.title}
                  onChange={(e) => setThreadForm((prev) => ({ ...prev, title: e.target.value }))}
                  placeholder="Thread title"
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 outline-none focus:ring-2 focus:ring-teal-400"
                />
                <textarea
                  value={threadForm.description}
                  onChange={(e) => setThreadForm((prev) => ({ ...prev, description: e.target.value }))}
                  placeholder="Thread description (optional)"
                  rows={2}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 outline-none focus:ring-2 focus:ring-teal-400"
                />
                <div className="flex justify-end">
                  <button
                    type="button"
                    onClick={handleCreateThread}
                    className="px-4 py-2 bg-teal-500 text-white rounded-lg hover:bg-teal-600 transition"
                  >
                    Create Thread
                  </button>
                </div>
              </div>
            </div>
          )}

          {threadsError && (
            <div className="bg-rose-50 border border-rose-200 text-rose-900 px-4 py-3 rounded-lg mb-4 text-sm">
              {threadsError.includes('denied') || threadsError.includes('access') || threadsError.includes('Forbidden')
                ? 'Join this group to view threads.'
                : threadsError}
            </div>
          )}

          {threads.map((thread) => {
            const messages = messagesByThread[thread.id] || [];
            const repliesByParent = messages.reduce((acc, msg) => {
              const parent = msg.parentMessageId || null;
              acc[parent] = acc[parent] || [];
              acc[parent].push(msg);
              return acc;
            }, {});
            return (
              <div key={thread.id} className="bg-gray-50 rounded-xl p-5 mb-4">
                <div className="flex items-center justify-between mb-2">
                  <div>
                    <h4 className="text-lg font-semibold text-gray-900">{thread.title}</h4>
                    <p className="text-xs text-gray-500">{new Date(thread.createdAt).toLocaleString()}</p>
                  </div>
                </div>
                {repliesByParent[null]?.length === 0 && (
                  <p className="text-sm text-gray-500">No messages yet.</p>
                )}
                {repliesByParent[null]?.map((msg) => {
                  const renderMessageTree = (message, depth = 0) => (
                    <div
                      key={message.id}
                      className={`bg-white rounded-lg p-4 mb-3 border border-gray-200 ${depth > 0 ? 'ml-4' : ''}`}
                      style={{ marginLeft: depth > 0 ? depth * 12 : 0 }}
                    >
                      <div className="flex items-center justify-between mb-2">
                        <div>
                          <div className="flex items-center gap-2">
                            <p className="font-semibold text-gray-900 text-sm">{message.authorName || 'Member'}</p>
                            {group?.mentorId === message.authorId && (
                              <span className="px-2 py-0.5 bg-gray-100 text-gray-700 text-[11px] font-semibold rounded-full">
                                Group Owner
                              </span>
                            )}
                          </div>
                          <p className="text-xs text-gray-500">{message.authorRole}</p>
                        </div>
                        <div className="flex items-center gap-2">
                          <span className="text-xs text-gray-500">{new Date(message.createdAt).toLocaleString()}</span>
                          {user && (message.authorId === user.id || isOwner) && (
                            <div className="relative">
                              <button
                                type="button"
                                onClick={() => setMessageMenuOpen((prev) => ({ ...prev, [message.id]: !prev[message.id] }))}
                                className="p-1 rounded-full hover:bg-gray-100 text-gray-600"
                                aria-label="Message actions"
                              >
                                <MoreHorizontal size={16} />
                              </button>
                              {messageMenuOpen[message.id] && (
                                <div className="absolute right-0 mt-2 w-36 bg-white border border-gray-200 rounded-lg shadow-md z-20">
                                  {message.authorId === user.id && (
                                    <button
                                      type="button"
                                      onClick={() => {
                                        setEditDrafts((prev) => ({ ...prev, [message.id]: message.content }));
                                        setEditingMessage((prev) => ({ ...prev, [message.id]: true }));
                                        setMessageMenuOpen((prev) => ({ ...prev, [message.id]: false }));
                                      }}
                                      className="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
                                    >
                                      Edit
                                    </button>
                                  )}
                                  {(message.authorId === user.id || isOwner) && (
                                    <button
                                      type="button"
                                      onClick={() => handleDeleteMessage(thread.id, message.id)}
                                      className="block w-full text-left px-4 py-2 text-sm text-rose-600 hover:bg-rose-50"
                                    >
                                      Delete
                                    </button>
                                  )}
                                </div>
                              )}
                            </div>
                          )}
                        </div>
                      </div>
                      {editingMessage[message.id] ? (
                        <div className="space-y-2">
                          <textarea
                            value={editDrafts[message.id] || ''}
                            onChange={(e) => setEditDrafts((prev) => ({ ...prev, [message.id]: e.target.value }))}
                            rows={3}
                            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-teal-400"
                          />
                          <div className="flex justify-end gap-2">
                            <button
                              type="button"
                              onClick={() => {
                                setEditingMessage((prev) => ({ ...prev, [message.id]: false }));
                                setEditDrafts((prev) => ({ ...prev, [message.id]: '' }));
                              }}
                              className="px-3 py-1 border border-gray-300 text-gray-700 rounded hover:bg-gray-50 text-sm"
                            >
                              Cancel
                            </button>
                            <button
                              type="button"
                              onClick={() => handleSaveEdit(thread.id, message.id)}
                              className="px-3 py-1 bg-teal-500 text-white rounded hover:bg-teal-600 text-sm"
                            >
                              Save
                            </button>
                          </div>
                        </div>
                      ) : (
                        renderMarkdown(message.content, message.id)
                      )}
                      {(joined || isOwner) && (
                        <div className="mt-3">
                          {openReplyForms[message.id] ? (
                            <>
                              <textarea
                                value={replyDrafts[message.id] || ''}
                                onChange={(e) => setReplyDrafts((prev) => ({ ...prev, [message.id]: e.target.value }))}
                                placeholder="Add a reply..."
                                rows={2}
                                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-teal-400"
                              />
                              <div className="flex justify-end mt-2 gap-2">
                                <button
                                  type="button"
                                  onClick={() => {
                                    setOpenReplyForms((prev) => ({ ...prev, [message.id]: false }));
                                    setReplyDrafts((prev) => ({ ...prev, [message.id]: '' }));
                                  }}
                                  className="px-3 py-1 border border-gray-300 text-gray-700 rounded hover:bg-gray-50 text-sm"
                                >
                                  Cancel
                                </button>
                                <button
                                  type="button"
                                  onClick={() => handleReply(message.id, thread.id)}
                                  className="px-3 py-1 bg-teal-500 text-white rounded hover:bg-teal-600 text-sm"
                                >
                                  Reply
                                </button>
                              </div>
                            </>
                          ) : (
                            <button
                              type="button"
                              onClick={() => setOpenReplyForms((prev) => ({ ...prev, [message.id]: true }))}
                              className="text-teal-600 hover:text-teal-700 text-sm font-medium"
                            >
                              Reply
                            </button>
                          )}
                        </div>
                      )}
                      {(repliesByParent[message.id] || []).map((child) => renderMessageTree(child, depth + 1))}
                    </div>
                  );
                  return renderMessageTree(msg, 0);
                })}
                {(joined || isOwner) && (
                  <div className="mt-4 pt-4 border-t border-gray-200">
                    {threadComposerOpen[thread.id] ? (
                      <>
                        <textarea
                          value={threadMessageDrafts[thread.id] || ''}
                          onChange={(e) => setThreadMessageDrafts((prev) => ({ ...prev, [thread.id]: e.target.value }))}
                          placeholder="Post a message to this thread..."
                          rows={3}
                          className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-teal-400"
                        />
                        <div className="flex justify-end mt-2 gap-2">
                          <button
                            type="button"
                            onClick={() => {
                              setThreadMessageDrafts((prev) => ({ ...prev, [thread.id]: '' }));
                              setThreadComposerOpen((prev) => ({ ...prev, [thread.id]: false }));
                            }}
                            className="px-3 py-1 border border-gray-300 text-gray-700 rounded hover:bg-gray-50 text-sm"
                          >
                            Cancel
                          </button>
                          <button
                            type="button"
                            onClick={async () => {
                              const content = threadMessageDrafts[thread.id] || '';
                              const ok = await postMessage(thread.id, content, null);
                              if (ok) {
                                setThreadMessageDrafts((prev) => ({ ...prev, [thread.id]: '' }));
                                setThreadComposerOpen((prev) => ({ ...prev, [thread.id]: false }));
                              }
                            }}
                            className="px-3 py-1 bg-teal-500 text-white rounded hover:bg-teal-600 text-sm"
                          >
                            Post
                          </button>
                        </div>
                      </>
                    ) : (
                      <button
                        type="button"
                        onClick={() => setThreadComposerOpen((prev) => ({ ...prev, [thread.id]: true }))}
                        className="text-teal-600 hover:text-teal-700 font-medium text-sm"
                      >
                        Post a message
                      </button>
                    )}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};

export default GroupPage;
