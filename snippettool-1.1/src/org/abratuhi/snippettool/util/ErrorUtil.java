/**
 * 
 */
package org.abratuhi.snippettool.util;

import java.awt.Component;
import java.awt.GraphicsEnvironment;

import javax.swing.JOptionPane;

/**
 * @author silvestre
 * 
 */
public final class ErrorUtil {

	/**
	 * The ErrorUtil should not be instantiated.
	 */
	private ErrorUtil() {
	}

	/**
	 * Shows an error dialog.
	 * 
	 * @param parentComponent
	 *            Defines the Component that is to be the parent of this dialog
	 *            box. It is used in two ways: the Frame that contains it is
	 *            used as the Frame parent for the dialog box, and its screen
	 *            coordinates are used in the placement of the dialog box. In
	 *            general, the dialog box is placed just below the component.
	 *            This parameter may be null, in which case a default Frame is
	 *            used as the parent, and the dialog will be centered on the
	 *            screen (depending on the L&F).
	 * @param message
	 *            The message to be displayed to the user as an explanation for
	 *            the error.
	 * @param e
	 *            The error which occured.
	 */
	public static void showError(final Component parentComponent,
			final String message, final Throwable e) {
		if (!GraphicsEnvironment.isHeadless()) {
			JOptionPane.showMessageDialog(parentComponent, message + ": "
					+ e.getLocalizedMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}
}
