#!/bin/bash

set -e

RUN_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $RUN_PATH

echo ----[ Prepare folder for docker image ]----
DOCKER_BUILD=$RUN_PATH/build/docker

rm -rf $DOCKER_BUILD
mkdir -p $DOCKER_BUILD/app

cp -v build/libs/foilen-infra-ui-$VERSION.jar $DOCKER_BUILD/app/foilen-infra-ui.jar
cp -v docker-release/* $DOCKER_BUILD
echo -n $VERSION > $DOCKER_BUILD/app/version.txt

echo ----[ Docker image folder content ]----
find $DOCKER_BUILD

echo ----[ Build docker image ]----
DOCKER_IMAGE=foilen-infra-ui:$VERSION
docker build -t $DOCKER_IMAGE $DOCKER_BUILD

rm -rf $DOCKER_BUILD
