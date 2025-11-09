# 🎤 Voice Assistant User Guide

## Quick Start

### First Time Setup
```bash
# 1. Set your Groq API key
export GROQ_API_KEY='your-key-here'

# 2. Start the assistant
./scripts/start-assistant.sh
```

### Daily Use
```bash
# Just run the startup script
./scripts/start-assistant.sh
```

## How to Use

### Asking Questions
1. Press **ENTER**
2. Wait for "I'm listening"
3. Speak your question clearly
4. Wait for the answer

### Example Questions
- "What is artificial intelligence?"
- "Explain quantum physics"
- "Who was Albert Einstein?"
- "What is photosynthesis?"
- "Tell me about machine learning"

### Commands
Type these commands instead of pressing ENTER:

- `local` - Use offline mode (Ollama)
- `cloud` - Use online mode (Groq, faster)
- `stats` - Show usage statistics
- `config` - Show current settings
- `help` - Show help menu
- `quit` - Exit assistant

## Tips for Best Results

### Speaking Tips
✅ Speak at normal conversational pace
✅ Use a quiet environment
✅ Keep microphone ~6 inches from mouth
✅ Speak clearly but naturally

❌ Don't speak too fast
❌ Don't whisper
❌ Avoid background noise

### Performance Tips
- **Slow responses?** Type `local` to use Ollama
- **Internet issues?** Automatically falls back to Ollama
- **First question slow?** Normal - model is loading

## Troubleshooting

### "No speech detected"
**Problem:** Assistant didn't hear you
**Solutions:**
- Check microphone permissions
- Speak louder
- Check microphone is working
- Close other apps using microphone

### "Groq API error"
**Problem:** Internet or API issue
**Solutions:**
- Check internet connection
- Type `local` to use offline mode
- Verify API key is set: `echo $GROQ_API_KEY`

### "Ollama not running"
**Problem:** Local AI not available
**Solutions:**
- Open new terminal
- Run: `ollama serve`
- Or just use cloud mode (default)

### Slow Performance
**Problem:** Responses take too long
**Solutions:**
- Use cloud mode (type `cloud`)
- Check internet speed
- Reduce recording duration in config
- Close other applications

## Configuration

Edit `config/assistant.properties` to customize:
```properties
# Change voice
tts.voice=Samantha  # or Alex, Victoria, Daniel

# Adjust speech rate
tts.rate=200  # 150=slow, 250=fast

# Switch default AI
llm.primary=groq  # or ollama
```

After editing, restart the assistant.

## Keyboard Shortcuts

- **ENTER** - Start voice input
- **Ctrl+C** - Stop current operation
- **Ctrl+D** - Quick exit

## FAQ

**Q: Can I use it offline?**
A: Yes! Type `local` to use Ollama. Responses will be slower but work without internet.

**Q: How accurate is speech recognition?**
A: ~85-90% in quiet environments. Accuracy improves with clear speech.

**Q: Can I change the voice?**
A: Yes! Edit `tts.voice` in config file. Run `say -v ?` to see all voices.

**Q: Is my data private?**
A: Voice processing is local. Only text goes to Groq API. Use `local` mode for 100% privacy.

**Q: How many questions can I ask?**
A: Unlimited! Groq free tier allows 14,400 requests/day.

## Advanced Usage

### Custom Recording Duration
```properties
# In config/assistant.properties
audio.recording.duration=7  # Record for 7 seconds
```

### Performance Monitoring
Type `perf` to see detailed statistics.

### Debug Mode
```properties
debug.mode=true
log.level=DEBUG
```

## Getting Help

- Type `help` in the assistant
- Check logs in `logs/` directory
- Review error messages - they include solutions!

## Updates & Maintenance

### Update Models
```bash
# Update Ollama model
ollama pull llama3.2:3b

# Models auto-update on Groq side
```

### Clear Cache
```bash
# If having issues, try:
mvn clean compile
```

---

**Enjoy your AI Voice Assistant!** 🎉