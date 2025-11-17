import React, { useMemo, useState } from 'react';

const PRESET_TAGS = ['system_design', 'algorithms', 'behavior_question'];

const TagSelector = ({ value = [], onChange, max = 5 }) => {
  const [input, setInput] = useState('');

  const availableTags = useMemo(
    () => PRESET_TAGS.filter((tag) => !value.includes(tag)),
    [value]
  );

  const addTag = (tag) => {
    if (!tag) return;
    const normalized = tag.trim().toLowerCase().replace(/\s+/g, '_');
    if (!normalized || value.includes(normalized) || value.length >= max) return;
    onChange([...value, normalized]);
    setInput('');
  };

  const removeTag = (tag) => {
    onChange(value.filter((t) => t !== tag));
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' || e.key === ',') {
      e.preventDefault();
      addTag(input);
    }
  };

  return (
    <div>
      <label className="text-sm text-gray-600 mb-2 block">Tags (select or add)</label>
      <div className="flex flex-wrap gap-2 mb-2">
        {value.map((tag) => (
          <span
            key={tag}
            className="inline-flex items-center gap-1 px-3 py-1 bg-teal-50 text-teal-700 rounded-full text-xs font-semibold"
          >
            {tag}
            <button
              type="button"
              onClick={() => removeTag(tag)}
              className="text-teal-600 hover:text-teal-800"
            >
              ×
            </button>
          </span>
        ))}
      </div>
      <div className="flex items-center gap-2">
        <input
          type="text"
          placeholder="Type and press Enter"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={handleKeyDown}
          className="flex-1 border border-gray-300 rounded-lg px-3 py-2 outline-none focus:ring-2 focus:ring-teal-400"
        />
        <button
          type="button"
          onClick={() => addTag(input)}
          className="px-3 py-2 bg-teal-500 text-white rounded-lg hover:bg-teal-600 transition text-sm"
          disabled={!input.trim() || value.length >= max}
        >
          Add
        </button>
      </div>
      {availableTags.length > 0 && (
        <div className="mt-3 flex flex-wrap gap-2">
          {availableTags.map((tag) => (
            <button
              key={tag}
              type="button"
              onClick={() => addTag(tag)}
              className="px-3 py-1 border border-gray-200 rounded-full text-sm text-gray-700 hover:border-teal-400 hover:text-teal-600"
              disabled={value.length >= max}
            >
              {tag}
            </button>
          ))}
        </div>
      )}
      <p className="text-xs text-gray-500 mt-2">Up to {max} tags. Presets: system_design, algorithms, behavior_question.</p>
    </div>
  );
};

export default TagSelector;
