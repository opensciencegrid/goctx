#!/bin/bash

export JAVA_HOME=/usr/lib/jvm/java-1.6.0
export GOCTX_HOME=@@install_dir@@

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

usage="This script is expecting as input a single fqdn."

if [ -z $1 ]
then
    echo $usage
    echo "No input provided. Exiting."
    exit 1
fi

$JAVA_HOME/bin/java -cp $GOCTX_HOME/lib/*:$GOCTX_HOME/lib/axis2-1.5/* goctx.InstallCert $1 

