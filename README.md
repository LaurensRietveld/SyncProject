SyncProject
===========

##Dependencies:##
The 3 servers (master, slave and a git server) all run in virtual machines (Debian on VirtualBox), with identical hardware. The scripts to setup/run the experiments and applications, depend on this setup. However, in general, the approaches here are generic to run on all types of OS and hardware.

###Restlet###
* Java webserver which alles encoded slashes in parameters. (for tomcat, add this to catalina.sh: JAVA_OPTS="$JAVA_OPTS -Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true"
###Daemon###
* Operating system with rsync


##Getting Started##
* Ideally, add a network interface on the host computer, and use that to create an internal network with the 3 nodes. (in virtualbox, a host-only adapter)
* For internet access, you can use NAT as another network adapter of the 3 nodes
* Make sure all hosts files (on the nodes, as well as the host) point to the right ip

## Directories:
* bin: collection of scripts to set up the servers, compile the project, sync the jars/wars to their proper location, and run experiments (see readme in there for more info)
* config: Config directory. Used in all java code, as well as in bin directory scripts. Other folder (e.g. restlet folder) use symlink to connect include a config file. Other applications (e.g. daemon) gets their config file by getting it from the github repo (https://raw.github.com/....)
* daemon: Code for the java daemon, which is runnable on the slave node. Simply build, and then execute using 'java -jar daemon.jar'.
* restlet: The restlet which runs on the master node, and which handles execution and logging of queries
