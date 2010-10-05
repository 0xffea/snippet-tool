package org.stonesutras.snippettool.model;

import org.stonesutras.snippettool.util.GetOpts;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Database;

public class ImageCutter {

	public SnippetTool snippettool;
	public GetOpts options;

	public ImageCutter() {
		snippettool = new SnippetTool();
	}

	public String getUsage() {
		return new String(
				"usage: java -jar imagecutter.jar -inscript [inscript_uri] -rubbing [rubbing_uri] -savemode [remote/local] -basename [snippet_basename]");
	}

	public static void main(String[] args) throws Exception {
		Database database = (Database) Class.forName("org.exist.xmldb.DatabaseImpl").newInstance();
		DatabaseManager.registerDatabase(database);

		ImageCutter imagecutter = new ImageCutter();
		imagecutter.options = new GetOpts(args);

		String inscript_uri = imagecutter.options.getOpt("inscript");
		String rubbing_uri = imagecutter.options.getOpt("rubbing");
		String mode = imagecutter.options.getOpt("savemode");
		String basename = imagecutter.options.getOpt("basename");

		if (inscript_uri != null && rubbing_uri != null && mode != null && basename != null) {
			imagecutter.snippettool.loadInscriptTextFromRemoteResource(inscript_uri.substring(0, inscript_uri
					.lastIndexOf("/")), inscript_uri.substring(inscript_uri.lastIndexOf("/") + 1));
			imagecutter.snippettool.setInscriptImageToRemoteRessource(rubbing_uri);
			imagecutter.snippettool.updateInscriptCoordinates();
			if (mode.equals("remote")) {
				imagecutter.snippettool.submitInscriptSnippets(basename);
			} else if (mode.equals("local")) {
				imagecutter.snippettool.saveLocalSnippets(basename);
			}
		} else {
			System.out.println(imagecutter.getUsage());
		}
	}

}
