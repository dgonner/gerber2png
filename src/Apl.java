import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.*;
import java.awt.geom.Point2D;

public class Apl {

	// GLOBAL OUTPUT SETTINGS
	private int ppi = 1000;
	private double board_width = 13; // mm
	private double board_height = 13; // mm
	private double border_mm = 1; // mm border around entire image
	
	// ==============================
	private double scale = 1;
	private double step = 0.5; // subpixel stepping

	private int border = (int)Math.round(border_mm/25.4*ppi); // pixel border around entire image
	private int imgw = 2 * border + (int)Math.round(board_width/25.4*ppi);
	private int imgh = 2 * border + (int)Math.round(board_height/25.4*ppi);
		
	//private int imgw = 2000;
	//private int imgh = 1200;
	
	private int offsetx = border;
	private int offsety = border;

	private boolean negative = false;
	
	//private BufferedImage image = new BufferedImage(imgw, imgh, BufferedImage.TYPE_INT_RGB);
	//private Graphics2D g2d = image.createGraphics();
	private BufferedImage image;
	private Graphics2D g2d;		
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
		
		// strip minus signs
		//if (xstr.startsWith("-")) { 
		//	xstr = xstr.substring(1);
		//}
		//if (ystr.startsWith("-")) { 
		//	ystr = ystr.substring(1);
		//}
				
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
		
		//x = Math.abs(x); // invert
		//y = Math.abs(y); // invert
				
		if (line.endsWith("D01*")) { // move with shutter OPEN
			// make a path from lastPoint to x,y
			double distance = Functions.getDistance(lastPoint, x, y);
			while(distance > this.step) {
				Point2D.Double next = Functions.calcStep(lastPoint, x, y, this.step);
								
				int xx = (int)Math.round(next.x);
				int yy = (int)Math.round(next.y);
				this.aperture.draw(this.g2d, xx, yy, this.imgw, this.imgh, this.offsetx, this.offsety, this.negative);
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
			this.aperture.draw(this.g2d, x, y, this.imgw, this.imgh, this.offsetx, this.offsety, this.negative);
			this.lastPoint.x = x;
			this.lastPoint.y = y;
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
		
		int x = (int)Math.round(Double.valueOf(xstr)*(double)this.ppi);
		int y = (int)Math.round(Double.valueOf(ystr)*(double)this.ppi);
				
		y = Math.abs(y); // invert
		
		this.tool.draw(this.g2d, x, y, this.imgw, this.imgh, this.offsetx, this.offsety, true);
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

	private void newImageFile() {
		this.image = new BufferedImage(imgw, imgh, BufferedImage.TYPE_INT_RGB);
		this.g2d = image.createGraphics();	
	}

	 private void saveGridImage(BufferedImage gridImage, File output) throws IOException {
	    output.delete();

	    final String formatName = "png";

	    for (Iterator<ImageWriter> iw = ImageIO.getImageWritersByFormatName(formatName); iw.hasNext();) {
	       ImageWriter writer = iw.next();
	       ImageWriteParam writeParam = writer.getDefaultWriteParam();
	       ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
	       IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
	       if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported()) {
	          continue;
	       }

	       setDPI(metadata, this.ppi);

	       final ImageOutputStream stream = ImageIO.createImageOutputStream(output);
	       try {
	          writer.setOutput(stream);
	          writer.write(metadata, new IIOImage(gridImage, null, metadata), writeParam);
	       } finally {
	          stream.close();
	       }
	       break;
	    }
	 }

	 private void setDPI(IIOMetadata metadata, int DPI) throws IIOInvalidTreeException {
	    // for PMG, it's dots per millimeter
		double INCH_2_MM = 25.4; 
	    double dotsPerMilli = (double)DPI / INCH_2_MM;

	    IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");
	    horiz.setAttribute("value", Double.toString(dotsPerMilli));

	    IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
	    vert.setAttribute("value", Double.toString(dotsPerMilli));

	    IIOMetadataNode dim = new IIOMetadataNode("Dimension");
	    dim.appendChild(horiz);
	    dim.appendChild(vert);

	    IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
	    root.appendChild(dim);

	    metadata.mergeTree("javax_imageio_1.0", root);
	 }
	
	
	private void saveImageFile(String filename) {
		// save the buffered image
		try {
		    File outputfile = new File(filename);
		    saveGridImage(this.image, outputfile);
		    //ImageIO.write(this.image, "png", outputfile);
		} catch (IOException e) {
			System.out.println("Error (8): "+e);
		}
		System.out.println("Output image saved...");
	}
	
	public Apl() {
//		String dir = "c:/temp/keyboard controller/gerbers/";
//		String dir = "c:/temp/arduino shield/plots/";
//		String dir = "d:/Development-Dave/chocopasta2-prototype/pcb/1wire-breakout/plots/";
//		String dir = "d:/Home-Dave/twinqle working/Fader Controller/plots/";
		String dir = "d:/Development-Dave/GlucoseMeter/electronics/first_pcb/plots/";
		String project = "first_pcb";
		
		newImageFile();
		this.negative = false;
		//processGerberFile(dir+project+"-B_Cu.gbl");
		processGerberFile(dir+project+"-F_Cu.gtl");
		processDrillFile(dir+project+"-NPTH.drl");
		saveImageFile(dir+project+"-mill-traces.png");
		
		newImageFile();
		this.negative = true;
		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, this.imgw, this.imgh);
		processGerberFile(dir+project+"-Edge_Cuts.gbr");
		processDrillFile(dir+project+"-NPTH.drl");
		Functions.floodFill(image, new Point(0, 0), Color.black);
		saveImageFile(dir+project+"-mill-outline.png");
	
	}
	
	public static void main(String[] args) {
		Apl apl = new Apl();
	}
}
