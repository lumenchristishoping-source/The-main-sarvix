import React from 'react';
import { useCurrentFrame } from 'remotion';
import { lerp } from '../utils/interpolators';
import { BEATS } from '../utils/beatMap';

export const LightLeak: React.FC = () => {
  const frame = useCurrentFrame();

  const activeBeat = BEATS.filter((b) => b <= frame).pop() ?? 0;
  const timeSinceBeat = frame - activeBeat;

  const opacity = lerp(timeSinceBeat, [0, 20], [0.5, 0]);
  const pos = lerp(frame, [0, 300], [-20, 120]);

  return (
    <div
      style={{
        position: 'absolute',
        inset: 0,
        pointerEvents: 'none',
        overflow: 'hidden',
        zIndex: 80,
        opacity,
        mixBlendMode: 'screen',
      }}
    >
      <div
        style={{
          position: 'absolute',
          top: 0,
          left: `${pos}%`,
          width: '40%',
          height: '100%',
          background:
            'linear-gradient(135deg, rgba(255,200,100,0) 0%, rgba(255,180,80,0.35) 50%, rgba(255,200,100,0) 100%)',
          transform: 'skewX(-15deg)',
        }}
      />
    </div>
  );
};
