#!/bin/bash

set -ex

VERSION=$1

TMP_DIR=$(mktemp -d -t buildDockerGingerbeard-XXXXXXXXXX)

find ./ -name 'automation_framework-*.jar' -exec cp {} . \;
find ./ -name 'automation_autocontrol-*.jar' -exec cp {} . \;

docker build --build-arg -t gingerbeard-automation-$1 .

rm -rf $TMP_DIR
