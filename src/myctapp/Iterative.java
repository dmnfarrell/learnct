package myctapp;

import java.awt.image.*;

/**@author     Damien Farrell
 *@created    February 2004 */
 /**this class contains test methods for iterative reconstruction */

public class Iterative {
    
      private static int views;
      private static int scans;
      private static float phi;
      private static int ang1, ang2;
      private static float stepsize;
      private static boolean fast;
      private static  String noise;
      private static double rate;
      public static int outputimgsize;
      public static int numiter;
      
      
      //constructor
      public static void InitialiseData(CTScanner c){
          views = c.views;
          scans = c.scans;
          phi = c.phi;
          ang1 = c.ang1;
          ang2 = c.ang2;
          stepsize = c.stepsize;
          fast = c.fast;
          noise = c.noise;
          rate = c.rate;
          outputimgsize = c.outputimgsize;
          numiter = c.numiter;
      }
      
   
  public static double[][] FindWeights(double[][] pix){
      int i=0;
      int j=0;
      int S=0; int ang=0;
      double[][] proj = new double[views][scans];
      double pos, val, Aleft, Aright;
      int x, y, Xcenter, Ycenter;
      double[] sintab = new double[views];
      double[] costab = new double[views];

      int inputimgsize = pix[0].length;
      
      for (phi=ang1;phi<ang2;phi=phi+stepsize){
          sintab[ang] = Math.sin((double) phi * Math.PI / 180 - Math.PI/2);
          costab[ang] = Math.cos((double) phi * Math.PI / 180 - Math.PI/2);
          ang++;
      } 

      // Project each pixel in the image 
      Xcenter = inputimgsize / 2;
      Ycenter = inputimgsize / 2;
      i=0;
      
      double scale = inputimgsize*1.42/scans;
      System.out.println("Getting weights from image pixels.. ");
      
      int N=0; val = 0;
      double weight = 0;
      double bw = 1/scans;
      double sang = Math.sqrt(2)/2;
      
      //weighting matrix for jth pixel
      double[][] W = new double[views*scans][inputimgsize];
      int w_index = 0;
      for (i = 0;i<views*scans;i++){
      //for (phi=ang1;phi<ang2;phi=phi+stepsize){

          //val = pix[x+Xcenter][y+Ycenter];
          x=0;y=0;
          pos = (x * sintab[ang] - y * costab[ang])/scale;

          for (S=0;S<scans;S++){
          //proj[ang][S] += val ;
          W[i][j]=0;
          }
          j++;
          ang++;   

           j=0; 
      }
      ang=0;
      i=0;
      
      //Utils.normalize2DArray(W,0,1);
      Utils.outputdoubleArray(W);
      return W;
  }  
  
  
  public static double[][] PerformART(CTScanner c ){
      
      InitialiseData(c);
      double lambda = 1;
      int outputimgsize = c.outputimgsize;
      double[][] proj = c.projection;
      
      int i=0; double val=0; int S=0; int N=0; int x,y; //double diff=1e-4;
      double[] sintab = new double[views];
      double[] costab = new double[views];
      double[] sintab1 = new double[views];
      double[] costab1 = new double[views];
      for (phi=ang1;phi<ang2;phi=phi+stepsize){
          sintab[i] = Math.sin((double) phi * Math.PI / 180 - Math.PI/2);
          costab[i] = Math.cos((double) phi * Math.PI / 180 - Math.PI/2);
          i++;
      } i=0;
      for (phi=ang1;phi<ang2;phi=phi+stepsize){
          sintab1[i] = Math.sin((double) phi * Math.PI / 180);
          costab1[i] = Math.cos((double) phi * Math.PI / 180);
          i++;
      } i=0;

      // Project each pixel in the image
      int Xcenter = outputimgsize / 2;
      int Ycenter = outputimgsize / 2;
      double scale = outputimgsize*1.42/scans;
      double sang = Math.sqrt(2)/2;
      double weight =  0;
      
      //temporary projection data to be used for comparison with actual data
      //double[][] tempproj = new double[views][scans];
      double[][] W = new double[views][scans];
      
      double[][] pix = new double[outputimgsize][outputimgsize];
      double[][] temppixels = new double[outputimgsize][outputimgsize];
      double[] diff = new double[scans];
      double[] guess = new double[scans];
      double[] ww = new double[scans];
      
      pix = c.BackProject(proj,outputimgsize);
      double m = Utils.getMax(pix);
      System.out.println("M="+m);
      //Utils.blank(pix,1);    //initial guess

      Utils.blank(W,1);
      
      for (int j=1;j<=numiter;j++){
          System.out.println("Iteration " +j);
          lambda /= numiter;
          for (phi=ang1;phi<ang2;phi=phi+stepsize){
              System.out.println("phi= "+phi);
              double a = -costab[i]/sintab[i] ;
              double aa = 1/a;
              if (Math.abs(sintab[i]) > sang){
                  for (S=0;S<scans;S++){               
                      N = S - scans/2; //System.out.print("N="+N+" ");
                      double b = (N - costab[i] - sintab[i]) / sintab[i];
                      b =  b * scale;
                      
                      for (x = -Xcenter; x < Xcenter; x++){
                          if (fast == true){
                              //just use nearest neighbour interpolation
                              y = (int) Math.round(a*x + b);
                              
                              //System.out.print("x= "+x+" ");
                              //System.out.print("y="+y+", ");
                              if (y >= -Xcenter && y < Xcenter ){
                                  val += pix[(x+Xcenter)][(y+Ycenter)];
                                  
                                  W[i][S]++;}
                              
                          } else {
                              //linear interpolation
                              y = (int) Math.round(a*x + b);
                              weight = Math.abs((a*x + b) - Math.ceil(a*x + b));
                              
                              if (y >= -Xcenter && y+1 < Xcenter ){
                                  val += (1-weight) * pix[(x+Xcenter)][(y+Ycenter)]
                                  + weight * pix[(x+Xcenter)][(y+Ycenter+1)];
                                W[i][S]++;}
                          }
                      }
                      
                      val /= W[i][S];
                      guess[S] = val;///(Math.abs(sintab[i])); 
                      //System.out.print("guess="+guess[S]); System.out.println(" proj="+proj[i][S]);
                      val=0; 

                  }
              }
               if (Math.abs(sintab[i]) <= sang){
                  for (S=0;S<scans;S++){
                      N = S - scans/2;
                      double bb = (N - costab[i] - sintab[i]) / costab[i];
                      bb = bb * scale;
                      for (y = -Ycenter; y < Ycenter; y++) {
                          x = (int) Math.round(aa*y + bb);
                          if (fast == true){
                              if (x >= -Xcenter && x < Xcenter ){
                                  val += pix[x+Xcenter][y+Ycenter];
                                   W[i][S]++;}
                          }
                          else {
                              weight = Math.abs((aa*y + bb) - Math.ceil(aa*y + bb));
                              
                              if (x >= -Xcenter && x+1 < Xcenter ){
                                  val += (1-weight) * pix[(x+Xcenter)][(y+Ycenter)]
                                  + weight * pix[(x+Xcenter+1)][(y+Ycenter)];
                                   W[i][S]++;}
                              
                          } //System.out.print("val="+val+" x="+x+" y="+y);
                      }
                      val /= W[i][S];            
                      guess[S] = val;///Math.abs(costab[i]); 
                      //System.out.print("guess="+guess[S]); System.out.println(" proj="+proj[i][S]);
                      val=0; 

                  }
              } System.out.println();
              
              //Utils.normalize1DArray(guess,0,1);
              for (S=0;S<scans;S++){
                    ww[S]= W[i][S];
              }
              
              Utils.normalize1DArray(ww,0,2);
              /*
              for (S=0;S<scans;S++){
                    
                    diff[S] = actual[S] - guess[S];
                    //diff[S] /= W[i][S];
                    //if (diff[S] <1e-4) diff[S]=0;
                    //System.out.print("proj="+actual[S]+" ");
                    //System.out.print("guessproj="+guess[S]+" ");
                    //System.out.print("W="+W[i][S]+" ");
                    //System.out.print("diff="+diff[S]+" ");
                    
              }  */
              S=0; double st = sintab1[i]; double ct = costab1[i];
              
              /*for (x = -Xcenter; x < Xcenter; x++){
                  for (y = -Ycenter; y < Ycenter; y++){
                      
                      double pos = x * st - y * ct;
                      //System.out.print("pos= "+pos+" ");

                      S = (int)Math.round(pos/scale); weight=Math.abs(S-pos);
                      S = S + scans/2;
                      
                      W[i][S] = weight;
                      System.out.println("W= "+weight+" ");
                      if (S>0 && S<scans) ;

                  }
              } System.out.println();*/
              
              //now backproject correction for these rays in guess image
              for (x = -Xcenter; x < Xcenter; x++){
                  for (y = -Ycenter; y < Ycenter; y++){
                      
                      double pos = x * st - y * ct;
                      //System.out.print("pos= "+pos+" ");
                      
                      S = (int)Math.round(pos/scale); 
                      weight=Math.abs((int)(Math.round(pos))-pos);
                      if (weight>1) weight=0;
                      //System.out.println("W= "+weight+" ");
                      S = S + scans/2;

                      //System.out.print("W="+actual[S]+" ");
                      //System.out.print("S= "+S+" ");
                      if (S>0 && S<scans)
                        if (weight >0 && weight<1){
                          diff[S] = ((proj[i][S] - guess[S])/ww[S])*(1-weight);
                          pix[x+Xcenter][y+Ycenter] += diff[S];//*(weight);
                        }
                          
                      if (pix[x+Xcenter][y+Ycenter] <0) pix[x+Xcenter][y+Ycenter] =0;
                      //if (pix[x+Xcenter][y+Ycenter] >m) pix[x+Xcenter][y+Ycenter] =m;

                  }
              } System.out.println();
              //Utils.normalize2DArray(pix,0,m);
              i++;
            /*double max = Utils.getMax(pix);
            BufferedImage img = c.CreateImagefromArray(pix,max);
            String filename = "C:\\temp\\"
            +"tesprojimg"+i+"_"+j;
            Utils.saveanImage(filename, img);  */

          }
          i=0;
      }

      //return W;
      return pix;
  }
  
  
   /**Performs a simple MLEM type iterative reconstruction */
  public static double[][] PerformMLEM(CTScanner c){
      
      InitialiseData(c);
      double[][] proj = c.projection;
      int i=0; double err, weight;
      int S=0;
      //temporary projection data to be used for comparison with actual data
      double[][] tempproj = new double[views][scans];
      double[][] projratio = new double[views][scans];
      double[][] W = new double[views][scans];
      double[] ww = new double[scans];     
      //setup pixel arrays
      double[][] pixels = new double[outputimgsize][outputimgsize];
      double[][] correction = new double[outputimgsize][outputimgsize];
      double[][] gauss = c.Create2DGaussian(outputimgsize);
      double[] sintab1 = new double[views];
      double[] costab1 = new double[views];
      
      for (phi=ang1;phi<ang2;phi=phi+stepsize){
          sintab1[i] = Math.sin((double) phi * Math.PI / 180);
          costab1[i] = Math.cos((double) phi * Math.PI / 180);
          i++;
      } i=0;
      
      Utils.blank(pixels,1);
      //pixels = BackProject(projection, outputimgsize);

      int Xcenter = outputimgsize / 2;
      int Ycenter = outputimgsize / 2;
      double scale = outputimgsize*1.42/scans;
      
      for (int j=1;j<=numiter;j++){
          System.out.println("Iteration " +j);
          //forward project the estimated pixel array
          tempproj = c.ForwardProject(pixels);
          //Utils.normalize2DArray(tempproj, 0, 1);
           
          for (phi = ang1; phi < ang2; phi+=stepsize) {
                    
              //now compare estimate with actual
              for (S = 0; S<scans; S++) {
                  err = (proj[i][S] / tempproj[i][S]);
                  //System.out.println("actual= "+proj[i][S]+" ");
                  //System.out.println("guess= "+tempproj[i][S]+" ");
                  if (err > 0 ) projratio[i][S] = err ;
                  else projratio[i][S] = 0;
                  System.out.print(projratio[i][S]+" ");
                  
              }  i++; //System.out.println();
          } i=0;

          //create correction image
          //correction = c.BackProject(projratio, outputimgsize);
          
          //backproject error with weighting
          for (phi = ang1; phi < ang2; phi+=stepsize) {   
             for (int x = -Xcenter; x < Xcenter; x++){
                  for (int y = -Ycenter; y < Ycenter; y++){
                      double pos = x * sintab1[i] - y * costab1[i];

                      //System.out.print("pos= "+pos+" ");
                      S = (int)Math.round(pos/scale); 
                      //get weights 
                      weight=Math.abs((int)(Math.round(pos))-pos);

                      //System.out.println("W= "+weight+" ");
                      S = S + scans/2;

                      if (S>0 && S<scans)
                        //if (weight >0 && weight<1){
                          correction[x+Xcenter][y+Ycenter] += projratio[i][S]*(1-weight);

                  }
              } 
            i++;
          } i=0;
          
          Utils.normalize2DArray(correction, 0, 1);
          
          //multiply correction with previous estimate to get new estimate
          pixels = Utils.multiply2Darrays(pixels, correction);
          
          /*for (int x = -Xcenter; x < Xcenter; x++){
              for (int y = -Ycenter; y < Ycenter; y++){
                  pixels[x+Xcenter][x+Xcenter] *= correction[x+Xcenter][x+Xcenter];
                  
              }
          } */

          //Utils.normalize2DArray(pixels, 0, 1);
          
      }
      //Utils.normalize2DArray(pixels, 0, 1);
      return pixels;
      
  }
  
    public static void main(String[] args) {
      
      BufferedImage img; String filename; double max;
      CTScanner c = new CTScanner();
      c.scans = 128; c.numiter=2;
      //c.fast = true;
      c.outputimgsize = 64;
      //c.phantomname = "test4";
      c.ProjectPhantom();

      
      //double[][] est = PerformART(c);
      double[][] est = PerformMLEM(c);
      max = Utils.getMax(est);
      
      img = c.CreateImagefromArray(est,max,0);
      filename = "C:\\temp\\"
                              +"testimg_"+numiter;
      Utils.saveanImage(filename, img);
           
    }
  
  
}
  