#!/bin/bash

set -ex

VERSION=$1

find ./ -name 'automation_framework-*.jar' -exec cp {} . \;
find ./ -name 'automation_autocontrol-*.jar' -exec cp {} . \;

docker build -t jordibetting/jordibetting:gingerbeard-domotica-framework-$1 .
