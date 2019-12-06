#!/bin/bash
clang -dynamiclib  src_c/webview.c -o libwebview.dylib -DWEBVIEW_COCOA=1 -framework WebKit -DOBJC_OLD_DISPATCH_PROTOTYPES=1