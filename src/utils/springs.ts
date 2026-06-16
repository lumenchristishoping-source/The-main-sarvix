import type { SpringConfig } from 'remotion';

export const snappy: SpringConfig = {
  damping: 20,
  stiffness: 300,
  mass: 1,
  overshootClamping: false,
};

export const bouncy: SpringConfig = {
  damping: 8,
  stiffness: 180,
  mass: 1,
  overshootClamping: false,
};

export const cinematic: SpringConfig = {
  damping: 14,
  stiffness: 80,
  mass: 1,
  overshootClamping: false,
};

export const overshoot: SpringConfig = {
  damping: 10,
  stiffness: 200,
  mass: 1,
  overshootClamping: false,
};
