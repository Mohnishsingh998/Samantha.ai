# 🤖 Samantha.ai – AI Voice Assistant

**An intelligent voice-activated assistant built from scratch in one week.**

![Status](https://img.shields.io/badge/status-production--ready-green)
![Java](https://img.shields.io/badge/java-17-orange)
![License](https://img.shields.io/badge/license-MIT-blue)

---

## 🎯 Overview

A fully functional AI-powered voice assistant that can answer any question using natural voice interaction. Built with Java, it features dual AI providers (cloud and local), automatic fallback mechanisms, and a complete voice interface.

### ✨ Key Features

- 🎤 **Natural Voice Input** – Speak your questions naturally  
- 🤖 **Intelligent AI** – Powered by Groq (cloud) and Ollama (local)  
- 🔊 **Voice Output** – Natural-sounding text-to-speech responses  
- 🔄 **Smart Routing** – Automatic fallback when offline  
- ⚙️ **Configurable** – Easy customization via config file  
- 📊 **Performance Monitoring** – Track response times and usage  
- 🛡️ **Error Recovery** – Graceful handling with helpful messages  

---

## 🚀 Quick Start

### Prerequisites
- macOS (tested on macOS 13+)  
- Java 17+  
- Python 3 with `SpeechRecognition` library  
- Groq API key ([get free key](https://console.groq.com))

### Installation
```bash
# 1. Clone the repository
git clone https://github.com/Mohnishsingh998/Samantha.ai.git
cd Samantha.ai

# 2. Install Python dependency
pip3 install SpeechRecognition

# 3. Install Ollama (optional, for offline mode)
brew install ollama
ollama pull llama3.2:3b

# 4. Set your Groq API key
export GROQ_API_KEY='your-key-here'
echo 'export GROQ_API_KEY="your-key"' >> ~/.zshrc

# 5. Make scripts executable
chmod +x scripts/*.sh

# 6. Start the assistant!
./scripts/start-assistant.sh
```

---

## 💡 Usage

### Basic Usage
```bash
# Start the assistant
./scripts/start-assistant.sh

# Press ENTER when prompted
# Speak your question
# Listen to the answer
```

### Commands
Type these commands in the assistant:
- `local` - Switch to offline mode (Ollama)
- `cloud` - Switch to online mode (Groq)
- `stats` - Show usage statistics
- `config` - Display current configuration
- `help` - Show help menu
- `quit` - Exit assistant

### Configuration
Edit `config/assistant.properties` to customize:
```properties
tts.voice=Samantha        # Voice selection
tts.rate=200              # Speech rate (wpm)
llm.primary=groq          # Default AI provider
llm.max.tokens=150        # Response length
```

---

## 🏗️ Architecture
```
┌─────────────────────────────────────────┐
│          Voice Assistant                │
├─────────────────────────────────────────┤
│  Voice Input                            │
│  ↓                                       │
│  Speech-to-Text (Google SR)             │
│  ↓                                       │
│  AI Brain (Groq/Ollama)                 │
│  • Smart routing                        │
│  • Automatic fallback                   │
│  ↓                                       │
│  Text-to-Speech (macOS)                 │
│  ↓                                       │
│  Voice Output                           │
└─────────────────────────────────────────┘
```

### Technology Stack
- **Language:** Java 17
- **Build Tool:** Maven 3.9+
- **AI Providers:**
  - Groq (llama-3.3-70b-versatile) - Fast cloud AI
  - Ollama (llama3.2:3b) - Local offline AI
- **Speech Recognition:** Google Speech Recognition API
- **Text-to-Speech:** macOS native `say` command
- **Libraries:** Gson, SLF4J, Logback, Commons Lang3

---

## 📊 Performance

**Average Response Times:**
- Speech-to-Text: ~1-2 seconds
- AI Processing (Groq): ~1-2 seconds
- AI Processing (Ollama): ~4-6 seconds
- Text-to-Speech: ~0.5-1 second
- **Total (Groq):** ~3-5 seconds ✅
- **Total (Ollama):** ~6-9 seconds ✅

**Speech Recognition Accuracy:** ~85-90% in quiet environments

---

## 📁 Project Structure
```
voice-assistant/
├── src/main/java/com/mohnish/voiceassistant/
│   ├── VoiceAssistant.java       # Main application
│   ├── audio/                     # Voice I/O components
│   ├── llm/                       # AI integration
│   └── utils/                     # Utilities
├── config/                        # Configuration
├── scripts/                       # Launcher scripts
├── docs/                          # Documentation
└── models/                        # Speech models
```

---

## 🧪 Testing
```bash
# Test microphone
mvn exec:java -Dexec.mainClass="com.mohnish.voiceassistant.audio.MicrophoneTest"

# Test speech recognition
mvn exec:java -Dexec.mainClass="com.mohnish.voiceassistant.audio.MacOSSTTTest"

# Test AI integration
mvn exec:java -Dexec.mainClass="com.mohnish.voiceassistant.llm.LLMTest"

# Run full suite
mvn test
```
## 🛠️ Troubleshooting

### Common Issues

**"No speech detected"**
- Check microphone permissions in System Settings
- Ensure microphone is not being used by another app
- Speak clearly at normal volume

**"Groq API error"**
- Verify API key: `echo $GROQ_API_KEY`
- Check internet connection
- Switch to local mode: type `local`

**"Ollama not running"**
- Start Ollama: `ollama serve` in separate terminal
- Or use cloud mode exclusively

See [User Guide](docs/USER_GUIDE.md) for more troubleshooting.

---

## 🗺️ Roadmap

### Completed (Week 1) ✅
- [x] Voice input/output
- [x] Speech recognition
- [x] AI integration (dual providers)
- [x] Smart routing & fallback
- [x] Configuration system
- [x] Error handling
- [x] Documentation

### Future Enhancements (Week 2+)
- [ ] Wake word detection ("Hey Assistant")
- [ ] RAG integration (answer from personal documents)
- [ ] Conversation memory
- [ ] Multiple language support
- [ ] Web dashboard
- [ ] Mobile app integration

---

## 📝 Development Journey

**Built in 7 days:**
- Day 1: Environment setup
- Day 2: Microphone capture
- Day 3: Speech-to-text
- Day 4: Text-to-speech
- Day 5: AI integration
- Day 6: Polish & configuration
- Day 7: Documentation & demo

**Total Lines of Code:** ~2,500+
**Time Invested:** ~28 hours

---

## 🤝 Contributing

This is a personal learning project, but suggestions are welcome!

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

---

---

## 👤 Author

**Mohnish Singh Yadav**
- GitHub: [@Mohnishsingh998]((https://github.com/Mohnishsingh998))
- LinkedIn: [mohnishsingh](https://www.linkedin.com/in/mohnishsingh-yadav-86916b257/)

---

## 🙏 Acknowledgments

- [Groq](https://groq.com) for fast AI inference
- [Ollama](https://ollama.ai) for local AI capabilities
- Google Speech Recognition API
- macOS for excellent TTS
- All open-source contributors

---

## ⭐ Star this repository if you found it helpful!

---
---

**Questions? Open an issue or reach out!**
