import React from 'react';

interface VignetteProps {
  intensity?: number;
}

export const Vignette: React.FC<VignetteProps> = ({ intensity = 0.85 }) => (
  <div
    style={{
      position: 'absolute',
      inset: 0,
      pointerEvents: 'none',
      background: `radial-gradient(ellipse at 50% 50%, transparent 50%, rgba(0,0,0,${intensity}) 100%)`,
      zIndex: 90,
    }}
  />
);
