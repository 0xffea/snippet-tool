package org.abratuhi.snippettool.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Collection of functions to handle different file I/O operations for 'all' and
 * 'xml' files.
 * 
 * @author Alexei Bratuhin
 * 
 */
public class FileUtil {


	public static void writeStringToFile(String file, String str) {
		try {
			FileOutputStream fos = new FileOutputStream(file);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos,
					"UTF-8"));
			bw.write(str);
			bw.flush();
			bw.close();
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void writeXMLStringToFile(File file, String str) {
		SAXBuilder saxb = new SAXBuilder();
		StringReader sr = new StringReader(str);
		try {
			Document d = saxb.build(sr);

			XMLOutputter xmlout = new XMLOutputter();
			xmlout.setFormat(Format.getPrettyFormat());
			xmlout.output(d, new FileOutputStream(file));
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Document readXMLDocumentFromFile(File file) {
		try {
			SAXBuilder builder = new SAXBuilder();
			Document d = builder.build(new FileInputStream(file));
			return d;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void writeXMLDocumentToFile(File file, Document document) {
		try {
			FileOutputStream fos = new FileOutputStream(file);
			XMLOutputter xmlout = new XMLOutputter();
			xmlout.setFormat(Format.getPrettyFormat());
			xmlout.output(document, fos);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the directory at tempdir name, creating it if did not exist.
	 * 
	 * @param tempdirName
	 * @return a file object representing the tempdir
	 * @throws IOException
	 */
	public static File getTempdir(String tempdirName) throws IOException {
		File tempdir = new File(tempdirName);
		if (!tempdir.isDirectory()) {
			if (tempdir.exists())
				throw new IllegalArgumentException("Supplied tempdirName \""
						+ tempdirName + "\" is not a directory");
			if (!tempdir.mkdirs())
				throw new IOException(
						"Could not create tempdir at tempdirName \""
								+ tempdirName + "\"");
		}
		return tempdir;
	}

}
