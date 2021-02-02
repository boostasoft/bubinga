package com.boostasoft.goaway.tilingDB.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.boostasoft.goaway.tilingDB.datastructure.RTreeIndex;
import com.boostasoft.goaway.tilingDB.datastructure.RtreeMultiLevelIndex;
import com.boostasoft.goaway.tilingDB.datastructure.management.RtreeMultiLevelException;
import com.boostasoft.goaway.tilingDB.datastructure.management.ScaleInfo;

/**
 * @author Dtsatcha
 *
 */
public class FileUtil {

	private static Logger log = LoggerFactory.getLogger(FileUtil.class);

	private static final String[] DOS_DEVICE_NAMES = { "AUX", "CLOCK$", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6",
			"COM7", "COM8", "COM9", "CON", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9",
			"NUL", "PRN" };

	private static final char[] INVALID_URL_CHARS = { '*', ':', '<', '>', '?', '\\', '/', '"', '|' };

	protected static final String Directory = "C:"+ File.separatorChar + "Dieudonne_2018" +  File.separatorChar + "Environnement_dev" + File.separatorChar + "projets" + File.separatorChar+ "codes"
			 +File.separatorChar+"goaway"+File.separatorChar
			 +"src"+File.separatorChar+"main"+File.separatorChar +"resources"+File.separatorChar+"templates"+File.separatorChar+"data";

	/**
	 * Encodes a string into a valid file name. We replace any invalid
	 * characters with an underscore.
	 *
	 * @param s
	 *            a string to encode.
	 * @return A string that can be used as a valid filename.
	 */
	public static String encodeStringForFilename(String s) {
		// Some helpful comments here:
		// http://stackoverflow.com/questions/62771/how-check-if-given-string-is-legal-allowed-file-name-under-windows

		// These DOS device names are not allowed
		for (String dos : DOS_DEVICE_NAMES) {
			if (s.trim().equalsIgnoreCase(dos)) {
				return "_" + s.trim();
			}
		}

		StringBuffer sb = new StringBuffer(s.length());
		for (int i = 0; i < s.length(); ++i) {
			char c = s.charAt(i);
			int cInt = c;

			boolean isInvalidChar = false;
			for (char invalidChar : INVALID_URL_CHARS) {
				if (c == invalidChar) {
					isInvalidChar = true;
					break;
				}
			}

			if ((cInt >= 0x0 && cInt <= 0x1F) || isInvalidChar) {
				sb.append('_');
			} else {
				sb.append(c);
			}
		}

		return sb.toString();
	}

	/**
	 * Delete a file. If the file is a directory, recursively delete all files
	 * in the directory.
	 *
	 * @param f
	 *            a file to delete.
	 */
	public static boolean delete(File f) {
		boolean success = true;
		if (f.isDirectory()) {
			for (File file : f.listFiles()) {
				success = success && delete(file);
			}
		}

		if (success) {
			if (f.exists()) {
				try {
					Path path = FileSystems.getDefault().getPath(f.getPath());
					Files.delete(path);
				} catch (IOException e) {
					log.warn("Error deleting '{}': {}", f, e.getMessage());
				}
			} else {
				log.info("Delete was called on a nonexistent file/directory of path ({}), ignoring call", f);
			}
		}
		return success;
	}

	public static Map<Integer, String[] > readInfo(String indexInfo, String sep, String categorie) {
		BufferedReader ficTexte = null;
		String filePath = Directory + File.separatorChar + indexInfo;
		String theLine;
		String[] containByLine;
		ScaleInfo scaleInfo;
		Map<Integer, String[] > categories = new HashMap<Integer, String[] >();
		int i=0;
		final int fin=5;

		try {
			ficTexte = new BufferedReader(new FileReader(new File(filePath)));
			do {
				theLine = ficTexte.readLine();
				if (theLine != null) {

					// on segemente les elements de la ligne en fonction des
					// espaces
					containByLine = theLine.split("\\"+sep);
					if (containByLine != null) {
						// on n a pas besoin de renseigner
						// le nameScale car la classe sert le contruire
						//System.out.println(containByLine[0]);
						
						if (containByLine[3].equals(categorie) && i>0){
							categories.put(i, containByLine);
							System.out.println(containByLine[3]);


							
						}
						// A retirer à  la longue....
						


						// }
					}

				}
				i++;
			
				
			} while (theLine != null);
			// System.out.println(ligne);
			ficTexte.close();
			System.out.println("\n");
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return categories;
	}
	
	
	
	public static Map<Integer, String[] > readInfo(String indexInfo, String sep, String categorie, String dataPath) {
		BufferedReader ficTexte = null;
		String filePath = dataPath + File.separatorChar + indexInfo;
		String theLine;
		String[] containByLine;
		ScaleInfo scaleInfo;
		Map<Integer, String[] > categories = new HashMap<Integer, String[] >();
		int i=0;
		final int fin=5;

		try {
			ficTexte = new BufferedReader(new FileReader(new File(filePath)));
			do {
				theLine = ficTexte.readLine();
				if (theLine != null) {

					// on segemente les elements de la ligne en fonction des
					// espaces
					containByLine = theLine.split("\\"+sep);
					if (containByLine != null) {
						// on n a pas besoin de renseigner
						// le nameScale car la classe sert le contruire
						//System.out.println(containByLine[0]);
						
						if (containByLine[3].equals(categorie) && i>0){
							categories.put(i, containByLine);
							System.out.println(containByLine[3]);


							
						}
						// A retirer à  la longue....
						


						// }
					}

				}
				i++;
			
				
			} while (theLine != null);
			// System.out.println(ligne);
			ficTexte.close();
			System.out.println("\n");
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return categories;
	}

	
	public static void replaceContent(String indexInfo, String currentName, String nextName, String Directory) {

		// on verifie que le contenu des adresses en mémoire est non null
		String filePath = Directory + File.separatorChar + indexInfo;
		BufferedReader ficTexte = null;

		String theLine;
		String[] containByLine;
		ScaleInfo scaleInfo;

		try {
			ficTexte = new BufferedReader(new FileReader(new File(filePath)));
			do {
				theLine = ficTexte.readLine();
				if (theLine != null) {
					theLine.replaceAll(currentName, nextName);
				}
			} while (theLine != null);
			// System.out.println(ligne);
			ficTexte.close();
			System.out.println("\n");
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

}
