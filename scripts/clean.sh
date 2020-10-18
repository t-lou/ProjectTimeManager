#!/bin/bash

set -e

cd $(dirname $(dirname $(realpath $0)))

rm $(find -name "*.class")
rm $(find -name "*.jar")