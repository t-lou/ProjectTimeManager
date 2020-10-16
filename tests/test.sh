#!/bin/bash

set -e

# rm $(find -name "*.class")

for sub in $(ls)
do
  if [[ -d $sub ]];
  then
    cd $sub
    javac -cp ../../ProjectTimeManager.jar Main.java
    java -ea -cp ../../ProjectTimeManager.jar:. Main
    if [[ -f MainCheck.java ]];
    then
      javac -cp ../../ProjectTimeManager.jar MainCheck.java
      java -ea -cp ../../ProjectTimeManager.jar:. MainCheck
    fi
    cd -
  fi
done
