const TimeTrakIcon = ({ width = 64, height = 64, className = "" }) => (
  <svg 
    xmlns="http://www.w3.org/2000/svg" 
    viewBox="0 0 100 100" 
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
    <circle cx="50" cy="50" r="40" fill="url(#iconGradient)" />
    
    {/* Clock face */}
    <circle cx="50" cy="50" r="30" fill="white" stroke="url(#iconGradient)" strokeWidth="3" />
    
    {/* Clock markings */}
    <g stroke="#2c5282" strokeWidth="3" fill="none">
      {/* 12 o'clock */}
      <line x1="50" y1="26" x2="50" y2="32" />
      {/* 3 o'clock */}
      <line x1="74" y1="50" x2="68" y2="50" />
      {/* 6 o'clock */}
      <line x1="50" y1="74" x2="50" y2="68" />
      {/* 9 o'clock */}
      <line x1="26" y1="50" x2="32" y2="50" />
    </g>
    
    {/* Clock hands */}
    <g stroke="#2c5282" strokeWidth="4" strokeLinecap="round">
      {/* Hour hand pointing to 2 */}
      <line x1="50" y1="50" x2="62" y2="38" />
      {/* Minute hand pointing to 6 */}
      <line x1="50" y1="50" x2="50" y2="32" />
    </g>
    
    {/* Center dot */}
    <circle cx="50" cy="50" r="4" fill="#2c5282" />
    
    {/* Checkmark accent */}
    <g transform="translate(72, 28)">
      <circle r="12" fill="url(#accentGradient)" />
      <path 
        d="M -5 0 L -1 4 L 5 -4" 
        stroke="white" 
        strokeWidth="3" 
        fill="none" 
        strokeLinecap="round" 
        strokeLinejoin="round" 
      />
    </g>
  </svg>
);

export default TimeTrakIcon;