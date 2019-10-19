#!/bin/bash

set -ex

VERSION=$1
GITHASH=$2

find ./ -name 'automation_framework-*.jar' -exec cp {} . \;
find ./ -name 'automation_autocontrol-*.jar' -exec cp {} . \;

docker build --build-arg git_version=$GITHASH -t jordibetting/jordibetting:gingerbeard-domotica-framework-$1 .