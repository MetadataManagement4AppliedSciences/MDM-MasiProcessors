#!/bin/bash

shopt -s extglob

LIB_DIR=../KITDM/WEB-INF/lib
MASI_LIB_DIR=${LIB_DIR}/${libFolder}
MASI_LIB_DIR_OLD=$MASI_LIB_DIR/oldlibs

# Change working dir to directory of the script.
WORKING_DIR="$( cd "$( dirname "$0" )" && pwd )"
cd $WORKING_DIR

# Remove old versions of libraries from lib directory.
mkdir $MASI_LIB_DIR_OLD

for i in $(cat listOfOldLibraries.txt);
do
  if [ "${i##*.}" != "jar" ]; then
    mv $LIB_DIR/$i-+([0-9.]).jar $MASI_LIB_DIR_OLD;
  else
    mv $LIB_DIR/$i $MASI_LIB_DIR_OLD;
  fi
done; 

# Link new versions to lib directory
ln $MASI_LIB_DIR/*.jar $LIB_DIR
