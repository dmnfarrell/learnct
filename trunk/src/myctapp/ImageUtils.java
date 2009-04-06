package myctapp;

import ij.*;
import ij.process.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.image.*;
import java.awt.color.ColorSpace;
import java.util.Iterator;
import java.io.*;

/**
 *
 * @author damien farrell
 * carries out various utility functions for image data, uses the imagej libraries
 */

public class ImageUtils {
    
    public ImageUtils() {
        
    }
    
       
    public static double[][] getImagePlusPixels(ImagePlus imp){
       //imp = IJ.getImage();
       int type = imp.getType();
       int w=0, h=0;
        ImageProcessor ip = imp.getProcessor();
        int stacksize = imp.getStackSize();
        ByteProcessor bip; ShortProcessor sip;
        byte[] bpixels = new byte[w*h]; short[] spixels = new short[w*h];
        
        if (imp.getType() == ImagePlus.GRAY8){
            //bip= (ByteProcessor) imp.getProcessor();
            bip = (ByteProcessor) ip;
            bpixels = (byte[]) bip.getPixels();
            w= bip.getWidth(); h = bip.getHeight();
        } else if (imp.getType() == ImagePlus.GRAY16){
            //sip = (ShortProcessor) imp.getProcessor();
            sip = (ShortProcessor) ip;
            spixels= (short[]) sip.getPixels();
            w = sip.getWidth(); h = sip.getHeight();
        } else if (imp.getType() == ImagePlus.COLOR_256){
            ImageConverter conv = new ImageConverter(imp);
            conv.setDoScaling(true);
            conv.convertToGray8();
            bip= (ByteProcessor) imp.getProcessor();
            bpixels =(byte[]) bip.getPixels();
            w= bip.getWidth(); h = bip.getHeight();
        } else if (imp.getType() == ImagePlus.COLOR_RGB){
            //new ImageConverter(imp).convertToGray8();
            //ImageProcessor ip = imp.getProcessor();
            ImageProcessor cp = new ColorProcessor(imp.getImage());
            imp.setProcessor(null, cp.convertToByte(true));
            bip= (ByteProcessor) imp.getProcessor();
            bpixels = (byte[]) bip.getPixels();
            w= bip.getWidth(); h = bip.getHeight();
        }
        
        //get pixel values from current image
        double[][] pix = new double[w][h];
        if (type == ImagePlus.GRAY8){
            for (int x = 0; x < pix[0].length; x++ ) {
                for (int y = 0; y < pix.length; y++ ) {
                    pix[x][y] = bpixels[y + x * pix.length] &0xFF;
                }
            }
        } else if (type == ImagePlus.GRAY16){
            for (int x = 0; x < pix[0].length; x++ ) {
                for (int y = 0; y < pix.length; y++ ) {
                    pix[x][y] = spixels[y + x * pix.length] &0xFFFF;
                }
            }
        }
        return pix;
    }
        
      /**gets the pixels values from a bufferedimage and returns a 2D integer pixel array*/
   public static int[][] getBuffImagePixels(BufferedImage img){

       final int iw = img.getWidth(null);
       final int ih = img.getHeight(null);
       final int[][] pix = new int[iw][ih];


       if(img.getType() == 11){ //TYPE_USHORT_GRAY
            DataBufferUShort db = (DataBufferUShort)img.getRaster().getDataBuffer();
            short[] pixelarray = db.getData();
            for (int x = 0; x < pix.length; x++ ) {
                for (int y = 0; y < pix[0].length; y++ ) {
                    pix[x][y] = pixelarray[x + y * pix.length] &0xFFFF;
                }
            }
        }
        else if(img.getType() == 1){ //TYPE_INT_RGB
            DataBufferInt db = (DataBufferInt)img.getRaster().getDataBuffer();
            int[] pixelarray = db.getData();
            for (int x = 0; x < pix.length; x++ ) {
                for (int y = 0; y < pix[0].length; y++ ) {
                    pix[x][y] = pixelarray[x + y * pix.length] ;
                }
            }
        }  //else assume 8 bit
        else {
            DataBufferByte db = (DataBufferByte)img.getRaster().getDataBuffer();
            byte[] pixelarray = db.getData();
            for (int x = 0; x < pix.length; x++ ) {
                for (int y = 0; y < pix[0].length; y++ ) {
                    pix[x][y] = pixelarray[x + y * pix.length] &0xFF;
                }
            }
        }
       return pix;
   }

   /**gets the pixels values from the bufferedimage and returns a 2D double pixel array*/
   public static double[][] getdoubleBuffImagePixels(BufferedImage img){
       
       final int iw = img.getWidth(null);
       final int ih = img.getHeight(null);
       final double[][] pix = new double[iw][ih];


       if(img.getType() == 11){
            DataBufferUShort db = (DataBufferUShort)img.getRaster().getDataBuffer();
            short[] pixelarray = db.getData();
            for (int x = 0; x < pix.length; x++ ) {
                for (int y = 0; y < pix[0].length; y++ ) {
                    pix[x][y] = pixelarray[x + y * pix.length] &0xFFFF;
                }
            }
        }
        else if(img.getType() == 1){
            DataBufferInt db = (DataBufferInt)img.getRaster().getDataBuffer();
            int[] pixelarray = db.getData();
            for (int x = 0; x < pix.length; x++ ) {
                for (int y = 0; y < pix[0].length; y++ ) {
                    pix[x][y] = pixelarray[x + y * pix.length] &0xFF;
                }
            }
        }  //else assume 8 bit
        else {
            DataBufferByte db = (DataBufferByte)img.getRaster().getDataBuffer();
            byte[] pixelarray = db.getData();
            for (int x = 0; x < pix.length; x++ ) {
                for (int y = 0; y < pix[0].length; y++ ) {
                    pix[x][y] = pixelarray[x + y * pix.length] &0xFF;
                }
            }
        }
       return pix;
   }
   
   /**Returns the pixel values of the current BufferedImage
     *@return 1D double array consisting of the pixel values of the current image*/
  
   public static double[] getbuffImage1DPixels(BufferedImage img){
       File file;
       
       ColorModel cm = img.getColorModel();
       int bpp = cm.getPixelSize();
       final int iw = img.getWidth(null);
       final int ih = img.getHeight(null);
       final double[] pixels = new double[iw*ih];
       
       DataBufferByte db = (DataBufferByte)img.getRaster().getDataBuffer();
       byte[] pixelarray = db.getData();
       for (int x = 0; x < pixels.length; x++ ) {
           pixels[x] = pixelarray[x] &0xFF;
       }
       
       return pixels;
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
   
   public static BufferedImage create12bitImage(int imageWidth, int imageHeight,
                                                 short data[]){
       
       DataBuffer dbuff =  new DataBufferUShort((short[])data, imageWidth);
       ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
       ColorModel cm = new ComponentColorModel(cs, new int[]{16}, false,
       false,
       Transparency.OPAQUE,
       DataBuffer.TYPE_USHORT);
       int[] band = {0};
       SampleModel sm =
       new PixelInterleavedSampleModel(dbuff.getDataType(), imageWidth, imageHeight, 1, 
                                         imageWidth, band);
       WritableRaster raster =  Raster.createWritableRaster(sm, dbuff, new Point(0,0));
       return new BufferedImage(cm, raster, false, null);
       
   }
   
    /**Creates an imnage from a 2D array of integers*/
    public static BufferedImage CreateImagefromIntArray(int[][] pixels){
        
        int i,j;
        i=0; j=0;
        
        short[] pixelshortArray = new short[pixels.length * pixels[0].length];
        //int[] pixelintArray = new int[pixels.length * pixels[0].length];
        int min = Utils.getMin(pixels);
        int max = Utils.getMax(pixels);
        int gray;
        System.out.println("rescaling output image: ");
            for (int x = 0; x < pixels[0].length; x++ ) {
                for (int y = 0; y < pixels.length; y++ ) {
                gray = pixels[x][y];  
                if (gray < 0){
                    pixelshortArray[x + y * pixels.length] = 0;
                } else{ 
                    pixelshortArray[x + y * pixels.length] = (short) gray;
                }
            }
        }
        // returns an 8-bit buffered image for display purposes
        BufferedImage img = getGrayBufferedImage(pixelshortArray, pixels.length,
        pixels[0].length, 0, 255);
        return img; 
    }
    
   static BufferedImage convert(BufferedImage image, int type) {
       if (image.getType() == type)
           return image;
       BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), type);
       Graphics2D g = result.createGraphics();
       g.drawRenderedImage(image, null);
       g.dispose();
       return result;
   }
    

   public static BufferedImage Scale(ImagePlus ijimg){
       WindowManager.setTempCurrentImage(ijimg);
       IJ.runPlugIn("ij.plugin.filter.Scaler","");
       BufferedImage img = BufferedImageCreator.create(ijimg,0);
       return img;
   }
      
}
