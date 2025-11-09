package com.mohnish.voiceassistant.utils;

import java.util.ArrayList;
import java.util.List;

public class PerformanceMonitor {
    private List<Long> sttTimes = new ArrayList<>();
    private List<Long> llmTimes = new ArrayList<>();
    private List<Long> ttsTimes = new ArrayList<>();
    private List<Long> totalTimes = new ArrayList<>();
    
    public void recordInteraction(long stt, long llm, long tts, long total) {
        sttTimes.add(stt);
        llmTimes.add(llm);
        ttsTimes.add(tts);
        totalTimes.add(total);
    }
    
    public void printSummary() {
        if (totalTimes.isEmpty()) {
            System.out.println("No interactions recorded yet.");
            return;
        }
        
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║      Performance Summary               ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println("Total interactions: " + totalTimes.size());
        System.out.println();
        System.out.println("Average times:");
        System.out.println("  STT: " + average(sttTimes) + "ms");
        System.out.println("  AI:  " + average(llmTimes) + "ms");
        System.out.println("  TTS: " + average(ttsTimes) + "ms");
        System.out.println("  Total: " + average(totalTimes) + "ms");
        System.out.println();
        System.out.println("Fastest total: " + min(totalTimes) + "ms");
        System.out.println("Slowest total: " + max(totalTimes) + "ms");
        System.out.println("══════════════════════════════════════════\n");
    }
    
    private long average(List<Long> list) {
        return list.stream().mapToLong(Long::longValue).sum() / list.size();
    }
    
    private long min(List<Long> list) {
        return list.stream().mapToLong(Long::longValue).min().orElse(0);
    }
    
    private long max(List<Long> list) {
        return list.stream().mapToLong(Long::longValue).max().orElse(0);
    }
}