/**
 * 
 */
package org.abratuhi.snippettool.gui;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import org.abratuhi.snippettool.model.SnippetTool;

/**
 * @author silvestre The main GUI runner class.
 * 
 */
public class StartGUI {

	/**
	 * Starts the GUI
	 * 
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		OptionParser parser = new OptionParser();
		OptionSpec<String> propertiesfile = parser.accepts("propertiesfile")
				.withRequiredArg().ofType(String.class);
		OptionSet options = parser.parse(args);

		SnippetTool snippetTool = null;
		if (options.has(propertiesfile))
			snippetTool = new SnippetTool(options.valueOf(propertiesfile));
		else
			snippetTool = new SnippetTool();

		new _frame_SnippetTool(snippetTool).createAndShowGUI();
	}
}
