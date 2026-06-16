import React from 'react';
import { useCurrentFrame, interpolate, spring } from 'remotion';

export const ImpactShake: React.FC<{
  hitFrame: number;
  children: React.ReactNode;
  intensity?: number;
}> = ({ hitFrame, children, intensity = 1 }) => {
  const frame = useCurrentFrame();
  const localFrame = frame - hitFrame;

  const scalePunch = localFrame >= 0 && localFrame <= 10
    ? spring({ frame: localFrame, fps: 30, config: { damping: 8, stiffness: 200 }, from: 1.0, to: 1.08 })
    : 1.0;

  const shake = localFrame >= 0 && localFrame <= 6
    ? interpolate(localFrame, [0, 1, 2, 3, 4, 5, 6], [0, -2, 2, -1.5, 1.5, -0.5, 0]) * intensity
    : 0;

  return (
    <div
      style={{
        width: '100%',
        height: '100%',
        transform: `scale(${scalePunch}) rotate(${shake}deg)`,
        transformOrigin: '50% 50%',
      }}
    >
      {children}
    </div>
  );
};
