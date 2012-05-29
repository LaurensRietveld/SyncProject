#!/bin/sh
VBoxManage startvm "Debian Slave" --type headless;
VBoxManage startvm "Debian Master" --type headless;
VBoxManage startvm "Debian Git Server" --type headless;