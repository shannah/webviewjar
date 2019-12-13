#!/bin/bash
set -e
c++ -I${JAVA_HOME}/include -I${JAVA_HOME}/include/darwin -dynamiclib  src_c/webview.c -o libwebview.dylib -DWEBVIEW_COCOA=1 -framework WebKit -DOBJC_OLD_DISPATCH_PROTOTYPES=1 -std=c++11
mv libwebview.dylib src/osx_64/