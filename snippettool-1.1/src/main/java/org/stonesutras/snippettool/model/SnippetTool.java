package org.stonesutras.snippettool.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.prefs.Preferences;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.jdom.Document;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stonesutras.snippettool.gui._frame_SnippetTool;
import org.stonesutras.snippettool.util.DbUtil;
import org.stonesutras.snippettool.util.ErrorUtil;
import org.stonesutras.snippettool.util.FileUtil;
import org.stonesutras.snippettool.util.XMLUtil;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.XMLDBException;

import com.Ostermiller.util.PasswordDialog;

public class SnippetTool extends Observable {
	private static final Logger logger = LoggerFactory.getLogger(SnippetTool.class);

	/** Application is initialized together with an Inscript Object **/
	public Inscript inscript;
	public _frame_SnippetTool gui;

	public boolean existingSign = false;
	private double scale = 1.0;
	public double scaleFactor = 1.1;

    public final Preferences prefs2;

	/**
	 * Creates a new SnipppetTool instance which uses the specified
	 * propertiesFile to initialize itself.
	 *
	 * @param propertiesFile
	 *            the properties file to read
	 */
	public SnippetTool(String propertiesFile) {
        prefs2 = Preferences.userNodeForPackage(getClass());

        try {
            if (!prefs2.getBoolean("configured", false)) {
                prefs2.put("db.data.uri", "xmldb:exist://stonesutras.org:8080/exist/xmlrpc/db/");
                prefs2.put("db.data.dir", "xmldb:exist://stonesutras.org:8080/exist/xmlrpc/db/docs");
                prefs2.put("db.snippet.uri", "xmldb:exist://stonesutras.org:7000/exist/xmlrpc/db/");
                prefs2.put("db.snippet.dir", "xmldb:exist://stonesutras.org:7000/exist/xmlrpc/db/snippets");
                prefs2.put("db.unicode.uri", "xmldb:exist://stonesutras.org:7000/exist/xmlrpc/db/");
                prefs2.put("db.unicode.dir", "xmldb:exist://stonesutras.org:7000/exist/xmlrpc/db/unicode");

                String username = "admin";
                String password = "";

                PasswordDialog p = new PasswordDialog(null, "Login to stonesutras.org");

                while (!validCredentials(username, password)) {

                    if (p.showDialog()) {

                        username = p.getName();
                        password = p.getPass();

                        prefs2.put("db.data.user", p.getName());
                        prefs2.put("db.data.password", p.getPass());
                        prefs2.put("db.snippet.user", p.getName());
                        prefs2.put("db.snippet.password", p.getPass());
                        prefs2.put("db.unicode.user", p.getName());
                        prefs2.put("db.unicode.password", p.getPass());

                    } else {
                        logger.error("User did not supply credentials.");
                        System.exit(-3);
                    }
                }

                prefs2.put("local.xslt.file", "data/xslt/extract-plain-mod.xml");
                prefs2.put("local.font.file", "data/font/ARIALUNI.TTF");
                prefs2.putInt("local.snippet.size.min", 50);

                final String baseTempPath = System.getProperty("java.io.tmpdir");
                File tempDir = new File(baseTempPath + File.separator + "snippettool");
                if (tempDir.exists() == false) {
                    tempDir.mkdir();
                }

                prefs2.put("local.inscript.dir", tempDir.getCanonicalPath() + File.separator + "/inscript");
                prefs2.put("local.image.dir", tempDir.getCanonicalPath() + File.separator + "/image");
                prefs2.put("local.snippet.dir", tempDir.getCanonicalPath() + File.separator + "/snippet");
                prefs2.put("local.unicode.dir", tempDir.getCanonicalPath() + File.separator + "/unicode");
                prefs2.put("local.xml.dir", tempDir.getCanonicalPath());

                prefs2.put("local.window.position", "0,25");
                prefs2.put("local.window.text.size", "775x47");
                prefs2.put("local.color.rubbing", "ffffff");
                prefs2.put("local.window.options.size", "265x582");
                prefs2.putFloat("local.alpha.marking.p", 0.0f);
                prefs2.put("local.window.info.size", "215x47");
                prefs2.put("local.window.size", "1004x699");
                prefs2.put("local.color.marking.border", "c0c0c0");
                prefs2.putInt("local.window.divider.width", 8);
                prefs2.put("local.color.text", "fff847");
                prefs2.putFloat("local.alpha.marking.border", 0.75f);
                prefs2.putFloat("local.alpha.text", 0.73f);
                prefs2.put("local.window.status.size", "998x15");
                prefs2.put("local.window.main.size", "484x582");
                prefs2.put("local.color.marking.p", "c0c0c0");
                prefs2.putFloat("local.alpha.marking.a", 0.41f);
                prefs2.put("local.color.marking.a", "00ff");
                prefs2.put("local.window.explorer.size", "233x582");
                prefs2.putFloat("local.alpha.rubbing", 1.0f);

                prefs2.putBoolean("configured", true);
            }
        } catch (IOException e) {
            logger.error("Could not create tempdir", e);
            System.exit(-2);
        }

		inscript = new Inscript();
        inscript.setFont(getInputStreamFromFileOrResource(prefs2.get("local.font.file", "")));
	}

    /**
     * Checks if username and password are valid.
     *
     * @param username
     * @param password
     */
    private boolean validCredentials(String username, String password) {
        try {
            org.xmldb.api.base.Collection c = DatabaseManager.getCollection(prefs2.get("db.data.uri", ""), username,
                password);
            c.listChildCollections();
        } catch (XMLDBException e) {
                return false;
        }
        return true;
    }

	/**
	 * Creates a new SnipppetTool instance which uses the default properties
	 * file to initialize itself.
	 */
	public SnippetTool() {
        this(null);
	}

    public void exit() {
        System.exit(0);
    }

	public void loadInscriptTextFromRemoteResource(String collection, String resource) throws Exception {
        String user = prefs2.get("db.data.user", "");
        String password = prefs2.get("db.data.password", "");
        String xml_temp_dir = prefs2.get("local.inscript.dir", "");

		inscript.setId(resource.substring(0, resource.length() - ".xml".length()));
		inscript.setPath(collection + resource);

		setInscriptText(DbUtil.downloadXMLResource(collection, resource, user, password, xml_temp_dir));

	}

	public void loadInscriptTextFromLocalFile(File file) throws Exception {
		String name = file.getName();
		inscript.setId(name.substring(0, name.length() - ".xml".length()));
		inscript.setPath(file.getCanonicalPath());

		setInscriptText(file);
	}

    private InputStream getInputStreamFromFileOrResource(String name) {
        try {
            return new FileInputStream(new File(name));
        } catch (FileNotFoundException e) {
            ClassLoader cl = this.getClass().getClassLoader();
            return cl.getResourceAsStream(name);
        }
    }

	private void setInscriptText(final File inscriptFile) throws Exception {
		logger.debug("Loading inscript text from {}", inscriptFile);

        String xsltFilename = prefs2.get("local.xslt.file", "");

		TransformerFactory tFactory = TransformerFactory.newInstance();
        Source xslSource = new StreamSource(getInputStreamFromFileOrResource(xsltFilename));

		Transformer transformer = tFactory.newTransformer(xslSource);

		StringWriter sw = new StringWriter();
		transformer.transform(new StreamSource(inscriptFile), new StreamResult(sw));

		String transformedInscriptText = sw.toString();

        File tempDir = new File(prefs2.get("local.xml.dir", "."));

		if (logger.isDebugEnabled()) {
            FileUtil.writeXMLStringToFile(new File(tempDir, "transformedInscriptText.xml"), transformedInscriptText);
		}

		String standardizedText = XMLUtil.standardizeXML(transformedInscriptText);

		if (logger.isDebugEnabled()) {
            FileUtil.writeXMLStringToFile(new File(tempDir, "standardizedText.xml"), standardizedText);
		}

		inscript.setTextFromXML(standardizedText);
	}

	public void setInscriptImageToLocalFile(File file) {
		inscript.setAbsoluteRubbingPath(file.getPath());
		inscript.loadLocalImage(file);
		setScale(1.0f);
	}

	public void setInscriptImageToRemoteRessource(String url) {
        String user = prefs2.get("db.data.user", "");
        String password = prefs2.get("db.data.password", "");
        String image_temp_dir = prefs2.get("local.image.dir", "");

		String collection = url.substring(0, url.lastIndexOf("/"));
		String resource = url.substring(url.lastIndexOf("/") + 1);

		try {
			File image = DbUtil.downloadBinaryResource(collection, resource, user, password, image_temp_dir);
			inscript.loadLocalImage(image);
			inscript.setAbsoluteRubbingPath(url);
			setScale(1.0f);
		} catch (IOException e) {
			logger.error("IOException occurred in " + "setInscriptImageToRemoteRessource", e);
			ErrorUtil.showError(gui, "I/O error while loading image", e);
		}

	}

	public void updateInscriptImagePathFromAppearances() {
        String collection = prefs2.get("db.unicode.dir", "");
        String user = prefs2.get("db.unicode.user", "");
        String password = prefs2.get("db.unicode.password", "");
		String query = "//appearance[contains(@id, '" + inscript.getId() + "_')]/rubbing/text()";

		String[] paths = DbUtil.convertResourceSetToStrings(DbUtil.executeQuery(collection, user, password, query));
		if (paths.length > 0) {
			String rubbingPath = paths[0];
			if (rubbingPath.startsWith("xmldb:")) {
				logger.warn("Rubbing path {} is absolute.", rubbingPath);
                rubbingPath = rubbingPath.replaceFirst("xmldb:.*?/db/", prefs2.get("db.data.uri", ""));
				logger.debug("Mapping to {}", rubbingPath);
			} else {
                rubbingPath = prefs2.get("db.data.uri", "") + rubbingPath;
			}
			inscript.setAbsoluteRubbingPath(rubbingPath);
		}
	}

	public void updateInscriptCoordinates() {
        String collection = prefs2.get("db.unicode.dir", "");
        String user = prefs2.get("db.unicode.user", "");
        String password = prefs2.get("db.unicode.password", "");
		String query = "//appearance[source='" + inscript.getId() + "'][@variant='0']";

		Element[] appearances = DbUtil.convertResourceSetToElements(DbUtil.executeQuery(collection, user, password,
				query));
		inscript.updateCoordinates(appearances);
	}

	public void submitInscript() {
		submitInscriptSnippets("snippet");
		submitInscriptCoordinates();
	}

	private void submitInscriptCoordinates() {
        String uri = prefs2.get("db.unicode.uri", "");
        String collection = prefs2.get("db.unicode.dir", "");
        String user = prefs2.get("db.unicode.user", "");
        String password = prefs2.get("db.unicode.password", "");

		// XMLUtil.clearAppearances(user, password, collection,
		// inscript.getId());
		XMLUtil.updateXML(inscript.getXUpdate("/db/" + collection.substring(uri.length())), user, password, collection);
	}

	public void submitInscriptSnippets(String snippetBasename) {
        String collection = prefs2.get("db.snippet.dir", "");
        String user = prefs2.get("db.snippet.user", "");
        String password = prefs2.get("db.snippet.password", "");

		String snippetdir = prefs2.get("local.snippet.dir", "");

		if (!snippetdir.endsWith(File.separator))
			snippetdir += File.separator;

		File[] preferredSnippets;
		try {
			preferredSnippets = inscript.getPyramidalImage().cutSnippets(inscript.getPreferredReadingText(),
					snippetdir, "subimage");

			DbUtil.uploadBinaryResources(preferredSnippets, collection, user, password);
			for (int i = 0; i < preferredSnippets.length; i++) {
				File snippet = preferredSnippets[i];
				if (snippet != null) {
					inscript.updatePathToSnippet(snippet.getName(), i);
				}
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
        String unicodedir = prefs2.get("local.unicode.dir", "");
		if (!unicodedir.endsWith(File.separator))
			unicodedir += File.separator;

		Document document = new Document(new Element("inscript").setAttribute("id", inscript.getId()).setAttribute(
				"xml", inscript.getPath()).setAttribute("img", inscript.getAbsoluteRubbingPath()));

		for (int i = 0; i < inscript.getText().size(); i++) {
			for (int j = 0; j < inscript.getText().get(i).size(); j++) {
				for (int k = 0; k < inscript.getText().get(i).get(j).size(); k++) {
					InscriptCharacter csign = inscript.getText().get(i).get(j).get(k);
					document.getRootElement().addContent(csign.toAppearance());
				}
			}
		}

		FileUtil.writeXMLDocumentToFile(new File(unicodedir + "tmarking_" + inscript.getId() + ".xml"), document);
	}

	public void saveLocalSnippets(String snippetBasename) {
        String snippetdir = prefs2.get("local.snippet.dir", "");
        String imagedir = prefs2.get("local.image.dir", "");

		if (!snippetdir.endsWith(File.separator))
			snippetdir += File.separator;
		if (!imagedir.endsWith(File.separator))
			imagedir += File.separator;

		List<InscriptCharacter> preferredReading = new ArrayList<InscriptCharacter>();
		for (int i = 0; i < inscript.getText().size(); i++) {
			preferredReading.add(inscript.getText().get(i).get(0).get(0));
		}

		try {
			inscript.getPyramidalImage().cutSnippets(preferredReading, snippetdir, snippetBasename);
		} catch (IOException e) {
			logger.error("I/O error while cutting snippets", e);
			ErrorUtil.showError(gui, "I/O error while cutting snippets", e);
		}
	}

	@SuppressWarnings("unchecked")
	public void loadLocal(File f) throws Exception {
		if (f == null)
			return;

		Document document = FileUtil.readXMLDocumentFromFile(f);
		Element documentRootElement = document.getRootElement();
		String xml = documentRootElement.getAttributeValue("xml");
		String img = documentRootElement.getAttributeValue("img");

		if (xml.startsWith("xmldb:")) {
			String xml_collection = xml.substring(0, xml.lastIndexOf("/"));
			String xml_resource = xml.substring(xml.lastIndexOf("/") + 1);
			loadInscriptTextFromRemoteResource(xml_collection, xml_resource);
		} else {
			loadInscriptTextFromLocalFile(new File(xml));
		}

		if (img.startsWith("xmldb:")) {
			setInscriptImageToRemoteRessource(img);
		} else {
			setInscriptImageToLocalFile(new File(img));
		}
		List<Element> apps = documentRootElement.getChildren("appearance");
		inscript.updateCoordinates(apps.toArray(new Element[apps.size()]));

	}


	/**
	 * @param scale
	 *            the scale to set
	 */
	public void setScale(double scale) {
		this.scale = scale;
		setChanged();
		notifyObservers();
	}

	/**
	 * @return the scale
	 */
	public double getScale() {
		return scale;
	}

}
