package org.abratuhi.snippettool.model;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;

import org.jdom.Element;

public class SnippetShape {

	public Rectangle base;
	public float angle;

	public Point center;
	public Polygon main;
	public Polygon cursor_outer;
	public Polygon cursor_inner;

	public SnippetShape(Rectangle rectangle) {
		base = new Rectangle(rectangle);
		angle = 0.0f;
		derivate();
	}

	public SnippetShape(Rectangle rectangle, float angle) {
		base = new Rectangle(rectangle);
		this.angle = angle;
		derivate();
	}

	public void derivate() {
		center = new Point(base.x + base.width / 2, base.y + base.height / 2);

		main = transformRectangle(base, 1.0);
		cursor_outer = transformRectangle(base, 1.1);
		cursor_inner = transformRectangle(base, 0.9);

		main = rotatePolygonO(main, angle, center);
		cursor_outer = rotatePolygonO(cursor_outer, angle, center);
		cursor_inner = rotatePolygonO(cursor_inner, angle, center);
	}

	public Polygon transformRectangle(Rectangle rectangle, double coefficient) {
		coefficient -= 1.0;
		Point pul = new Point((int) (rectangle.x - rectangle.width / 2
				* coefficient), (int) (rectangle.y - rectangle.height / 2
				* coefficient));
		Point pur = new Point(
				(int) (rectangle.x + rectangle.width + rectangle.width / 2
						* coefficient), (int) (rectangle.y - rectangle.height
						/ 2 * coefficient));
		Point plr = new Point(
				(int) (rectangle.x + rectangle.width + rectangle.width / 2
						* coefficient),
				(int) (rectangle.y + rectangle.height + rectangle.height / 2
						* coefficient));
		Point pll = new Point((int) (rectangle.x - rectangle.width / 2
				* coefficient),
				(int) (rectangle.y + rectangle.height + rectangle.height / 2
						* coefficient));
		Polygon poly = new Polygon(new int[] { pul.x, pur.x, plr.x, pll.x },
				new int[] { pul.y, pur.y, plr.y, pll.y }, 4);
		return poly;
	}

	public void rotate(double d) {
		angle += d;
	}

	public void shift(int dx, int dy) {
		base.x += dx;
		base.y += dy;
	}

	public Point rotatePointO(Point oldpoint, float angle, int cx, int cy) {
		Point newpoint = new Point(oldpoint);
		newpoint.x = (int) (+(oldpoint.x - cx) * Math.cos(angle)
				+ (oldpoint.y - cy) * Math.sin(angle) + cx);
		newpoint.y = (int) (-(oldpoint.x - cx) * Math.sin(angle)
				+ (oldpoint.y - cy) * Math.cos(angle) + cy);
		return newpoint;
	}

	public String getPointRelative(Point point) {
		Point p = rotatePointO(point, -angle, center.x, center.y);
		Rectangle out = rotatePolygonO(cursor_outer, -angle, center)
				.getBounds();
		Rectangle in = rotatePolygonO(cursor_inner, -angle, center).getBounds();

		if (in.contains(p)) {
			return "in";
		} else if (out.contains(p)) {
			if (new Rectangle(out.x, out.y, (out.width - in.width) / 2,
					(out.height - in.height) / 2).contains(p))
				return "nw";
			else if (new Rectangle(out.x + (out.width - in.width) / 2, out.y,
					in.width, (out.height - in.height) / 2).contains(p))
				return "n";
			else if (new Rectangle(out.x + (out.width - in.width) / 2
					+ in.width, out.y, (out.width - in.width) / 2,
					(out.height - in.height) / 2).contains(p))
				return "ne";
			else if (new Rectangle(out.x + (out.width - in.width) / 2
					+ in.width, out.y + (out.height - in.height) / 2,
					(out.width - in.width) / 2, in.height).contains(p))
				return "e";
			else if (new Rectangle(out.x + (out.width - in.width) / 2
					+ in.width, out.y + (out.height - in.height) / 2
					+ in.height, (out.width - in.width) / 2,
					(out.height - in.height) / 2).contains(p))
				return "se";
			else if (new Rectangle(out.x + (out.width - in.width) / 2, out.y
					+ (out.height - in.height) / 2 + in.height, in.width,
					(out.height - in.height) / 2).contains(p))
				return "s";
			else if (new Rectangle(out.x, out.y + (out.height - in.height) / 2
					+ in.height, (out.width - in.width) / 2,
					(out.height - in.height) / 2).contains(p))
				return "sw";
			else if (new Rectangle(out.x, out.y + (out.height - in.height) / 2,
					(out.width - in.width) / 2, in.height).contains(p))
				return "w";
			else
				return "cursor";
		} else {
			return "out";
		}
	}

	public static Polygon rotatePolygonO(Polygon oldpoly, double d, Point c) {
		Polygon newpoly = new Polygon(oldpoly.xpoints, oldpoly.ypoints,
				oldpoly.npoints);
		for (int i = 0; i < oldpoly.npoints; i++) {
			newpoly.xpoints[i] = (int) (+(oldpoly.xpoints[i] - c.x)
					* Math.cos(d) + (oldpoly.ypoints[i] - c.y) * Math.sin(d) + c.x);
			newpoly.ypoints[i] = (int) (-(oldpoly.xpoints[i] - c.x)
					* Math.sin(d) + (oldpoly.ypoints[i] - c.y) * Math.cos(d) + c.y);
		}
		return newpoly;
	}

	public static Polygon shiftPolygon(Polygon oldpoly, int dx, int dy) {
		Polygon newpoly = new Polygon(oldpoly.xpoints, oldpoly.ypoints,
				oldpoly.npoints);
		for (int i = 0; i < oldpoly.npoints; i++) {
			newpoly.xpoints[i] = oldpoly.xpoints[i] - dx;
			newpoly.ypoints[i] = oldpoly.ypoints[i] - dy;
		}
		return newpoly;
	}

	public void resizeS(int dy) {
		base = new Rectangle(base.x, base.y, base.width, base.height + dy);
	}

	public void resizeN(int dy) {
		base = new Rectangle(base.x, base.y - dy, base.width, base.height + dy);
	}

	public void resizeW(int dx) {
		base = new Rectangle(base.x - dx, base.y, base.width + dx, base.height);
	}

	public void resizeE(int dx) {
		base = new Rectangle(base.x, base.y, base.width + dx, base.height);
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
		r.setAttribute("x", String.valueOf(base.x));
		r.setAttribute("y", String.valueOf(base.y));
		r.setAttribute("width", String.valueOf(base.width));
		r.setAttribute("height", String.valueOf(base.height));
		a.setAttribute("phi", String.valueOf(angle));
		sh.addContent(r);
		sh.addContent(a);
		return sh;
	}

	public static SnippetShape fromElement(Element e) {
		SnippetShape sh = null;
		if (e.getContentSize() > 0) {
			Element r = e.getChild("base");
			Rectangle rectangle = new Rectangle(Integer.valueOf(r
					.getAttributeValue("x")), Integer.valueOf(r
					.getAttributeValue("y")), Integer.valueOf(r
					.getAttributeValue("width")), Integer.valueOf(r
					.getAttributeValue("height")));
			Element a = e.getChild("angle");
			float angle = Float.valueOf(a.getAttributeValue("phi"));
			sh = new SnippetShape(rectangle, angle);
		} else {
			Rectangle rectangle = new Rectangle(Integer.valueOf(e
					.getAttributeValue("x")), Integer.valueOf(e
					.getAttributeValue("y")), Integer.valueOf(e
					.getAttributeValue("width")), Integer.valueOf(e
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
