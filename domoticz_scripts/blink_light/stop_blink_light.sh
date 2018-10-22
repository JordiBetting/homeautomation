#!/bin/bash

LIGHT_IDX=$1
LOCKFILE=/tmp/blink_lock_$LIGHT_IDX.idx

if [ "${LIGHT_IDX}" == "" ]
then
	echo "Usage: start_blink_light.sh switch_idx"
	exit 1
fi

rm -f ${LOCKFILE}