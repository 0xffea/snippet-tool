package src.model;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import org.jdom.Element;

import src.util.num.NumUtil;
import src.util.prefs.PrefUtil;

/**
 * (row, column) - for numeration text is represented in european way 
 * @author abratuhi
 *
 */
public class HiWi_Object_Sign {
	//
	public HiWi_Object_Sutra sutra;
	public String sign_path_snippet = new String();

	//
	public Rectangle s;			//marked area on sutra's .jpg

	//
	public String characterStandard = new String();		// standard unicode sign, which is represents characterOriginal in unicode database 
	public String characterOriginal = new String();		// original unicode sign, which was mapped to characterStandard due to lack of characterOriginal in unicode database

	//
	public String id;	// id, e.g. HDS_1_2_1
	public int row;	// numeration starts with 1 because of compatibility with existing scripts and numeration
	public int column;	// numeration starts with 1 because of compatibility with existing scripts and numeration
	public int number;	// numeration starts with 1 because of compatibility with existing scripts and numeration
	public float cert;	// certainty -> max=1.0, min=0.0
	boolean preferred_reading;	// whether this reading is preferred by heidelberger academy of science
	int groupnumber;
	int variant;	// reading variant, numeration starts with 0, no compatibilitiy needed (yet?)

	//
	public String getInfo(){
		String out = new String();
		out += "id="+id+",";
		out += "chS="+characterStandard+",";
		out += "chO="+characterOriginal+",";
		out += "number="+getNumber()+",";
		out += "cert="+cert+",";
		out += "variant="+variant+",";
		out += "preferred_reading="+preferred_reading+",";
		out += "row="+getRow()+",";
		out += "column="+getColumn()+",";
		out += "point=("+s.x+","+s.y+")"+",";
		out += "dimension=("+s.width+"x"+s.height+")"+",";
		out += ";";
		return out;
	}

	// empty constructor, needed sometimes in texhnical procedures
	public HiWi_Object_Sign(){

	}
	// basic constructor
	public HiWi_Object_Sign(HiWi_Object_Sutra s, String chStandard, String chOriginal, float cert, boolean preferred, int var, int r, int c, int n, Point base, Dimension delta){
		// initialize parent sutra
		this.sutra = s;
		// get needed properties
		String dbSnips = sutra.root.props.getProperty("db.dir.snippet");
		// initialize further attributes
		this.characterStandard = chStandard;
		this.characterOriginal = chOriginal;
		this.cert = cert;
		this.preferred_reading = preferred;
		this.variant = var;
		this.setColumn(c);
		this.setRow(r);
		this.setNumber(n);
		this.id = this.sutra.sutra_id+"_"+this.row+"_"+this.column;
		this.sign_path_snippet = dbSnips+"/subimage_"+sutra.sutra_id+"_"+r+"_"+c+".png";
		// 
		this.s = new Rectangle(base, delta);
	}

	public void setNumber(int n){
		this.number = n;
	}
	public int getNumber(){
		return this.number;
	}
	public void setColumn(int c){
		this.column = c;
	}
	public int getColumn(){
		return this.column;
	}
	public void setRow(int r){
		this.row = r;
	}
	public int getRow(){
		return this.row;
	}

	// 
	public void resizeSnippet(String direction, int dx, int dy){
		if(direction == null) return;
		if(direction.equals("nw")){s.setBounds(s.x+dx, s.y+dy, s.width-dx, s.height-dy);return;}
		if(direction.equals("n")){s.setBounds(s.x, s.y+dy, s.width, s.height-dy);return;}
		if(direction.equals("ne")){s.setBounds(s.x, s.y+dy, s.width+dx, s.height-dy);return;}
		if(direction.equals("e")){s.setBounds(s.x, s.y, s.width+dx, s.height);return;}
		if(direction.equals("se")){s.setBounds(s.x, s.y, s.width+dx, s.height+dy);return;}
		if(direction.equals("s")){s.setBounds(s.x, s.y, s.width, s.height+dy);return;}
		if(direction.equals("sw")){s.setBounds(s.x+dx, s.y, s.width-dx, s.height+dy);return;}
		if(direction.equals("w")){s.setBounds(s.x+dx, s.y, s.width-dx, s.height);return;}
	}
	public void moveSnippet(int dx, int dy){
		s.setLocation(s.x+dx, s.y+dy);
	}
	public String computeMoveDirection(Cursor c){
		if(c.getType() == Cursor.NW_RESIZE_CURSOR) return new String("nw");
		if(c.getType() == Cursor.N_RESIZE_CURSOR) return new String("n");
		if(c.getType() == Cursor.NE_RESIZE_CURSOR) return new String("ne");
		if(c.getType() == Cursor.E_RESIZE_CURSOR) return new String("e");
		if(c.getType() == Cursor.SE_RESIZE_CURSOR) return new String("se");
		if(c.getType() == Cursor.S_RESIZE_CURSOR) return new String("s");
		if(c.getType() == Cursor.SW_RESIZE_CURSOR) return new String("sw");
		if(c.getType() == Cursor.W_RESIZE_CURSOR) return new String("w");
		return null;
	}
	public String placeOnBorder(Point p){
		if(!s.contains(p)) return new String("none");
		//1     2
		// 11 22
		// 33 44
		//3     4			
		float part = 0.1f;
		int x0 = s.getLocation().x;
		int y0 = s.getLocation().y;
		int x1 = x0 + s.width;
		int y1 = y0 + s.height;
		Point p1 = new Point(x0, y0);
		Point p2 = new Point(x1, y0);
		Point p3 = new Point(x0, y1);
		Point p4 = new Point(x1, y1);
		int x = x1 - x0;
		int y = y1 - y0;
		int dx = (int)(x*part);
		int dy = (int)(y*part);
		if(new Rectangle(p1.x, p1.y, dx, dy).contains(p)) return new String("nw");
		if(new Rectangle(p1.x+dx, p1.y, x-dx-dx, dy).contains(p)) return new String("n");
		if(new Rectangle(p2.x-dx, p2.y, dx, dy).contains(p)) return new String("ne");
		if(new Rectangle(p2.x-dx, p2.y+dy, dx, y-dy-dy).contains(p)) return new String("e");
		if(new Rectangle(p4.x-dx, p4.y-dy, dx, dy).contains(p)) return new String("se");
		if(new Rectangle(p3.x+dx, p3.y-dy, x-dx-dx, dy).contains(p)) return new String("s");
		if(new Rectangle(p3.x, p3.y-dy, dx, dy).contains(p)) return new String("sw");
		if(new Rectangle(p1.x, p1.y+dy, dx, y-dy-dy).contains(p)) return new String("w");
		return null;
	}
	public void draw(Graphics2D g){
		adjustFont();
		drawBorder(g);
		drawMarkup(g);
		if(sutra.showId) drawID(g); 
		if(sutra.showNumber) drawN(g);
		if(sutra.showRowColumn) drawRC(g);
	}
	public void adjustFont(){
		if(sutra.f != null){
			Font f2 = sutra.f.deriveFont((float)(Math.min(s.width, s.height)));
			sutra.f = f2;
		}
	}
	public void setAlpha(Graphics2D g, float alpha){
		int rule = AlphaComposite.SRC_OVER;
		AlphaComposite ac;
		ac = AlphaComposite.getInstance(rule, alpha);
		g.setComposite(ac);
	}
	public void drawBorder(Graphics2D g){
		// get needed properties
		Float alpha = Float.parseFloat(sutra.root.props.getProperty("local.alpha.markup.border"));
		Color color = PrefUtil.String2Color(sutra.root.props.getProperty("local.color.markup.border"));
		// draw
		setAlpha(g, alpha);
		g.setColor(color);
		g.draw(s);
	}
	public void drawMarkup(Graphics2D g){
		if(this.number!=sutra.getActiveSign()) {
			// get needed properties
			Float alpha = Float.parseFloat(sutra.root.props.getProperty("local.alpha.markup.p"));
			Color color = PrefUtil.String2Color(sutra.root.props.getProperty("local.color.markup.p"));
			// draw
			setAlpha(g, alpha);
			g.setColor(color);
		}
		else {
			// get needed properties
			Float alpha = Float.parseFloat(sutra.root.props.getProperty("local.alpha.markup.a"));
			Color color = PrefUtil.String2Color(sutra.root.props.getProperty("local.color.markup.a"));
			// draw
			setAlpha(g, alpha);
			g.setColor(color);
		}
		g.fill(s);
	}
	public void drawID(Graphics2D g){
		// get needed properties
		Float alpha = Float.parseFloat(sutra.root.props.getProperty("local.alpha.text"));
		Color color = PrefUtil.String2Color(sutra.root.props.getProperty("local.color.text"));
		// draw
		if(sutra.f != null) g.setFont(sutra.f);
		setAlpha(g, alpha);
		g.setColor(color);
		g.drawString(characterStandard, s.getBounds().x, s.getBounds().y+g.getFontMetrics().getHeight()*25/40);
	}
	public void drawN(Graphics2D g){
		// get needed properties
		Float alpha = Float.parseFloat(sutra.root.props.getProperty("local.alpha.text"));
		Color color = PrefUtil.String2Color(sutra.root.props.getProperty("local.color.text"));
		// draw
		if(sutra.f != null) g.setFont(sutra.f.deriveFont(sutra.f.getSize()/3.0f));
		setAlpha(g, alpha);
		g.setColor(color);
		g.drawString(String.valueOf(number), s.getBounds().x, s.getBounds().y+g.getFontMetrics().getAscent());
	}
	public void drawRC(Graphics2D g){
		// get needed properties
		Float alpha = Float.parseFloat(sutra.root.props.getProperty("local.alpha.text"));
		Color color = PrefUtil.String2Color(sutra.root.props.getProperty("local.color.text"));
		// draw
		if(sutra.f != null) g.setFont(sutra.f.deriveFont(sutra.f.getSize()/5.0f));
		setAlpha(g, alpha);
		g.setColor(color);
		g.drawString("("+String.valueOf(row)+","+String.valueOf(column)+")", s.getBounds().x, s.getBounds().y+g.getFontMetrics().getAscent());
	}

	//
	public String getXUpdate(boolean updateOnly){
		if(!updateOnly){
			String xupdate = 
				"<xu:modifications version=\'1.0\' xmlns:xu=\'http://www.xmldb.org/xupdate\'>" +
				//"    <xu:append select=\"//unihandb/char[@xmlid=\'U+"+NumUtil.dec2hex(character.codePointAt(0)).toUpperCase()+"\']\">" +
				"    <xu:append select=\"//char[@xmlid=\'U+"+NumUtil.dec2hex(characterStandard.codePointAt(0)).toUpperCase()+"\']\">" +
				"       <xu:element name=\"appearance\">" +
				"           <xu:attribute name=\"character\">"+this.characterStandard+"</xu:attribute>" +
				"           <xu:attribute name=\"original\">"+this.characterOriginal+"</xu:attribute>" +
				"           <xu:attribute name=\"id\">"+this.id+"</xu:attribute>" +
				"           <xu:attribute name=\"preferred_reading\">"+this.preferred_reading+"</xu:attribute>" +
				"           <xu:attribute name=\"variant\">"+this.variant+"</xu:attribute>" +
				"           <xu:attribute name=\"cert\">"+this.cert+"</xu:attribute>" +
				"           <xu:attribute name=\"nr\">"+sutra.sutra_id+"_"+this.number+"</xu:attribute>" +
				"           <source>"+sutra.sutra_id+"</source>" +
				"           <rubbing>"+sutra.sutra_path_rubbing+"</rubbing>" +
				"           <graphic>"+sign_path_snippet+"</graphic>" +
				"           <coordinates x=\""+s.x+"\" y=\""+s.y+"\" width=\""+s.width+"\" height=\""+s.height+"\" />" +
				"       </xu:element>" +
				"    </xu:append>" +
				"</xu:modifications>";
			return xupdate;
		}
		else{
			String xupdate = 
				"<xu:modifications version=\'1.0\' xmlns:xu=\'http://www.xmldb.org/xupdate\'>" +
				"	<xu:update select=\"//unihandb/char[@xmlid=\'U+"+NumUtil.dec2hex(characterStandard.codePointAt(0)).toUpperCase()+"\']/appearance[@id='"+this.id+"']/coordinates/@x\">" + this.s.x + "</xu:update>" +
				"	<xu:update select=\"//unihandb/char[@xmlid=\'U+"+NumUtil.dec2hex(characterStandard.codePointAt(0)).toUpperCase()+"\']/appearance[@id='"+this.id+"']/coordinates/@y\">" + this.s.y + "</xu:update>" + 
				"	<xu:update select=\"//unihandb/char[@xmlid=\'U+"+NumUtil.dec2hex(characterStandard.codePointAt(0)).toUpperCase()+"\']/appearance[@id='"+this.id+"']/coordinates/@width\">" + this.s.width + "</xu:update>" + 
				"	<xu:update select=\"//unihandb/char[@xmlid=\'U+"+NumUtil.dec2hex(characterStandard.codePointAt(0)).toUpperCase()+"\']/appearance[@id='"+this.id+"']/coordinates/@height\">" + this.s.height + "</xu:update>" + 
				"</xu:modifications>";
			return xupdate;
		}
	}

	public Element toAppearance(){
		Element appearance = new Element("appearance");

		appearance.setAttribute("character", this.characterStandard);
		appearance.setAttribute("character", this.characterOriginal);
		appearance.setAttribute("id", this.id);
		appearance.setAttribute("preferred_reading", String.valueOf(this.preferred_reading));
		appearance.setAttribute("variant", String.valueOf(this.variant));
		appearance.setAttribute("cert", String.valueOf(this.cert));
		appearance.setAttribute("nr", String.valueOf(sutra.sutra_id+"_"+this.number));

		Element source = new Element("source");
		source.setText(sutra.sutra_id);

		Element rubbing = new Element("rubbing");
		rubbing.setText(sutra.sutra_path_rubbing);

		Element graphic = new Element("graphic");
		graphic.setText(this.sign_path_snippet);

		Element coordinates = new Element("coordinates");
		coordinates.setAttribute("x", String.valueOf(this.s.x));
		coordinates.setAttribute("y", String.valueOf(this.s.y));
		coordinates.setAttribute("height", String.valueOf(this.s.height));
		coordinates.setAttribute("width", String.valueOf(this.s.width));

		appearance.addContent(source);
		appearance.addContent(rubbing);
		appearance.addContent(graphic);
		appearance.addContent(coordinates);

		return appearance;
	}

	public static HiWi_Object_Sign fromAppearance(HiWi_Object_Sutra sutra, Element appearance){
		String chStandard = appearance.getAttributeValue("character");
		String chOriginal = appearance.getAttributeValue("original");
		String chId = appearance.getAttributeValue("id");
		boolean preferred = Boolean.parseBoolean(appearance.getAttributeValue("preferred_reading"));
		float cert = Float.parseFloat(appearance.getAttributeValue("cert"));
		int var = Integer.parseInt(appearance.getAttributeValue("variant"));
		int n = Integer.parseInt(appearance.getAttributeValue("nr").substring((sutra.sutra_id+"_").length()));
		String rc = chId.substring((sutra.sutra_id+"_").length());
		int r = Integer.parseInt(rc.substring(0, rc.indexOf("_")));
		int c = Integer.parseInt(rc.substring(rc.indexOf("_")+1));
		Element xmlc = appearance.getChild("coordinates");
		int x = Integer.parseInt(xmlc.getAttributeValue("x"));
		int y = Integer.parseInt(xmlc.getAttributeValue("y"));
		int width = Integer.parseInt(xmlc.getAttributeValue("width"));
		int height = Integer.parseInt(xmlc.getAttributeValue("height"));

		HiWi_Object_Sign sign = new HiWi_Object_Sign(sutra, chStandard, chOriginal, cert, preferred, var, r, c, n, new Point(x,y), new Dimension(width, height));				
		sign.id = chId; // just to be sure, since it's the only value not mentioned in constructor explicit

		return sign;
	}
	
	public void updateCoordinatesFromAppearance(Element appearance){
		Element xmlc = appearance.getChild("coordinates");
		int x = Integer.parseInt(xmlc.getAttributeValue("x"));
		int y = Integer.parseInt(xmlc.getAttributeValue("y"));
		int width = Integer.parseInt(xmlc.getAttributeValue("width"));
		int height = Integer.parseInt(xmlc.getAttributeValue("height"));
		this.s = new Rectangle(x, y, width, height);
	}

	public void updateSnippet(Rectangle rectangle) {
		this.s = (Rectangle) rectangle.clone();
	}
}

