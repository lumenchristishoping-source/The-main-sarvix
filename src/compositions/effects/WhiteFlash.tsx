import React from 'react';
import { useCurrentFrame, interpolate } from 'remotion';

export const WhiteFlash: React.FC<{ hitFrame: number; holdFrames?: number; fadeFrames?: number }> = ({
  hitFrame,
  holdFrames = 2,
  fadeFrames = 6,
}) => {
  const frame = useCurrentFrame();
  const localFrame = frame - hitFrame;

  if (localFrame < 0 || localFrame > holdFrames + fadeFrames) return null;

  const opacity = interpolate(
    localFrame,
    [0, holdFrames, holdFrames + fadeFrames],
    [1, 1, 0],
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
