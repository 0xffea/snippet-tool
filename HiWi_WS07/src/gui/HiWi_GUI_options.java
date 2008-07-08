package src.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.java.dev.colorchooser.ColorChooser;

import src.model.HiWi_Object_Sutra;
import src.util.prefs.PrefUtil;
import src.util.spring.SpringUtilities;

@SuppressWarnings("serial")
public class HiWi_GUI_options extends JPanel implements ActionListener, ItemListener, ChangeListener{
	
	public final static int SIZE_FIELD_LENGTH = 4;
	
	HiWi_GUI root;
	HiWi_Object_Sutra s;
	
	JLabel label_oa = new JLabel("x offset:");
	JLabel label_ob = new JLabel("y offset:");
	JLabel label_a = new JLabel("width:");
	JLabel label_b = new JLabel("height:");
	JLabel label_da = new JLabel("x distance:");
	JLabel label_db = new JLabel("y distance:");
	
	public JTextField jtf_oa = new JTextField(SIZE_FIELD_LENGTH);
	public JTextField jtf_ob = new JTextField(SIZE_FIELD_LENGTH);
	public JTextField jtf_a = new JTextField(SIZE_FIELD_LENGTH);
	public JTextField jtf_b = new JTextField(SIZE_FIELD_LENGTH);
	public JTextField jtf_da = new JTextField(SIZE_FIELD_LENGTH);
	public JTextField jtf_db = new JTextField(SIZE_FIELD_LENGTH);

	JButton b_addtext = new JButton("Add Text to Image");
		
	public HiWi_GUI_colourchooser cc_rubb;// = new HiWi_GUI_colourchooser(Preferences.String2Color(Preferences.LOCAL_COLOR_RUBBING_BACKGROUND), Preferences.LOCAL_ALPHA_RUBBING, this, this);
	public HiWi_GUI_colourchooser cc_text;// = new HiWi_GUI_colourchooser(Preferences.String2Color(Preferences.LOCAL_COLOR_TEXT), Preferences.LOCAL_ALPHA_TEXT, this, this);
	public HiWi_GUI_colourchooser cc_back;// = new HiWi_GUI_colourchooser(Preferences.String2Color(Preferences.LOCAL_COLOR_MARKUP_P), Preferences.LOCAL_ALPHA_MARKUP_P, this, this);
	public HiWi_GUI_colourchooser cc_fore;// = new HiWi_GUI_colourchooser(Preferences.String2Color(Preferences.LOCAL_COLOR_MARKUP_A), Preferences.LOCAL_ALPHA_MARKUP_A, this, this);
	
	public ButtonGroup bg_direction = new ButtonGroup();
	public JRadioButton rb_left_to_right = new JRadioButton("left->right");
	public JRadioButton rb_right_to_left = new JRadioButton("right->left");
	
	public ButtonGroup bg_textout = new ButtonGroup();
	public JRadioButton rb_id = new JRadioButton("char");
	public JRadioButton rb_n = new JRadioButton("number");
	public JRadioButton rb_rc = new JRadioButton("(row,column)");
	
	public ButtonGroup bg_markup = new ButtonGroup();
	public JRadioButton rb_auto = new JRadioButton("guided");
	public JRadioButton rb_man = new JRadioButton("manually");
	
	public HiWi_GUI_options(HiWi_GUI r, HiWi_Object_Sutra su){
		super();
		setLayout(new SpringLayout());
		setBorder(new TitledBorder("options"));
		setPreferredSize(new Dimension(100, 600));
		
		this.root = r;
		this.s = su;
		
		cc_rubb = new HiWi_GUI_colourchooser(PrefUtil.String2Color(root.props.getProperty("local.color.rubbing")), Float.parseFloat(root.props.getProperty("local.alpha.rubbing")), this, this);
		cc_text = new HiWi_GUI_colourchooser(PrefUtil.String2Color(root.props.getProperty("local.color.text")), Float.parseFloat(root.props.getProperty("local.alpha.text")), this, this);
		cc_back = new HiWi_GUI_colourchooser(PrefUtil.String2Color(root.props.getProperty("local.color.markup.p")), Float.parseFloat(root.props.getProperty("local.alpha.markup.p")), this, this);
		cc_fore = new HiWi_GUI_colourchooser(PrefUtil.String2Color(root.props.getProperty("local.color.markup.a")), Float.parseFloat(root.props.getProperty("local.alpha.markup.a")), this, this);
		
		JPanel box1 = new JPanel(new SpringLayout());
		box1.setBorder(new TitledBorder("default markup values"));
		box1.add(label_oa);
		box1.add(jtf_oa);
		box1.add(label_ob);
		box1.add(jtf_ob);
		box1.add(label_a);
		box1.add(jtf_a);
		box1.add(label_b);
		box1.add(jtf_b);
		box1.add(label_da);
		box1.add(jtf_da);
		box1.add(label_db);
		box1.add(jtf_db);
		SpringUtilities.makeCompactGrid(box1, 3, 4, 3, 3, 3, 3);
		
		JPanel box2 = new JPanel(new SpringLayout());
		box2.add(b_addtext);
		b_addtext.addActionListener(this);
		SpringUtilities.makeCompactGrid(box2, 1, 1, 3, 3, 3, 3);
		
		cc_rubb.setBorder(new TitledBorder("rubbing"));
		cc_text.setBorder(new TitledBorder("text"));
		cc_back.setBorder(new TitledBorder("markup back"));
		cc_fore.setBorder(new TitledBorder("markup fore"));
		
		JPanel box3 = new JPanel(new SpringLayout());
		box3.setBorder(new TitledBorder("colour & alpha"));
		box3.add(cc_rubb);
		box3.add(cc_text);
		box3.add(cc_back);
		box3.add(cc_fore);
		SpringUtilities.makeCompactGrid(box3, 2, 2, 3, 3, 3, 3);
				
		JPanel box4 = new JPanel(new SpringLayout());
		box4.setBorder(new TitledBorder("text direction"));
		box4.add(rb_left_to_right); bg_direction.add(rb_left_to_right);
		box4.add(rb_right_to_left); bg_direction.add(rb_right_to_left);
		
		rb_left_to_right.addItemListener(this);
		rb_right_to_left.addItemListener(this);
		
		SpringUtilities.makeCompactGrid(box4, 2, 1, 3, 3, 3, 3);
		
		JPanel box5 = new JPanel(new SpringLayout());
		box5.setBorder(new TitledBorder("output"));
		box5.add(rb_id); bg_textout.add(rb_id);
		box5.add(rb_n); bg_textout.add(rb_n);
		box5.add(rb_rc); bg_textout.add(rb_rc);
		
		rb_id.addItemListener(this);
		rb_n.addItemListener(this);
		rb_rc.addItemListener(this);	
		
		SpringUtilities.makeCompactGrid(box5, 3, 1, 3, 3, 3, 3);
		
		
		JPanel box6 = new JPanel(new SpringLayout());
		box6.add(box4);
		box6.add(box5);		
		
		SpringUtilities.makeCompactGrid(box6, 1, 2, 3, 3, 3, 3);
		
		JPanel box7 = new JPanel(new SpringLayout());
		box7.setBorder(new TitledBorder("markup"));
		box7.add(rb_auto);
		box7.add(rb_man);
		bg_markup.add(rb_auto);
		bg_markup.add(rb_man);
		rb_auto.addItemListener(this);
		rb_man.addItemListener(this);
		rb_auto.setSelected(true);
		SpringUtilities.makeCompactGrid(box7, 2, 1, 3, 3, 3, 3);
			
		
		add(box1);
		add(box2);
		add(box3);
		add(box6);
		add(box7);
		
		SpringUtilities.makeCompactGrid(this, 5, 1, 0, 0, 0, 0);
		
		rb_right_to_left.setSelected(true);
		rb_id.setSelected(true);
		
		setVisible(true);
	}

	public void itemStateChanged(ItemEvent ie) {
		if(rb_left_to_right.isSelected()) s.is_left_to_right = true;
		if(rb_right_to_left.isSelected()) s.is_left_to_right = false;
		
		if(rb_id.isSelected()) s.showId = true; else s.showId = false;
		if(rb_n.isSelected()) s.showNumber = true; else s.showNumber = false;
		if(rb_rc.isSelected()) s.showRowColumn = true; else s.showRowColumn = false;
		
		if(rb_auto.isSelected()) root.main.main_image.changeMouseController("auto");
		if(rb_man.isSelected()) root.main.main_image.changeMouseController("manual");
		
		root.main.repaint();
	}

	public void actionPerformed(ActionEvent e) {
		//
		String cmd = e.getActionCommand();
		
		// set changed properties
		root.props.setProperty("local.color.rubbing", PrefUtil.Color2String(cc_rubb.cc.getColor()));
		root.props.setProperty("local.color.text", PrefUtil.Color2String(cc_text.cc.getColor()));
		root.props.setProperty("local.color.markup.p", PrefUtil.Color2String(cc_back.cc.getColor()));
		root.props.setProperty("local.color.markup.a", PrefUtil.Color2String(cc_fore.cc.getColor()));
		
		// 
		if(cmd.equals(b_addtext.getActionCommand())){
			s.loadMarkupSchema(this);			
			root.repaint();
		}
		
		// repaint
		root.main.repaint();
	}

	public void stateChanged(ChangeEvent e) {
		// handle jslider
		if(e.getSource().equals(cc_rubb.as)){
			float value = (float) (cc_rubb.as.getValue()/100.0);
			root.props.setProperty("local.alpha.rubbing", String.valueOf(value));
			cc_rubb.as2.setValue((int)(value*100));
		}
		if(e.getSource().equals(cc_text.as)){
			float value = (float) (cc_text.as.getValue()/100.0);
			root.props.setProperty("local.alpha.text", String.valueOf(value));
			cc_text.as2.setValue((int)(value*100));
		}
		if(e.getSource().equals(cc_back.as)){
			float value = (float) (cc_back.as.getValue()/100.0);
			root.props.setProperty("local.alpha.markup.p", String.valueOf(value));
			cc_back.as2.setValue((int)(value*100));
		}
		if(e.getSource().equals(cc_fore.as)){
			float value = (float) (cc_fore.as.getValue()/100.0);
			root.props.setProperty("local.alpha.markup.a", String.valueOf(value));
			cc_fore.as2.setValue((int)(value*100));
		}
		// handle jspinner
		if(e.getSource().equals(cc_rubb.as2)){
			float value = (float) ((Integer)(cc_rubb.as2.getValue())/100.0);
			root.props.setProperty("local.alpha.rubbing", String.valueOf(value));
			cc_rubb.as.setValue((int) (value*100));
		}
		if(e.getSource().equals(cc_text.as2)){
			float value = (float) ((Integer)(cc_text.as2.getValue())/100.0);
			root.props.setProperty("local.alpha.text", String.valueOf(value));
			cc_text.as.setValue((int) (value*100));
		}
		if(e.getSource().equals(cc_back.as2)){
			float value = (float) ((Integer)(cc_back.as2.getValue())/100.0);
			root.props.setProperty("local.alpha.markup.p", String.valueOf(value));
			cc_back.as.setValue((int) (value*100));
		}
		if(e.getSource().equals(cc_fore.as2)){
			float value = (float) ((Integer)(cc_fore.as2.getValue())/100.0);
			root.props.setProperty("local.alpha.markup.a", String.valueOf(value));
			cc_fore.as.setValue((int) (value*100));
		}
		// repaint
		root.main.repaint();
	}
	
	public class HiWi_GUI_colourchooser extends JPanel{
		ColorChooser cc;
		JSlider as;
		JSpinner as2;
		public HiWi_GUI_colourchooser(Color def, double a, ActionListener al, ChangeListener cl){
			super();
			setLayout(new SpringLayout());
			setVisible(true);
			cc = new ColorChooser();
			cc.setColor(def);
			cc.addActionListener(al);
			as = new JSlider(JSlider.VERTICAL, 0, 100, (int)(a*100));
			as.addChangeListener(cl);
			as2 = new JSpinner(new SpinnerNumberModel((int)(a*100), 0, 100, 1));
			as2.setEditor(new JSpinner.NumberEditor(as2));
			as2.addChangeListener(cl);
			add(cc);
			add(as);
			add(as2);
			SpringUtilities.makeCompactGrid(this, 1, 3, 3, 3, 3, 3);
		}
	}

}
