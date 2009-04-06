
package myctapp;

import java.awt.*;
import java.awt.event.*;
import java.awt.BasicStroke;
import javax.swing.*;
import javax.swing.JOptionPane;
import javax.swing.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;

/*@author     Damien Farrell
 *@created    February 2004
 *drawpanel class for drawing and displaying 2d geometrical objects - testing
*/

class DrawPanel extends JPanel {
   private int x, y, x1, y1, x2, y2;
   private int houns;
   public CTPhantom phantom;
   private PhantomElement currshape;
   private boolean elementadded = false;
   private Ellipse2D FOV;
   
   public void setupPanel(String path)
   {

       setPreferredSize(new Dimension(512, 512));
       setBackground(Color.white);
       setBorder(BorderFactory.createLineBorder(Color.black));
       // True if the user pressed, dragged or released the mouse 
       //outside of the rectangle; false otherwise.
       phantom = new CTPhantom();
       if (path != null){
           loadPhant(path);
       }
       houns = 0;
       addMouseListener(

         // anonymous inner class for mouse pressed and 
         // released event handling
         new MouseAdapter() {

            // handle mouse press event
            public void mousePressed( MouseEvent event )
            {
               x1 = event.getX();
               y1 = event.getY();
               repaint();
            }

            // handle mouse release event
            public void mouseReleased( MouseEvent event )
            {
               x2 = event.getX();
               y2 = event.getY();
               String s =  (String) JOptionPane.showInputDialog(null,
                    "Enter a greyscale value: (0-255)",
                    "Customized Dialog",
                    JOptionPane.PLAIN_MESSAGE);
               double d = Double.parseDouble(s);
	       //px and py are the x,y coords given to draw the element
	       //must compensate if the user drags from right to left or bottom to top
	       int px = x1;
	       int py = y1;
               int alpha = 0;
               if (x1 > x2) {
		 px = x2;
               }
               if (y1 > y2) {
		 py = y2;
	       }
               phantom.addElement("ellipse", (double)Math.abs( x1 - x2 ), 
                (double)Math.abs( y1 - y2 ),(double)d, px, py, alpha);                   
               
               elementadded = true;
              
            // houns = (int) phantom.elements(0).getdensity()*1000; 
               x1=x2=y1=y2=0;
               repaint();
            }
         }  // end anonymous inner class
       );	
 
      // set up mouse motion listener
      addMouseMotionListener(

         // anonymous inner class to handle mouse drag events
         new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent event )
            {
                x = event.getX();
                y = event.getY();
                phantom.pinfoArea.setPanelCoords(x,y);
                repaint();
            }
                
            // handle mouse drag event
            public void mouseDragged( MouseEvent event )
            {
               x2 = event.getX();
               y2 = event.getY();
               
               repaint();
            }
         }  // end anonymous inner class
      ); // end call to addMouseMotionListener

   }  // end constructor

 
   // paint an oval at the specified coordinates
   public void paintComponent( Graphics g )
   {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D)g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
      RenderingHints.VALUE_ANTIALIAS_ON);
      AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);  
      g2.setComposite(ac);
      FOV = new Ellipse2D.Float(0, 0, 512, 512);
      g2.setClip(FOV); g2.setColor(Color.black);
      g2.fill(FOV);

      if (elementadded == true){
          int size = phantom.elements.size();
          for (int i=0; i<size; i++){
              try {
                currshape = (PhantomElement) phantom.getCurrentElement(i);
                int d = (int) currshape.rho;
                g2.setColor (new Color(d, d, d));
                g2.fill(new Ellipse2D.Double(currshape.x,currshape.y,
                  currshape.A, currshape.B));
              }
              catch (ArrayIndexOutOfBoundsException e){
                  System.out.println("NO ELEMENTS TO DISPLAY");
              }
          }
      }            
      g2.setStroke(new BasicStroke(1));
      Color shade = new Color(55, 55, 235);
      g2.setColor(shade);
      g2.fill (new Ellipse2D.Double(Math.min( x1, x2 ), Math.min( y1, y2 ),
         Math.abs( x1 - x2 ), Math.abs( y1 - y2 )));
      
   }
   
   public DrawPanel() {
      setupPanel(null);
   }

   public DrawPanel (String path) {
       setupPanel(path);
   }

   public void savePhant(String path){
        this.phantom.savePhantom(path);
   }
  
   public void loadPhant(String path){
        this.phantom.loadPhantom(path);
        elementadded=true;
        repaint();
   }
   
   public void savePhantomAsImage(String path){
        try {
          BufferedOutputStream out = new BufferedOutputStream(new
          FileOutputStream(path));
          BufferedImage mBufferedImage = createComponentImage(this);
          ImageIO.write( mBufferedImage, "png", out ); 
        } catch (IOException ioe) {  ioe.printStackTrace();  }
   }
  
   public static BufferedImage createComponentImage(Component component) {
        BufferedImage image = (BufferedImage) component.createImage(
                        component.getWidth(), component.getHeight());
        Graphics graphics = image.getGraphics();
        if (graphics != null) {
            component.paintAll(graphics);
            component.update(graphics);
        }
        return image;
    }


}  // end class drawpanel
