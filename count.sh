#!/bin/bash
echo -n "Files: "
(find -name '*.java' | grep -v com | grep -v Messages.java; echo Shared/src/codeday/squareassault/protobuf/main.proto) | wc -l
wc -cl `find -name '*.java' | grep -v com | grep -v Messages.java; echo Shared/src/codeday/squareassault/protobuf/main.proto`
