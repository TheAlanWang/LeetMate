import { AiHighlightCard } from "@/components/AiHighlightCard";
import { GroupCard } from "@/components/GroupCard";

const groups = [
  {
    title: "Leetcode Daily",
    mentor: "Alan Wang",
    mentorTagline: "NEU CSA",
    members: 312,
  },
  {
    title: "Dynamic Programming",
    mentor: "ShiKun Y",
    mentorTagline: "Amazon SDE lvl3",
    members: 312,
  },
];

const aiHighlights = [
  {
    title: "Array",
    description: "AI-generated patterns, edge-case reminders, and code reviews on arrays.",
  },
  {
    title: "Daily question",
    description: "Start your day with an AI-curated challenge that matches your streak.",
  },
];

const navLinks = ["Mentor List", "Group List", "Community"];

export default function Home() {
  return (
    <main className="mx-auto flex max-w-6xl flex-col gap-8 px-4 py-8">
      <header className="flex flex-col gap-4 rounded-2xl border border-green-200 bg-white/90 p-4 shadow-card sm:flex-row sm:items-center sm:justify-between">
        <div className="flex items-center gap-2 text-2xl font-semibold text-brand-dark">
          <span className="rounded-md border border-brand-dark px-3 py-1 text-base font-semibold text-brand-dark">leetmate</span>
        </div>
        <nav className="flex flex-wrap gap-3">
          {navLinks.map((link) => (
            <button
              key={link}
              className="rounded-md border border-brand-primary px-4 py-2 text-sm font-semibold text-brand-primary transition hover:bg-brand-primary hover:text-white"
            >
              {link}
            </button>
          ))}
        </nav>
        <div className="flex gap-3">
          <button className="rounded-md border border-brand-dark px-4 py-2 text-sm font-semibold text-brand-dark">Log in</button>
          <button className="button-primary">Sign up</button>
        </div>
      </header>

      <section className="rounded-2xl border border-green-200 bg-white/80 p-4 shadow-card">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
          <div className="flex items-center gap-3 text-sm text-gray-500">
            <span className="font-semibold text-brand-dark">Explore</span>
            <div className="flex items-center gap-2 rounded-full border border-gray-200 bg-white px-4 py-2 shadow-inner">
              <input
                placeholder="What do you want to learn?"
                className="w-full border-none text-sm text-gray-700 focus:outline-none"
              />
              <span className="text-brand-primary">🔍</span>
            </div>
          </div>
          <div className="flex flex-col gap-3 text-center text-white sm:flex-row">
            <button className="button-primary w-full bg-brand-primary">BE a Mentor · Create a group</button>
            <button className="button-primary w-full bg-brand-primary">BE a Mentee · Join a group</button>
          </div>
        </div>
      </section>

      <section className="grid gap-6 lg:grid-cols-3">
        <div className="lg:col-span-2 space-y-5 rounded-3xl border border-green-200 bg-gradient-to-br from-green-50 to-green-100 p-6 shadow-card">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xl font-bold text-brand-dark">Group</p>
              <p className="text-sm text-gray-600">Popular groups curated by mentors</p>
            </div>
            <span className="text-sm font-semibold text-brand-primary">Popular Group →</span>
          </div>
          <div className="flex flex-col gap-4">
            {groups.map((group) => (
              <GroupCard key={group.title} {...group} />
            ))}
          </div>
        </div>
        <div className="space-y-4 rounded-3xl border border-green-200 bg-gradient-to-b from-green-50 to-green-100 p-6 shadow-card">
          <p className="text-xl font-bold text-brand-dark">AI Group</p>
          {aiHighlights.map((highlight) => (
            <AiHighlightCard key={highlight.title} {...highlight} />
          ))}
        </div>
      </section>
    </main>
  );
}
