package org.abratuhi.snippettool.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.abratuhi.snippettool.gui._frame_SnippetTool;
import org.abratuhi.snippettool.util.DbUtil;
import org.abratuhi.snippettool.util.ErrorUtil;
import org.abratuhi.snippettool.util.FileUtil;
import org.abratuhi.snippettool.util.ImageUtil;
import org.abratuhi.snippettool.util.PrefUtil;
import org.abratuhi.snippettool.util.XMLUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnippetTool {
	private static final Logger logger = LoggerFactory
			.getLogger(SnippetTool.class);

	/** Default .properties file **/
	public final static String DEFAULT_PROPERTIES_FILE = "snippet-tool.properties";

	/** Default .preferences file **/
	public final static String PREFERENCES_FILE = "snippet-tool.preferences";

	/** Backup .preferences file **/
	public final static String BACKUP_PREFERENCES_FILE = "data/snippet-tool.preferences";

	/** Application is initialized together with an Inscript Object **/
	public Inscript inscript;
	public _frame_SnippetTool gui;

	public Properties props;
	public Properties prefs;

	public boolean existingSign = false;
	public double scale = 1.0;
	public double scaleFactor = 1.1;

	/**
	 * Creates a new SnipppetTool instance which uses the specified
	 * propertiesFile to initialize itself.
	 * 
	 * @param propertiesFile
	 *            the properties file to read
	 */
	public SnippetTool(String propertiesFile) {
		props = PrefUtil.loadProperties(propertiesFile);

		if (new File(PREFERENCES_FILE).canRead())
			prefs = PrefUtil.loadProperties(PREFERENCES_FILE);
		else
			prefs = PrefUtil.loadProperties(BACKUP_PREFERENCES_FILE);

		inscript = new Inscript();
		inscript.setFont(props.getProperty("local.font.file"));
	}

	/**
	 * Creates a new SnipppetTool instance which uses the default properties
	 * file to initialize itself.
	 */
	public SnippetTool() {
		this(DEFAULT_PROPERTIES_FILE);
	}

	public void setInscriptText(String mode, String collection, String resource) {
		String xsltFilename = props.getProperty("local.xslt.file");
		String inscriptText = null;

		if (mode.equals("remote")) {
			String user = props.getProperty("db.data.user");
			String password = props.getProperty("db.data.password");
			String xml_temp_dir = props.getProperty("local.inscript.dir");

			inscriptText = FileUtil.readStringFromFile(DbUtil
					.downloadXMLResource(collection, resource, user, password,
							xml_temp_dir));
		} else if (mode.equals("local")) {
			if (!collection.endsWith(File.separator))
				collection += File.separator;
			if (resource.startsWith(File.separator))
				resource = resource.substring(1);

			inscriptText = FileUtil.readStringFromFile(collection + resource);
		}

		String xsltText = FileUtil.readStringFromFile(xsltFilename);
		String transformedInscriptText = XMLUtil.transformXML(inscriptText,
				xsltText);
		String standardizedText = XMLUtil
				.standardizeXML(transformedInscriptText);

		if (logger.isDebugEnabled()) {
			FileUtil.writeXMLStringToFile(new File("1.xml"), inscriptText);
			FileUtil.writeXMLStringToFile(new File("2.xml"),
					transformedInscriptText);
			FileUtil.writeXMLStringToFile(new File("3.xml"), standardizedText);
		}

		inscript.setId(resource.substring(0, resource.length()
				- ".xml".length()));
		inscript.setPath(collection + resource);
		inscript.setTextFromXML(standardizedText);
	}

	public void setInscriptImageToLocalFile(File file) {
		inscript.setAbsoluteRubbingPath(file.getPath());
		inscript.loadLocalImage(file);
		scale = 1.0f;
	}

	public void setInscriptImageToRemoteRessource(String url) {
		String user = props.getProperty("db.data.user");
		String password = props.getProperty("db.data.password");
		String image_temp_dir = props.getProperty("local.image.dir");

		String collection = url.substring(0, url.lastIndexOf("/"));
		String resource = url.substring(url.lastIndexOf("/") + 1);

		try {
			File image = DbUtil.downloadBinaryResource(collection, resource,
					user, password, image_temp_dir);
			inscript.loadLocalImage(image);
			inscript.setAbsoluteRubbingPath(url);
			scale = 1.0f;
		} catch (IOException e) {
			logger.error("IOException occurred in "
					+ "setInscriptImageToRemoteRessource", e);
			ErrorUtil.showError(gui, "I/O error while loading image", e);
		}

	}

	public void updateInscriptImagePathFromAppearances(String mode) {
		if (mode.equals("remote")) {
			String collection = props.getProperty("db.unicode.dir");
			String user = props.getProperty("db.unicode.user");
			String password = props.getProperty("db.unicode.password");
			String query = "//appearance[contains(@id, '" + inscript.getId()
					+ "_')]/rubbing/text()";

			String[] paths = DbUtil.convertResourceSetToStrings(DbUtil
					.executeQuery(collection, user, password, query));
			if (paths.length > 0) {
				String rubbingPath = paths[0];
				if (rubbingPath.startsWith("xmldb:")) {
					logger.warn("Rubbing path {} is absolute.", rubbingPath);
					rubbingPath = rubbingPath.replaceFirst("xmldb:.*?/db/",
							props.getProperty("db.data.uri"));
					logger.debug("Mapping to {}", rubbingPath);
				} else {
					rubbingPath = props.getProperty("db.data.uri")
							+ rubbingPath;
				}
				inscript.setAbsoluteRubbingPath(rubbingPath);
			}
		} else if (mode.equals("local")) {

		}
	}

	public void updateInscriptCoordinates(String mode) {
		if (mode.equals("remote")) {
			String collection = props.getProperty("db.unicode.dir");
			String user = props.getProperty("db.unicode.user");
			String password = props.getProperty("db.unicode.password");
			String query = "//appearance[source='" + inscript.getId()
					+ "'][@variant='0']";

			Element[] appearances = DbUtil.convertResourceSetToElements(DbUtil
					.executeQuery(collection, user, password, query));
			inscript.updateCoordinates(appearances);
		} else if (mode.equals("local")) {

		}
	}

	public void submitInscript() {
		submitInscriptCoordinates();
		submitInscriptSnippets("snippet");
	}

	public void submitInscriptCoordinates() {
		String uri = props.getProperty("db.unicode.uri");
		String collection = props.getProperty("db.unicode.dir");
		String user = props.getProperty("db.unicode.user");
		String password = props.getProperty("db.unicode.password");

		XMLUtil.clearAppearances(user, password, collection, inscript.getId());
		XMLUtil.updateXML(inscript.getXUpdate("/db/"
				+ collection.substring(uri.length())), user, password,
				collection);
	}

	public void submitInscriptSnippets(String snippetBasename) {
		String collection = props.getProperty("db.snippet.dir");
		String user = props.getProperty("db.snippet.user");
		String password = props.getProperty("db.snippet.password");

		String snippetdir = props.getProperty("local.snippet.dir");
		String imagedir = props.getProperty("local.image.dir");

		if (!snippetdir.endsWith(File.separator))
			snippetdir += File.separator;
		if (!imagedir.endsWith(File.separator))
			imagedir += File.separator;

		File inputImageFile = new File(imagedir + inscript.getId() + ".png");

		ArrayList<InscriptCharacter> preferredReading = new ArrayList<InscriptCharacter>();
		for (int i = 0; i < inscript.getText().size(); i++) {
			preferredReading.add(inscript.getText().get(i).get(0).get(0));
		}

		ImageUtil.store(inscript.getImage(), "PNG", inputImageFile);
		File[] preferredSnippets;
		try {
			preferredSnippets = ImageUtil.cutSnippets(inputImageFile,
					preferredReading, snippetdir, "subimage");

			DbUtil.uploadBinaryResources(preferredSnippets, collection, user,
					password);
			for (int i = 0; i < preferredSnippets.length; i++) {
				inscript.updatePathToSnippet(preferredSnippets[i].getName(), i);
			}
		} catch (IOException e) {
			logger.error("I/O error while cutting snippets", e);
			ErrorUtil.showError(gui, "I/O error while cutting snippets", e);
		}
	}

	public void clearInscript() {
		inscript.clear();
	}

	public void saveLocal() {
		saveLocalCoordinates();
		saveLocalSnippets("tcut");
	}

	public void saveLocalCoordinates() {
		String unicodedir = props.getProperty("local.unicode.dir");
		if (!unicodedir.endsWith(File.separator))
			unicodedir += File.separator;

		Document document = new Document(new Element("inscript").setAttribute(
				"id", inscript.getId()).setAttribute("xml", inscript.getPath())
				.setAttribute("img", inscript.getAbsoluteRubbingPath()));

		for (int i = 0; i < inscript.getText().size(); i++) {
			for (int j = 0; j < inscript.getText().get(i).size(); j++) {
				for (int k = 0; k < inscript.getText().get(i).get(j).size(); k++) {
					InscriptCharacter csign = inscript.getText().get(i).get(j)
							.get(k);
					document.getRootElement().addContent(csign.toAppearance());
				}
			}
		}

		FileUtil.writeXMLDocumentToFile(new File(unicodedir + "tmarking_"
				+ inscript.getId() + ".xml"), document);
	}

	public void saveLocalSnippets(String snippetBasename) {
		String snippetdir = props.getProperty("local.snippet.dir");
		String imagedir = props.getProperty("local.image.dir");

		if (!snippetdir.endsWith(File.separator))
			snippetdir += File.separator;
		if (!imagedir.endsWith(File.separator))
			imagedir += File.separator;

		File inputImageFile = new File(imagedir + inscript.getId() + ".png");

		ArrayList<InscriptCharacter> preferredReading = new ArrayList<InscriptCharacter>();
		for (int i = 0; i < inscript.getText().size(); i++) {
			preferredReading.add(inscript.getText().get(i).get(0).get(0));
		}

		ImageUtil.store(inscript.getImage(), "PNG", inputImageFile);
		try {
			ImageUtil.cutSnippets(inputImageFile, preferredReading, snippetdir,
					snippetBasename);
		} catch (IOException e) {
			logger.error("I/O error while cutting snippets", e);
			JOptionPane.showMessageDialog(gui,
					"I/O error while cutting snippets: "
							+ e.getLocalizedMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	@SuppressWarnings("unchecked")
	public void loadLocal(File f) {
		if (f == null)
			return;

		Document document = FileUtil.readXMLDocumentFromFile(f);
		Element documentRootElement = document.getRootElement();
		String xml = documentRootElement.getAttributeValue("xml");
		String img = documentRootElement.getAttributeValue("img");

		// TODO: Remove use of local/remote method strings
		String xml_collection = null;
		String xml_resource = xml;
		String xml_method = "local";

		if (xml.startsWith("xmldb:")) {
			xml_collection = xml.substring(0, xml.lastIndexOf("/"));
			xml_resource = xml.substring(xml.lastIndexOf("/") + 1);
			xml_method = "remote";
		}

		setInscriptText(xml_method, xml_collection, xml_resource);

		if (img.startsWith("xmldb:")) {
			setInscriptImageToRemoteRessource(img);
		} else {
			setInscriptImageToLocalFile(new File(img));
		}
		List<Element> apps = documentRootElement.getChildren("appearance");
		inscript.updateCoordinates(apps.toArray(new Element[apps.size()]));

	}

	public void exit() {
		// PrefUtil.saveProperties(props, propfile);
		PrefUtil.saveProperties(prefs, PREFERENCES_FILE);
		System.exit(0);
	}

}
