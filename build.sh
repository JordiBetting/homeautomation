#!/bin/bash

set -e # stop on failure
set -x # print all commands

SCRIPTNAME=`basename "$0"`
SCRIPTNAME=${SCRIPTNAME%.*}

WORKSPACE="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

COMMITCOUNT=$(git -C ${WORKSPACE} rev-list --count HEAD)
TAG="jordibetting/jordibetting:java8build-${COMMITCOUNT}"

if [ "build" == $SCRIPTNAME ]; then
	docker build \
	-t $TAG \
	${WORKSPACE}
elif [ "publish" == $SCRIPTNAME ]; then
    docker push $TAG
else
	>&2 echo "Unsupported action '${SCRIPTNAME}'"
	exit 1
fi
