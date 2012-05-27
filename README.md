SyncProject
===========

##Dependencies:##
###Restlet###
* Java webserver which alles encoded slashes in parameters. (for tomcat, add this to catalina.sh: JAVA_OPTS="$JAVA_OPTS -Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true"
* Currently, for the export/import of XML from the triple store, there is a dependency on sesame openrdf workbench
###Daemon###
* Operating system with rsync


##Getting Started##
* Make sure master and slave both have existing query log dirs
* Create ssh keys for the rsync between master and slave
* Set up mysql on both, and configure replication between both

