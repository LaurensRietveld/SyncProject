#!/bin/sh
echo "==== Compiling Restlet ====";
cd /home/lrd900/code/syncProject/restlet;
ant -q;
echo "==== Compiling Daemon ====";
cd /home/lrd900/code/syncProject/daemon;
ant -q;
echo "==== Compiling Sesame Export ====";
cd /home/lrd900/code/syncProject/sesameExport;
ant -q;
