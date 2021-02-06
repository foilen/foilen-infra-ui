#!/bin/bash

set -e

RUN_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"


echo ----[ Prepare build folder ]----
BUILD_PATH=$RUN_PATH/build/ui
mkdir -p $RUN_PATH/build/ui
cp -v $RUN_PATH/src/main/resources/WEB-INF/infra/ui/resources/ui2/vendor/package.json $BUILD_PATH/

echo ----[ Download ]----
cd $BUILD_PATH
npm install

echo ----[ Cleanup destination ]----
DEST_PATH=$RUN_PATH/src/main/resources/WEB-INF/infra/ui/resources/ui2/vendor/dist
rm -rfv $DEST_PATH
mkdir -p $DEST_PATH

echo ----[ Move desired files ]----
cd $BUILD_PATH/node_modules
for dep in $(ls); do
	if [ -d $dep/dist/ ]; then
		cp -rv $dep/dist/* $DEST_PATH
  else
  	cp -rv $dep/src/* $DEST_PATH
	fi
done
