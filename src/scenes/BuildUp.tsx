import React from 'react';
import { useCurrentFrame, interpolate } from 'remotion';
import { FilmGrain } from '../effects/FilmGrain';
import { Vignette } from '../effects/Vignette';
import { Particles } from '../effects/Particles';
import { GlitchWipe } from '../transitions/GlitchWipe';
import { DynamicPan } from '../camera/DynamicPan';
import { SceneBg } from './SceneBg';

// 4 scenes cut on beats (300f each = every 5s at 60fps)
const CUT_FRAMES = [0, 300, 600, 900] as const;

export const BuildUp: React.FC = () => {
  const frame = useCurrentFrame();

  const activeScene = CUT_FRAMES.reduce<number>(
    (acc, cutFrame, i) => (frame >= cutFrame ? i : acc),
    0
  );

  const particleOpacity = interpolate(frame, [0, 1200], [0, 0.4], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
  });

  const cutStart = CUT_FRAMES[activeScene];
  const cutEnd = CUT_FRAMES[activeScene + 1] ?? 1200;
  const sceneDuration = cutEnd - cutStart;
  const direction = activeScene % 2 === 0 ? 'left' : ('right' as const);

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
      <DynamicPan
        direction={direction}
        duration={sceneDuration}
        startFrame={cutStart}
        distance={80}
      >
        <SceneBg
          index={activeScene + 1}
          label={`scene-${activeScene + 2}.jpg`}
        />
      </DynamicPan>

      {CUT_FRAMES.map((cutFrame, i) => (
        <GlitchWipe key={i} startFrame={cutFrame} duration={8} />
      ))}

      <Particles opacity={particleOpacity} />
      <Vignette intensity={0.7} />
      <FilmGrain opacity={0.09} />
    </div>
  );
};
