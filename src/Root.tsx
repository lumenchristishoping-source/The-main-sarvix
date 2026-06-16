import React from 'react';
import { registerRoot, Composition } from 'remotion';
import { AnimeEdit } from './Composition';

export const RemotionRoot: React.FC = () => {
  return (
    <>
      <Composition
        id="AnimeEdit"
        component={AnimeEdit}
        durationInFrames={5400}
        fps={60}
        width={1920}
        height={1080}
        defaultProps={{}}
      />
    </>
  );
};

registerRoot(RemotionRoot);
