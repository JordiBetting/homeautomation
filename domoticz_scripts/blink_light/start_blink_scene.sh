#!/bin/bash

SCENE_ON_IDX=$1
SCENE_OFF_IDX=$2
LOCKFILE=/tmp/blink_lock_${SCENE_ON_IDX}-${SCENE_OFF_IDX}.idx

if [ "$SCENE_ON_IDX" == "" ] || [ "$SCENE_OFF_IDX" == "" ]
then
	echo "Usage: start_blink_scene.sh scene_on_idx scene_off_idx"
	exit 1
fi

if [ -f ${LOCKFILE} ]
then
	echo "Script already running. Lockfile Present: ${LOCKFILE}"
	exit 1
fi

touch ${LOCKFILE}

while [ -f ${LOCKFILE} ]
do
	curl -s "http://127.0.0.1:8080/json.htm?type=command&param=switchscene&idx=${SCENE_ON_IDX}&switchcmd=On" > /dev/null
	sleep 1
	curl -s "http://127.0.0.1:8080/json.htm?type=command&param=switchscene&idx=${SCENE_OFF_IDX}&switchcmd=On" > /dev/null
	sleep 1
done
