#!/bin/bash

# ==========================================
# 🧪 Quick AI Test (Text-Only)
# ==========================================

echo "╔══════════════════════════════════════════╗"
echo "║   🧪 Quick AI Test (Text Only Mode)      ║"
echo "╚══════════════════════════════════════════╝"
echo ""

# Step 1: Check dependencies
command -v curl >/dev/null 2>&1 || { echo "❌ curl not found! Install it first."; exit 1; }
command -v python3 >/dev/null 2>&1 || { echo "❌ python3 not found! Install Python 3."; exit 1; }

# Step 2: Check API key
if [ -z "$GROQ_API_KEY" ]; then
    echo "❌ GROQ_API_KEY not set!"
    echo ""
    echo "👉 Run this before testing:"
    echo "   export GROQ_API_KEY='your-key-here'"
    echo "   (Get your key from https://console.groq.com)"
    exit 1
fi

# Step 3: Make test request
echo "🌐 Testing Groq API connection..."
RESPONSE=$(curl -s -X POST "https://api.groq.com/openai/v1/chat/completions" \
  -H "Authorization: Bearer $GROQ_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "llama-3.3-70b-versatile",
    "messages": [{"role": "user", "content": "Say hello in 5 words"}],
    "max_tokens": 20
  }')

# Step 4: Validate response
if echo "$RESPONSE" | grep -q '"choices"'; then
    echo ""
    echo "✅ Groq API is working!"
    echo ""
    echo "🧠 Response:"
    echo "$RESPONSE" | python3 -m json.tool | grep '"content"' | head -1
else
    echo ""
    echo "❌ Failed to get a valid response!"
    echo ""
    echo "Raw output:"
    echo "$RESPONSE"
fi
