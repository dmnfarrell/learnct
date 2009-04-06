/*
 * InternalDrawFrame.java
 *
 * Created on 09 March 2006, 14:20
 */

package myctapp;

import ij.*;
import ij.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.image.*;
import java.util.*;


public class InternalDrawFrame extends JInternalFrame {
    
    static int openFrameCount = 0;
    static final int xOffset = 30, yOffset = 30;
    //public int PANELTYPE;
    public DrawPanel drawpanel;
    private JLabel label;
    
    /** Creates a new instance of InternalDrawFrame */
    public InternalDrawFrame() {
        super("Frame#" + (++openFrameCount), true, true, true, true);
        setDefaultCloseOperation( DISPOSE_ON_CLOSE );
        setResizable(true);
        setMaximizable(true);
        setLocation(xOffset*openFrameCount, yOffset*openFrameCount);

        // attach panel to internal frame content pane
        setDefaultCloseOperation( DISPOSE_ON_CLOSE );
        setResizable(false);
        setMaximizable(false);
        Container container = this.getContentPane();
        drawpanel = new DrawPanel();
        container.add( drawpanel, BorderLayout.CENTER );
        container.add( drawpanel.phantom.pinfoArea, BorderLayout.SOUTH);
        
        // set size internal frame to size of its contents
        this.pack();
        this.setVisible( true );
        setLocation(xOffset*openFrameCount, yOffset*openFrameCount);
        
    }
    
  /*          
    public void setupDrawPanel(String path){
        
        // attach panel to internal frame content pane
        setDefaultCloseOperation( DISPOSE_ON_CLOSE );
        setResizable(false);
        setMaximizable(false);
        Container container = this.getContentPane();
        drawpanel = new DrawPanel(path);
        //drawpanel.loadPhant(path);
        container.add( drawpanel, BorderLayout.CENTER );
        container.add( drawpanel.phantom.pinfoArea, BorderLayout.SOUTH);
        
        // set size internal frame to size of its contents
        this.pack();
        this.setVisible( true );
        setLocation(xOffset*openFrameCount, yOffset*openFrameCount);
        PANELTYPE = 1;
        //addInternalFrameListener(this);
    } */
}
