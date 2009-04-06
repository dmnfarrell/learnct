package myctapp;

import ij.*;
import ij.io.*;
import ij.process.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.image.*;
import java.awt.color.ColorSpace;
import java.util.Iterator;
import java.io.*;
import com.sun.image.codec.jpeg.*;
import javax.imageio.*;
import javax.imageio.stream.*;
import java.awt.geom.Ellipse2D;

/**@author     Damien Farrell
 *@created    February 2004 */
/**jpanel class for loading, manipulating and displaying images */

public class ImagePanel extends JPanel {
    private int x, y, x1, y1, x2, y2;
    private Point p1, p2;
    private double imageratio, panelratio, magnification = 1;
    public static String selectiontype;
    /**An jtextarea that can be attached to the main image panel*/
    public ProgressPanel bpprogresspanel;
    /**infopanel for image data*/
    public static InfoPanel imageinfopanel;
    /**This member variable holds the currently displayed image. */
    public BufferedImage mBufferedImage;
    /**stores the current windowed image */
    public BufferedImage windowedImage;
    public ImagePlus ijimage;
    public Object[] imgstack; Object[] datastack;
    public int stacksize;
    
    public int[][] pixels;
    /**stores the actual array values that were used to create the image, to be
     * used for windowing the data and re-displaying the image  */
    //public double[][] data;
    public int lowerwinlvl, upperwinlvl;
    public boolean windowing, closed, finished, displayroicircle = false;
    
    public ImagePanel()  {
        setBackground(Color.white);
        setBorder(BorderFactory.createLineBorder(Color.black));
        windowing = true;
        closed = false;
        lowerwinlvl = 0;
        upperwinlvl = 255;
        imageratio = 1;
        p1 = new Point(0,0); p2 = new Point(0,0);
        pixels = null;
        //data = null;
        addMouseListener(
                // anonymous inner class for mouse pressed and
                // released event handling
                new MouseAdapter() {
            
            // handle mouse press event
            public void mousePressed( MouseEvent event ) {
                x1 = event.getX();
                y1 = event.getY();
                //repaint();
            }
            // handle mouse release event
            public void mouseReleased( MouseEvent event ) {
                x2 = event.getX();
                y2 = event.getY();
                repaint();
            }
        }  // end anonymous inner class
        );
        
        // set up mouse motion listener
        addMouseMotionListener(
                // anonymous inner class to handle mouse drag events
                new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent event )  {
                x = event.getX();
                y = event.getY();
                int pxval = 0; double actualval = 0;
                Point p = new Point(0,0);
                p = getPixelCoords(x, y);
                try {
                    pxval = getPixel(p.x, p.y);  //get pixel value
                    if (pixels != null){
                        actualval = pixels[p.x][p.y]-1000; //pseudo hounsfield number
                    }
                } catch (ArrayIndexOutOfBoundsException ae) {};
                imageinfopanel.updateInfo(x, y, p.x, p.y, pxval, actualval);
                repaint();
            }
            // handle mouse drag event
            public void mouseDragged( MouseEvent event ) {
                x2 = event.getX();
                y2 = event.getY();
                p1 = getPixelCoords(x1, y1); p2 = getPixelCoords(x2, y2);
                imageinfopanel.updateDragInfo(p1.x, p1.y, p2.x, p2.y);
                repaint();
            }
        }  // end anonymous inner class
        ); // end call to addMouseMotionListener
    }
    
    public int getImageType(){
        return mBufferedImage.getType();
    }
    
    public void setMagnification(double magnification) {
        this.magnification = magnification;
    }
    
    public double getMagnification() {
        return magnification;
    }
    
    /**gets panel coords to actual pixel coords scale value*/
    public double getPixelPanelScale(){
        double scale = 1;
        try{
            panelratio = (double)this.getWidth()
            / (double)this.getHeight();
            if (panelratio <= 1){
                scale = (double) this.getWidth() / (double)mBufferedImage.getWidth();
                
            } else {
                scale = (double) this.getHeight() / (double)mBufferedImage.getHeight();
            }
            
            
        } catch (NullPointerException ne) {};
        return scale;
    }
    
    public Point getPixelCoords(int x, int y){
        Point p = new Point(0,0);
        //gets the correct scaling value for jpanel to image pixels
        double s = getPixelPanelScale();
        //get position of actual image pixels, scaled and offset
        try {
            if (panelratio <= 1){
                p.x = (int)((double)x/s) ;
                p.y = (int) (y/s) - ((int)Math.floor(getHeight()/s) -
                        mBufferedImage.getHeight())/2;
            } else {
                p.y = (int)((double)y/s) ;
                p.x = (int) (x/s) - ((int)Math.floor(getWidth()/s) -
                        mBufferedImage.getWidth())/2;
            }
        } catch (NullPointerException ne) {};
        return p;
    }
    
    public int getPixel(int x, int y){
        int val = 0;
        try{
            if (mBufferedImage.getType() == 10){
                
                DataBufferByte db = (DataBufferByte)mBufferedImage.getRaster().getDataBuffer();
                byte[] pixelarray = db.getData();
                val = pixelarray[x + y * mBufferedImage.getWidth()] &0xFF;
                
            } else if(mBufferedImage.getType() == 11){
                
                DataBufferUShort db = (DataBufferUShort)mBufferedImage.getRaster().getDataBuffer();
                short[] pixelarray = db.getData();
                val = pixelarray[x + y * mBufferedImage.getWidth()] &0xFFFF;
            }
        } catch(NullPointerException ne) {};
        
        return val;
        
    }
    
    public int[][] getSelectionPixels(){
        
        BufferedImage subimg = getImageSelection();
        final int iw = subimg.getWidth(null);
        final int ih = subimg.getHeight(null);
        int[][] pix = new int[iw][ih];
        try{
            if (mBufferedImage.getType() == 10){
                DataBufferByte db = (DataBufferByte)subimg.getRaster().getDataBuffer();
                byte[] pixelarray = db.getData();
                //System.out.println(pix[0].length * pix.length);
                //System.out.println(pixelarray.length);
                for (int x = 0; x < iw; x++ ) {
                    for (int y = 0; y < ih; y++ ) {
                        pix[x][y] = pixelarray[x + y * iw] &0xFF;
                    }
                }
            }
            
            else if(mBufferedImage.getType() == 11){
                
                DataBufferUShort db = (DataBufferUShort)mBufferedImage.getRaster().getDataBuffer();
                short[] pixelarray = db.getData();
                
                for (int x = 0; x < iw; x++ ) {
                    for (int y = 0; y < ih; y++ ) {
                        pix[x][y] = pixelarray[x + y * iw] &0xFFFF;
                    }
                }
            }
        } catch (NullPointerException ne) {};
        return  pix;
    }
    
    /**returns a bufferedimage consisting of the current selection. This is done by
     *copying the result of getsubimage into a new bufferedimage and returning this */
    public BufferedImage getImageSelection(){
        int w = Math.abs(p1.x-p2.x);
        int h = Math.abs(p1.y-p2.y);
        BufferedImage newimg;
        BufferedImage img = mBufferedImage.getSubimage
                (Math.min(p1.x, p2.x), Math.min(p1.y, p2.y), w, h);
        //int numcomp = img.getColorModel().getNumColorComponents();
        if (mBufferedImage.getType() == 10) {
            newimg = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        } else if (mBufferedImage.getType() == 11) {
            newimg = new BufferedImage(w, h, BufferedImage.TYPE_USHORT_GRAY);
            //DataBufferUShort db = (DataBufferUShort)img.getRaster().getDataBuffer();
            //short[] pixelarray = db.getData();
            //newimg = ImageUtils.create12bitImage(w, h, pixelarray);
        } else {
            newimg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        }
        newimg.createGraphics().drawImage(img, 0, 0, null);
        return newimg;
        
    }
    
    public double[][] getSelectionData(){
        int w = Math.abs(p1.x-p2.x);
        int h = Math.abs(p1.y-p2.y);
        x1 = Math.min(p1.x, p2.x);
        y1 = Math.min(p1.y, p2.y);
        double[][] sdata = new double[w][h];
        try{
            for (int x = 0; x < w; x++ ) {
                for (int y = 0; y < h; y++ ) {
                    sdata[x][y] = this.pixels[x+x1][y+y1];
                }
            }
        } catch (NullPointerException ne) {};
        return sdata;
    }
    
    public void setPixelData(){
        setPixelData(mBufferedImage);
    }
    
    public BufferedImage setPixelData(BufferedImage img){
        pixels = ImageUtils.getBuffImagePixels(img);
        if (img.getType() == 11){
            upperwinlvl = 2000;
        } else{
            upperwinlvl = 255;
        }
        return img;
    }
    
    
    private void zoomImage(){
        
    }
    
    public void loadBufferedImage(BufferedImage img){
        mBufferedImage = img;
        setPixelData();
        ijimage = createImagePlus(img);
        PerformWindowing();
        repaint();
    }
    
    public void loadImagePlus(ImagePlus img){
        ijimage = img;
        mBufferedImage = BufferedImageCreator.create(img,0);
        setPixelData();
        PerformWindowing();
        repaint();
        
    }
    
    public ImagePlus createImagePlus(BufferedImage img){
        
        int w = img.getWidth();
        int h = img.getHeight();
        ImageProcessor ip = new ShortProcessor(w,h);
        ImagePlus imp = new ImagePlus("temp", ip);
        short[] sarray= new short[w*h];
        
        for (int i = 0; i < w; i++ ) {
            for (int j = 0; j < h; j++ ) {
                sarray[i + j * w] = (short) pixels[i][j];
            }
        }
        ip.setPixels(sarray);
        
        return imp;
        
    }
    
    public void saveImagePlus() {
        ijimage = createImagePlus(mBufferedImage);
        FileSaver fs = new FileSaver(ijimage);
        fs.save();
    }
    
    
    /**saves the current bufferedimage to a file*/
    
    public void saveImage(String fileName) {
        try {
            javax.imageio.ImageWriter writer = (javax.imageio.ImageWriter)
            ImageIO.getImageWritersByFormatName("png").next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            ImageTypeSpecifier imTy = param.getDestinationType();
            ImageTypeSpecifier imTySp =
                    ImageTypeSpecifier.createFromRenderedImage(mBufferedImage);
            param.setDestinationType(imTySp);
            System.out.println("Found writer " + writer);
            File file = new File(fileName+".png");
            ImageOutputStream ios = new FileImageOutputStream(file);
            writer.setOutput(ios);
            writer.write( mBufferedImage);
            
        } catch (IOException ioe) { ioe.printStackTrace(); }
    }
    
    
    public void importIJStack(ImagePlus img){
        stacksize = img.getStackSize();
        int stsize = stacksize; System.out.println(stsize);
        imgstack = new Object[stsize];
        datastack = new Object[stsize];
        for (int st=0;st<stsize;st++) {
            BufferedImage buffimg = BufferedImageCreator.create(img,st);
            imgstack[st] = buffimg;
            
        }
        windowing = false;
        getCurrentImageinStack(0);
    }
    
    public void getCurrentImageinStack(int st){
        mBufferedImage = (BufferedImage) imgstack[st];
        if (windowing == true){
            setPixelData();
            PerformWindowing();
        }
        repaint();
    }
    
    public void setStackSize(int st){
        this.stacksize = st;
    }
    
    public int getStackSize(){
        return this.stacksize;
    }
    
    public BufferedImage getBufferedImage(){
        return mBufferedImage;
    }
    
    public void ApplyChanges(){
        mBufferedImage = windowedImage;
        setPixelData(windowedImage);
    }
    
    public int getImageBitDepth(){
        ColorModel cm = mBufferedImage.getColorModel();
        int bpp = cm.getPixelSize();
        return bpp;
    }
    
    /**loads an image from file and puts it into mBufferedImage */
    public void loadImage(String fileName) {
        
        try {
            File file = new File(fileName);
            mBufferedImage = ImageIO.read(file);
            ColorModel cm = mBufferedImage.getColorModel();
            int bpp = cm.getPixelSize();
            String formatName = getFormatName(file);
            System.out.println("Image is of type "+formatName);
            System.out.println("Depth is "+bpp+" bits");
            System.out.println("Size: "+mBufferedImage.getWidth()+"x"+mBufferedImage.getHeight());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        //mBufferedImage = new BufferedImage( image.getWidth(null), image.getHeight(null),
        //				   BufferedImage.TYPE_BYTE_GRAY);
        PerformWindowing();
        repaint();
    }
    
    
    /**loads a sinogram image into the panel*/
    public void loadSinogram(final CTScanner ctscanner) {
        
        final SwingWorker worker = new SwingWorker() {
            public Object construct() {
                mBufferedImage  = ctscanner.CreateSinogram(null);
                return mBufferedImage;
            }
            
            //Runs on the event-dispatching thread.
            public void finished() {
                setPixelData();
                PerformWindowing();
                repaint();
            }
        };
        worker.start();
    }
    
    /**loads a sinogram image into the panel from a file*/
    public void loadSinogram(final CTScanner ctscanner, final String path) {
        
        final SwingWorker worker = new SwingWorker() {
            public Object construct() {
                mBufferedImage  = ctscanner.CreateSinogram(path);
                return mBufferedImage;
            }
            
            //Runs on the event-dispatching thread.
            public void finished() {
                setPixelData();
                PerformWindowing();
                repaint();
            }
        };
        worker.start();
    }
    
    public final static int ONE_SECOND = 1000;
    long dt = 0; long startTime = 0;
 /*  Timer timer = new Timer(ONE_SECOND, new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
            progressBar.setValue(task.getCurrent());
            String s = task.getMessage();
            if (s != null) {
                taskOutput.append(s + newline);
                taskOutput.setCaretPosition(
                taskOutput.getDocument().getLength());
            }
            if (task.isDone()) {
                Toolkit.getDefaultToolkit().beep();
                timer.stop();
                startButton.setEnabled(true);
                setCursor(null); //turn off the wait cursor
                progressBar.setValue(progressBar.getMinimum());
            }
        }
    }); */
    
    /**loads a reconstructed image into the current bufferedimage and displays*/
    public void loadBPImage(final CTScanner ctscanner) {
        bpprogresspanel = new ProgressPanel();
        final SwingWorker worker = new SwingWorker() {
            public Object construct() {
                
                startTime = System.currentTimeMillis();
                if (ctscanner.method == "fbp"){
                    bpprogresspanel.append("Performing back projection.. ");
                    if (ctscanner.filtering == true){
                        bpprogresspanel.append("Using "+ctscanner.filtername+ " Filter \n");
                        if (ctscanner.filtername == "hamming" || ctscanner.filtername == "hann"  ){
                            bpprogresspanel.append("Cutoff at "+ctscanner.filtercutoff+ "\n");
                        }
                    } if (ctscanner.truncate == true){
                        bpprogresspanel.append("Using truncated data at "
                                +ctscanner.truncatewidth+ " \n");
                    }
                } else {
                    bpprogresspanel.append("Performing iterative reconstruction.. ");
                    bpprogresspanel.append(ctscanner.numiter+" iterations \n");
                }
                
                /**this section is for animating the back projection in the panel */
                if (ctscanner.animate == true){
                 /*if (ctscanner.truncate == true && ctscanner.truncmethod == "simple extrap"){
                     double t = ctscanner.truncatewidth;
                     ctscanner.fixtruncatedProjections(t);
                 } */
                    float phi; float stepsize = ctscanner.stepsize;
                    int ang1 = ctscanner.ang1;int ang2 = ctscanner.ang2;
                    int s = ctscanner.outputimgsize;
                    ctscanner.animimage = new double[s][s];
                    //for (phi=ang1;phi<=ang2;phi=phi+stepsize){
                     for (int v=0; v<ctscanner.views; v++){   
                        if (closed) break;
                        mBufferedImage = ctscanner.CreateReconstructedImage();                      
                        windowing = false;
                        repaint();
                        ctscanner.animcount++;
                    }
                    double maxval = Utils.getMax(ctscanner.pixels);
                    mBufferedImage = ctscanner.CreateImagefromArray(ctscanner.pixels, maxval, 1);
                    //data = ctscanner.pixels;
                    //pixels = Get12bitPixelData(data, ctscanner.maxval);
                    ctscanner.animcount=0;
                    
                } else{
                    mBufferedImage = ctscanner.CreateReconstructedImage();
                }
                
                return mBufferedImage;
                
            }
            
            //Runs on the event-dispatching thread.
            public void finished() {
                finished = true;
                //dt = System.currentTimeMillis() - startTime;
                //bpprogresspanel.append("time taken: "+(double)dt/1000+" seconds\n");
                bpprogresspanel.append("time taken: "+ctscanner.timetaken+" seconds\n");
                bpprogresspanel.produceSummary(ctscanner.outputimgsize,
                        ctscanner.views, ctscanner.scans, ctscanner.stepsize);
                ctscanner.animcount=0;
                
                setPixelData();
                PerformWindowing();
                repaint();
            }
        };
        worker.start();
    }
    
    
    
    public void ReprojectImage(final CTScanner ctscanner){
        bpprogresspanel = new ProgressPanel();
        finished = false;
        
        final SwingWorker worker = new SwingWorker() {
            public Object construct() {
                double[][] pix;
                if (ijimage != null){  //if an ImagePlus is loaded use that
                    int type = ijimage.getType();
                    if (type == ImagePlus.GRAY8) { bpprogresspanel.append("Input Image is 8 bit gray"); }
                    if (type == ImagePlus.GRAY16) { bpprogresspanel.append("Input Image is 16 bit gray"); }
                    if (type == ImagePlus.COLOR_256) { bpprogresspanel.append("Input Image is 8 bit color"); }
                    if (type == ImagePlus.COLOR_RGB) { bpprogresspanel.append("Input Image is RGB color"); }
                    bpprogresspanel.append(" Size: "+mBufferedImage.getWidth()+"x"+
                            mBufferedImage.getHeight()+"\n");
                    
                    //pix = ImageUtils.getImagePlusPixels(ijimage);
                    //}
                    //else{ //otherwise use the current buffered image
                    
                } pix = getdoubleBuffImagePixels();
                System.out.println();
                bpprogresspanel.append("Generating projection data from image pixels.."+"\n");
                ctscanner.ProjectfromImage(pix);
                if (ctscanner.method == "fbp"){
                    bpprogresspanel.append("Performing back projection.. ");
                    if (ctscanner.filtering == true){
                        bpprogresspanel.append("Using "+ctscanner.filtername+ " Filter \n");
                        if (ctscanner.filtername == "hamming" || ctscanner.filtername == "hann"  ){
                            bpprogresspanel.append("Cutoff at "+ctscanner.filtercutoff+ "\n");
                        }
                    } if (ctscanner.truncate == true){
                        bpprogresspanel.append("Using truncated data at "
                                +ctscanner.truncatewidth+ " \n");
                    }
                }
                
                else {bpprogresspanel.append("Performing iterative reconstruction.. \n"); }
                mBufferedImage = ctscanner.CreateReconstructedImage();
                
                return mBufferedImage;
                
            }
            //Runs on the event-dispatching thread.
            public void finished() {
                finished = true;
                bpprogresspanel.produceSummary(ctscanner.outputimgsize,
                        ctscanner.views, ctscanner.scans, ctscanner.stepsize);
                //once done imageplus object is no longer needed, so set to null
                ijimage = null;
                
                setPixelData();
                PerformWindowing();
                repaint();
            }
        };
        worker.start();
    }
    
    
    
    
    /**gets the pixels values from a bufferedimage and returns a 2D integer pixel array*/
    public int[][] getBuffImagePixels(){
        int[][] pix = ImageUtils.getBuffImagePixels(mBufferedImage);
        return pix;
    }
    
    /**gets the pixels values from the bufferedimage and returns a 2D double pixel array*/
    public double[][] getdoubleBuffImagePixels(){
        
        double[][] pix = ImageUtils.getdoubleBuffImagePixels(mBufferedImage);
        return pix;
    }
    
    public void PerformImageop(BufferedImageOp op) {
        //mBufferedImage = op.filter(mBufferedImage, null);
        windowedImage = op.filter(windowedImage, null);
        repaint();
    }
    
    
    /**performs histogram equalisation on the current bufferedimage*/
    public void EqImageHistogram() {
        Histogram hist = new Histogram( mBufferedImage );
        //mBufferedImage  = hist.makeHistEqualizedImg();
        windowedImage  = hist.makeHistEqualizedImg();
        repaint();
    }
    
    /**windows the image*/
    public void PerformWindowing(){
        windowing = true;
        if (pixels == null) setPixelData();
        int iw = mBufferedImage.getWidth();
        int ih = mBufferedImage.getHeight();
        int winwidth = upperwinlvl - lowerwinlvl;
        if ((getImageType() == 11) || (getImageType() == 10))  {
            windowedImage = new BufferedImage(iw,ih,BufferedImage.TYPE_BYTE_GRAY);
            
            WritableRaster wraster = windowedImage.getRaster();
            for (int x = 0; x < iw; x++ ) {
                for (int y = 0; y < ih; y++ ) {
                    //int val = wraster.getSample(x, y, 0);
                    int val = pixels[x][y];
                    if ( val <= lowerwinlvl){
                        wraster.setSample(x, y, 0, 0);
                    } else if (val >= upperwinlvl) {
                        wraster.setSample(x, y, 0, 255);
                    } else {
                        int newval = (val-lowerwinlvl) *256/winwidth;
                        wraster.setSample(x, y, 0, newval);
                    }
                }
            }
        }
        
        else  {
            windowedImage = new BufferedImage(iw,ih,BufferedImage.TYPE_INT_RGB);
            windowedImage.createGraphics().drawImage(mBufferedImage, 0, 0, null);
            /*WritableRaster wraster = windowedImage.getRaster();
            for (int x = 0; x < iw; x++ ) {
                for (int y = 0; y < ih; y++ ) {

                    int pixel = mBufferedImage.getRGB(x, y);
                    int a = ((pixel >> 24) & 0xff);
                    int r = ((pixel >> 16) & 0xff);
                    int g = ((pixel >> 8) & 0xff);
                    int b = ((pixel ) & 0xff);
          
                    //int sum = r + g + b;
                    //double val = (sum / 3);
                    double val = 0.299*r + 0.587*g + 0.114*b;
                    if (val <= lowerwinlvl * 0.1275)
                        windowedImage.setRGB(x, y, 0);
                    else if (val >= upperwinlvl * 0.1275)
                        windowedImage.setRGB(x, y, 0xffffff);
                    else{
                        double width = winwidth * 0.1275;
                        double l = lowerwinlvl * 0.1275;
                        double u = upperwinlvl * 0.1275;
                        r = (byte) ((r-1) * width/255);
                        g = (byte) ((g-1) * width/255);
                        b = (byte) ((b-1) * width/255);
                        //pixel = (r<<16)|(g<<8)|b;
                        pixel = (a << 24) | (r&0xff << 16) | (g&0xff << 8) | b&0xff;
                        windowedImage.setRGB(x, y, pixel);
                        //wraster.setSample(x, y, 0, r);
                        //wraster.setSample(x, y, 1, g);
                        //wraster.setSample(x, y, 2, b);
                        
                    }
                    
                }
            } */
        }
        
        repaint();
    }
    
    
    public void WindowingOff(){
        windowing = false;
        repaint();
    }
    
    public void getWindowLevels(int low, int upp){
        low = this.lowerwinlvl;
        upp = this.upperwinlvl;
    }
    
    public void resetWindowLevels(){
        this.lowerwinlvl = 0;
        this.upperwinlvl = 255;
    }
    
    
    
    /**custom paint method that ensures proper display ratio of images*/
    public void paintComponent(Graphics g) {
        //Graphics2D g2 = (Graphics2D)g;
        int w = this.getWidth();
        int h = this.getHeight();
        super.paintComponent(g);
        //g2.setRenderingHints(Graphics2D.ANTIALIASING, Graphics2D.ANTIALIAS_ON);
        if (mBufferedImage == null) return;
        Insets insets = getInsets();
        
        
        //the following code ensures that the image is displayed with the
        //correct ratio as the internal frame is resized
        imageratio = (double)mBufferedImage.getWidth()
        / (double)mBufferedImage.getHeight();
        if (imageratio >= 1) {
            w = this.getWidth();
            h = (int) ((double)w / imageratio);
            insets.top = this.getHeight()/2 - h/2;
            insets.left = getInsets().left;
            if (h > this.getHeight()){
                h = this.getHeight();
                w = (int) ((double)h * imageratio);
                insets.left = this.getWidth()/2 - w/2;
                insets.top = getInsets().top;
            }
        } else if (imageratio < 1){
            h = this.getHeight();
            w = (int) ((double)h * imageratio);
            insets.left = this.getWidth()/2 - w/2;
            insets.top = getInsets().top;
            if (w > this.getWidth()){
                w = this.getWidth();
                h = (int) ((double)w / imageratio);
                insets.top = this.getHeight()/2 - h/2;
                insets.left = getInsets().left;
                
            }
        }
        
        if (windowing == true){
            try {
                g.drawImage(windowedImage, insets.left, insets.top, w, h, null);
            } catch (NullPointerException ne){};
        } else{
            g.drawImage(mBufferedImage, insets.left, insets.top, w, h, null);
        }
        
        g.setColor(Color.yellow);
        if (x1 != x2){
            g.drawRect( Math.min( x1, x2 ), Math.min( y1, y2 ),
                    Math.abs( x1 - x2 ), Math.abs( y1 - y2 ) );
        }
    }
    
    private static String getFormatName(Object o) {
        try {
            // Create an image input stream on the image
            ImageInputStream iis = ImageIO.createImageInputStream(o);
            
            // Find all image readers that recognize the image format
            Iterator iter = ImageIO.getImageReaders(iis);
            if (!iter.hasNext()) {
                // No readers found
                return null;
            }
            
            // Use the first reader
            javax.imageio.ImageReader reader = (javax.imageio.ImageReader)iter.next();
            
            // Close stream
            iis.close();
            
            // Return the format name
            return reader.getFormatName();
        } catch (IOException e) {
        }
        // The image could not be read
        return null;
    }
    
    
    /**Performs fft operation on input image
     *@return   filtered BufferedImage
     *@param    img the input image
     */
    public BufferedImage FFTImage(BufferedImage img) {
        int iw = img.getWidth(null);
        int ih = img.getHeight(null);
        double[] pixels = ImageUtils.getbuffImage1DPixels(img);
        //the integer array to store the final result
        int[] newpixels = new int[iw*ih];
        
        MyFFT myfft = new MyFFT();
        newpixels = myfft.forward2dFFT(pixels, iw, ih);
        img = Utils.CreateImagefrom1DintArray(newpixels, iw, ih);
        
        repaint();
        return img;
    }
    
    /**Filters the current image and updates the currently displayed image in-place
     *@param    choice  string representing the type of filter, 'blur' or 'edge'
     */
    public void FFTCurrentImage(){
        //mBufferedImage = FFTImage(mBufferedImage);
        windowedImage = FFTImage(mBufferedImage);
        repaint();
    }
    
    public void ScaleImage(){
        //windowedImage = ImageUtils.Scale(ijimage);
        mBufferedImage = ImageUtils.Scale(ijimage);
        repaint();
    }
    
    
    /**A textarea class used to indicate the steps in processing for imagepanel
     *this is an inner class */
    public class ProgressPanel extends JTextArea{
        
        public ProgressPanel(){
            super(5,1);
            setLineWrap(true);
            setEditable(false);
            setBorder(BorderFactory.createEmptyBorder(10,5,10,5));
            //setPreferredSize(new Dimension(100, 60));
            setFont(new Font("Monospaced", 0, 11));
            setBackground(Color.white);
            
        }
        
        public void produceSummary(int size, int v, int s, float step){
            String strsize =  Integer.toString(size);
            append("Completed:  views used: "+ v+" scans: "+s+" step size:"+step+"\n");
            append("Image is "+ strsize+"x"+strsize);
            setCaretPosition(this.getText().length());
            
        }
        
        public void updatecoords(int x, int y){
            
            append(Integer.toString(x)+", ");append(Integer.toString(y)+"\n");
            setCaretPosition(this.getText().length());
            
        }
        
        public void updateproj(int p, int s){
            
            append("phi:"+Integer.toString(p)+" ");append("scan:"+Integer.toString(s));
            setCaretPosition(this.getText().length());
            
        }
    }
    
}







