import React from 'react';
import { useCurrentFrame, spring } from 'remotion';
import { bouncy } from '../utils/springs';

interface GlowPulseProps {
  color?: string;
  children?: React.ReactNode;
  triggerFrame?: number;
}

export const GlowPulse: React.FC<GlowPulseProps> = ({
  color = 'rgba(255,200,100,0.6)',
  children,
  triggerFrame = 0,
}) => {
  const frame = useCurrentFrame();
  const localFrame = Math.max(0, frame - triggerFrame);

  const glow = spring({ frame: localFrame, fps: 60, config: bouncy });
  const glowSize = glow * 40;

  return (
    <div
      style={{
        filter: `drop-shadow(0 0 ${glowSize}px ${color})`,
        display: 'contents',
      }}
    >
      {children}
    </div>
  );
};
