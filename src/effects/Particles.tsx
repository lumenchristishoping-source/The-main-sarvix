import React from 'react';
import { useCurrentFrame } from 'remotion';

interface ParticlesProps {
  opacity?: number;
  count?: number;
}

export const Particles: React.FC<ParticlesProps> = ({
  opacity = 0.4,
  count = 80,
}) => {
  const frame = useCurrentFrame();

  const particles = Array.from({ length: count }, (_, i) => {
    const seed = i * 137.508;
    const drift = i % 3 === 0 ? 1 : -1;
    const x = ((Math.sin(seed) * 0.5 + 0.5) * 100 + frame * 0.02 * drift) % 100;
    const y = ((Math.cos(seed * 1.3) * 0.5 + 0.5) * 100 + frame * 0.015) % 100;
    const size = 1 + (Math.sin(seed * 2.7) * 0.5 + 0.5) * 2;
    const alpha = 0.3 + (Math.cos(seed + frame * 0.05) * 0.5 + 0.5) * 0.7;
    return { x, y, size, alpha };
  });

  return (
    <div
      style={{
        position: 'absolute',
        inset: 0,
        pointerEvents: 'none',
        opacity,
        zIndex: 70,
      }}
    >
      <svg width="100%" height="100%" viewBox="0 0 1920 1080">
        {particles.map((p, i) => (
          <circle
            key={i}
            cx={p.x * 19.2}
            cy={p.y * 10.8}
            r={p.size}
            fill={`rgba(255,255,255,${p.alpha})`}
          />
        ))}
      </svg>
    </div>
  );
};
