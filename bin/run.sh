#!/bin/bash

export JAVA_HOME=/usr/lib/jvm/java-sun
export PATH=$JAVA_HOME/bin:$PATH
cd /usr/local/goctx

echo `date` `whoami` "run.sh invoking goctx" >> /usr/local/goctx/log.txt

#-cp . is needed for log4 to find the log4j
#java -cp /usr/local/goctx/lib/*:/usr/local/goctx/bin/*:/usr/local/goctx/build/lib/goctx.jar:. edu.iu.grid.tx.Main
ant "GOC-TX Run" >> /usr/local/goctx/log.txt
