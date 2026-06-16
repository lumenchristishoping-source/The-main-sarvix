import React from 'react';
import { useCurrentFrame, interpolate } from 'remotion';

interface ParallaxProps {
  children: React.ReactNode;
  speed?: number;
  duration: number;
  startFrame?: number;
  distance?: number;
}

export const Parallax: React.FC<ParallaxProps> = ({
  children,
  speed = 0.3,
  duration,
  startFrame = 0,
  distance = 60,
}) => {
  const frame = useCurrentFrame();
  const localFrame = frame - startFrame;

  const translateX = interpolate(
    localFrame,
    [0, duration],
    [0, -distance * speed],
    { extrapolateLeft: 'clamp', extrapolateRight: 'clamp' }
  );

  return (
    <div
      style={{
        width: '110%',
        height: '100%',
        transform: `translateX(${translateX}px)`,
        overflow: 'hidden',
        position: 'absolute',
        top: 0,
        left: 0,
      }}
    >
      {children}
    </div>
  );
};
