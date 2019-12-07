#!/bin/bash
set -e
if [ ! -d dist ]; then
    mkdir dist
fi
gcc -c src_c/webview.c -o dist/webview.o -DWEBVIEW_WINAPI=1 -lole32 -lcomctl32 -loleaut32 -luuid -lgdi32 -std=c99 -Wall -Wextra -pedantic -I./src_c
gcc -shared dist/webview.o -o dist/webview.dll  -lole32 -lcomctl32 -loleaut32 -luuid -lgdi32
if [ ! -d src/win32-x86-64 ]; then
    mkdir src/win32-x86-64
fi
mv dist/webview.dll src/win32-x86-64/
rm dist/webview.o