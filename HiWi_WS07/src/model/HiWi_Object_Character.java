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
 * Character class. Representation for characters of the inscript together with all their relevant properties to be stored.
 * (row, column) - for numeration text is represented in european way 
 * @author Alexei Bratuhin
 *
 */
public class HiWi_Object_Character {
	
	/** Reference to parent object **/
	public HiWi_Object_Inscript inscript;
	
	/** path to character snippet on database server **/
	public String sign_path_snippet = new String();

	/** snippet marking **/
	public Rectangle s;

	/** standard unicode sign, which is represents characterOriginal in unicode database **/
	public String characterStandard = new String();
	/** original unicode sign, which was mapped to characterStandard due to lack of characterOriginal in unicode database **/
	public String characterOriginal = new String(); 

	/** character's id, e.g. HDS_1_2_1;
	 * built as {InscriptId}_{CharacterRowNumber}_{CharacterColumnNumber} **/
	public String id;
	
	/** character's row number;
	 * numeration starts with 1 because of compatibility with existing scripts and numeration **/
	public int row;
	
	/** character's column number;
	 * numeration starts with 1 because of compatibility with existing scripts and numeration **/
	public int column;
	
	/** character's continuous number;
	 * numeration starts with 1 because of compatibility with existing scripts and numeration **/
	public int number;
	
	/** certainty -> max=1.0, min=0.0 **/
	public float cert;
	
	/** whether this reading is preferred by heidelberger academy of science **/
	boolean preferred_reading;
	
	/** whether this sign was newly added to inscript and not yet marked up as its colleagues **/
	boolean missing = false;
	
	/** reading variant, numeration starts with 0, no compatibilitiy needed (yet?) **/
	int variant;

	/**
	 * Get character attributes values as readable string
	 * @return	character information
	 */
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

	/** empty constructor, needed sometimes in technical procedures **/
	public HiWi_Object_Character(){

	}
	


	/**
	 * Basic Constructor
	 * 
	 * @param s				parent Inscript Object
	 * @param chStandard	standard character used
	 * @param chOriginal	original character
	 * @param cert			certainty of a character
	 * @param preferred		whether character is from preferred reading
	 * @param var			character's reading variant number (preferred reading <-> variant=0)
	 * @param r				character's row number
	 * @param c				character's column number
	 * @param n				character's number
	 * @param base			character's snippet left upper corner coordinates
	 * @param delta			character's snippet dimension
	 */
	public HiWi_Object_Character(HiWi_Object_Inscript s, String chStandard, String chOriginal, float cert, boolean preferred, int var, int r, int c, int n, Point base, Dimension delta){
		// initialize parent inscript
		this.inscript = s;
		// get needed properties
		String dbSnips = inscript.root.props.getProperty("db.dir.snippet");
		// initialize further attributes
		this.characterStandard = chStandard;
		this.characterOriginal = chOriginal;
		this.cert = cert;
		this.preferred_reading = preferred;
		this.variant = var;
		this.setColumn(c);
		this.setRow(r);
		this.setNumber(n);
		this.id = this.inscript.inscript_id+"_"+this.row+"_"+this.column;
		this.sign_path_snippet = dbSnips+"/subimage_"+inscript.inscript_id+"_"+r+"_"+c+".png";
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

	/**
	 * Resize cahracter's snippet's marking.
	 * @param direction		direction code. Possible values: n/nw/w/sw/s/se/e/ne, correspond to world directions.
	 * @param dx			x resize
	 * @param dy			x resize
	 */
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
	
	/**
	 * Move character's snippet's marking
	 * @param dx			x shift
	 * @param dy			y shift
	 */
	public void moveSnippet(int dx, int dy){
		s.setLocation(s.x+dx, s.y+dy);
	}
	
	/**
	 * Produce direction code, basing on current Cursor form
	 * @param c		current cursor
	 * @return		direction code
	 */
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
	
	/**
	 * Compute, in which part of character's snippet's marking point is situated
	 * @param p		point to check
	 * @return		direction code
	 */
	public String placeOnBorder(Point p){
		if(!s.contains(p)) return new String("none");
		//p1     p2
		//   
		//   
		//p3     p4			
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
	
	/**
	 * Draw character's marking using passed Graphics object of the parent -> on parent's pane
	 * @param g parent Graphics object
	 */
	public void draw(Graphics2D g){
		adjustFont();
		drawBorder(g);
		drawMarkup(g);
		if(inscript.showCharacter) drawCharacter(g); 
		if(inscript.showNumber) drawN(g);
		if(inscript.showRowColumn) drawRC(g);
	}
	
	/**
	 * Adjust Font object, so that the size of the Font corresponds dimension of marking
	 */
	public void adjustFont(){
		if(inscript.f != null){
			Font f2 = inscript.f.deriveFont((float)(Math.min(s.width, s.height)));
			inscript.f = f2;
		}
	}
	
	/**
	 * Set Opacity value
	 * @param g			Graphics 
	 * @param alpha		opacity value
	 */
	public void setAlpha(Graphics2D g, float alpha){
		int rule = AlphaComposite.SRC_OVER;
		AlphaComposite ac;
		ac = AlphaComposite.getInstance(rule, alpha);
		g.setComposite(ac);
	}
	
	public void drawBorder(Graphics2D g){
		// get needed properties
		Float alpha = Float.parseFloat(inscript.root.props.getProperty("local.alpha.markup.border"));
		Color color = PrefUtil.String2Color(inscript.root.props.getProperty("local.color.markup.border"));
		// draw
		setAlpha(g, alpha);
		g.setColor(color);
		g.draw(s);
	}
	public void drawMarkup(Graphics2D g){
		if(this.number!=inscript.getActiveSignNumber()) {
			// get needed properties
			Float alpha = Float.parseFloat(inscript.root.props.getProperty("local.alpha.markup.p"));
			Color color = PrefUtil.String2Color(inscript.root.props.getProperty("local.color.markup.p"));
			// draw
			setAlpha(g, alpha);
			g.setColor(color);
		}
		else {
			// get needed properties
			Float alpha = Float.parseFloat(inscript.root.props.getProperty("local.alpha.markup.a"));
			Color color = PrefUtil.String2Color(inscript.root.props.getProperty("local.color.markup.a"));
			// draw
			setAlpha(g, alpha);
			g.setColor(color);
		}
		g.fill(s);
	}
	
	/**
	 * Draw character's string.
	 * @param g
	 */
	public void drawCharacter(Graphics2D g){
		// get needed properties
		Float alpha = Float.parseFloat(inscript.root.props.getProperty("local.alpha.text"));
		Color color = PrefUtil.String2Color(inscript.root.props.getProperty("local.color.text"));
		// draw
		if(inscript.f != null) g.setFont(inscript.f);
		setAlpha(g, alpha);
		g.setColor(color);
		g.drawString(characterStandard, s.getBounds().x, s.getBounds().y+g.getFontMetrics().getHeight()*25/40);
	}
	
	/**
	 * Draw character's continuous number.
	 * @param g
	 */
	public void drawN(Graphics2D g){
		// get needed properties
		Float alpha = Float.parseFloat(inscript.root.props.getProperty("local.alpha.text"));
		Color color = PrefUtil.String2Color(inscript.root.props.getProperty("local.color.text"));
		// draw
		if(inscript.f != null) g.setFont(inscript.f.deriveFont(inscript.f.getSize()/3.0f));
		setAlpha(g, alpha);
		g.setColor(color);
		g.drawString(String.valueOf(number), s.getBounds().x, s.getBounds().y+g.getFontMetrics().getAscent());
	}
	
	/**
	 * Draw character's row and column numbers: ({CharacterRowNumber},{CharacterColumnNumber})
	 * @param g
	 */
	public void drawRC(Graphics2D g){
		// get needed properties
		Float alpha = Float.parseFloat(inscript.root.props.getProperty("local.alpha.text"));
		Color color = PrefUtil.String2Color(inscript.root.props.getProperty("local.color.text"));
		// draw
		if(inscript.f != null) g.setFont(inscript.f.deriveFont(inscript.f.getSize()/5.0f));
		setAlpha(g, alpha);
		g.setColor(color);
		g.drawString("("+String.valueOf(row)+","+String.valueOf(column)+")", s.getBounds().x, s.getBounds().y+g.getFontMetrics().getAscent());
	}

	/**
	 * Generate XUpdate
	 * @param updateOnly	whether only coordinates need to be updated
	 * @return		XUpdate
	 */
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
				"           <xu:attribute name=\"nr\">"+inscript.inscript_id+"_"+this.number+"</xu:attribute>" +
				"           <source>"+inscript.inscript_id+"</source>" +
				"           <rubbing>"+inscript.inscript_path_rubbing+"</rubbing>" +
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
	
	/**
	 * Generate an org.jdom.Element <appearance> representation of Character
	 * Notice: is used for saving marking inforamtion locally
	 * @return		org.jdom.Element representation of Character
	 */
	public Element toAppearance(){
		Element appearance = new Element("appearance");

		appearance.setAttribute("character", this.characterStandard);
		appearance.setAttribute("character", this.characterOriginal);
		appearance.setAttribute("id", this.id);
		appearance.setAttribute("preferred_reading", String.valueOf(this.preferred_reading));
		appearance.setAttribute("variant", String.valueOf(this.variant));
		appearance.setAttribute("cert", String.valueOf(this.cert));
		appearance.setAttribute("nr", String.valueOf(inscript.inscript_id+"_"+this.number));

		Element source = new Element("source");
		source.setText(inscript.inscript_id);

		Element rubbing = new Element("rubbing");
		rubbing.setText(inscript.inscript_path_rubbing);

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
	
	/**
	 * Generate character from org.jdom.Element <appearance>
	 * Notice: is used for loading marking information from database after having received corresponding <appearance>s list
	 * Notice: is used for loading marking information from local file
	 * @param inscript		inscript object that contains character generated
	 * @param appearance	org.jdom.Element <appearance>
	 * @return
	 */
	public static HiWi_Object_Character fromAppearance(HiWi_Object_Inscript inscript, Element appearance){
		String chStandard = appearance.getAttributeValue("character");
		String chOriginal = appearance.getAttributeValue("original");
		String chId = appearance.getAttributeValue("id");
		boolean preferred = Boolean.parseBoolean(appearance.getAttributeValue("preferred_reading"));
		float cert = Float.parseFloat(appearance.getAttributeValue("cert"));
		int var = Integer.parseInt(appearance.getAttributeValue("variant"));
		int n = Integer.parseInt(appearance.getAttributeValue("nr").substring((inscript.inscript_id+"_").length()));
		String rc = chId.substring((inscript.inscript_id+"_").length());
		int r = Integer.parseInt(rc.substring(0, rc.indexOf("_")));
		int c = Integer.parseInt(rc.substring(rc.indexOf("_")+1));
		Element xmlc = appearance.getChild("coordinates");
		int x = Integer.parseInt(xmlc.getAttributeValue("x"));
		int y = Integer.parseInt(xmlc.getAttributeValue("y"));
		int width = Integer.parseInt(xmlc.getAttributeValue("width"));
		int height = Integer.parseInt(xmlc.getAttributeValue("height"));

		HiWi_Object_Character sign = new HiWi_Object_Character(inscript, chStandard, chOriginal, cert, preferred, var, r, c, n, new Point(x,y), new Dimension(width, height));				
		sign.id = chId; // just to be sure, since it's the only value not mentioned in constructor explicit

		return sign;
	}
	
	/**
	 * Update characater'S snippet's marking coordinates from <appearance> element
	 * @param appearance org.jdom.Element <appearance>
	 */
	public void updateCoordinatesFromAppearance(Element appearance){
		Element xmlc = appearance.getChild("coordinates");
		int x = Integer.parseInt(xmlc.getAttributeValue("x"));
		int y = Integer.parseInt(xmlc.getAttributeValue("y"));
		int width = Integer.parseInt(xmlc.getAttributeValue("width"));
		int height = Integer.parseInt(xmlc.getAttributeValue("height"));
		this.s = new Rectangle(x, y, width, height);
	}
	
	/**
	 * Update cahracter's snippet's marking cordinates from Rectangle object
	 * @param rectangle		marking rectangle
	 */
	public void updateSnippet(Rectangle rectangle) {
		this.s = (Rectangle) rectangle.clone();
	}
}

