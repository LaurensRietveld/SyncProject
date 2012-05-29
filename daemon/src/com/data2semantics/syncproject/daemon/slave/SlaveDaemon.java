package com.data2semantics.syncproject.daemon.slave;

import com.data2semantics.syncproject.daemon.slave.modes.SlaveDb;
import com.data2semantics.syncproject.daemon.slave.modes.SlaveGit;
import com.data2semantics.syncproject.daemon.slave.modes.SlaveTextQuery;
import com.data2semantics.syncproject.daemon.slave.modes.SlaveXml;
import com.typesafe.config.Config;

public class SlaveDaemon {
	public Config config;
	public int mode;
	
	public SlaveDaemon(Config config, int mode) throws Exception{
		this.config = config;
		this.mode = mode;
		if (mode == SlaveGit.MODE) {
			new SlaveGit(config);
		} else if (mode == SlaveTextQuery.MODE) {
			new SlaveTextQuery(config);
		} else if (mode == SlaveXml.MODE) {
			new SlaveXml(config);
		} else if (mode == SlaveDb.MODE) {
			new SlaveDb(config);
		}
	}
	


	
}
