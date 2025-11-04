package com.mohnish.voiceassistant;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for Voice Assistant
 */
public class AppTest {
    
    @Test
    public void testAppInitialization() {
        // Basic test to verify project compiles
        assertTrue(true, "Project setup successful");
    }
    
    @Test
    public void testEnvironmentVariable() {
        // Test that we can read environment variables
        String testVar = System.getenv("PATH");
        assertNotNull(testVar, "Should be able to read environment variables");
    }
}