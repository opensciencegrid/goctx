#!/bin/bash

url=http://jira.opensciencegrid.org/rpc/soap/jirasoapservice-v2?wsdl
wget -O /tmp/jira.wsdl $url

export PATH=~/app/axis2-1.5/bin:$PATH
wsdl2java.sh -S ../src -ssi -or -uri /tmp/jira.wsdl -p edu.iu.grid.tx.soap.jira 
