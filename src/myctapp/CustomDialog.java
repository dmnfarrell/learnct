/*
 * CustomDialog.java
 *
 * Created on 09 March 2006, 11:51
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package myctapp;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import ij.plugin.frame.*;
import javax.swing.*;

public class CustomDialog extends Dialog implements ActionListener {
    private JPanel myPanel = null;
    private JButton colsButton = null;
    private JButton rowsButton = null;
    private boolean answer = false;
    public boolean getAnswer() { return answer; }
    
    public CustomDialog(JFrame frame, boolean modal, String myMessage) {
        super(frame, modal);
        myPanel = new JPanel();
        add(myPanel);
        setTitle("Import Options");
        //setPreferredSize(new Dimension(200,100));
        setLayout(new GridLayout(1,4,1,1));
        myPanel.add(new JLabel(myMessage));
        colsButton = new JButton("Columns");
        colsButton.addActionListener(this);
        myPanel.add(colsButton);
        rowsButton = new JButton("Rows");
        rowsButton.addActionListener(this);
        myPanel.add(rowsButton);
        pack();
        setSize(new Dimension(200,100));	
        //setLocationRelativeTo(frame);
        setLocation(frame.getWidth()/2, frame.getHeight()/2);
        setVisible(true);
        
    }
    
    public void actionPerformed(ActionEvent e) {
        if(colsButton == e.getSource()) {
            //System.out.println("User chose rows.");
            answer = true;
            setVisible(false);
        } else if(rowsButton == e.getSource()) {
            //System.out.println("User chose no.");
            answer = false;
            setVisible(false);
        }
    }
    
}
