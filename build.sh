#!/bin/bash

set -e # stop on failure
set -x # print all commands

SCRIPTNAME=`basename "$0"`
SCRIPTNAME=${SCRIPTNAME%.*}

USERNAME=$USER
GROUPNAME=docker
USERID=$(id -u)
GROUPID=$(getent group docker | awk -F: '{printf "%d\n", $3}')


WORKSPACE="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

COMMITCOUNT=$(git -C ${WORKSPACE} rev-list --count HEAD)
TAG="jordibetting/jordibetting:java8build-${COMMITCOUNT}"

if [ "build" == $SCRIPTNAME ]; then
	docker build \
	-t $TAG \
	--build-arg userid=${USERID} \
	--build-arg username=${USERNAME} \
	--build-arg groupname=${GROUPNAME} \
	--build-arg groupid=${GROUPID} \
	${WORKSPACE}
elif [ "publish" == $SCRIPTNAME ]; then
    docker push $TAG
else
	>&2 echo "Unsupported action '${SCRIPTNAME}'"
	exit 1
fi
