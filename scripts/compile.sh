#!/bin/bash

cd $(dirname $(dirname $(realpath $0)))

javac ./ProjectTimeManager/*.java

jar -cvfe ./PTM.jar ProjectTimeManager.Main ./ProjectTimeManager/*.class