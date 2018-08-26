#!/bin/bash

set -e

# Set environment
export LANG="C.UTF-8"

RUN_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $RUN_PATH

# Prepare folders
FOLDER_PLUGINS_JARS=$(pwd)/_plugins-jars
mkdir -p $FOLDER_PLUGINS_JARS

# Download plugins
USER_ID=$(id -u)
docker run -ti \
  --rm \
  --env PLUGINS_JARS=/plugins \
  --user $USER_ID \
  --volume $FOLDER_PLUGINS_JARS:/plugins \
  foilen/foilen-infra-system-app-test-docker:latest \
  download-latest-plugins \
  /plugins apachephp application bind9 composableapplication domain dns email infraconfig letsencrypt machine mariadb unixuser urlredirection webcertificate website

# Download H2 Driver
if [ ! -f $FOLDER_PLUGINS_JARS/h2.jar ] ; then
	wget http://jcenter.bintray.com/com/h2database/h2/1.4.197/h2-1.4.197.jar -O $FOLDER_PLUGINS_JARS/h2.jar
fi
