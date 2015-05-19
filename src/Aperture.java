//import java.awt.*;

public class Aperture {

	private int ppi;
	private String type;
	private float[] modarray;
	
	public Aperture(int ppi, String type, float[] modarray) {
		this.ppi = ppi;
		this.type = type;
		this.modarray = modarray;
	}


	public void draw(MyGraphics myg, int x, int y, int offsetx, int offsety) {
		this.draw(myg, x, y, offsetx, offsety, false);
	}

	public void draw(MyGraphics myg, int x, int y, int offsetx, int offsety, boolean inverted) {

		if (this.type.equals("C")) { // draw circle
			//System.out.println("Drawing CIRCLE at x,y ["+x+","+y+"]");
			
			int diameter =  (int)Math.round((double)this.modarray[0]*(double)this.ppi);
			//System.out.println("diameter: "+diameter);
			
			int xx = x - (int)Math.round(diameter/2.0);
			int yy = y - (int)Math.round(diameter/2.0);
				
			myg.circle(offsetx+xx, offsety+yy, diameter, inverted);
		}
		
		if (this.type.equals("R")) { // draw rectangle
			//System.out.println("Drawing RECTANGLE at x,y ["+x+","+y+"]");
			
			int width = (int)Math.round((double)this.modarray[0]*(double)this.ppi);
			int height = (int)Math.round((double)this.modarray[1]*(double)this.ppi);
			
			int xx = x - (int)Math.round(width/2.0);
			int yy = y - (int)Math.round(height/2.0);
						
			myg.rect(offsetx+xx, offsety+yy, width, height);
		}

		if (this.type.equals("O")) { // draw oval
			//System.out.println("Drawing OVAL at x,y ["+x+","+y+"]");
			
//			double min = Math.min(this.modarray[0], this.modarray[1]);
//			double max = Math.max(this.modarray[0], this.modarray[1]);
//			
//			int diameter = (int)Math.round((double)min*(double)this.ppi);
//			int width = (int)Math.round((double)max*(double)this.ppi);
			
			double w = (double)this.modarray[0]*(double)this.ppi;
			double h = (double)this.modarray[1]*(double)this.ppi;
			
			boolean upright = true;
			if (w > h) upright = false;
			
			if (upright) {
				int diameter = (int)Math.round(w);
				int half_diameter = (int)Math.round(w/2.0);
				int height = (int)Math.round(h);
				int half_height = (int)Math.round(h/2.0);
				
				myg.circle(offsetx+(x-half_diameter), offsety+(y-half_height), diameter);
				myg.circle(offsetx+(x-half_diameter), offsety+(y+half_height-diameter), diameter);
				myg.circle(offsetx+(x-half_diameter), offsety+(y-half_diameter), diameter);
			} else {
				int diameter = (int)Math.round(h);
				int half_diameter = (int)Math.round(h/2.0);
				int width = (int)Math.round(w);
				int half_width = (int)Math.round(w/2.0);
			
				myg.circle(offsetx+(x-half_width), offsety+(y-half_diameter), diameter);
				myg.circle(offsetx+(x+half_width-diameter), offsety+(y-half_diameter), diameter);
				myg.circle(offsetx+(x-half_diameter), offsety+(y-half_diameter), diameter);
			}
			
			
			//int xx = x - (int)Math.round(width/2.0);
			//int yy = y - (int)Math.round(height/2.0);
						
			//g2d.fillOval(offsetx+xx, offsety+yy, width, height);
		}
		
		
	}
	
	
}
