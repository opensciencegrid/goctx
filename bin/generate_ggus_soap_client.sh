#!/bin/bash

#cd C:/dev/java/goctx

#see https://groups.google.com/d/msg/osggocdev/8uLQG1Ez3g4/GkFixtgblOEJ

#echo "downloding wsdls - don't forget to remove the urn: for GGUS 7.5 as of 5/9/2011"
prefix=7.5.dev
url=https://train-ars.ggus.eu/arsys/WSDL/public/train-ars

#prefix=7.5.prod
#url=https://prod-ars.ggus.eu/arsys/WSDL/public/prod-ars

#wget --no-check-certificate -O /tmp/ggus.helpdesk.$prefix.wsdl $url/Grid_HelpDesk
wget --no-check-certificate -O /tmp/ggus.helpdesk.$prefix.wsdl $url/GGUS
wget --no-check-certificate -O /tmp/ggus.history.$prefix.wsdl $url/Grid_History
wget --no-check-certificate -O /tmp/ggus.attachment.$prefix.wsdl $url/Grid_Attachment
wget --no-check-certificate -O /tmp/ggus.HISTORY.$prefix.wsdl $url/GGUS_HISTORY
wget --no-check-certificate -O /tmp/ggus.ATTACH.$prefix.wsdl $url/GGUS_ATTACH

###################################################################################################

#echo "downloading patched wsdl"
#prefix=guenter.train
#wget --no-check-certificate -O /tmp/ggus.helpdesk.$prefix.wsdl https://train.ggus.eu/unlinked/grid_hd.php
#wget --no-check-certificate -O /tmp/ggus.history.$prefix.wsdl https://train.ggus.eu/unlinked/grid_hist.php
#wget --no-check-certificate -O /tmp/ggus.attachment.$prefix.wsdl https://train.ggus.eu/unlinked/grid_att.php

#prefix=guenter.prod
#wget --no-check-certificate -O /tmp/ggus.helpdesk.$prefix.wsdl https://ggus.eu/unlinked/grid_hd.php
#wget --no-check-certificate -O /tmp/ggus.history.$prefix.wsdl https://ggus.eu/unlinked/grid_hist.php
#wget --no-check-certificate -O /tmp/ggus.attachment.$prefix.wsdl https://ggus.eu/unlinked/grid_att.php

echo "generating client stub (you need to download 2 wsdls if necessary)"
wsdl2java.sh -S ../src -ssi -or -uri /tmp/ggus.helpdesk.$prefix.wsdl -p edu.iu.grid.tx.soap.ggus 
wsdl2java.sh -S ../src -ssi -or -uri /tmp/ggus.history.$prefix.wsdl -p edu.iu.grid.tx.soap.ggus 
wsdl2java.sh -S ../src -ssi -or -uri /tmp/ggus.attachment.$prefix.wsdl -p edu.iu.grid.tx.soap.ggus 
wsdl2java.sh -S ../src -ssi -or -uri /tmp/ggus.HISTORY.$prefix.wsdl -p edu.iu.grid.tx.soap.ggus 
wsdl2java.sh -S ../src -ssi -or -uri /tmp/ggus.ATTACH.$prefix.wsdl -p edu.iu.grid.tx.soap.ggus 

