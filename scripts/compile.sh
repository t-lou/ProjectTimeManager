#!/bin/bash

set -e

cd $(dirname $(dirname $(realpath $0)))

javac Main.java

jar cf ProjectTimeManager.jar ProjectTimeManager/*.class

jar cfm PTM.jar MANIFEST.MF ProjectTimeManager.jar Main.class
