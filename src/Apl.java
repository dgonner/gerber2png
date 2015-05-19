import java.io.*;
import java.util.*;
import java.awt.geom.Point2D;

public class Apl {

	// GLOBAL OUTPUT SETTINGS
	private	String dir = "/home/daveg/Electronics/relay-clock/plots/";
	private	String project = "relay-clock";

	private boolean process_F_Cu = false;
	private boolean process_B_Cu = true;
	private boolean process_NPTH = false;

	private int ppi = 1000; 		// pixels per inch
	private double border_mm = 1; 	// mm border around entire image
	
	// ==============================
	private double scale = 1;
	private double step = 0.5; // subpixel stepping
	private int border = (int)Math.round(border_mm/25.4*ppi); // pixel border around entire image
	private int offsetx = border;
	private int offsety = border;
	private boolean single_quadrant = false;
	private MyGraphics myg = new MyGraphics();
	private int linenumber = 0;
	private HashMap<Integer,Aperture> apertures = new HashMap<Integer,Aperture>();
	private Aperture aperture = null;
	private HashMap<Integer,Aperture> tools = new HashMap<Integer,Aperture>();
	private Aperture tool = null;
	private Point2D.Double lastPoint = new Point2D.Double(0,0);
	
	public void addAperture(String line) {
		// strip the %AD
		String s = line.substring(3); 
		// get the aperture number
		int n = Integer.parseInt(s.substring(1, 3));
		System.out.println("aperture number: "+n);
		// get the type
		String type = s.substring(3, 4);
		System.out.println("aperture type: #"+type+"#");
				
		String modifiers = s.substring(s.indexOf(",")+1, s.indexOf("*"));
		System.out.println("modifiers: #"+modifiers+"#");
		
		// extract modifiers
		float[] modarray = new float[4];
		int modindex = 0;
		
		while (modifiers.length() > 0) {
			int xpos = modifiers.indexOf("X");
			if (xpos != -1) {
				modarray[modindex] = Float.valueOf(modifiers.substring(0, xpos));
				modifiers = modifiers.substring(xpos+1);
			} else {
				modarray[modindex] = Float.valueOf(modifiers);
				modifiers = "";
			}
			modindex++;
		}
		
		System.out.println("modifier 0:"+modarray[0]);
		System.out.println("modifier 1:"+modarray[1]);
		System.out.println("modifier 2:"+modarray[2]);
		System.out.println("modifier 3:"+modarray[3]);
		
		Aperture a = new Aperture(this.ppi, type, modarray);
		this.apertures.put(new Integer(n), a);
	}
	
	
	public void addTool(String line) {
		// strip the T
		String s = line.substring(1); 
		System.out.println("trimmed: "+s);
		
		// get the tool number
		int cpos = s.indexOf("C");
		int n = Integer.parseInt(s.substring(0, cpos));
		System.out.println("tool number: "+n);

		// extract modifiers
		float[] modarray = new float[4];
		int modindex = 0;
		String modifiers = s.substring(cpos+1);
		
		while (modifiers.length() > 0) {
			int xpos = modifiers.indexOf("X");
			if (xpos != -1) {
				modarray[modindex] = Float.valueOf(modifiers.substring(0, xpos));
				modifiers = modifiers.substring(xpos+1);
			} else {
				modarray[modindex] = Float.valueOf(modifiers);
				modifiers = "";
			}
			modindex++;
		}
		
		System.out.println("modifier 0:"+modarray[0]);
		System.out.println("modifier 1:"+modarray[1]);
		System.out.println("modifier 2:"+modarray[2]);
		System.out.println("modifier 3:"+modarray[3]);
		
		Aperture a = new Aperture(this.ppi, "C", modarray);
		this.tools.put(new Integer(n), a);
	}
			
	public void selectAperture(String line) {
		// strip the G54
		String s = line.substring(3); 
		// get the aperture number
		int n = Integer.parseInt(s.substring(1, 3));
		System.out.println("selecting aperture number: "+n);
		this.aperture = this.apertures.get(new Integer(n));
	}
	
	
	public void selectTool(String line) {
		// strip the T
		String s = line.substring(1); 
		// get the tool number
		int n = Integer.parseInt(s);
		System.out.println("selecting tool number: "+n);
		this.tool = this.tools.get(new Integer(n));
	}
	
	public void draw(String line) {
		int xpos = line.indexOf("X");
		int ypos = line.indexOf("Y");
		int dpos = line.indexOf("D");
		String xstr = line.substring(xpos+1, ypos);
		String ystr = line.substring(ypos+1, dpos);
		
		// add leading zeroes
		while (xstr.length() < 7) {
			xstr = "0"+xstr;
		}
		while (ystr.length() < 7) {
			ystr = "0"+ystr;
		}

		// add decimal point
		xstr = xstr.substring(0, 3) + "." + xstr.substring(3);
		ystr = ystr.substring(0, 3) + "." + ystr.substring(3);
		
		int x = (int)Math.round(Double.valueOf(xstr)*(double)this.ppi);
		int y = (int)Math.round(Double.valueOf(ystr)*(double)this.ppi);
				
		if (line.endsWith("D01*")) { // move with shutter OPEN
			// make a path from lastPoint to x,y
			double distance = Functions.getDistance(lastPoint, x, y);
			while(distance > this.step) {
				Point2D.Double next = Functions.calcStep(lastPoint, x, y, this.step);
								
				int xx = (int)Math.round(next.x);
				int yy = (int)Math.round(next.y);
				this.aperture.draw(this.myg, xx, yy, this.offsetx, this.offsety);
				this.lastPoint.x = next.x;
				this.lastPoint.y = next.y;
								
				distance = Functions.getDistance(lastPoint, x, y);
				//System.out.println("distance: "+distance);
			}
		}
		if (line.endsWith("D02*")) { // move with shutter CLOSED
			this.lastPoint.x = x;
			this.lastPoint.y = y;
		}
		if (line.endsWith("D03*")) { // flash
			this.aperture.draw(this.myg, x, y, this.offsetx, this.offsety);
			this.lastPoint.x = x;
			this.lastPoint.y = y;
		}
	}

	private int parseValue(String s) {
		boolean negative = false;
		// strip minus signs
		if (s.startsWith("-")) { 
			s = s.substring(1);
			negative = true;
		}
		s = addLeadingZeroes(s);
		s = addDecimalPoint(s);
		int i = (int)Math.round(Double.valueOf(s)*(double)this.ppi);
		if (negative) {
			i = i*-1;
		}
		return i;
	}
	
	private String addDecimalPoint(String s) {
		return s.substring(0, 3) + "." + s.substring(3);
	}
	
	private String addLeadingZeroes(String s) {
		while (s.length() < 7) {
			s = "0"+s;
		}
		return s;
	}
	
	public void drawArc(String line) {
		int xpos = line.indexOf("X");
		int ypos = line.indexOf("Y");
		int ipos = line.indexOf("I");
		int jpos = line.indexOf("J");
		int dpos = line.indexOf("D");

		String xstr = line.substring(xpos+1, ypos);
		String ystr = line.substring(ypos+1, ipos);
		String istr = line.substring(ipos+1, jpos);
		String jstr = line.substring(jpos+1, dpos);
		
		int x = parseValue(xstr);
		int y = parseValue(ystr);
		int i = parseValue(istr);
		int j = parseValue(jstr);
		
		System.out.println("Arc: "+x+", "+y+", "+i+", "+j);

		int centerx = (int)this.lastPoint.x + i;
		int centery = (int)this.lastPoint.y + j;
		
		double radius = Functions.getDistance(lastPoint, centerx, centery);
		double arcResolution = 0.00175;
		
		System.out.println("Circle at: ["+centerx+", "+centery+"] Radius:"+radius);
		
		// The parametric equation for a circle is
		// 	x = cx + r * cos(a) 
		// y = cy + r * sin(a) 
		
		// Where r is the radius, cx,cy the origin, 
		// and a the angle from 0..2PI radians or 0..360 degrees.
		
//		int xx = (int)Math.round(centerx + radius * Math.cos(arcResolution));
//		int yy = (int)Math.round(centery + radius * Math.sin(arcResolution));
		
		if (line.endsWith("D01*")) { // move with shutter OPEN
			// make a path from lastPoint to x,y
			double angle = 2 * Math.PI;
			while (angle > 0) {
				int xx = (int)Math.round(centerx + radius * Math.cos(angle));
				int yy = (int)Math.round(centery + radius * Math.sin(angle));

				this.aperture.draw(this.myg, xx, yy, this.offsetx, this.offsety);
				this.lastPoint.x = xx;
				this.lastPoint.y = yy;
			
				angle = angle - arcResolution;
			}
		
			
//			double distance = Functions.getDistance(lastPoint, x, y);
//			while(distance > this.step) {
//				Point2D.Double next = Functions.calcStep(lastPoint, x, y, this.step);
//								
//				int xx = (int)Math.round(next.x);
//				int yy = (int)Math.round(next.y);
//				this.aperture.draw(this.g2d, xx, yy, this.imgw, this.imgh, this.offsetx, this.offsety, this.negative);
//				this.lastPoint.x = next.x;
//				this.lastPoint.y = next.y;
//								
//				distance = Functions.getDistance(lastPoint, x, y);
//				//System.out.println("distance: "+distance);
//			}
		}
	}

	
	
	public void drill(String line) {
		int xpos = line.indexOf("X");
		int ypos = line.indexOf("Y");
		String xstr = line.substring(xpos+1, ypos);
		String ystr = line.substring(ypos+1);
		
		if (ystr.startsWith("-")) { 
			ystr = ystr.substring(1);
		}
		
		// add leading zeroes
		while (xstr.length() < 6) {
			xstr = "0"+xstr;
		}
		while (ystr.length() < 6) {
			ystr = "0"+ystr;
		}

		// add decimal point
		xstr = xstr.substring(0, 2) + "." + xstr.substring(2);
		ystr = ystr.substring(0, 2) + "." + ystr.substring(2);
		//System.out.println("xstr:"+xstr);
		//System.out.println("ystr:"+xstr);
				
		int x = (int)Math.round(Double.valueOf(xstr)*(double)this.ppi);
		int y = (int)Math.round(Double.valueOf(ystr)*(double)this.ppi);
				
		y = Math.abs(y); // invert
		
		this.tool.draw(this.myg, x, y, this.offsetx, this.offsety, true);
		this.lastPoint.x = x;
		this.lastPoint.y = y;
	}
	
	public boolean processGerber(String line) {
		this.linenumber++;
		
		line = line.trim().toUpperCase();
		if (line.startsWith("%FS")) {
			System.out.println("got format definition! line "+this.linenumber);
			if (!line.equals("%FSLAX34Y34*%")) {
				System.out.println("wrong format definition! STOPPING...");
				return true;
			}
		}
		
		if (line.startsWith("%AD")) {
			System.out.println("got aperture definition! line "+this.linenumber);
			addAperture(line);
		}

		if (line.startsWith("%MOIN*%")) {
			System.out.println("Dimensions are expressed in inches");
			this.scale = 25.4;
		}
		if (line.startsWith("%MOMM*%")) {
			System.out.println("Dimensions are expressed in millimeters");
			this.scale = 1;
		}

		if (line.startsWith("G04")) {
			System.out.println("ignoring comment on line "+this.linenumber);
		}
		
		if (line.startsWith("G70")) {
			System.out.println("Set unit to INCH");
		}
		if (line.startsWith("G71")) {
			System.out.println("Set unit to MM");
		}
		
		if (line.startsWith("G74")) {
			System.out.println("Selecting Single quadrant mode");
			single_quadrant = true;
		}
		if (line.startsWith("G75")) {
			System.out.println("Selecting Multi quadrant mode");
			single_quadrant = false;
		}
		
		if (line.startsWith("G90")) {
			System.out.println("Set Coordinate format to Absolute notation");
		}
		if (line.startsWith("G91")) {
			System.out.println("Set the Coordinate format to Incremental notation");
		}
				
		if (line.startsWith("G54")) {
			System.out.println("Select aperture");
			selectAperture(line);
		}

		if (line.startsWith("M02")) {
			System.out.println("STOP");
			return true;
		}
		
		if (line.startsWith("G02")) {
			drawArc(line);
		}
		if (line.startsWith("G03")) {
			drawArc(line);
		}
		
		
		if (line.startsWith("X")) {
			draw(line);
		}

		return false;
	}

	
	
	public boolean processDrill(String line) {
		
		this.linenumber++;
		
		line = line.trim().toUpperCase();

		if (line.startsWith("T")) {
			if (line.indexOf("C") != -1) {
				System.out.println("got tool definition! line "+this.linenumber);
				addTool(line);
			} else {
				System.out.println("got tool change! line "+this.linenumber);
				if (!line.equals("T0")) {
					selectTool(line);
				}
			}
		}
		
		if (line.startsWith("M30")) {
			System.out.println("STOP");
			return true;
		}
		
		if (line.startsWith("X")) {
			drill(line);
		}

		return false;
	}
	
	private void processGerberFile(String filename) {
		File file = new File(filename);
		
		FileReader fr = null;
		try {
			fr = new FileReader(file);
		} catch (Exception e) {
			System.out.println("Error (1): "+e);
		}
		
		BufferedReader br = new BufferedReader(fr);
		
		String line;
		try {
			boolean stop = false;
			this.linenumber = 0;
			while ((line = br.readLine()) != null && !stop) {
		   		stop = processGerber(line);
			}
		} catch (Exception e) {
			System.out.println("Error (2): "+e);
			e.printStackTrace();
		}
				
		try {
			br.close();
		} catch (Exception e) {
			System.out.println("Error (3): "+e);
		}	
	}

	private void processDrillFile(String filename) {
		File file = new File(filename);
		
		FileReader fr = null;
		try {
			fr = new FileReader(file);
		} catch (Exception e) {
			System.out.println("Error (4): "+e);
		}
		
		BufferedReader br = new BufferedReader(fr);
		
		String line;
		try {
			boolean stop = false;
			this.linenumber = 0;
			while ((line = br.readLine()) != null && !stop) {
		   		stop = processDrill(line);
			}
		} catch (Exception e) {
			//System.out.println("Error (6): "+);
			e.printStackTrace();
		}
				
		try {
			br.close();
		} catch (Exception e) {
			System.out.println("Error (7): "+e);
		}	
	}


	public Apl() {
		String prefix = dir+project;

		// process the largest image first, usually the outline. 
		//then use the same image dimensions to make the traces image

		processGerberFile(prefix+"-Edge_Cuts.gbr");
		if (process_NPTH) processDrillFile(prefix+"-NPTH.drl");
		processDrillFile(prefix+".drl");
		myg.drawAndWritePNG(prefix+"-mill-outline.png", this.ppi, border, true); 

		if (process_B_Cu) processGerberFile(prefix+"-B_Cu.gbl");
		if (process_F_Cu) processGerberFile(prefix+"-F_Cu.gtl");
		if (process_NPTH) processDrillFile(prefix+"-NPTH.drl");
		processDrillFile(prefix+".drl");
		myg.drawAndWritePNG(prefix+"-mill-traces.png", this.ppi, border, false); 

	}
	
	public static void main(String[] args) {
		Apl apl = new Apl();
	}
}
