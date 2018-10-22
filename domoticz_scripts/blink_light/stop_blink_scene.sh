#!/bin/bash

SCENE_ON_IDX=$1
SCENE_OFF_IDX=$2
LOCKFILE=/tmp/blink_lock_${SCENE_ON_IDX}-${SCENE_OFF_IDX}.idx

if [ "$SCENE_ON_IDX" == "" ] || [ "$SCENE_OFF_IDX" == "" ]
then
	echo "Usage: stop_blink_scene.sh scene_on_idx scene_off_idx"
	exit 1
fi

rm -f ${LOCKFILE}