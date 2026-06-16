import React from 'react';
import { useCurrentFrame } from 'remotion';
import { FilmGrain } from '../effects/FilmGrain';
import { Vignette } from '../effects/Vignette';
import { ChromaticAberration } from '../effects/ChromaticAberration';
import { ZoomBlur } from '../transitions/ZoomBlur';
import { DepthPush } from '../camera/DepthPush';
import { SceneBg } from './SceneBg';
import { lerp } from '../utils/interpolators';

const TOTAL_FRAMES = 1500;
const CUT_INTERVAL = 30; // cut every beat
const NUM_SCENES = Math.floor(TOTAL_FRAMES / CUT_INTERVAL);

export const Climax: React.FC = () => {
  const frame = useCurrentFrame();

  const activeScene = Math.floor(frame / CUT_INTERVAL);

  const chromaticOffset = lerp(frame, [0, TOTAL_FRAMES], [0, 6]);

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
      <ChromaticAberration offset={chromaticOffset}>
        <DepthPush
          duration={CUT_INTERVAL}
          startFrame={activeScene * CUT_INTERVAL}
          intensity={1.2}
        >
          <SceneBg
            index={(activeScene % 8) + 1}
            label={`scene-${(activeScene % 8) + 1}.jpg`}
          />
        </DepthPush>
      </ChromaticAberration>

      {Array.from({ length: NUM_SCENES }, (_, i) => (
        <ZoomBlur key={i} startFrame={i * CUT_INTERVAL} duration={8} />
      ))}

      <Vignette intensity={0.75} />
      <FilmGrain opacity={0.12} />
    </div>
  );
};
