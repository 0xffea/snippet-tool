package org.abratuhi.snippettool.model;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.Properties;

import org.abratuhi.snippettool.util.NumUtil;
import org.abratuhi.snippettool.util.PrefUtil;
import org.jdom.Element;

/**
 * Character class. Representation for characters of the inscript together with
 * all their relevant properties to be stored. (row, column) - for numeration
 * text is represented in european way
 * 
 * @author Alexei Bratuhin
 * 
 */
public class InscriptCharacter {

	/** Reference to parent object **/
	public Inscript inscript;

	/** path to character snippet on database server **/
	public String path_to_snippet = new String();

	/** snippet marking **/
	private SnippetShape shape;

	/**
	 * standard unicode sign, which is represents characterOriginal in unicode
	 * database
	 **/
	public String characterStandard = new String();

	/**
	 * original unicode sign, which was mapped to characterStandard due to lack
	 * of characterOriginal in unicode database
	 **/
	public String characterOriginal = new String();

	/**
	 * character's id, e.g. HDS_1_2_1; built as
	 * {InscriptId}_{CharacterRowNumber}_{CharacterColumnNumber}
	 **/
	private String id;

	/**
	 * character's row number; numeration starts with 1 because of compatibility
	 * with existing scripts and numeration
	 **/
	private int row;

	/**
	 * character's column number; numeration starts with 1 because of
	 * compatibility with existing scripts and numeration
	 **/
	private int column;

	/**
	 * character's continuous number; numeration starts with 1 because of
	 * compatibility with existing scripts and numeration
	 **/
	private int number;

	/**
	 * reading variant, numeration starts with 0, no compatibilitiy needed
	 * (yet?)
	 **/
	int variant;

	/** certainty -> max=1.0, min=0.0 **/
	public float cert;

	/** whether this reading is preferred by heidelberger academy of science **/
	boolean preferred_reading;

	/**
	 * whether this sign was newly added to inscript and not yet marked up as
	 * its colleagues
	 **/
	boolean missing = false; // TODO: replace by shape.isEmpty

	/**
	 * Has the character been modified since the last load/save?
	 */
	private boolean modified = false;

	/** empty constructor, needed sometimes in technical procedures **/
	public InscriptCharacter() {

	}

	/**
	 * Basic Constructor
	 * 
	 * @param s
	 *            parent Inscript Object
	 * @param chStandard
	 *            standard character used
	 * @param chOriginal
	 *            original character
	 * @param cert
	 *            certainty of a character
	 * @param preferred
	 *            whether character is from preferred reading
	 * @param var
	 *            character's reading variant number (preferred reading <->
	 *            variant=0)
	 * @param r
	 *            character's row number
	 * @param c
	 *            character's column number
	 * @param n
	 *            character's number
	 * @param base
	 *            character's snippet left upper corner coordinates
	 * @param delta
	 *            character's snippet dimension
	 */

	public InscriptCharacter(Inscript s, String chStandard, String chOriginal, float cert, boolean preferred, int var,
			int r, int c, int n, SnippetShape sh) {
		// initialize parent inscript
		inscript = s;
		// initialize further attributes
		characterStandard = chStandard;
		characterOriginal = chOriginal;
		this.cert = cert;
		preferred_reading = preferred;
		variant = var;
		column = c;
		this.row = r;
		this.number = n;
		this.id = this.inscript.getId() + "_" + this.row + "_" + this.column;
		this.shape = sh;
	}

	public int getNumber() {
		return this.number;
	}

	public int getColumn() {
		return this.column;
	}

	public int getRow() {
		return this.row;
	}

	/**
	 * Resize cahracter's snippet's marking.
	 * 
	 * @param direction
	 *            direction code. Possible values: n/nw/w/sw/s/se/e/ne,
	 *            correspond to world directions.
	 * @param dx
	 *            x resize
	 * @param dy
	 *            x resize
	 */
	public void resizeSnippet(String direction, int dx, int dy) {
		if (direction == null)
			return;
		else {
			if (direction.equals("nw")) {
				shape.resizeNW(-dx, -dy);
			} else if (direction.equals("n")) {
				shape.resizeN(-dy);
			} else if (direction.equals("ne")) {
				shape.resizeNE(dx, -dy);
			} else if (direction.equals("e")) {
				shape.resizeE(dx);
			} else if (direction.equals("se")) {
				shape.resizeSE(dx, dy);
			} else if (direction.equals("s")) {
				shape.resizeS(dy);
			} else if (direction.equals("sw")) {
				shape.resizeSW(-dx, dy);
			} else if (direction.equals("w")) {
				shape.resizeW(-dx);
			}

			modified = true;
		}
	}

	/**
	 * Move character's snippet's marking
	 * 
	 * @param dx
	 *            x shift
	 * @param dy
	 *            y shift
	 */
	public void moveSnippet(int dx, int dy) {
		shape.shift(dx, dy);
		modified = true;
	}

	public void rotateSnippet(double phi) {
		shape.rotate(phi);
		modified = true;
	}

	/**
	 * Produce direction code, basing on current Cursor form
	 * 
	 * @param c
	 *            current cursor
	 * @return direction code
	 */
	public String computeMoveDirection(Cursor c) {
		if (c.getType() == Cursor.NW_RESIZE_CURSOR)
			return new String("nw");
		if (c.getType() == Cursor.N_RESIZE_CURSOR)
			return new String("n");
		if (c.getType() == Cursor.NE_RESIZE_CURSOR)
			return new String("ne");
		if (c.getType() == Cursor.E_RESIZE_CURSOR)
			return new String("e");
		if (c.getType() == Cursor.SE_RESIZE_CURSOR)
			return new String("se");
		if (c.getType() == Cursor.S_RESIZE_CURSOR)
			return new String("s");
		if (c.getType() == Cursor.SW_RESIZE_CURSOR)
			return new String("sw");
		if (c.getType() == Cursor.W_RESIZE_CURSOR)
			return new String("w");
		return null;
	}

	/**
	 * Generate XUpdate.
	 * 
	 * @return XUpdate
	 */
	public String getXUpdate(String collection) {
		if (!shape.isEmpty()) {
			return "    <xu:append select=\"collection('" + collection + "')//char[@xmlid=\'U+"
			+ NumUtil.dec2hex(characterStandard.codePointAt(0)).toUpperCase() + "\']\">\n"
			+ "       <xu:element name=\"appearance\">\n" + "           <xu:attribute name=\"character\">"
			+ this.characterStandard + "</xu:attribute>\n" + "           <xu:attribute name=\"original\">"
			+ this.characterOriginal + "</xu:attribute>\n" + "           <xu:attribute name=\"id\">" + this.id
			+ "</xu:attribute>\n" + "           <xu:attribute name=\"preferred_reading\">"
			+ this.preferred_reading + "</xu:attribute>\n" + "           <xu:attribute name=\"variant\">"
			+ this.variant + "</xu:attribute>\n" + "           <xu:attribute name=\"cert\">" + this.cert
			+ "</xu:attribute>\n" + "           <xu:attribute name=\"nr\">" + inscript.getId() + "_"
			+ this.getNumber() + "</xu:attribute>\n" + "           <source>" + inscript.getId() + "</source>\n"
			+ "           <rubbing>" + inscript.getRelativeRubbingPath() + "</rubbing>\n"
			+ "           <graphic>" + path_to_snippet + "</graphic>\n" + "           <coordinates>\n"
			+ "           <base x=\"" + shape.base.x + "\" y=\"" + shape.base.y + "\" width=\""
			+ shape.base.width + "\" height=\"" + shape.base.height + "\" />\n" + "           <angle phi=\""
			+ shape.angle + "\"/>\n" + "           </coordinates>\n" + "       </xu:element>\n"
					+ "    </xu:append>\n";
		} else {
			return "";
		}
	}

	/**
	 * Generate an org.jdom.Element <appearance> representation of Character
	 * Notice: is used for saving marking inforamtion locally
	 * 
	 * @return org.jdom.Element representation of Character
	 */
	public Element toAppearance() {
		Element appearance = new Element("appearance");

		appearance.setAttribute("character", this.characterStandard);
		appearance.setAttribute("character", this.characterOriginal);
		appearance.setAttribute("id", this.id);
		appearance.setAttribute("preferred_reading", String.valueOf(this.preferred_reading));
		appearance.setAttribute("variant", String.valueOf(this.variant));
		appearance.setAttribute("cert", String.valueOf(this.cert));
		appearance.setAttribute("nr", String.valueOf(inscript.getId() + "_" + this.getNumber()));

		Element source = new Element("source");
		source.setText(inscript.getId());

		Element rubbing = new Element("rubbing");
		rubbing.setText(inscript.getAbsoluteRubbingPath());

		Element graphic = new Element("graphic");
		graphic.setText(this.path_to_snippet);

		Element coordinates = shape.toElement();

		appearance.addContent(source);
		appearance.addContent(rubbing);
		appearance.addContent(graphic);
		appearance.addContent(coordinates);

		return appearance;
	}

	/**
	 * Generate character from org.jdom.Element <appearance> Notice: is used for
	 * loading marking information from database after having received
	 * corresponding <appearance>s list Notice: is used for loading marking
	 * information from local file
	 * 
	 * @param inscript
	 *            inscript object that contains character generated
	 * @param appearance
	 *            org.jdom.Element <appearance>
	 * @return
	 */
	public static InscriptCharacter fromAppearance(Inscript inscript, Element appearance) {
		String chStandard = appearance.getAttributeValue("character");
		String chOriginal = appearance.getAttributeValue("original");
		String chId = appearance.getAttributeValue("id");
		boolean preferred = Boolean.parseBoolean(appearance.getAttributeValue("preferred_reading"));
		float cert = Float.parseFloat(appearance.getAttributeValue("cert"));
		int var = Integer.parseInt(appearance.getAttributeValue("variant"));
		int n = Integer.parseInt(appearance.getAttributeValue("nr").substring((inscript.getId() + "_").length()));
		String rc = chId.substring((inscript.getId() + "_").length());
		int r = Integer.parseInt(rc.substring(0, rc.indexOf("_")));
		int c = Integer.parseInt(rc.substring(rc.indexOf("_") + 1));
		SnippetShape shape = SnippetShape.fromElement(appearance.getChild("coordinates"));

		InscriptCharacter sign = new InscriptCharacter(inscript, chStandard, chOriginal, cert, preferred, var, r, c, n,
				shape);

		return sign;
	}

	/**
	 * Update character's snippet's marking coordinates from Rectangle object
	 * 
	 * @param rectangle
	 *            marking rectangle
	 */
	public void updateSnippet(SnippetShape shape) {
		this.shape = shape.clone();
		modified = true;
	}

	private static void setAlpha(Graphics2D g, float alpha) {
		int rule = AlphaComposite.SRC_OVER;
		AlphaComposite ac;
		ac = AlphaComposite.getInstance(rule, alpha);
		g.setComposite(ac);
	}

	public void drawCharacter(Graphics2D g, Properties preferences2) {
		// produce graphics derivatives
		shape.derivate();

		// adjust font
		Font f = inscript.getFont();
		float fontBaseSize = (Math.min(shape.base.width, shape.base.height));

		Float alpha;
		Color color;

		// draw border
		alpha = Float.parseFloat(preferences2.getProperty("local.alpha.marking.border"));
		color = PrefUtil.String2Color(preferences2.getProperty("local.color.marking.border"));
		setAlpha(g, alpha);
		g.setColor(color);
		g.draw(shape.main);

		// draw marking
		if (!equals(inscript.getActiveCharacter())) {
			alpha = Float.parseFloat(preferences2.getProperty("local.alpha.marking.p"));
			color = PrefUtil.String2Color(preferences2.getProperty("local.color.marking.p"));
		} else {
			alpha = Float.parseFloat(preferences2.getProperty("local.alpha.marking.a"));
			color = PrefUtil.String2Color(preferences2.getProperty("local.color.marking.a"));
		}
		setAlpha(g, alpha);
		g.setColor(color);
		g.fill(shape.main);

		// draw text
		alpha = Float.parseFloat(preferences2.getProperty("local.alpha.text"));
		color = PrefUtil.String2Color(preferences2.getProperty("local.color.text"));
		setAlpha(g, alpha);
		g.setColor(color);

		AffineTransform textrotator = g.getTransform();
		textrotator.rotate(-shape.angle, shape.center.x, shape.center.y);
		g.setTransform(textrotator);
		if (inscript.isCharacterVisible()) {
			g.setFont(f.deriveFont(fontBaseSize));
			g.drawString(characterStandard, shape.base.x, shape.base.y + g.getFontMetrics().getHeight() * 25 / 40);
		}
		if (inscript.isNumberVisible()) {
			g.setFont(f.deriveFont(fontBaseSize / 3.0f));
			g.drawString(String.valueOf(getNumber()), shape.base.x, shape.base.y + g.getFontMetrics().getAscent());
		}
		if (inscript.isRowColumnVisible()) {
			g.setFont(f.deriveFont(fontBaseSize / 5.0f));
			g.drawString("(" + String.valueOf(row) + "," + String.valueOf(column) + ")", shape.base.x, shape.base.y
					+ g.getFontMetrics().getAscent());
		}
		AffineTransform textderotator = g.getTransform();
		textderotator.rotate(shape.angle, shape.center.x, shape.center.y);
		g.setTransform(textderotator);
	}

	/**
	 * @param shape the shape to set
	 */
	public void setShape(SnippetShape shape) {
		this.shape = shape;
	}

	/**
	 * @return the shape
	 */
	public SnippetShape getShape() {
		return shape;
	}

	public void clearShape() {
		if (!shape.isEmpty()) {
			shape.clear();
			modified = true;
		}
	}

	public boolean isModified() {
		return modified;
	}

	public void setModified(boolean modified) {
		this.modified = modified;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
}
