import React from 'react';
import { useCurrentFrame } from 'remotion';
import { FilmGrain } from '../effects/FilmGrain';
import { Vignette } from '../effects/Vignette';
import { LightLeak } from '../effects/LightLeak';
import { GlowPulse } from '../effects/GlowPulse';
import { Parallax } from '../camera/Parallax';
import { SceneBg } from './SceneBg';

// Slower cuts every 120f (2s)
const CUT_FRAMES = [0, 120, 240, 360, 480, 600, 720, 840, 960, 1080] as const;

export const Emotional: React.FC = () => {
  const frame = useCurrentFrame();

  const activeScene = CUT_FRAMES.reduce<number>(
    (acc, cutFrame, i) => (frame >= cutFrame ? i : acc),
    0
  );

  const cutStart = CUT_FRAMES[activeScene];
  const cutEnd = CUT_FRAMES[activeScene + 1] ?? 1200;
  const sceneDuration = cutEnd - cutStart;

  return (
    <div
      style={{
        width: '100%',
        height: '100%',
        position: 'relative',
        overflow: 'hidden',
        background: 'black',
      }}
    >
      <Parallax
        speed={0.3}
        duration={sceneDuration}
        startFrame={cutStart}
        distance={60}
      >
        <SceneBg
          index={(activeScene % 4) + 3}
          label={`scene-${(activeScene % 4) + 4}.jpg`}
        />
      </Parallax>

      <GlowPulse color="rgba(255,180,100,0.4)" triggerFrame={cutStart}>
        {/* Subject silhouette placeholder */}
        <div
          style={{
            position: 'absolute',
            bottom: '20%',
            left: '50%',
            transform: 'translateX(-50%)',
            width: 200,
            height: 300,
            background: 'rgba(255,255,255,0.05)',
            borderRadius: 4,
          }}
        />
      </GlowPulse>

      <LightLeak />
      <Vignette intensity={0.9} />
      <FilmGrain opacity={0.1} />
    </div>
  );
};
