#!/bin/bash
# Compile and run java file

cd src
javac *.java -d ../bin
cd ../bin
java GraphicsRunner