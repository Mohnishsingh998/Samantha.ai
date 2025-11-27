#!/bin/bash

echo "🚀 Starting RAG Pipeline Development Environment"
echo ""

# Check services
if ! curl -s http://localhost:8000/api/v2 > /dev/null; then
    echo "❌ ChromaDB not running!"
    echo "Start it: chroma run --path ./chroma_data --port 8000"
    exit 1
fi

if ! curl -s http://localhost:11434/api/tags > /dev/null; then
    echo "❌ Ollama not running!"
    echo "Start it: ollama serve"
    exit 1
fi

echo "✅ All services running"
echo ""
# Check API key
if [ -z "$GROQ_API_KEY" ]; then
    echo "⚠️  GROQ_API_KEY not set (will use Ollama - slower)"
    echo "Set it: export GROQ_API_KEY='your-key'"
    echo ""
fi

# Run RAG test
echo "🧠 Starting RAG Pipeline..."
mvn exec:java -Dexec.mainClass="com.mohnish.voiceassistant.rag.RAGTest"