import React from 'react';

interface SceneBgProps {
  index: number;
  label?: string;
}

export const SceneBg: React.FC<SceneBgProps> = ({ index, label }) => {
  const hue = (index * 40) % 360;
  return (
    <div
      style={{
        width: '100%',
        height: '100%',
        background: `hsl(${hue}, 40%, 15%)`,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        position: 'absolute',
        inset: 0,
      }}
    >
      <span
        style={{
          color: 'rgba(255,255,255,0.3)',
          fontSize: 24,
          fontFamily: 'sans-serif',
          textAlign: 'center',
          padding: '0 40px',
        }}
      >
        INSERT: {label ?? `scene-${index}.jpg`} (1920×1080)
      </span>
    </div>
  );
};
