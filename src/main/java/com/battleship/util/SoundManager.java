package com.battleship.util;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SoundManager.java
 * Handles all game audio using programmatically generated sounds.
 * No external audio files required – tones are synthesized at runtime.
 */
public class SoundManager {

    private boolean soundEnabled = true;

    // Thread pool so sounds don't block the EDT
    private final ExecutorService executor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "sound-thread");
        t.setDaemon(true);
        return t;
    });

    // ─── Public API ──────────────────────────────────────────────────────────

    public void playHit()  { if (soundEnabled) executor.submit(() -> playTone(880, 180, ToneType.HIT));  }
    public void playMiss() { if (soundEnabled) executor.submit(() -> playTone(300, 220, ToneType.MISS)); }
    public void playWin()  { if (soundEnabled) executor.submit(() -> playFanfare(true));  }
    public void playLose() { if (soundEnabled) executor.submit(() -> playFanfare(false)); }
    public void playSunk() { if (soundEnabled) executor.submit(() -> playSunkSound());    }
    public void playClick(){ if (soundEnabled) executor.submit(() -> playTone(660, 80, ToneType.CLICK)); }

    public boolean isSoundEnabled()          { return soundEnabled; }
    public void setSoundEnabled(boolean on)  { this.soundEnabled = on; }
    public void toggleSound()                { soundEnabled = !soundEnabled; }

    // ─── Internal sound synthesis ────────────────────────────────────────────

    private enum ToneType { HIT, MISS, CLICK }

    /**
     * Generates and plays a simple sine-wave tone.
     * @param frequency Hz
     * @param durationMs milliseconds
     */
    private void playTone(int frequency, int durationMs, ToneType type) {
        try {
            float sampleRate = 44100f;
            int numSamples   = (int)(sampleRate * durationMs / 1000.0);
            byte[] buf       = new byte[2 * numSamples];

            for (int i = 0; i < numSamples; i++) {
                double angle = 2.0 * Math.PI * i * frequency / sampleRate;
                double envelope = getEnvelope(i, numSamples, type);
                short val = (short)(Short.MAX_VALUE * 0.5 * Math.sin(angle) * envelope);
                buf[2 * i]     = (byte)(val & 0xFF);
                buf[2 * i + 1] = (byte)((val >> 8) & 0xFF);
            }
            play(buf, sampleRate);
        } catch (Exception e) {
            // Silently ignore audio errors
        }
    }

    private double getEnvelope(int i, int total, ToneType type) {
        double t = (double) i / total;
        switch (type) {
            case HIT:   return t < 0.1 ? t / 0.1 : 1.0 - (t - 0.1) / 0.9;  // attack-decay
            case MISS:  return Math.exp(-3.0 * t);                            // exponential decay
            case CLICK: return t < 0.5 ? 1.0 : 1.0 - (t - 0.5) / 0.5;
            default:    return 1.0;
        }
    }

    /**
     * Plays a multi-note fanfare: ascending for win, descending for lose.
     */
    private void playFanfare(boolean win) {
        try {
            int[] freqs = win
                ? new int[]{523, 659, 784, 1047}   // C5, E5, G5, C6
                : new int[]{784, 659, 523, 392};    // G5, E5, C5, G4
            for (int freq : freqs) {
                playTone(freq, 180, ToneType.HIT);
                Thread.sleep(200);
            }
        } catch (InterruptedException ignored) {}
    }

    /**
     * Layered explosion-like sound for ship sunk event.
     */
    private void playSunkSound() {
        try {
            // Low rumble + high crack
            playTone(120, 400, ToneType.MISS);
            Thread.sleep(100);
            playTone(800, 150, ToneType.HIT);
        } catch (InterruptedException ignored) {}
    }

    private void play(byte[] audioData, float sampleRate) throws Exception {
        AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) return;

        try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
            line.open(format);
            line.start();
            line.write(audioData, 0, audioData.length);
            line.drain();
        }
    }
}
