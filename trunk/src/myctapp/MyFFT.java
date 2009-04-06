package myctapp;

import java.awt.*;
import java.io.*;

/**FFT class to compute 2D FFT for images*/
//found at http://acg.media.mit.edu/courses/mas961/people/geilfuss/ps1/source/

public class MyFFT{

   private final int FORWARD_FFT = -1;
   private final int REVERSE_FFT = 1;
   private double direction = (double)FORWARD_FFT;
   static final double twoPI = (double)(2 * Math.PI);
   private int N;
   private int numBits;
   private int width, height;
   private double minPSD = 9999999;
   private double maxPSD = -9999999;
   public boolean DisplayLogPSD = false;
   // These arrays hold the complex image data.
   double[][] cR_r, cR_i; // Red real and imaginary
   double r_data[] = null;
   double i_data[] = null;
   double mag[] = null;


   public void myfft(){
       
       init();
   }
   
private void init() {

      minPSD = 9999999;
      maxPSD = -9999999;
}



public int[] psd() {

      // Take PSD on complex RGB values.
      // Magnitude of result.
      double[] magnitudeR = magnitudeSpectrum(cR_r, cR_i);
      double scaleFactor = 100; //255.0/Math.log(256);
      System.out.println("Max psd = "+maxPSD);
      scaleFactor = 255.0/(Math.log(1+maxPSD));
      System.out.println("Scalefactor = "+scaleFactor);

      // Adjust 2-D FFT data so that the minimum PSD is
      // based at a value close to a black pixel.

      for(int i=0; i<N; i++) {
           magnitudeR[i] = (double) (scaleFactor * Math.log(1+magnitudeR[i]));
      }

      System.out.println("Minimum PSD value: " + minPSD);
      mag = magnitudeR;
      // Convert to single ARGB int array and return.
      //(int)((Math.log(scans) / Math.log(2)))+1;
      System.out.println(mag.length);
      return(pad(mag, mag.length));
}



public int[] pad(double[] pixels, int w)  {
    //image must be square
    
    //System.out.println("in pad");
    int[] ip = new int[w];
    for (int i=0; i<w; i++)  {
        int val = (int)pixels[i];
        //ip[i] = 0xFF000000 | (val<<16) | (val<<8) | val;
        ip[i] = val;
    }
    return(ip);
}


public int[] forward2dFFT(double[] imageData_R, int imageWidth, int imageHeight) {
      init();

      // save image size to locals.
      width = imageWidth;
      height = imageHeight;
      // We know the size of the image now, allocate
      // the required arrays to hold the complex image data.
      cR_r = new double[height][width];
      cR_i = new double[height][width];
      // Total number of pixels.
      N = width * height;
      // Number of bits in one direction, i.e. 256x256 image m = 8
      // assuming image is square, which has already been checked.
      numBits = (int)(Math.log((double)width)/Math.log(2.0));
      copydoubleToComplex(cR_r, cR_i, imageData_R);
      for(int i=0; i<height; i++)
      {
         forwardFFT(cR_r[i], cR_i[i]);
      }

      System.out.println("FFT on rows:");
      cR_r = Rotate90(cR_r);
      cR_i = Rotate90(cR_i);
      System.out.println("Rotate 90 degrees CW:");
      for(int i=0; i<height; i++)
      {
         forwardFFT(cR_r[i], cR_i[i]);
      }
      System.out.println("Triple FFT on columns:");
      return psd();
}


   public int[] reverse2dFFT(double[] newmag){
      double pscale;
      init();
      N = width * height;

      numBits = (int)(Math.log((double)width)/Math.log(2.0));

      for(int i=0; i<height; i++){
        for(int j=0; j<width; j++){
         pscale = newmag[i*height+j]/mag[i*height+j];
         cR_r[i][j] = cR_r[i][j]*pscale*0.9f;
         cR_i[i][j] = cR_i[i][j]*pscale*0.9f;
        }
         reverseFFT(cR_r[i], cR_i[i]);
      }

      System.out.println("IFFT on rows:");
      cR_r = Rotate90(cR_r);
      cR_i = Rotate90(cR_i);
      System.out.println("Rotate 90 degrees CW:");
      for(int i=0; i<height; i++){
         reverseFFT(cR_r[i], cR_i[i]);
      }
      System.out.println("IFFT on columns:");
      rotateInPlace180(cR_r);
      rotateInPlace180(cR_i);
      double[] realR = magnitudeSpectrum(cR_r, cR_i);
      return(pad(realR, realR.length));
   }

   /** 1D FFT utility functions */

   public void swap(int i){

      double tempr;
      int j = bitr(i);
      String js = Integer.toBinaryString(j);
      String is = Integer.toBinaryString(i);

      tempr = r_data[j];
      r_data[j] = r_data[i];
      r_data[i] = tempr;
      tempr = i_data[j];
      i_data[j] = i_data[i];
      i_data[i] = tempr;
   }



   /** swap Zi with Zj */

   public void swapInt(int i, int j) {

      double tempr;
      int   ti;
      int   tj;
      ti = i-1;
      tj = j-1;
      tempr = r_data[tj];
      r_data[tj] = r_data[ti];
      r_data[ti] = tempr;
      tempr = i_data[tj];
      i_data[tj] = i_data[ti];
      i_data[ti] = tempr;
   }

   double getMaxValue(double in[]){

      double max;
      max = -0.99e30;
     for(int i=0; i<in.length; i++)
       if(in[i]  >max)
      max = in[i];
     return max;
  }


   void bitReverse2(){

      /* bit reversal */
      int n = r_data.length;
      int j=1;
      int k;

      for (int i=1; i<n; i++){
         if (i<j)
         {
            swapInt(i,j);
         } //if
         k = n / 2;
         while (k >=1 &&  k < j){
            j = j - k;
            k = k / 2;
         }
         j = j + k;
      } // for
   }



   void bitReverse(){
     /* bit reversal */
     int n = r_data.length;
     int j=1;
     int k;

     for (int i=1; i<n; i++){
       if  (i<j) {
          // this does not work...
          // why?
           swap(i);
          } //if
   j = bitr(i);
  } // for
 }


 int bitr(int j) {
    int ans = 0;
    for (int i = 0; i< numBits; i++) {
       ans = (ans <<1) + (j&1);
       j = j>>1;
    }
    return ans;
 }



   public void forwardFFT(double[] in_r, double[] in_i){

      direction = FORWARD_FFT;
      fft(in_r, in_i);
   }



   public void reverseFFT(double[] in_r, double[] in_i)  {

      direction = REVERSE_FFT;
      fft(in_r, in_i);
   }



   /**FFT engine  */

   public void fft(double in_r[], double in_i[]){

	  int id;
      // radix 2 number if sample, 1D of course.
      int localN;
      double wtemp, Wjk_r, Wjk_i, Wj_r, Wj_i;
      double theta, tempr, tempi;
      int ti, tj;

      // Truncate input data to a power of two
      int length = 1 << numBits; // length = 2**nu
      int n=length;

      // Copy passed references to variables to be used within
      // fft routines & utilities
      r_data = in_r;
      i_data = in_i;
      bitReverse2();
      for(int m=1; m<=numBits; m++) {
         // localN = 2^m;
         localN = 1 << m;
         Wjk_r = 1;
         Wjk_i = 0;
         theta = twoPI / localN;
         Wj_r = (double)Math.cos(theta);
         Wj_i = (double)(direction * Math.sin(theta));
         int nby2 = localN / 2;
         for(int j=0; j<nby2; j++)  {

            // This is the FFT innermost loop
            // Any optimizations that can be made here will yield
            // great rewards.

            for(int k=j; k<n; k+=localN){

               id = k + nby2;
               tempr = Wjk_r * r_data[id] - Wjk_i * i_data[id];
               tempi = Wjk_r * i_data[id] + Wjk_i * r_data[id];
               // Zid = Zi -C
               r_data[id] = r_data[k] - tempr;
               i_data[id] = i_data[k] - tempi;
               r_data[k] += tempr;
               i_data[k] += tempi;
            }
            // (eq 6.23) and (eq 6.24)
            wtemp = Wjk_r;
            Wjk_r = Wj_r * Wjk_r  - Wj_i * Wjk_i;
            Wjk_i = Wj_r * Wjk_i  + Wj_i * wtemp;
         }
      }
   }

   public double[][] getRedReal() {

      return(cR_r);
   }


   public double[][] getRedImaginary() {

      return(cR_i);
   }


   /** The two arrays must be the same size  */

   private void copy2dArray(double[][] dst, double[][] src){

      for(int i=0; i<height; i++){
         for(int j=0; j<width; j++){
            dst[i][j] = src[i][j];
         }
      }
   }


private void copydoubleToComplex(double[][] dst_r, double[][] dst_i, double[] imageData) {

      int k = 0;
      double alternateSign = 1;
      for(int i=0; i<height; i++) {
         for(int j=0; j<width; j++){
            // Calculate (-1)**(i+j)
            alternateSign = ((i+j)%2 == 0) ? -1 : 1;
            // 1. Put short image array into a complex pair,
            // (-1)**(i+j) is to put the zero frequency in the
            // center of the image when it is displayed.
            // 2. We also take this opportunity to scale the input
            // by N (width * height).
            dst_r[i][j] = (double)(imageData[k++] * alternateSign / N);
            dst_i[i][j] = (double)0.0;
         }
      }
   }



   private void copyShortToComplex(double[][] dst_r, double[][] dst_i, short[] imageData){

      int k = 0;
      double alternateSign = 1;
      for(int i=0; i<height; i++){
         for(int j=0; j<width; j++){
            // Calculate (-1)**(i+j)
            alternateSign = ((i+j)%2 == 0) ? -1 : 1;
            // 1. Put short image array into a complex pair,
            // (-1)**(i+j) is to put the zero frequency in the
            // center of the image when it is displayed.
            // 2. We also take this opportunity to scale the input
            // by N (width * height).
            dst_r[i][j] = (double)(imageData[k++] * alternateSign / N);
            dst_i[i][j] = (double)0.0;
         }
      }
   }


   private double[][] Rotate90(double[][] in){

      double[][] out = new double[height][width];
      for(int i=0; i<height; i++){
         for(int j=0; j<width; j++){
            out[i][j] = in[height-j-1][i];
         }
      }
      return(out);
   }



   private double[] flatten(double[][] in){

      double[] out = new double[height*width];
      for (int i=0; i<height; i++){
        for (int j=0; j<width; j++){
            out[i*height+j] = in[i][j];
        }
      }
      return(out);
   }



   private double[][] raise(double[] in){
        double[][] out = new double[height][width];
        for (int i=0; i<height; i++){
            for (int j=0; j<width; j++){
                out[i][j] = in[i*height+j];
            }
        }
        return(out);
   }



   private void rotateInPlace180(double[][] in){
      double temp;
      for(int i=0; i<height/2; i++){
         for(int j=0; j<width; j++){
            temp = in[i][j];
            in[i][j] = in[height-i-1][width-j-1];
            in[height-i-1][width-j-1] = temp;
         }
      }
   }

   private double[] copyRealTodouble(double[][] in_r){
      double[] f_data = new double[N];
      int k = 0;
      for(int i=0; i<height; i++){
         for(int j=0; j<width; j++){
            f_data[k++] = in_r[i][j];
         }
      }
      return(f_data);
   }


   private double[] magnitudeSpectrum(double[][] in_r, double[][] in_i){
      double[] mag = new double[N];
      int k = 0;
      for(int i=0; i<height; i++){
         for(int j=0; j<width; j++){
            mag[k] = (double)Math.sqrt(in_r[i][j] * in_r[i][j] +
               in_i[i][j] * in_i[i][j]);
            // Since we're iterating through the loop anyway, see what min is.
            if (minPSD > mag[k])
               minPSD = mag[k];
            if (maxPSD < mag[k])
               maxPSD = mag[k];
            k++;
         }
      }
      return(mag);
   }


   public double[] magnitudeSpectrum(double[] in_r, double[] in_i){

      N = in_r.length;
      double[] mag = new double[N];
      for(int i=0; i<N; i++){
         mag[i] = (double)Math.sqrt(in_r[i] * in_r[i] + in_i[i] * in_i[i]);
         // Since we're iterating through the loop anyway,
         // find what min is.
         if (minPSD > mag[i]) {
            minPSD = mag[i];
         }
      }
      return(mag);
   }
   
}

