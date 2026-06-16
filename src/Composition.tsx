import React from 'react';
import { AbsoluteFill, Sequence } from 'remotion';
import { Intro } from './scenes/Intro';
import { BuildUp } from './scenes/BuildUp';
import { Emotional } from './scenes/Emotional';
import { Climax } from './scenes/Climax';
import { Outro } from './scenes/Outro';

export const AnimeEdit: React.FC = () => {
  return (
    <AbsoluteFill style={{ background: 'black' }}>
      {/* Intro: 0–600f (0–10s) */}
      <Sequence from={0} durationInFrames={600}>
        <Intro />
      </Sequence>

      {/* Build-Up: 600–1800f (10–30s) */}
      <Sequence from={600} durationInFrames={1200}>
        <BuildUp />
      </Sequence>

      {/* Emotional: 1800–3000f (30–50s) */}
      <Sequence from={1800} durationInFrames={1200}>
        <Emotional />
      </Sequence>

      {/* Climax: 3000–4500f (50–75s) */}
      <Sequence from={3000} durationInFrames={1500}>
        <Climax />
      </Sequence>

      {/* Outro: 4500–5400f (75–90s) */}
      <Sequence from={4500} durationInFrames={900}>
        <Outro />
      </Sequence>
    </AbsoluteFill>
  );
};
