package org.stonesutras.snippettool.model;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.geom.Rectangle2D;

import org.jdom.Element;

public class SnippetShape {

    public Rectangle2D.Double base;
	public float angle;

    public Point2D.Double center;
	public Polygon main;
	public Polygon cursor_outer;
	public Polygon cursor_inner;

	public boolean isEmpty() {
		return base.isEmpty();
	}

	public void clear() {
        base = new Rectangle2D.Double();
	}

    public SnippetShape(Rectangle r) {
        base = new Rectangle2D.Double(r.x, r.y, r.width, r.height);
		angle = 0.0f;
		derivate();
	}

    public SnippetShape(Rectangle2D.Double base2, float angle) {
        base = new Rectangle2D.Double(base2.x, base2.y, base2.width, base2.height);
		this.angle = angle;
		derivate();
	}

	public void derivate() {
        center = new Point2D.Double(base.x + base.width / 2, base.y + base.height / 2);

		main = transformRectangle(base, 1.0);
		cursor_outer = transformRectangle(base, 1.1);
		cursor_inner = transformRectangle(base, 0.9);

		main = rotatePolygonO(main, angle, center);
		cursor_outer = rotatePolygonO(cursor_outer, angle, center);
		cursor_inner = rotatePolygonO(cursor_inner, angle, center);
	}

    public Polygon transformRectangle(Rectangle2D.Double base2, double coefficient) {
		coefficient -= 1.0;
        Point pul = new Point((int) (base2.x - base2.width / 2 * coefficient), (int) (base2.y - base2.height / 2
            * coefficient));
        Point pur = new Point((int) (base2.x + base2.width + base2.width / 2 * coefficient),
            (int) (base2.y - base2.height / 2 * coefficient));
        Point plr = new Point((int) (base2.x + base2.width + base2.width / 2 * coefficient), (int) (base2.y
            + base2.height + base2.height / 2 * coefficient));
        Point pll = new Point((int) (base2.x - base2.width / 2 * coefficient),
            (int) (base2.y + base2.height + base2.height / 2 * coefficient));
		Polygon poly = new Polygon(new int[] { pul.x, pur.x, plr.x, pll.x }, new int[] { pul.y, pur.y, plr.y, pll.y },
				4);
		return poly;
	}

	public void rotate(double d) {
		angle += d;
        derivate();
	}

	public void shift(int dx, int dy) {
		base.x += dx;
		base.y += dy;
        derivate();
	}

    public void scale(double factor) {
        base.x *= factor;
        base.y *= factor;
        base.height *= factor;
        base.width *= factor;
        derivate();
    }

    public Point rotatePointO(Point oldpoint, float angle, double x, double y) {
		Point newpoint = new Point(oldpoint);
        newpoint.x = (int) (+(oldpoint.x - x) * Math.cos(angle) + (oldpoint.y - y) * Math.sin(angle) + x);
        newpoint.y = (int) (-(oldpoint.x - x) * Math.sin(angle) + (oldpoint.y - y) * Math.cos(angle) + y);
		return newpoint;
	}

	public String getPointRelative(Point point) {
		Point p = rotatePointO(point, -angle, center.x, center.y);
		Rectangle out = rotatePolygonO(cursor_outer, -angle, center).getBounds();
		Rectangle in = rotatePolygonO(cursor_inner, -angle, center).getBounds();

		if (in.contains(p)) {
			return "in";
		} else if (out.contains(p)) {
			if (new Rectangle(out.x, out.y, (out.width - in.width) / 2, (out.height - in.height) / 2).contains(p))
				return "nw";
			else if (new Rectangle(out.x + (out.width - in.width) / 2, out.y, in.width, (out.height - in.height) / 2)
			.contains(p))
				return "n";
			else if (new Rectangle(out.x + (out.width - in.width) / 2 + in.width, out.y, (out.width - in.width) / 2,
					(out.height - in.height) / 2).contains(p))
				return "ne";
			else if (new Rectangle(out.x + (out.width - in.width) / 2 + in.width, out.y + (out.height - in.height) / 2,
					(out.width - in.width) / 2, in.height).contains(p))
				return "e";
			else if (new Rectangle(out.x + (out.width - in.width) / 2 + in.width, out.y + (out.height - in.height) / 2
					+ in.height, (out.width - in.width) / 2, (out.height - in.height) / 2).contains(p))
				return "se";
			else if (new Rectangle(out.x + (out.width - in.width) / 2,
					out.y + (out.height - in.height) / 2 + in.height, in.width, (out.height - in.height) / 2)
			.contains(p))
				return "s";
			else if (new Rectangle(out.x, out.y + (out.height - in.height) / 2 + in.height, (out.width - in.width) / 2,
					(out.height - in.height) / 2).contains(p))
				return "sw";
			else if (new Rectangle(out.x, out.y + (out.height - in.height) / 2, (out.width - in.width) / 2, in.height)
			.contains(p))
				return "w";
			else
				return "cursor";
		} else {
			return "out";
		}
	}

    public static Polygon rotatePolygonO(Polygon oldpoly, double d, Double center2) {
		Polygon newpoly = new Polygon(oldpoly.xpoints, oldpoly.ypoints, oldpoly.npoints);
		for (int i = 0; i < oldpoly.npoints; i++) {
            newpoly.xpoints[i] = (int) (+(oldpoly.xpoints[i] - center2.x) * Math.cos(d)
                + (oldpoly.ypoints[i] - center2.y) * Math.sin(d) + center2.x);
            newpoly.ypoints[i] = (int) (-(oldpoly.xpoints[i] - center2.x) * Math.sin(d)
                + (oldpoly.ypoints[i] - center2.y) * Math.cos(d) + center2.y);
		}
		return newpoly;
	}

	public static Polygon shiftPolygon(Polygon oldpoly, int dx, int dy) {
		Polygon newpoly = new Polygon(oldpoly.xpoints, oldpoly.ypoints, oldpoly.npoints);
		for (int i = 0; i < oldpoly.npoints; i++) {
			newpoly.xpoints[i] = oldpoly.xpoints[i] - dx;
			newpoly.ypoints[i] = oldpoly.ypoints[i] - dy;
		}
		return newpoly;
	}

	public void resizeS(int dy) {
        base = new Rectangle2D.Double(base.x, base.y, base.width, base.height + dy);
	}

	public void resizeN(int dy) {
        base = new Rectangle2D.Double(base.x, base.y - dy, base.width, base.height + dy);
	}

	public void resizeW(int dx) {
        base = new Rectangle2D.Double(base.x - dx, base.y, base.width + dx, base.height);
	}

	public void resizeE(int dx) {
        base = new Rectangle2D.Double(base.x, base.y, base.width + dx, base.height);
	}

	public void resizeNE(int dx, int dy) {
		resizeN(dy);
		resizeE(dx);
	}

	public void resizeNW(int dx, int dy) {
		resizeN(dy);
		resizeW(dx);
	}

	public void resizeSE(int dx, int dy) {
		resizeS(dy);
		resizeE(dx);
	}

	public void resizeSW(int dx, int dy) {
		resizeS(dy);
		resizeW(dx);
	}

	public Element toElement() {
		Element sh = new Element("coordinates");
		Element r = new Element("base");
		Element a = new Element("angle");
		r.setAttribute("x", String.valueOf(Math.round(base.x)));
        r.setAttribute("y", String.valueOf(Math.round(base.y)));
		r.setAttribute("width", String.valueOf(Math.round(base.width)));
		r.setAttribute("height", String.valueOf(Math.round(base.height)));
		a.setAttribute("phi", String.valueOf(angle));
		sh.addContent(r);
		sh.addContent(a);
		return sh;
	}

	public static SnippetShape fromElement(Element e) {
		SnippetShape sh = null;
		if (e.getContentSize() > 0) {
			Element r = e.getChild("base");
            Rectangle2D.Double rectangle = new Rectangle2D.Double(Integer.valueOf(r.getAttributeValue("x")),
                Integer.valueOf(r
					.getAttributeValue("y")), Integer.valueOf(r.getAttributeValue("width")), Integer.valueOf(r
							.getAttributeValue("height")));
			Element a = e.getChild("angle");
			float angle = Float.valueOf(a.getAttributeValue("phi"));
			sh = new SnippetShape(rectangle, angle);
		} else {
			Rectangle rectangle = new Rectangle(Integer.valueOf(e.getAttributeValue("x")), Integer.valueOf(e
					.getAttributeValue("y")), Integer.valueOf(e.getAttributeValue("width")), Integer.valueOf(e
							.getAttributeValue("height")));
			sh = new SnippetShape(rectangle);
		}
		return sh;
	}

	@Override
	public SnippetShape clone() {
		SnippetShape cloned = new SnippetShape(this.base, this.angle);
		return cloned;
	}

}
