import { interpolate, type InterpolateOptions } from 'remotion';
import { easeOutCinematic, easeOutExpo } from './easing';

export const fadeIn = (
  frame: number,
  startFrame: number,
  duration: number
): number =>
  interpolate(frame, [startFrame, startFrame + duration], [0, 1], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
    easing: easeOutCinematic,
  });

export const fadeOut = (
  frame: number,
  startFrame: number,
  duration: number
): number =>
  interpolate(frame, [startFrame, startFrame + duration], [1, 0], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
    easing: easeOutExpo,
  });

export const lerp = (
  frame: number,
  inputRange: [number, number],
  outputRange: [number, number],
  options?: InterpolateOptions
): number =>
  interpolate(frame, inputRange, outputRange, {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
    ...options,
  });
