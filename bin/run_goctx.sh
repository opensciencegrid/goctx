#!/bin/bash

export JAVA_HOME=/usr/lib/jvm/java-1.6.0
export GOCTX_HOME=/opt/goctx

if [ ! -d $JAVA_HOME ]
then 
  echo "\$JAVA_HOME does not exist or is not accessible. Exiting."
  exit 1
fi

if [ ! -f $JAVA_HOME/bin/java ]
then
  echo "java executable does not exist or is not accessible. Exiting."
  exit 1
fi

usage="This script is expecting as input the full text of a single email, including full headers."

if [ -t 0 ]
then
    echo $usage
    echo "No input provided. Exiting."
    exit 1
fi

email=`cat`
echo "$email" | $JAVA_HOME/bin/java -cp $GOCTX_HOME/lib/*:$GOCTX_HOME/lib/axis2-1.5/* goctx.Main

