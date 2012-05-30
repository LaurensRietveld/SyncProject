#!/bin/sh
echo "==== Compiling ====";
echo "\tRestlet";
cd /home/lrd900/code/syncProject/restlet;
ant -q;
echo "\tDaemon";
cd /home/lrd900/code/syncProject/daemon;
ant -q;
echo "\tSesame Export";
cd /home/lrd900/code/syncProject/sesameExport;
ant -q;
