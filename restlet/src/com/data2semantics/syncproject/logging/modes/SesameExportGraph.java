package com.data2semantics.syncproject.logging.modes;

import java.io.File;
import java.io.IOException;

public class SesameExportGraph {

    public static void export(File exportJar, String server, String repoId, File exportFile) throws Exception{
    	if (!exportJar.exists()) {
			throw new IOException("No jar file exists to serialize graph: " + exportJar.getAbsolutePath());
    	}
    	if (!exportJar.canExecute()) {
    		throw new IOException("No permissions to execute " + exportJar.getAbsolutePath());
    	}
    	ProcessBuilder pb = new ProcessBuilder("java", "-jar", exportJar.getAbsolutePath(), server, repoId, exportFile.getAbsolutePath());
    	Process p;
		p = pb.start();
        int val = p.waitFor();
        if (val != 0) {
            throw new Exception("Exception when calling export; return val = " + val);
        }
    }
}
