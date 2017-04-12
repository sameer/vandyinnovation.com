#!/bin/bash
echo "[clang-format] Formatting..."
find src/main/java/ -iname *.java | xargs clang-format -style=file -i
echo "[clang-format] Done!"
