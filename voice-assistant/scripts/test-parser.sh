#!/bin/bash

# Quick test script for DocumentParser

echo "🧪 Testing Document Parser..."
echo ""

# Check if books directory exists
if [ ! -d "books" ]; then
    echo "❌ 'books' directory not found!"
    echo "Creating it..."
    mkdir books
    
    # Create test file
    cat > books/test.txt << 'EOF'
This is a test document for the voice assistant.
It contains some sample text about artificial intelligence.
EOF
    echo "✅ Created test.txt in books/"
fi

# Compile if needed
if [ ! -d "target/classes" ]; then
    echo "🔨 Compiling project..."
    mvn compile -q
fi

# Run the test
echo "📄 Running DocumentParserTest..."
echo ""

java -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" \
    com.mohnish.voiceassistant.document.DocumentParserTest