#!/bin/bash

LIGHT_IDX=$1
LOCKFILE=/tmp/blink_lock_$LIGHT_IDX.idx

if [ "${LIGHT_IDX}" == "" ]
then
	echo "Usage: start_blink_light.sh switch_idx"
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
	curl -s "http://127.0.0.1:8080/json.htm?type=command&param=switchlight&idx=${LIGHT_IDX}&switchcmd=On" > /dev/null
	sleep 1
	curl -s "http://127.0.0.1:8080/json.htm?type=command&param=switchlight&idx=${LIGHT_IDX}&switchcmd=Off" > /dev/null
	sleep 1
done
