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

Installation Detail
-------------------

Installation instructions are for CentOS 6.3. Other distributions may vary.

git clone git://github.com/soichih/goctx.git 
cd goctx 
git checkout mpackard
ant install 

That will install in /opt/goctx-${version}

(To install in a different location, run: 'ant -Dinstall.dir=/some/other/directory install'.)




Here we use Postfix for mail service. Configure it to allow for sending and receiving mail. Also your firewall should allow both incoming and outgoing mail to this server.

Install mysql-server, git:

# yum install -y mysql-server git

Checkout GOC-TX source:

# git clone https://github.com/soichih/goctx

Create GOC-TX database & user, grant rights (use a different password):

# mysqladmin -p create goctx
# mysql> create user 'goctx'@'localhost' identified by '12345abcde’;
# mysql> grant all privileges on goctx.* to goctx@localhost;

Import GOC-TX tables using included sql file:

# mysql -u goctx -p goctx < mysql.sql

Configure Postfix for address extension. Put something like this in /etc/postfix/main.cf:

        recipient_delimiter = +

Create an alias for the GOC-TX account to receive mail. Add this to /etc/aliases:

        tx: |/usr/local/goctx/run.sh

# newaliases

...

Build GOC-TX jar:

ant jar

Install on system (not working yet):

ant install 
