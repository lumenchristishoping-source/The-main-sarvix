import React from 'react';

export const DBVignette: React.FC<{ intensity?: number }> = ({ intensity = 0.8 }) => (
  <div
    style={{
      position: 'absolute',
      inset: 0,
      pointerEvents: 'none',
      background: `radial-gradient(ellipse at 50% 50%, transparent 40%, rgba(0,0,0,${intensity}) 100%)`,
      zIndex: 10,
    }}
  />
);
