/**
 * @author silvestre
 *
 */
package org.stonesutras.snippettool.gui;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;

/**
 * @author silvestre
 * 
 *         A JComponent with some utility functions regarding
 *         Mouse(Motion)Listeners.
 */
@SuppressWarnings("serial")
public abstract class JComponentE extends JComponent {

	/**
	 * Removes all currently attached mouse listeners.
	 */
	public void clearMouseListeners() {
		for (MouseListener mouseListener : getMouseListeners()) {
			removeMouseListener(mouseListener);
		}
	}

	/**
	 * Removes all currently attached mouse motion listeners.
	 * 
	 */
	public void clearMouseMotionListeners() {
		for (MouseMotionListener mouseMotionListener : getMouseMotionListeners()) {
			removeMouseMotionListener(mouseMotionListener);
		}
	}

	/**
	 * Removes all currently attached mouse listeners and then adds the
	 * specified mouse listener.
	 * 
	 * @see JComponentE#addMouseListener(MouseListener)
	 * 
	 * @param mouseListener
	 *            the mouse listener
	 */
	public void setMouseListener(final MouseListener mouseListener) {
		clearMouseListeners();
		addMouseListener(mouseListener);
	}

	/**
	 * Removes all currently attached mouse motion listeners and then adds the
	 * specified mouse motion listener.
	 * 
	 * @see JComponentE#addMouseMotionListener(MouseMotionListener)
	 * 
	 * @param mouseMotionListener
	 *            the mouse motion listener
	 */
	public void setMouseMotionListener(
			final MouseMotionListener mouseMotionListener) {
		clearMouseMotionListeners();
		addMouseMotionListener(mouseMotionListener);
	}
}
