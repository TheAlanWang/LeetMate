export type AiHighlightCardProps = {
  title: string;
  description: string;
};

export function AiHighlightCard({ title, description }: AiHighlightCardProps) {
  return (
    <div className="card border border-green-200 bg-white/80">
      <p className="text-base font-semibold text-brand-dark">{title}</p>
      <p className="text-sm text-gray-600">{description}</p>
    </div>
  );
}
