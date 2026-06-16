import React from 'react';
import { useCurrentFrame, interpolate } from 'remotion';

export const AuraOverlay: React.FC<{
  startFrame: number;
  peakFrame: number;
  endFrame: number;
  color?: string;
}> = ({ startFrame, peakFrame, endFrame, color = 'rgba(255, 200, 0,' }) => {
  const frame = useCurrentFrame();

  const opacity = interpolate(
    frame,
    [startFrame, peakFrame, endFrame],
    [0, 0.55, 0],
    { extrapolateLeft: 'clamp', extrapolateRight: 'clamp' }
  );

  const scale = interpolate(
    frame,
    [startFrame, peakFrame],
    [0.8, 1.2],
    { extrapolateLeft: 'clamp', extrapolateRight: 'clamp' }
  );

  return (
    <div
      style={{
        position: 'absolute',
        inset: 0,
        pointerEvents: 'none',
        opacity,
        mixBlendMode: 'screen',
        background: `radial-gradient(ellipse at 50% 65%, ${color} 0.6) 0%, ${color} 0.3) 40%, rgba(0,0,0,0) 70%)`,
        transform: `scale(${scale})`,
      }}
    />
  );
};
