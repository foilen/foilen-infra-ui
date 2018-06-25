#!/bin/bash

set -e

# Set environment
export LANG="C.UTF-8"
export VERSION=master-SNAPSHOT

RUN_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $RUN_PATH

# Build
echo '###[ Create build ]###'
./step-update-copyrights.sh
./step-compile-no-tests.sh
./step-create-docker-image.sh

# Prepare folders
echo '###[ Create folders ]###'
FOLDER_DATA=$(pwd)/_data
FOLDER_PLUGINS_JARS=$(pwd)/_plugins-jars
mkdir -p $FOLDER_DATA $FOLDER_PLUGINS_JARS

# Download plugins
echo '###[ Download plugins ]###'
./download-local-plugins-jars.sh

# Start mariadb
INSTANCE=infra_ui_db
DBNAME=infra_ui

cat > $FOLDER_DATA/createDb.sh << _EOF
#!/bin/bash
mysql -uroot -pABC << _EOFF
  CREATE DATABASE $DBNAME;
_EOFF
_EOF
chmod +x $FOLDER_DATA/createDb.sh

if ! docker ps | grep infra_ui_db ; then
	echo '###[ Start mariadb ]###'
	docker run \
	  --rm \
	  --name $INSTANCE \
	  --env MYSQL_ROOT_PASSWORD=ABC \
	  --env DBNAME=$DBNAME \
	  --volume $FOLDER_DATA:/data \
	  -d mariadb:10.3.6
  
  echo '###[ Wait 20 seconds ]###'
  sleep 20s
  echo '###[ Create the MariaDB database ]###'
  docker exec -ti $INSTANCE /data/createDb.sh
fi

# Config file
cat > $FOLDER_DATA/config.json << _EOF
{
	"baseUrl" : "http://infra.localhost",
	"csrfSalt" : "404117EAC615CC20867B2150B",
	
	"mailHost" : "127.0.0.1",
	"mailPort" : 25,
	
	"mailAlertsTo" : "admin@localhost",
	"mailFrom" : "infra-ui@localhost",
	
	"mysqlDatabaseName" : "infra_ui",
	"mysqlDatabaseUserName" : "root",
	"mysqlDatabasePassword" : "ABC",
	
	"loginConfigDetails" : {
		"appId" : "BC805427E1",
		"baseUrl" : "http://login.localhost"
	},
	"loginCookieSignatureSalt" : "fa7c8c64f538931381e245661"
}
_EOF

# Start
echo '###[ Start UI ]###'
USER_ID=$(id -u)
docker run -ti \
  --rm \
  --env PLUGINS_JARS=/plugins \
  --env CONFIG_FILE=/data/config.json \
  --user $USER_ID \
  --volume $FOLDER_PLUGINS_JARS:/plugins \
  --volume $FOLDER_DATA:/data \
  --publish 8080:8080 \
  --link ${INSTANCE}:mysql \
  foilen-infra-ui:master-SNAPSHOT \
  --mode TEST

