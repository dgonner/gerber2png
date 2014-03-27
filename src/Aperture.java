import java.awt.*;

public class Aperture {

	private int ppi;
	private String type;
	private float[] modarray;
	
	public Aperture(int ppi, String type, float[] modarray) {
		this.ppi = ppi;
		this.type = type;
		this.modarray = modarray;
	}
	
	public void draw(Graphics2D g2d, int x, int y, int imgw, int imgh, int offsetx, int offsety, boolean inverted) {
		if (inverted) {
			g2d.setColor(Color.black);
		} else {
			g2d.setColor(Color.white);
		}
		
		if (this.type.equals("C")) { // draw circle
			//System.out.println("Drawing CIRCLE at x,y ["+x+","+y+"]");
			
			//g2d.setColor(Color.red);
			int diameter =  (int)Math.round((double)this.modarray[0]*(double)this.ppi);
			//diameter=20;
			//System.out.println("diameter: "+diameter);
			
			int xx = x - (int)Math.round(diameter/2.0);
			int yy = y - (int)Math.round(diameter/2.0);
				
			g2d.fillOval(offsetx+xx, offsety+yy, diameter, diameter);
			//g2d.fillOval(offsetx+x, offsety+y, 20, 20);
			
			
		}
		
		if (this.type.equals("R")) { // draw rectangle
			//System.out.println("Drawing RECTANGLE at x,y ["+x+","+y+"]");
			
			//g2d.setColor(Color.red);
			int width = (int)Math.round((double)this.modarray[0]*(double)this.ppi);
			int height = (int)Math.round((double)this.modarray[1]*(double)this.ppi);
			
			int xx = x - (int)Math.round(width/2.0);
			int yy = y - (int)Math.round(height/2.0);
						
			g2d.fillRect(offsetx+xx, offsety+yy, width, height);
		}

		if (this.type.equals("O")) { // draw rectangle
			//System.out.println("Drawing OVAL at x,y ["+x+","+y+"]");
			
			//g2d.setColor(Color.red);
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
				
				g2d.fillOval(offsetx+(x-half_diameter), offsety+(y-half_height), diameter, diameter);
				g2d.fillOval(offsetx+(x-half_diameter), offsety+(y+half_height-diameter), diameter, diameter);
				//g2d.fillRect(offsetx+(x-half_diameter), offsety+(y-half_diameter), diameter, diameter);
				g2d.fillOval(offsetx+(x-half_diameter), offsety+(y-half_diameter), diameter, diameter);
			} else {
				int diameter = (int)Math.round(h);
				int half_diameter = (int)Math.round(h/2.0);
				int width = (int)Math.round(w);
				int half_width = (int)Math.round(w/2.0);
				
				g2d.fillOval(offsetx+(x-half_width), offsety+(y-half_diameter), diameter, diameter);
				g2d.fillOval(offsetx+(x+half_width-diameter), offsety+(y-half_diameter), diameter, diameter);
				
//				g2d.fillRect(offsetx+(x-half_diameter+2), offsety+(y-half_diameter), diameter-4, diameter);
				g2d.fillOval(offsetx+(x-half_diameter), offsety+(y-half_diameter), diameter, diameter);
				
			}
			
			
			//int xx = x - (int)Math.round(width/2.0);
			//int yy = y - (int)Math.round(height/2.0);
						
			//g2d.fillOval(offsetx+xx, offsety+yy, width, height);
		}
		
		
	}
	
	
}
