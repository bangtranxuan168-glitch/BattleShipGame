package com.battleship.util;

import javafx.scene.media.AudioClip;

/**
 * JavaFX sound manager.
 */
public class SoundManager {
    private boolean soundEnabled = true;
    private final AudioClip click = load("/ui/sfx/click.mp3");
    private final AudioClip hit = load("/ui/sfx/hit.mp3");
    private final AudioClip miss = load("/ui/sfx/miss.mp3");
    private final AudioClip sunk = load("/ui/sfx/sunk.mp3");
    private final AudioClip win = load("/ui/sfx/win.mp3");
    private final AudioClip lose = load("/ui/sfx/lose.mp3");

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
