import React from 'react';
import { useCurrentFrame, interpolate } from 'remotion';
import { easeOutCinematic } from '../utils/easing';

interface GlitchWipeProps {
  startFrame: number;
  duration?: number;
}

export const GlitchWipe: React.FC<GlitchWipeProps> = ({
  startFrame,
  duration = 8,
}) => {
  const frame = useCurrentFrame();
  const localFrame = frame - startFrame;

  if (localFrame < 0 || localFrame > duration) return null;

  const progress = interpolate(localFrame, [0, duration], [0, 100], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
    easing: easeOutCinematic,
  });

  const rShift = interpolate(localFrame, [0, duration], [8, 0], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
  });

  return (
    <div
      style={{
        position: 'absolute',
        inset: 0,
        overflow: 'hidden',
        zIndex: 150,
        pointerEvents: 'none',
      }}
    >
      <div
        style={{
          position: 'absolute',
          inset: 0,
          background: 'rgba(255,0,0,0.3)',
          clipPath: `inset(0 0 0 ${Math.max(0, progress - rShift)}%)`,
        }}
      />
      <div
        style={{
          position: 'absolute',
          inset: 0,
          background: 'rgba(0,0,255,0.3)',
          clipPath: `inset(0 0 0 ${Math.min(100, progress + rShift)}%)`,
        }}
      />
      <div
        style={{
          position: 'absolute',
          inset: 0,
          background: 'rgba(0,0,0,0.95)',
          clipPath: `inset(0 0 0 ${progress}%)`,
        }}
      />
    </div>
  );
};
