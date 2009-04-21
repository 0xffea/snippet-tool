package org.abratuhi.snippettool.model;

import org.abratuhi.snippettool.util.GetOpts;

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

	public static void main(String[] args) {
		ImageCutter imagecutter = new ImageCutter();
		imagecutter.options = new GetOpts(args);

		String inscript_uri = imagecutter.options.getOpt("inscript");
		String rubbing_uri = imagecutter.options.getOpt("rubbing");
		String mode = imagecutter.options.getOpt("savemode");
		String basename = imagecutter.options.getOpt("basename");

		if (inscript_uri != null && rubbing_uri != null && mode != null
				&& basename != null) {
			imagecutter.snippettool.setInscriptText("remote", inscript_uri
					.substring(0, inscript_uri.lastIndexOf("/")), inscript_uri
					.substring(inscript_uri.lastIndexOf("/") + 1));
			imagecutter.snippettool
					.setInscriptImageToRemoteRessource(rubbing_uri);
			imagecutter.snippettool.updateInscriptCoordinates("remote");
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
