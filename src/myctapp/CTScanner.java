package myctapp;

import ij.*;
import ij.process.*;
import java.awt.image.*;
import java.awt.color.ColorSpace;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import javax.imageio.*;
import java.awt.geom.Ellipse2D;

/*@author     Damien Farrell
 *@created    February 2004 */
/**
 *Class to create simulated scan (proj) data either from image reprojection
 *or use phantom shapes to produce projection data that can be back projected.
 *this simulation uses a simple parallel beam scan-rotate geometry
 *The examples given from KUIM image processing system were a useful reference
 *as a starting point for writing the back projection algorithms
 *(http://www.cim.mcgill.ca/~sbouix/kuim/)
 */


public class CTScanner {
    
    //Nt - the transmitted no. of photons
    //R - radius of sphere
    //scans - number of scans in each 'view'
    //S - step position with respect to centre of rot.
    //phi - angle of rotation for each projection
    //views - no. of projection angles
    
    public int views, scans, S, ang1, ang2, numiter, projstacksize=1,
            numrays, totalrays, truncbeamrays ;
    public float phi, stepsize, v;
    public double muwater, filtercutoff, zoom, rate, xoffset, yoffset, factor,
            truncatewidth;   //width to truncate projections by, must be even
    public double projection[][]; // stores all projections for each view
    public double fprojection[][]; //stores filtered projections to be back projected
    public double truncprojection[][];
    public double pleft[], pright[];
    public double pixels[][]; //2-D array store pixel values
    public int outputimgsize, tagnum; //width-height of output image
    boolean fast, filtering, truncate, roicrop, displayimgdetails, displayroicircle,
            setedgezero, animate, interrupt, keepcurrentextrapwidths;
    public String method, filtername, interp, phantomname, truncmethod, noise;
    public double maxval;     //stores the max value of the current summ. image - temporary hack!
    public int animcount;     //global counter for animation of back projection
    public double animimage[][];    //summation image for animation of b proj. must also ne global
    Object projectionstack[];
    public double timetaken;
    
    public CTScanner(){
        initialise();
        
    }
    
    public CTScanner(double[][] pix){
        initialise();
        ProjectfromImage(pix, projection);
    }
    
    /**initialises scanning parameters*/
    public void initialise(){
        
        ang1=0;ang2=180;          //start and stop angles for projections
        stepsize = (float) 1;     //determines the no. of views, should be a divisor of 180
        v = ((float)(Math.abs(ang2)-ang1)/stepsize);
        views = (int) v;
        scans = 128;              //default value set here
        phi=0;                    //angle to loop over
        
        outputimgsize = 128;      //default output image size width=height
        zoom = 1;
        xoffset = 0;
        yoffset = 0;
        fast = false;
        method = "fbp";
        filtering = true;
        filtername = "ramp";
        interp = "linear";
        phantomname = "sheppl";
        filtercutoff = 1;
        numiter = 1; //no. of iterations
        truncate = false;
        roicrop = false;
        truncatewidth = .165;
        truncmethod = "simple cos-squared";
        displayimgdetails = false;
        displayroicircle = false;
        noise = "none";
        rate = 300;
        muwater = .206;
        numrays = totalrays = truncbeamrays = 0;
        tagnum = 1;
        animate = false; animcount=0;
        factor = Math.PI/4;
        setedgezero = true;
        interrupt = false;
        keepcurrentextrapwidths = false;
    }
    
    /**initialises the 2D arrays to hold the projection data*/
    public void initialiseprojection(){
        projection = new double[views][scans];
        fprojection = new double[views][scans];
    }
    
    /**resets the number of views using the currently chosen values of angle range
     *and stepsize*/
    public void resetviews(){
        v = (int) ((Math.abs(ang2)-ang1)/stepsize);
        views = (int) v;
    }
    
    
    
    /**method which creates projection data for an ellipse, given the axes sizes, offset
     *position and the rotation angle. This method can be used to create a phantom
     *consisting of the sum of projections for any number of ellipses */
    
    public double[][] ProjectEllipse(double A, double B, double rho, double x1, double y1, int alpha){
        int i=0;
        
        double[][] proj = new double[views][scans];
        double val, a2theta, boundary, N, offset, gamma, pos;
        double[] sintab = new double[views];
        double[] costab = new double[views];
        //scaling factor to match parameter values to array index, and hence the image
        double scale = (double)Math.sqrt(2)/scans;
        S=0;
        x1 /=scale; y1 /= scale;
        pos = Math.sqrt(x1*x1+y1*y1);
        gamma = Math.atan2(y1,x1);
        //zero all values in projections array
        Utils.blank(proj,0);
        
        for (phi=ang1;phi<ang2;phi=phi+stepsize){
            sintab[i] = Math.sin((double) (Math.abs(phi-alpha)) * Math.PI / 180);
            costab[i] = Math.cos((double) (Math.abs(phi-alpha)) * Math.PI / 180);
            i++;
        }
        
        // create projections for each angle
        i=0;
        //System.out.println("scale: "+scale);
        System.out.println("Generating projection data.. ");
        
        for (phi=ang1;phi<ang2;phi=phi+stepsize){
            a2theta = Math.pow(A * costab[i], 2) + Math.pow(B * sintab[i], 2) ;
            boundary = A* costab[i] + B* sintab[i] *500;     //half-width of ellipse..
            //translation for off-centre ellipse
            offset = pos * Math.cos(gamma-(double)(phi-alpha) * Math.PI/180);
            //System.out.print("a2theta= "+a2theta+"bound= "+boundary);
            for (S=0;S<scans;S++){
                N = (S - scans/2 + offset)*scale; //System.out.print("N="+N+" ");
                if (Math.abs(N) < boundary && a2theta > Math.pow(N,2)) {
                    val = 2*rho*A*B / a2theta * Math.sqrt(a2theta-Math.pow(N,2));
                } else{
                    val = 0;
                }
                proj[i][S] += val;
                //System.out.print(proj[i][S]+ " ");
            } i++;
        }
        
        return proj;
    }
    
    /**method which creates projection data for a square, given the axes sizes, offset
     *position and the rotation angle. This method can be utilised to create a phantom
     *consisting of the sum of projections for a number of shapes */
    
    public double[][] ProjectSquare(double A, double B, double rho, double x1, double y1, int alpha){
        int i=0;
        
        double[][] proj = new double[views][scans];
        double val, boundary, N, offset, gamma, pos, xp1, xm1, yp1;
        double[] sintab = new double[views];
        double[] costab = new double[views];
        
        //scaling factor to match parameter values to array index, and hence the image
        //double scale = (double)Math.sqrt(2)/scans;
        S=0;
        //x1 /=scale; y1 /= scale;
        pos = Math.sqrt(x1*x1+y1*y1);
        gamma = Math.atan2(y1,x1);
        //zero all values in projections array
        Utils.blank(proj,0);
        //setup trig values
        for (phi=ang1;phi<ang2;phi=phi+stepsize){
            sintab[i] = Math.sin((double) (Math.abs(phi-alpha)) * Math.PI / 180);
            costab[i] = Math.cos((double) (Math.abs(phi-alpha)) * Math.PI / 180);
            i++;
        }
        
        // create projections for each angle
        i=0;
        System.out.println("Generating projection data.. ");
        
     /* for (phi=ang1;phi<ang2;phi=phi+stepsize){
      
          //translation for off-centre object
          offset = pos * Math.cos(gamma-(double)(phi-alpha) * Math.PI/180);
      
          for (S=0;S<scans;S++){
              N = Math.abs((S - scans/2 + offset)*scale*3); //System.out.print("N="+N+" ");
              xp1 = N/costab[i]-(sintab[i]/costab[i]); //System.out.print("xp1: "+xp1+" ");
              xm1 = N/costab[i]+(sintab[i]/costab[i]); //System.out.print("xm1: "+xm1+" ");
              yp1 = (N-costab[i])/sintab[i]; //System.out.print("yp1: "+yp1+" ");
      
              if (Math.abs(N)>A) {
                    val =0;
              }
              else{
              if (xp1>1){
                  val = rho;
              }
              else if (xm1<1) {
                  val = 2*rho*A*B * 2/costab[i];
              }
              else {
                  val =  2*rho*A*B * Math.sqrt(Math.pow(1-xp1,2) + Math.pow(1-yp1,2));
              }
              }
      
              proj[i][S] += val;
              //System.out.print(proj[i][S]+ " ");
          } i++; System.out.println("i: "+i);
      } */
        
        i=0;
        double scale = (double)2/scans;
        double step = 2/(double)scans; System.out.println(step);
        for (double x = -1; x <= 1; x+=step){
            for (double y = -1; y <= 1; y+=step) {
                for (phi=ang1;phi<ang2;phi=phi+stepsize){
                    N = x*costab[i] - y*sintab[i];
                    
                    xp1 = N/costab[i]-(sintab[i]/costab[i]); //System.out.print("xp1: "+xp1+" ");
                    xm1 = N/costab[i]+(sintab[i]/costab[i]); //System.out.print("xm1: "+xm1+" ");
                    yp1 = (N-costab[i])/sintab[i]; //System.out.print("yp1: "+yp1+" ");
                    
                    if (Math.abs(N)>Math.sqrt(2)) {
                        val =0;
                    } else{
                        if (xp1 > 1){
                            val = rho;
                            //System.out.print(" case1");
                        } else if (xm1 < 1) {
                            val = 2*rho*A*B * 2/costab[i];
                            //System.out.print(" case2");
                        } else {
                            val =  2*rho*A*B * Math.sqrt(Math.pow(1-xp1,2) + Math.pow(1-yp1,2));
                            //System.out.print(" case3");
                        }
                    }
                    //System.out.print(N+" ");
                    S = (int)Math.round(N/2*scale);
                    S = S + scans/2;
                    
                    proj[i][S] += val;
                    i++;
                } i=0;
                
                
            } System.out.println(); S++;
        }
        return proj;
        
    }
    
    public double[][] ProjectGaussian(double A, double B, double rho, double x1, double y1, int alpha){
        int i=0;
        
        double[][] proj = new double[views][scans];
        double val, boundary, N, offset, gamma, pos, xp1, xm1, yp1;
        double[] sintab = new double[views];
        double[] costab = new double[views];
        double[] tantab = new double[views];
        //scaling factor to match parameter values to array index, and hence the image
        double scale = (double)Math.sqrt(2)/scans;
        S=0;
        x1 /=scale; y1 /= scale;
        pos = Math.sqrt(x1*x1+y1*y1);
        gamma = Math.atan2(y1,x1);
        offset = 0;
        //zero all values in projections array
        Utils.blank(proj,0);
        //setup trig values
        for (phi=ang1;phi<ang2;phi=phi+stepsize){
            sintab[i] = Math.sin((double) (Math.abs(phi-alpha)) * Math.PI / 180);
            costab[i] = Math.cos((double) (Math.abs(phi-alpha)) * Math.PI / 180);
            i++;
        }
        
        i=0;
        for (phi=ang1;phi<ang2;phi=phi+stepsize){
            for (S=0;S<scans;S++){
                N = (S - scans/2 + offset)*scale;
                val = 2*rho*A*B * Math.exp(-N*N/A)*Math.sqrt(Math.PI);
                proj[i][S] += val;
            } i++;
        }
        return proj;
    }
    
    
    
    /**Creates projection data by summing for the individual shapes */
    
    public void ProjectPhantom(){
        int i=0;
        CTPhantom phant = new CTPhantom();
        phant.CreatePhantomData(phantomname); //create a phantom object
        String elemtype = "ellipse";
        double A=0, B=0, x=0, y=0, rho=0;
        int alpha=0;
        initialiseprojection();
        double[][] projdata;
        int elems = phant.elements.size();
        System.out.println(elems);
        for (int e=0; e<elems; e++){
            try {
                PhantomElement shape = (PhantomElement) phant.getCurrentElement(e);
                elemtype = (String) shape.type;
                A = (double) shape.A;
                B = (double) shape.B;
                x = (double) shape.x;
                y = (double) shape.y;
                rho = (double) shape.rho;
                alpha = (int) shape.alpha;
                System.out.print(elemtype+ " "+A + " "+B+" "+rho+" "+ x+" "+y+" "+alpha);
                
            } catch (ArrayIndexOutOfBoundsException evt){
                System.out.println("NO ELEMENTS TO DISPLAY");  }
            //calls the method to project an ellipse using current parameters
            if (elemtype == "ellipse"){
                projdata = ProjectEllipse(A, B, rho, x, y, alpha);
            } else if (elemtype == "square"){
                projdata = ProjectSquare(A, B, rho, x, y, alpha);
            } else {
                projdata = ProjectGaussian(A, B, rho, x, y, alpha);
            }
            //phantom projection data is sum of all ellipse proj.
            for (phi=ang1;phi<ang2;phi=phi+stepsize){
                for (S=0;S<scans;S++){
                    projection[i][S] += projdata[i][S];
                } i++;
            } i=0;
        }
        Utils.normalize2DArray(projection,0,1);
        // add counting noise
        if (noise == "addtoproj"){
            Utils.addpoissonnoise(projection, rate);
            Utils.normalize2DArray(projection,0,1);
        }
        
        //Utils.outputdoubleArray(projection);
        
    }
    
    
    /**Creates a pixel matrix for a phantom to be used as base image in comparing
     *reconstructions
     *@return    image representing the phantom grey values*/
    public  BufferedImage CreatePhantomRaster(){
        int size = this.outputimgsize;
        CTPhantom phant = new CTPhantom();
        phant.CreatePhantomData(phantomname);
        double A=0, B=0, xpos=0, ypos=0, rho=0;
        String elemtype = "ellipse";
        double pos, val = 0;
        int alpha=0;
        double[][] image = new double[size][size];
        int x, y, Xcenter, Ycenter;
        Xcenter = size / 2;
        Ycenter = size / 2;
        
        // Initialize output image to zero
        for (x=0;x<size;x++){
            for (y=0;y<size;y++){
                image[x][y] = 0;
            }
        }
        //read in ellipse elements from the phantom object
        int elems = phant.elements.size();
        for (int e=0; e<elems; e++){
            try {
                PhantomElement shape = (PhantomElement) phant.getCurrentElement(e);
                elemtype = (String) shape.type;
                A = (double) shape.A;
                B = (double) shape.B;
                xpos = (double) shape.x;
                ypos = (double) shape.y;
                rho = (double) shape.rho;
                alpha = (int) shape.alpha;
                System.out.print(A + " "+B+" "+rho+" "+ xpos+" "+ypos+" "+alpha);
                System.out.println();
            } catch (ArrayIndexOutOfBoundsException evt){
                System.out.println("NO ELEMENTS TO DISPLAY");  }
            
            A = (double) A*size; B = (double) B*size; xpos = xpos *size; ypos = ypos*size;
            System.out.print(A + " "+B+" "+rho+" "+ xpos+" "+ypos+" "+alpha);
            double x1, y1;
            //for each element, calculate coords in matrix and add appropriate density value
            for (x = -Xcenter; x < Xcenter; x++){
                for (y = -Ycenter; y < Ycenter; y++) {
                    if (alpha != 90){ //only rotate co-ord system if necessary
                        x1 = x*Math.cos((alpha-90)*Math.PI/180) + y*Math.sin((alpha-90)*Math.PI/180);
                        y1 = -x*Math.sin((alpha-90)*Math.PI/180) + y*Math.cos((alpha-90)*Math.PI/180);
                        pos = Math.pow(x1+xpos,2)/Math.pow(A,2) + Math.pow(y1+ypos,2)/Math.pow(B,2);
                    } else
                        pos = Math.pow(x+xpos,2)/Math.pow(A,2) + Math.pow(y+ypos,2)/Math.pow(B,2);
                    if (pos <=1 ){
                        image[y+Ycenter][x+Xcenter]+= rho;
                    }
                }
            }
            
        } //Utils.normalize2DArray(image,0,1, true);
        
        //add poisson noise to image
        if (noise == ""){
            Utils.addpoissonnoise(image, rate);
        }
       /*Poisson p = new Poisson("p", rate );
       for (x = -Xcenter; x < Xcenter; x++){
           for (y = -Ycenter; y < Ycenter; y++) {
        
               double noise = (image[x+Xcenter][y+Ycenter] * Math.abs(rate - p.sample())/rate);
               //System.out.println(image[x+Xcenter][y+Ycenter]+" "+noise);
               image[x+Xcenter][y+Ycenter] += noise;
               //System.out.println(image[x+Xcenter][y+Ycenter]);
           }
       } */
        
        BufferedImage img = CreateImagefromArray(image, Utils.getMax(image), 1);
        return img;
    }
    
   /*
   public CTPhantom CreatePhantomData(String phantomname){
    
       CTPhantom phantom = new CTPhantom();
       phantom.CreatePhantomData(phantomname);
       return phantom;
    
   } */
    
    public void  ProjectfromImage(double[][] pix){
        initialiseprojection();
        projection = ForwardProject(pix);
    }
    
    public double[][] ProjectfromImage(double[][] pix, double[][] proj){
        initialiseprojection();
        proj = ForwardProject(pix);
        return proj;
    }
    
    public void PerformFBP(double[][] proj){
        initialiseprojection();
        pixels = BackProject(proj, outputimgsize);
    }
    
    public void doBackProjection(){
        pixels = BackProject(projection, outputimgsize);
    } 
    
    /**Converts the summation image with density values to hounsfield units*/
    public void ConverttoHounsfield(){
        for (int y = 0; y < pixels[0].length; y++ ) {
            for (int x = 0; x < pixels.length; x++ ) {
                pixels[x][y] = ((pixels[x][y]-muwater)/muwater) * 1000;
            }
        }
    }
    
    public double getScale(){
        double sc = zoom*outputimgsize*Math.sqrt(2)/scans;
        return sc;
    }
    
    
    
    /** Creates a set of projections from the pixel values in a greyscale image
  This projection data can then be reconstructed as normal and a comparison
  made between the reconstructed image and original
  The reprojection can be done using a fast method, which simply applies a value
  to the projection array based on its nearest radon transform value  */
    
    public double[][] ForwardProject(double[][] pix){
        int i;
        i=0;
        //following if statement prevents no. of scans from becoming larger than
        //the number of scans or projection fails
        //does not work as yet without interpolation 'down' of proj data to match
        //image size
        //if image width is greater than no. scans, the value is simply rounded off
        //as part of the algorithm, so it's not a problem
        //if (scans > pix.length){
        //    scans = pix.length;
        //}
        double[][] proj = new double[views][scans];
        double pos, val, Aleft, Aright;
        int x, y, Xcenter, Ycenter, Ileft, Iright;
        double[] sintab = new double[views];
        double[] costab = new double[views];
        double[] tantab = new double[views];
        S=0;
        int inputimgsize = pix[0].length;
        //int min = getMin(pix);
        //int max = getMax(pix);
        
        //zero all values in projections array
        Utils.blank(proj,0);
        
        for (phi=ang1;phi<ang2;phi=phi+stepsize){
            sintab[i] = Math.sin((double) phi * Math.PI / 180 - Math.PI/2);
            costab[i] = Math.cos((double) phi * Math.PI / 180 - Math.PI/2);
            i++;
        }
        
        // Project each pixel in the image
        Xcenter = inputimgsize / 2;
        Ycenter = inputimgsize / 2;
        i=0;
        //if no. scans is greater than the image width, then scale will be <1
        if (fast == false) System.out.println("Using accurate method ");
        
        double scale = inputimgsize*Math.sqrt(2)/scans;
        System.out.println("Generating projection data from image pixels.. ");
        
        //better way
        int N=0; val = 0;
        double weight = 0;
        double sang = Math.sqrt(2)/2;
        interrupt = false;
        
        for (phi=ang1;phi<ang2;phi=phi+stepsize){
            if (interrupt) break;
            double a = -costab[i]/sintab[i];
            double aa = 1/a;
            if (Math.abs(sintab[i]) > sang){
                for (S=0;S<scans;S++){
                    N = S - scans/2; //System.out.print("N="+N+" ");
                    double b = (N - costab[i] - sintab[i]) / sintab[i];
                    b =  b * scale;
                    //System.out.print("b="+b+" ");
                    
                    for (x = -Xcenter; x < Xcenter; x++){
                        if (fast == true){
                            //just use nearest neighbour interpolation
                            y = (int) Math.round(a*x + b);
                            
                            //System.out.print("x= "+x+" ");
                            //System.out.print("y="+y+", ");
                            if (y >= -Xcenter && y < Xcenter )
                                val += pix[(y+Ycenter)][(x+Xcenter)];
                            
                        } else {
                            //linear interpolation
                            y = (int) Math.round(a*x + b);
                            weight = Math.abs((a*x + b) - Math.ceil(a*x + b));
                            
                            if (y >= -Xcenter && y+1 < Xcenter )
                                val += (1-weight) * pix[(y+Ycenter)][(x+Xcenter)]
                                        + weight * pix[(y+Ycenter)][(x+Xcenter)];
                            
                        }
                    } proj[i][S] = val/Math.abs(sintab[i]); val=0;
                    
                }
            } else if (Math.abs(sintab[i]) <= sang){
                for (S=0;S<scans;S++){
                    N = S - scans/2;
                    double bb = (N - costab[i] - sintab[i]) / costab[i];
                    bb = bb * scale;
                    for (y = -Ycenter; y < Ycenter; y++) {
                        if (fast ==true){
                            x = (int) Math.round(aa*y + bb);
                            if (x >= -Xcenter && x < Xcenter )
                                val += pix[(y+Ycenter)][(x+Xcenter)];
                        } else {
                            x = (int) Math.round(aa*y + bb);
                            weight = Math.abs((aa*y + bb) - Math.ceil(aa*y + bb));
                            
                            if (x >= -Xcenter && x+1 < Xcenter )
                                val += (1-weight) * pix[(y+Ycenter)][(x+Xcenter)]
                                        + weight * pix[(y+Ycenter)][(x+Xcenter)];
                            
                        }
                    } proj[i][S] = val/Math.abs(costab[i]); val=0;
                    
                }
            } i++;
        }
        
        //old method used - produces bad artifacts at 'steep' angles
      /*
      for (x = -Xcenter; x < Xcenter; x++){
          for (y = -Ycenter; y < Ycenter; y++) {
             val = pix[x+Xcenter][y+Ycenter];
       
             for (phi=ang1;phi<ang2;phi=phi+stepsize){
       
                 pos = (x * sintab[i] - y * costab[i])/scale;
                 //System.out.println("phi="+phi+" "+"pos="+pos+", ");
                 if (fast == true){
                     S = (int) Math.round (pos);
                     weight = 1-Math.abs(S-pos);
                     //System.out.println("pos="+pos+", S="+S+", wght="+weight+" ");
                     S = S + scans/2;
                     proj[i][S] += val ;//* weight;
                     //System.out.println( proj[i][S]+" " );
       
                 }
                 else if (fast == false){
                 // Slower more accurate projection
                 // Calculate weighted values for both left and right projections
       
                   if (pos >= 0) {
                     Ileft = (int) Math.floor (pos);
                     //Ileft += scans/2;
                     Iright = Ileft + 1;
                     Aleft = Iright - pos;
                     Aright = pos - Ileft;
                   }
                   else {
                     Iright = (int) Math.ceil (pos);
                     //Iright += scans/2;
                     Ileft = Iright - 1;
                     Aleft = Math.abs(pos) - Math.abs(Iright)  ;
                     Aright = Math.abs(Ileft) - Math.abs(pos) ;
                   }
       
                   //adjust ileft and iright to match array index
                   Ileft += scans/2;
                   Iright += scans/2;
       
                   if (Ileft > 0 && Ileft < scans && Iright > 0 && Iright < scans) {
                        proj[i][Ileft] += (val * Aleft);
                        proj[i][Iright] += (val * Aright);
                   }
                   //System.out.print("pos= "+(pos/scale+scans/2)+"Aleft= "+Aleft+ "Aright= "+Aright);
       
                 }
                 i++;
             }
       
             i=0;
             val = 0;
          }
          //Fix 45 and 135 projections for fast method
           if (fast == true) {
               for (S = 0; S < scans; S++) {
                   //proj[0][S] = proj[1][S];
                   //proj[0][S] = 0;
                   //proj[views/2][S] = 0;
                   //proj[views/2][S] = proj[(int)(1 + views/2)][S];
                   //proj[views/4][S] = proj[1 + views/4][S];
                   //proj[3*views/4][S] = proj[1 + 3*views/4][S];
               }
           }
     } */
        i=0;
        
        //saveProjectionsFile(null);
        Utils.normalize2DArray(proj,0,1);
        if (noise == "addtoproj"){
            Utils.addpoissonnoise(proj, rate);
        }
        Utils.normalize2DArray(proj,0,1);
        //Utils.outputdoubleArray(proj);
        
        return proj;
    }
    
    /**Performs back projection and returns a 2D array of double pixel values that is used
   to create the summation image*/
    
    public double[][] BackProject(double[][] proj, int size){
        int i, a;
        int sxoffset=0, syoffset=0;
        i=0;
        double val = 0, pos, Aleft, Aright;
        
        double[][]  bpimage = new double[size][size];
        int x, y, Xcenter, Ycenter, Ileft, Iright;
        double[] sintab = new double[views];
        double[] costab = new double[views];
        double[][] nproj;
        
        S=0;
        
        //filter projections before back projection
        if (filtering == true && method == "fbp"){
            if (noise == "reconwithnoise"){
                nproj = Utils.ArrayCopy(proj);  //makes a copy of the array
                Utils.addpoissonnoise(nproj, rate);
                fprojection = Filter(nproj);
            } else {
                System.out.println(proj[0].length);
                fprojection = Filter(proj);
            }
        } else{
            fprojection = proj; //no filtering
        }
        
        //Utils.normalize2DArray(fprojection, -1, 1);
        //create tables of sin and cos values for each angle used
        for (phi=ang1;phi<ang2;phi=phi+stepsize){
            sintab[i] = Math.sin((double) phi * Math.PI / 180);
            costab[i] = Math.cos((double) phi * Math.PI / 180);
            i++;
        }
        
        // Initialize output image to zero
        for (x=0;x<size;x++){
            for (y=0;y<size;y++){
                bpimage[x][y] = 0;
            }
        }
        
        //Back Project each pixel in the image
        Xcenter = size / 2;
        Ycenter = size / 2;
        i=0;
        double scale = zoom*size*Math.sqrt(2)/scans;
        System.out.println("Performing back projection.. ");
        if (xoffset > 0) sxoffset = (int) Math.floor(xoffset*size*zoom);
        if (yoffset > 0) syoffset = (int) Math.floor(yoffset*size*zoom);
        //int soffset = (int) Math.floor(offset*scale);
        interrupt = false;
        if (animate == false){
            for (x = -Xcenter; x < Xcenter; x++){
                if (interrupt == true) break;
                for (y = -Ycenter; y < Ycenter; y++) {
                    int x1 = x - sxoffset;
                    int y1 = y - syoffset;
                    if (Math.abs(x1) <= Xcenter+Math.abs(sxoffset) &&
                            Math.abs(y1) <= Ycenter+Math.abs(syoffset) ){
                        
                        for (phi=ang1;phi<ang2;phi=phi+stepsize){
                            //pos = (x1 * sintab[i] - y1 * costab[i]);
                            pos = (-x1 * costab[i] + y1 * sintab[i]);
                            //System.out.print("pos= "+pos+" ");
                            
                            if (interp == "nearest"){
                                S = (int)Math.round(pos/scale);
                                S = S + scans/2;
                                if (S<scans && S>0)
                                    val = val + fprojection[i][S];
                            }
                            //perform linear interpolation
                            else if (interp == "linear"){
                                if (pos>=0){
                                    a = (int)Math.floor(pos/scale);
                                    int b = a + scans/2;
                                    if (b<scans-1 && b>0){
                                        val = val +  fprojection[i][b] + (fprojection[i][b+1]
                                                - fprojection[i][b])
                                                * (pos/scale-(double)a);
                                    }
                                } else if (pos<0){
                                    a = (int)Math.floor(pos/scale);
                                    int b = a + scans/2;
                                    if (b<scans-1 && b>0){
                                        val = val + fprojection[i][b] + (fprojection[i][b]
                                                - fprojection[i][b+1])
                                                * (Math.abs(pos/scale) - Math.abs(a));
                                    }
                                }
                            }
                            i++;
                        }S=0;i=0;
                        
                        bpimage[x + Xcenter][y + Ycenter] = val/views;
                        //bpimage[x + Xcenter][y + Ycenter] = val*Math.PI/2*views;
                        //img = img*pi/(2*length(theta));
                        
                        val=0;
                    }
                }
            }
        }
        
        else if (animate == true){
            i = animcount;
            for (x = -Xcenter; x < Xcenter; x++){
                for (y = -Ycenter; y < Ycenter; y++) {
                    int x1 = x - sxoffset;
                    int y1 = y - syoffset;
                    if (Math.abs(x1) <= Xcenter+Math.abs(sxoffset) &&
                            Math.abs(y1) <= Ycenter+Math.abs(syoffset) ){
                        //pos = (x1 * sintab[i] - y1 * costab[i]);
                        pos = (-x1 * costab[i] + y1 * sintab[i]);
                        if (interp == "nearest"){
                            S = (int)Math.round(pos/scale);
                            S = S + scans/2;
                            if (S<scans && S>0)
                                val = val + fprojection[i][S];
                        }
                        //perform linear interpolation
                        else if (interp == "linear"){
                            if (pos>=0){
                                a = (int)Math.floor(pos/scale);
                                int b = a + scans/2;
                                if (b<scans-1 && b>0){
                                    val = val +  fprojection[i][b] + (fprojection[i][b+1]
                                            - fprojection[i][b])
                                            * (pos/scale-(double)a);
                                }
                            } else if (pos<0){
                                a = (int)Math.floor(pos/scale);
                                int b = a + scans/2;
                                if (b<scans-1 && b>0){
                                    val = val + fprojection[i][b] + (fprojection[i][b]
                                            - fprojection[i][b+1])
                                            * (Math.abs(pos/scale) - Math.abs(a));
                                }
                            }
                        }
                    }S=0;
                    //this is a global variable
                    animimage[x + Xcenter][y + Ycenter] += val/views;
                    
                    val=0;
                    
                }
            } //System.out.println("ANIMCOUNT: "+animcount);
        }
        if (animate == false){
            return bpimage;
        } else {        
            //save the animated images
            String path = "C:\\Documents and Settings\\farrell\\My Documents\\myctapp\\anim\\";
            String filename = path+"frame"+"_"+i;
            System.out.println("i"+i);
            maxval = Utils.getMax(animimage);
            BufferedImage currimg = CreateImagefromArray(animimage, maxval, 1);
            ImagePanel imgpanel = new ImagePanel();

            imgpanel.loadBufferedImage(currimg);
            imgpanel.setPixelData();
            imgpanel.lowerwinlvl = 0;
            imgpanel.upperwinlvl = 2000;
            imgpanel.PerformWindowing();

            Utils.saveanImage(filename, imgpanel.windowedImage);
            //imgpanel.saveImage(filename);
            return animimage;
        }
        
    }
    
    
    /**filter the projection data before back projection. This is done here in the
  frequency domain by multiplying the FT of the proj data with a frequency filter
  and then retrieving the inverse FT of the result  */
    
    public double[][] Filter(double[][] proj){
        
        int i, pscans;
        double filter[], pfilter[];   //array to store filter and padded filter
        
        double[] rawdata;
        double[] idata;
        
        double[][] fproj = new double[views][scans];
        
        //length of array - no of 'scans', must be a power of 2
        //if scans is a power of 2 then just allocated twice this value for arrays and then pad
        //the projection (and filter data?) with zeroes before applying FFT
        if (Utils.isPow2(scans) == true){
            pscans = scans;   //System.out.println("power of 2");
        }
        //if scans is not a power of 2, then round pscans up to nearest power and double
        else {
            int power = (int)((Math.log(scans) / Math.log(2)))+1; //closest power of 2 rounded up
            pscans = (int) Math.pow(2, power);
            System.out.println("PSCANS: "+pscans);
        }
        rawdata = new double[pscans*2];
        idata = new double[pscans*2];
        pfilter = new double[pscans*2];
        
        for (S = 0; S < pscans; S++) {
            idata[S] = 0;
        }
        
        // Initialize the filter
        filter = Filters.filter1(filtername, pscans*2, filtercutoff);
        
      /*for (S = 0; S<scans; S++) {
          pfilter[S] = filter[S];
      }
      //zero pad filter
      for (S = scans; S<pscans*2; S++) {
          pfilter[S] = 0;
      }*/
        i=0;
        
        // Filter each projection
        for (phi = ang1; phi < ang2; phi+=stepsize) {
            for (S = 0; S<scans; S++) {
                rawdata[S] = proj[i][S];
            }
            //zero pad projections
            for (S = scans; S<pscans*2; S++) {
                rawdata[S] = 0;
            }
            Filters.FFT(1, pscans*2, rawdata, idata);
            for (S = 0; S<scans*2; S++) {
                rawdata[S] *= filter[S];
            }
            //perform inverse fourier transform of filtered product
            Filters.FFT(0, pscans*2, rawdata, idata);
            for (S = 0; S<scans; S++) {
                fproj[i][S] = rawdata[S];
            }
            for (S = 0; S<pscans*2; S++) {
                idata[S] = 0;
            }
            i++;
        }
        return fproj;
    }
    
    /**truncates projections to simulate a ROI study*/
    public void truncateProjections(double width){
        
        truncprojection = Utils.ArrayCopy(projection);
        int i=0;
        int radius = (int) Math.ceil(width*scans);
        //make sure radius is even value
        if (radius%2 != 0) radius ++; System.out.println("R= "+radius);
        
        int s1= (int)(scans/2)-radius; System.out.println("S1= "+s1);
        int s2= (int)(scans/2)+radius; System.out.println("S2= "+s2);
        
        for (phi = ang1; phi < ang2; phi+=stepsize) {
            for (S = 0; S<=s1-1; S++) {
                projection[i][S] = 0;
            }
            for (S = s2+1; S<scans; S++) {
                projection[i][S] = 0;
            } i++;
        }
        System.out.println(width);
    }
    
    /**prepare truncated projections before filtering and back projection using the
  method proposed by Seger
  first step is to remove the DC offset and hence the discontinuity at the edges
  the second step is to multiply outer projection data by a cos-squared function*/
    
    public void fixtruncatedProjections(double width){
        int i=0;
        double corr, p1, p2;
        int radius = (int) Math.ceil(width*scans);
        //make sure radius is even value
        if (radius%2 != 0) radius ++;
        
        int s1= (int)(scans/2)-radius;
        int s2= (int)(scans/2)+radius;
        
        //find average of the two end values and subtract from the projection
        //this is suggested in Seger paper for the normal case where p(k) not equal p(-k)
        //Utils.normalize2DArray(projection,0,1);
        for (phi = ang1; phi < ang2; phi+=stepsize) {
            p1 = projection[i][s1];    //p1 is p(k) from Seger notation
            p2 = projection[i][s2];
          /*if (p1 != p2){
            corr = (p1 + p2)/2; //Math.min(p1,p2);
          }
          else {
            corr = p1;
          }  */
            //System.out.println("CORR= "+corr);
            //for method1
          /*for (S=scans*2/6;S<scans*4/6+2; S++) {
              projection[i][S] -= corr;
          } */
            for (S=0;S<=s1; S++) {
                //projection[i][S] = p1-corr;     //for method1
                projection[i][S] = p1;
            }
            for (S=s2;S<scans; S++) {
                //projection[i][S] = p2-corr;     //for method1
                projection[i][S] = p2;
            }
            
            //extraploate out with cos-squared or sqaure root roll-off
            if (truncmethod == "simple cos-squared"){
                if (p1 != 0){
                    for (S=0;S<s1; S++) {
                        //projection[i][S] -= (Math.pow(Math.cos(Math.PI*((S-p1-corr)/(2*s1))),2)); // method1
                        //projection[i][S] = p1 * (Math.pow(Math.cos(Math.PI*((S-radius/2)/(radius))),2)); //method3
                        projection[i][S] -= Math.pow(Math.cos(Math.PI*((S-p1)/(2*s1))),2); //method2
                        //if ( projection[i][S] < 0 ) projection[i][S] =0;
                    }
                    for (S=s2;S<scans; S++) {
                        //projection[i][S] -= (1-Math.pow(Math.cos(Math.PI*((S+p2-corr)/(s2))),2)); //method1
                        //projection[i][S] = p2 * Math.pow(Math.cos(Math.PI*((S+radius/2)/(radius))),2);
                        projection[i][S] -= (1-Math.pow(Math.cos(Math.PI*((S+p2)/(s2))),2));   //method2
                        // if ( projection[i][S] < 0 ) projection[i][S] =0;
                    }
                }
            } else if (truncmethod == "simple square root"){
                if (p1 != 0){
                    for (S=0;S<s1; S++) {
                        projection[i][S] = p1 * Math.pow(((S-p1)/(2*s1)),.5); 
                    }
                    for (S=s2;S<scans; S++) {
                        projection[i][S] = p2 * (Math.pow(((S+p2)/(s2)),.5));
                    }
                }
            }
            
            /*double[] x = new double[scans];
            for (S=0;S<scans; S++) {
                x[S] = projection[i][S];
            }
            Utils.normalize1DArray(x,0,1);
            for (S=0;S<scans; S++) {
                projection[i][S] = x[S];
            }   */         
            
            i++;
        }
    }
    
    
    /**prepare truncated projections before filtering and back projection using the
   ADT method */
    
    private void replaceouterProjections(double[][] rproj, double width){
        int i=0;
        double corr, p1, p2;
        int radius = (int) Math.ceil(width*scans);
        //make sure radius is even value
        if (radius%2 != 0) radius ++;
        
        double[] r = new double[scans];
        int s1= (int)(scans/2)-radius;
        int s2= (int)(scans/2)+radius;
        
        //Utils.normalize2DArray(projection,0,1);
        //Utils.normalize2DArray(rproj,0,1);
        
        for (phi = ang1; phi < ang2; phi+=stepsize) {
            p1 = projection[i][s1];    //p1 is p(k) from Seger notation
            p2 = projection[i][s2];
            System.out.print("p1= "+p1);
            System.out.println("p2= "+p2+" ");
            
            for (S=0;S<scans; S++) {
                r[S] = rproj[i][S];   //temp array for replacement proj data
            }
            //normalize outer proj data to edge value, p1 and p2
            Utils.normalize1DArray(r,0,p1);
            //now replace outer projection data
            for (S=0;S<s1; S++) {
                projection[i][S] = r[S];
                System.out.print(r[S]+",");
            } System.out.println();
            Utils.normalize1DArray(r,0,p2);
            for (S=s2+1;S<scans; S++) {
                projection[i][S] = r[S];
            }
            
            i++;
        }
    }

    /**gets the positions in the array of left and right data extent for each view*/
    private void GetProjectionWidths(double[][] proj){
        int i=0;
        S=0;
        int radius = (int) Math.ceil(truncatewidth*scans);
        //make sure radius is even value
        if (radius%2 != 0) radius ++;
        int s1 = (int)(scans/2)-radius;
        int s2 = (int)(scans/2)+radius;
        pleft = new double[views];
        pright = new double[views];
        
        //Utils.normalize2DArray(projection,0,1);
        //Utils.normalize2DArray(rproj,0,1);
        for (phi = ang1; phi < ang2; phi+=stepsize) {
            S=0;
            while (proj[i][S] <= 0 && S<=s1){
                if (proj[i][S] <= 0) pleft[i] = S;
                S++;
            } pleft[i] = S;
            //System.out.print(pleft[i]+", ");
            S=scans-1;
            while (proj[i][S] <= 0 && S>=s2){
                if (proj[i][S] <= 0) pright[i] = S;
                S--;
            }  pright[i] = S;
            //System.out.println(pright[i]);
            i++;
        } i=0;
        
    }
    
    /**Extrapolates with cos-squared function, but sets widths based on full data
   The full data is estimated from back projecting the full object, in this
   case a thresholded imafe of the edges of the object can be used to eliminate
   'false' data that might appear in a real image because of other structures
   outside the body  like the ct couch or clothing */
    
    private void AdaptiveDetrunc(double width){
        int i=0;
        double corr, p1, p2;
        int radius = (int) Math.ceil(width*scans);
        //make sure radius is even value
        if (radius%2 != 0) radius ++;
        int s1 = (int)(scans/2)-radius;
        int s2 = (int)(scans/2)+radius;
        
        //Utils.normalize2DArray(projection,0,1);
        //Utils.normalize2DArray(rproj,0,1);
        
        //do extrapolation based on proj data widths recorded above
        for (phi = ang1; phi < ang2; phi+=stepsize) {
            p1 = projection[i][s1];    //p1 is p(k) from Seger notation
            p2 = projection[i][s2];
            //System.out.print("p1= "+p1);
            //System.out.println("p2= "+p2+" ");
            
            for (S=0;S<=s1; S++) {
                projection[i][S] = p1;
            }
            for (S=s2;S<scans; S++) {
                projection[i][S] = p2;
            }
            //extraploate out with cos-squared roll-off, width determined by pleft and pright
            //double rad1 = factor*(s1-pleft[i])/scans;
            double rad1 = (s1-pleft[i]);
            //System.out.print("pleft="+pleft[i]+", "+"rad1="+rad1+" ");
            //double rad2 = factor*(pright[i]-s2)/scans;
            double rad2 = (pright[i]-s2);
            //System.out.println("pright="+pright[i]+", "+"rad2="+rad2+" ");
            if (pleft[i]<s1){
                for (S=s1;S>=0; S--) {
                    //double W = ((S-s1)/rad1)/(scans/128);
                    double W = Math.PI/4*((S-s1)/rad1);

                    projection[i][S] -=  1-Math.pow(Math.cos((W)),2);
                    //projection[i][S] -= 1-Math.pow(Math.cos(Math.PI*(W)),2);
                    //projection[i][S] -= Math.pow(Math.cos(Math.PI*((S-p1)/(2*s1))),2); //method2

                    //if (S < pleft[i] && S<s1) projection[i][S]=projection[i][S+1];
                    if (setedgezero == true)
                        if (S < pleft[i] && S<s1) projection[i][S]= 0;
                    if ( projection[i][S] < 0 ) projection[i][S] =0;
                }
            }
            if (pright[i]>s2){
                for (S=s2;S<scans; S++) {
                    //double W = ((S-s2)/rad2)/(scans/128);
                    double W = Math.PI/4*((S-s2)/rad2);
                    projection[i][S] -= 1-Math.pow(Math.cos((W)),2);
                    //projection[i][S] -= (1-Math.pow(Math.cos(Math.PI*((S+p2)/(rad2))),2));   //method2

                    //if (S > pright[i] && S>s2) projection[i][S]=projection[i][S-1];
                    if (setedgezero == true)
                        if (S > pright[i] && S>s2) projection[i][S] = 0;
                    if ( projection[i][S] < 0 ) projection[i][S] =0;
                }
            }
            i++;
        }
        //Utils.normalize2DArray(projection,0,1);
    }
    
    /**Hybrid-Adaptive Detruncation method to correct for truncated projections  */
    private double[][] performHDTReconstruction(){
        
        int dscans = scans * 2;
        //back project adjusted proj data
        double[][] temppixels = BackProject(projection, outputimgsize);
        double[][] tempproj = new double[views][dscans];
        double[][] img = new double[outputimgsize][outputimgsize];
        
        for (int iter=0;iter<=2;iter++){
            
            //blank inner ROI from temp image
            blankROI(temppixels, outputimgsize);
            
            //now forward project the temporary image
            tempproj = ForwardProject(temppixels);
            
            //replace outer projection data with this tempproj
            replaceouterProjections(tempproj, truncatewidth);
            
            //back project final proj data
            img = BackProject(projection, outputimgsize);
            
            temppixels = img;
        }
        return img;
        
    }
    
    
    /**if we wish to crop the image to an ROI, zero pixel values outside region of interest
    this is applied to the short array used to create the BufferedImage*/
    public short[] CropPixelstoROI(short[] pix, int size){
        
        int Xcenter = size / 2;
        int Ycenter = size / 2;
        double scale = zoom*size*Math.sqrt(2)/scans;
        int radius = (int) Math.ceil(truncatewidth*scans);
        //make sure radius is even value
        if (radius%2 != 0) radius ++; System.out.println("R= "+radius);
        
        for (int x = -Xcenter; x < Xcenter; x++){
            for (int y = -Ycenter; y < Ycenter; y++) {
                double val = Math.sqrt(Math.pow(x,2)+Math.pow(y,2));
                if (val > (radius*2)*(scale/2)){
                    //pix[y + Ycenter][x + Xcenter] = 0;
                    pix[(y + Ycenter) + (x + Xcenter)  * size] = 2000;
                }
            }
        }
        return pix;
    }
    
    /**if we wish to draw a circle to highlight the ROI, this is applied
     * directly to the input BufferedImage, so becomes part of the image raster*/
    public void DrawROICircle(BufferedImage img){
        
        int size = outputimgsize;
        int Xcenter = size / 2;
        int Ycenter = size / 2;
        double scale = zoom*size*Math.sqrt(2)/scans;
        int diam = (int) Math.ceil(truncatewidth*scans*2);
        //make sure diam is even value
        if (diam%2 != 0) diam ++;
        double d = diam*scale;    //scale diameter to image
        Graphics2D g2 = (Graphics2D) img.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(Color.white);
        //draw the roi circle
        g2.draw(new Ellipse2D.Double(Xcenter-d/2, Ycenter-d/2, d, d));
        
    }
    
    
    public double[][] blankROI(double[][] pix, int size){
        
        int Xcenter = size / 2;
        int Ycenter = size / 2;
        double scale = zoom*size*Math.sqrt(2)/scans;
        int radius = (int) Math.ceil(truncatewidth*scans);
        //make sure radius is even value
        if (radius%2 != 0) radius ++; System.out.println("R= "+radius);
        
        for (int x = -Xcenter; x < Xcenter; x++){
            for (int y = -Ycenter; y < Ycenter; y++) {
                double val = Math.sqrt(Math.pow(x,2)+Math.pow(y,2));
                if (val < (radius*2)*(scale/2)){
                    
                    pix[x + Xcenter][y + Ycenter] = 0;
                }
            }
        }
        return pix;
    }
    
    /**Estimate the number of rays in the current projection data and the rays that
     *pass through the object (values > 0). */
    public void FindNumRays(){
        int i=0;
        
        double[] sintab = new double[views];
        double[] costab = new double[views];
        S=0;
        
        int radius = (int) Math.ceil(truncatewidth*scans);
        //make sure radius is even value
        if (radius%2 != 0) radius ++; //System.out.println("R= "+radius);
        
        int A, B, C, D;
        A= (int) Math.ceil(scans*0.05);//System.out.println("A= "+A);
        B= (int) Math.ceil(scans*0.1);//System.out.println("B= "+B);
        C= (int) Math.ceil(scans*0.15);//System.out.println("C= "+C);
        D= (int) Math.ceil(scans*0.2);//System.out.println("C= "+C);
        
        int numA=0, numB=0, numC=0, numD=0;
        
        int s1= (int)(scans/2)-radius; //System.out.println("S1= "+s1);
        int s2= (int)(scans/2)+radius; //System.out.println("S2= "+s2);
        
        int A1 = (int)(scans/2)-A;
        int A2 = (int)(scans/2)+A;
        int B1 = (int)(scans/2)-B;
        int B2 = (int)(scans/2)+B;
        int C1 = (int)(scans/2)-C;
        int C2 = (int)(scans/2)+C;
        int D1 = (int)(scans/2)-D;
        int D2 = (int)(scans/2)+D;
        
        
        totalrays = 0; numrays = 0; truncbeamrays = 0;
        //System.out.println("Estimating number of rays .. ");
        for (phi=ang1;phi<ang2;phi=phi+stepsize){
            for (S=0;S<scans;S++){
                totalrays++;
                if (projection[i][S] > 0) numrays++;
                if (truncate == true){
                    if ((S>s1 && S<s2) && projection[i][S] > 0){
                        truncbeamrays++;
                    }
                }
                if (projection[i][S] > 0){
                    if (S>A1 && S<A2) numA++;
                    if (S>B1 && S<B2) numB++;
                    if (S>C1 && S<C2) numC++;
                    if (S>D1 && S<D2) numD++;
                }
            }  i++;
            
        } i=0;
        numD = numD-numC;
        numC = numC-numB;
        numB = numB-numA;
        
      /*System.out.println();
      System.out.println("TOTAL RAYS: "+totalrays);
      System.out.println("RAYS thru obj: "+numrays);
      System.out.println("RAYS thru A: "+numA);
      System.out.println("RAYS thru B: "+numB);
      System.out.println("RAYS thru C: "+numC);
      System.out.println("RAYS thru D: "+numD);*/
        //System.out.println("% thru obj: "+(double)numrays/totalrays);
        if (truncate == true){
            //System.out.println("For truncated beam at "+truncatewidth);
            //System.out.print("RAYS thru obj: "+truncbeamrays+" ");
            double truncrayperc = Utils.round((double)truncbeamrays/numrays,2);
            //System.out.println("% truncated beam rays: "+truncrayperc);
        }
        
    }
    
    
    /**creates the back projected image by calling method BackProject, which returns
  the projected pixel values. These values are normalized and then put into a
  databuffer which is used to create the greyscale bufferedimage for display */
    
    public BufferedImage CreateReconstructedImage(){
        
        double startTime = 0;
        timetaken = 0;
        BufferedImage bpimg;
        int i,j;
        i=0; j=0;
        if (projection == null){
            ProjectPhantom();
        } else { scans = projection[0].length;}
        if (animate == false) FindNumRays();
        if (truncate == true){
            pixels = BackProject(projection, outputimgsize); //create full recon. first
            maxval = Utils.getMax(pixels);       //get max in full recon data for proper scaling
            System.out.println("MAXVAL: "+maxval);
            startTime = System.currentTimeMillis();
            if (keepcurrentextrapwidths == false || pleft == null){
             GetProjectionWidths(projection);   //get untruncated data object widths
            } 
            truncateProjections(truncatewidth);
            double[][] roipixels = new double[outputimgsize][outputimgsize];
            if (truncmethod == "simple cos-squared" || truncmethod == "simple square root" ){     //just use cos-sq roll-off
                if (animate == false)
                    fixtruncatedProjections(truncatewidth);
                if (method == "fbp"){
                    roipixels = BackProject(projection, outputimgsize);
                } else if (method =="iterative"){
                    //roipixels = Iterative.PerformMLEM(this,projection);
                    roipixels = Iterative.PerformART(this);
                }
            } else if (truncmethod == "adaptive cos-squared" ){     //do additional stuff aswell
                AdaptiveDetrunc(truncatewidth);
                roipixels = BackProject(projection, outputimgsize);
            } else if (truncmethod == "vangompel" ){     //do additional stuff aswell
                fixtruncatedProjections(truncatewidth);
                roipixels = performROIReconstruction();
                maxval = Utils.getMax(roipixels);
            } else if (truncmethod == "HDT" ){     //do additional stuff aswell
                //fixtruncatedProjections(truncatewidth);
                AdaptiveDetrunc(truncatewidth);
                roipixels = performHDTReconstruction();
                maxval = Utils.getMax(roipixels);
            } else if (truncmethod == "none" ){
                roipixels = BackProject(projection, outputimgsize);
            }
            //Utils.normalize2DArray(roipixels, 0, maxval);
            bpimg = CreateImagefromArray(roipixels, maxval, 1);     //create roi image
            pixels = roipixels;
            timetaken = (System.currentTimeMillis() - startTime)/1000;

        } else if (method =="iterative" && truncate == false){
            //pixels = Iterative.PerformMLEM(this);
            pixels = Iterative.PerformART(this);
            maxval = Utils.getMax(pixels);
            bpimg = CreateImagefromArray(pixels, maxval, 1);
        } else if (animate == true){
             doBackProjection();
             maxval = Utils.getMax(pixels);
             bpimg = CreateImagefromArray(pixels, maxval, 0);
        } else{    //normal FBP reconstruction
            startTime = System.currentTimeMillis();
            pixels = BackProject(projection, outputimgsize);
            maxval = Utils.getMax(pixels);
            bpimg = CreateImagefromArray(pixels, maxval, 1);
            timetaken = (System.currentTimeMillis() - startTime)/1000;
        }
        
        //Utils.normalize2DArray(pixels, 0, .5);
        //Utils.zeronegvals2DArray(pixels);
        //BufferedImage bpimg = CreateImagefromArray(pixels);
        //ConverttoHounsfield();
        
        if (displayimgdetails == true){
            Graphics2D g = (Graphics2D) bpimg.getGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            //g.drawString(outputimgsize+"x"+outputimgsize,
            //bpimg.getWidth()-30, bpimg.getHeight()-8);
            g.setColor(Color.black);
            g.drawString(Integer.toString(tagnum), bpimg.getWidth()/2+2, 42);            
            g.setColor(Color.white);
            g.drawString(Integer.toString(tagnum), bpimg.getWidth()/2, 40);
            tagnum++;
        }
        if (displayroicircle == true){
            DrawROICircle(bpimg);
        }
        return bpimg;
        
    }
    
    /**Creates an imnage from a 2D array of doubles */
    public BufferedImage CreateImagefromArray(double[][] pix, double max, int type){
        
        int i,j;
        i=0; j=0;
        
        short[] pixelshortArray = new short[pix.length * pix[0].length];
        double min = Utils.getMin(pix);

        //Utils.normalize2DArray(pix,0,max,false);
        Utils.zeronegvals2DArray(pix);
        int gray;
        //System.out.println("rescaling output image: ");
        
        for ( int y = 0; y < pix[0].length; y++ ) {
            for ( int x = 0; x < pix.length; x++ ) {
                if (min<0){
                        gray = (int) ((pix[x][y])*2000/(max)); 
                } else{
                    
                        gray = (int) ((pix[x][y])*2000/(max)); 
                }
                pixelshortArray[x + y * pix.length] = (short) gray;
            }
        }
        if (roicrop == true){
            pixelshortArray = CropPixelstoROI(pixelshortArray, outputimgsize);
        }
        BufferedImage img;
        // returns an 8-bit buffered image for display purposes
        if (type == 1){
            img = ImageUtils.create12bitImage(outputimgsize,outputimgsize, pixelshortArray);
        } else {
            img = ImageUtils.getGrayBufferedImage(pixelshortArray, pix.length,
                    pix[0].length, 0, 2000);
        }
        
        return img;
        
    }
    
    
    /**creates a sinogram from the projection data by outputting pixel values to a
  buffered image with a greyscale colorspace */
    
    public BufferedImage CreateSinogram(String filename){
        int x,y;
        x=0; y=0;
        int gray;
        
        if (projection == null){
            ProjectPhantom();
        }
        if (filename != null){
            loadProjectionsFile(filename);
        }
        double min = Utils.getMin(projection);System.out.println(min);
        double max = Utils.getMax(projection);System.out.println(max);
        short[] pixelshortArray = new short[projection.length * projection[0].length];
        
        for ( x = 0; x < projection.length; x++ ) {
            for ( y = 0; y < projection[0].length; y++ ) {
                //rescale pixel values for 12-bit grayscale image??
                if (max>min){
                    gray = (int)((projection[x][y])*2000/(max));
                } else{
                   gray = (int)((projection[x][y]-min)*2000/(max));
                }
                pixelshortArray[y + x * projection[0].length] = (short) gray;
                //System.out.print(pixelshortArray[y + x * projection[0].length]);
                //System.out.print(", ");
            }
            //System.out.println();
        }
        //BufferedImage sinoimg = ImageUtils.getGrayBufferedImage(pixelshortArray, projection[0].length,
          //      projection.length, 0, 2000);
        BufferedImage sinoimg = ImageUtils.create12bitImage(projection[0].length, projection.length, pixelshortArray);   
        // Class cast the BufferedImage as an Image and return it.
        return sinoimg;
        
    }
    
    
    /**Saves the projection data as a text file with each line representing
     *a set of projections for each angle*/
    
    public void saveProjectionsFile(String filename){
        
        double[][] array = new double[views][scans];
        
        array = projection;  //just use most recently stored single set of proj data
        PrintWriter writer;
        
        try{
            if (filename == null){
                writer = new PrintWriter(new FileWriter("/proj.txt"));
            } else{
                writer = new PrintWriter(new FileWriter(filename));
            }
            writer.println(projstacksize);
            writer.println(stepsize);
            writer.println(array.length);
            writer.println(array[0].length);
            for ( int p = 0; p < projstacksize; p++ ) {
                if (projstacksize>1){
                    array = (double[][]) projectionstack[p];
                }
                for ( int y = 0; y < array[0].length; y++ ) {
                    for ( int x = 0; x < array.length; x++ ) {
                        writer.print( array[x][y]+" ");
                    } writer.println();
                } //writer.println();
                
            }
            writer.close();
        } catch (IOException ioe) {
            System.out.println("I/O Exception in file writing: "+ioe); }
        
    }
    
    /**loads the projection data from the text file, each line has a projection
     * The first four values are the stacksize, stepsize, views, scans respectively  */
    
    public void loadProjectionsFile(String filename){
        double[][] array;
        float step; int s;int v; int ps=1;
        File file;
        BufferedReader reader;
        //read header
        try{
            if (filename == null){
                file = new File("/proj.txt");
                reader = new BufferedReader(new FileReader(file));
            } else{
                file = new File(filename);
                reader = new BufferedReader(new FileReader(file));
            }
            ps =  Integer.valueOf(reader.readLine()).intValue();
            step = Float.valueOf(reader.readLine()).floatValue();
            v = Integer.valueOf(reader.readLine()).intValue();
            s = Integer.valueOf(reader.readLine()).intValue();
            
            System.out.println("projstacksize="+ps);
            System.out.println("v="+v);System.out.println("s="+s);
            reader.close();
        } catch (IOException ioe) { System.out.println("I/O Exception "+ioe);
        s=0;v=0;step=0;}
        array = new double[v][s];
        projstacksize = ps;
        views = v;
        scans = s;
        stepsize = step;
        //if (ps > 1) { projectionstack = new Object[ps]; }
        try{
            if (filename == null){
                file = new File("/proj.txt");
                reader = new BufferedReader(new FileReader(file));
            } else{
                file = new File(filename);
                reader = new BufferedReader(new FileReader(file));
            }
            reader.readLine();reader.readLine();reader.readLine();reader.readLine(); //skip over header
            // process the entire file, of space or comma-delimited data
            String aLine = new String();
            for ( int p = 0; p < ps; p++ ) {
                for ( int y = 0; y < array[0].length; y++ ) {
                    aLine = reader.readLine();
                    StringTokenizer st = new StringTokenizer(aLine);
                    for ( int x = 0; x < array.length; x++ ) {
                        String str = st.nextToken();
                        array[x][y] =  Double.parseDouble(str);
                    }
                } //reader.readLine();
                /*if (ps > 1){
                    projectionstack[p] = array;
                }*/
                //array = new double[v][s];
            }
            reader.close();
        } catch (IOException ioe) { System.out.println("I/O Exception "+ioe); }
        initialiseprojection();
        projection = array;
    }
    
    /**imports the projection data from an image sinogram, results may be noisy */
    
    public void importProjections(ImagePlus inputimg, boolean incols, int ps){
        
        int s;int v; //int ps=1;
        short[] sarray; byte[] barray;
        
        int type = inputimg.getType();
        projstacksize = ps;
        //infopane.append("stacksize= "+ps+"\n");
        if (ps > 1) { projectionstack = new Object[ps]; }
        
        ImageProcessor inputip  = inputimg.getProcessor();
        if (incols == true){
            v = inputip.getWidth(); s = inputip.getHeight();
        } else{
            s = inputip.getWidth(); v = inputip.getHeight();
        }
        
        System.out.println("Importing projection data as image "+"\n");
        views = v;
        scans = s;
        stepsize = 180/v;
        System.out.println("Scans= "+s+" "); System.out.println("Views= "+v+"\n");
        System.out.println("Stepsize= "+stepsize+"\n ");
        
        //these are defined in the case of a stack of images as input.
        //ImageStack inputstack = imp.getImageStack();
        //ImageStack sinogramstack = new ImageStack(views,scans);
        
        for ( int p = 0; p < ps; p++ ) {
            
            //if (cancelled == true) break;
            projection = new double[v][s];
            
            //now put pixel values from image into the projection data array
            if (type == ImagePlus.GRAY16){
                if (ps > 1){
                    System.out.println("data "+p+"\n");
                    //inputip = (ShortProcessor) inputstack.getProcessor(p+1);
                } else {
                    inputip= (ShortProcessor) inputimg.getProcessor();
                }
                sarray = (short[]) inputip.getPixels();
                if (incols == true){
                    System.out.println("incols"); System.out.println("incols "+p+"\n");
                    for (int x = 0; x < projection[0].length; x++ ) {
                        for (int y = 0; y < projection.length; y++ ) {
                            projection[y][x] = sarray[y + x * projection.length] &0xFFFF;
                        }
                    }
                } else{
                    System.out.println("inrows"); System.out.println("inrows "+p+"\n");
                    for (int x = 0; x < projection[0].length; x++ ) {
                        for (int y = 0; y < projection.length; y++ ) {
                            projection[y][x] = sarray[x + y * projection[0].length] &0xFFFF;
                        }
                    }
                }
                
            } else{
                if (ps > 1){
                    System.out.println("data "+p+"\n");
                    //inputip = (ByteProcessor) inputstack.getProcessor(p+1);
                } else {
                    inputip= (ByteProcessor) inputimg.getProcessor();
                }
                barray = (byte[]) inputip.getPixels();
                if (incols == true){
                    for (int x = 0; x < projection[0].length; x++ ) {
                        for (int y = 0; y < projection.length; y++ ) {
                            projection[y][x] = barray[y + x * projection.length] &0xFF;
                        }
                    }
                } else{
                    for (int x = 0; x < projection[0].length; x++ ) {
                        for (int y = 0; y < projection.length; y++ ) {
                            projection[y][x] = barray[x + y * projection[0].length] &0xFF;
                        }
                    }
                }
                
            }
            /*if (ps > 1){
                projectionstack[p] = projection;
                ImageProcessor sino = createSinogram(false);
                sinogramstack.addSlice(Integer.toString(p+1),sino);
            }*/
        }
        /*if (ps > 1){
            ImagePlus sinoimages = new ImagePlus("Proj Stack", sinogramstack);
            sinoimages.show();
            dostackbox.setSelected(true); doentirestack = true;
        }*/
        
    }
    
    
    
    public void fanrebin(double[][] proj){
        
        
    }
    
    
    /**for use with method proposed by Tissan et al. in SPIE paper - see paper */
    
    private void applyGaussiantoProjections(double[][] arr, String choice){
        int i=0;
        double corr, p1, p2;
        double[] gauss = CreateGaussian(scans);
        if (choice == "multiply"){
            for (phi = ang1; phi < ang2; phi+=stepsize) {
                for (S=0; S<scans; S++) {
                    arr[i][S] = arr[i][S] * gauss[S];
                }
                i++;
            }
        } else if (choice == "divide"){
            for (phi = ang1; phi < ang2; phi+=stepsize) {
                for (S=0; S<scans; S++) {
                    arr[i][S] = arr[i][S] / gauss[S];
                }
                i++;
            }
        }
        
    }
    
    /**create discrete 1D Gaussian for use in correcting truncated projections
   sigma is the standard deviation of the distribution, with a mean at
   halfway along the array*/
    public double[] CreateGaussian(int scans){
        
      /*double corr, p1, p2;
      int radius = (int) Math.ceil(width*scans);
      //make sure radius is even value
      if (radius%2 != 0) radius ++;
       
      int s1= (int)(scans/2)-radius;
      int s2= (int)(scans/2)+radius; */
        
        
        double[] gauss = new double[scans];
        int i;
        double width = scans*2/6;
        //double width = scans*truncatewidth*2;
        double sigma = width/2.3548;
        double half = (scans -1)/2;
        for (i=0; i<scans; i++)
            gauss[i] = (double) ((1.0 / (Math.sqrt(2.0*Math.PI)*sigma)) *
                    Math.exp(-Math.pow((i-half),2)/(2*Math.pow(sigma,2))));
        Utils.normalize1DArray(gauss,0,1);
        return gauss;
        
    }
    
    /**create a 2D Gaussian matrix for use in correcting truncated projections
   same idea as 1D aove, but in 2-dimensions*/
    public double[][] Create2DGaussian(int size){
        
        double[][] gauss = new double[size][size];
        int Xcenter = size / 2;
        int Ycenter = size / 2;
        int x, y;
        double width = size*4/6;
        double sigma = width/2.3548;
        for (x = -Xcenter; x < Xcenter; x++){
            for (y = -Ycenter; y < Ycenter; y++) {
                
                gauss[y + Ycenter][x + Xcenter] = 1/(2*Math.PI*(Math.pow(sigma,2))) *
                        Math.exp(-(Math.pow(x,2)+Math.pow(y,2))/(2*Math.pow(sigma,2)));
                
                //System.out.print(gauss[x + Xcenter][y + Ycenter]);
            }
            //System.out.println();
        }
        
        //int[][] test = this.BackProject(size);
        
        //gauss =  multiplyIntDoubleArrays(test, gauss);
        //gauss =  divideIntDoubleArrays(test, gauss);
        //BufferedImage img = CreateDummyImage(gauss);
        //return img;
        Utils.normalize2DArray(gauss,0,1);
        return gauss;
    }
    
    
    /**method to correct for truncated proj. steps are those proposed in Tissan et. al */
    
    private double[][] performROIReconstruction(){
        
        //fixtruncatedProjections();                //cos-squared extrapolation
        applyGaussiantoProjections(projection, "multiply");   // multiply each projection with 1D gaussian
        double[][] temppixels = BackProject(projection, outputimgsize*3/2);     //back project adjusted proj data
        double[][] gaussian = Create2DGaussian(outputimgsize*3/2);               //multiply temp image by gaussian
        temppixels = Utils.multiply2Darrays(temppixels, gaussian);
        double[][] tempproj = new double[views][scans*2];
        //ProjectfromImage(temppixels);
        //tempproj = ForwardProject(temppixels);
        scans = scans*2;
        tempproj = ProjectfromImage(temppixels, tempproj);      //forward project the temporary image
        applyGaussiantoProjections(tempproj, "divide");                   //divide each proj by 1D gaussian
        
        //initialiseprojection();
        double[][] img = BackProject(tempproj, outputimgsize);        //back project final proj data
        gaussian = Create2DGaussian(outputimgsize);
        img = Utils.divide2Darrays(img, gaussian);                 //divide final image by gaussian
        //Utils.normalize2DArray(img,0,1);
        scans = scans/2;
        return img;        
    }
        
    //Main method for running the ctscanner class on command line
    
    public static void main(String[] args) {
        
        for (int i = 0; i < args.length; i++)
            System.out.println(args[i]);
        
        CTScanner scanner = new CTScanner();
        BufferedImage img;
        
        scanner.scans = 512;
        //scanner.stepsize = (float) 0.8;
        scanner.views = 225;
        scanner.outputimgsize = 256;
        scanner.phantomname = "test1";
        
        scanner.filtername = "hann";
        scanner.filtercutoff = 1;
        scanner.zoom = 1.4;
        
        //scanner.truncate = true;
        //scanner.roicrop = true;
        //scanner.truncatewidth = .2;
        scanner.truncmethod = "adaptive cos-squared";
        scanner.displayimgdetails = true;
        //scanner.displayroicircle = true;
        //scanner.noise = "reconwithnoise";
        scanner.rate = 650;
        
        scanner.tagnum = 1;
        double t;
        int i=0;  int numsims = 10;
        //for (i=0;i<=numsims;i++){
            for (t=.14;t<=.14;t+=0.01){
            i=1;
            scanner.ProjectPhantom();
            scanner.truncatewidth = Utils.round(t,2);
            //String path = "C:\\Program Files\\Java\\projects\\testimages\\";
            String path = "C:\\Documents and Settings\\farrell\\My Documents\\myctapp\\testimages\\";
            String filename = path+"brain.txt"; //+"_"+i;
            scanner.loadProjectionsFile(filename);
            //ImagePanel imgpanel = new ImagePanel();
            //imgpanel.getbuffImagePixels(filename, scanner);
            
            img = scanner.CreateReconstructedImage();
            
            //String filename = "C:\\Program Files\\Java\\projects\\mtf_testimages2\\"
              //      +"beadphant"+scanner.truncatewidth;//+"_"+;
            //String filename = "C:\\Program Files\\Java\\projects\\test\\"
            //        +"test4_full";//+scanner.truncatewidth;       
            //String filename = "C:\\Documents and Settings\\damien\\My Documents" +
            //"\\course\\projects\\mtf_testimages\\"+"beadphant_"+scanner.truncatewidth;//+"_"+i;
            //Utils.saveanImage(filename, img);
            
            ImagePanel imgpanel = new ImagePanel();
            imgpanel.loadImage(filename+".png");
            imgpanel.loadBufferedImage(img);
            imgpanel.setPixelData();
            imgpanel.lowerwinlvl = 978;
            imgpanel.upperwinlvl = 1220;
            imgpanel.PerformWindowing();
            
            Utils.saveanImage(filename, imgpanel.windowedImage);
            //while (!imgpanel.finished){
            //long time = System.currentTimeMillis();
            //}
            //if (imgpanel.finished == true)
            //Utils.saveanImage(path+"r"+i, imgpanel.mBufferedImage);
            
        }
        
    }
    
}
