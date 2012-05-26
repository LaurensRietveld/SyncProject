#!/bin/sh
ssh master /home/lrd900/syncProject/bin/master/pullGit.sh;
ssh master /home/lrd900/syncProject/bin/master/deployExportXml.sh;
