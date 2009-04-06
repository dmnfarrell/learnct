package myctapp;


import java.awt.image.*;
import java.io.*;
import com.sun.image.codec.jpeg.*;
import javax.imageio.*;
import javax.imageio.stream.*;

/*@author     Damien Farrell
 *@created    February 2004 */
 /**Contains methods for general utility purposes, such as operations on 
  *integer and double arrays */

public class Utils{
       
    /**method to blank a 2D array of doubles */
    public static void blank(double data[][], double value) {     
        for ( int i = 0; i < data.length; i++ ) {
            for ( int j = 0; j < data[0].length; j++ ) {
                data[i][j] = value;
            }
        }
    }
    
    
   /**method to find the minimum value in a 1D array of doubles */
    public static double getMin(double data[]) {
		
	double min = data[0];
        for ( int i = 0; i < data.length; i++ ) {
	      if (data[i] < min) min = data[i] ;
        }
	return min;
    }
    
    /**method to find the maximum value in a 1D array of doubles */
    public static double getMax(double data[]) {
        
        double max = data[0];
        for ( int i = 0; i < data.length; i++ ) {
            if (data[i] > max) max = data[i] ;
        }
        return max;
    }
    
    /**method to find the minimum value in a 1D array of int */
    public static int getMin(int data[]) {
		
	int min = data[0];
        for ( int i = 0; i < data.length; i++ ) {
	      if (data[i] < min) min = data[i] ;
        }
	return min;
    }
    
    /**method to find the maximum value in a 1D array of integers */
    public static int getMax(int data[]) {
        
        int max = data[0];
        for ( int i = 0; i < data.length; i++ ) {
            if (data[i] > max) max = data[i] ;
        }
        return max;
    }
    
    /**method to find the minimum value in a 2D array of integers */
    public static int getMin(int data[][]) {
        
        int min = data[0][0];
        for ( int i = 0; i < data.length; i++ ) {
            for ( int j = 0; j < data[0].length; j++ ) {
                if (data[i][j] < min) min = data[i][j] ;
            }
        }
        return min;
    }
    
    /**method to find the maximum value in a 2D array of integers */
    public static int getMax(int data[][]) {
        
        int max = data[0][0];
        for ( int i = 0; i < data.length; i++ ) {
            for ( int j = 0; j < data[0].length; j++ ) {
                if (data[i][j] > max) max = data[i][j] ;
            }
        }
        return max;
    }
    
    /**method to find the minimum value in a 2D array of doubles */
    public static double getMin(double data[][]) {
        
        double min = data[0][0];
        for ( int i = 0; i < data.length; i++ ) {
            for ( int j = 0; j < data[0].length; j++ ) {
                if (data[i][j] < min) min = data[i][j] ;
            }//System.out.println(min);
        }
        return min;
    }
    
    /**method to find the maximum value in a 2D array of doubles */
    public static double getMax(double data[][]) {
        
        double max = data[0][0];
        for ( int i = 0; i < data.length; i++ ) {
            for ( int j = 0; j < data[0].length; j++ ) {
                if (data[i][j] > max) max = data[i][j] ;
            } //System.out.println(max);
        }
        return max;
    }
    
   /**outputs the input array for debugging puroses*/
    public static void outputintArray(int data[][]) {

        
        for ( int i = 0; i < data.length; i++ ) {
            for ( int j = 0; j < data[0].length; j++ ) {
                System.out.print(data[i][j]+" ");
            } System.out.println();
        } System.out.println();
    }
    
   /**outputs the input array for debugging puroses*/
    public static void outputdoubleArray(double data[][]) {
 
        for ( int i = 0; i < data.length; i++ ) {
            for ( int j = 0; j < data[0].length; j++ ) {
                System.out.print(data[i][j]+" ");
            } System.out.println();
        } System.out.println();
    }
    
    /**outputs a given section of the input array for debugging puroses*/
    public static void outputintArrayValues(int data[][], int size, int x, int y) {

        if (x+size>data.length) x = data.length - size;
        if (y+size>data[0].length) y = data[0].length - size;
        
        for ( int i = x; i < x+size; i++ ) {
            for ( int j = y; j < y+size; j++ ) {
                System.out.print(data[i][j]+" ");
            } System.out.println();
        } System.out.println();
        
    }
    
    /**muliplies together two 2D arrays of type double and returns the result*/
    public static double[][] multiply2Darrays(double data1[][], double data2[][]) {
        
        double[][] result = new double [data1.length][data1[0].length];
        for ( int i = 0; i < result.length; i++ ) {
            for ( int j = 0; j < result[0].length; j++ ) {
                result[i][j] = data1[i][j] * data2[i][j] ;
            } 
        }
        return result;
    }
    
    /**muliplies together two 2D arrays of type int and returns the result*/
    public static int[][] multiply2Dintarrays(int data1[][], int data2[][]) {
        
        int[][] result = new int [data1.length][data1[0].length];
        for ( int i = 0; i < result.length; i++ ) {
            for ( int j = 0; j < result[0].length; j++ ) {
                result[i][j] = data1[i][j] * data2[i][j] ;
            } 
        }
        return result;
    } 
    
    /**muliplies together double and int 2D arrays and returns the result*/
    public static double[][]  multiplyIntDoubleArrays(int data1[][], double data2[][]) {
        
        double[][] result = new double [data1.length][data1[0].length];
        for ( int i = 0; i < result.length; i++ ) {
            for ( int j = 0; j < result[0].length; j++ ) {
                result[i][j] = (double) data1[i][j] * data2[i][j] ;
            } 
        }
        return result;
    }
    
    /**divides together two 2D arrays of type double and returns the result*/
    public static double[][] divide2Darrays(double data1[][], double data2[][]) {
        
        double[][] result = new double [data1.length][data1[0].length];
        for ( int i = 0; i < result.length; i++ ) {
            for ( int j = 0; j < result[0].length; j++ ) {
                if (data1[i][j] == 0) result[i][j]=0; else 
                result[i][j] = data1[i][j] / data2[i][j] ;
            } 
        }
        return result;
    } 
    
    /**divides together two 2D arrays of type double and returns the result*/
    public static double[][] divide2Dintarrays(int data1[][], int data2[][]) {
        
        double[][] result = new double [data1.length][data1[0].length];
        for ( int i = 0; i < result.length; i++ ) {
            for ( int j = 0; j < result[0].length; j++ ) {
                if (data2[i][j] == 0){
                    result[i][j] = 0;
                } else{  result[i][j] = data1[i][j] / data2[i][j] ;  }
            } 
        }
        return result;
    } 
      
    
   /**divides two 2D arrays, of type double and int and returns the result*/
    public static double[][] divideIntDoubleArrays(int data1[][], double data2[][]) {
        
        double[][] result = new double [data1.length][data1[0].length];
        for ( int i = 0; i < result.length; i++ ) {
            for ( int j = 0; j < result[0].length; j++ ) {
                result[i][j] = (double) data1[i][j] / data2[i][j] ;
            } 
        }
        return result;
    }
    
    public static double[][] subtract2DArrays(double data1[][], double data2[][]) {
        
        double[][] result = new double [data1.length][data1[0].length];
        for ( int i = 0; i < result.length; i++ ) {
            for ( int j = 0; j < result[0].length; j++ ) {
                result[i][j] = data1[i][j] - data2[i][j] ;
            } 
        }
        return result;
    }
    
     public static int[][] subtract2DintArrays(int data1[][], int data2[][]) {
        
        int[][] result = new int [data1.length][data1[0].length];
        for ( int i = 0; i < result.length; i++ ) {
            for ( int j = 0; j < result[0].length; j++ ) {
                result[i][j] = data1[i][j] - data2[i][j] ;
            } 
        }
        return result;
    }       
     
     public static double[][] add2DArrays(double data1[][], double data2[][]) {
        
        double[][] result = new double [data1.length][data1[0].length];
        for ( int i = 0; i < result.length; i++ ) {
            for ( int j = 0; j < result[0].length; j++ ) {
                result[i][j] = data1[i][j] + data2[i][j] ;
            } 
        }
        return result;
    }
    
     public static int[][] add2DintArrays(int data1[][], int data2[][]) {
        
        int[][] result = new int [data1.length][data1[0].length];
        for ( int i = 0; i < result.length; i++ ) {
            for ( int j = 0; j < result[0].length; j++ ) {
                result[i][j] = data1[i][j] + data2[i][j] ;
            } 
        }
        return result;
    }  
     
  
     public static int[][] setRange2DintArray(int data[][], int min, int max) {
        
        int[][] result = new int [data.length][data[0].length];
        for ( int i = 0; i < result.length; i++ ) {
            for ( int j = 0; j < result[0].length; j++ ) {
                if (data[i][j] > max){
                    result[i][j] = max ;
                }
                else if (data[i][j] < min){
                     result[i][j] = min ;
                } else { result [i][j] = data[i][j];}
            } 
        }
        return result;
    }    
     
     public static void normalize2DArray(double data[][], double min, double max) {
         
         double datamax = getMax(data);
         //System.out.println("MAX:"+datamax);
         zeronegvals2DArray(data);
         double datamin = 0;
         
         for ( int i = 0; i < data.length; i++ ) {
             for ( int j = 0; j < data[0].length; j++ ) {
                 data[i][j] = (double) (((data[i][j]-datamin) * (max))/datamax);
                 
             }
         }
     }
     
      public static void normalize2DArray(double data[][], double min, double max, boolean neg) {
         
         double datamax = getMax(data);
         double datamin;
         System.out.println("MAX:"+datamax);
         if (neg == true) { //deal with negative values
             datamin = getMin(data);
             
             for ( int i = 0; i < data.length; i++ ) {
                 for ( int j = 0; j < data[0].length; j++ ) {
                     data[i][j] += datamin; 
                     
                 }
             }
             datamin = 0;
         }
         else{ 
             zeronegvals2DArray(data);  //ignore negative vals
             datamin = 0;

         }
         for ( int i = 0; i < data.length; i++ ) {
                 for ( int j = 0; j < data[0].length; j++ ) {
                     data[i][j] = (double) (((data[i][j]-datamin) * (max))/datamax);

                 }
             }
     }

     public static void normalize1DArray(double data[], double min, double max) {
         
         double datamax = getMax(data);
         //System.out.println("MAX:"+datamax);
         double datamin = 0;
         
         for ( int i = 0; i < data.length; i++ ) {
             data[i] = (double) (((data[i]-datamin) * (max))/datamax);
         }
     }
     
    public static void zeronegvals2DArray(double data[][]) {
         
         double datamin = getMin(data);

         if (datamin < 0 ){
             for ( int i = 0; i < data.length; i++ ) {
                 for ( int j = 0; j < data[0].length; j++ ) {
                     if (data[i][j] < 0 ){
                         data[i][j] = 0;    
                     }
                 }
             }
         }
     }

     public static double getAverage1DArray(double data[]) {
         
         double average, sum=0;
         
         for ( int i = 0; i < data.length; i++ ) {
             sum += data[i];
         }
         average = sum /data.length;
         return average;
     }
         
    public static double getAverage2DIntArray(int[][] data){
       int sum = 0;
       double result;
       
       for ( int i = 0; i < data.length; i++ ) {
            for ( int j = 0; j < data[0].length; j++ ) {
                sum += data[i][j];
            } 
       }
      result = sum / (data.length * data[0].length);
      return result;

    }
    
    public static double getAverage2DDoubleArray(double[][] data){
       double sum = 0;
       double result;
       
       for ( int i = 0; i < data.length; i++ ) {
            for ( int j = 0; j < data[0].length; j++ ) {
                sum += data[i][j];
            } 
       }
      result = sum / (data.length * data[0].length);
      return result;

    }
    
    public static double getMSE2DintArrays(int data1[][], int data2[][]) {
        
        double sum=0; double mse=0;
        int[][] result = new int [data1.length][data1[0].length];
        for ( int i = 0; i < result.length; i++ ) {
            for ( int j = 0; j < result[0].length; j++ ) {
                result[i][j] = Math.abs(data1[i][j] - data2[i][j]);
                //System.out.print(result[i][j]+" ");
                sum += Math.pow(result[i][j],2);
            }  //System.out.println();
        }
        mse = (double)sum / (result.length*result.length);
        sum =0;
        return mse;
    }
    
     public static double getPSNR2DintArrays(int data1[][], int data2[][]) {
        
        double psnr=0; double mse=0;

        mse = getMSE2DintArrays(data1, data2);
        double temp = 255 / Math.sqrt(mse);
        psnr =  20 * (Math.log (temp)/ Math.log(10));
        return psnr; 

    }
    
    /**Creates an image from a 1D array of doubles
    *@param pixels  the input double array
    *@return    the 8 bit BufferedImage
    */
     public static BufferedImage CreateImagefrom1DintArray(int[] pixels, int iw, int ih){
        
        int i,j;
        i=0; j=0;
        short[] pixelshortArray = new short[iw*ih];
        int max = getMax(pixels);
        int min = getMin(pixels);
        for ( int x = 0; x < pixels.length; x++ ) {
            
           // if (pixels[x] < 0){
              pixelshortArray[x] += min; 
              if (max == 0){
                  pixelshortArray[x] = (short)(pixels[x]*255);
              } else 
              pixelshortArray[x] = (short)((pixels[x]*255)/(max));
            //System.out.print(pixelshortArray[x]);
            //System.out.print(" ");
        }

        // returns an 8-bit buffered image for display purposes
        BufferedImage bpimg = getGrayBufferedImage(pixelshortArray, iw, ih, 0, 255);
        
        return bpimg;
     }
     
     public static void toGrayBytes(byte[] grayBytes, short[] shortBuffer, int imgMin, int imgMax) {
         final int displayMin = 0; // Black is zero
         final int displayMax = 255; //(2 << 7) - 1; // For 8 bits per sample (255)
         final float displayRatio = (float) (displayMax - displayMin) / (imgMax - imgMin);
         
         for (int i = 0; i < shortBuffer.length; ++i) {
             int in = shortBuffer[i];
             int out;
             if (in < imgMin)
                 out = displayMin;
             else if (in > imgMax)
                 out = displayMax;
             else
                 out = (int) ((in - imgMin)* displayRatio);
             grayBytes[i] = (byte) out;
         }
     }
     
     public static BufferedImage getGrayBufferedImage(short[]buf, int width, int height, int imgMin, int imgMax) {
         if (buf.length != width*height)
             throw new IllegalArgumentException(width + " * " + height + " != " + buf.length);
         BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
         byte[] data = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
         toGrayBytes(data, buf, imgMin, imgMax);
         return image;
     }
     
     // method for quickly finding the standard deviation for a 2D array of ints
     public static double getStDev2DIntArray( int[][] data ) {
         // sd is sqrt of sum of (values-mean) squared divided by n - 1
         // Calculate the mean
         double mean = 0;
         final int n = data.length * data[0].length;
         if ( n < 2 )
             return Double. NaN;
         for ( int i=0 ; i < data.length ; i++ ) {
             for ( int j = 0; j < data[0].length; j++ ) {
                 mean += data [i][j] ;
             }
         }
         mean /= n ;
         // calculate the sum of squares
         double sum = 0;
         for ( int i=0 ; i < data.length ; i++ ) {
             for ( int j = 0; j < data[0].length; j++ ) {
                 final double v = data[i][j] - mean;
                 sum += v *v;
             }
         }
         return Math.sqrt(sum / ( n - 1 ));
     }
     
     // method for quickly finding the standard deviation for a 2D array of ints
     public static double getStDev2DDoubleArray( double[][] data ) {
         // sd is sqrt of sum of (values-mean) squared divided by n - 1
         // Calculate the mean
         double mean = 0;
         final int n = data.length * data[0].length;
         if ( n < 2 )
             return Double. NaN;
         for ( int i=0 ; i < data.length ; i++ ) {
             for ( int j = 0; j < data[0].length; j++ ) {
                 mean += data [i][j] ;
             }
         }
         mean /= n ;
         // calculate the sum of squares
         double sum = 0;
         for ( int i=0 ; i < data.length ; i++ ) {
             for ( int j = 0; j < data[0].length; j++ ) {
                 final double v = data[i][j] - mean;
                 sum += v *v;
             }
         }
         return Math.sqrt(sum / ( n - 1 ));
     }
     
     // method for quickly finding the standard deviation for a 1D array of doubles
     public static double stdevFast( double[] data ) {
         // sd is sqrt of sum of (values-mean) squared divided by n - 1
         // Calculate the mean
         double mean = 0;
         final int n = data.length;
         if ( n < 2 )
             return Double. NaN;
         for ( int i=0 ; i<n ; i++ ) {
             mean += data [ i ] ;
         }
         mean /= n ;
         // calculate the sum of squares
         double sum = 0;
         for ( int i=0 ; i<n ; i++ ) {
             final double v = data[ i ]- mean;
             sum += v *v;
         }
         return Math.sqrt(sum / ( n - 1 ));
     }
     
     /**rounds a double down to the specified number of decimal places*/
     public static double round(double a, int b) {
         double tempA = a * Math.pow(10, b);
         long tempB = (long) tempA;
         int diff = (int) (10 * (tempA - tempB));
         if (diff >= 5) {
             ++tempA;
             tempB = (long) tempA;
         }
         tempA = tempB / Math.pow(10, b);
         return tempA;
     }
     
     public static int pow2(int power) {
         return (1 << power);
     }
     
     /* Is value<a power of 2? */
     
     public static boolean isPow2(int value) {
         return (value == (int)roundPow2(value));
     }
     
     public static double roundPow2(double value) {
         double power = (double)(Math.log(value) / Math.log(2));
         int intPower = (int)Math.round(power);
         return (double)(pow2(intPower));
     }
     
     // factorial of n
     // Argument is of type double but must be, numerically, an integer
     // factorial returned as double but is, numerically, should be an integer
     // numerical rounding may makes this an approximation after n = 21
     public static double factorial(double n){
         if(n<0 || (n-(int)n)!=0)throw new IllegalArgumentException("\nn must be a positive integer\nIs a Gamma funtion [Fmath.gamma(x)] more appropriate?");
         double f = 1.0D;
         int nn = (int)n;
         for(int i=1; i<=nn; i++)f*=i;
         return f;
     }

     
     public static double[] importArrayData(String filename){
         
         double[] data; int size = 0; int val1;
         File file;
         BufferedReader reader;
         data = new double[size];
         
         try{
             if (filename == null){
                 size = 10;
                 data = new double[size];
             }
             else{
                 file = new File(filename);
                 reader = new BufferedReader(new FileReader(file));
                 while (reader.readLine()!= null){
                     size++;
                 } System.out.println(size);
                 data = new double[size];
                 reader.close();
                 reader = new BufferedReader(new FileReader(file));
                 int i = 0; String s;
                 while ((s = reader.readLine())!= null){
                     
                     String[] sdata = s.split("\\s");        //split string into elements
                     data[i] = Float.valueOf(sdata[1]).intValue();
                     System.out.println((i+1)+"  "+data[i]);
                     
                     i++;
                 }
                 reader.close();
             }
         } catch (IOException ioe) { System.out.println("I/O Exception "+ioe); }
         
         return data;
         
     }
     
      public static void addpoissonnoise(double[][] data, double rate){
      
      double noise = 0;
      int i,j=0;
      int w = data.length;
      int h = data[0].length;
      Poisson p = new Poisson("p", rate );
      double max = getMax(data);
      double counts = rate;
      double time = 100;

          for (i=0;i<w;i++){
              for (j=0;j<h;j++){
                  if (data[i][j] == 0)  { noise = 0; }
                  else {
                       //each value is given a count rate as a percentage of the max = rate
                      counts = rate * data[i][j]/max;  
                      noise = (data[i][j] * (rate - p.sample())/rate);
                      noise = noise / time;
                  }
                  if (data[i][j] != 0) {
                      
                      //System.out.print(data[i][j]+" ");
                      //System.out.println(noise);
                  }
                  
                  data[i][j] += noise; //System.out.println(projection[i][S]+" ");
              }  
          } 
      }
     
      public static double[][] ArrayCopy(double[][] data ){
          
          int i,j=0;
          int w = data.length;
          int h = data[0].length;
          
          double[][] result = new double[w][h];
          
          for (i=0;i<w;i++){
              for (j=0;j<h;j++){
                    result[i][j] = data[i][j];
              }
          }
          return result;
      }
      
  /**static method that saves any image to a file*/

    public static void saveanImage(String fileName, BufferedImage img) {
       try {
          ImageWriter writer = (ImageWriter)ImageIO.getImageWritersByFormatName("png").next();
          ImageWriteParam param = writer.getDefaultWriteParam();
          ImageTypeSpecifier imTy = param.getDestinationType();
          ImageTypeSpecifier imTySp =
          ImageTypeSpecifier.createFromRenderedImage(img);
          param.setDestinationType(imTySp);
          System.out.println("Found writer " + writer);
          File file = new File(fileName+".png");
          ImageOutputStream ios = new FileImageOutputStream(file);
	  writer.setOutput(ios);
          writer.write(img);     
  
       } catch (IOException ioe) { ioe.printStackTrace(); }
    } 
    
      
}