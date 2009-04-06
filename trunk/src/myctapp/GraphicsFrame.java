package myctapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.DecimalFormat;
import java.awt.image.*;

class GraphicsFrame extends JInternalFrame {
    public static String sFunction;
    public static JComboBox cmbFunc;
    public static JLabel lblNameFunc;
    public static JTextField txtInputY, txtInputX;
    public static JLabel lblFunc;
    public static JTextField txtInputRight;
    public JLabel lblZero, lblScans, lblViews, lblcurrview;
    public static JButton btnBuildFunc, replotFunc;
    public static double a = 1;
    public static double b = 1;
    public int v = 0;
    public double[] filter;
    
    public GraphicsFrame(CTScanner scanner, int choice) {
        
        setSize(500,500);
        setDefaultCloseOperation ( DISPOSE_ON_CLOSE );
        setResizable(true);
        setMaximizable(true);
        setClosable(true);
        this.setVisible( true );
        
        if (choice == 0){
            setTitle("Visualise Projections");
            createBProjGraphs(scanner);            
        }
        else if (choice == 1){
            setTitle("Filter Graph");
            createFilterGraphs(scanner);
        }
    }
     
    void createBProjGraphs(CTScanner scanner){
        
        Font theMainFont = new Font("SansSerif", Font.PLAIN, 11);
        UIManager.put("Button.font", theMainFont );
        UIManager.put("Label.font", theMainFont );
        UIManager.put("ComboBox.font", theMainFont );
        JPanel pTB = new JPanel();
        pTB.setLayout(new BoxLayout(pTB, BoxLayout.X_AXIS));
        //small preview image
        final ImagePanel bpPanel1 = new ImagePanel(){
             public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawString("PREVIEW", 10, 10);
            }
        };
        bpPanel1.setBackground(Color.black);  
        int tmp = scanner.outputimgsize;
        scanner.outputimgsize = 40;
        BufferedImage img;
        if (scanner.animate == true){
            scanner.animate = false;
            img = scanner.CreateReconstructedImage();
            scanner.animate = true; 
        } else {
            img = scanner.CreateReconstructedImage();
        }
        scanner.outputimgsize = tmp;    //reset size to prev. value
        bpPanel1.loadBufferedImage(img);

        final GraphPanel bpPanel2 = new GraphPanel(); 
        bpPanel2.setBackground(Color.white);  
        final GraphPanel bpPanel3 = new GraphPanel(); 
        bpPanel3.setBackground(Color.white);  
        final ImagePanel sinogrampanel = new ImagePanel(){
            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawString("SINOGRAM", 10, 10);
            }
        };
        sinogrampanel.setBackground(Color.black);
        sinogrampanel.loadSinogram(scanner);
        bpPanel2.setData(scanner, 1);
        bpPanel3.setData(scanner, 2);
        
        pTB.add(bpPanel1);
        pTB.add(Box.createRigidArea(new Dimension(6,0)));
        pTB.add(sinogrampanel);
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));        
        main.add(pTB);   
        main.add(Box.createRigidArea(new Dimension(0,6)));
        main.add(bpPanel2); 
        main.add(Box.createRigidArea(new Dimension(0,6)));
        main.add(bpPanel3);         
        lblScans = new JLabel("Scans: "+scanner.scans);
        lblViews = new JLabel("Views: "+scanner.views);
        final double step = scanner.stepsize;
        v=0;
        lblcurrview = new JLabel("\u0398=" +v*step);
        JPanel widgetsPanel = new JPanel();
        widgetsPanel.setMaximumSize(new Dimension(350, 30));
        btnBuildFunc = new JButton("Next");
        bpPanel2.update(v);
        bpPanel3.update(v);
        btnBuildFunc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (v>=180){ v=0; } else { v++; }
                //bpPanel1.update(v*step);
                bpPanel2.update(v);
                bpPanel3.update(v);
                Graphics2D gs = (Graphics2D) sinogrampanel.getGraphics();
                gs.setColor(Color.blue);
                gs.drawLine(0,v,sinogrampanel.getWidth(),v);
                DecimalFormat df1 = new DecimalFormat("##.00");
                lblcurrview.setText("\u0398=" +df1.format(v*step));
                repaint();
            }
        });
        cmbFunc = new JComboBox();
        cmbFunc.addItem("spatial");        
        cmbFunc.addItem("freq");
        cmbFunc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                repaint();
            }
        });
        JLabel lblXMag = new JLabel("  X Mag:");      
        txtInputX = new JTextField("1", 2);
        widgetsPanel.add(lblXMag);
        widgetsPanel.add(txtInputX);
        
        JLabel lblYMag = new JLabel("  Y Mag:");      
        txtInputY = new JTextField("1", 2);
        widgetsPanel.add(lblYMag);
        widgetsPanel.add(txtInputY);
        replotFunc = new JButton("Replot");
        replotFunc.setMaximumSize(new Dimension(100, 25));
        replotFunc.setSize(new Dimension(100, 25));
        replotFunc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                a = Double.parseDouble(txtInputY.getText());
                b = Double.parseDouble(txtInputX.getText());
                repaint();
            }
        });
        widgetsPanel.add(replotFunc);
        
        widgetsPanel.add(cmbFunc);
        widgetsPanel.add(btnBuildFunc);
        widgetsPanel.add(lblScans);
        widgetsPanel.add(lblViews);
        widgetsPanel.add(lblcurrview);
        main.add(widgetsPanel);
        Container contentPane = getContentPane();
        contentPane.add(main);
        
    }
    
    void createFilterGraphs(CTScanner scanner){
        sFunction = new String("filter");
        cmbFunc = new JComboBox();
        cmbFunc.addItem("test");        
        cmbFunc.addItem("ramlak");
        cmbFunc.addItem("shepp-logan");
        cmbFunc.addItem("hamming");
        cmbFunc.addItem("hann");
        cmbFunc.addItem("cosine");
        cmbFunc.addItem("blackman");
        cmbFunc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                repaint();
            }
        });
        lblNameFunc = new JLabel(" ");
        JLabel lblYMag = new JLabel("  Y Mag:");
        txtInputY = new JTextField("1", 5);
        //txtInputRight = new JTextField("0", 5);
        btnBuildFunc = new JButton("Replot");
        btnBuildFunc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                a = Double.parseDouble(txtInputY.getText());
                repaint();
            }
        });

        FilterGraphPanel imgPanel = new FilterGraphPanel();
        imgPanel.setBackground(Color.black);
        imgPanel.showfft = 0;
        FilterGraphPanel imgPanel2 = new FilterGraphPanel();
        imgPanel2.setBackground(Color.black);
        FilterGraphPanel.makefilterData(scanner);
        imgPanel2.showfft = 1;
        JPanel pTB = new JPanel();
        pTB.setLayout(new BoxLayout(pTB, BoxLayout.Y_AXIS));
        cmbFunc.setMaximumSize(new Dimension(120, 25));
        pTB.add(cmbFunc);
        pTB.add(imgPanel);
        pTB.add(Box.createRigidArea(new Dimension(0,6)));
        //pTB.add(imgPanel2);
        //pTB.add(Box.createRigidArea(new Dimension(0,6)));
        JPanel widgetsPanel = new JPanel();
        widgetsPanel.add(lblNameFunc);
        widgetsPanel.add(lblYMag);
        widgetsPanel.setMaximumSize(new Dimension(300, 30));
        widgetsPanel.add(txtInputY);
        widgetsPanel.add(btnBuildFunc);
        pTB.add(widgetsPanel);
        Container contentPane = getContentPane();
        contentPane.add(pTB);
    }

}

class BProjPanel extends JPanel{
    
    int s=128; 
    double[] test;
    int num =1;
    double angle=0;
    
    public void BProjPanel(){
        setBackground(Color.gray);
    }
    
    public void makeData(CTScanner scanner){
        for ( int i = 0; i < s; i++ ) { 
            test[i] = scanner.projection[0][i];

        } 
    }
    
    public void update(double a){
        angle = a;
        repaint();
    }

    public void paintComponent(Graphics2D g2) {
        super.paintComponent(g2);
        int w = getSize().width;
        int h = getSize().height;
        Line2D line = null;
        //Graphics2D g2 = (Graphics2D)g;
        g2.setColor(Color.gray);
        Line2D lCoordX = new Line2D.Double(0, getSize().height / 2,getSize().width, getSize().height / 2);
        Line2D lCoordY = new Line2D.Double(getSize().width / 2, 0, getSize().width / 2, getSize().height);

        g2.draw(lCoordX);
        g2.draw(lCoordY);
        g2.translate(getSize().width / 2, getSize().height / 2);
        g2.drawString("2", 10,10);
        
        double ratio = (double)getSize().width / (double)getSize().height;
        if (ratio >= 1) {    
               w = getSize().height;
               h = getSize().height;              
        }        
        else if (ratio < 1){
               h = getSize().width;
               w = getSize().width;       
        }
        
        for(int x = -s/2; x < s/2-1; x++ ) {
            g2.setPaint(Color.blue);
            g2.drawOval(-w/2+6, -h/2+6, w-6, h-6 );          
        }     
        
        int x = (int)(50* Math.cos(angle*Math.PI/180) );
        int y = (int)(50* Math.sin(angle*Math.PI/180) );
        g2.drawLine( 0, 0, w/2 + x, h/2 + y );
        g2.drawLine( 0, 0, -w/2 + x, -h/2 + y );
    }
}

class GraphPanel extends JPanel {
    
    int c=1;
    int y = getSize().height;
    double[][] proj, fproj;
    int s, v, a1, a2;
    double cutoff, truncwidth;
    boolean trunc;
    float step;
    double[] projdata, fprojdata;
    String filtname;
    boolean showinfo = false;
    
    public void GraphPanel(){   
        setMinimumSize(new Dimension(200, 100));
        setPreferredSize(new Dimension(300, 200));
        setMaximumSize(new Dimension(600, 450));
    }
    
    public void setData(CTScanner scanner, int choice){
        c = choice;
        s = scanner.scans;
        v = scanner.views;
        a1 = scanner.ang1;
        a2 = scanner.ang2;
        step = scanner.stepsize;
        cutoff = scanner.filtercutoff;        
        filtname = scanner.filtername;
        trunc = scanner.truncate;
        truncwidth = scanner.truncatewidth;
        
        if (c == 1){
            proj = scanner.projection;
            projdata = new double[s];
        }
        else if (c == 2){
            fproj = scanner.fprojection;
            fprojdata = new double[s];
        }         

    }
    
    public void update(int v){
        y = 70;
        float phi=0;
        int i, j=v;
        try{ Thread.sleep(40); } catch (InterruptedException e) {} //delay
        //for (phi=a1;phi<a2;phi=phi+step){
          for (i=0;i<s;i++){
              if (c == 1){ projdata[i] = proj[j][i]; }//System.out.print(projdata[i]+", ");
              else if (c == 2){ fprojdata[i] = fproj[j][i]; }
          } System.out.println();
          if (c == 1){ normalizeData(projdata, y);}
          else if (c == 2){  normalizeData(fprojdata, y);}
          repaint();
          //j++;
       //}

    }
        
    public void normalizeData(double data[], int y){
        double min = Utils.getMin(data);
        double max = Utils.getMax(data);
        //rescale values to fit on graph panel
        for ( int i = 0; i < s; i++ ) {
            if (max>min){
                data[i] = ((data[i])*y)/(max-min);
            }
        } 
    }
    
    double f(double x) {
        double[] idata = new double[s];
        for (int i = 0; i<s; i++) {
               idata[i] = 0;
        }
        if (c == 1){
            if (GraphicsFrame.cmbFunc.getSelectedItem().equals("freq")) {
                Filters.FFT(1, s, projdata, idata);
                Utils.normalize1DArray(projdata, 0, 1);
            }
            return GraphicsFrame.a * projdata[(int)x];
        }
        else {
            if (GraphicsFrame.cmbFunc.getSelectedItem().equals("freq")) {
                Filters.FFT(1, s, fprojdata, idata);
                Utils.normalize1DArray(fprojdata, 0, 1);
            }
            return GraphicsFrame.a * fprojdata[(int)x];
        }
    }


    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Line2D line = null;
        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(Color.gray);
        Line2D lCoordX;
        if (c == 1){   
            lCoordX = new Line2D.Double(0, getSize().height -5 ,
                                getSize().width, getSize().height -5);
        }
        else{
            lCoordX = new Line2D.Double(0, getSize().height / 2,
                                getSize().width, getSize().height / 2);
        }
        Line2D lCoordY = new Line2D.Double(getSize().width / 2, 0,
                                getSize().width / 2, getSize().width);
        

        g2.draw(lCoordX);
        g2.draw(lCoordY);
        if (showinfo == true){
            if (c == 1){
                g2.drawString("UNFILTERED", getSize().width/2-160 , getSize().height  - 15);
                if (trunc == true){
                    g2.drawString("truncated at", getSize().width/2+140 , getSize().height - 25);
                    g2.drawString(Double.toString(truncwidth), getSize().width/2+140
                    , getSize().height - 15);
                }
            } else{
                g2.drawString("FILTERED", getSize().width/2-160 , getSize().height - 15);
                g2.drawString(filtname, getSize().width/2+140 , getSize().height - 15);
                g2.drawString("cutoff: "+cutoff, getSize().width/2+140 , getSize().height - 25);
            }
        }
        
        //g2.drawString("X", getSize().width - 10, getSize().height / 2 - 15);
        //g2.drawString("Y", getSize().width / 2 - 15, 0 + 10);
        //g2.drawString("0", getSize().width / 2 - 10, getSize().height / 2 + 15);
        
        if (c==1){
            g2.translate(getSize().width / 2, getSize().height -15);
        }
        else{
            g2.translate(getSize().width / 2, getSize().height / 2);
        }
        
        for(int x = -s/2 ; x < s/2-1; x++ ) {
            //g2.setPaint(Color.red);
            g2.setPaint(Color.black);
            line = new Line2D.Double(x*2*GraphicsFrame.b, -(int)f(x+s/2), 
                    x*2*GraphicsFrame.b+(2*GraphicsFrame.b) , -(int)f(x+s/2+1));
            g2.draw(line);
        }


    }
}


class FilterGraphPanel extends JPanel {
    
    int showfft;
    static int y;
    boolean nocheck = false;
    static int s;
    static double[] test;
    static double[] ffttest;
    static double[] ramlak, shepplogan, hamming, hann, cosine, blackman;
    
    public void FilterGraphPanel(){
        
    }
    
    public static void makeData(CTScanner scanner){
        y=100;
        s = scanner.scans;
        test = new double[s];
        for ( int i = 0; i < s; i++ ) {
            test[i] = scanner.projection[45][i];
        } 
        normalizeData(test, y);
    }
        
    public static void makefilterData(CTScanner scanner){
        y=100;
        s = scanner.scans;
        test = new double[s];
        double c = scanner.filtercutoff;
        ramlak = Filters.filter1("ramp", s, c);
        shepplogan = Filters.filter1("shepplogan", s, c);
        hamming = Filters.filter1("hamming", s, c);
        hann = Filters.filter1("hann", s, c);
        cosine = Filters.filter1("cosine", s, c);
        blackman = Filters.filter1("blackman", s, c);
        
        for ( int i = 0; i < s; i++ ) {
            double freq = 1/s;
            double pi = Math.PI;
            //test[i] = Math.sin(i*2*Math.PI/4); System.out.println(test[i]+" ");
             test[i] = Math.cos(2*pi*i*0.1);

            //test[i] = Math.random()* testfilt[i];
            //if (i>40 && i<60){ test[i] = 10; }

        } 
        //ffttest = Filters.dft(test, idata, s);
        //test = Filters.idft(ffttest, idata, s);
        //cdata = Cmplx.fft(test);
        //ffttest = Cmplx.fftInverse(cdata, s);
        //Filters.FFT(1, s, test, idata);
        //ffttestfilt = Filters.idft(shepplogan, idata, s);  
        //fftramlak = Filters.dft(ramlak, idata, s);      
        
        normalizeData(test, y);
        normalizeData(ramlak, y);
        normalizeData(shepplogan, y);
        normalizeData(hamming, y);
        normalizeData(hann, y);
        normalizeData(cosine, y);
        normalizeData(blackman, y);
        
    }
    

    public static void normalizeData(double data[], int y){
        double min = Utils.getMin(data);
        double max = Utils.getMax(data);
        //rescale values to fit on graph panel
        for ( int i = 0; i < s; i++ ) {
            if (max>min){
                data[i] = ((data[i])*y)/(max-min);
            }
        } 
    }
    
    double fSqrt(double x) {
        return -Math.sqrt(x);
    }
    
  
    double fTanX(double x) {
        return -Math.tan(0.01 * x) * getSize().height / 2;
    }
    
     double f(double x) {
        if (nocheck == true){
            return GraphicsFrame.a * test[(int)x];
        }
        else if (GraphicsFrame.cmbFunc.getSelectedItem().equals("test")) {
            GraphicsFrame.lblNameFunc.setText("test");    
            return GraphicsFrame.a * test[(int)x]; 
            /*if (showfft == 0){
                return GraphicsFrame.a * test[(int)x]; 
            }
            else {
                return GraphicsFrame.a * ffttest[(int)x]; 
            } */
        }

        else if (GraphicsFrame.cmbFunc.getSelectedItem().equals("ramlak")) {
            GraphicsFrame.lblNameFunc.setText("ramlak");
            return GraphicsFrame.a * ramlak[(int)x];
        }  
        else if (GraphicsFrame.cmbFunc.getSelectedItem().equals("shepp-logan")) {
            GraphicsFrame.lblNameFunc.setText("shepp-logan");
            return GraphicsFrame.a * shepplogan[(int)x];
        }     
        else if (GraphicsFrame.cmbFunc.getSelectedItem().equals("hamming")) {
            GraphicsFrame.lblNameFunc.setText("hamming");
            return GraphicsFrame.a * hamming[(int)x];

        }  
        else if (GraphicsFrame.cmbFunc.getSelectedItem().equals("hann")) {
            GraphicsFrame.lblNameFunc.setText("hann");
            return GraphicsFrame.a * hann[(int)x];
        }             
        else if (GraphicsFrame.cmbFunc.getSelectedItem().equals("cosine")) {
            GraphicsFrame.lblNameFunc.setText("cosine");
            return GraphicsFrame.a * cosine[(int)x];
        }    
        else if (GraphicsFrame.cmbFunc.getSelectedItem().equals("blackman")) {
            GraphicsFrame.lblNameFunc.setText("blackman");
            return GraphicsFrame.a * blackman[(int)x];
        }           
        else      
            return GraphicsFrame.a * test[(int)x];
        
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Line2D line = null;
        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(Color.gray);
        Line2D lCoordX = new Line2D.Double(0, getSize().height / 2,getSize().width, getSize().height / 2);
        Line2D lCoordY = new Line2D.Double(getSize().width / 2, 0, getSize().width / 2, getSize().width);
           
        g2.draw(lCoordX);
        g2.draw(lCoordY);

        //g2.drawString("X", getSize().width - 10, getSize().height / 2 - 15);
        //g2.drawString("Y", getSize().width / 2 - 15, 0 + 10);
        g2.drawString("0", getSize().width / 2 - 10, getSize().height / 2 + 15);
        
        g2.translate(getSize().width / 2, getSize().height / 2);
        
        //Numbers for coordinates//
        //X right numbers
        
        int xr = 50;
        for(int i = 10 ; i < getSize().width - 15; i += 10) {
            g2.drawLine(i,  - 2, i, + 2);
            //g2.drawString(Integer.toString(xr / 10).toString(), xr, 20);
            xr += 50;
            
        }
        //X left numbers
        int xl = -50;
        for(int i = -10 ; i > -getSize().width + 15; i -= 10) {
            g2.drawLine(i,  - 2, i, + 2);
            //g2.drawString(Integer.toString(xl / 10).toString(), xl, 20);
            xl -= 50;
        }
        //Y up numbers
        int yu = -50;
        for(int i = -10 ; i > -getSize().height + 15; i -= 10) {
            g2.drawLine(-2,  i, +2, i);
            //g2.drawString(Integer.toString(-yu / 10).toString(), -25, yu);
            yu -= 50;
        }
        //Y down numbers
        int yd = 50;
        for(int i = 10 ; i < getSize().height + 15; i += 10) {
            g2.drawLine(-2,  i, +2, i);
            //g2.drawString(Integer.toString(-yd / 10).toString(), -25, yd);
            yd += 50;
        }
        
        //End of numbers for coordinates//
        
        for(int x = -s/2; x < s/2-1; x++ ) {
            g2.setPaint(Color.red);

            line = new Line2D.Double(x*2, -(int)f(x+s/2), x*2+1 , -(int)f(x+s/2+1));
            g2.draw(line);
        }       
        
    }
}


