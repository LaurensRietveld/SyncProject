#!/bin/sh
VBoxManage controlvm "Debian Slave" savestate;
VBoxManage controlvm "Debian Master" savestate;
VBoxManage controlvm "Debian Git Server" savestate;