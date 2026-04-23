package com.battleship.util;

import javafx.scene.media.AudioClip;

/**
 * JavaFX sound manager.
 */
public class SoundManager {
    private boolean soundEnabled = true;
    private final AudioClip click = load("/ui/sfx/click.wav");
    private final AudioClip hit = load("/ui/sfx/hit.wav");
    private final AudioClip miss = load("/ui/sfx/miss.wav");
    private final AudioClip sunk = load("/ui/sfx/sunk.wav");
    private final AudioClip win = load("/ui/sfx/win.wav");
    private final AudioClip lose = load("/ui/sfx/lose.wav");

    private AudioClip load(String path) {
        try {
            var url = getClass().getResource(path);
            return url == null ? null : new AudioClip(url.toExternalForm());
        } catch (Exception e) {
            return null;
        }
    }

    private void play(AudioClip clip) { if (soundEnabled && clip != null) clip.play(); }
    public void playClick() { play(click); }
    public void playHit() { play(hit); }
    public void playMiss() { play(miss); }
    public void playSunk() { play(sunk); }
    public void playWin() { play(win); }
    public void playLose() { play(lose); }
    public void toggleSound() { soundEnabled = !soundEnabled; }
    public boolean isSoundEnabled() { return soundEnabled; }
}
