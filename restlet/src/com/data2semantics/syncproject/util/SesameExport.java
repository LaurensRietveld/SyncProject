package com.data2semantics.syncproject.util;


import java.io.File;
import java.io.FileOutputStream;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.rio.rdfxml.RDFXMLWriter;

public class SesameExport {

    public static void export(String server, String repoId, File exportFile){
    	//String sesameServer = "http://127.0.0.1:8080/openrdf-sesame";
    	String sesameServer = server;
    	String repositoryID = repoId;
    	
    	Repository repo = new HTTPRepository(sesameServer, repositoryID);
        try {
			repo.initialize();
			RepositoryConnection con = repo.getConnection();
			con.isAutoCommit();
			FileOutputStream out = new FileOutputStream(exportFile);
			RDFXMLWriter rdfXmlWriter = new RDFXMLWriter(out);
			con.export(rdfXmlWriter);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    
    public static void main(String[] args) {
    	SesameExport.export("http://127.0.0.1:8080/openrdf-sesame", "census", new File("/home/lrd900/master.xml"));
    }

}
