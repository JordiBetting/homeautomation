#!/bin/bash
### BEGIN INIT INFO
# Provides:          subwoofer_control
# Required-Start:    $all
# Required-Stop:
# Default-Start:     2 3 4 5
# Default-Stop:
# Short-Description: Presence detector Jordi
### END INIT INFO



echo "Legend: 0=off, 1=on"
curl --silent 'http://127.0.0.1:8080/json.htm?type=devices&rid=396' | grep 'Status' | grep -i 'off' > /dev/null
printf $? > /dev/shm/subwoofer

while true; do
  subwoofer_on=$(cat /dev/shm/subwoofer)

  printf "subwoofer_on=${subwoofer_on}"

  printf ", onkyo="
  onkyo system-power=query | grep off > /dev/null

  if [ "$?" -eq 0 ]; then
    echo "0"
    if [ "${subwoofer_on}" -eq 1 ]; then
      echo "> Turning subwoofer off"
      (curl 'http://127.0.0.1:8080/json.htm?type=command&param=switchlight&idx=396&switchcmd=Off&level=0' 2>&1 | grep OK > /dev/null) && printf 0 > /dev/shm/subwoofer
    fi
  else
    echo  "1"
    if [ "${subwoofer_on}" -eq 0 ]; then
      echo "> Turning subwoofer on"
      (curl 'http://127.0.0.1:8080/json.htm?type=command&param=switchlight&idx=396&switchcmd=On&level=0' 2>&1 | grep OK > /dev/null) && printf 1 > /dev/shm/subwoofer
    fi
  fi
  sleep 3
done
