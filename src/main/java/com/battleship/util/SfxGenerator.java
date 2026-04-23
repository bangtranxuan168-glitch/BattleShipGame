package com.battleship.util;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

public class SfxGenerator {
    static int sampleRate = 44100;
    
    public static void main(String[] args) throws Exception {
        File dir = new File("src/main/resources/ui/sfx");
        if (!dir.exists()) dir.mkdirs();
        
        saveWav("click.wav", generateBeep(1000, 50));
        saveWav("hit.wav", generateNoise(200, 0.5));
        saveWav("miss.wav", generateSweep(600, 200, 300));
        saveWav("sunk.wav", generateNoise(500, 0.8));
        saveWav("win.wav", generateArpeggio(new double[]{523.25, 659.25, 783.99, 1046.50}, 150));
        saveWav("lose.wav", generateArpeggio(new double[]{261.63, 246.94, 233.08, 220.00}, 250));
    }
    
    static byte[] generateBeep(double freq, int ms) {
        int length = sampleRate * ms / 1000;
        byte[] buffer = new byte[length];
        for (int i = 0; i < length; i++) {
            double angle = i / ((double)sampleRate / freq) * 2.0 * Math.PI;
            buffer[i] = (byte)(Math.sin(angle) * 127.0);
        }
        return buffer;
    }
    
    static byte[] generateNoise(int ms, double volume) {
        int length = sampleRate * ms / 1000;
        byte[] buffer = new byte[length];
        Random rnd = new Random();
        for (int i = 0; i < length; i++) {
            // Apply exponential decay to noise to make it sound like explosion
            double decay = Math.pow(1.0 - (double)i / length, 2.0);
            buffer[i] = (byte)((rnd.nextInt(256) - 128) * volume * decay);
        }
        return buffer;
    }
    
    static byte[] generateSweep(double startFreq, double endFreq, int ms) {
        int length = sampleRate * ms / 1000;
        byte[] buffer = new byte[length];
        for (int i = 0; i < length; i++) {
            double t = (double)i / length;
            double freq = startFreq * (1-t) + endFreq * t;
            double angle = i / ((double)sampleRate / freq) * 2.0 * Math.PI;
            buffer[i] = (byte)(Math.sin(angle) * 127.0);
        }
        return buffer;
    }
    
    static byte[] generateArpeggio(double[] freqs, int msPerNote) {
        int notes = freqs.length;
        int noteLen = sampleRate * msPerNote / 1000;
        byte[] buffer = new byte[notes * noteLen];
        for (int n = 0; n < notes; n++) {
            for (int i = 0; i < noteLen; i++) {
                double angle = i / ((double)sampleRate / freqs[n]) * 2.0 * Math.PI;
                buffer[n * noteLen + i] = (byte)(Math.sin(angle) * 127.0);
            }
        }
        return buffer;
    }
    
    static void saveWav(String filename, byte[] data) throws Exception {
        AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, false);
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        AudioInputStream ais = new AudioInputStream(bais, format, data.length);
        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File("src/main/resources/ui/sfx/" + filename));
        System.out.println("Generated " + filename);
    }
}
