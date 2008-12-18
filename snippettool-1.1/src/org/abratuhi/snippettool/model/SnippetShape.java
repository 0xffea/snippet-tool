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
	public Rectangle bound;
	
	public SnippetShape(Rectangle rectangle){
		base = new Rectangle(rectangle);
		angle = 0.0f;
		derivate();
	}
	
	public SnippetShape(Rectangle rectangle, float angle){
		base = new Rectangle(rectangle);
		this.angle = angle;
		derivate();
	}
	
	public void derivate(){
		center = new Point(base.x + base.width/2, base.y + base.height/2);
		
		main = transformRectangle(base, 1.0);
		cursor_outer = transformRectangle(base, 1.1);
		cursor_inner = transformRectangle(base, 0.9);
		
		main = rotatePolygonO(main, angle, center);
		cursor_outer = rotatePolygonO(cursor_outer, angle, center);
		cursor_inner = rotatePolygonO(cursor_inner, angle, center);

		bound = main.getBounds();
	}
	
	public Polygon transformRectangle(Rectangle rectangle, double coefficient){
		coefficient -= 1.0;
		Point pul = new Point((int) (rectangle.x - rectangle.width/2*coefficient), (int) (rectangle.y - rectangle.height/2*coefficient));
		Point pur = new Point((int) (rectangle.x + rectangle.width + rectangle.width/2*coefficient), (int) (rectangle.y - rectangle.height/2*coefficient));
		Point plr = new Point((int) (rectangle.x + rectangle.width + rectangle.width/2*coefficient), (int) (rectangle.y + rectangle.height + rectangle.height/2*coefficient));
		Point pll = new Point((int) (rectangle.x - rectangle.width/2*coefficient), (int) (rectangle.y + rectangle.height + rectangle.height/2*coefficient));
		Polygon  poly = new Polygon(new int[]{pul.x, pur.x, plr.x, pll.x}, new int[]{pul.y, pur.y, plr.y, pll.y}, 4);
		return poly;
	}
	
	public void rotate(double d){
		angle += d;
	}
	
	public void shift(int dx, int dy){
		base.x += dx;
		base.y += dy;
	}
	
	public Point rotatePointO(Point oldpoint, float angle, int cx, int cy){
		Point newpoint = new Point(oldpoint);
		newpoint.x = (int) (+ (oldpoint.x-cx)*Math.cos(angle) + (oldpoint.y-cy)*Math.sin(angle) + cx);
		newpoint.y = (int) (- (oldpoint.x-cx)*Math.sin(angle) + (oldpoint.y-cy)*Math.cos(angle) + cy);
		return newpoint;
	}
	
	/*public String getPointRelative(Point point){
		if(cursor_inner.contains(point)){
			return "in";
		}
		else if(cursor_outer.contains(point)){
			double radius = Math.max(base.width, base.height) / 2;
			double dist = center.distance(point);
			if(point.x < center.x && point.y < center.y && dist > radius) return "nw";
			if(point.x < center.x && point.y < center.y && dist < radius) return "n";
			if(point.x < center.x && point.y < center.y && dist > radius) return "ne";
			if(point.x < center.x && point.y < center.y && dist < radius) return "e";
			if(point.x < center.x && point.y < center.y && dist > radius) return "se";
			if(point.x < center.x && point.y < center.y && dist < radius) return "s";
			if(point.x < center.x && point.y < center.y && dist > radius) return "sw";
			if(point.x < center.x && point.y < center.y && dist < radius) return "w";
			return "cursor";	//TODO
		}
		else{
			return "out";
		}
	}*/
	
	public static Polygon rotatePolygonO(Polygon oldpoly, double d, Point c){
		Polygon newpoly =new Polygon(oldpoly.xpoints, oldpoly.ypoints, oldpoly.npoints);
		for(int i=0; i<oldpoly.npoints; i++){
			newpoly.xpoints[i] = (int) (+ (oldpoly.xpoints[i]-c.x)*Math.cos(d) + (oldpoly.ypoints[i]-c.y)*Math.sin(d) + c.x);
			newpoly.ypoints[i] = (int) (- (oldpoly.xpoints[i]-c.x)*Math.sin(d) + (oldpoly.ypoints[i]-c.y)*Math.cos(d) + c.y);
		}
		return newpoly;
	}
	
	public static Polygon shiftPolygon(Polygon oldpoly, int dx, int dy){
		Polygon newpoly =new Polygon(oldpoly.xpoints, oldpoly.ypoints, oldpoly.npoints);
		for(int i=0; i<oldpoly.npoints; i++){
			newpoly.xpoints[i] = oldpoly.xpoints[i] - dx;
			newpoly.ypoints[i] = oldpoly.ypoints[i] - dy;
		}
		return newpoly;
	}
	
	public void resizeS(int dy){
		base = new Rectangle(base.x, base.y, base.width, base.height+dy);
	}
	public void resizeN(int dy){
		base = new Rectangle(base.x, base.y-dy, base.width, base.height+dy);
	}
	public void resizeW(int dx){
		base = new Rectangle(base.x-dx, base.y, base.width+dx, base.height);
	}
	public void resizeE(int dx){
		base = new Rectangle(base.x, base.y, base.width+dx, base.height);
	}
	public void resizeNE(int dx, int dy){
		resizeN(dy);
		resizeE(dx);
	}
	public void resizeNW(int dx, int dy){
		resizeN(dy);
		resizeW(dx);
	}
	public void resizeSE(int dx, int dy){
		resizeS(dy);
		resizeE(dx);
	}
	public void resizeSW(int dx, int dy){
		resizeS(dy);
		resizeW(dx);
	}
	
	public Element toElement(){
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
	
	public static SnippetShape fromElement(Element e){
		SnippetShape sh = null;
		if(e.getContentSize() > 0){
			Element r = e.getChild("base");
			Rectangle rectangle = new Rectangle(Integer.valueOf(r.getAttributeValue("x")),
					Integer.valueOf(r.getAttributeValue("y")),
					Integer.valueOf(r.getAttributeValue("width")),
					Integer.valueOf(r.getAttributeValue("height")));
			Element a = e.getChild("angle");
			float angle = Float.valueOf(a.getAttributeValue("phi"));
			sh = new SnippetShape(rectangle, angle);
		}
		else{
			Rectangle rectangle = new Rectangle(Integer.valueOf(e.getAttributeValue("x")),
					Integer.valueOf(e.getAttributeValue("y")),
					Integer.valueOf(e.getAttributeValue("width")),
					Integer.valueOf(e.getAttributeValue("height")));
			sh = new SnippetShape(rectangle);
		}
		return sh;
	}
	
	public SnippetShape clone(){
		SnippetShape cloned = new SnippetShape(this.base, this.angle);
		return cloned;
	}

}
