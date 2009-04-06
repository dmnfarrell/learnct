package myctapp;

import ij.*;
import ij.io.*;
import myctapp.gui.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.EtchedBorder;
import java.awt.image.*;
import java.awt.geom.*;
import java.util.*;
import java.io.*;
import java.net.URL;
import java.text.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.text.html.HTMLDocument;
import java.lang.reflect.*;
import java.lang.Exception.*;

 /*@author     Damien Farrell
  *@created    January 18, 2004 */
/**  main GUI interface */

public class MyApp extends JFrame implements ActionListener, ChangeListener,
        PropertyChangeListener, ItemListener {
    
    public static String osname = System.getProperty("os.name");
    private MDIDesktopPane theDesktop;  //custom scrollable desktop
    private JScrollPane scrollPane;
    private FrameListener frameListener;
    private JPanel widgetPanel, textfieldPanel, windowingPanel, winPanel, sliderPanel;
    private GradientPanel gradientPanel;
    private InfoPanel imginfoPanel;
    private InternalImageFrame currentframe;
    private JLabel scansLabel, viewsLabel, stepsizeLabel, imgsizeLabel, zoomLabel, xoffsetLabel,
            yoffsetLabel, lowerlevelLabel, upperlevelLabel, bitdepthLabel;
    private JFormattedTextField scansField, viewsField, stepsizeField, imgsizeField,
            zoomField, xoffsetField, yoffsetField, lowerlevelField,
            upperlevelField, bitdepthField;
    private NumberFormat scansFormat, viewsFormat, stepsizeFormat, imgsizeFormat,
            zoomFormat, offsetFormat, lowerlevelFormat, upperlevelFormat;
    private JCheckBoxMenuItem filtercheckbox, truncatecheckbox, ROIcropcheckbox, ROIcirclecheckbox,
            setedgezerocheckbox, animatecheckbox, keepcurrentextrapwidthscheckbox;
    private static String scansString = "Scans: ";
    private static String viewsString = "Views: ";
    private static String stepsizeString = "\u0398 Step:";
    private static String imgsizeString = "Img Size:";
    private static String zoomString = "Zoom:";
    private static String xoffsetString = "X-Offset:";
    private static String yoffsetString = "Y-Offset: ";
    private MThumbSlider mSlider1, mSlider2;
    private JToolBar imagetoolBar, filetoolBar;
    private Action openimageAction, saveimageAction, quitAction, blurAction,
            sharpenAction, invertAction, brightenAction, darkenAction,
            greyscaleAction, scaleAction, loadPhantomAction, savePhantomAction,
            createSinogramAction, loadSinogramAction, projPhantomAction,
            drawCustomPhantomAction,
            loadProjAction, saveProjAction, copyAction, pasteasimageAction,
            createBPImageAction, createProjfromImageAction,
            importProjAction, exportProjAction,
            showhistogramAction, eqhistimageAction, fftimageAction,
            addimagesAction, subimagesAction, divimagesAction, findmseAction;
    private JProgressBar progressBar;
    private MemoryMonitor memmonitor;
    private static Font theMainFont, smallFont;
    private BufferedImage clipboardImage;
    private double[][] clipboarddata;
    private int upper, lower;
    private static CTScanner ctscanner;
    
    
//  private Timer timer;
    
    // constructor for the menu bar and items
    
    private JMenuBar createMenuBar() {
        theMainFont = new Font("SansSerif", Font.PLAIN, 11);
        smallFont = new Font("SansSerif", Font.PLAIN, 10);
        JMenuBar menuBar;
        JMenu filemenu, toolsmenu, imagemenu, windowmenu, phantommenu, helpmenu,
                settingsmenu;
        JMenuItem menuItem;
        UIManager.put("MenuBar.font", theMainFont );
        UIManager.put("Menu.font", theMainFont);
        UIManager.put("MenuItem.font", theMainFont);
        UIManager.put("RadioButton.font", theMainFont);
        UIManager.put("RadioButtonMenuItem.font", theMainFont);
        UIManager.put("CheckBoxMenuItem.font", theMainFont);
        //Create the menu bar.
        menuBar = new JMenuBar();
        
        menuBar.setOpaque(true);
        menuBar.setMargin(new Insets(0,0,20,0));
        
        menuBar.setPreferredSize(new Dimension(300, 20));
        
        
        //add file menu items to menuba
        filemenu = new JMenu("File");
        filemenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(filemenu);
        menuItem = new JMenuItem(loadProjAction);
        filemenu.add(menuItem);
        menuItem = new JMenuItem(saveProjAction);
        filemenu.add(menuItem);
        //menuItem = new JMenuItem(exportProjAction);
        //filemenu.add(menuItem);
        menuItem = new JMenuItem(importProjAction);
        filemenu.add(menuItem);
        menuItem = new JMenuItem(openimageAction);
        filemenu.add(menuItem);
        menuItem = new JMenuItem(saveimageAction);
        filemenu.add(menuItem);
        JMenu Prefmenu = new JMenu("Preferences");
        
        Action setLNFAction = new AbstractAction("Set LNF", null){
            public void actionPerformed(ActionEvent evt) {
                LNFSwitcher();
            }
        };
        
        menuItem = new JMenuItem(setLNFAction);
        Prefmenu.add(menuItem);
        filemenu.add(Prefmenu);
        menuItem = new JMenuItem(quitAction);
        filemenu.add(menuItem);
        
        imagemenu = new JMenu("Image");
        menuBar.add(imagemenu);
        JMenu imageFiltersMenu = new JMenu("Filters");
        menuItem = new JMenuItem(blurAction);
        imageFiltersMenu.add(menuItem);
        menuItem = new JMenuItem(sharpenAction);
        imageFiltersMenu.add(menuItem);
        imagemenu.add(imageFiltersMenu);
        
        JMenu imageHistogramMenu = new JMenu("Histogram");
        menuItem = new JMenuItem(showhistogramAction);
        imageHistogramMenu.add(menuItem);
        menuItem = new JMenuItem(eqhistimageAction);
        imageHistogramMenu.add(menuItem);
        imagemenu.add(imageHistogramMenu);
        
        JMenu imageArithmeticMenu = new JMenu("Arithemtic");
        menuItem = new JMenuItem(addimagesAction);
        imageArithmeticMenu.add(menuItem);
        menuItem = new JMenuItem(subimagesAction);
        imageArithmeticMenu.add(menuItem);
        menuItem = new JMenuItem(divimagesAction);
        imageArithmeticMenu.add(menuItem);
        imagemenu.add(imageArithmeticMenu);
        menuItem = new JMenuItem(findmseAction);
        imageArithmeticMenu.add(menuItem);
        imagemenu.add(imageArithmeticMenu);
        
        JMenu imageFFTMenu = new JMenu("FFT");
        menuItem = new JMenuItem(fftimageAction);
        imageFFTMenu.add(menuItem);
        imagemenu.add(imageFFTMenu);
        
        menuItem = new JMenuItem(invertAction);
        imagemenu.add(menuItem);
        menuItem = new JMenuItem(brightenAction);
        imagemenu.add(menuItem);
        menuItem = new JMenuItem(darkenAction);
        imagemenu.add(menuItem);
        
        phantommenu = new JMenu("Phantom");
        menuBar.add(phantommenu);
        createPhantomMenu(phantommenu);
        
        settingsmenu = new JMenu("Settings");
        menuBar.add(settingsmenu);
        createSettingsMenu(settingsmenu);
        
        toolsmenu = new JMenu("Tools");
        menuBar.add(toolsmenu);
        createToolsMenu(toolsmenu);
        menuBar.add(new WindowMenu(theDesktop));
        
        helpmenu = new JMenu("Help");
        menuBar.add(helpmenu);
        
        menuItem = new JMenuItem("Manual");
        menuItem.setActionCommand("manual");
        //read in the help html file
        menuItem.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                try {
                    String path=MyApp.class.getResource("MyApp.class").toString();
                    //path = path.substring((path.indexOf('/')+1),path.lastIndexOf('/'));
                    path = path.substring((path.indexOf('/')+1),path.indexOf("!/")-4);  //for jar
                    System.out.println(path);
                    InputStream inputStream = new FileInputStream(path+"\\help.html");
                    InputStreamReader reader = new InputStreamReader( inputStream );
                    
                    //read it in, line by line
                    BufferedReader br = new BufferedReader( reader );
                    String tempLine;
                    StringBuffer strb = new StringBuffer();
                    while( ( tempLine = br.readLine() ) != null ) { strb.append( tempLine ); }
                    
                    // append the resulting whole to your textarea
                    JEditorPane pane = new JEditorPane();
                    pane.setEditable(false);
                    JScrollPane scrollPane = new JScrollPane(pane);
                    HTMLDocument htmlDoc = new HTMLDocument();
                    //URL u = this.getCodeBase();
                    //HTMLDocument.setBase();
                    pane.setDocument(htmlDoc);
                    pane.setContentType( "text/html" );
                    pane.setText( strb.toString() );
                    
                    InternalImageFrame frame = new InternalImageFrame();
                    frame.getContentPane().add(scrollPane);
                    theDesktop.add(frame);
                } catch(IOException ie){System.out.println("Oops- an IOException happened."); }}
        });
        helpmenu.add(menuItem);
        menuItem = new JMenuItem("About");
        menuItem.setActionCommand("help");
        menuItem.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                displaySplash();}
        });
        helpmenu.add(menuItem);
        
        return menuBar;
        
    }
    
    // instantiate action variables to be used in meus and buttons
    private void createActions() {
        loadProjAction = new LoadProjAction("Load Proj File", createIcon("open"),
                "Load proj file",  new Integer(KeyEvent.VK_H));
        saveProjAction = new SaveProjAction("Save Current Projs.", createIcon("save"),
                "Save current projections to file",  new Integer(KeyEvent.VK_P));
        importProjAction = new ImportProjAction("Import Proj. Data", createIcon("open"),
                "Import projection data from Image",  new Integer(KeyEvent.VK_R));
        //exportProjAction = new ExportProjAction("Export Current Projs.", createIcon("save"),
        //      "Export current projections",  new Integer(KeyEvent.VK_X));
        projPhantomAction = new ProjPhantomAction("Proj Phantom",
                "Back proj dummmy phantom",  new Integer(KeyEvent.VK_N));
        createSinogramAction = new CreateSinogramAction("Sinogram",
                "Display the Sinogram for current projection",  new Integer(KeyEvent.VK_G));
        loadSinogramAction = new LoadSinogramAction("Load Sinogram",
                "Load a Sinogram from projections file",  new Integer(KeyEvent.VK_D));
        createBPImageAction = new CreateBPImageAction("Back Project",
                "Display the Back Projection",  new Integer(KeyEvent.VK_A));
        createProjfromImageAction = new CreateProjfromImageAction("Reproject Image",
                "Create Projection data from an Image",  new Integer(KeyEvent.VK_E));
        drawCustomPhantomAction = new DrawCustomPhantomAction("Draw Custom Phantom", createIcon("draw"),
                "Draw an object from ellipses",  new Integer(KeyEvent.VK_J));
        openimageAction = new OpenImageAction("Open Image", createIcon("open"),
                "Open an image",  new Integer(KeyEvent.VK_O));
        saveimageAction = new SaveImageAction("Save Image", createIcon("save"),
                "Save image",  new Integer(KeyEvent.VK_S));
        quitAction = new QuitAction("Quit",
                "Quit Application",  new Integer(KeyEvent.VK_Q));
        blurAction = new BlurAction("Blur", createIcon("blur"),
                "Blur image", new Integer(KeyEvent.VK_1));
        sharpenAction = new SharpenAction("Sharpen", createIcon("sharpen"),
                "Sharpen image",  new Integer(KeyEvent.VK_2));
        invertAction = new InvertAction("Invert", createIcon("invert"),
                "Negative image",  new Integer(KeyEvent.VK_3));
        brightenAction = new BrightenAction("Brighten", createIcon("brighten"),
                "Brighten image",  new Integer(KeyEvent.VK_4));
        darkenAction = new DarkenAction("Darken", createIcon("darken"),
                "Darken image",  new Integer(KeyEvent.VK_5));
        greyscaleAction = new GreyScaleAction("Greyscale", createIcon("greyscale"),
                "Convert to greyscale",  new Integer(KeyEvent.VK_6));
        scaleAction = new ScaleAction("Scale", createIcon("scale"),
                "Scale Image",  new Integer(KeyEvent.VK_7));
        showhistogramAction = new showHistogramAction("View Histogram", createIcon("histogram"),
                "Display Histogram",  new Integer(KeyEvent.VK_M));
        eqhistimageAction = new eqHistImageAction("Eq Histogram", createIcon("eq_histogram"),
                "Eq Histogram",  new Integer(KeyEvent.VK_L));
        addimagesAction = new addImagesAction("Add", createIcon("add_images"),
                "Add Images",  new Integer(KeyEvent.VK_W));
        subimagesAction = new subImagesAction("Subtract", createIcon("sub_images"),
                "Subtract Images",  new Integer(KeyEvent.VK_Y));
        //multimagesAction = new multImagesAction("Multiply", createIcon("mult images"),
        //"Add Images",  new Integer(KeyEvent.VK_X));
        divimagesAction = new divImagesAction("Divide", createIcon("divide"),
                "Divide Images",  new Integer(KeyEvent.VK_Z));
        findmseAction = new findMSEAction("Find MSE", createIcon("MSE"),
                "Divide Images",  new Integer(KeyEvent.VK_X));
        fftimageAction = new FFTImageAction("FFT", createIcon("fft"),
                "Find Image FFT",  new Integer(KeyEvent.VK_T));
        
        
    }
    
    /**create tools menu*/
    private void createToolsMenu(JMenu rootMenu){
        
        JMenuItem menuItem;
        //set up actions first
        JMenu SelectionMenu = new JMenu("Selection");
        copyAction = new CopySelectionAction("Copy", createIcon("copy"),
                "Copy Selection",  new Integer(KeyEvent.VK_C));
        pasteasimageAction = new PasteasImageAction("Paste as Image", createIcon("paste"),
                "Paste Selection",  new Integer(KeyEvent.VK_V));
        Action findMeanaction = new AbstractAction("Measure") {
            public void actionPerformed(ActionEvent evt) {
                InternalImageFrame frame1 = (InternalImageFrame)theDesktop.getSelectedFrame();
                if (frame1.PANELTYPE >= 2) {
                    int[][] temppixels = frame1.imagepanel.getSelectionPixels();
                    double[][] tempdata = frame1.imagepanel.getSelectionData();
                    double mean = Utils.getAverage2DIntArray(temppixels);
                    double min = Utils.getMin(temppixels);
                    double max = Utils.getMax(temppixels);
                    double stdev = Utils.round(Utils.getStDev2DIntArray(temppixels),2);
                    double datamean = Utils.round(Utils.getAverage2DDoubleArray(tempdata),3);
                    double datamin = Utils.round(Utils.getMin(tempdata),3);
                    double datamax = Utils.round(Utils.getMax(tempdata),3);
                    double datastdev = Utils.round(Utils.getStDev2DDoubleArray(tempdata),3);
                    JOptionPane.showInternalMessageDialog(theDesktop, "Mean: "+ mean+
                            " Min: "+min + " Max: "+max+
                            " St. Dev: "+stdev+
                            "\n"+"Data Mean: "+datamean+
                            " Min: "+datamin + " Max: "+datamax+
                            " St. Dev: "+datastdev,
                            "Measurements",
                            JOptionPane.PLAIN_MESSAGE);
                    //System.out.println("Mean: "+ mean);
                    //System.out.println("Min: "+min);
                    //System.out.println("Max: "+max);
                }
            }
        };
        
        menuItem = new JMenuItem(copyAction);
        SelectionMenu.add(menuItem);
        menuItem = new JMenuItem(pasteasimageAction);
        SelectionMenu.add(menuItem);
        SelectionMenu.add(new JSeparator());
        
        menuItem = new JMenuItem(findMeanaction);
        SelectionMenu.add(menuItem);
        rootMenu.add(SelectionMenu);
        
        Action FindNumRaysAction = new AbstractAction("Show ray data"){
            public void actionPerformed(ActionEvent evt) {
                //ctscanner.FindNumRays();
                double truncrayperc = Utils.round((double)ctscanner.truncbeamrays/ctscanner.numrays,2);
                JOptionPane.showInternalMessageDialog(theDesktop, "Total rays: "+ ctscanner.totalrays+"\n"+
                        "Full rays through object: "+ctscanner.numrays+"\n"+
                        "Trunc rays through object: "+ctscanner.truncbeamrays+
                        "  ("+truncrayperc+")",
                        "Ray Data",
                        JOptionPane.PLAIN_MESSAGE);
            }
        };
        menuItem = new JMenuItem(FindNumRaysAction);
        rootMenu.add(menuItem);
        
        Action GraphDataAction = new AbstractAction("Graph Data"){
            public void actionPerformed(ActionEvent evt) {
                
                DataFrame frame = new DataFrame();
                frame.setupPanels();
                frame.pack(); frame.setVisible( true );
                theDesktop.add( frame );
                try {
                    frame.setSelected(true);
                } catch (java.beans.PropertyVetoException s) {}
                
                
            }
        };
        menuItem = new JMenuItem(GraphDataAction);
        rootMenu.add(menuItem);
        
    }
    
    private void createSettingsMenu(JMenu rootMenu){
        //set up actions first
        
        Action setFBPaction = new AbstractAction("FBP") {
            public void actionPerformed(ActionEvent evt) {
                ctscanner.method = "fbp";
            }
        };
        Action setIterativeaction = new AbstractAction("Iterative") {
            public void actionPerformed(ActionEvent evt) {
                ctscanner.method =  "iterative";
            }
        };
        
        Action setnumitersaction = new AbstractAction("Set No. Iterations") {
            public void actionPerformed(ActionEvent evt) {
                String f = (String) JOptionPane.showInternalInputDialog(theDesktop,
                        "Enter value", "Set No. Iterations", 3,
                        null, null, Integer.toString(ctscanner.numiter));
                try {
                    //workaround for BlueJ bug - misses first exception after compilation
                    if (f == null){
                        ctscanner.numiter = ctscanner.numiter;
                    } else{
                        ctscanner.numiter = Integer.parseInt(f); }
                } catch(NumberFormatException exception) {}
            }
        };
        
        Action setfastaction = new AbstractAction("Fast") {
            public void actionPerformed(ActionEvent evt) {
                ctscanner.fast = true;
            }
        };
        Action setaccaction = new AbstractAction("Accurate") {
            public void actionPerformed(ActionEvent evt) {
                ctscanner.fast = false;
            }
        };
        Action setfiltcutoffaction = new AbstractAction("Set Filter Cut-Off") {
            public void actionPerformed(ActionEvent evt) {
                String f = (String) JOptionPane.showInternalInputDialog(theDesktop,
                        "Enter cutoff value", "Set Filter Cut-Off", 3,
                        null, null, Double.toString(ctscanner.filtercutoff));
                try {
                    //workaround for BlueJ bug - misses first exception after compilation
                    if (f == null){
                        ctscanner.filtercutoff = ctscanner.filtercutoff;
                    } else{
                        ctscanner.filtercutoff = Double.parseDouble(f); }
                } catch(NumberFormatException exception) {}
            }
        };
        
        Action displaygraphaction = new AbstractAction("Graph Filters"){
            public void actionPerformed(ActionEvent event) {
                GraphicsFrame graphframe = new GraphicsFrame(ctscanner, 1);
                theDesktop.add( graphframe );
                try {
                    graphframe.setSelected(true);
                } catch (java.beans.PropertyVetoException s) {}
            }
        };
        
        Action setrampaction = new AbstractAction("Simple Ramp") {
            public void actionPerformed(ActionEvent evt) {
                ctscanner.filtername = "ramp";
            }
        };
        
        Action setshepplaction = new AbstractAction("Shepp-Logan") {
            public void actionPerformed(ActionEvent evt) {
                ctscanner.filtername = "shepplogan";
            }
        };
        Action sethammingaction = new AbstractAction("Hamming") {
            public void actionPerformed(ActionEvent evt) {
                ctscanner.filtername = "hamming";
            }
        };
        Action sethannaction = new AbstractAction("Hann") {
            public void actionPerformed(ActionEvent evt) {
                ctscanner.filtername = "hann";
            }
        };
        Action setcosineaction = new AbstractAction("Cosine") {
            public void actionPerformed(ActionEvent evt) {
                ctscanner.filtername = "cosine";
            }
        };
        Action setgaussianaction = new AbstractAction("Blackman") {
            public void actionPerformed(ActionEvent evt) {
                ctscanner.filtername = "blackman";
            }
        };
        Action setinterpnearestaction = new AbstractAction("Nearest") {
            public void actionPerformed(ActionEvent evt) {
                ctscanner.interp = "nearest";
            }
        };
        Action setinterplinearaction = new AbstractAction("Linear") {
            public void actionPerformed(ActionEvent evt) {
                ctscanner.interp = "linear";
            }
        };
        
        JMenu ReconstrMethodMenu = new JMenu("Method");
        JRadioButtonMenuItem FBPButton = new JRadioButtonMenuItem(setFBPaction);
        FBPButton.setSelected(true);
        JRadioButtonMenuItem IterativeButton = new JRadioButtonMenuItem(setIterativeaction);
        IterativeButton.setSelected(false);
        ButtonGroup groupm = new ButtonGroup();
        groupm.add(FBPButton);
        groupm.add(IterativeButton);
        ReconstrMethodMenu.add(FBPButton);
        ReconstrMethodMenu.add(IterativeButton);
        FBPButton.addActionListener(this);
        IterativeButton.addActionListener(this);
        JMenuItem setnumitersButton = new JMenuItem(setnumitersaction);
        setnumitersButton.setSelected(true);
        ReconstrMethodMenu.add(new JSeparator());
        ReconstrMethodMenu.add(setnumitersButton);
        
        JMenu ProjSpeedMenu = new JMenu("Reprojection");
        JRadioButtonMenuItem fastButton = new JRadioButtonMenuItem(setfastaction);
        fastButton.setSelected(false);
        JRadioButtonMenuItem accButton = new JRadioButtonMenuItem(setaccaction);
        accButton.setSelected(true);
        ButtonGroup group1 = new ButtonGroup();
        group1.add(fastButton);
        group1.add(accButton);
        ProjSpeedMenu.add(fastButton);
        ProjSpeedMenu.add(accButton);
        fastButton.addActionListener(this);
        accButton.addActionListener(this);
        
        JMenu FilterMenu = new JMenu("Filter");
        
        filtercheckbox = new JCheckBoxMenuItem("Filtering");
        if (ctscanner.filtering == true){
            filtercheckbox.setSelected(true);
        }
        filtercheckbox.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e)  {
                if (filtercheckbox.isSelected() == true){ ctscanner.filtering = true; }
                if (filtercheckbox.isSelected() == false){ ctscanner.filtering = false; }
            }
        });
        
        JMenuItem setfiltcutoffButton = new JMenuItem(setfiltcutoffaction);
        setfiltcutoffButton.setSelected(true);
        JMenuItem displaygraphButton = new JMenuItem(displaygraphaction);
        displaygraphButton.setSelected(true);
        
        JRadioButtonMenuItem rampButton = new JRadioButtonMenuItem(setrampaction);
        rampButton.setSelected(true);
        JRadioButtonMenuItem shepplButton = new JRadioButtonMenuItem(setshepplaction);
        shepplButton.setSelected(false);
        JRadioButtonMenuItem hammingButton = new JRadioButtonMenuItem(sethammingaction);
        hammingButton.setSelected(false);
        JRadioButtonMenuItem hannButton = new JRadioButtonMenuItem(sethannaction);
        hannButton.setSelected(false);
        JRadioButtonMenuItem cosineButton = new JRadioButtonMenuItem(setcosineaction);
        cosineButton.setSelected(false);
        JRadioButtonMenuItem gaussianButton = new JRadioButtonMenuItem(setgaussianaction);
        gaussianButton.setSelected(false);
        ButtonGroup group2 = new ButtonGroup();
        group2.add(rampButton);
        group2.add(hammingButton);
        group2.add(shepplButton);
        group2.add(hannButton);
        group2.add(cosineButton);
        group2.add(gaussianButton);
        FilterMenu.add(filtercheckbox);
        FilterMenu.add(setfiltcutoffButton);
        FilterMenu.add(displaygraphButton);
        FilterMenu.add(new JSeparator());
        FilterMenu.add(rampButton);
        FilterMenu.add(shepplButton);
        FilterMenu.add(hammingButton);
        FilterMenu.add(hannButton);
        FilterMenu.add(cosineButton);
        FilterMenu.add(gaussianButton);
        
        JMenu InterpMenu = new JMenu("Interpolation");
        JRadioButtonMenuItem interp1Button = new JRadioButtonMenuItem(setinterpnearestaction);
        interp1Button.setSelected(false);
        JRadioButtonMenuItem interp2Button = new JRadioButtonMenuItem(setinterplinearaction);
        interp2Button.setSelected(true);
        ButtonGroup group3 = new ButtonGroup();
        group3.add(interp1Button);
        group3.add(interp2Button);
        InterpMenu.add(interp1Button);
        InterpMenu.add(interp2Button);
        
        JMenu NoiseMenu = new JMenu("Noise");
        
        final JRadioButtonMenuItem nonoisebutton = new JRadioButtonMenuItem("No Noise");
        final JRadioButtonMenuItem addnoisebutton = new JRadioButtonMenuItem("Add Noise to Data");
        final JRadioButtonMenuItem addnoisereconbutton = new JRadioButtonMenuItem("Reconstruct with Noise");
        nonoisebutton.setSelected(true);
        addnoisebutton.setSelected(false);
        addnoisereconbutton.setSelected(false);
        
        nonoisebutton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                ctscanner.noise = "none";
            }
        });
        addnoisebutton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                ctscanner.noise = "addtoproj";
            }
        });
        addnoisereconbutton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                ctscanner.noise = "reconwithnoise";
                System.out.println("***"+ctscanner.noise);
            }
        });
        ButtonGroup noisegrp = new ButtonGroup();
        noisegrp.add(nonoisebutton);
        noisegrp.add(addnoisebutton);
        noisegrp.add(addnoisereconbutton);
        NoiseMenu.add(nonoisebutton);
        NoiseMenu.add(addnoisebutton);
        NoiseMenu.add(addnoisereconbutton);
        
        Action setnoiselevelaction = new AbstractAction("Set Count Rate") {
            public void actionPerformed(ActionEvent evt) {
                String f = (String) JOptionPane.showInternalInputDialog(theDesktop,
                        "Enter Counts ", "Set Count Rate" ,3,
                        null, null, Double.toString(ctscanner.rate));
                
                try {
                    if (f == null){
                        ctscanner.rate = ctscanner.rate;
                    } else{
                        ctscanner.rate = Double.parseDouble(f); }
                } catch(NumberFormatException exception) {}
            }
        };
        NoiseMenu.add(new JSeparator());
        JMenuItem setnoiselevelButton = new JMenuItem(setnoiselevelaction);
        setnoiselevelButton.setSelected(true);
        NoiseMenu.add(setnoiselevelButton);
        
        JMenu ROIMenu = new JMenu("ROI");
        truncatecheckbox = new JCheckBoxMenuItem("Truncate");
        truncatecheckbox.addItemListener(this);
        if (ctscanner.truncate == true){
            truncatecheckbox.setSelected(true);
        }
        
        
        ROIcropcheckbox = new JCheckBoxMenuItem("Crop to ROI");
        if (ctscanner.roicrop == true){
            ROIcropcheckbox.setSelected(true);
        }
        ROIcropcheckbox.addItemListener(this);
        
        ROIcirclecheckbox = new JCheckBoxMenuItem("Add ROI outline");
        if (ctscanner.displayroicircle == true){
            ROIcirclecheckbox.setSelected(true);
        }
        ROIcirclecheckbox.addItemListener(this);
        
        Action settruncatewidthaction = new AbstractAction("Set ROI Radius") {
            public void actionPerformed(ActionEvent evt) {
                String f = (String) JOptionPane.showInternalInputDialog(theDesktop,
                        "Enter ROI Width (0 to .5)", "Set ROI Radius", 3,
                        null, null, Double.toString(ctscanner.truncatewidth));
                
                try {
                    if (f == null){
                        ctscanner.truncatewidth = ctscanner.truncatewidth;
                    } else{
                        ctscanner.truncatewidth = Double.parseDouble(f); }
                } catch(NumberFormatException exception) {}
            }
        };
        
        JMenuItem settruncatewidthButton = new JMenuItem(settruncatewidthaction);
        settruncatewidthButton.setSelected(true);
        
        Action setfactoraction = new AbstractAction("Set extrap factor") {
            public void actionPerformed(ActionEvent evt) {
                String f = (String) JOptionPane.showInternalInputDialog(theDesktop,
                        "Enter factor ", "Set factor", 3,
                        null, null, Double.toString(ctscanner.factor));
                
                try {
                    if (f == null){
                        ctscanner.factor = ctscanner.factor;
                    } else{
                        ctscanner.factor = Double.parseDouble(f); }
                } catch(NumberFormatException exception) {}
            }
        };
        JMenuItem setfactorButton = new JMenuItem(setfactoraction);
        setfactorButton.setSelected(true);
        
        keepcurrentextrapwidthscheckbox = new JCheckBoxMenuItem("Use Current Extrap Widths");
        if (ctscanner.keepcurrentextrapwidths == true){
            keepcurrentextrapwidthscheckbox.setSelected(true);
        }
        keepcurrentextrapwidthscheckbox.addItemListener(this);
        
        setedgezerocheckbox = new JCheckBoxMenuItem("Set Edge to Zero");
        if (ctscanner.setedgezero == true){
            setedgezerocheckbox.setSelected(true);
        }
        setedgezerocheckbox.addItemListener(this);
        
        Action truncatemethod0action = new AbstractAction("none") {
            public void actionPerformed(ActionEvent evt) {
                ctscanner.truncmethod = "none";
            }
        };
        Action truncatemethod1action = new AbstractAction("simple cos-squared") {
            public void actionPerformed(ActionEvent evt) {
                ctscanner.truncmethod = "simple cos-squared";
            }
        };
        Action truncatemethod2action = new AbstractAction("adaptive cos-squared") {
            public void actionPerformed(ActionEvent evt) {
                ctscanner.truncmethod = "adaptive cos-squared";
            }
        };
        Action truncatemethod5action = new AbstractAction("simple square root") {
            public void actionPerformed(ActionEvent evt) {
                ctscanner.truncmethod = "simple square root";
            }
        };
        Action truncatemethod3action = new AbstractAction("vangompel") {
            public void actionPerformed(ActionEvent evt) {
                ctscanner.truncmethod = "vangompel";
            }
        };
        Action truncatemethod4action = new AbstractAction("HDT") {
            public void actionPerformed(ActionEvent evt) {
                ctscanner.truncmethod = "HDT";
            }
        };
        
        JMenu truncatemethodMenu = new JMenu("Method");
        JRadioButtonMenuItem truncatemethod0Button = new JRadioButtonMenuItem(truncatemethod0action);
        truncatemethod0Button.setSelected(false);
        JRadioButtonMenuItem truncatemethod1Button = new JRadioButtonMenuItem(truncatemethod1action);
        truncatemethod1Button.setSelected(true);
        JRadioButtonMenuItem truncatemethod2Button = new JRadioButtonMenuItem(truncatemethod2action);
        truncatemethod2Button.setSelected(false);
        JRadioButtonMenuItem truncatemethod5Button = new JRadioButtonMenuItem(truncatemethod5action);
        truncatemethod5Button.setSelected(false);
        JRadioButtonMenuItem truncatemethod3Button = new JRadioButtonMenuItem(truncatemethod3action);
        truncatemethod3Button.setSelected(false);
        JRadioButtonMenuItem truncatemethod4Button = new JRadioButtonMenuItem(truncatemethod4action);
        truncatemethod4Button.setSelected(false);
        ButtonGroup truncgrp = new ButtonGroup();
        truncgrp.add(truncatemethod0Button);
        truncgrp.add(truncatemethod1Button);
        truncgrp.add(truncatemethod2Button);
        truncgrp.add(truncatemethod3Button);
        truncgrp.add(truncatemethod4Button);
        truncgrp.add(truncatemethod5Button);
        truncatemethodMenu.add(truncatemethod0Button);
        truncatemethodMenu.add(truncatemethod1Button);
        truncatemethodMenu.add(truncatemethod2Button);
        truncatemethodMenu.add(truncatemethod3Button);
        truncatemethodMenu.add(truncatemethod4Button);
        truncatemethodMenu.add(truncatemethod5Button);
        ROIMenu.add(truncatecheckbox);
        ROIMenu.add(ROIcropcheckbox);
        ROIMenu.add(ROIcirclecheckbox);
        ROIMenu.add(settruncatewidthButton);
        ROIMenu.add(setfactorButton);
        ROIMenu.add(setedgezerocheckbox);
        ROIMenu.add(keepcurrentextrapwidthscheckbox);
        ROIMenu.add(new JSeparator());
        ROIMenu.add(truncatemethodMenu);
        
        JMenu AnimateMenu = new JMenu("Animate");
        animatecheckbox = new JCheckBoxMenuItem("Animate Back Proj.");
        if (ctscanner.animate == true){
            animatecheckbox.setSelected(true);
        }
        animatecheckbox.addItemListener(this);
        AnimateMenu.add(animatecheckbox);
        
        rootMenu.add(ReconstrMethodMenu);
        rootMenu.add(ProjSpeedMenu);
        rootMenu.add(FilterMenu);
        rootMenu.add(InterpMenu);
        rootMenu.add(NoiseMenu);
        rootMenu.add(ROIMenu);
        rootMenu.add(AnimateMenu);
    }
    
    private void createPhantomMenu(JMenu rootMenu){
        //set up actions first
        Action phantomrasterizeAction = new AbstractAction("Phantom Image") {
            public void actionPerformed(ActionEvent evt) {
                //run this action in it's own thread
                final SwingWorker worker = new SwingWorker() {
                    public Object construct() {
                        InternalImageFrame frame = new InternalImageFrame();
                        BufferedImage phtimg = ctscanner.CreatePhantomRaster();
                        Container container = frame.getContentPane();
                        ImagePanel panel = new ImagePanel();
                        panel.loadBufferedImage(phtimg);
                        frame.PANELTYPE = 2;
                        container.add(panel, BorderLayout.CENTER);
                        frame.imagepanel = panel;
                        frame.pack(); frame.setVisible( true );
                        theDesktop.add( frame );
                        try {
                            frame.setSelected(true);
                        } catch (java.beans.PropertyVetoException s) {}
                        return phtimg;
                    }
                    public void finished() {
                        
                    }
                };
                worker.start();
            }
        };
        
        Action shepplogphantomAction = new AbstractAction("SheppLogan") {
            public void actionPerformed(ActionEvent evt) {
                ctscanner.phantomname = "sheppl";
            }
        };
        Action test1phantomAction = new AbstractAction("Test Object1") {
            public void actionPerformed(ActionEvent evt) {
                ctscanner.phantomname = "test1";
            }
        };
        Action test2phantomAction = new AbstractAction("Test Object2") {
            public void actionPerformed(ActionEvent evt) {
                ctscanner.phantomname = "test2";
            }
        };
        Action test3phantomAction = new AbstractAction("Test Object3") {
            public void actionPerformed(ActionEvent evt) {
                ctscanner.phantomname = "test3";
            }
        };
        Action test4phantomAction = new AbstractAction("Test Object4") {
            public void actionPerformed(ActionEvent evt) {
                ctscanner.phantomname = "test4";
            }
        };
        Action deltaphantomAction = new AbstractAction("Delta Func.") {
            public void actionPerformed(ActionEvent evt) {
                ctscanner.phantomname = "delta";
            }
        };
        Action dotsphantomAction = new AbstractAction("Bead") {
            public void actionPerformed(ActionEvent evt) {
                ctscanner.phantomname = "bead";
            }
        };
        Action barphantomAction = new AbstractAction("Bars") {
            public void actionPerformed(ActionEvent evt) {
                ctscanner.phantomname = "bars";
            }
        };
        
        JMenu selectPhantomMenu = new JMenu("Select");
        JRadioButtonMenuItem shepplogphantomButton = new JRadioButtonMenuItem(shepplogphantomAction);
        shepplogphantomButton.setSelected(true);
        JRadioButtonMenuItem test1phantomButton = new JRadioButtonMenuItem(test1phantomAction);
        test1phantomButton.setSelected(false);
        JRadioButtonMenuItem test2phantomButton = new JRadioButtonMenuItem(test2phantomAction);
        test2phantomButton.setSelected(false);
        JRadioButtonMenuItem test3phantomButton = new JRadioButtonMenuItem(test3phantomAction);
        test3phantomButton.setSelected(false);
        JRadioButtonMenuItem test4phantomButton = new JRadioButtonMenuItem(test4phantomAction);
        test4phantomButton.setSelected(false);
        JRadioButtonMenuItem deltaphantomButton = new JRadioButtonMenuItem(deltaphantomAction);
        deltaphantomButton.setSelected(false);
        JRadioButtonMenuItem dotsphantomButton = new JRadioButtonMenuItem(dotsphantomAction);
        dotsphantomButton.setSelected(false);
        JRadioButtonMenuItem barphantomButton = new JRadioButtonMenuItem(barphantomAction);
        barphantomButton.setSelected(false);
        selectPhantomMenu.add(shepplogphantomButton);
        selectPhantomMenu.add(test1phantomButton);
        selectPhantomMenu.add(test2phantomButton);
        selectPhantomMenu.add(test3phantomButton);
        selectPhantomMenu.add(test4phantomButton);
        selectPhantomMenu.add(deltaphantomButton);
        selectPhantomMenu.add(dotsphantomButton);
        selectPhantomMenu.add(barphantomButton);
        ButtonGroup groupph = new ButtonGroup();
        groupph.add(shepplogphantomButton);
        groupph.add(test1phantomButton);
        groupph.add(test2phantomButton);
        groupph.add(test3phantomButton);
        groupph.add(test4phantomButton);
        groupph.add(deltaphantomButton);
        groupph.add(dotsphantomButton);
        groupph.add(barphantomButton);
        rootMenu.add(selectPhantomMenu);
        
        JMenuItem projectphantomButton = new JMenuItem(projPhantomAction);
        rootMenu.add(projectphantomButton);
        JMenuItem phantomrasterizeButton = new JMenuItem(phantomrasterizeAction);
        rootMenu.add(phantomrasterizeButton);
        
        rootMenu.add(new JSeparator());
        JMenuItem drawCustomPhantomButton = new JMenuItem(drawCustomPhantomAction);
        rootMenu.add(drawCustomPhantomButton);
        
    }
    
    // Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createIcon(String imageName) {
        String imgLocation = "images/"
                + imageName
                + ".png";
        java.net.URL imageURL = MyApp.class.getResource(imgLocation);
        
        if (imageURL == null) {
            // System.err.println("Resource not found: "
            //                  + imgLocation);
            return null;
        } else {
            return new ImageIcon(imageURL);
        }
    }
    
    
    private void createimageToolBar() {
        JButton button = null;
        
        //Create the toolbar.
        imagetoolBar = new JToolBar("Image Tools", JToolBar.HORIZONTAL);
        
        button = new JButton(openimageAction);
        if (button.getIcon() != null) {
            button.setText(""); //an icon-only button
        }
        imagetoolBar.add(button);
        
        //second button
        button = new JButton(saveimageAction);
        if (button.getIcon() != null) {
            button.setText(""); //an icon-only button
        }
        imagetoolBar.add(button);
        
        button = new JButton(blurAction);
        if (button.getIcon() != null) {
            button.setText(""); //an icon-only button
        }
        imagetoolBar.add(button);
        
        //second button
        button = new JButton(sharpenAction);
        if (button.getIcon() != null) {
            button.setText(""); //an icon-only button
        }
        imagetoolBar.add(button);
        
        button = new JButton(invertAction);
        if (button.getIcon() != null) {
            button.setText(""); //an icon-only button
        }
        imagetoolBar.add(button);
        
        button = new JButton(brightenAction);
        if (button.getIcon() != null) {
            button.setText(""); //an icon-only button
        }
        imagetoolBar.add(button);
        
        button = new JButton(darkenAction);
        if (button.getIcon() != null) {
            button.setText(""); //an icon-only button
        }
        imagetoolBar.add(button);
        
        button = new JButton(greyscaleAction);
        if (button.getIcon() != null) {
            button.setText(""); //an icon-only button
        }
        imagetoolBar.add(button);
        button = new JButton(scaleAction);
        if (button.getIcon() != null) {
            button.setText(""); //an icon-only button
        }
        imagetoolBar.add(button);
        
        imagetoolBar.addSeparator();
        
        button = new JButton(addimagesAction);
        if (button.getIcon() != null) {
            button.setText("");
        }
        imagetoolBar.add(button);
        button = new JButton(subimagesAction);
        if (button.getIcon() != null) {
            button.setText("");
        }
        imagetoolBar.add(button);
        button = new JButton(divimagesAction);
        if (button.getIcon() != null) {
            button.setText("");
        }
        
        imagetoolBar.add(button);
        button = new JButton(showhistogramAction);
        if (button.getIcon() != null) {
            button.setText("");
        }
        
        imagetoolBar.add(button);
        
        button = new JButton("gauss");
        if (button.getIcon() != null) {
            button.setText("gauss");
            Icon icon = new ImageIcon("gauss.png");
            button.setIcon(icon);
        }
        button.addActionListener(  new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                InternalImageFrame frame = new InternalImageFrame();
                Container container = frame.getContentPane();
                ImagePanel panel = new ImagePanel();
                double[][] temp = ctscanner.Create2DGaussian(256);
                BufferedImage img = ctscanner.CreateImagefromArray(temp, Utils.getMax(temp),0);
                panel.loadBufferedImage(img);
                frame.PANELTYPE = 2;
                container.add(panel, BorderLayout.CENTER);
                frame.imagepanel = panel;
                frame.pack(); frame.setVisible( true );
                theDesktop.add( frame );
                try {
                    frame.setSelected(true);
                } catch (java.beans.PropertyVetoException s) {}
            }
        });
        imagetoolBar.add(button);
        
        button = new JButton("Apply");
        if (button.getIcon() != null) {
            button.setText("apply");
            Icon icon = new ImageIcon("gauss.png");
            button.setIcon(icon);
        }
        button.addActionListener(  new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                JInternalFrame frame = theDesktop.getSelectedFrame();
                String cls = frame.getClass().getName(); //System.out.println(cls);
                if (cls == "myctapp.InternalImageFrame") {
                    InternalImageFrame currframe = (InternalImageFrame)frame;
                    currframe.imagepanel.ApplyChanges();
                }
            }
        });
        imagetoolBar.add(button);
        
    }
    
    
    private void createtextfieldPanel(){
        textfieldPanel = new JPanel();
        //textfieldPanel.setPreferredSize(new Dimension(100, 140));
        //textfieldPanel.setLayout(new GridLayout(5, 2));
        SpringLayout layout = new SpringLayout();
        textfieldPanel.setLayout(layout);
        //set up text fields for user input
        scansFormat = NumberFormat.getNumberInstance();
        scansLabel = new JLabel(scansString);
        scansLabel.setFont(theMainFont);
        scansField = new JFormattedTextField(scansFormat);
        scansField.setValue(new Integer(ctscanner.scans));
        scansField.setColumns(3);
        scansField.addPropertyChangeListener("value", this);
        scansLabel.setLabelFor(scansField);
        textfieldPanel.add(scansLabel);
        textfieldPanel.add(scansField);
        
        stepsizeFormat = NumberFormat.getNumberInstance();
        stepsizeLabel = new JLabel(stepsizeString);
        stepsizeLabel.setFont(theMainFont);
        stepsizeField = new JFormattedTextField(stepsizeFormat);
        stepsizeField.setValue(new Float(ctscanner.stepsize));
        stepsizeField.setColumns(3);
        stepsizeField.addPropertyChangeListener("value", this);
        stepsizeLabel.setLabelFor(stepsizeField);
        textfieldPanel.add(stepsizeLabel);
        textfieldPanel.add(stepsizeField);
        
        viewsFormat = NumberFormat.getNumberInstance();
        viewsLabel = new JLabel(viewsString);
        viewsLabel.setFont(theMainFont);
        viewsField = new JFormattedTextField(viewsFormat);
        viewsField.setValue(new Integer(ctscanner.views));
        viewsField.setColumns(3);
        viewsField.setEditable(false);
        viewsField.addPropertyChangeListener("value", this);
        viewsLabel.setLabelFor(viewsField);
        textfieldPanel.add(viewsLabel);
        textfieldPanel.add(viewsField);
        
        imgsizeFormat = NumberFormat.getNumberInstance();
        imgsizeLabel = new JLabel(imgsizeString);
        imgsizeLabel.setFont(theMainFont);
        imgsizeField = new JFormattedTextField(imgsizeFormat);
        imgsizeField.setValue(new Integer(ctscanner.outputimgsize));
        imgsizeField.setColumns(3);
        imgsizeField.addPropertyChangeListener("value", this);
        imgsizeLabel.setLabelFor(imgsizeField);
        textfieldPanel.add(imgsizeLabel);
        textfieldPanel.add(imgsizeField);
        
        zoomFormat = NumberFormat.getNumberInstance();
        zoomLabel = new JLabel(zoomString);
        zoomLabel.setFont(theMainFont);
        zoomField = new JFormattedTextField(zoomFormat);
        zoomField.setValue(new Double(ctscanner.zoom));
        zoomField.setColumns(3);
        zoomField.addPropertyChangeListener("value", this);
        zoomLabel.setLabelFor(zoomField);
        textfieldPanel.add(zoomLabel);
        textfieldPanel.add(zoomField);
        
        offsetFormat = NumberFormat.getNumberInstance();
        xoffsetLabel = new JLabel(xoffsetString);
        xoffsetLabel.setFont(smallFont);
        xoffsetField = new JFormattedTextField(offsetFormat);
        xoffsetField.setFont(smallFont);
        xoffsetField.setValue(new Double(ctscanner.xoffset));
        xoffsetField.setColumns(3);
        xoffsetField.addPropertyChangeListener("value", this);
        xoffsetLabel.setLabelFor(xoffsetField);
        yoffsetLabel = new JLabel(yoffsetString);
        yoffsetLabel.setFont(smallFont);
        yoffsetField = new JFormattedTextField(offsetFormat);
        yoffsetField.setFont(smallFont);
        yoffsetField.setValue(new Double(ctscanner.yoffset));
        yoffsetField.setColumns(3);
        yoffsetField.addPropertyChangeListener("value", this);
        yoffsetLabel.setLabelFor(yoffsetField);
        
        textfieldPanel.add(xoffsetLabel);
        textfieldPanel.add(yoffsetLabel);
        textfieldPanel.add(xoffsetField);
        textfieldPanel.add(yoffsetField);
        
        SpringUtilities.makeCompactGrid(textfieldPanel,
                7, 2, //rows, cols
                2, 2, //initialX, initialY
                2, 2);//xPad, yPad
    }
    
    //creates custom sliders for performing windowing & thresholding ops
    private void createSliders(){
        
        int n = 2;
        mSlider1 = new MThumbSlider(n);
        mSlider1.setOrientation(JSlider.VERTICAL);
        mSlider1.setMinimum(0);
        mSlider1.setMaximum(2000);
        mSlider1.setValueAt(0, 0);
        mSlider1.setValueAt(2000, 1);
        //mSlider1.setPaintLabels(true);
        mSlider1.setPaintTicks(true);
        mSlider1.setMajorTickSpacing(512);
        mSlider1.setMinorTickSpacing(128);
        mSlider1.setFillColorAt(Color.black,  0);
        mSlider1.setFillColorAt(new Color(103,104,156), 1);
        mSlider1.setTrackFillColor(Color.white);
        mSlider1.putClientProperty( "JSlider.isFilled", Boolean.TRUE );
        
        mSlider1.addChangeListener(new ChangeListener() {
            // This method is called whenever the slider's value is changed
            public void stateChanged(ChangeEvent evt) {
                MThumbSlider mSlider = (MThumbSlider)evt.getSource();
                
                if (!mSlider.getValueIsAdjusting()) {
                    // Get new value
                    int value1 = mSlider.getValueAt(0);
                    int value2 = mSlider.getValueAt(1);
                    System.out.println("val1: "+value1);
                    System.out.println("val2: "+value2);
                    try{
                        InternalImageFrame currframe = (InternalImageFrame)theDesktop.getSelectedFrame();
                        currframe.imagepanel.lowerwinlvl = value1;
                        currframe.imagepanel.upperwinlvl = value2;
                        lowerlevelField.setValue(new Integer(value1));
                        upperlevelField.setValue(new Integer(value2));
                        currframe.imagepanel.PerformWindowing();
                        
                    } catch(NullPointerException s){}
                }
            }
        });
        
        //gradientPanel = new GradientPanel();
        sliderPanel = new JPanel();
        sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.X_AXIS));
        //sliderPanel.setLayout(new FlowLayout());
        sliderPanel.setBorder(new EtchedBorder());
        sliderPanel.setPreferredSize(new Dimension(50, 155));
        sliderPanel.setMaximumSize(new Dimension(50, 155));
        //gradientPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
        mSlider1.setAlignmentX(Component.LEFT_ALIGNMENT);
        mSlider1.setPreferredSize(new Dimension(45, 145));
        mSlider1.setMinimumSize(new Dimension(40, 145));
        mSlider1.setMaximumSize(sliderPanel.getPreferredSize());
        sliderPanel.add(mSlider1);
        //sliderPanel.add(gradientPanel);
        
        
    }
    
    private void createwindowingPanel(){
        winPanel = new JPanel();
        int w1,w2;
        if (osname.matches("Mac OS X")){
            w1=60; w2=10;
        } else{
            w1=40; w2=25;
        }
        winPanel.setPreferredSize(new Dimension(w1,155));
        SpringLayout layout = new SpringLayout();
        winPanel.setLayout(layout);
        JPanel thumbPanel = new JPanel();
        winPanel.add(thumbPanel);
        thumbPanel.setPreferredSize(new Dimension(35,25));
        JLabel templabel = new JLabel("depth:");
        templabel.setFont(smallFont);
        winPanel.add(templabel);
        
        NumberFormat numFormat = NumberFormat.getNumberInstance();
        bitdepthField = new JFormattedTextField(numFormat);
        bitdepthField.setPreferredSize(new Dimension(10,8));
        bitdepthField.setFont(smallFont);
        bitdepthField.setValue(new Integer(0));
        bitdepthField.setColumns(2);
        bitdepthField.setEditable(false);
        bitdepthField.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        //bitdepthField.addPropertyChangeListener("value", this);
        winPanel.add(bitdepthField);
        
        //set up text fields
        
        upperlevelFormat = NumberFormat.getNumberInstance();
        upperlevelLabel = new JLabel("upper");
        upperlevelLabel.setFont(smallFont);
        upperlevelField = new JFormattedTextField(upperlevelFormat);
        upperlevelField.setValue(new Integer(2000));
        upperlevelField.setEditable(false);
        upperlevelField.setColumns(4);
        upperlevelField.setFont(smallFont);
        upperlevelField.setBackground(new Color(255,255,155));
        upperlevelField.addPropertyChangeListener("value", this);
        upperlevelLabel.setLabelFor(upperlevelField);
        winPanel.add(upperlevelLabel);
        winPanel.add(upperlevelField);
        lowerlevelFormat = NumberFormat.getNumberInstance();
        lowerlevelLabel = new JLabel("lower");
        lowerlevelLabel.setFont(smallFont);
        lowerlevelField = new JFormattedTextField(lowerlevelFormat);
        lowerlevelField.setValue(new Integer(0));
        lowerlevelField.setEditable(false);
        lowerlevelField.setColumns(4);
        lowerlevelField.setFont(smallFont);
        lowerlevelField.setBackground(new Color(255,255,155));
        lowerlevelField.addPropertyChangeListener("value", this);
        lowerlevelLabel.setLabelFor(lowerlevelField);
        winPanel.add(lowerlevelLabel);
        winPanel.add(lowerlevelField);
        
        /*JButton setwinlvlbutton = new JButton("set");
        setwinlvlbutton.setMargin(new Insets (0,0,2,0));
        setwinlvlbutton.setPreferredSize(new Dimension(25, 22));
        winPanel.add(setwinlvlbutton);
        setwinlvlbutton.addActionListener(  new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try{
                    InternalImageFrame currframe = (InternalImageFrame)theDesktop.getSelectedFrame();
                    currframe.imagepanel.lowerwinlvl = lower;
                    currframe.imagepanel.upperwinlvl = upper;
                    currframe.imagepanel.PerformWindowing();
                }
                catch(NullPointerException s){};
            }
        });*/
        
        JButton resetwinlvlbutton = new JButton("reset");
        resetwinlvlbutton.setMargin(new Insets(0,0,2,2));
        resetwinlvlbutton.setSize(new Dimension(w2, 22));
        winPanel.add(resetwinlvlbutton);
        resetwinlvlbutton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try{
                    InternalImageFrame currframe = (InternalImageFrame)theDesktop.getSelectedFrame();
                    currframe.imagepanel.resetWindowLevels();
                    currframe.imagepanel.PerformWindowing();
                    if (currframe.imagepanel.getStackSize() > 1 )
                        currframe.imagepanel.windowing = false;
                    if (currframe.imagepanel.getImageType() == 11){
                        lowerlevelField.setValue(new Integer(0));
                        upperlevelField.setValue(new Integer(2000));
                        mSlider1.setMaximum(2000);
                    } else {
                        lowerlevelField.setValue(new Integer(0));
                        upperlevelField.setValue(new Integer(255));
                        mSlider1.setMaximum(255);
                    }
                    
                    //mSlider1.setValueAt(0,0);
                    //mSlider1.setValueAt(4095, 1);
                    mSlider1.ResetThumbs();
                    mSlider1.repaint();
                    
                } catch(NullPointerException s){};
            }
        });
        
        SpringUtilities.makeCompactGrid(winPanel,
                8, 1, //rows, cols
                2, 2, //initialX, initialY
                1, 1);//xPad, yPad
        
        windowingPanel = new JPanel();
        windowingPanel.setLayout(new BorderLayout());
        windowingPanel.setBorder(BorderFactory.createTitledBorder("Window Lvl"));
        createSliders();
        windowingPanel.add(sliderPanel,BorderLayout.WEST);
        windowingPanel.add(winPanel,BorderLayout.EAST);
    }
    
    
    private void createInfoPanel(){
        
        ImagePanel.imageinfopanel = new InfoPanel();
        imginfoPanel = ImagePanel.imageinfopanel;
    }
    
    private void createwidgetPanel(){
        int w1,h1;
        Font theMainFont = new Font("SansSerif", Font.PLAIN, 11);
        UIManager.put("Button.font", theMainFont );
        UIManager.put("Label.font", theMainFont );
        UIManager.put("JCheckBox.font", theMainFont );
        //create control panel and add buttons
        widgetPanel = new JPanel();
        if (osname.matches("Mac OS X")){
            w1=120; h1=30;
        } else{
            w1=100; h1=26;
        }
        widgetPanel.setPreferredSize(new Dimension(w1, 300));
        FlowLayout thelayout = new FlowLayout(FlowLayout.CENTER,2,2);
        widgetPanel.setLayout(thelayout);
        //JButton loadButton = new JButton("New Phantom");
        //loadButton.setPreferredSize(new Dimension(100, 25));
        //loadButton.setFont(theMainFont);
        JButton projphantomButton = new JButton(projPhantomAction);
        projphantomButton.setPreferredSize(new Dimension(100, h1));
        projphantomButton.setMargin(new Insets(0,0,2,0));
        JButton openimagebutton = new JButton(openimageAction);
        openimagebutton.setPreferredSize(new Dimension(100, h1));
        openimagebutton.setMargin(new Insets(0,0,2,0));
        JButton createsinogramButton = new JButton(createSinogramAction);
        createsinogramButton.setPreferredSize(new Dimension(100, h1));
        createsinogramButton.setMargin(new Insets(0,0,2,0));
        JButton loadsinogramButton = new JButton(loadSinogramAction);
        loadsinogramButton.setPreferredSize(new Dimension(100, h1));
        loadsinogramButton.setMargin(new Insets(0,0,2,0));
        JButton createBPImageButton = new JButton(createBPImageAction);
        createBPImageButton.setPreferredSize(new Dimension(100, h1));
        createBPImageButton.setMargin(new Insets(0,0,2,0));
        JButton createProjfromImageButton = new JButton(createProjfromImageAction);
        createProjfromImageButton.setPreferredSize(new Dimension(100, h1));
        createProjfromImageButton.setMargin(new Insets(0,0,2,0));
        JButton displayprojButton = new JButton("Visualize");
        displayprojButton.setPreferredSize(new Dimension(100, h1));
        JButton quitButton = new JButton(quitAction);
        quitButton.setPreferredSize(new Dimension(100, 30));
        
        displayprojButton.addActionListener(  new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                GraphicsFrame graphframe = new GraphicsFrame(ctscanner, 0);
                theDesktop.add( graphframe );
                try {
                    graphframe.setSelected(true);
                } catch (java.beans.PropertyVetoException s) {}
            }
        });
        
        
        /* set up listener for new phantom display button
        loadButton.addActionListener(  new ActionListener() {
           public void actionPerformed(ActionEvent event) {
                 InternalImageFrame frame = new InternalImageFrame(null, 1, ctscanner);
                 theDesktop.add( frame );
                 try {
                   frame.setSelected(true);
                 } catch (java.beans.PropertyVetoException s) {}
         
             }
          }); */
        
        //widgetPanel.add(loadButton);
        widgetPanel.add(projphantomButton);
        widgetPanel.add(openimagebutton);
        widgetPanel.add(createsinogramButton);
        widgetPanel.add(loadsinogramButton);
        widgetPanel.add(createBPImageButton);
        widgetPanel.add(createProjfromImageButton);
        widgetPanel.add(displayprojButton);
        
        createtextfieldPanel();
        widgetPanel.add(textfieldPanel);
        createwindowingPanel();
        widgetPanel.add(windowingPanel);
        createInfoPanel();
        widgetPanel.add(imginfoPanel);
        
        widgetPanel.add(quitButton);
        
        // create memory monitor - runs in it's own thread
        //final MemoryMonitor memmonitor = new MemoryMonitor();
        //memmonitor.surf.start();
        //widgetPanel.add(memmonitor);
    }
    
    
    private void createGUI(){
        
        ctscanner = new CTScanner();
        //Create the main desktop pane to display windows in
        theDesktop = new MDIDesktopPane();
        theDesktop.setBackground(new Color(102,100,165));
        frameListener = new FrameListener();
        
        scrollPane = new JScrollPane();
        scrollPane.getViewport().add(theDesktop);
        getContentPane().add(scrollPane);
        
        //Set the menu and tool bars
        // and add the panels to the content pane
        this.setJMenuBar(createMenuBar());
        createwidgetPanel();
        getContentPane().add(widgetPanel, BorderLayout.LINE_END);
        createimageToolBar();
        getContentPane().add(imagetoolBar, BorderLayout.PAGE_END);
    }
    
    // constructor for the application class
    
    public MyApp() {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //when window is closed application exits
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                
                //create image processing operations, actions, menu bar and GUI
                createActions();
                //createMenuBar();
                createGUI();
                
                //Display the window
                pack();
                setVisible(true);
                setTitle("LearnCT");
            }
        });
    }
    
    class CopySelectionAction extends AbstractAction {
        public CopySelectionAction(String text, ImageIcon icon,
                String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        
        public void actionPerformed(ActionEvent e) {
            InternalImageFrame frame1 = (InternalImageFrame)theDesktop.getSelectedFrame();
            //if (frame1.PANELTYPE >= 2) {
            if (frame1.imagepanel.pixels == null) frame1.imagepanel.setPixelData();
            clipboardImage = frame1.imagepanel.getImageSelection();
            clipboarddata = frame1.imagepanel.getSelectionData();
            //}
        }
    }
    
    class PasteasImageAction extends AbstractAction {
        public PasteasImageAction(String text, ImageIcon icon,
                String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        
        public void actionPerformed(ActionEvent e) {
            InternalImageFrame frame = new InternalImageFrame();
            Container container = frame.getContentPane();
            ImagePanel panel = new ImagePanel();
            panel.loadBufferedImage(clipboardImage);
            panel.PerformWindowing();
            //panel.windowing = false;
            frame.PANELTYPE = 2;
            container.add(panel, BorderLayout.CENTER);
            frame.imagepanel = panel;
            frame.pack(); frame.setVisible( true );
            theDesktop.add( frame );
            try {
                frame.setSelected(true);
            } catch (java.beans.PropertyVetoException s) {}
            
            
        }
    }
    
    class LoadPhantomAction extends AbstractAction {
        public LoadPhantomAction(String text, ImageIcon icon,
                String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        
        public void actionPerformed(ActionEvent e) {
            
            FileDialog fd = new FileDialog(MyApp.this);
            fd.setVisible(true);
            if (fd.getFile() == null) return;
            String path = fd.getDirectory() + fd.getFile();
            InternalImageFrame frame = new InternalImageFrame(path, 2, ctscanner);
            theDesktop.add( frame );
            try {
                frame.setSelected(true);
            } catch (java.beans.PropertyVetoException s) {}
            
        }
    }
    
    class SavePhantomAction extends AbstractAction {
        public SavePhantomAction(String text, ImageIcon icon,
                String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        public void actionPerformed(ActionEvent e) {
            try {
                InternalDrawFrame currframe = (InternalDrawFrame)theDesktop.getSelectedFrame();
                
                FileDialog fd = new FileDialog(MyApp.this, "Save Phantom", FileDialog.SAVE);
                fd.setVisible(true);
                if (fd.getFile() == null) return;
                String path = fd.getDirectory() + fd.getFile();
                currframe.drawpanel.savePhant(path);
            } catch (ClassCastException c){};
            
            //String filename = currframe.getTitle();
            //currframe.drawpanel.savePhant(filename+".pht");
            
        }
        
    }
    
    
    class ProjPhantomAction extends AbstractAction {
        public ProjPhantomAction(String text,
                String desc, Integer mnemonic) {
            super(text);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        
        public void actionPerformed(ActionEvent e) {
            
            InternalImageFrame frame = new InternalImageFrame(null, 6, ctscanner);
            frame.addInternalFrameListener(frameListener);
            
            scansField.setValue(new Integer(ctscanner.scans));
            ctscanner.resetviews();
            viewsField.setValue(new Integer(ctscanner.views));
            
            theDesktop.add( frame );
            //reset window levels
            //lowerlevelField.setValue(new Integer(0));
            //upperlevelField.setValue(new Integer(4095));
            winPanel.repaint();
            try {
                frame.setSelected(true);
            } catch (java.beans.PropertyVetoException s) {}
            
        }
    }
    
    class LoadProjAction extends AbstractAction {
        public LoadProjAction(String text, ImageIcon icon,
                String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        
        public void actionPerformed(ActionEvent e) {
            FileDialog fd = new FileDialog(MyApp.this);
            fd.setVisible(true);
            if (fd.getFile() == null) return;
            String path = fd.getDirectory() + fd.getFile();
            scansField.setValue(new Integer(ctscanner.scans));
            viewsField.setValue(new Integer(ctscanner.views));
            ctscanner.loadProjectionsFile(path);
        }
    }
    
    class SaveProjAction extends AbstractAction {
        public SaveProjAction(String text, ImageIcon icon,
                String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        public void actionPerformed(ActionEvent e) {
            FileDialog fd = new FileDialog(MyApp.this, "Save Phantom", FileDialog.SAVE);
            fd.setVisible(true);
            if (fd.getFile() == null) return;
            String path = fd.getDirectory() + fd.getFile();
            ctscanner.saveProjectionsFile(path);
        }
    }
    
    class ImportProjAction extends AbstractAction {
        public ImportProjAction(String text, ImageIcon icon,
                String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        
        public void actionPerformed(ActionEvent e) {
            FileDialog fd = new FileDialog(MyApp.this);
            fd.setVisible(true);
            if (fd.getFile() == null) return;
            String path = fd.getDirectory() + fd.getFile();
            Opener op = new Opener();
            ImagePlus imp = op.openImage(path);
            
            CustomDialog importdialog = new CustomDialog(MyApp.this, true, "Data bins in cols or rows?");
            boolean incols = importdialog.getAnswer();
            ctscanner.importProjections(imp, incols, 1);
            scansField.setValue(new Integer(ctscanner.scans));
            stepsizeField.setValue(new Float(ctscanner.stepsize));
            viewsField.setValue(new Integer(ctscanner.views));
            InternalImageFrame frame = new InternalImageFrame(null, 3, ctscanner);
            theDesktop.add( frame );
            try {
                frame.setSelected(true);
            } catch (java.beans.PropertyVetoException s) {}
        }
    }
    
    class DrawCustomPhantomAction extends AbstractAction {
        public DrawCustomPhantomAction(String text, ImageIcon icon,
                String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        
        public void actionPerformed(ActionEvent e) {
            InternalDrawFrame frame = new InternalDrawFrame();
            theDesktop.add( frame );
            try {
                frame.setSelected(true);
            } catch (java.beans.PropertyVetoException s) {}
        }
    }
    
    /*class ExportProjAction extends AbstractAction {
        public ExportProjAction(String text, ImageIcon icon,
                String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        public void actionPerformed(ActionEvent e) {
            FileDialog fd = new FileDialog(MyApp.this, "Export Data", FileDialog.SAVE);
            fd.setVisible(true);
            if (fd.getFile() == null) return;
            String path = fd.getDirectory() + fd.getFile();
            ctscanner.exportProjectionsFile(path);
        }
    } */
    
    class CreateSinogramAction extends AbstractAction {
        public CreateSinogramAction(String text,
                String desc, Integer mnemonic) {
            super(text);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        
        public void actionPerformed(ActionEvent e) {
            InternalImageFrame frame = new InternalImageFrame(null, 3, ctscanner);
            scansField.setValue(new Integer(ctscanner.scans));
            viewsField.setValue(new Integer(ctscanner.views));
            theDesktop.add( frame );
            try {
                frame.setSelected(true);
            } catch (java.beans.PropertyVetoException s) {}
        }
    }
    
    class LoadSinogramAction extends AbstractAction {
        public LoadSinogramAction(String text,
                String desc, Integer mnemonic) {
            super(text);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        
        public void actionPerformed(ActionEvent e) {
            
            FileDialog fd = new FileDialog(MyApp.this);
            fd.setVisible(true);
            if (fd.getFile() == null) return;
            String path = fd.getDirectory() + fd.getFile();
            InternalImageFrame frame = new InternalImageFrame(path, 3, ctscanner);
            scansField.setValue(new Integer(ctscanner.scans));
            viewsField.setValue(new Integer(ctscanner.views));
            theDesktop.add( frame );
            try {
                frame.setSelected(true);
            } catch (java.beans.PropertyVetoException s) {}
        }
    }
    
    class CreateBPImageAction extends AbstractAction {
        public CreateBPImageAction(String text,
                String desc, Integer mnemonic) {
            super(text);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        
        public void actionPerformed(ActionEvent e) {
            
            InternalImageFrame frame = new InternalImageFrame(null, 4,ctscanner);
            frame.addInternalFrameListener(frameListener);
            scansField.setValue(new Integer(ctscanner.scans));
            if (ctscanner.projection == null){
                stepsizeField.setValue(new Float(ctscanner.stepsize));
                viewsField.setValue(new Integer(ctscanner.views));
            }
            theDesktop.add( frame );
            try {
                frame.setSelected(true);
            } catch (java.beans.PropertyVetoException s) {}
            
        }
    }
    
    class CreateProjfromImageAction extends AbstractAction {
        public CreateProjfromImageAction(String text,
                String desc, Integer mnemonic) {
            super(text);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        
        public void actionPerformed(ActionEvent e) {
            int choice = 1;
            InternalImageFrame newframe = new InternalImageFrame();
            if (theDesktop.getSelectedFrame() != null){
                choice = JOptionPane.showInternalOptionDialog(theDesktop,
                        "Reproject Current Image?", "Reprojection",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                        null, null, null);
                //System.out.println("CHOICE="+choice);
                if (choice == 0){
                    try {
                        JInternalFrame frame = theDesktop.getSelectedFrame();
                        String cls = frame.getClass().getName(); //System.out.println(cls);
                        if (cls == "myctapp.InternalImageFrame") {
                            InternalImageFrame currframe = (InternalImageFrame)frame;
                            newframe = currframe.CreateCopy();
                            
                            BufferedImage img = currframe.imagepanel.getBufferedImage();
                            if (currframe.imagepanel.getStackSize() > 1){
                                newframe.imagepanel.ReprojectImage(ctscanner);
                                newframe.AddPanelsandDisplay();
                            } else {
                                newframe.imagepanel.ReprojectImage(ctscanner);
                                newframe.AddPanelsandDisplay();
                            }
                        }
                        
                    } catch (NullPointerException n){};
                }
            }
            if(theDesktop.getSelectedFrame() == null || choice == 1){
                
                FileDialog fd = new FileDialog(MyApp.this);
                fd.setVisible(true);
                if (fd.getFile() == null) return;
                String path = fd.getDirectory() + fd.getFile();
                newframe = new InternalImageFrame(path, 5, ctscanner);
            }
            scansField.setValue(new Integer(ctscanner.scans));
            ctscanner.resetviews();
            viewsField.setValue(new Integer(ctscanner.views));
            
            theDesktop.add( newframe );
            try {
                newframe.setSelected(true);
            } catch(java.beans.PropertyVetoException s) {}
            
        }
    }
    
    class OpenImageAction extends AbstractAction {
        public OpenImageAction(String text, ImageIcon icon,
                String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        
        public void actionPerformed(ActionEvent e) {
            
            FileDialog fd = new FileDialog(MyApp.this);
            fd.setVisible(true);
            if (fd.getFile() == null) return;
            String path = fd.getDirectory() + fd.getFile();
            //Opener op = new Opener();
            //ImagePlus imp = op.openImage(path);
            ImagePlus imp = IJ.openImage(path);
            InternalImageFrame frame = new InternalImageFrame(imp);
            
            theDesktop.add( frame );
            try {
                frame.setSelected(true);
            } catch (java.beans.PropertyVetoException s) {}
            
        }
    }
    
    class SaveImageAction extends AbstractAction {
        public SaveImageAction(String text, ImageIcon icon,
                String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        public void actionPerformed(ActionEvent e) {
            JInternalFrame frame = theDesktop.getSelectedFrame();
            String cls = frame.getClass().getName(); //System.out.println(cls);
            if (cls == "myctapp.InternalImageFrame") {
                InternalImageFrame currframe = (InternalImageFrame)frame;
                //FileDialog fd = new FileDialog(MyApp.this, "Save Image", FileDialog.SAVE);
                //fd.setVisible(true);
                //if (fd.getFile() == null) return;
                //String path = fd.getDirectory() + fd.getFile();
                //currframe.imagepanel.saveImage(path);
                currframe.imagepanel.saveImagePlus();
            }
            if (cls == "myctapp.InternalDrawFrame") {
                InternalDrawFrame currframe = (InternalDrawFrame)frame;
                FileDialog fd = new FileDialog(MyApp.this, "Save Phantom", FileDialog.SAVE);
                fd.setVisible(true);
                if (fd.getFile() == null) return;
                String path = fd.getDirectory() + fd.getFile();
                currframe.drawpanel.savePhantomAsImage(path);
            }
            
        }
    }
    
    class QuitAction extends AbstractAction {
        public QuitAction(String text,
                String desc, Integer mnemonic) {
            super(text);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        public void actionPerformed(ActionEvent e) {
            quit();
        }
    }
    
    class BlurAction extends AbstractAction {
        public BlurAction(String text, ImageIcon icon,
                String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        public void actionPerformed(ActionEvent e) {
            InternalImageFrame currframe = (InternalImageFrame)theDesktop.getSelectedFrame();
            try{
                if (currframe.PANELTYPE >= 2) {
                    String key = "Sharpen";
                    BufferedImageOp op = ImageOps.blurOp();
                    currframe.imagepanel.PerformImageop(op);
                } } catch ( NullPointerException ne){}
            
            /*ImageJ ijapp = new ImageJ();
            InternalImageFrame currframe = (InternalImageFrame)theDesktop.getSelectedFrame();
            ImagePlus imp = currframe.getImagePlus();
            ImageWindow ijframe = new ImageWindow(imp);
            //WindowManager.setCurrentWindow(ijframe);
            WindowManager.setTempCurrentImage(imp); //temp hack
            IJ.runPlugIn("ij.plugin.filter.Filters", "smooth");
            currframe.updateImage(IJ.getImage()); */
        }
    }
    
    class SharpenAction extends AbstractAction {
        public SharpenAction(String text,  ImageIcon icon,
                String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        public void actionPerformed(ActionEvent e) {
            InternalImageFrame currframe = (InternalImageFrame)theDesktop.getSelectedFrame();
            try{
                if (currframe.PANELTYPE >= 2) {
                    String key = "Sharpen";
                    BufferedImageOp op = ImageOps.sharpenOp();
                    currframe.imagepanel.PerformImageop(op);
                } } catch ( NullPointerException ne){}
        }
    }
    
    class InvertAction extends AbstractAction {
        public InvertAction(String text, ImageIcon icon,
                String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        public void actionPerformed(ActionEvent e) {
            InternalImageFrame currframe = (InternalImageFrame)theDesktop.getSelectedFrame();
            try {
                if (currframe.PANELTYPE >= 2) {
                    String key = "Invert";
                    BufferedImageOp op = ImageOps.invertOp();
                    currframe.imagepanel.PerformImageop(op);
                } } catch ( NullPointerException ne){}
        }
    }
    
    class BrightenAction extends AbstractAction {
        public BrightenAction(String text, ImageIcon icon,
                String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        public void actionPerformed(ActionEvent e) {
            InternalImageFrame currframe = (InternalImageFrame)theDesktop.getSelectedFrame();
            try {
                if (currframe.PANELTYPE >= 2) {
                    String key = "Brighten";
                    BufferedImageOp op = ImageOps.rescaleOp(1.2f);
                    currframe.imagepanel.PerformImageop(op);
                } } catch ( NullPointerException ne){}
        }
    }
    
    class DarkenAction extends AbstractAction {
        public DarkenAction(String text, ImageIcon icon,
                String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        public void actionPerformed(ActionEvent e) {
            InternalImageFrame currframe = (InternalImageFrame)theDesktop.getSelectedFrame();
            try{
                if (currframe.PANELTYPE >= 2) {
                    String key = "Darken";
                    BufferedImageOp op = ImageOps.rescaleOp(0.9f);
                    currframe.imagepanel.PerformImageop(op);
                } } catch ( NullPointerException ne){}
        }
    }
    
    class GreyScaleAction extends AbstractAction {
        public GreyScaleAction(String text, ImageIcon icon,
                String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        public void actionPerformed(ActionEvent e) {
            InternalImageFrame currframe = (InternalImageFrame)theDesktop.getSelectedFrame();
            try{
                if (currframe.PANELTYPE >= 2) {
                    String key = "Greyscale";
                    BufferedImageOp op = ImageOps.colorconvertOp();
                    currframe.imagepanel.PerformImageop(op);
                } } catch ( NullPointerException ne){}
        }
    }
    
    class ScaleAction extends AbstractAction {
        public ScaleAction(String text, ImageIcon icon,
                String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        public void actionPerformed(ActionEvent e) {
            String key = "Scale";
            InternalImageFrame currframe = (InternalImageFrame)theDesktop.getSelectedFrame();
            currframe.imagepanel.ScaleImage();
        }
    }
    
    class addImagesAction extends AbstractAction {
        public addImagesAction(String text, ImageIcon icon,
                String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        public void actionPerformed(ActionEvent e) {
            InternalImageFrame frame1 = (InternalImageFrame)theDesktop.getSelectedFrame();
            
            if (frame1.PANELTYPE >= 2) {
                int[][] image1 = frame1.imagepanel.getBuffImagePixels();
                FileDialog fd = new FileDialog(MyApp.this);
                fd.setTitle("Select Image to Add");
                fd.setVisible(true);
                if (fd.getFile() == null) return;
                String path = fd.getDirectory() + fd.getFile();
                InternalImageFrame frame2 = new InternalImageFrame(path,2,ctscanner);
                theDesktop.add( frame2 );
                try {
                    frame2.setSelected(true);
                } catch (java.beans.PropertyVetoException s) {}
                int[][] image2 = frame2.imagepanel.getBuffImagePixels();
                int[][] newimage = Utils.add2DintArrays(image1, image2);
                newimage = Utils.setRange2DintArray(newimage, 0, 255);   //for 8-bit images
                InternalImageFrame frame = new InternalImageFrame();
                Container container = frame.getContentPane();
                ImagePanel panel = new ImagePanel();
                
                BufferedImage img = ImageUtils.CreateImagefromIntArray(newimage);
                panel.loadBufferedImage(img);
                frame.PANELTYPE = 2;
                container.add(panel, BorderLayout.CENTER);
                frame.imagepanel = panel;
                frame.pack(); frame.setVisible( true );
                theDesktop.add( frame );
                try {
                    frame.setSelected(true);
                } catch (java.beans.PropertyVetoException s) {}
            }
        }
    }
    
    class subImagesAction extends AbstractAction {
        public subImagesAction(String text, ImageIcon icon,
                String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        public void actionPerformed(ActionEvent e) {
            InternalImageFrame frame1 = (InternalImageFrame)theDesktop.getSelectedFrame();
            
            if (frame1.PANELTYPE >= 2) {
                int[][] image1 = frame1.imagepanel.getBuffImagePixels();
                FileDialog fd = new FileDialog(MyApp.this);
                fd.setTitle("Select Image to Subtract");
                fd.setVisible(true);
                if (fd.getFile() == null) return;
                String path = fd.getDirectory() + fd.getFile();
                InternalImageFrame frame2 = new InternalImageFrame(path,2,ctscanner);
                theDesktop.add( frame2 );
                try {
                    frame2.setSelected(true);
                } catch (java.beans.PropertyVetoException s) {}
                int[][] image2 = frame2.imagepanel.getBuffImagePixels();
                int[][] newimage = Utils.subtract2DintArrays(image1, image2);
                newimage = Utils.setRange2DintArray(newimage, 0, 255);   //for 8-bit images
                InternalImageFrame frame = new InternalImageFrame();
                Container container = frame.getContentPane();
                ImagePanel panel = new ImagePanel();
                
                BufferedImage img = ImageUtils.CreateImagefromIntArray(newimage);
                panel.loadBufferedImage(img);
                frame.PANELTYPE = 2;
                container.add(panel, BorderLayout.CENTER);
                frame.imagepanel = panel;
                frame.pack(); frame.setVisible( true );
                theDesktop.add( frame );
                try {
                    frame.setSelected(true);
                } catch (java.beans.PropertyVetoException s) {}
            }
        }
    }
    
    class divImagesAction extends AbstractAction {
        public divImagesAction(String text, ImageIcon icon,
                String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        public void actionPerformed(ActionEvent e) {
            InternalImageFrame frame1 = (InternalImageFrame)theDesktop.getSelectedFrame();
            
            if (frame1.PANELTYPE >= 2) {
                int[][] image1 = frame1.imagepanel.getBuffImagePixels();
                FileDialog fd = new FileDialog(MyApp.this);
                fd.setTitle("Select Image to Divide");
                fd.setVisible(true);
                if (fd.getFile() == null) return;
                String path = fd.getDirectory() + fd.getFile();
                InternalImageFrame frame2 = new InternalImageFrame(path,2,ctscanner);
                theDesktop.add( frame2 );
                try {
                    frame2.setSelected(true);
                } catch (java.beans.PropertyVetoException s) {}
                int[][] image2 = frame2.imagepanel.getBuffImagePixels();
                double[][] newimage = Utils.divide2Dintarrays(image1, image2);
                //newimage = Utils.setRange2DArray(newimage, 0, 255);
                InternalImageFrame frame = new InternalImageFrame();
                Container container = frame.getContentPane();
                ImagePanel panel = new ImagePanel();
                
                BufferedImage img = ctscanner.CreateImagefromArray(newimage, Utils.getMax(newimage),0);
                panel.loadBufferedImage(img);
                frame.PANELTYPE = 2;
                container.add(panel, BorderLayout.CENTER);
                frame.imagepanel = panel;
                frame.pack(); frame.setVisible( true );
                theDesktop.add( frame );
                try {
                    frame.setSelected(true);
                } catch (java.beans.PropertyVetoException s) {}
            }
        }
    }
    
    /**find mean-squared error between images*/
    class findMSEAction extends AbstractAction {
        public findMSEAction(String text, ImageIcon icon,
                String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        public void actionPerformed(ActionEvent e) {
            InternalImageFrame frame1 = (InternalImageFrame)theDesktop.getSelectedFrame();
            
            if (frame1.PANELTYPE >= 2) {
                int[][] image1 = frame1.imagepanel.getBuffImagePixels();
                FileDialog fd = new FileDialog(MyApp.this);
                fd.setTitle("Select the Other Image");
                fd.setVisible(true);
                if (fd.getFile() == null) return;
                String path = fd.getDirectory() + fd.getFile();
                InternalImageFrame frame2 = new InternalImageFrame(path,2,ctscanner);
                theDesktop.add( frame2 );
                try {
                    frame2.setSelected(true);
                } catch (java.beans.PropertyVetoException s) {}
                int[][] image2 = frame2.imagepanel.getBuffImagePixels();
                double mse = 0;
                mse = Utils.getMSE2DintArrays(image1, image2);
                double temp = 255 / Math.sqrt(mse);
                double psnr =  20 * (Math.log(temp)/ Math.log(10)); //log base 10
                System.out.println("MSE="+mse);
                System.out.println("PSNR="+psnr);
                
                JOptionPane.showInternalMessageDialog(theDesktop, "MSE: "+ mse+"\n"+
                        "PSNR: "+psnr+"\n",
                        "Error Metrics",
                        JOptionPane.PLAIN_MESSAGE);
            }
        }
    }
    
    
    class showHistogramAction extends AbstractAction {
        public showHistogramAction(String text, ImageIcon icon,
                String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        public void actionPerformed(ActionEvent e) {
            InternalImageFrame frame1 = (InternalImageFrame)theDesktop.getSelectedFrame();
            if (frame1.PANELTYPE >= 2) {
                BufferedImage img =  frame1.imagepanel.getBufferedImage();
                Histogram hist = new Histogram(img);
                InternalImageFrame frame = new InternalImageFrame();
                Container container = frame.getContentPane();
                container.add(hist.histpanel, BorderLayout.CENTER);
                frame.pack();  frame.setResizable(false);
                frame.setMaximizable(false);
                frame.setVisible( true );
                theDesktop.add( frame );
                
                try {
                    frame.setSelected(true);
                } catch (java.beans.PropertyVetoException s) {}
            }
        }
    }
    
    class eqHistImageAction extends AbstractAction {
        public eqHistImageAction(String text, ImageIcon icon,
                String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        public void actionPerformed(ActionEvent e) {
            InternalImageFrame frame = (InternalImageFrame)theDesktop.getSelectedFrame();
            if (frame.PANELTYPE >= 2) {
                frame.imagepanel.EqImageHistogram();
                try {
                    frame.setSelected(true);
                } catch (java.beans.PropertyVetoException s) {}
            }
        }
    }
    
    class FFTImageAction extends AbstractAction {
        public FFTImageAction(String text, ImageIcon icon,
                String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        public void actionPerformed(ActionEvent e) {
            InternalImageFrame frame = (InternalImageFrame)theDesktop.getSelectedFrame();
            if (frame.PANELTYPE >= 2) {
                frame.imagepanel.FFTCurrentImage();
                try {
                    frame.setSelected(true);
                } catch (java.beans.PropertyVetoException s) {}
            }
        }
    }
    
    public void UpdateWindowingPanel(InternalImageFrame frame){
        try{
            int type = frame.imagepanel.getImageType();
            if (frame.imagepanel.getImageType() == 11){
                lowerlevelField.setValue(new Integer(0));
                upperlevelField.setValue(new Integer(2095));
                mSlider1.setMaximum(2095);
            } else {
                lowerlevelField.setValue(new Integer(0));
                upperlevelField.setValue(new Integer(255));
                mSlider1.setMaximum(255);
            }
            bitdepthField.setValue(new Integer(type));
            winPanel.repaint();
            lower = frame.imagepanel.lowerwinlvl;
            lowerlevelField.setValue(new Integer(lower));
            mSlider1.setValueAt(lower,0);
            upper = frame.imagepanel.upperwinlvl;
            upperlevelField.setValue(new Integer(upper));
            mSlider1.setValueAt(upper,1);
            sliderPanel.repaint();
            
        } catch( NullPointerException ne){}
    }
    
    /**An internal listener class to detect internalframe actions on the desktoppane*/
    class FrameListener implements InternalFrameListener {
        
        public void internalFrameActivated(InternalFrameEvent e) {
            //System.out.println("fire");
            InternalImageFrame currframe = (InternalImageFrame)e.getInternalFrame();
            UpdateWindowingPanel(currframe);
        }
        
        public void internalFrameClosed(InternalFrameEvent e) {
            InternalImageFrame currframe = (InternalImageFrame)e.getInternalFrame();
            currframe.imagepanel.closed = true;
            ctscanner.animcount = 0;
            ctscanner.interrupt = true;
        }
        
        public void internalFrameClosing(InternalFrameEvent e) {
            InternalImageFrame frame = (InternalImageFrame)e.getInternalFrame();
            int confirm = JOptionPane.showOptionDialog(frame,
                    "Save Image " + frame.getTitle() + "?", "Close Confirmation",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, null, null);
            if (confirm == 0) {
                frame.imagepanel.saveImage(frame.getTitle());
            } else if (confirm == 1) {
                
            }
        }
        
        
        public void internalFrameDeactivated(InternalFrameEvent e) {
        }
        
        public void internalFrameDeiconified(InternalFrameEvent e) {
        }
        
        public void internalFrameIconified(InternalFrameEvent e) {
        }
        
        public void internalFrameOpened(InternalFrameEvent e) {
            
        }
    }
    
    
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if ("quit".equals(e.getActionCommand())) {
            quit();
        }
    }
    
    
    public void stateChanged(ChangeEvent e) {
        memmonitor.surf.stop();
        
    }
    
    public void itemStateChanged(ItemEvent e) {
        
        Object source = e.getItemSelectable();
        
        if (source == filtercheckbox) {
            if (filtercheckbox.isSelected() == true){ ctscanner.filtering = true; }
            if (filtercheckbox.isSelected() == false){ ctscanner.filtering = false; }
        } else if(source == truncatecheckbox){
            if (truncatecheckbox.isSelected() == true){ ctscanner.truncate = true; }
            if (truncatecheckbox.isSelected() == false){ ctscanner.truncate = false; }
            
        } else if(source == ROIcropcheckbox){
            if (ROIcropcheckbox.isSelected() == true){ ctscanner.roicrop = true; }
            if (ROIcropcheckbox.isSelected() == false){ ctscanner.roicrop = false; }
        } else if(source == ROIcirclecheckbox){
            if (ROIcirclecheckbox.isSelected() == true){ ctscanner.displayroicircle = true; }
            if (ROIcirclecheckbox.isSelected() == false){ ctscanner.displayroicircle = false; }
        } else if(source == setedgezerocheckbox){
            if (setedgezerocheckbox.isSelected() == true){ ctscanner.setedgezero = true; }
            if (setedgezerocheckbox.isSelected() == false){ ctscanner.setedgezero = false; }
        } else if(source == keepcurrentextrapwidthscheckbox) {
            if (keepcurrentextrapwidthscheckbox.isSelected() == true){ ctscanner.keepcurrentextrapwidths = true; }
            if (keepcurrentextrapwidthscheckbox.isSelected() == false){ ctscanner.keepcurrentextrapwidths = false; }
        } else if(source == animatecheckbox){
            if (animatecheckbox.isSelected() == true){ ctscanner.animate = true; }
            if (animatecheckbox.isSelected() == false){ ctscanner.animate = false; }
            
        }
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        Object source = evt.getSource();
        if (source == scansField) {
            ctscanner.scans = ((Number)scansField.getValue()).intValue();
            System.out.println(ctscanner.scans);
        } else if (source == stepsizeField) {
            ctscanner.stepsize = ((Number)stepsizeField.getValue()).floatValue();
            ctscanner.resetviews();
            viewsField.setValue(new Integer(ctscanner.views));
            System.out.println(ctscanner.stepsize);
        } else if (source == imgsizeField) {
            int outputimgsize = ((Number)imgsizeField.getValue()).intValue();
            ctscanner.outputimgsize = outputimgsize;
            //if (outputimgsize < ctscanner.scans){
            //    ctscanner.outputimgsize = ctscanner.scans;
            //}
            //else {ctscanner.outputimgsize = outputimgsize;}
            System.out.println(ctscanner.outputimgsize);
        } else if (source == zoomField) {
            double zoom = ((Number)zoomField.getValue()).doubleValue();
            ctscanner.zoom = zoom;
            System.out.println(ctscanner.zoom);
        } else if (source == xoffsetField) {
            double xoffset = ((Number)xoffsetField.getValue()).doubleValue();
            ctscanner.xoffset = xoffset;
            System.out.println(ctscanner.xoffset);
        } else if (source == yoffsetField) {
            double yoffset = ((Number)yoffsetField.getValue()).doubleValue();
            ctscanner.yoffset = yoffset;
            System.out.println(ctscanner.yoffset);
        } else if (source == lowerlevelField){
            lower = ((Number)lowerlevelField.getValue()).intValue();
        } else if (source == upperlevelField){
            upper = ((Number)upperlevelField.getValue()).intValue();
        }
        
    }
    
    static final String INT_TITLE = "Enter a value";
    /**
     ** returns integer input from the user via a simple dialog.
     ** @param prompt the message string to be displayed inside dialog
     ** @return the input integer
     **/
    public double getDoublefromDialog(String prompt) {
        Object[] commentArray = {prompt, "", ""};
        Object[] options = { "OK","Cancel" };
        
        String inputValue = "";
        boolean validResponse = false;
        
        double response = 1;
        while(!validResponse) {
            final JOptionPane optionPane = new JOptionPane(commentArray,
                    JOptionPane.PLAIN_MESSAGE,
                    JOptionPane.OK_CANCEL_OPTION,
                    null,
                    options,
                    options[0]);
            
            optionPane.setWantsInput(true);
            JDialog dialog = optionPane.createDialog(null, INT_TITLE);
            dialog.pack();
            dialog.setVisible(true);
            
            String result = (String)optionPane.getInputValue();
            
            try {
                //workaround for BlueJ bug - misses first exception after compilation
                response = Double.parseDouble(result);
                response = Double.parseDouble(result);
                validResponse = true;
            } catch(NumberFormatException exception) {
                if(result.equals("uninitializedValue"))
                    result = "";
                commentArray[1] = "Invalid int: " + result;
                commentArray[2] = "Enter a valid integer";
            }
        }
        return response;
    }
    
    //Quit the application.
    protected void quit() {
        System.exit(0);
    }
    
    public void displaySplash(){
        Frame splashFrame = null;
        java.net.URL imageURL = MyApp.class.getResource("images/"+"splash.png");
        if (imageURL != null) {
            splashFrame = SplashWindow.splash(
                    Toolkit.getDefaultToolkit().createImage(imageURL)
                    );
        } else {
            
            System.err.println("Splash image not found");
            
        }
    }
    /* Class to set the Look and Feel on a frame */
    class LNFSetter implements ActionListener {
        String theLNFName;
        
        JRadioButton thisButton;
        
        /** Called to setup for button handling */
        LNFSetter(String lnfName, JRadioButton me) {
            theLNFName = lnfName;
            thisButton = me;
        }
        
        /** Called when the button actually gets pressed. */
        public void actionPerformed(ActionEvent e) {
            try {
                UIManager.setLookAndFeel(theLNFName);
                SwingUtilities.updateComponentTreeUI(theFrame);
                //SwingUtilities.updateUI(theFrame);
                
                theFrame.pack();
            } catch (Exception evt) {
                JOptionPane.showMessageDialog(null,
                        "setLookAndFeel didn't work: " + evt, "UI Failure",
                        JOptionPane.INFORMATION_MESSAGE);
                previousButton.setSelected(true); // reset the GUI to agree
            }
            previousButton = thisButton;
        }
    }
    
    final static String PREFERREDLOOKANDFEELNAME = "javax.swing.plaf.metal.MetalLookAndFeel";
    protected String curLF = PREFERREDLOOKANDFEELNAME;
    protected JRadioButton previousButton;
    JInternalFrame theFrame;
    
    public void LNFSwitcher() {
        //JInternalFrame theFrame;
        Container cp;
        /** Start with the Java look-and-feel, if possible */
        
        theFrame = new JInternalFrame("LNF Switcher");
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        theFrame.pack(); theDesktop.add(theFrame);
        theFrame.setVisible( true );
        
        cp = theFrame.getContentPane();
        cp.setLayout(new FlowLayout());
        
        ButtonGroup bg = new ButtonGroup();
        JRadioButton bJava = new JRadioButton("Java");
        bJava.addActionListener(new LNFSetter(
                "javax.swing.plaf.metal.MetalLookAndFeel", bJava));
        bg.add(bJava);
        cp.add(bJava);
        
        JRadioButton bMSW = new JRadioButton("MS-Windows");
        bMSW.addActionListener(new LNFSetter(
                "com.sun.java.swing.plaf.windows.WindowsLookAndFeel", bMSW));
        bg.add(bMSW);
        cp.add(bMSW);
        
        JRadioButton bMotif = new JRadioButton("Motif");
        bMotif.addActionListener(new LNFSetter(
                "com.sun.java.swing.plaf.motif.MotifLookAndFeel", bMotif));
        bg.add(bMotif);
        cp.add(bMotif);
        
        
        String defaultLookAndFeel = UIManager.getSystemLookAndFeelClassName();
        // System.out.println(defaultLookAndFeel);
        JRadioButton bDefault = new JRadioButton("Default");
        bDefault.addActionListener(new LNFSetter(defaultLookAndFeel, bDefault));
        bg.add(bDefault);
        cp.add(bDefault);
        
        (previousButton = bDefault).setSelected(true);
        
        theFrame.pack();
        theFrame.setVisible(true);
    }
    
// The main program for the MyApp class
    
    public static void main(String[] args) {
        
        // Read the image data and display the splash screen
        //PlasticLookAndFeel laf = new PlasticLookAndFeel();
        
        try {
            //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            //UIManager.setLookAndFeel(laf);
        } catch (Exception e) { }
        
        /*Frame splashFrame = null;
        java.net.URL imageURL = MyApp.class.getResource("images/"+"splash.png");
        if (imageURL != null) {
            splashFrame = SplashWindow.splash(
                 Toolkit.getDefaultToolkit().createImage(imageURL)
            );
        } else {
         
            System.err.println("Splash image not found");
        }   */
        MyApp theapp = new MyApp();
        MyApp.setDefaultLookAndFeelDecorated(true);
        //if (splashFrame != null) splashFrame.dispose();
    }
    
}
