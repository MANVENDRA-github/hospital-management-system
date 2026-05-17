/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        // Slate / charcoal monochrome palette mapped onto the `brand-*` scale
        // so all existing class names keep working.
        brand: {
          50:  '#f8fafc', // slate-50  (subtle bg)
          100: '#f1f5f9', // slate-100 (active nav bg, avatar bg)
          200: '#e2e8f0', // slate-200 (ring/border)
          300: '#cbd5e1', // slate-300
          400: '#94a3b8', // slate-400
          500: '#64748b', // slate-500 (focus ring base)
          600: '#0f172a', // slate-900 (PRIMARY: buttons, logo)
          700: '#1e293b', // slate-800 (hover, active text)
          800: '#0f172a', // slate-900
          900: '#020617', // slate-950 (deepest accent)
        },
      },
      fontFamily: {
        sans: ['Inter', 'ui-sans-serif', 'system-ui', '-apple-system', 'Segoe UI', 'Roboto', 'Helvetica Neue', 'Arial', 'sans-serif'],
      },
      boxShadow: {
        card: '0 1px 3px 0 rgb(15 23 42 / 0.06), 0 1px 2px -1px rgb(15 23 42 / 0.04)',
        'card-hover': '0 12px 28px -8px rgb(15 23 42 / 0.10), 0 4px 10px -4px rgb(15 23 42 / 0.05)',
      },
    },
  },
  plugins: [],
};
