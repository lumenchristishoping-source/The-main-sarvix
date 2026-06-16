import React from 'react';

interface ChromaticAberrationProps {
  offset: number;
  children: React.ReactNode;
}

export const ChromaticAberration: React.FC<ChromaticAberrationProps> = ({
  offset,
  children,
}) => (
  <div style={{ position: 'relative', width: '100%', height: '100%' }}>
    <svg width="0" height="0" style={{ position: 'absolute' }}>
      <defs>
        <filter id="ca-red">
          <feColorMatrix
            type="matrix"
            values="1 0 0 0 0  0 0 0 0 0  0 0 0 0 0  0 0 0 1 0"
          />
        </filter>
        <filter id="ca-blue">
          <feColorMatrix
            type="matrix"
            values="0 0 0 0 0  0 0 0 0 0  0 0 1 0 0  0 0 0 1 0"
          />
        </filter>
      </defs>
    </svg>

    {/* Red channel shifted left */}
    <div
      style={{
        position: 'absolute',
        inset: 0,
        transform: `translateX(${-offset}px)`,
        mixBlendMode: 'screen',
        filter: 'url(#ca-red)',
        opacity: 0.8,
      }}
    >
      {children}
    </div>

    {/* Blue channel shifted right */}
    <div
      style={{
        position: 'absolute',
        inset: 0,
        transform: `translateX(${offset}px)`,
        mixBlendMode: 'screen',
        filter: 'url(#ca-blue)',
        opacity: 0.8,
      }}
    >
      {children}
    </div>

    {/* Base layer */}
    <div style={{ position: 'absolute', inset: 0 }}>{children}</div>
  </div>
);
