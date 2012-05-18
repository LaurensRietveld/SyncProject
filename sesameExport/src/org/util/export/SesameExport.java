package org.util.export;



import java.io.File;
import java.io.FileOutputStream;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.rio.rdfxml.RDFXMLWriter;

public class SesameExport {
	/**
	 * REAALLYY ugly workaround. Conflicting libs (http-common and logging) with restlet lib
	 * @param server
	 * @param repoId
	 * @param exportFile
	 */
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
    	if (args.length != 3) {
    		System.out.println("Not enough parameters passed: sesameRdfServer repoId ExportFile");
    		System.exit(1);
    	}
//    	SesameExport.export("http://127.0.0.1:8080/openrdf-sesame", "master", new File("/home/lrd900/master.xml"));
    	SesameExport.export(args[0], args[1], new File(args[2]));
    }

}
