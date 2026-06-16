import React from 'react';
import { useCurrentFrame, spring, interpolate } from 'remotion';
import { FilmGrain } from '../effects/FilmGrain';
import { Vignette } from '../effects/Vignette';
import { GlowPulse } from '../effects/GlowPulse';
import { FlashCut } from '../transitions/FlashCut';
import { FadeBlack } from '../transitions/FadeBlack';
import { cinematic } from '../utils/springs';
import { lerp } from '../utils/interpolators';

const TOTAL_FRAMES = 900;

export const Outro: React.FC = () => {
  const frame = useCurrentFrame();

  const logoScale = spring({
    frame: Math.max(0, frame - 10),
    fps: 60,
    config: cinematic,
    from: 0.8,
    to: 1.0,
  });

  const logoOpacity = interpolate(frame, [10, 50], [0, 1], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
  });

  const grainOpacity = lerp(frame, [0, TOTAL_FRAMES], [0.08, 0.18]);
  const vignetteIntensity = lerp(frame, [0, TOTAL_FRAMES], [0.85, 0.95]);

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
        <GlowPulse color="rgba(255,200,100,0.7)" triggerFrame={10}>
          <div
            style={{
              transform: `scale(${logoScale})`,
              opacity: logoOpacity,
              color: 'white',
              fontSize: 72,
              fontFamily: 'serif',
              fontWeight: 'bold',
              letterSpacing: '0.12em',
              textShadow: '0 0 60px rgba(255,200,100,0.8)',
              textAlign: 'center',
            }}
          >
            THE END
          </div>
        </GlowPulse>
        <div
          style={{
            opacity: logoOpacity * 0.6,
            color: 'rgba(255,200,100,0.6)',
            fontSize: 18,
            fontFamily: 'serif',
            letterSpacing: '0.3em',
            marginTop: 24,
          }}
        >
          INSERT: studio / series name
        </div>
      </div>

      {/* Flash cut at start of outro */}
      <FlashCut startFrame={0} duration={4} />
      {/* Fade to black in final 60 frames */}
      <FadeBlack startFrame={TOTAL_FRAMES - 60} duration={60} direction="out" />

      <Vignette intensity={vignetteIntensity} />
      <FilmGrain opacity={grainOpacity} />
    </div>
  );
};
