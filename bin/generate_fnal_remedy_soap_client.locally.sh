#!/bin/bash
wsdl2java.sh -ssi -or -uri ~/Downloads/fnal.dev.wsdl -p edu.iu.grid.tx.soap.remedy
wsdl2java.sh -ssi -or -uri ~/Downloads/fnal.dev.create.create.wsdl -p edu.iu.grid.tx.soap.remedy 
