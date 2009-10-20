package org.abratuhi.snippettool.gui;

import java.awt.Component;
import java.awt.Cursor;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.abratuhi.snippettool.util.SpringUtilities;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

@SuppressWarnings("serial")
public class _panel_Status extends JPanel {

	private final JLabel labelStatus = new JLabel();
	private final JLabel runningTasksLabel = new JLabel();

	private final Component root;

	private final Multiset<String> tasks = HashMultiset.create();

	public _panel_Status(Component root) {
		super();
		this.root = root;
		setLayout(new SpringLayout());
		add(labelStatus);
		add(runningTasksLabel);
		labelStatus.setFont(getFont().deriveFont(10.0f));
		SpringUtilities.makeCompactGrid(this, 1, 2, 0, 0, 0, 0);
	}

	public void setStatus(final String status) {
		labelStatus.setText("<html><i>" + status + "</i></html>");
	}

	public void setError(final String message, final Throwable t) {
		labelStatus.setText("<html><font color=red>" + message + ": " + t.getLocalizedMessage() + "</font></html>");
	}

	public String addTask(final String taskDescription) {
		tasks.add(taskDescription);
		updateRunningTasksLabel();
		root.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		return taskDescription;
	}

	public void removeTask(final String taskDescription) {
		tasks.remove(taskDescription);
		updateRunningTasksLabel();
		if (tasks.isEmpty()) {
			root.setCursor(Cursor.getDefaultCursor());
		}
	}

	private void updateRunningTasksLabel() {
		Joiner joiner = Joiner.on("; ");
		runningTasksLabel.setText("<html>" + joiner.join(tasks) + "</html>");
	}
}
