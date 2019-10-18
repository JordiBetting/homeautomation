#!/bin/bash

set -ex

VERSION=$1

find ./ -name '*.jar' -exec mv {} . \;

docker build -t jordibetting/jordibetting:gingerbeard-domotica-framework-$1 .
