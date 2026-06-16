import React from 'react';
import { useCurrentFrame, interpolate } from 'remotion';
import { easeInOutCubic } from '../utils/easing';

interface DynamicPanProps {
  children: React.ReactNode;
  direction?: 'left' | 'right';
  duration: number;
  startFrame?: number;
  distance?: number;
}

export const DynamicPan: React.FC<DynamicPanProps> = ({
  children,
  direction = 'left',
  duration,
  startFrame = 0,
  distance = 80,
}) => {
  const frame = useCurrentFrame();
  const localFrame = frame - startFrame;

  const from = direction === 'left' ? 0 : -distance;
  const to = direction === 'left' ? -distance : 0;

  const translateX = interpolate(localFrame, [0, duration], [from, to], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
    easing: easeInOutCubic,
  });

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
