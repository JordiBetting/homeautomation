#!/bin/bash

set -e # stop on failure
set -x # print all commands

SCRIPTNAME=`basename "$0"`
SCRIPTNAME=${SCRIPTNAME%.*}

WORKSPACE="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd $WORKSPACE

# Create version
COMMITCOUNT=$(git rev-list --count)
BRANCH=$(git rev-parse --abbrev-ref HEAD)
VERSION="${COMMITCOUNT}-${BRANCH}"

TAG="jordibetting/jordibetting:java8build-${VERSION}"

if [ "build" == $SCRIPTNAME ]; then
	docker build -t $TAG .
elif [ "publish" == $SCRIPTNAME ]; then
    docker push $TAG
else
	>&2 echo "Unsupported action '${SCRIPTNAME}'"
	exit 1
fi
