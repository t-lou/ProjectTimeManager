#!/bin/bash

if [[ -f PTM.jar ]]; then
	echo "launch with jar"
    java -jar PTM.jar
else 
	echo "launch with class"
    java ProjectTimeManager.Main
fi