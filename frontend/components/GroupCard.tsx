export type GroupCardProps = {
  title: string;
  mentor: string;
  mentorTagline: string;
  members: number;
  badge?: string;
};

export function GroupCard({ title, mentor, mentorTagline, members, badge = "Free" }: GroupCardProps) {
  return (
    <div className="card flex flex-col gap-3 bg-gradient-to-br from-white to-green-50">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-lg font-semibold text-brand-dark">{title}</p>
          <p className="text-sm text-gray-600">
            {mentor}
            <span className="ml-2 text-xs text-gray-500">{mentorTagline}</span>
          </p>
        </div>
        <span className="rounded-full bg-orange-100 px-3 py-1 text-sm font-semibold text-brand-accent">
          {badge}
        </span>
      </div>
      <p className="text-sm font-medium text-gray-700">
        {members}
        <span className="ml-1 text-xs font-normal uppercase tracking-wide text-gray-500">members</span>
      </p>
    </div>
  );
}
