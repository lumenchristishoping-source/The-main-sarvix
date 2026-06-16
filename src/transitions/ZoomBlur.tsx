import React from 'react';
import { useCurrentFrame, interpolate } from 'remotion';
import { easeOutCinematic } from '../utils/easing';

interface ZoomBlurProps {
  startFrame: number;
  duration?: number;
}

export const ZoomBlur: React.FC<ZoomBlurProps> = ({
  startFrame,
  duration = 12,
}) => {
  const frame = useCurrentFrame();
  const localFrame = frame - startFrame;

  if (localFrame < 0 || localFrame > duration) return null;

  const scale = interpolate(localFrame, [0, duration], [1, 2.5], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
    easing: easeOutCinematic,
  });

  const opacity = interpolate(localFrame, [0, duration], [1, 0], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
  });

  const blur = interpolate(localFrame, [0, duration], [0, 20], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
  });

  return (
    <div
      style={{
        position: 'absolute',
        inset: 0,
        background: 'black',
        opacity: 1 - opacity,
        transform: `scale(${scale})`,
        filter: `blur(${blur}px)`,
        zIndex: 150,
        pointerEvents: 'none',
      }}
    />
  );
};
