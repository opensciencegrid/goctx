#!/bin/bash

#test
url=http://ar-mtdev.fnal.gov/arsys/WSDL/public/ar-srvdev.fnal.gov

#production
#url=https://ar-mt1.fnal.gov/arsys/WSDL/public/ar-srv1.fnal.gov

wget --no-check-certificate -O /tmp/remedy.wsdl $url/HPD_IncidentInterface_WS
wsdl2java.sh -ssi -or -uri /tmp/remedy.wsdl -p edu.iu.grid.tx.soap.remedy 

wget --no-check-certificate -O /tmp/remedy.create.wsdl $url/HPD_IncidentInterface_Create_WS
wsdl2java.sh -ssi -or -uri /tmp/remedy.create.wsdl -p edu.iu.grid.tx.soap.remedy 
