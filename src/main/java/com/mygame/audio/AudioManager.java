// src/com/mygame/audio/AudioManager.java
package com.mygame.audio;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/** Singleton – loads .wav clips once, keeps them in RAM, plays on demand. */
public final class AudioManager {

    private static final AudioManager INSTANCE = new AudioManager();
    public  static AudioManager get() { return INSTANCE; }

    /* ---- config ---- */
    private float masterGain = 0f;                // 0 = full volume, -80dB = mute

    /* ---- cache ---- */
    private final Map<String, Clip> cache = new HashMap<>();

    private AudioManager() { }  // singleton

    /* ------------------------------------------------------ */

    public void setVolume(double percent) {          // 0‒100 slider
        // convert [0,100] → decibels   (-80 silence … 0 full)
        masterGain = (float) (Math.log10(Math.max(0.0001, percent/100)) * 20);
        cache.values().forEach(this::applyGain);
    }
    public double getVolume() {
        return Math.pow(10, masterGain / 20.0) * 100;
    }

    public void playFx(String key) {
        Clip c = cache.computeIfAbsent(key, this::loadClip);
        if (c == null) return;                     // failed to load
        if (c.isRunning()) c.stop();
        c.setFramePosition(0);
        applyGain(c);
        c.start();
    }

    public Clip loopMusic(String key) {
        Clip c = cache.computeIfAbsent(key, this::loadClip);
        if (c == null) return null;
        applyGain(c);
        c.loop(Clip.LOOP_CONTINUOUSLY);
        return c;
    }

    /* ------------ helpers ------------ */
    private Clip loadClip(String key) {
        try {
            URL url = getClass().getResource("/snd/" + key + ".wav");
            if (url == null) throw new IOException("Missing /snd/" + key + ".wav");

            AudioInputStream in = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(in);
            return clip;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    private void applyGain(Clip clip) {
        FloatControl ctrl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        ctrl.setValue(masterGain);                 // dB
    }
}
