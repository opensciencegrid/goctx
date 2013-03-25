goctx
=====

GOC Ticket Exchanger

is a Java application which allows various support centers to exchange tickets to and from various other ticketing system. 

GOC-TX wait for "trigger" which is an email containing ticket exchange ID, and ticket ID sent by various ticketing system, and initiate creation / synchronization between source / destination ticketing systems.

GOC-TX requires following components

1) A linux sever to run GOC-TX
2) A mail server that can forward email to GOC-TX (such as /etc/aliaes for postfix)
3) MySQL DB for GOC-TX.

For more detail, please see https://sites.google.com/site/osggocdev/projects/goc-tx