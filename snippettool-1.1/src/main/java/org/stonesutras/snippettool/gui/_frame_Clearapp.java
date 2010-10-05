package org.stonesutras.snippettool.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.stonesutras.snippettool.model.SnippetTool;
import org.stonesutras.snippettool.util.XMLUtil;

/**
 * GUI interface for cleaning appearances from the database.
 * Use with care! Could potentially destroy all saved appearances data.
 *
 * @author Alexei Bratuhin
 *
 */
@SuppressWarnings("serial")
public class _frame_Clearapp extends JFrame implements ActionListener{

	/** Reference to parent component **/
	_frame_SnippetTool root;

	SnippetTool snippettool;
    Preferences properties;

	/** Regular Expression to search for appearances to remove **/
	JTextField jtf_regexp = new JTextField();

	JButton jb_ok = new JButton("OK");
	JButton jb_cancel = new JButton("Cancel");

	public _frame_Clearapp(_frame_SnippetTool r, SnippetTool snippettool){
		// super
		super("Clear Appearances");

		//
		this.root = r;
		this.snippettool = snippettool;
        this.properties = snippettool.prefs2;

		// button <-> action listener
		jb_ok.addActionListener(this);
		jb_cancel.addActionListener(this);

		// hiwi_gui_clearapp
		setLayout(new GridLayout(2,2, 0,0));
		setVisible(true);
		add(new JLabel("ID:"));
		add(jtf_regexp);
		add(jb_ok);
		add(jb_cancel);

		pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(jb_ok)){
			//
            String dbOut = properties.get("db.unicode.dir", "");
            String dbUser = properties.get("db.unicode.user", "");
            String dbPass = properties.get("db.unicode.password", "");

		    // clear appearances
			XMLUtil.clearAppearances(dbUser, dbPass, dbOut, jtf_regexp.getText());

			// close
			dispose();
		}
		if(e.getSource().equals(jb_cancel)){
			dispose();
		}
	}

}
