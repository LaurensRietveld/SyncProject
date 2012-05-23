#!/bin/sh
echo "==== Building restlet ====\n";
cd ~/syncProject/restlet;
build;

echo "==== Deploying restlet ====\n";
cp dist/syncRestlet.war /var/lib/tomcat6/webapps/;
