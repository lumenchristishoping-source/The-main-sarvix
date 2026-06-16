import React from 'react';
import { useCurrentFrame, interpolate } from 'remotion';

interface FlashCutProps {
  startFrame: number;
  duration?: number;
}

export const FlashCut: React.FC<FlashCutProps> = ({
  startFrame,
  duration = 4,
}) => {
  const frame = useCurrentFrame();
  const localFrame = frame - startFrame;

  if (localFrame < 0 || localFrame > duration) return null;

  const opacity = interpolate(
    localFrame,
    [0, duration / 2, duration],
    [0, 1, 0],
    { extrapolateLeft: 'clamp', extrapolateRight: 'clamp' }
  );

  return (
    <div
      style={{
        position: 'absolute',
        inset: 0,
        background: 'white',
        opacity,
        zIndex: 200,
        pointerEvents: 'none',
      }}
    />
  );
};
