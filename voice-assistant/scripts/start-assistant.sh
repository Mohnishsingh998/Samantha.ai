#!/bin/bash

# ==========================================
# 🚀 Voice Assistant Startup Script
# ==========================================

clear
echo "╔══════════════════════════════════════════╗"
echo "║   🤖 Starting Voice Assistant...        ║"
echo "╚══════════════════════════════════════════╝"
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[1;34m'
NC='\033[0m'

# Utility functions
print_status() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✅ $2${NC}"
    else
        echo -e "${RED}❌ $2${NC}"
        return 1
    fi
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

pause_for_fix() {
    echo ""
    print_warning "Please fix the issue above and rerun this script."
    exit 1
}

# Always run from project root
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$PROJECT_DIR" || { echo "❌ Failed to enter project root"; exit 1; }

# Step 1: Check dependencies
echo -e "${BLUE}🔍 Checking dependencies...${NC}"
java -version > /dev/null 2>&1
print_status $? "Java 17+ installed" || pause_for_fix

mvn -version > /dev/null 2>&1
print_status $? "Maven installed" || pause_for_fix

python3 --version > /dev/null 2>&1
print_status $? "Python 3 installed" || pause_for_fix

# Step 2: Ensure Python libs
python3 -c "import speech_recognition" > /dev/null 2>&1
if [ $? -ne 0 ]; then
    print_warning "SpeechRecognition not found — installing..."
    pip3 install SpeechRecognition > /dev/null 2>&1
    print_status $? "SpeechRecognition installed"
else
    print_status 0 "SpeechRecognition library ready"
fi

python3 -c "import pyaudio" > /dev/null 2>&1
if [ $? -ne 0 ]; then
    print_warning "PyAudio not found — installing..."
    brew install portaudio > /dev/null 2>&1
    pip3 install pyaudio > /dev/null 2>&1
    print_status $? "PyAudio installed"
else
    print_status 0 "PyAudio library ready"
fi

# Step 3: Check GROQ key
if [ -z "$GROQ_API_KEY" ]; then
    echo -e "${RED}❌ GROQ_API_KEY not set!${NC}"
    echo "👉 Run this before retrying:"
    echo "   export GROQ_API_KEY='your-key-here'"
    pause_for_fix
else
    print_status 0 "Groq API key configured"
fi

# Step 4: Ollama checks
if ! command -v ollama &> /dev/null; then
    print_warning "Ollama not found — installing via Homebrew..."
    brew install ollama > /dev/null 2>&1
    print_status $? "Ollama installed"
fi

if ! pgrep -x "ollama" > /dev/null; then
    print_warning "Ollama not running — starting..."
    ollama serve > /dev/null 2>&1 &
    sleep 2
    OLLAMA_STARTED=true
else
    print_status 0 "Ollama already running"
    OLLAMA_STARTED=false
fi

ollama list | grep "llama3.2:3b" > /dev/null 2>&1
if [ $? -ne 0 ]; then
    print_warning "Ollama model missing — pulling llama3.2:3b..."
    ollama pull llama3.2:3b
fi
print_status 0 "Ollama model ready"

# Step 5: Mic permissions
echo ""
print_warning "Make sure Terminal has microphone access:"
echo "   System Settings → Privacy & Security → Microphone"
echo ""

# Step 6: Compile project from root (fixed)
if [ ! -d "target/classes" ]; then
    echo "🔨 Compiling project..."
    mvn -f "$PROJECT_DIR/pom.xml" clean compile -q
    print_status $? "Compilation successful"
else
    print_status 0 "Project already compiled"
fi

# Step 7: Launch assistant (fixed)
echo ""
echo "╔══════════════════════════════════════════╗"
echo "║   🚀 Launching Voice Assistant!         ║"
echo "╚══════════════════════════════════════════╝"
echo ""
sleep 1

mvn -f "$PROJECT_DIR/pom.xml" exec:java -Dexec.mainClass="com.mohnish.voiceassistant.VoiceAssistant"

# Step 8: Cleanup
if [ "$OLLAMA_STARTED" = true ]; then
    echo ""
    echo "🛑 Stopping Ollama..."
    pkill ollama
fi

echo ""
echo -e "${GREEN}👋 Voice Assistant stopped. Goodbye!${NC}"
