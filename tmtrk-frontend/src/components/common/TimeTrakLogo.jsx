const TimeTrakLogo = ({ width = 300, height = 100, className = "" }) => (
  <svg 
    xmlns="http://www.w3.org/2000/svg" 
    viewBox="0 0 300 100" 
    width={width} 
    height={height}
    className={className}
  >
    <defs>
      {/* Gradient for the icon */}
      <linearGradient id="iconGradient" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" style={{stopColor:"#4299e1", stopOpacity:1}} />
        <stop offset="100%" style={{stopColor:"#2c5282", stopOpacity:1}} />
      </linearGradient>
      
      {/* Gradient for accent */}
      <linearGradient id="accentGradient" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" style={{stopColor:"#48bb78", stopOpacity:1}} />
        <stop offset="100%" style={{stopColor:"#38a169", stopOpacity:1}} />
      </linearGradient>
    </defs>
    
    {/* Background circle for the icon */}
    <circle cx="50" cy="50" r="30" fill="url(#iconGradient)" />
    
    {/* Clock face */}
    <circle cx="50" cy="50" r="22" fill="white" stroke="url(#iconGradient)" strokeWidth="2" />
    
    {/* Clock markings */}
    <g stroke="#2c5282" strokeWidth="2" fill="none">
      {/* 12 o'clock */}
      <line x1="50" y1="33" x2="50" y2="38" />
      {/* 3 o'clock */}
      <line x1="67" y1="50" x2="62" y2="50" />
      {/* 6 o'clock */}
      <line x1="50" y1="67" x2="50" y2="62" />
      {/* 9 o'clock */}
      <line x1="33" y1="50" x2="38" y2="50" />
    </g>
    
    {/* Clock hands */}
    <g stroke="#2c5282" strokeWidth="3" strokeLinecap="round">
      {/* Hour hand pointing to 2 */}
      <line x1="50" y1="50" x2="58" y2="42" />
      {/* Minute hand pointing to 6 */}
      <line x1="50" y1="50" x2="50" y2="38" />
    </g>
    
    {/* Center dot */}
    <circle cx="50" cy="50" r="3" fill="#2c5282" />
    
    {/* Checkmark accent */}
    <g transform="translate(68, 32)">
      <circle r="10" fill="url(#accentGradient)" />
      <path 
        d="M -4 0 L -1 3 L 4 -3" 
        stroke="white" 
        strokeWidth="2.5" 
        fill="none" 
        strokeLinecap="round" 
        strokeLinejoin="round" 
      />
    </g>
    
    {/* Company name */}
    <text x="110" y="45" fontFamily="'Segoe UI', Arial, sans-serif" fontSize="28" fontWeight="700" fill="#2c5282">
      Time<tspan fill="url(#accentGradient)">Trak</tspan>
    </text>
    
    {/* Tagline */}
    <text x="112" y="65" fontFamily="'Segoe UI', Arial, sans-serif" fontSize="12" fontWeight="400" fill="#718096" letterSpacing="1px">
      PROFESSIONAL TIME TRACKING
    </text>
  </svg>
);

export default TimeTrakLogo;