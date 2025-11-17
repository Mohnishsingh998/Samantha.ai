#!/bin/bash

echo "🚀 Starting Voice Assistant Development Environment"
echo ""

# Check ChromaDB
if ! curl -s http://localhost:8000/api/v2 > /dev/null; then
    echo "⚠️  ChromaDB not running!"
    echo "Start it in another terminal:"
    echo "  chromadb run --path ./chroma_data --port 8000"
    exit 1
fi

# Check Ollama
if ! curl -s http://localhost:11434/api/tags > /dev/null; then
    echo "⚠️  Ollama not running!"
    echo "Start it in another terminal:"
    echo "  ollama serve"
    exit 1
fi

echo "✅ All services running"
echo ""

# Check if collection exists
echo "📚 Checking knowledge base..."
COLLECTIONS=$(curl -s http://localhost:8000/api/v2/tenants/default_tenant/databases/default_database/collections | jq '. | length')

if [ "$COLLECTIONS" -eq 0 ]; then
    echo "⚠️  No books indexed yet!"
    echo ""
    echo "Index your book first:"
    echo "  mvn exec:java -Dexec.mainClass=\"com.mohnish.voiceassistant.indexing.KnowledgeBaseIndexer\""
    exit 1
fi

echo "✅ Knowledge base ready ($COLLECTIONS collection(s))"
echo ""
echo "🎉 Ready to run tests!"