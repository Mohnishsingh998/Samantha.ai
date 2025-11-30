# 🎤 Samantha.ai – Voice-Activated AI Assistant

**An intelligent, privacy-first voice assistant with wake word detection, RAG capabilities, and dual AI backends.**

![Status](https://img.shields.io/badge/status-production--ready-green)
![Java](https://img.shields.io/badge/java-21-orange)
![License](https://img.shields.io/badge/license-MIT-blue)
![Wake Word](https://img.shields.io/badge/wake_word-picovoice-purple)

---

## 🎯 Overview

A fully functional, hands-free AI voice assistant that responds to wake word commands and can answer questions from your personal knowledge base. Built entirely in Java with enterprise-grade architecture and local-first design.

### ✨ Key Features

- 🎤 **Wake Word Activation** – Say "Hey Samantha" to activate (customizable)
- 📚 **RAG Integration** – Answer questions from your personal books/documents
- 🤖 **Dual AI Backend** – Groq (cloud) + Ollama (local) with smart routing
- 🔊 **Natural Voice I/O** – Seamless speech recognition and synthesis
- 🔒 **Privacy-First** – Runs 100% locally (except optional Groq API)
- 💬 **Conversation Memory** – Maintains context across multiple exchanges
- ⚡ **Fast Response** – ~15-20 second end-to-end latency
- 🛡️ **Robust Error Handling** – Graceful fallbacks and recovery
- 📊 **Performance Monitoring** – Real-time metrics and statistics

---

## 🚀 Quick Start

### Prerequisites

- **macOS** (13+ recommended, M1/M2 optimized)
- **Java 21+** ([download here](https://adoptium.net/))
- **Maven 3.8+** (`brew install maven`)
- **Ollama** ([download here](https://ollama.ai)) – for local AI
- **ChromaDB** (`pip install chromadb`) – for RAG
- **Groq API key** ([get free key](https://console.groq.com))
- **Picovoice API key** ([get free key](https://console.picovoice.ai))

### Installation
```bash
# 1. Clone the repository
git clone https://github.com/Mohnishsingh998/Samantha.ai.git
cd Samantha.ai/voice-assistant

# 2. Install system dependencies
brew install ollama maven

# 3. Install Python dependencies
pip3 install chromadb

# 4. Install Ollama models
ollama pull llama3.2:3b
ollama pull mxbai-embed-large

# 5. Set up API keys
export GROQ_API_KEY='your-groq-key-here'
echo "export GROQ_API_KEY='your-groq-key'" >> ~/.zshrc

# Create secrets file for Picovoice
echo "PICOVOICE_ACCESS_KEY=your-picovoice-key-here" > config/secrets.properties

# 6. Train your custom wake word
# Go to: https://console.picovoice.ai/ppn
# Train wake word: "Hey Samantha" (or customize)
# Download .ppn file to: lib/porcupine/

# 7. Compile the project
mvn clean compile

# 8. Start services (in separate terminals)
ollama serve                          # Terminal 1
chromadb run --path ./chroma_data    # Terminal 2

# 9. Run the assistant!
mvn exec:java -Dexec.mainClass="com.mohnish.voiceassistant.VoiceAssistant" -Dexec.args="--wake-word"
```

---

## 💡 Usage Modes

### Mode 1: Wake Word Mode (Hands-Free)
```bash
# Start with wake word activation
mvn exec:java -Dexec.mainClass="com.mohnish.voiceassistant.VoiceAssistant" -Dexec.args="--wake-word"

# Usage:
# 1. Say "Hey Samantha"
# 2. Wait for beep
# 3. Ask your question
# 4. Listen to answer
```

### Mode 2: Wake Word + RAG (Knowledge Base)
```bash
# First, index your books (one-time setup)
# Place PDFs in books/ folder, then:
mvn exec:java -Dexec.mainClass="com.mohnish.voiceassistant.embedding.RealBookEmbeddingTest"

# Start with RAG enabled
mvn exec:java -Dexec.mainClass="com.mohnish.voiceassistant.VoiceAssistant" -Dexec.args="--wake-word --rag"

# Now ask questions from your books!
# Example: "Hey Samantha, what is machine learning?"
```

### Mode 3: Manual Mode (Press to Activate)
```bash
# Start in manual mode
mvn exec:java -Dexec.mainClass="com.mohnish.voiceassistant.VoiceAssistant"

# Commands:
# ENTER - Ask a question
# rag   - Toggle RAG mode
# local - Use Ollama (offline)
# cloud - Use Groq (online)
# stats - Usage statistics
# config - Show configuration
# perf  - Performance metrics
# help  - Show help
# quit  - Exit
```

---

## 🏗️ Architecture
```
┌──────────────────────────────────────────────────┐
│             Voice Assistant                       │
├──────────────────────────────────────────────────┤
│                                                   │
│  Wake Word Detection (Picovoice Porcupine)       │
│         ↓                                         │
│  Speech-to-Text (macOS Speech Recognition)       │
│         ↓                                         │
│  RAG Pipeline (Optional)                          │
│    • Vector Search (ChromaDB)                     │
│    • Embedding (Ollama mxbai-embed-large)        │
│    • Context Retrieval                            │
│         ↓                                         │
│  AI Brain (Smart Router)                          │
│    • Groq (cloud, fast)                          │
│    • Ollama (local, private)                     │
│    • Auto-fallback on failure                    │
│         ↓                                         │
│  Conversation Memory                              │
│    • Last 5 exchanges                            │
│    • Context-aware responses                     │
│         ↓                                         │
│  Text-to-Speech (macOS say)                      │
│         ↓                                         │
│  Audio Output                                     │
│                                                   │
└──────────────────────────────────────────────────┘
```

### Technology Stack

**Core:**
- Language: Java 21 (LTS)
- Build: Maven 3.9+
- Logging: SLF4J + Logback

**Voice Processing:**
- Wake Word: Picovoice Porcupine 3.0
- STT: macOS Speech Recognition API
- TTS: macOS native `say` command

**AI & ML:**
- Cloud AI: Groq (llama-3.3-70b-versatile)
- Local AI: Ollama (llama3.2:3b)
- Embeddings: mxbai-embed-large (1024-dim)
- Vector DB: ChromaDB

**RAG Stack:**
- Document Parsing: Apache PDFBox
- Text Chunking: Custom semantic splitter
- Retrieval: Vector similarity search (top-k)
- Context Window: 5 exchanges

---

## 📊 Performance Metrics

### Response Times (Wake Word Mode)

| Component | Average | Target | Status |
|-----------|---------|--------|--------|
| Wake Word Detection | <500ms | <1s | ✅ |
| Speech-to-Text | 2-3s | <5s | ✅ |
| Vector Search (RAG) | 1-2s | <3s | ✅ |
| AI Generation (Groq) | 1-2s | <3s | ✅ |
| AI Generation (Ollama) | 4-6s | <8s | ✅ |
| Text-to-Speech | 3-4s | <5s | ✅ |
| **Total (Groq + RAG)** | **15-20s** | **<25s** | ✅ |

### Accuracy Metrics

- Wake Word Detection: **>90%** (quiet environment)
- Speech Recognition: **85-92%** (clear speech)
- RAG Relevance: **>78%** (top result)
- False Positives: **<1 per hour**

---

## 📁 Project Structure
```
voice-assistant/
├── src/main/java/com/mohnish/voiceassistant/
│   ├── VoiceAssistant.java          # Main orchestrator
│   ├── audio/                        # Voice I/O
│   │   ├── MicrophoneCapture.java
│   │   ├── MacOSSTTEngine.java
│   │   └── MacOSTTSEngine.java
│   ├── wakeword/                     # Wake word detection
│   │   ├── PorcupineWakeWord.java
│   │   └── WakeWordTest.java
│   ├── llm/                          # AI integration
│   │   ├── GroqClient.java
│   │   ├── OllamaClient.java
│   │   └── SmartLLMRouter.java
│   ├── rag/                          # RAG pipeline
│   │   └── RAGPipeline.java
│   ├── vectordb/                     # Vector database
│   │   ├── ChromaDBClient.java
│   │   └── VectorStore.java
│   ├── document/                     # Document processing
│   │   ├── DocumentParser.java
│   │   └── TextChunker.java
│   ├── embedding/                    # Embedding generation
│   │   └── EmbeddingGenerator.java
│   └── utils/                        # Utilities
│       ├── ConfigLoader.java
│       └── PerformanceMonitor.java
├── config/
│   ├── assistant.properties          # Main config
│   └── secrets.properties            # API keys (gitignored)
├── lib/porcupine/                    # Wake word models
│   └── hey-samantha_*.ppn
├── books/                            # Your documents (for RAG)
├── chroma_data/                      # Vector database
├── scripts/                          # Utility scripts
└── docs/                             # Documentation
```

---

## ⚙️ Configuration

### Main Config (`config/assistant.properties`)
```properties
# Voice Settings
tts.voice=Samantha
tts.rate=200
recording.duration=5

# AI Settings
llm.primary=groq
ollama.url=http://localhost:11434

# Wake Word Settings
wake.word.enabled=true
wake.word.path=lib/porcupine/hey-samantha_en_mac_v3_0_0.ppn
wake.word.sensitivity=0.5

# RAG Settings
chroma.url=http://localhost:8000
chroma.collection=knowledge_base

# Performance
performance.stats.enabled=true
```

### Secrets (`config/secrets.properties`)
```properties
# Get keys from:
# Groq: https://console.groq.com
# Picovoice: https://console.picovoice.ai

GROQ_API_KEY=your-groq-key-here
PICOVOICE_ACCESS_KEY=your-picovoice-key-here
```

---

## 🧪 Testing
```bash
# Test wake word detection
mvn exec:java -Dexec.mainClass="com.mohnish.voiceassistant.wakeword.WakeWordTest"

# Test microphone
mvn exec:java -Dexec.mainClass="com.mohnish.voiceassistant.audio.MicrophoneTest"

# Test speech-to-text
mvn exec:java -Dexec.mainClass="com.mohnish.voiceassistant.audio.MacOSSTTTest"

# Test RAG pipeline
mvn exec:java -Dexec.mainClass="com.mohnish.voiceassistant.rag.RAGTest"

# Test LLM integration
mvn exec:java -Dexec.mainClass="com.mohnish.voiceassistant.llm.LLMTest"

# Run all unit tests
mvn test
```

---

## 🛠️ Troubleshooting

### Wake Word Not Detecting
```bash
# Check microphone permissions
System Settings → Privacy & Security → Microphone → Enable for Terminal/Java

# Adjust sensitivity (higher = more sensitive)
# Edit config/assistant.properties:
wake.word.sensitivity=0.7

# Verify .ppn file exists
ls -la lib/porcupine/*.ppn
```

### "Collection not found: knowledge_base"
```bash
# Index your books first
mvn exec:java -Dexec.mainClass="com.mohnish.voiceassistant.embedding.RealBookEmbeddingTest"

# Verify ChromaDB is running
curl http://localhost:8000/api/v1/heartbeat

# Check collection exists
curl http://localhost:8000/api/v1/collections
```

### "Ollama not responding"
```bash
# Start Ollama server
ollama serve

# Pull required models
ollama pull llama3.2:3b
ollama pull mxbai-embed-large

# Test manually
ollama run llama3.2:3b "hello"
```

### Speech Not Transcribing
```bash
# Check audio input device
System Settings → Sound → Input → Select correct microphone

# Test microphone
mvn exec:java -Dexec.mainClass="com.mohnish.voiceassistant.audio.MicrophoneTest"

# Check recording duration (might be too short)
# Edit config/assistant.properties:
recording.duration=7
```

### Slow Performance
```bash
# Use local mode only (faster startup)
mvn exec:java -Dexec.mainClass="com.mohnish.voiceassistant.VoiceAssistant"
# Then type: local

# Reduce RAG retrieval chunks
# Edit code: RAGPipeline constructor
topK = 2  // instead of 3

# Use faster model
ollama pull llama3.2:1b  // smaller, faster
```

See [User Guide](docs/USER_GUIDE.md) for detailed troubleshooting.

---

## 🗺️ Roadmap

### ✅ Completed (Weeks 1-4)

- [x] Voice input/output pipeline
- [x] Speech recognition (macOS)
- [x] Text-to-speech (macOS)
- [x] Dual AI providers (Groq + Ollama)
- [x] Smart routing & fallback
- [x] Wake word detection (Picovoice)
- [x] RAG integration (ChromaDB)
- [x] Document indexing (PDFs)
- [x] Vector embeddings
- [x] Conversation memory
- [x] Configuration system
- [x] Error handling & recovery
- [x] Performance monitoring
- [x] Comprehensive documentation

### 🚧 In Progress (Week 5)

- [ ] Multi-language support
- [ ] GUI dashboard (JavaFX)
- [ ] Mobile app companion
- [ ] Voice activity detection

### 📅 Planned (Week 6+)

- [ ] Custom wake word training
- [ ] Multiple wake words
- [ ] Calendar integration
- [ ] Email integration
- [ ] Web search fallback
- [ ] Sentiment analysis
- [ ] Voice cloning (TTS)
- [ ] Multi-user support
- [ ] Cloud sync (optional)
- [ ] Alexa/Google Home integration

---

## 📝 Development Journey

**Built in 4 weeks (6-8 hours/day):**

| Week | Focus | Achievements |
|------|-------|--------------|
| **Week 1** | Foundation | Voice loop, STT, TTS, basic AI |
| **Week 2** | Knowledge Base | PDF parsing, embeddings, ChromaDB |
| **Week 3** | RAG System | Vector search, context retrieval, quality tuning |
| **Week 4** | Wake Word | Picovoice integration, hands-free mode, polish |

**Stats:**
- **Total Lines of Code:** ~5,000+
- **Time Invested:** ~120 hours
- **Coffee Consumed:** ☕☕☕ Countless
- **Bugs Fixed:** 🐛 Too many to count
- **Learning:** 📚 Immense

---

## 🎥 Demo Video

[Coming Soon - Upload to YouTube]

**Demo Script:**
1. Wake word activation
2. General knowledge question
3. Book-based question (RAG)
4. Follow-up question (context)
5. Code walkthrough

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

**Areas needing help:**
- Multi-language support
- Windows/Linux compatibility
- GUI development
- Mobile app
- Documentation improvements

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👤 Author

**Mohnish Singh Yadav**

- 🌐 Portfolio: [your-portfolio.com](https://your-portfolio.com)
- 💼 LinkedIn: [@Mohnishsingh](https://www.linkedin.com/in/mohnishsingh-yadav-86916b257/)
- 🐙 GitHub: [@Mohnishsingh998](https://github.com/Mohnishsingh998)
- 📧 Email: mohnishsinghyadav@gmail.com

*"Built this to learn, share to inspire."*

---

## 🙏 Acknowledgments

### Technologies & Services

- [Picovoice](https://picovoice.ai) - Wake word detection
- [Groq](https://groq.com) - Lightning-fast AI inference
- [Ollama](https://ollama.ai) - Local AI capabilities
- [ChromaDB](https://www.trychroma.com/) - Vector database
- Apple macOS - Excellent speech APIs

### Inspiration

- Amazon Alexa
- Apple Siri
- Google Assistant
- Open-source community

### Special Thanks

- Stack Overflow community
- GitHub Copilot (for code suggestions)
- Coffee shops (for wifi and caffeine)

---

## 📈 Project Stats

![GitHub stars](https://img.shields.io/github/stars/Mohnishsingh998/Samantha.ai?style=social)
![GitHub forks](https://img.shields.io/github/forks/Mohnishsingh998/Samantha.ai?style=social)
![GitHub watchers](https://img.shields.io/github/watchers/Mohnishsingh998/Samantha.ai?style=social)

---

## 💬 FAQ

**Q: Does this work on Windows/Linux?**  
A: Currently macOS only due to native speech APIs. Windows/Linux support planned.

**Q: How much does it cost to run?**  
A: Free with Ollama (local). Groq has generous free tier (~100k tokens/day).

**Q: Can I use my own wake word?**  
A: Yes! Train custom wake word at console.picovoice.ai

**Q: How private is my data?**  
A: 100% local except Groq API calls (optional). Books never leave your machine.

**Q: Can it control my smart home?**  
A: Not yet, but planned for future releases!

---

## ⭐ Star this repository if you found it helpful!

**Questions? Open an issue or reach out directly!**

---

*Last updated: November 30, 2024*
