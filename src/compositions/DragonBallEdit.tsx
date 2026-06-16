import React from 'react';
import {
  AbsoluteFill,
  OffthreadVideo,
  Sequence,
  useCurrentFrame,
  useVideoConfig,
  interpolate,
  spring,
  staticFile,
} from 'remotion';
import { AuraOverlay } from './effects/AuraOverlay';
import { WhiteFlash } from './effects/WhiteFlash';
import { SpeedLines } from './effects/SpeedLines';
import { GlitchTransition } from './effects/GlitchTransition';
import { DBVignette } from './effects/Vignette';
import { ImpactShake } from './effects/ImpactShake';

// ─── Beat / impact map at 30fps ──────────────────────────────────────────────
// Adjust these frame numbers to match your track's beat drops
const IMPACTS = [30, 60, 90, 120, 180, 240, 300, 360, 420, 480, 540, 600, 660, 720, 810, 900, 990, 1080, 1170, 1260];
const CUT_FRAMES = [0, 90, 225, 360, 450, 585, 720, 855, 990, 1125, 1260];

// ─── Speed ramp: returns playback rate for a given frame ─────────────────────
const getPlaybackRate = (frame: number): number => {
  // Slow-mo during power-up windows
  const slowMoZones: Array<[number, number]> = [
    [75, 120],
    [345, 390],
    [570, 615],
    [840, 885],
    [1110, 1155],
  ];

  for (const [start, end] of slowMoZones) {
    if (frame >= start && frame <= end) {
      const t = (frame - start) / (end - start);
      return interpolate(t, [0, 0.2, 0.8, 1], [1, 0.3, 0.3, 1], {
        extrapolateLeft: 'clamp',
        extrapolateRight: 'clamp',
      });
    }
  }

  // Fast zones near cuts
  for (const cut of CUT_FRAMES) {
    if (frame >= cut + 5 && frame <= cut + 20) {
      return 1.8;
    }
  }

  return 1.0;
};

// ─── Which clip to show at a given frame ─────────────────────────────────────
const getActiveClip = (frame: number): number => {
  const active = CUT_FRAMES.reduce((acc, cutFrame, i) =>
    frame >= cutFrame ? i : acc, 0
  );
  return active % 2; // alternates between clip-1 and clip-2
};

// ─── Color grade filter ───────────────────────────────────────────────────────
const getColorGrade = (frame: number): string => {
  const isPowerZone = [75, 345, 570, 840, 1110].some(
    (s) => frame >= s && frame <= s + 45
  );
  const base = 'contrast(1.15) saturate(1.35) brightness(1.05)';
  return isPowerZone ? `${base} sepia(0.25)` : base;
};

// ─── Title Card ───────────────────────────────────────────────────────────────
const TitleCard: React.FC = () => {
  const frame = useCurrentFrame();

  const scale = spring({ frame, fps: 30, config: { damping: 10, stiffness: 120 }, from: 0.8, to: 1.0 });
  const opacity = interpolate(frame, [0, 15, 75, 90], [0, 1, 1, 0], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
  });

  return (
    <div
      style={{
        position: 'absolute',
        inset: 0,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        zIndex: 50,
        opacity,
      }}
    >
      <div
        style={{
          transform: `scale(${scale})`,
          color: 'white',
          fontSize: 80,
          fontFamily: '"Bebas Neue", "Black Ops One", Impact, sans-serif',
          fontWeight: 900,
          letterSpacing: '0.06em',
          textAlign: 'center',
          textShadow: '0 0 30px rgba(255,200,0,0.9), 0 0 60px rgba(255,150,0,0.6), 0 4px 20px rgba(0,0,0,0.8)',
          lineHeight: 1.1,
        }}
      >
        DRAGON BALL
        <br />
        <span style={{ fontSize: 48, color: 'rgba(255,200,0,0.9)', letterSpacing: '0.2em' }}>
          EDIT
        </span>
      </div>
    </div>
  );
};

// ─── Main composition ─────────────────────────────────────────────────────────
export const DragonBallEdit: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps, durationInFrames } = useVideoConfig();

  const playbackRate = getPlaybackRate(frame);
  const activeClip = getActiveClip(frame);
  const colorGrade = getColorGrade(frame);
  const clipSrc = staticFile(`video/clip-${activeClip + 1}.mp4`);

  // Global zoom based on playback rate (slow-mo = zoom in)
  const globalScale = interpolate(playbackRate, [0.3, 1.0, 1.8], [1.12, 1.0, 1.04], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
  });

  return (
    <AbsoluteFill style={{ background: 'black', overflow: 'hidden' }}>

      {/* ── Base video layer ── */}
      <ImpactShake
        hitFrame={IMPACTS.find((h) => h <= frame && frame <= h + 6) ?? -999}
      >
        <div
          style={{
            width: '100%',
            height: '100%',
            transform: `scale(${globalScale})`,
            transformOrigin: '50% 50%',
            filter: colorGrade,
          }}
        >
          <OffthreadVideo
            src={clipSrc}
            playbackRate={playbackRate}
            style={{ width: '100%', height: '100%', objectFit: 'cover' }}
          />
        </div>
      </ImpactShake>

      {/* ── Vignette ── */}
      <DBVignette intensity={0.75} />

      {/* ── Aura overlays on power-up zones ── */}
      <AuraOverlay startFrame={75}  peakFrame={97}  endFrame={120} color="rgba(255,220,0," />
      <AuraOverlay startFrame={345} peakFrame={367} endFrame={390} color="rgba(255,220,0," />
      <AuraOverlay startFrame={570} peakFrame={592} endFrame={615} color="rgba(80,140,255," />
      <AuraOverlay startFrame={840} peakFrame={862} endFrame={885} color="rgba(255,220,0," />
      <AuraOverlay startFrame={1110} peakFrame={1132} endFrame={1155} color="rgba(80,140,255," />

      {/* ── Speed lines on hard cuts ── */}
      {CUT_FRAMES.map((cutFrame, i) => (
        <SpeedLines key={i} triggerFrame={cutFrame} duration={8} />
      ))}

      {/* ── White flash on impacts ── */}
      {IMPACTS.map((hitFrame, i) => (
        <WhiteFlash key={i} hitFrame={hitFrame} holdFrames={2} fadeFrames={5} />
      ))}

      {/* ── Glitch transitions on cuts ── */}
      {CUT_FRAMES.slice(1).map((cutFrame, i) => (
        <GlitchTransition key={i} triggerFrame={cutFrame} duration={5} />
      ))}

      {/* ── Title card: first 90 frames (3 seconds) ── */}
      <Sequence from={0} durationInFrames={90}>
        <TitleCard />
      </Sequence>

    </AbsoluteFill>
  );
};
