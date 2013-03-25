GOC Ticket Exchanger (GOCTX)
============================

About
-----

Java application to exchange(synchronize) tickets to and from various ticketing systems (Footprints, GGUS, RT, ServiceNow, and others)

GOC-TX wait for "trigger" which is an email containing ticket exchange ID, and ticket ID sent by various ticketing system, and initiate creation / synchronization between source / destination ticketing systems.

GOC-TX requires following components

1) A linux sever to run GOC-TX
2) A mail server that can forward email to GOC-TX (such as /etc/aliaes for postfix)
3) MySQL DB for GOC-TX.

You will also need to be able to write a few Java classes which will handle various "corner" cases if you want GOC-TX to do something intelligent.

Installation
------------
git clone git://github.com/soichih/goctx.git
cd goctx
ant


For more detail, please see https://sites.google.com/site/osggocdev/projects/goc-tx