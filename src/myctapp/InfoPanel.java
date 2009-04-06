package myctapp;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

   /**jpanel class to display image info such as pixel values and co-ords*/
  public class InfoPanel extends JPanel{
      
      int x, y, imx, imy, x1, y1, x2, y2, pixelval, numpixels;
      double houns;
      
      public InfoPanel(){

          setBorder(BorderFactory.createLineBorder (Color.blue, 1));
          setPreferredSize(new Dimension(100, 80));
          setFont(new Font("SansSerif", 0, 10));
          setBackground(Color.white);
          
      }
      
      public void updateInfo(int x, int y, int imx, int imy, int pixval, double actval){
          //co-ords in the panel
          this.x = x;
          this.y = y;
          //co-ordinates for pixels in the image itself
          this.imx = imx; 
          this.imy = imy;
          pixelval = pixval;
          houns = actval;
          repaint();
      }
      
       public void updateDragInfo(int x1, int y1, int x2, int y2){
          //co-ords in the panel
          this.x1 = x1;
          this.y1 = y1;
          this.x2 = x2;
          this.y2 = y2;  
          numpixels = Math.abs(x2-x1) * Math.abs(y2-y1);
          repaint();
      }   
      
      public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawString ("x="+imx+" y="+imy, 5, 20);
        g.setColor(Color.gray);
        g.drawString (x+", "+y, 5, 10);
        g.setColor(Color.black);
        g.drawString ("Grey Value: "+Integer.toString(pixelval), 5, 30);
        g.drawString ("HU: "+Double.toString(houns), 5, 40);
        g.drawString ("Area: "+Integer.toString(Math.abs(x2-x1))+"x"+
                        Integer.toString(Math.abs(y2-y1)),5, 55);
        g.drawString("Num Pixels: "+numpixels, 5, 65);
        
      }
  }
  