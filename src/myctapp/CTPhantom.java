package myctapp;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.io.*;


/*@author     Damien Farrell
 *created    February 2004
 *A class to store the geometrical elements that make up a 2-D this that can be
 *drawn by a user interactively and then saved to a file. This phantom can then be
 *used to create projection data for reconstruction
 *a vector is used to store the elements of type PhantomElement
 */

class CTPhantom {
    public Vector elements;
    private PhantomElement newelement, currentelement;
    public PhantomInfoArea pinfoArea;
    
    public CTPhantom(){
        elements = new Vector(7);
        pinfoArea = new PhantomInfoArea();
        pinfoArea.updateInfo();
    }
    
    public void addElement(String type, double w, double h,  double d, double x, double y, int alpha){
        newelement = new PhantomElement(type, w, h, d, x, y, alpha);
        elements.add(newelement);
        pinfoArea.updateInfo();        
    }
    
    public PhantomElement getCurrentElement(int index){
        try{
          currentelement = (PhantomElement) elements.get(index);
        }
        catch (NoSuchElementException exception) {
            System.out.println("no elements");
        }
        return currentelement;
    }      
    
    public void updateCurrentElement(int A, int B, int index){
        
        currentelement = (PhantomElement) elements.remove(index);
        currentelement.updateSize(A, B);
        elements.add(currentelement);
        pinfoArea.updateInfo();
        
    }       

    
    class PhantomInfoArea extends JPanel  {
       
       int xcoords, ycoords;
        
       public PhantomInfoArea() {
           setPreferredSize(new Dimension(100, 60));
           setBackground(Color.white);
           setBorder(BorderFactory.createLineBorder(Color.black));     

       }  // end constructor

       public void updateInfo(){
            repaint();
       }
       
       public void setPanelCoords(int x, int y){
           xcoords = x;
           ycoords = y;     
           repaint();
       }
       
       // paint an oval at the specified coordinates
       public void paintComponent( Graphics g )
       {
          super.paintComponent( g );
          g.setFont(new Font("SansSerif", Font.PLAIN, 9));
          g.drawString("Info Window" , 5, 10);
          g.drawString("PHANTOM DATA", 5, 20);
          g.drawString("No. elements:", 5, 30);
          g.drawString(""+elements.size(), 70, 30);     
          g.drawString("Coords: "+xcoords+","+ycoords, 180,10); 
          
       }
   }
    
    public void savePhantom(String filename){
        int maxelements = this.elements.size();
        PhantomElement currelement;
        if (filename != null) { 
          try {
          
           PrintWriter writer = new PrintWriter(new FileWriter(filename));
            //writer.println("PHANTOM ELEMENTS");
            for ( int i = 0; i < maxelements; i++ ) {
                currelement = getCurrentElement(i);
                writer.println(currelement.type);
                writer.println(currelement.A);
                writer.println(currelement.B);
                writer.println(currelement.x);
                writer.println(currelement.y);
                writer.println(currelement.rho);
                writer.println(currelement.alpha);
            }
            System.out.println("Saved "+maxelements+" elements");
            writer.close();
          }
          catch (IOException ioe) {
            System.out.println("I/O Exception in file writing: "+ioe);
            ioe.printStackTrace(); }
        }
    }
    
    public void loadPhantom(String filename){
       File file;
       int count=0;int noelems=0;int length=0; 
       try {
            
            file = new File(filename);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            length = (int) file.length();
            reader.mark(length + 1);
            while (reader.readLine() != null){
                noelems++;
            }
            noelems/=7;
            System.out.println(noelems);
            reader.reset();
            while (count < noelems){
                String type = reader.readLine();
                int w = Integer.valueOf(reader.readLine()).intValue(); 
                int h = Integer.valueOf(reader.readLine()).intValue();
                int x = Integer.valueOf(reader.readLine()).intValue();
                int y = Integer.valueOf(reader.readLine()).intValue();
                double d = Double.valueOf(reader.readLine()).doubleValue();
                int a = Integer.valueOf(reader.readLine()).intValue();
                count++;
                addElement(type, w, h, d, x, y, a);     
            }
            System.out.println("Loaded "+count+" elements");
            reader.close();        
       } catch (IOException ioe) {
          System.out.println("I/O Exception in file writing: "+ioe); 
       }
   }   
    
     /**
    * creates data for phantoms including the well known Shepp-Logan phantom
    * @return
    */
   public void CreatePhantomData(String phantomname){
       
       //CTPhantom phantom = new CTPhantom();
       if (phantomname == "sheppl"){
           this.addElement("ellipse", .46, .345, 2, 0, 0, 90);
           this.addElement("ellipse", .436, .3310, -1.2, -.0092, 0, 90);
           this.addElement("ellipse", .155, .055, -.8, .03, -.105, 72);
           this.addElement("ellipse", .205, .08, -.8, .03, .105, 108);
           this.addElement("ellipse", .125, .105, .4, .175, 0, 90);
           this.addElement("ellipse", .023, .023, .4, .05, 0, 90);
           this.addElement("ellipse", .023, .023, .4, -.05, 0, 90);
           this.addElement("ellipse", .0115, .023, .4, -.3025, .04, 90);
           this.addElement("ellipse", .0115, .0115, .4, -.3025, 0,  90);
           this.addElement("ellipse", .023, .0115, .4, -.3025, -.03, 90);
       }
       else if (phantomname == "test1"){
           //main ellipses
           this.addElement("ellipse", .42, .42, 2, 0, 0, 90);
           this.addElement("ellipse", .4, .4, -.9, -.0092, 0, 90);
       } 
       else if (phantomname == "test2"){ //for roi measurements..
           //main ellipses
           this.addElement("ellipse", .45, .45, 2, 0, 0, 90);
           this.addElement("ellipse", .44, .44, -1.2, 0, 0, 90);
           //circles arranged around edge at angular interval theta
           double x, y, r, theta;
           theta = 90;
           r = 0.36; 
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);           
           this.addElement("ellipse", .03, .03, .2, x, y, 90);
           r= 0.28;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);           
           this.addElement("ellipse", .03, .03, .2, x, y, 90);
           r= 0.2;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);           
           this.addElement("ellipse", .03, .03, .2, x, y, 90);
           r= 0.12;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);           
           this.addElement("ellipse", .03, .03, .2, x, y, 90);
           
           theta = 130;
           r = 0.36; 
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);           
           this.addElement("ellipse", .03, .03, .15, x, y, 90);
           r= 0.28;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);           
           this.addElement("ellipse", .03, .03, .15, x, y, 90);
           r= 0.2;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);           
           this.addElement("ellipse", .03, .03, .15, x, y, 90);
           r= 0.12;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);           
           this.addElement("ellipse", .03, .03, .15, x, y, 90);
           theta = 170;
           r = 0.36; 
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);           
           this.addElement("ellipse", .03, .03, .1, x, y, 90);
           r= 0.28;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);           
           this.addElement("ellipse", .03, .03, .1, x, y, 90);
           r= 0.2;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);           
           this.addElement("ellipse", .03, .03, .1, x, y, 90);
           r= 0.12;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);           
           this.addElement("ellipse", .03, .03, .1, x, y, 90);      
           theta = 210;
           r = 0.36; 
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);           
           this.addElement("ellipse", .03, .03, .07, x, y, 90);
           r= 0.28;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);           
           this.addElement("ellipse", .03, .03, .07, x, y, 90);
           r= 0.2;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);           
           this.addElement("ellipse", .03, .03, .07, x, y, 90);
           r= 0.12;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);           
           this.addElement("ellipse", .03, .03, .07, x, y, 90);
           theta = 250;
           r = 0.36; 
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);           
           this.addElement("ellipse", .03, .03, .05, x, y, 90);
           r= 0.28;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);           
           this.addElement("ellipse", .03, .03, .05, x, y, 90);
           r= 0.2;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);           
           this.addElement("ellipse", .03, .03, .05, x, y, 90);
           r= 0.12;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);           
           this.addElement("ellipse", .03, .03, .05, x, y, 90);  
           theta = 300;
           r = 0.36; 
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);           
           this.addElement("ellipse", .03, .03, .035, x, y, 90);
           r= 0.28;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);           
           this.addElement("ellipse", .03, .03, .035, x, y, 90);
           r= 0.2;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);           
           this.addElement("ellipse", .03, .03, .035, x, y, 90);
           r= 0.12;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);           
           this.addElement("ellipse", .03, .03, .035, x, y, 90);
           theta = 350;
           r = 0.36; 
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);           
           this.addElement("ellipse", .03, .03, .022, x, y, 90);
           r= 0.28;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);           
           this.addElement("ellipse", .03, .03, .022, x, y, 90);
           r= 0.2;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);           
           this.addElement("ellipse", .03, .03, .022, x, y, 90);
           r= 0.12;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);           
           this.addElement("ellipse", .03, .03, .022, x, y, 90);
           theta = 30;
           r = 0.36; 
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);           
           this.addElement("ellipse", .03, .03, .015, x, y, 90);
           r= 0.28;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);           
           this.addElement("ellipse", .03, .03, .015, x, y, 90);
           r= 0.2;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);           
           this.addElement("ellipse", .03, .03, .015, x, y, 90);
           r= 0.12;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);           
           this.addElement("ellipse", .03, .03, .015, x, y, 90);
           
   }
       
       else if (phantomname == "test3"){ //lcd 'CATPHAN' style this
           //main ellipses
           this.addElement("ellipse", .42, .42, 2, 0, 0, 90);
           this.addElement("ellipse", .4, .4, -.9, -.0092, 0, 90);
           //circles arranged around edge at angular interval theta
           double x, y, r, theta;
  
          //inner discs
           r= 0.18;
           theta = 111;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);           
           this.addElement("ellipse", .008, .008, .1, x, y, 90);
           theta = 119;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);          
           this.addElement("ellipse", .01, .01, .1, x, y, 90);
           theta = 130;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);   
           this.addElement("ellipse", .015, .015, .1, x, y, 90);
           theta = 145;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);            
           this.addElement("ellipse", .02, .02, .1, x, y, 90);
           theta = 165;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);          
           this.addElement("ellipse", .03, .03, .1, x, y, 90);
           theta = 190;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);          
           this.addElement("ellipse", .035, .035, .1, x, y, 90);
         
           theta = 230;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);           
           this.addElement("ellipse", .008, .008, .07, x, y, 90);
           theta = 238;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);          
           this.addElement("ellipse", .01, .01, .07, x, y, 90);
           theta = 249;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);   
           this.addElement("ellipse", .015, .015, .07, x, y, 90);
           theta = 264;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);            
           this.addElement("ellipse", .02, .02, .07, x, y, 90);
           theta = 284;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);          
           this.addElement("ellipse", .03, .03, .07, x, y, 90);
           theta = 309;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);          
           this.addElement("ellipse", .035, .035, .07, x, y, 90);
           
           theta = 350;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);           
           this.addElement("ellipse", .008, .008, .05, x, y, 90);
           theta = 358;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);          
           this.addElement("ellipse", .01, .01, .05, x, y, 90);
           theta = 9;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);   
           this.addElement("ellipse", .015, .015, .05, x, y, 90);
           theta = 26;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);            
           this.addElement("ellipse", .02, .02, .05, x, y, 90);
           theta = 46;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);          
           this.addElement("ellipse", .03, .03, .05, x, y, 90);
           theta = 71;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);          
           this.addElement("ellipse", .035, .035, .05, x, y, 90);         

        }
       
       else if (phantomname == "test4"){ //another lcd 'CATPHAN' style
           //main ellipses
           this.addElement("ellipse", .42, .42, 2, 0, 0, 90);
           this.addElement("ellipse", .4, .4, -.9, -.0092, 0, 90);
           //circles arranged around edge at angular interval theta
           double x, y, r, theta;

          //nine inner discs
           r= 0.14;
           theta = 88;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);           
           this.addElement("ellipse", .006, .006, .005, x, y, 90);           
           theta = 98;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);           
           this.addElement("ellipse", .008, .008, .005, x, y, 90);
           theta = 110;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);          
           this.addElement("ellipse", .01, .01, .005, x, y, 90);
           theta = 126;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);   
           this.addElement("ellipse", .015, .015, .005, x, y, 90);
           theta = 148;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);            
           this.addElement("ellipse", .02, .02, .005, x, y, 90);
           theta = 175;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);          
           this.addElement("ellipse", .03, .03, .005, x, y, 90);
           theta = 210;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);          
           this.addElement("ellipse", .035, .035, .005, x, y, 90);
           theta = 250;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);          
           this.addElement("ellipse", .04, .04, .005, x, y, 90);           
           theta = 300;
           x = r*Math.cos(theta*Math.PI/180);  y = r*Math.sin(theta*Math.PI/180);          
           this.addElement("ellipse", .05, .05, .005, x, y, 90);              
           
       }
       
       else if (phantomname == "delta"){
           //this.addElement("ellipse", .00005, .00005, 2, 0, 0, 0);
           this.addElement("ellipse", .2, .2, 2, 0.3, 0, 0);
       }
       else if (phantomname == "bars"){
           //this.addElement("gaussian", .1, .1, 1, .1, 0, 0);
           this.addElement("square", .2, .2, .4, 0, 0, 0);
       } 
       else if (phantomname == "bead"){
           this.addElement("ellipse", .48, .48, .05, 0, 0, 0);
           //this.addElement("ellipse", .0005, .0005, 2, -.08, -0.02, 0);
           //this.addElement("ellipse", .0005, .0005, 2, 0.08, 0.02, 0);
           //this.addElement("ellipse", .0005, .0005, 2, -0.02, 0.11, 0);
           //this.addElement("ellipse", .0005, .0005, 2, 0.02, -0.11, 0);   
           
           //this.addElement("ellipse", .0005, .0005, 2, -.035, -0.025, 0);
           //this.addElement("ellipse", .0005, .0005, 2, 0.02, 0.035, 0);
           //this.addElement("ellipse", .0005, .0005, 2, 0.02, -.035, 0);
           this.addElement("ellipse", .0006, .0006, 1, -0.01, 0.1, 0);  
           

       } 
   }   

}


class PhantomElement {

    public String type;
    public double A, B, x, y, rho;
    public int alpha;

    public PhantomElement(){
        type = "null";
        A = 0;
        B = 0;
        rho = 0;
        x = 0;
        y = 0;            
    }

    public PhantomElement(String t, double w, double h,  double d, double x, double y, int a){
        type = t;
        A = w;
        B = h;
        rho = d;
        this.x = x;
        this.y = y;
        alpha = a;

    }

    public void updateSize(double w, double h){
        A = w;
        B = h;   
    }

    public void updatedensity(double d){
        rho = d;   
    }

    public double getdensity(){
        return rho;   
    }
    
  
    
}