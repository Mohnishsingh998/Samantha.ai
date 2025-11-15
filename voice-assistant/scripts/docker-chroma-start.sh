#!/bin/bash
echo "🐳 Starting ChromaDB in Docker..."

# Check if container exists
if docker ps -a --format '{{.Names}}' | grep -q '^chromadb$'; then
    echo "Starting existing container..."
    docker start chromadb
else
    echo "Creating new container..."
    docker run -d \
      --name chromadb \
      -p 8000:8000 \
      -v ~/Projects/voice-assistant/voice-assistant/chroma_data:/chroma/chroma \
      chromadb/chroma:latest
fi

echo "✅ ChromaDB running on http://localhost:8000"