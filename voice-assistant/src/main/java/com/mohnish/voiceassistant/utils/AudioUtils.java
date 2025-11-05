package com.mohnish.voiceassistant.utils;

import javax.sound.sampled.AudioFormat;

public class AudioUtils {
    
    public static AudioFormat getStandardAudioFormat() {
        float sampleRate = 16000.0F;  // 16kHz for speech
        int sampleSizeInBits = 16;    // 16-bit
        int channels = 1;              // Mono
        boolean signed = true;
        boolean bigEndian = false;
        
        return new AudioFormat(
            sampleRate,
            sampleSizeInBits,
            channels,
            signed,
            bigEndian
        );
    }
    
    public static void printAudioFormat(AudioFormat format) {
        System.out.println("Audio Format:");
        System.out.println("  Sample Rate: " + format.getSampleRate() + " Hz");
        System.out.println("  Sample Size: " + format.getSampleSizeInBits() + " bits");
        System.out.println("  Channels: " + format.getChannels());
        System.out.println("  Signed: " + format.getEncoding());
        System.out.println("  Big Endian: " + format.isBigEndian());
    }
}