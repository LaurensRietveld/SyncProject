package com.data2semantics.syncproject.stores;

import java.util.ArrayList;

public class TripleStores {
	private ArrayList<TripleStore> tripleStores = new ArrayList<TripleStore>();
	TripleStores() {
		tripleStores.add(new TripleStore("http://localhost:1234", true));
	}
	
}
