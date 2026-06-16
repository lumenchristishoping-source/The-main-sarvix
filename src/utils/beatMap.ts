// 120 BPM at 60fps = beat every 30 frames, covering full 5400-frame (90s) timeline
export const BEATS: number[] = Array.from({ length: 180 }, (_, i) => i * 30);

export const snapToBeat = (frame: number): number =>
  BEATS.reduce((a, b) =>
    Math.abs(b - frame) < Math.abs(a - frame) ? b : a
  );

export const isBeat = (frame: number): boolean => BEATS.includes(frame);

export const beatProgress = (frame: number, beat: number): number => {
  const idx = BEATS.indexOf(beat);
  const next = BEATS[idx + 1] ?? beat + 30;
  return Math.min(1, (frame - beat) / (next - beat));
};
