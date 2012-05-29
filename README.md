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

## Directories:
* bin: collection of scripts to set up the servers, compile the project, and sync the jars/wars to their proper location
* config: Config directory. Used in all java code, as well as in bin directory scripts. Other folder (e.g. restlet folder) use symlink to connect include a config file. Other applications (e.g. daemon) gets their config file by getting it from the github repo (https://raw.github.com/....)
* daemon: Code for the java daemon, which is runnable on both the master and slave node. Simply build, and then execute using 'java -jar daemon.jar'.
* restlet: The restlet which runs on the master node, and which handles execution and logging of queries
* sesameExport: A separate project to be able to export xml from sesame. Conflicting libs do not allow including this project in the restlet, therefor it needs to be called commandline. Build this dir, and put it in the appropriate folder on the master node (see config file for which one).
