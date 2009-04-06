package myctapp;

import myctapp.gui.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.geom.*;
import javax.swing.border.EtchedBorder;
import java.util.*;
import java.text.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**jpanel class to display array and graph info*/
public class DataFrame extends InternalImageFrame implements PropertyChangeListener{
    
    JPanel main;
    JPanel dpanel;
    LogArea  lpanel, fftlpanel;
    Vector data;      //stores all the arrays to be displayed
    JButton button;
    JComboBox cmbFunc, trigFunc;
    double[] test, ffttest;
    int current; int s = 128; double freq = 1; double gswidth = 0.1;
    JFormattedTextField gwidthField, freqField, samplesField, zoomField;
    Color[] myColors;
    int choice = 1;
    double zoom = 1;
    boolean absfft = true;
    
    public DataFrame(){
        setPreferredSize(new Dimension(100, 80));
        setFont(new Font("SansSerif", 0, 10));
        setBackground(Color.white);
        setTitle("Data Grapher");
        data = new Vector(4);
        createColors();
    }
    
    public void setupPanels(){
        Font theMainFont = new Font("SansSerif", Font.PLAIN, 11);
        UIManager.put("InternalFrame.activeTitleBackground", Color.yellow);
        UIManager.put("Button.font", theMainFont );
        UIManager.put("Label.font", theMainFont );
        UIManager.put("ComboBox.font", theMainFont );
        final JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        final ArrayGraphPanel gpanel = new ArrayGraphPanel();
        gpanel.fft = false;
        final ArrayGraphPanel fftgpanel = new ArrayGraphPanel();
        fftgpanel.fft = true;
        final ArraysComboBox arrlistbox = new ArraysComboBox();
        arrlistbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                current = arrlistbox.getSelectedIndex();
                if (current >= 0){
                    test = (double[]) data.get(current);
                    lpanel.updatecurrdata(test);
                    fftlpanel.updatecurrdata(test);
                    System.out.println(current);
                }
            }
        });
        lpanel = new LogArea();
        lpanel.setBorder(BorderFactory.createLineBorder(Color.black));
        fftlpanel = new LogArea(); fftlpanel.fft = true;
        fftlpanel.setBorder(BorderFactory.createLineBorder(Color.black));
        JPanel widgetspanel = new JPanel();
        widgetspanel.setMinimumSize(new Dimension(200, 10));
        widgetspanel.setMaximumSize(new Dimension(600, 30));
        widgetspanel.setBorder(new EtchedBorder());
        
        button = new JButton("Load Data");
        
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                final JFrame frame = new JFrame();  //temp frame to act as parent!!
                FileDialog fd = new FileDialog(frame, "Load Data");
                fd.show();
                if (fd.getFile() == null) return;
                String path = fd.getDirectory() + fd.getFile();
                double[] temp = Utils.importArrayData(path);
                if (temp.length < s && current > 0){
                    temp = pad(temp);
                }
                data.add(temp);
                if (current <= 0) {
                    s = temp.length;
                    samplesField.setValue(new Integer(s));
                }
                arrlistbox.update();
                lpanel.updatecurrdata(temp);
                fftlpanel.updatecurrdata(temp);
                repaint();
            }
        });
        widgetspanel.add(button);
        
        button = new JButton("Diff");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                try {
                    test = (double[]) data.get(current);
                    Differentiate();
                    lpanel.updatecurrdata(test);
                    fftlpanel.updatecurrdata(test);
                    repaint();
                } catch (ArrayIndexOutOfBoundsException e){};
            }
        });
        widgetspanel.add(button);
        
        cmbFunc = new JComboBox();
        cmbFunc.addItem("ABSFFT");
        cmbFunc.addItem("FFT");
        cmbFunc.addItem("IFFT");
        
        cmbFunc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String s = (String) cmbFunc.getSelectedItem();
                //test = (double[]) data.get(current);
                try {
                    if (s == "FFT"){
                        absfft = false;
                        choice = 1;    
                    } else if (s == "IFFT"){
                        choice = 0;
                    } else if (s == "ABSFFT"){
                        absfft = true;
                    }
                    lpanel.updatecurrdata(test);
                    fftlpanel.updatecurrdata(test);
                    repaint();
                } catch (NullPointerException ne){};
            }
        });
        widgetspanel.add(cmbFunc);
        
        trigFunc = new JComboBox();
        trigFunc.addItem("sin");
        trigFunc.addItem("cos");
        trigFunc.addItem("gauss");
        trigFunc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String s = (String) trigFunc.getSelectedItem();
                addData(s);
                lpanel.updatecurrdata(test);
                fftlpanel.updatecurrdata(test);
                arrlistbox.update();
                repaint();
            }
        });
        widgetspanel.add(trigFunc);
        arrlistbox.update();
        widgetspanel.add(arrlistbox);
        
        button = new JButton("Del Data");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                data.remove(current);
                arrlistbox.update();
                repaint();
            }
        });     
        widgetspanel.add(button);
        
        main.add(widgetspanel);
        JPanel entrypanel = new JPanel();

        entrypanel.setMinimumSize(new Dimension(400, 20));
        entrypanel.setMaximumSize(new Dimension(400, 50));
        NumberFormat format = NumberFormat.getNumberInstance();
        JLabel gwidthLabel = new JLabel("Gs. Width"); 
        gwidthField = new JFormattedTextField(format);
        gwidthField.setValue(new Double(0.1));
        gwidthField.setColumns(3);
        gwidthField.addPropertyChangeListener("value", this);   
        gwidthLabel.setLabelFor(gwidthField);
        entrypanel.add(gwidthLabel);
        entrypanel.add(gwidthField);      
        
        JLabel freqLabel = new JLabel("Func Freq"); 
        freqField = new JFormattedTextField(format);
        freqField.setValue(new Integer(1));
        freqField.setColumns(3);
        freqField.addPropertyChangeListener("value", this);   
        freqLabel.setLabelFor(freqField);
        entrypanel.add(freqLabel);
        entrypanel.add(freqField); 
        JLabel samplesLabel = new JLabel("No Samples"); 
        samplesField = new JFormattedTextField(format);
        samplesField.setValue(new Integer(128));
        samplesField.setColumns(3);
        samplesField.addPropertyChangeListener("value", this);   
        samplesLabel.setLabelFor(samplesField);
        entrypanel.add(samplesLabel);
        entrypanel.add(samplesField);
        JLabel zoomLabel = new JLabel("Zoom"); 
        zoomField = new JFormattedTextField(format);
        zoomField.setValue(new Integer(1));
        zoomField.setColumns(3);
        zoomField.addPropertyChangeListener("value", this);   
        zoomLabel.setLabelFor(zoomField);
        entrypanel.add(zoomLabel);
        entrypanel.add(zoomField);       
        main.add(entrypanel);
        
        main.add(Box.createRigidArea(new Dimension(0,6)));
        Container container = this.getContentPane();
        main.add(gpanel);
        main.add(Box.createRigidArea(new Dimension(0,6)));
        main.add(fftgpanel);
        
        JPanel bottompanel = new JPanel();
        //bottompanel.setLayout(new BorderLayout());
        bottompanel.setMinimumSize(new Dimension(400, 60));
        bottompanel.setMaximumSize(new Dimension(1000, 75));  
        bottompanel.add(lpanel);
        bottompanel.add(fftlpanel);
        bottompanel.setBorder(new EtchedBorder());
        JPanel buttpanel = new JPanel();
        buttpanel.setLayout(new GridLayout(2, 1));
        buttpanel.setMinimumSize(new Dimension(30, 50));
        buttpanel.setMaximumSize(new Dimension(90, 65));
        button = new JButton("Copy");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                repaint();
            }
        }); 

        buttpanel.add(button);
        button = new JButton("Clear");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                data.clear();
                arrlistbox.update();
                repaint();
            }
        });
        buttpanel.add(button);
        bottompanel.add(buttpanel);
        main.add(bottompanel); 
        container.add(main);
    }
    
    public double[] pad(double[] data){
        
        double[] res = new double[s];
        int l = data.length;
        
        for (int x = 0; x < l; x++) {
            res[x] = data[x];
        }
        for (int x = l; x < s; x++) {
            res[x] = 0;
        }        
        return res;
    }
    
    /**create colors for graphing */
    public void createColors(){
        myColors = new Color[25];
        for (int i=1; i < 25; i++){
            int rd = (int) (255*Math.random());
            int gr = (int) (200*Math.random());
            int bl = (int) (200*Math.random());
            myColors[i] = new Color(rd, gr, bl);
        }
    }
    
    public void makeData(double[] data){
        
        s = data.length;
        test = new double[s];
        for ( int i = 0; i < s; i++ ) {
            test[i] = data[i];
        }
        
    }
    
    public void Differentiate(){
        //double df = ( f(x+dx) - f(x) )/dx ;
        for (int x = 0; x < s-1; x++) {
            test[x] = (test[x+1] - test[x]);
        }
        test[s-1]=0;
    }
    

   public void getFFT(double[] data, int ch){
       double[] idata = new double[s];

        if (ch == 1){
            for (int i = 0; i < s; i++) {
                idata[i] = 0;
            }
            Filters.FFT(1, s, data, idata);
        } else if (ch == 0) {
            Filters.FFT(0, s, data, idata);
        }
        if (absfft == true){
            for (int x = 0; x < s; x++) {
                data[x] = Math.abs(data[x]);
            }
        }
    }
            
    public double[] fsinX(double f) {
        double[] temp = new double[s];
        for (int x = 0; x < s; x++) {
            double t = ((double) x) * 180 /s;
            temp[x] = Math.sin(Math.toRadians(2*t*f));
        }
        return temp;
    }
    
    public double[] fcosX(double f) {
        double[] temp = new double[s];
        for (int x = 0; x < s; x++) {
            double t = ((double) x) * 180 /s;
            temp[x] = Math.cos(Math.toRadians(2*t*f));
            //temp[x] = Math.cos(Math.toRadians(2*t*f))+ Math.cos(Math.toRadians(2*t*f*16))
             //   + Math.cos(Math.toRadians(2*t*f*3));
        }
        return temp;
    }
    
    public double[] fgaussianX(){
        double[] gauss = new double[s];
        int i;
        double width = s*gswidth;
        double sigma = width/2.3548;
        double half = (s-1)/2;
        for (i=0; i<s; i++)
            gauss[i] = (double) ((1.0 / (Math.sqrt(2.0*Math.PI)*sigma)) *
            Math.exp(-Math.pow((i-half),2)/(2*Math.pow(sigma,2))));
        return gauss;
    }
    
    public void addData(String func){
        if (func == "sin") {
            test = fsinX(freq);
        }
        else if (func == "cos") {
            test = fcosX(freq);
        }
        else if (func == "gauss") {
            test = fgaussianX();
        }
        data.add(test);
        
    }


    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.gray);
        g.setColor(Color.black);
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        Object source = evt.getSource();
        if (source == freqField) {
            this.freq = ((Number)freqField.getValue()).doubleValue();
        }
        else if (source == samplesField) {
            s = ((Number)samplesField.getValue()).intValue();
        }        
        else if (source == zoomField) {
            zoom = ((Number)zoomField.getValue()).doubleValue();
            repaint();
        }  
        else if (source == gwidthField) {
            gswidth = ((Number)gwidthField.getValue()).doubleValue();
            repaint();
        }          
    }
    
    //internal class to implement the selection of current data set
    class ArraysComboBox extends JComboBox {
        double[] temp;
        
        public void ArraysComboBox(){
            
        }
        
        public void update(){
            try{
                this.removeAllItems();
                int elems = data.size();
                for (int e=0; e<elems; e++){
                    //temp = (double[]) data.get(e);
                    this.addItem("data"+e);
                }
                current = data.indexOf(data.lastElement());
                this.setSelectedIndex(current);
            } catch (NoSuchElementException ne){};
        }       
    }
    
    //internal class to draw graphs of arrays data
    class ArrayGraphPanel extends JPanel {
        
        int y;
        boolean fft;
   
        public void ArrayGraphPanel(){
             
        }
        
        public void normalizeData(double data[], int y){
            double min = Utils.getMin(data);
            double max = Utils.getMax(data);
            //rescale values to fit on graph panel
            for ( int i = 0; i < s; i++ ) {
                data[i] = ((data[i])*y)/(max-min);
            }
        }

        double f(double x) {
            if (fft == true){
                return (ffttest[(int)x]);
            } else {
                return (test[(int)x]); 
            } 
        }
        
        public void paintComponent(Graphics g) {
            this.setBackground(Color.white);
            super.paintComponent(g);
            Line2D line = null;
            Graphics2D g2 = (Graphics2D)g;
            g2.setStroke(new BasicStroke(2));
            g2.setColor(Color.gray);
            Line2D lCoordX = new Line2D.Double(0, getSize().height / 2,getSize().width, 
            getSize().height / 2);
            Line2D lCoordY = new Line2D.Double(6, 0, 6, getSize().width);

            g2.draw(lCoordX);
            g2.draw(lCoordY);

            //g2.translate(getSize().width / 2, getSize().height / 2);

            //Numbers for coordinates//
            //X right numbers

            int xr = 50;
            for(int i = 10 ; i < getSize().width - 15; i += 10) {
                g2.drawLine(i,  getSize().height / 2- 2, i, getSize().height / 2+ 2);
                //g2.drawString(Integer.toString(xr / 10).toString(), xr, 20);
                xr += 50;
            }
            //X left numbers
            int xl = -50;
            for(int i = -10 ; i > -getSize().width + 15; i -= 10) {
                g2.drawLine(i, getSize().height / 2 - 2, i, getSize().height / 2+ 2);
                //g2.drawString(Integer.toString(xl / 10).toString(), xl, 20);
                xl -= 50;
            }
            //Y up numbers
            int yu = -50;
            for(int i = -10 ; i > -getSize().height + 15; i -= 10) {
                g2.drawLine(4,  i, 8, i);
                //g2.drawString(Integer.toString(-yu / 10).toString(), -25, yu);
                yu -= 50;
            }
            //Y down numbers
            int yd = 50;
            for(int i = 10 ; i < getSize().height + 15; i += 10) {
                g2.drawLine(4, i, 8, i);
                //g2.drawString(Integer.toString(-yd / 10).toString(), -25, yd);
                yd += 50;
            }
            
            if (test != null && current >= 0){
                //g2.drawString("X", getSize().width - 10, getSize().height / 2 - 15);
                //g2.drawString("Y", getSize().width / 2 - 15, 0 + 10);
                g2.drawString("0", 20, getSize().height / 2 + 15);
                g2.drawString(Integer.toString(test.length), getSize().width -25,
                getSize().height / 2 + 15);
                //End of numbers for coordinates//
                int elems = data.size();
                for (int e=0; e<elems; e++){
                    if (fft == true){
                        test = (double[]) data.get(e);
                        ffttest = new double[test.length];
                        for (int i=0; i<s; i++){
                           ffttest[i] = test[i]; 
                        }
                        getFFT(ffttest, choice);
                    } else{
                        test = (double[]) data.get(e);
                    }
                    g2.setPaint(myColors[e+1]);
                    for(int x = 0; x < s-1; x++ ) {
                        double r;
                        y = getSize().height/2;
                        if (fft == true) { normalizeData(ffttest, y-6); }
                        else normalizeData(test, y-6);
                        double scale = getSize().width/s;
                        r = getSize().width/s * zoom;
                        
                        line = new Line2D.Double(x*r+6, y-(int)f(x), x*r+r+6 , y-(int)f(x+1));
                        g2.draw(line);
                        
                    }
                }
                test = (double[]) data.get(current);
            }
            g2.setPaint(Color.black);
            if (fft == true) g2.drawString("FFT", 20, 15);
        }
    }

    /**A textarea class */
    class LogArea extends JPanel{
        JTextArea ta;
        JScrollPane sp;
        boolean fft = false;
        
        public LogArea(){
            setPreferredSize(new Dimension(180, 80));
            setMinimumSize(new Dimension(70, 80));
            setMaximumSize(new Dimension(300, 80));
            setFont(new Font("Monospaced", 0, 11));
            ta = new JTextArea(10,25);
            JScrollPane sp = new JScrollPane(ta);
            sp.setPreferredSize(new Dimension(175, 70));
            sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            this.add(sp);

        }
        
        public void updatecurrdata(int[] data){
            ta.setText("");
            for (int i=0;i<data.length;i++)  {
                //ta.append((i+1)+"     ");
                ta.append(Integer.toString(data[i])+"\n");
            }
            ta.setCaretPosition(ta.getText().length());
        }
        
        public void updatecurrdata(double[] data){
            ta.setText("");
            double[] fftdata = new double[data.length];
            if (fft == true){
                for (int i=0; i<s; i++){
                    fftdata[i] = data[i];
                }
                getFFT(fftdata, choice);
                for (int i=0;i<data.length;i++)  {
                    ta.append(Double.toString(fftdata[i])+"\n");
                }
                
            }
            else {
                for (int i=0;i<data.length;i++)  {
                    ta.append(Double.toString(data[i])+"\n");
                }
            }
            ta.setCaretPosition(ta.getText().length());
        }
        
    }
    
    /**run as a seperate app*/
    public static void main(String[] args) {
        
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                DataFrame app = new DataFrame();
                //when window is closed application exits
                app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                app.pack();
                app.setVisible(true);
            }
        });
    }
}
  
