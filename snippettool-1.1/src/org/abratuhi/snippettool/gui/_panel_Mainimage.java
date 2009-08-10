package org.abratuhi.snippettool.gui;

import java.awt.BorderLayout;
import java.util.Properties;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.abratuhi.snippettool.controller._controller_AutoGuided;
import org.abratuhi.snippettool.controller._controller_Keyboard;
import org.abratuhi.snippettool.model.SnippetTool;

/**
 * Snippet-Tool main_image component, subcomponent of Snippet-tool's main
 * component. Holds image and attached mouse listeners for marking the inscript.
 * 
 * @author Alexei Bratuhin
 * 
 */
@SuppressWarnings("serial")
public class _panel_Mainimage extends JPanel {

	/** Reference to parent component **/
	public _frame_SnippetTool root;

	SnippetTool snippettool;
	Properties preferences;

	/** Scroll component holding the image pane **/
	public final JScrollPane scroll_image;

	public final MainImageCanvas imageCanvas;

	public _panel_Mainimage(_frame_SnippetTool jf, SnippetTool snippettool) {
		super();
		setLayout(new BorderLayout());
		setFocusable(true);

		this.root = jf;
		this.snippettool = snippettool;
		this.preferences = this.root.preferences;

		imageCanvas = new MainImageCanvas(snippettool, preferences);

		scroll_image = new JScrollPane(imageCanvas);

		add(scroll_image, BorderLayout.CENTER);

		_controller_AutoGuided mouse1 = new _controller_AutoGuided(this);
		imageCanvas.setMouseListener(mouse1);
		imageCanvas.setMouseMotionListener(mouse1);
		addKeyListener(new _controller_Keyboard(this));

	}
}
