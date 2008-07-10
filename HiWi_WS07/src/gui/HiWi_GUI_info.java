package src.gui;

import java.awt.FlowLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import src.model.HiWi_Object_Sign;
import src.model.HiWi_Object_Sutra;

public class HiWi_GUI_info extends JPanel{
	
	HiWi_Object_Sutra s;
	JTextArea jta_info = new JTextArea();
	
	public HiWi_GUI_info(HiWi_Object_Sutra sutra){
		// super
		super();
		setVisible(true);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new TitledBorder("info"));
		// 
		Box box3 = new Box(BoxLayout.X_AXIS);
		box3.add(new JScrollPane(jta_info));
		jta_info.setWrapStyleWord(true);
		jta_info.setLineWrap(true);
		jta_info.setColumns(20);
		jta_info.setRows(10);
		//
		add(box3);
		//
		this.s = sutra;
	}
	
	public void showInfo(int i){
		HiWi_Object_Sign sn = s.getSign(i, 0);
		// print info on label
		String info = new String();
		info += "character:\t"+sn.characterStandard+" ("+sn.characterOriginal+")"+"\n";
		info += "x, y:\t"+sn.s.x+", "+sn.s.y+"\n";
		info += "width, height:\t"+sn.s.width+", "+sn.s.height+"\n";
		info += "number:\t"+sn.number+" (r:"+sn.row+", c:"+sn.column+")"+"\n";
		jta_info.setText(info);
	}

}
