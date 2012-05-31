package com.data2semantics.syncproject.logging.modes;

import java.io.File;
import java.io.IOException;

public class SesameExportGraph {

    public static void export(File exportJar, String server, String repoId, File exportFile) throws Exception{
    	File shFile = new File(exportJar.getParent() + "/sesameExport.sh");//Bash file using nohup. Process seems to hang when I call it from processbuilder..
    	if (!shFile.exists()) {
    		throw new IOException("No shell file exists to launch the export jar from: " + shFile.getAbsolutePath());
    	}
    	if (!exportJar.exists()) {
			throw new IOException("No jar file exists to serialize graph: " + exportJar.getAbsolutePath());
    	}
    	if (!exportJar.canExecute()) {
    		throw new IOException("No permissions to execute " + exportJar.getAbsolutePath());
    	}
    	if (!exportFile.canWrite()) {
    		throw new IOException("No permissions to write to exportfile " + exportFile.getAbsolutePath());
    	}
    	
    	//./sesameExport.sh "java -jar sesameExport.jar http://master:8080/openrdf-sesame master xmlDump/dump.xml" 
    	
    	String cmd = "'java -jar " + exportJar.getAbsolutePath() + " " + server + " " + repoId + " " + exportFile.getAbsolutePath() + "'";
    	
    	ProcessBuilder pb = new ProcessBuilder(shFile.getAbsolutePath(), cmd);
    	Process p;
		p = pb.start();
        int val = p.waitFor();
        if (val != 0) {
            throw new Exception("Exception when calling export; return val = " + val);
        }
    }
}
