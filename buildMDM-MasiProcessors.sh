#!/bin/sh
export MAVEN_OPTS="-DskipTests=true"
export GIT_BASE=/tmp/git
export KIT_DM_BASE=$GIT_BASE/kit_dm
export KIT_DM_VERSION=1.5
export MASI_PROCESSORS_BASE=`pwd`

# Create temporary directories.
if [ ! -e $GIT_BASE ]; then mkdir $GIT_BASE; fi
if [ ! -e $KIT_DM_BASE ]; then mkdir $KIT_DM_BASE; fi

# First KIT Data Manager $KIT_DM_VERSION has to be built
cd $KIT_DM_BASE

# Remove old sources
rm -rf *

# Get release of KIT Data Manager
wget https://github.com/kit-data-manager/base/archive/KITDM_$KIT_DM_VERSION.zip
unzip KITDM_$KIT_DM_VERSION.zip

# Copy dependency of Tools 1.5 to lib 1.6
cp -r $MASI_PROCESSORS_BASE/lib/org/fzk/ipe/Tools/1.5  base-KITDM_$KIT_DM_VERSION/libs/org/fzk/ipe/Tools/

# Change dependency of grid-util from Tools 1.5 to Tools 1.6
cp $MASI_PROCESSORS_BASE/lib/org/fzk/ipe/grid-util/2.1/grid-util-2.1.pom  base-KITDM_$KIT_DM_VERSION/libs/org/fzk/ipe/grid-util/2.1/grid-util-2.1.pom

# Build libraries needed for MetaStore
cd base-KITDM_$KIT_DM_VERSION
mvn $MAVEN_OPTS clean install -P Release,generate-doc,test

# Now build MDM-MasiProcessors
cd $MASI_PROCESSORS_BASE

# Remove old zip files first
rm zip/*

echo Performing clean assembly
mvn  $MAVEN_OPTS clean assembly:assembly 

