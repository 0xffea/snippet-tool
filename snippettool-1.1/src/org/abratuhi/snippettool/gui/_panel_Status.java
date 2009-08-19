package org.abratuhi.snippettool.gui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.abratuhi.snippettool.util.SpringUtilities;

@SuppressWarnings("serial")
public class _panel_Status extends JPanel {

	private final JLabel labelStatus = new JLabel();

	public _panel_Status() {
		super();
		setLayout(new SpringLayout());
		add(labelStatus);
		labelStatus.setFont(getFont().deriveFont(10.0f));
		SpringUtilities.makeCompactGrid(this, 1, 1, 0, 0, 0, 0);
	}

	public void setStatus(final String status) {
		labelStatus.setText("<html><i>" + status + "</i></html>");
	}

	public void setError(final String message, final Throwable t) {
		labelStatus.setText("<html><font color=red>" + message + ": " + t.getLocalizedMessage() + "</font></html>");
	}
}
