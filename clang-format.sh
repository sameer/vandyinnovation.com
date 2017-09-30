#!/bin/bash
echo "[clang-format] Formatting..."
find src/main/java/ -iname *.java -exec clang-format -i -style=file -- {} +
echo "[clang-format] Done!"
