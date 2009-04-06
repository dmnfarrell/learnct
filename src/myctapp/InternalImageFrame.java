package myctapp;

import ij.*;
import ij.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.image.*;
import java.util.*;


/*@author     Damien Farrell
 *@created    February 2004 */
/**Custom JInternalFrame class to provide windows inside the application */

public class InternalImageFrame extends JInternalFrame {
    
    static int openFrameCount = 0;
    static final int xOffset = 30, yOffset = 30;
    public int PANELTYPE;
    public ImagePanel imagepanel;
    public JSlider stackslider;
    private JLabel label;
    
    public InternalImageFrame(){
        super("Frame#" + (++openFrameCount), true, true, true, true);
        setDefaultCloseOperation( DISPOSE_ON_CLOSE );
        setResizable(true);
        setMaximizable(true);
        setClosable(true);
        setIconifiable(true);
        this.pack();
        this.setVisible( true );
        setLocation(xOffset*openFrameCount, yOffset*openFrameCount);
    }
    
    public InternalImageFrame(String path, int ptype, CTScanner ctscanner) {
        super("Frame#" + (++openFrameCount), true, true, true, true);
        //setupImagePanel(path, ptype, ctscanner);
        setDefaultCloseOperation( DISPOSE_ON_CLOSE );
        // attach panel to internal frame content pane
        Container container = this.getContentPane();
        //creates a new ImagePanel object that will load the image into itself
        
        imagepanel = new ImagePanel();
        container.add( imagepanel, BorderLayout.CENTER);
        
        // set size internal frame to size of its contents
        this.pack();
        this.setVisible( true );
        
        setLocation(xOffset*openFrameCount, yOffset*openFrameCount);
        //addInternalFrameListener(this);
        if (ptype == 2){
            Opener op = new Opener(); 
            ImagePlus imp = op.openImage(path);
            imagepanel.loadImagePlus(imp);
            PANELTYPE = 2;
        }

        else if (ptype == 3){
            if(path == null){
                imagepanel.loadSinogram(ctscanner);
            }
            else{
                imagepanel.loadSinogram(ctscanner, path);
            }
            PANELTYPE = 3;
        }
        else if (ptype == 4){
            
            imagepanel.loadBPImage(ctscanner);
            JScrollPane scrollingResult = new JScrollPane
            (imagepanel.bpprogresspanel);
            scrollingResult.setPreferredSize(new Dimension(200, 60));
            container.add(scrollingResult,  BorderLayout.SOUTH);

            PANELTYPE = 4;
        }
        else if (ptype == 5){
            
            Opener op = new Opener(); 
            ImagePlus imp = op.openImage(path);
            imagepanel.loadImagePlus(imp);
            imagepanel.ReprojectImage(ctscanner);
            JScrollPane scrollingResult = new JScrollPane
            (imagepanel.bpprogresspanel);
            scrollingResult.setPreferredSize(new Dimension(200, 60));
            container.add(scrollingResult,  BorderLayout.SOUTH);
            PANELTYPE = 5;
        }
        else if (ptype == 6){
            ctscanner.ProjectPhantom();           
            imagepanel.loadBPImage(ctscanner);
            JScrollPane scrollingResult = new JScrollPane
            (imagepanel.bpprogresspanel);
            scrollingResult.setPreferredSize(new Dimension(200, 60));
            container.add(scrollingResult,  BorderLayout.SOUTH);
            PANELTYPE = 6;
        }
               

    }
    
    public InternalImageFrame(ImagePlus imp) {
        super("Frame#" + (++openFrameCount), true, true, true, true);
        setDefaultCloseOperation( DISPOSE_ON_CLOSE );
        // attach panel to internal frame content pane
        Container container = this.getContentPane();
        imagepanel = new ImagePanel();
        int stsize = imp.getStackSize();
        if (stsize > 1){
            stackslider = new JSlider(SwingConstants.HORIZONTAL, 0, stsize-1, 0 );
            //int major = Math.round(stsize/2), minor=major/2;
            //stackslider.setMajorTickSpacing(major); stackslider.setMinorTickSpacing(minor);
            //stackslider.setPaintTicks(true); //stackslider.setPaintLabels( true );
            stackslider.addChangeListener(new ChangeListener() {
                // This method is called whenever the slider's value is changed
                public void stateChanged(ChangeEvent evt) {
                    JSlider slider = (JSlider)evt.getSource();
                    
                    if (slider.getValueIsAdjusting()) {
                        int value1 = slider.getValue();
                        //System.out.println("val1: "+value1);
                        try{
                            imagepanel.getCurrentImageinStack(value1);
                        } catch(NullPointerException s){}
                    }
                }
            });
            
            this.add(stackslider, BorderLayout.NORTH);
            imagepanel.importIJStack(imp);
            imagepanel.setStackSize(stsize);
            PANELTYPE = 8;
        } else{
            imagepanel.loadImagePlus(imp);
            PANELTYPE = 2;
        }
        
        container.add( imagepanel, BorderLayout.CENTER); 
        // set size internal frame to size of its contents
        this.pack();
        this.setVisible( true );
        setLocation(xOffset*openFrameCount, yOffset*openFrameCount);
        
    }
    

     public void AddPanelsandDisplay() {
      
          setDefaultCloseOperation( DISPOSE_ON_CLOSE );
          // attach panel to internal frame content pane
          Container container = this.getContentPane();
          JScrollPane scrollingResult = new JScrollPane
                  (imagepanel.bpprogresspanel);
          scrollingResult.setPreferredSize(new Dimension(200, 60));
          container.add(scrollingResult,  BorderLayout.SOUTH);
          PANELTYPE = 5;
          container.add( imagepanel, BorderLayout.CENTER);
          // set size internal frame to size of its contents
          this.pack();
          this.setVisible( true );
          setLocation(xOffset*openFrameCount, yOffset*openFrameCount);
      }
      

    
    public InternalImageFrame CreateCopy () {
        InternalImageFrame copy = new InternalImageFrame();
        copy.imagepanel = new ImagePanel();
        if (this.imagepanel.ijimage != null){
            copy.imagepanel.loadImagePlus(this.imagepanel.ijimage);
        } else{
            copy.imagepanel.loadBufferedImage(this.imagepanel.getBufferedImage());            
        }
        
        return copy;
    }
   

}

