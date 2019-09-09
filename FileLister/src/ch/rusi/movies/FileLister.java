package ch.rusi.movies;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class FileLister {
	
	private static String FILE_NAME_TIME = "files_time.txt";
	private static String FILE_NAME_ALPHA = "files_alpha.txt";

	private SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss"); 
	private List<File> fileList = new ArrayList<File>();
	
	public static void main(String[] args) {
		List<String> pathList = new ArrayList<String>();
		boolean deepScan = false;
		
		for (int i = 0 ; i < args.length ; i++) {
			String param = args[i];
			if (param.startsWith("/")) {
				// Path parameter
				pathList.add(param);
			} else if (param.equals("-deep")) {
				deepScan = true;
			} else {
				System.out.println(FileLister.class.getName() + " - Invalid parameter '" + param + "'!");
				return;
			}
		}
		
		if (pathList.size() == 0) {
			System.out.println(FileLister.class.getName() + " - Path parameter is missing!");
			return;
		}
		
		FileLister process = new FileLister();
		for (String path : pathList) {
			process.generateFileIndex(path, deepScan);
		}
	}
	
	private void generateFileIndex(String path, boolean deepScan) {
		fileList.clear();
		File fileDirectory = new File(path);
		listFiles(fileDirectory);
		Collections.sort(fileList, new Comparator<File>() {
			public int compare(File f1, File f2) {
				return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
			}
		});
		writeFile(fileDirectory, FILE_NAME_TIME);
		Collections.sort(fileList, new Comparator<File>() {
			public int compare(File f1, File f2) {
				return f1.getName().compareTo(f2.getName());
			}
		});
		writeFile(fileDirectory, FILE_NAME_ALPHA);
		
	}
	
	private void writeFile(File directory, String outFileName) {
		
		File outFile;
		try {
			
			outFile = new File(directory + "/" + outFileName);
			FileWriter out = new FileWriter(outFile);
			
			// Find largest file name
			int lenFile = 0;
			int lenPath = 4;
			for (File file : fileList) {
				if (file.getName().length() > lenFile) {
					lenFile = file.getName().length();
				}
				if (getRelPath(file, directory).length() > lenPath) {
					lenPath = getRelPath(file, directory).length();
				}
			}
			lenFile += 1;
			lenPath += 1;
			
			out.write(padRight("File", lenFile));
			out.write(padRight("Path", lenPath));
			out.write("Timestamp\n");
			out.write(padRight("----", lenFile));
			out.write(padRight("----", lenPath));
			out.write("---------\n");
			
			for (File file : fileList) {
				if (!FILE_NAME_ALPHA.equals(file.getName()) && !FILE_NAME_TIME.equals(file.getName()) && !file.getName().startsWith(".")) {
					out.write(file.getName());
					for (int i = file.getName().length() ; i <= lenFile ; i++) {
						out.write(" ");
					}
					out.write(getRelPath(file, directory));
					for (int i = getRelPath(file, directory).length() ; i <= lenPath ; i++) {
						out.write(" ");
					}
					out.write(df.format(new Date(file.lastModified())));
					out.write("\n");
				}
			}
			out.close();
			System.out.println(this.getClass().getName() + " - File list successfully written to " + outFile.getAbsolutePath());
		} catch (Exception e) {
			System.out.println(this.getClass().getName() + " - File " + "" + " could not be written!");
			e.printStackTrace();
		}
		
	}
	
	private String getRelPath(File file, File directory) {
		String relPath = file.getParent().replace(directory.getAbsolutePath(), "");
		if (relPath.length() == 0) {
			relPath = "/";
		}
		return relPath;
	}
	
	private String padRight(String s, int n) {
	     return String.format("%1$-" + n + "s", s);  
	}
	
	private void listFiles(File file) {
		if (file.isFile()) {
			System.out.println("[FILE] " + file.getName());
			if (!file.getName().startsWith(".")) {
				fileList.add(file);
			}
		} else if (file.isDirectory()) {
			System.out.println("[DIR] " + file.getName());
			File[] fileList = file.listFiles();
			for (File subFile : fileList) {
				listFiles(subFile);
			}
		}
	}

}
