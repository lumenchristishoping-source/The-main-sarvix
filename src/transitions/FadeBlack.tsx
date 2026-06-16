import React from 'react';
import { useCurrentFrame, interpolate } from 'remotion';
import { easeOutCinematic } from '../utils/easing';

interface FadeBlackProps {
  startFrame: number;
  duration?: number;
  direction?: 'in' | 'out';
}

export const FadeBlack: React.FC<FadeBlackProps> = ({
  startFrame,
  duration = 30,
  direction = 'out',
}) => {
  const frame = useCurrentFrame();
  const localFrame = frame - startFrame;

  if (localFrame < 0 || localFrame > duration) return null;

  const opacity = interpolate(
    localFrame,
    [0, duration],
    direction === 'out' ? [0, 1] : [1, 0],
    {
      extrapolateLeft: 'clamp',
      extrapolateRight: 'clamp',
      easing: easeOutCinematic,
    }
  );

  return (
    <div
      style={{
        position: 'absolute',
        inset: 0,
        background: 'black',
        opacity,
        zIndex: 180,
        pointerEvents: 'none',
      }}
    />
  );
};
