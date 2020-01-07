#!/bin/bash
set -e
cd windows
script/build.bat
cp dll/x64/*.dll ../src/windows_64/
cp dll/x86/*.dll ../src/windows_32/
cd ..
ant jar -Dplatforms.JDK_1.8.home="$JAVA_HOME"