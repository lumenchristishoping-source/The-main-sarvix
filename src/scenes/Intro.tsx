import React from 'react';
import { useCurrentFrame, spring, interpolate } from 'remotion';
import { FilmGrain } from '../effects/FilmGrain';
import { Vignette } from '../effects/Vignette';
import { SlowZoom } from '../camera/SlowZoom';
import { cinematic } from '../utils/springs';
import { SceneBg } from './SceneBg';

export const Intro: React.FC = () => {
  const frame = useCurrentFrame();

  const titleScale = spring({
    frame,
    fps: 60,
    config: cinematic,
    from: 0.8,
    to: 1.0,
  });

  const titleOpacity = interpolate(frame, [0, 40], [0, 1], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
  });

  const subtitleOpacity = interpolate(frame, [60, 120], [0, 1], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
  });

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
      <SlowZoom duration={600} startScale={1.0} endScale={1.06}>
        <SceneBg index={0} label="scene-1.jpg (Opening background)" />
      </SlowZoom>

      <div
        style={{
          position: 'absolute',
          inset: 0,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 10,
        }}
      >
        <div
          style={{
            transform: `scale(${titleScale})`,
            opacity: titleOpacity,
            color: 'white',
            fontSize: 96,
            fontFamily: 'serif',
            fontWeight: 'bold',
            letterSpacing: '0.08em',
            textShadow: '0 0 40px rgba(255,200,100,0.6)',
            textAlign: 'center',
          }}
        >
          ANIME EDIT
        </div>
        <div
          style={{
            opacity: subtitleOpacity,
            color: 'rgba(255,200,100,0.8)',
            fontSize: 24,
            fontFamily: 'serif',
            letterSpacing: '0.25em',
            marginTop: 16,
          }}
        >
          INSERT: anime-title.ttf — replace with series title
        </div>
      </div>

      <Vignette intensity={0.85} />
      <FilmGrain opacity={0.08} />
    </div>
  );
};
