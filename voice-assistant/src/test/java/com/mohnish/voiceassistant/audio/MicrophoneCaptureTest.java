package com.mohnish.voiceassistant.audio;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MicrophoneCaptureTest {
    
    @Test
    public void testMicrophoneInitialization() {
        MicrophoneCapture mic = new MicrophoneCapture();
        assertDoesNotThrow(() -> mic.initialize());
        mic.stop();
    }
    
    @Test
    public void testListMicrophones() {
        assertDoesNotThrow(() -> MicrophoneCapture.listMicrophones());
    }
}