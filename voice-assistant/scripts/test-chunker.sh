#!/bin/bash
echo "✂️  Testing Text Chunker..."
java -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" \
    com.mohnish.voiceassistant.document.TextChunkerTest
