package com.data2semantics.syncproject.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.restlet.data.MediaType;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;
import com.data2semantics.syncproject.EntryPoint;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

import freemarker.template.Configuration;

public class Util  {

	
	public static Representation getQueryForm(EntryPoint entryPoint, boolean isUpdateQuery, String uri) {
		Configuration configuration = entryPoint.getFMConfiguration();
		Map<String, Object> map = new HashMap<String, Object>();
		
		String queryType = isUpdateQuery? "update": "query";
		map.put("queryType", queryType);
		map.put("action", uri.endsWith("/")? "": queryType);
		
		return new TemplateRepresentation("query.ftl", configuration, map, MediaType.TEXT_HTML);
	}
		
	public static Representation getErrorPage(EntryPoint entryPoint, String error) {
		entryPoint.getLogger().severe(error);
		Configuration configuration = entryPoint.getFMConfiguration();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("error", error.replace("\t", "&nbsp;&nbsp;&nbsp;").replace("\n", "<br>"));
		return new TemplateRepresentation("error.ftl", configuration, map, MediaType.TEXT_HTML);
	}
	
	public static void rsync(File srcFile, String destFile) throws Exception {
		// Currently uses passwordless SSH keys to login
        String[] cmd = new String[]{"rsync", "-a", srcFile.getAbsolutePath(), destFile};
        ProcessBuilder pb = new ProcessBuilder(cmd);
        Process process = pb.start();
        int val = process.waitFor();
        if (val != 0) {
            throw new Exception("Exception during RSync; return code = " + val + " cmd: " + Arrays.toString(cmd));
        }
	}
	
	public static void writeQueryToFile(Logger logger, File file, String string) throws IOException {
		FileWriter fw;
		if(!file.exists()){
	    	logger.warning("Log file does not existing. Creating one: " + file.getPath());
	    	file.createNewFile();
	    	fw = new FileWriter(file);
	    } else {
	    	fw = new FileWriter(file, true);
	    }
	    logger.info("Writing to file: " + string);
	    
	    BufferedWriter bw = new BufferedWriter(fw);
	    bw.write(string);
	    bw.close();
	}
	
	public static String getTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		Date dt = new Date();
		return sdf.format(dt); // formats to 09/23/2009 13:53:28
	}
	
	public static void writeSerializationToFile(ResultSet result, File exportFile) throws IOException {
		FileWriter fileWriter = new FileWriter(exportFile.getAbsolutePath(),true);
        BufferedWriter bufferWritter = new BufferedWriter(fileWriter);
    	while (result.hasNext()) {
    		QuerySolution solution = result.next();
    		String writeString = getNodeAsString(solution.get("subject")) + " " + 
	        		getNodeAsString(solution.get("predicate")) + " " + 
	        		getNodeAsString(solution.get("object")) + "\n";
			bufferWritter.write(writeString);
		}
    	bufferWritter.close();
	}
	
	/**
	 * For an rdf node (taken from a 'querysolution'), get a formatted string, so that this node can easily be used in the insert query of the slave
	 * @param rdfNode
	 * @return
	 */
	public static String getNodeAsString(RDFNode rdfNode) {
		String result = "";
		if (rdfNode.isLiteral()) {
			result = rdfNode.toString();
			try {
				Integer.parseInt(result);
				//Keep result value as it is
			} catch (Exception e) {
				//apparently a string, so add quotes
				result = "'" + result + "'";
			}

		} else if (rdfNode.isAnon()) {
			//Make it into a uri
			result = "<" + rdfNode.toString() + ">";
			
		} else if (rdfNode.isResource() || rdfNode.isURIResource()) {
			result = "<" + rdfNode.toString() + ">";
		}
		return result;
	}
	
	/**
	 * Execute query
	 * 
	 * @param endpoint Endpoint Uri
	 * @param queryString
	 * 
	 * @return ResultSet
	 */
	public static ResultSet query(String endpoint, String queryString) {
		Query query = QueryFactory.create(queryString);
		QueryExecution queryExecution = QueryExecutionFactory.sparqlService(endpoint, query);
		ResultSet results = queryExecution.execSelect();
		return results;
	}
	
	/**
	 * Execute command (e.g. git pull, push or commit)
	 * 
	 * @param cmd
	 * @throws Exception In case git encounters error (e.g. failed merges)
	 * 
	 * @returns boolean False if nothing changed ('already up to date'), true otherwise
	 */
	public static boolean executeCmd(ProcessBuilder cmd) throws Exception {
		boolean result = true;
        Process process;
		process = cmd.start();
        int val = process.waitFor();

        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line;
        String output = "";
        while ((line = br.readLine()) != null) {
        	if (val != 0) {
        		//Output this for debugging purposes. Something went wrong.
        		output += line;
        		continue;
        	}
        	if (line.equals("Already up-to-date.")) {
        		result = false;
        	}
        	break;
        }
        if (val != 0) {
            throw new Exception("Exception excecuting " + cmd.command().toString() +"; return code = " + val + "; command output = " + output);
        }
        return result;
	}
	
	
	
	
	
	

	public static void executeQuery(String uri, String query) throws IOException {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(uri);
		post.addHeader("Content-type", "application/x-www-form-urlencoded");
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("update", query));
		post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		client.execute(post);
	}
	
	
	/**
	 * Compare text files, execute differences (i.e. new queries), and log new executed queries
	 * 
	 */
	public static void processTextFileChanges(File queriesFile, File executedQueriesFile, String delimiter, String tripleStoreUri) throws Exception {
		// Get the object of DataInputStream
		DataInputStream inSrc = new DataInputStream(new FileInputStream(queriesFile));
		DataInputStream inDest = new DataInputStream(new FileInputStream(executedQueriesFile));
		BufferedReader brSrc = new BufferedReader(new InputStreamReader(inSrc));
		BufferedReader brDest = new BufferedReader(new InputStreamReader(inDest));
		String srcLine;
		boolean firstline = true;
		String changes = "";
		// Read File Line By Line
		while ((srcLine = brSrc.readLine()) != null) {
			if (!srcLine.equals(brDest.readLine())) {
				changes += (firstline? "" : "\n") + srcLine;
			}
			firstline = false;

		}
		//System.out.println("Wrinting Changes: '" + changes + "'");
		inSrc.close();
		inDest.close();
		
		executeChanges(changes, delimiter, tripleStoreUri);
		storeChanges(executedQueriesFile, changes);
			
	}
	
	/**
	 * Execute string containing changes
	 * 
	 * @param changes String containing changes
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private static void executeChanges(String changes, String delimiter, String tripleStoreUri) throws IOException {
		String[] queries = changes.split(delimiter);
		for (String query: queries) {
			query = query.trim();
			if (query.length() > 0) {
//				System.out.println("Executing: " + query);
				executeQuery(tripleStoreUri, query);
			}
		}
	}
	
	/**
	 * Store (the just executed) changes in log file, to keep track of what has been executed
	 * 
	 * @param changes
	 * @param executedQueriesFile
	 * @throws IOException 
	 */
	private static void storeChanges(File file, String changes) throws IOException {
		FileWriter fw;
		if (file.length() <= 1) {
			fw = new FileWriter(file);
		} else {
			fw = new FileWriter(file, true);
		}
		
	    BufferedWriter bw = new BufferedWriter(fw);
	    bw.write(changes);
	    bw.close();
	}
	
	
	public static void importDumpFile(File dumpFile, String uri) throws Exception {
		String queryString = "INSERT DATA {\n";
		FileReader fr = new FileReader(dumpFile);
		BufferedReader reader = new BufferedReader(fr);
		String st = "";
		while ((st = reader.readLine()) != null) {
			queryString += st + " .\n";
		}
		queryString += "}";
		Util.executeQuery(uri, queryString);
	}
	
	
	
	
	
}