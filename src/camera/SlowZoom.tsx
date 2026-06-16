import React from 'react';
import { useCurrentFrame, interpolate } from 'remotion';
import { easeOutCinematic } from '../utils/easing';

interface SlowZoomProps {
  children: React.ReactNode;
  duration: number;
  startScale?: number;
  endScale?: number;
  startFrame?: number;
}

export const SlowZoom: React.FC<SlowZoomProps> = ({
  children,
  duration,
  startScale = 1.0,
  endScale = 1.08,
  startFrame = 0,
}) => {
  const frame = useCurrentFrame();
  const localFrame = frame - startFrame;

  const scale = interpolate(localFrame, [0, duration], [startScale, endScale], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
    easing: easeOutCinematic,
  });

  return (
    <div
      style={{
        width: '100%',
        height: '100%',
        transform: `scale(${scale})`,
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
