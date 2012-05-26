#!/bin/sh
echo "==== Building runnable jar for sesame export XML ====\n";
cd ~/syncProject/sesameExport;
ant;

echo "==== Moving to desired location ====\n";
cp dist/sesameExport.jar /usr/local/share/syncProject/;
echo "done";
