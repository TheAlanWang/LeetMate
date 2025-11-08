import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "LeetMate | Collaborative Mentorship",
  description: "Discover mentor-led LeetCode study groups with AI assistance.",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
