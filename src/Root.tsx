import React from 'react';
import { registerRoot, Composition } from 'remotion';
import { AnimeEdit } from './Composition';
import { DragonBallEdit } from './compositions/DragonBallEdit';

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
      <Composition
        id="DragonBallEdit"
        component={DragonBallEdit}
        durationInFrames={1350}
        fps={30}
        width={1920}
        height={1080}
        defaultProps={{}}
      />
    </>
  );
};

registerRoot(RemotionRoot);
