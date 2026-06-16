import React from 'react';
import { useCurrentFrame, interpolate } from 'remotion';

export const SpeedLines: React.FC<{ triggerFrame: number; duration?: number }> = ({
  triggerFrame,
  duration = 8,
}) => {
  const frame = useCurrentFrame();
  const localFrame = frame - triggerFrame;

  if (localFrame < 0 || localFrame > duration) return null;

  const scale = interpolate(localFrame, [0, duration], [1.0, 1.5], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
  });

  const opacity = interpolate(localFrame, [0, duration * 0.3, duration], [0, 0.4, 0], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
  });

  const lines = Array.from({ length: 24 }, (_, i) => {
    const angle = (i / 24) * 360;
    return angle;
  });

  return (
    <div
      style={{
        position: 'absolute',
        inset: 0,
        pointerEvents: 'none',
        opacity,
        mixBlendMode: 'overlay',
        zIndex: 60,
        transform: `scale(${scale})`,
        transformOrigin: '50% 50%',
      }}
    >
      <svg width="100%" height="100%" viewBox="0 0 1920 1080">
        {lines.map((angle, i) => {
          const cx = 960;
          const cy = 540;
          const rad = (angle * Math.PI) / 180;
          const len = 800;
          const width = 8 + (i % 3) * 4;
          return (
            <line
              key={i}
              x1={cx}
              y1={cy}
              x2={cx + Math.cos(rad) * len}
              y2={cy + Math.sin(rad) * len}
              stroke="white"
              strokeWidth={width}
              strokeOpacity={0.6}
            />
          );
        })}
      </svg>
    </div>
  );
};
