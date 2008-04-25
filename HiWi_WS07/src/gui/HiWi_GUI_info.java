package src.gui;

import java.awt.Graphics;

import javax.swing.JLabel;

import src.model.HiWi_Object_Sutra.HiWi_Object_Sign;

public class HiWi_GUI_info extends JLabel{
	
	public HiWi_Object_Sign sn;
	
	public HiWi_GUI_info(){
		// super
		super();
	}
	
	public void setSign(HiWi_Object_Sign sign){
		sn = sign;
	}
	
	public void showInfo(){
		// print info on label
		if(sn!=null) this.setText("("+sn.s.x+","+sn.s.y+";"+sn.s.width+"x"+sn.s.height+") "+"("+sn.number+","+sn.row+","+sn.column+")");
	}

}
