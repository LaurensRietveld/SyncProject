SyncProject
===========

##Dependencies:##

* Java webserver which alles encoded slashes in parameters. (for tomcat, add this to catalina.sh: JAVA_OPTS="$JAVA_OPTS -Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true"