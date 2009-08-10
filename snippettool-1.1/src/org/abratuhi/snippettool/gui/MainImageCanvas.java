/**
 * @author silvestre
 *
 */
package org.abratuhi.snippettool.gui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

import org.abratuhi.snippettool.model.Inscript;
import org.abratuhi.snippettool.model.InscriptCharacter;
import org.abratuhi.snippettool.model.PyramidalImage;
import org.abratuhi.snippettool.model.SnippetTool;
import org.abratuhi.snippettool.util.ErrorUtil;
import org.abratuhi.snippettool.util.PrefUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author silvestre
 * 
 */
@SuppressWarnings("serial")
public class MainImageCanvas extends JComponentE implements Observer {

	/** The inscript to draw. */
	private final Inscript inscript;

	/** The logger. */
	private static final Logger logger = LoggerFactory
			.getLogger(MainImageCanvas.class);

	/** The preferences to use when drawing. */
	private final Properties preferences;

	/** The snippetTool dictating the scale factor to use. */
	private final SnippetTool snippetTool;

	/** The image to display. */
	private PyramidalImage image;

	/**
	 * Creates an MainImageCanvas displaying the image and text of the supplied
	 * inscript using the given preferences.
	 * 
	 * @param snippetTool
	 *            the snippet tool containg the scale factor and the inscript to
	 *            display
	 * @param preferences
	 *            the preferences to apply when drawing the image
	 */
	public MainImageCanvas(final SnippetTool snippetTool,
			final Properties preferences) {
		super();
		this.snippetTool = snippetTool;
		this.inscript = snippetTool.inscript;
		this.preferences = preferences;
		snippetTool.addObserver(this);
		inscript.addObserver(this);
		updateSize();
	}

	/**
	 * Recalculates the preferred size of the component based on the scale
	 * factor and the base image size.
	 */
	private void updateSize() {
		image = inscript.getPyramidalImage();
		if (image != null) {
			try {
				setPreferredSize(image.getDimension(snippetTool.getScale()));
				revalidate();
				repaint();
			} catch (IOException e) {
				logger.error("IOException occurred in update", e);
				ErrorUtil.showError(this, "Error concerning image:", e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(final Observable o, final Object arg) {
		updateSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(final Graphics g) {
		super.paintComponent(g);
		Graphics2D gg = (Graphics2D) g;

		// load paint properties
		Color rubbingColor = PrefUtil.String2Color(preferences
				.getProperty("local.color.rubbing"));
		Float rubbingAlpha = Float.parseFloat(preferences
				.getProperty("local.alpha.rubbing"));

		// draw background
		gg.setBackground(rubbingColor);
		gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				rubbingAlpha));

		if (image != null) {
			try {
				image.drawImage(snippetTool.getScale(), g);
			} catch (IOException e) {
				logger.error("IOException occurred in paintComponent", e);
				ErrorUtil.showError(this, "Error painting image:", e);
				image = null; // Stop drawing the image if an error occurred.
			}
		}
		// draw marking
		gg.scale(snippetTool.getScale(), snippetTool.getScale());
		for (int i = 0; i < inscript.getText().size(); i++) {
			InscriptCharacter sign = inscript.getText().get(i).get(0).get(0);
			if (sign.shape.base.width > 0
					&& sign.shape.base.height > 0
					&& gg.hitClip(sign.shape.base.x, sign.shape.base.y,
							sign.shape.base.width, sign.shape.base.height)) {
				sign.drawCharacter(gg, preferences);
			}
		}

	}
}
