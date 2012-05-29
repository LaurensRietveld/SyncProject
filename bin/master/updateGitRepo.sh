#!/bin/sh
echo "==== ".basename(__DIR__).": Updating GIT repo ====\n";
cd ~/gitCode/;
git pull origin master;