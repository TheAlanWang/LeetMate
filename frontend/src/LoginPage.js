import React, { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from './App';

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
        onMessage?.(mode === 'login' ? 'Login successful, welcome back!' : 'Registration successful, you are now logged in.');
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
              Currently logged in: {user.name} ({user.role})
            </p>
            <p className="text-gray-500 text-sm mt-1">
              {isMentor ? 'You can create study groups and challenges.' : isMentee ? 'You can join groups and submit code.' : ''}
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
            {value === 'login' ? 'Log In' : 'Sign Up'}
          </button>
        ))}
      </div>
      <form onSubmit={handleSubmit} className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {mode === 'register' && (
          <input
            type="text"
            value={form.name}
            onChange={(e) => handleChange('name', e.target.value)}
            placeholder="Full Name"
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
          placeholder="Password"
          className="border border-gray-300 rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-teal-400 col-span-1 md:col-span-2"
          required
        />
        {mode === 'register' && (
          <div className="col-span-1 md:col-span-2">
            <label className="text-sm font-medium text-gray-600 mb-2 block">Role</label>
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
          {submitting ? 'Loading...' : mode === 'login' ? 'Log In' : 'Sign Up & Log In'}
        </button>
        
        {mode === 'login' && (
          <div className="col-span-1 md:col-span-2 text-center text-sm text-gray-600 mt-4">
            If you don't have an account,{' '}
            <button
              type="button"
              onClick={() => setMode('register')}
              className="text-teal-600 hover:text-teal-700 font-medium underline"
            >
              Sign up
            </button>
          </div>
        )}
      </form>
    </div>
  );
};

export default LoginPage;
export { AuthPanel };