package com.data2semantics.syncproject.stores;

public class TripleStore {
	private String location;
	private boolean master;
	TripleStore(String location, boolean master) {
		this.location = location;
		this.setMaster(master);
	}
	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
	public boolean isMaster() {
		return master;
	}
	public void setMaster(boolean master) {
		this.master = master;
	}
	
}
