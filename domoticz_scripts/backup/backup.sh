#!/bin/bash
DOMO_IP="127.0.0.1"  # Domoticz IP 
DOMO_PORT="8080"        # Domoticz port 
### END OF USER CONFIGURABLE PARAMETERS
TIMESTAMP=`/bin/date +%Y-%m-%d_%H-%M-%S`
BACKUPFILE="domoticzbackup_fromapi.db"
BACKUPFILEGZ="$BACKUPFILE".gz

BACKUP_LOCATION=/mnt/diskstation/domoticz_backup/$TIMESTAMP
mkdir $BACKUP_LOCATION

#Create backup and make tar archives
/usr/bin/curl -s http://$DOMO_IP:$DOMO_PORT/backupdatabase.php > ${BACKUP_LOCATION}/$BACKUPFILE
tar --absolute-names -zcvf $BACKUP_LOCATION/opt_domoticz_scripts.tar.gz /opt/domoticz/scripts/
tar --absolute-names -zcvf $BACKUP_LOCATION/opt_domoticz_www.tar.gz /opt/domoticz/www/
tar --absolute-names -zcvf $BACKUP_LOCATION/opt_domticz_Config.tar.gz /opt/domoticz/Config/

#Delete old backups 
/usr/bin/find ${BACKUP_LOCATION} -type d -mtime +183 -exec rm -rf {} \;