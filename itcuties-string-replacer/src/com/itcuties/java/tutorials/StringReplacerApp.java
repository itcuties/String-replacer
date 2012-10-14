package com.itcuties.java.tutorials;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Replaces file contents
 * 
 * @author itcuties
 *
 */
public class StringReplacerApp {

	/* A map of strings where:
	 * key represents a string to be replaced
	 * value is a string to replace key with
	 */
	private static Map<String, String> replaceStrings;
	
	/* Extension of a file that we are going to process .
	 */
	private static String fileExtension;
	
	/* A sepparator used in a file that contains replace definitions. 
	 */
	private static String SEPARATOR = "===>";
	
	/**
	 * Load replace strings data.
	 * 
	 * @param dataFile
	 * @throws IOException 
	 */
	private static void loadReplaceData(String dataFile) throws IOException {
		System.out.println("Loading replace data from file " + dataFile);
		
		replaceStrings = new HashMap<String, String>();
		
		FileInputStream fstream = new FileInputStream(dataFile);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		
		String line;
		while ((line = br.readLine()) != null) {
			String[] lineSplit = line.split(SEPARATOR);
			System.out.println("reading replace: " + lineSplit[0] + " with:" + lineSplit[1]);
			replaceStrings.put(lineSplit[0], lineSplit[1]);
		}
		
		br.close();
		in.close();
		fstream.close();
		
	}
	
	/**
	 * Process directory
	 * @param directory
	 * @throws IOException 
	 */
	private static void processDirectory(File directory) throws IOException {
		System.out.println("Processing directory " + directory.getName());
		
		for (File node: directory.listFiles()) {
			if (node.isFile())
				processFile(node);
			else if (node.isDirectory())
				processDirectory(node);
		}
		
	}
	
	/**
	 * Process file
	 * @param file
	 * @throws IOException 
	 */
	private static void processFile(File file) throws IOException {
		System.out.println("Processing file " + file.getName());
		// If a file has an appropriate extension then process it
		if (file.getName().indexOf(fileExtension) != -1) {
			
			// Reading objects
			FileInputStream fis = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fis);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			// Writing objects
			File tmpFile = new File(file.getAbsolutePath() + "_tmp");
			FileWriter fw = new FileWriter(tmpFile);
			BufferedWriter out = new BufferedWriter(fw);
			
			String line;
			while ((line = br.readLine()) != null) {
				for (String replaceKey: replaceStrings.keySet()) {
					if (line.indexOf(replaceKey) != -1)
						line = line.replaceAll(replaceKey, replaceStrings.get(replaceKey));
				}
				
				out.write(line+"\n");
			}
			
			br.close();
			in.close();
			fis.close();
			
			out.close();
			fw.close();
			
			// Tmp file is ready now
			// Delete original file
			if (!file.delete())
				throw new IOException("Can't delete " + file.getName());
			if (!file.createNewFile())
				throw new IOException("Can't create " + file.getName());
			
			// Copy from _tmp to original file - we have to create it now
			copyFile(tmpFile,file);
			
			// Delete tmp file
			if (!tmpFile.delete())
				throw new IOException("Can't delete " + tmpFile.getName());
			
		}
		
	}
	
	/**
	 * Copy from file to file.
	 * @param src	- source file 
	 * @param dest	- destination file
	 * @throws IOException 
	 */
	private static void copyFile(File src, File dest) throws IOException {
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dest);

		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0){
			out.write(buf, 0, len);
		}
		
		in.close();
		out.close();
	}
	
	/**
	 * Run application
	 * @param args
	 */
	public static void main(String[] args) {
		if (args == null || args.length != 3 ) {
			System.out.println("Invalid program arguments");
			System.out.println("arg1 - a path to a data file");
			System.out.println("arg2 - a path to a directory that we are going to start our search");
			System.out.println("arg3 - file extension");
			System.out.println("example usage: java StringReplacerApp /home/user/projects/regex-data.txt /home/user/projects/my-project-directory xhtml");
			return; 
		}
		
		String dataFilePath = args[0];
		String replaceDirectoryPath = args[1];
		fileExtension = args[2];
		
		try {
			// Load replace data
			loadReplaceData(dataFilePath);
		
			// Run directory processing
			File startingDirectory = new File(replaceDirectoryPath);
			if (startingDirectory.exists() && startingDirectory.isDirectory())
				processDirectory(startingDirectory);
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
