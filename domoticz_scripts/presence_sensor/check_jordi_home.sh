#!/bin/bash
### BEGIN INIT INFO
# Provides:          jordi
# Required-Start:    $all
# Required-Stop:
# Default-Start:     2 3 4 5
# Default-Stop:
# Short-Description: Presence detector Jordi
### END INIT INFO

MY_PATH="`dirname \"$0\"`"              # relative
MY_PATH="`( cd \"$MY_PATH\" && pwd )`"  # absolutized and normalized
if [ -z "$MY_PATH" ] ; then
  # error; for some reason, the path is not accessible
  # to the script (e.g. permissions re-evaled after suid)
  exit 1  # fail
fi

source ${MY_PATH}/presence_parmas.sh

${MY_PATH}/check_device_online.py 192.168.2.54 113 ${CHECK_INTERVAL} ${COOLDOWN}
