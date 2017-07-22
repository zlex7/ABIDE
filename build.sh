#!/bin/bash
# Compile and run java file

cd src
javac * -d ../bin
cd ../bin
java GraphicsRunner