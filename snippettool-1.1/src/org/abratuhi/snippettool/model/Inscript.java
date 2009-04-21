package org.abratuhi.snippettool.model;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.abratuhi.snippettool.gui._panel_Options;
import org.abratuhi.snippettool.util.ImageUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

/**
 * Inscript class. Stores all inscript relevant attributes from inscript's .xml
 * description and inscript's text structured due reading variants.
 * 
 * @author Alexei Bratuhin
 * 
 */
public class Inscript {

	/** Inscript's id, e.g., HDS_1 **/
	public String id = new String();

	/** Absolute database server path to inscript's image **/
	// FIXME: Inscript shouldn't know full path
	private String absoluteRubbingPath = new String();

	/** Absolute database server path to inscript's .xml description **/
	public String path_file = new String();

	/** Inscript's image **/
	public BufferedImage image = null;

	/**
	 * Inscript's text. Structure: 1st index: continuous character numbering;
	 * 2nd index: continuous character's variant numbering; 3rd index:
	 * supplementary index for the case, when not preferred reading contains
	 * more characters, than preferred one
	 **/
	public ArrayList<ArrayList<ArrayList<InscriptCharacter>>> text = new ArrayList<ArrayList<ArrayList<InscriptCharacter>>>();

	/** Whether text is read left-to-right **/
	public boolean is_left_to_right = false;

	/** Whether character should be drawn **/
	public boolean showCharacter = true;

	/** Whether character's number should be drawn **/
	public boolean showNumber = false;

	/** Whether character's row, column must be drawn **/
	public boolean showRowColumn = false;

	/** Currently selected character index **/
	public InscriptCharacter activeCharacter = null;

	/** Font used to draw characters **/
	Font f;

	/** Marking values **/

	/** X Offset **/
	int oa;

	/** Y Offset **/
	int ob;

	/** Snippet width **/
	int a;

	/** Snippet height **/
	int b;

	/** X distance between snippets **/
	int da;

	/** Y distance between snippets **/
	int db;

	public Inscript() {
	}

	public void setFont(Font f) {
		this.f = f;
	}

	public void setFont(String localFont) {
		try {
			FileInputStream fontStream = new FileInputStream(localFont);
			Font basisfont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
			f = basisfont.deriveFont(14.0f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Font getFont() {
		return this.f;
	}

	/**
	 * Load inscript image from local file
	 * 
	 * @param img
	 *            absolute path to image
	 */
	public void setImage(File img) {
		this.image = ImageUtil.load(img);
	}

	public String getPlainText() {
		String out = new String();
		int row = 1;
		int crow = 1;
		for (int i = 0; i < text.size(); i++) {
			InscriptCharacter csign = text.get(i).get(0).get(0);
			crow = csign.row;

			if (crow != row) { // add breakline
				out += "\n";
				row = crow;
			}

			out += csign.characterStandard;
		}
		if (out.startsWith("\n")) {
			out = out.substring(1);
		}
		return out;
	}

	/**
	 * Load text from inscript's .xml description
	 * 
	 * @param xml
	 *            content of inscript's .xml description
	 */
	@SuppressWarnings("unchecked")
	public void setTextFromXML(String xml) {
		// clear old text
		text.clear();

		// prepare index variables
		int current_number = 1;
		int current_row = 1;
		int current_column = 1;

		// start
		try {
			// parse XML document
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder
					.build(new InputSource(new StringReader(xml)));
			List<Element> l = doc.getRootElement().getChildren();
			for (int i = 0; i < l.size(); i++) {
				// line break
				if (l.get(i).getName().equals("br")) {
					// remove starting newline if any
					if (i != 0)
						current_row++; // some texts start with leading
					// linebreak, which needs to be
					// eliminated explicitly
					current_column = 1; // line break -> start numbering of
					// columns from beginning
				}

				// choice, which may possibly mean no choice, but just a sign
				// formatted to choice
				// to achieve compatibility
				if (l.get(i).getName().equals("choice")) {
					// List<org.jdom.Element> lvariants =
					// l.get(i).getChildren("variant");
					List<Element> lvariants = l.get(i).getChildren();

					// set the length of preferred reading
					int basicvariant = 0;
					int basiclength = 0;
					float basiccert = 0.0f;
					for (int v = 0; v < lvariants.size(); v++) {
						float cert = Float.parseFloat(lvariants.get(v)
								.getAttributeValue("cert"));
						if (cert > basiccert) {
							basicvariant = v;
							basiclength = lvariants.get(v).getChildren().size();
							basiccert = cert;
						}
					}

					// preferred reading should be the first variant
					if (basicvariant != 0) {
						Element tvariant = lvariants.get(0);
						lvariants.set(0, lvariants.remove(basicvariant));
						lvariants.add(tvariant);
					}

					// if preferred reading is empty, dismiss the whole choice
					if (lvariants.get(0).getChildren().size() == 0) {
						continue;
					}

					// search for variant with maximum stringlength
					int maxlength = 0;
					for (int v = 0; v < lvariants.size(); v++) {
						if (lvariants.get(v).getChildren().size() > maxlength) {
							maxlength = lvariants.get(v).getChildren().size();
						}
					}

					// proceed basic length
					for (int j = 0; j < basiclength; j++) {
						ArrayList<ArrayList<InscriptCharacter>> signVariants = new ArrayList<ArrayList<InscriptCharacter>>();
						ArrayList<InscriptCharacter> signs = new ArrayList<InscriptCharacter>();
						InscriptCharacter csign = new InscriptCharacter();
						boolean supplied = false;

						for (int v = 0; v < lvariants.size(); v++) {

							if (lvariants.get(v).getChildren().size() == 0) {
								continue;
							}

							signs.clear();

							// the first reading in choice schema is preferred
							boolean preferred = (v == 0) ? true : false;
							// load cert form parent tag choice
							float cert = Float.parseFloat(lvariants.get(v)
									.getAttributeValue("cert"));
							// create variant number
							int variantnumber = v;

							if (j < lvariants.get(variantnumber).getChildren()
									.size()) { // if there is a sign with
								// indexed j in this variant

								Element cspan = (Element) lvariants.get(v)
										.getChildren().get(j);

								if (cspan == null)
									System.out
											.println("NULL Element cspan while proceeding.");
								if (cspan.getAttribute("class") == null)
									System.out
											.println("Element doesn't have 'class' attribute;\n"
													+ cspan.toString());

								if (cspan.getAttributeValue("class").equals(
										"supplied")) {
									supplied = true;
								} else {
									String ch = cspan.getText();
									String chOriginal = (cspan
											.getAttribute("original") != null) ? cspan
											.getAttributeValue("original")
											: ch;

									csign = new InscriptCharacter(this, ch,
											chOriginal, cert, preferred,
											variantnumber, current_row,
											current_column, current_number,
											new SnippetShape(new Rectangle(0,
													0, 0, 0)));

									signs.add(csign);
									signVariants
											.add((ArrayList<InscriptCharacter>) signs
													.clone());
								}
							}
						}

						if (!supplied) {
							// add variants arraylist to sutra text
							text
									.add((ArrayList<ArrayList<InscriptCharacter>>) signVariants
											.clone());
							//
							current_column++;
							current_number++;
						} else {
							current_column++;
						}
					}

					// proceed extra length
					for (int j = basiclength; j < maxlength; j++) {
						InscriptCharacter csign = new InscriptCharacter();
						int current_number_for_extra = current_number - 1; // current_number
						// was
						// already
						// incremented
						int current_column_for_extra = current_column - 1;

						for (int v = 0; v < lvariants.size(); v++) {

							if (lvariants.get(v).getChildren().size() < basiclength) {
								continue;
							}

							// the first reading in choice schema is preferred
							boolean preferred = (v == 0) ? true : false;
							// load cert form parent tag choice
							float cert = Float.parseFloat(lvariants.get(v)
									.getAttributeValue("cert"));
							// create variant number
							int variantnumber = v;

							if (j < lvariants.get(variantnumber).getChildren()
									.size()) { // if there is a sign with
								// indexed j in this variant

								Element cspan = (Element) lvariants.get(v)
										.getChildren().get(j);

								if (cspan == null)
									System.out
											.println("NULL Element cspan while proceeding.");
								if (cspan.getAttribute("class") == null)
									System.out
											.println("Element doesn't have 'class' attribute;\n"
													+ cspan.toString());

								if (cspan.getAttributeValue("class").equals(
										"supplied")) {

								} else {
									String ch = cspan.getText();
									String chOriginal = (cspan
											.getAttribute("original") != null) ? cspan
											.getAttributeValue("original")
											: ch;

									csign = new InscriptCharacter(this, ch,
											chOriginal, cert, preferred,
											variantnumber, current_row,
											current_column_for_extra,
											current_number_for_extra,
											new SnippetShape(new Rectangle(0,
													0, 0, 0)));

									// no imagesign -> attach it to last placed
									// sign
									// -1, current_number starts with 1, not 0
									// and in arraylist numbering starts with 0
									text.get(current_number_for_extra - 1).get(
											variantnumber).add(csign);
								}
							}
						}
					}
				}
			}
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Update characters' snippet marking coordinates and dimension using
	 * results from querying the inscript database
	 * 
	 * @param appearances
	 *            <appearance>s as result of querying database
	 */
	public void updateCoordinates(Element[] appearances) {
		// transform to list of Characters
		ArrayList<InscriptCharacter> tarrayOfSigns = new ArrayList<InscriptCharacter>();
		for (Element appearance : appearances)
			tarrayOfSigns.add(InscriptCharacter
					.fromAppearance(this, appearance));

		// use each appearance's number to update coordinates
		for (int i = 0; i < tarrayOfSigns.size(); i++) {
			InscriptCharacter csign = tarrayOfSigns.get(i);
			updateSnippet(csign.shape, csign.row, csign.column);
		}

		// check, whether all signs have become coordinates assigned, if not,
		// mark those as missing
		for (int i = 0; i < text.size(); i++) {
			for (int j = 0; j < text.get(i).size(); j++) {
				for (int k = 0; k < text.get(i).get(j).size(); k++) {
					InscriptCharacter csign = text.get(i).get(j).get(k);
					if (csign.shape.base.width == 0
							|| csign.shape.base.height == 0) {
						csign.missing = true;
					}
				}
			}
		}
	}

	/**
	 * 
	 * @param list
	 *            list of character objects
	 */
	@SuppressWarnings("unchecked")
	public void setTextFromArrayList(ArrayList<InscriptCharacter> list) {
		// sort tarrayOfSigns after their continuous number
		for (int i = 0; i < list.size(); i++) {
			for (int j = i; j < list.size(); j++) {
				if (list.get(i).number > list.get(j).number) { // handle
					// different
					// signs
					InscriptCharacter tempsign = list.get(i);
					list.set(i, list.get(j));
					list.set(j, tempsign);
				} else {
					if (list.get(i).number == list.get(j).number
							&& list.get(i).variant > list.get(j).variant) { // handle
						// variants
						// of
						// the
						// same
						// sign
						InscriptCharacter tempsign = list.get(i);
						list.set(i, list.get(j));
						list.set(j, tempsign);
					}
				}
			}
		}

		// add sign-variants to inscript's signs
		int lnumber = 0; // remembering that numbers start from 1
		int lvariant = 0;

		text.clear();

		ArrayList<ArrayList<InscriptCharacter>> signVariants = new ArrayList<ArrayList<InscriptCharacter>>();
		ArrayList<InscriptCharacter> signs = new ArrayList<InscriptCharacter>();
		InscriptCharacter csign = new InscriptCharacter();

		for (int i = 0; i < list.size(); i++) {
			csign = list.get(i);
			int cnumber = csign.number;
			int cvariant = csign.variant;
			if (cnumber == lnumber) {
				if (cvariant == lvariant) {
					signVariants = text.get(cnumber);
					signs = signVariants.get(cvariant);
					signs.add(csign);
				} else {
					signs = new ArrayList<InscriptCharacter>();

					signs.add(csign);
					signVariants.add(signs);
				}
			} else {
				lvariant = 0;

				signVariants = new ArrayList<ArrayList<InscriptCharacter>>();
				signs = new ArrayList<InscriptCharacter>();

				signs.add(csign);
				signVariants.add(signs);
				text.add((ArrayList<ArrayList<InscriptCharacter>>) signVariants
						.clone());
			}

			lnumber = cnumber;
			lvariant = cvariant;
		}

	}

	public InscriptCharacter getCharacterNV(int n, int v) {
		return text.get(n).get(v).get(0);
	}

	public InscriptCharacter getCharacterRC(int r, int c) {
		for (int i = 0; i < text.size(); i++) {
			InscriptCharacter ch = getCharacterNV(i, 0);
			if (ch.row == r && ch.column == c) {
				return ch;
			}
		}
		return null;
	}

	/**
	 * Resize character's snippet's marking. Notice: all corresponding
	 * characters (those with the same continuous number) will be automatically
	 * resized too. Notice: generally applies - all variants marking are
	 * adjusted using preferred reading's marking.
	 * 
	 * @param sn
	 *            character
	 * @param dir
	 *            resize direction
	 * @param dx
	 *            x resize
	 * @param dy
	 *            y resize
	 */
	public void resizeSnippet(InscriptCharacter sn, String dir, int dx, int dy) {
		int index = sn.getNumber() - 1; // all variants must be resized
		for (int j = 0; j < this.text.get(index).size(); j++) {
			for (int k = 0; k < this.text.get(index).get(j).size(); k++) {
				this.text.get(index).get(j).get(k).resizeSnippet(dir, dx, dy);
			}
		}
	}

	/**
	 * Move character's snippet's marking. Notice: all corresponding characters
	 * (those with the same continuous number) will be automatically resized
	 * too. Notice: generally applies - all variants marking are adjusted using
	 * preferred reading's marking.
	 * 
	 * @param sn
	 *            character
	 * @param dx
	 *            x shift
	 * @param dy
	 *            y shift
	 */
	public void moveSnippet(InscriptCharacter sn, int dx, int dy) {
		int index = sn.getNumber() - 1; // all variants must be moved
		for (int j = 0; j < this.text.get(index).size(); j++) {
			for (int k = 0; k < this.text.get(index).get(j).size(); k++) {
				this.text.get(index).get(j).get(k).moveSnippet(dx, dy);
			}
		}
	}

	public void rotateSnippet(InscriptCharacter sn, double phi) {
		int index = sn.getNumber() - 1; // all variants must be moved
		for (int j = 0; j < this.text.get(index).size(); j++) {
			for (int k = 0; k < this.text.get(index).get(j).size(); k++) {
				this.text.get(index).get(j).get(k).rotateSnippet(phi);
			}
		}
	}

	public void updateSnippet(SnippetShape shape, int r, int c) {
		int indexTarget = -1;
		for (int i = 0; i < text.size(); i++) {
			if (text.get(i).get(0).get(0).row == r
					&& text.get(i).get(0).get(0).column == c) {
				indexTarget = i;
				break;
			}
		}

		// check whether indexTarget found
		if (indexTarget != -1) {
			for (int j = 0; j < text.get(indexTarget).size(); j++) {
				for (int k = 0; k < text.get(indexTarget).get(j).size(); k++) {
					text.get(indexTarget).get(j).get(k).updateSnippet(shape);
				}
			}
		}
	}

	public void updatePathToSnippet(String pathToSnippet, int indexTarget) {
		for (int j = 0; j < this.text.get(indexTarget).size(); j++) {
			for (int k = 0; k < this.text.get(indexTarget).get(j).size(); k++) {
				text.get(indexTarget).get(j).get(k).path_to_snippet = pathToSnippet;
			}
		}
	}

	/**
	 * Load values for autoguided marking from Snippet-tool's Options component.
	 * 
	 * @param options
	 *            Snippet-Tool options component
	 * @param missingOnly
	 *            whether marking schema should be applied for characters not
	 *            already in database only
	 */
	public void loadMarkingSchema(_panel_Options options, boolean missingOnly) {
		// load markup parameters
		oa = Integer.valueOf(options.jtf_oa.getText());
		ob = Integer.valueOf(options.jtf_ob.getText());
		a = Integer.valueOf(options.jtf_a.getText());
		b = Integer.valueOf(options.jtf_b.getText());
		da = Integer.valueOf(options.jtf_da.getText());
		db = Integer.valueOf(options.jtf_db.getText());

		// check whether all markup snippets are seen
		// use preferred reading's signs
		int dim_x = image.getWidth();
		int dim_y = image.getHeight();
		int x_width = 0;
		int y_height = 0;
		int max_row = 0;
		int max_column = 0;
		for (int i = 0; i < text.size(); i++) {
			if (text.get(i).get(0).get(0).column > max_column)
				max_column = text.get(i).get(0).get(0).column;
			if (text.get(i).get(0).get(0).row > max_row)
				max_row = text.get(i).get(0).get(0).row;
		}
		x_width = oa + (max_row - 1) * (a + da);
		y_height = ob + (max_column - 1) * (b + db);
		if (x_width > dim_x || y_height > dim_y) { // TODO: show warning message
			return;
		}

		// apply parameters if check passed
		for (int i = 0; i < text.size(); i++) {
			ArrayList<ArrayList<InscriptCharacter>> signvariants = text.get(i);
			for (int j = 0; j < signvariants.size(); j++) {
				for (int k = 0; k < signvariants.get(j).size(); k++) {
					InscriptCharacter csign = signvariants.get(j).get(k);

					// if dimension greater 0, sign already has coordinates
					// assigned -> no need to generate
					if (missingOnly && csign.missing) {
						continue;
					} else {
						int r = csign.getRow() - 1;
						int c = csign.getColumn() - 1;
						if (is_left_to_right) {
							csign.shape = new SnippetShape(new Rectangle(
									new Point(oa + (a + da) * r, ob + (b + db)
											* c), new Dimension(a, b)));
						} else {
							csign.shape = new SnippetShape(new Rectangle(
									new Point(dim_x - oa - a - (a + da) * r, ob
											+ (b + db) * c),
									new Dimension(a, b)));
						}
					}
				}
			}
		}
	}

	public String getXUpdate(String collection) {
		String xupdate = "";
		for (int i = 0; i < text.size(); i++) {
			for (int j = 0; j < text.get(i).size(); j++) {
				for (int k = 0; k < text.get(i).get(j).size(); k++) {
					xupdate += text.get(i).get(j).get(k).getXUpdate(collection);
				}
			}
		}
		xupdate = "<xu:modifications version=\'1.0\' xmlns:xu=\'http://www.xmldb.org/xupdate\'>"
				+ xupdate;
		xupdate = xupdate + "</xu:modifications>";
		// System.out.println(xupdate);
		return xupdate;
	}

	/**
	 * clear inscript's information
	 */
	public void clear() {
		image = null;
		text.clear();
		id = new String();
		path_file = new String();
		setAbsoluteRubbingPath(new String());
		activeCharacter = null;
	}

	/**
	 * @param absoluteRubbingPath
	 *            the absoluteRubbingPath to set
	 */
	public void setAbsoluteRubbingPath(String absoluteRubbingPath) {
		this.absoluteRubbingPath = absoluteRubbingPath;
	}

	/**
	 * Get the absolute rubbing path.
	 * 
	 * @return the absoluteRubbingPath
	 */
	public String getAbsoluteRubbingPath() {
		return absoluteRubbingPath;
	}

	public String getRelativeRubbingPath() {
		return absoluteRubbingPath.replaceFirst("xmldb:.*?/db/", "");
	}

}
