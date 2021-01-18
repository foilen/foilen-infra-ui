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

# Start mongodb
INSTANCE=infra-ui-mongodb

if ! docker ps | grep $INSTANCE ; then
	echo '###[ Start mongodb ]###'
	./mongodb-start.sh
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
	
	"mongoUri" : "mongodb://172.17.0.1:27085/foilen-ui-test",
	
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
  --publish 8888:8080 \
  foilen-infra-ui:master-SNAPSHOT \
  --mode TEST

