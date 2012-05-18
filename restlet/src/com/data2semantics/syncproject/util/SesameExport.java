package com.data2semantics.syncproject.util;

import java.io.File;

public class SesameExport {

    public static void export(String exportJar, String server, String repoId, File exportFile){
    	ProcessBuilder pb = new ProcessBuilder("java", "-jar", exportJar, server, repoId, exportFile.getAbsolutePath());
    	Process p;
		try {
			p = pb.start();
	        int val = p.waitFor();
	        if (val != 0) {
	            throw new Exception("Exception when calling export; return val = " + val);
	        }
		} catch (Exception e) {
			System.out.println("Failed performing export to xml: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
    }
}
