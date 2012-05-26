#!/bin/sh
echo "==== Building runnable jar for sesame export XML ====\n";
cd ~/syncProject/sesameExport;
ant;

echo "==== Moving to desired location ====\n";
cp dist/syncRestlet.war /var/lib/tomcat6/webapps/;
echo "done";
