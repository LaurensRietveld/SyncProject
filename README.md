SyncProject
===========

##Dependencies:##
The 3 servers (master, slave and a git server) all run in virtual machines (Debian on VirtualBox), with identical hardware. The scripts to setup/run the experiments and applications, depend on this setup. However, in general, the approaches here are generic to run on all types of OS and hardware.

###Restlet###
* Java webserver which alles encoded slashes in parameters. (for tomcat, add this to catalina.sh: JAVA_OPTS="$JAVA_OPTS -Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true"
* Currently, for the export/import of XML from the triple store, there is a dependency on sesame triple store
###Daemon###
* Operating system with rsync


##Getting Started##
* Make sure master and slave both have existing query log dirs, to which tomcat6 and the user running the daemon has write permissions
* Create ssh keys for the rsync between master and slave
* Set up mysql on both, and configure replication between both

