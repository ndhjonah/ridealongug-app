export default {
  content: ['./src/**/*.{html,ts}'],
  theme: {
    extend: {
      colors: {
        ink: '#14161A',
        paper: '#FBF8F2',
        marigold: '#F2B705',
        savanna: '#2F6D4F',
        brick: '#B33F2E',
        charcoal: '#3A3D42',
        mist: '#E7E2D6',
      },
      fontFamily: {
        display: ['"Space Grotesk"', 'sans-serif'],
        body: ['Inter', 'sans-serif'],
        plate: ['"JetBrains Mono"', 'monospace'],
      },
      borderRadius: {
        sign: '4px',
      },
    },
  },
  plugins: [],
};
