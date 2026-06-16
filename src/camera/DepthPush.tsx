import React from 'react';
import { useCurrentFrame, interpolate } from 'remotion';
import { easeOutCinematic } from '../utils/easing';

interface DepthPushProps {
  children: React.ReactNode;
  duration: number;
  startFrame?: number;
  intensity?: number;
}

export const DepthPush: React.FC<DepthPushProps> = ({
  children,
  duration,
  startFrame = 0,
  intensity = 1,
}) => {
  const frame = useCurrentFrame();
  const localFrame = frame - startFrame;

  const scale = interpolate(
    localFrame,
    [0, duration],
    [1.0, 1.0 + 0.12 * intensity],
    {
      extrapolateLeft: 'clamp',
      extrapolateRight: 'clamp',
      easing: easeOutCinematic,
    }
  );

  const translateZ = interpolate(
    localFrame,
    [0, duration],
    [0, 60 * intensity],
    {
      extrapolateLeft: 'clamp',
      extrapolateRight: 'clamp',
      easing: easeOutCinematic,
    }
  );

  return (
    <div
      style={{
        width: '100%',
        height: '100%',
        transform: `perspective(800px) translateZ(${translateZ}px) scale(${scale})`,
        transformOrigin: '50% 50%',
        overflow: 'hidden',
        position: 'absolute',
        inset: 0,
      }}
    >
      {children}
    </div>
  );
};
