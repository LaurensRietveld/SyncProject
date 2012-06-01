#!/bin/sh
VBoxManage controlvm "Debian Slave" poweroff;
VBoxManage controlvm "Debian Master" poweroff;
VBoxManage controlvm "Debian Git Server" poweroff;