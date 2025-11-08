import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./app/**/*.{js,ts,jsx,tsx}",
    "./components/**/*.{js,ts,jsx,tsx}",
    "./pages/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          primary: "#3DA221",
          dark: "#2A6F15",
          accent: "#FF7F32",
        },
      },
      boxShadow: {
        card: "0 10px 25px rgba(0,0,0,0.1)",
      },
    },
  },
  plugins: [],
};

export default config;
