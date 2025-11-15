#!/bin/bash
echo "🚀 Starting all services..."

# Start ChromaDB in Docker
echo "Starting ChromaDB..."
if docker ps -a --format '{{.Names}}' | grep -q '^chromadb$'; then
    docker start chromadb
else
    docker run -d \
      --name chromadb \
      -p 8000:8000 \
      -v ~/Projects/voice-assistant/voice-assistant/chroma_data:/chroma/chroma \
      chromadb/chroma:latest
fi

# Start Ollama
echo "Starting Ollama..."
ollama serve &
OLLAMA_PID=$!

echo ""
echo "✅ All services started!"
echo "   ChromaDB: http://localhost:8000"
echo "   Ollama: http://localhost:11434"
echo ""
echo "To stop:"
echo "   docker stop chromadb"
echo "   kill $OLLAMA_PID"