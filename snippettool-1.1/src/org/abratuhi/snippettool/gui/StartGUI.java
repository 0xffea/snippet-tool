/**
 * 
 */
package org.abratuhi.snippettool.gui;

import javax.swing.JOptionPane;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import org.abratuhi.snippettool.model.SnippetTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Database;

/**
 * The main GUI runner class.
 * 
 * @author silvestre
 * 
 */
public class StartGUI {

	private static final Logger logger = LoggerFactory
			.getLogger(StartGUI.class);

	/**
	 * Starts the GUI
	 * 
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		try {

			Database database = (Database) Class.forName(
					"org.exist.xmldb.DatabaseImpl").newInstance();
			DatabaseManager.registerDatabase(database);

			OptionParser parser = new OptionParser();
			OptionSpec<String> propertiesfile = parser
					.accepts("propertiesfile").withRequiredArg().ofType(
							String.class);
			OptionSet options = parser.parse(args);

			SnippetTool snippetTool = null;
			if (options.has(propertiesfile))
				snippetTool = new SnippetTool(options.valueOf(propertiesfile));
			else
				snippetTool = new SnippetTool();

			new _frame_SnippetTool(snippetTool).createAndShowGUI();
		} catch (Exception e) {
			logger.error("Exception occured", e);
			JOptionPane.showMessageDialog(null, e.getLocalizedMessage(),
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}
