import React from 'react';
import { useCurrentFrame, interpolate } from 'remotion';

export const GlitchTransition: React.FC<{ triggerFrame: number; duration?: number }> = ({
  triggerFrame,
  duration = 5,
}) => {
  const frame = useCurrentFrame();
  const localFrame = frame - triggerFrame;

  if (localFrame < 0 || localFrame > duration) return null;

  const progress = localFrame / duration;
  const shift = interpolate(localFrame, [0, duration / 2, duration], [6, 12, 0], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
  });

  const hueRotate = interpolate(localFrame, [0, duration], [0, 180], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
  });

  const opacity = interpolate(localFrame, [0, 1, duration - 1, duration], [0, 0.7, 0.7, 0], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
  });

  return (
    <div style={{ position: 'absolute', inset: 0, pointerEvents: 'none', zIndex: 150, opacity }}>
      {/* Red channel */}
      <div
        style={{
          position: 'absolute',
          inset: 0,
          background: 'rgba(255,0,0,0.25)',
          transform: `translateX(${-shift}px)`,
          mixBlendMode: 'screen',
          filter: `hue-rotate(${hueRotate}deg)`,
        }}
      />
      {/* Blue channel */}
      <div
        style={{
          position: 'absolute',
          inset: 0,
          background: 'rgba(0,0,255,0.25)',
          transform: `translateX(${shift}px)`,
          mixBlendMode: 'screen',
        }}
      />
      {/* Scanline bar */}
      <div
        style={{
          position: 'absolute',
          left: 0,
          right: 0,
          top: `${progress * 100}%`,
          height: 4,
          background: 'rgba(255,255,255,0.8)',
        }}
      />
    </div>
  );
};
