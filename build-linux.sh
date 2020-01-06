#!/bin/bash
set -e
g++ -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux -fPIC -std=c++11 -Wall -Wextra -pedantic -I./src_c -DWEBVIEW_GTK=1 `pkg-config --cflags gtk+-3.0 webkit2gtk-4.0` src_c/webview.c $LDFLAGS `pkg-config --libs gtk+-3.0 webkit2gtk-4.0` -shared -o libwebview.so
mv libwebview.so src/linux_64/
ant jar -Dplatforms.JDK_1.8.home=$JAVA_HOME