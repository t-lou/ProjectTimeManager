#!/bin/bash

if [[ -f PTM.jar ]]; then
	echo "launch with jar"
    java -ea -jar PTM.jar
else
	echo "launch with class"
    java -ea Main
fi
