///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VSysvalTextDialog.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.Trace;
import com.ibm.as400.access.SystemValue;
import com.ibm.as400.access.SystemValueList;
import javax.swing.JDialog;
import javax.swing.JPanel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JList;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.JTabbedPane;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JRadioButton;
import java.awt.Frame;
import java.awt.Component;

import java.math.BigDecimal;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;


    /**
     * The class VsysvalTextDialog defines a dialog
     * for the user to modify the system values
     * of char type.
     **/    
    class VSysvalTextDialog extends JDialog
          implements ActionListener, KeyListener

    {
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

        // Private data.
        private JTextField valueText_;
        private VSystemValue systemValue_;
        private Frame frame_;
        private JButton okButton_    ;
        private JButton cancelButton_ ;
        private JButton applyButton_  ;

        // MRI
        final private static String okButtonText_;
        final private static String cancelButtonText_;
        final private static String applyButtonText_;
        final private static String modifyDialogTitle_;
        final private static String modifyActionTab_;
        final private static String valueName_;
        final private static String valueDescription_;
        final private static String valueInformation_;

        static
        {
			okButtonText_ = (String)ResourceLoader.getText("DLG_OK");
			cancelButtonText_ = (String)ResourceLoader.getText("DLG_CANCEL");
			applyButtonText_ = (String)ResourceLoader.getText("DLG_APPLY");
                        modifyDialogTitle_ = (String)ResourceLoader.getText("DLG_MODIFY_0"); // @C1C
			modifyActionTab_ = (String)ResourceLoader.getText("ACTION_MODIFY");
			valueName_ = (String)ResourceLoader.getText("COLUMN_NAME");
			valueDescription_ = (String)ResourceLoader.getText("COLUMN_DESCRIPTION");
			valueInformation_ = (String)ResourceLoader.getText("COLUMN_VALUE");
        }
        

        /** Constructs a VSysvalTextDialog object. It creates a dialog for the user 
         *  to modify the system value.
         *  @param systemValue  The VSystemValue to modify.
         *  @param frame        The frame in which this dialog resides.
         **/    
        public VSysvalTextDialog(VSystemValue systemValue, Frame frame)
        {
            super(frame, ResourceLoader.substitute(modifyDialogTitle_, systemValue.getName()), true); // @C1C
                
            systemValue_ = systemValue;
            frame_ = frame;

            int length = systemValue_.getDescription().length();
            if (length <=29)
                length=30;
    
            setSize(30+8*length,200);
            setResizable(false);
        
            JPanel line1 = new JPanel();

            okButton_    = new JButton(okButtonText_);
            cancelButton_ = new JButton(cancelButtonText_);
            applyButton_  = new JButton(applyButtonText_);
            applyButton_.setEnabled(false);
            okButton_.addActionListener(this);
            cancelButton_.addActionListener(this);
            applyButton_.addActionListener(this);

            line1.add(okButton_);
            line1.add(cancelButton_);
            line1.add(applyButton_);
        
            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.addTab(modifyActionTab_,getComponent());

            Container c=getContentPane();

            c.add("North",tabbedPane);

            c.add("South",line1);
        
            pack();
        }

        /**
         * Processes the action event.
         * @param e     The action event.
         **/
        public void actionPerformed(ActionEvent e)
        {
            if (e.getSource().equals(okButton_))
            {
              try
              {
                applyChanges();
                dispose();
              }
              catch(Exception x)
              {
                x.fillInStackTrace();
                systemValue_.errorEventSupport_.fireError(x);
              }
            }
            
            if (e.getSource().equals(cancelButton_)) 
                dispose();
                
            if (e.getSource().equals(applyButton_))
            {
              try
              {
                applyChanges();
                applyButton_.setEnabled(false);
              }
              catch(Exception x)
              {
                x.fillInStackTrace();
                systemValue_.errorEventSupport_.fireError(x);
              }
            }
        }
    
        /**
         * Processes the change action.
         **/
        public void applyChanges()
        {
            int type = systemValue_.getType();
            switch(type)
            {
                case SystemValueList.TYPE_INTEGER:
                    Integer intValue = null;
                    intValue = new Integer(valueText_.getText());
                    systemValue_.setValue(intValue);
                    break;
                case SystemValueList.TYPE_DECIMAL:
                    BigDecimal decValue = null;
                    decValue = new BigDecimal(valueText_.getText());
                    systemValue_.setValue(decValue);
                    break;
                case SystemValueList.TYPE_STRING:
                    systemValue_.setValue(valueText_.getText());
                    break;
            }
            Object value = null;
            value = systemValue_.getValue();
            applyButton_.setEnabled(false);
            systemValue_.load();
        }

        /**
         * Returns the component.
         * @return The component to display the content.
         **/
        private Component getComponent()
        {
            JPanel jPanel1= new JPanel();
            GridBagLayout gridBa2= new GridBagLayout();
            jPanel1.setLayout(((LayoutManager)gridBa2));
            jPanel1.setBorder(
                ((Border)new EmptyBorder(10, 10, 10, 10)));
    
            int int3= 0;
            int3++;
            VUtilities.constrain(valueName_ + ": ",
                                 systemValue_.getName(), 
                                 jPanel1, 
                                 gridBa2, 
                                 int3++);
    
            int3++;
    
            valueText_ = new JTextField();
            valueText_.addKeyListener(this);

            int valueType = systemValue_.getType();
            String valueString = null;

            Object valueObject = null;
            valueObject = systemValue_.getValue();

            Integer in = null;
            BigDecimal bd = null;

            switch(valueType)
            {
                case SystemValueList.TYPE_INTEGER:
                    in = (Integer) valueObject;
                    valueString = in.toString();
                    break;
                case SystemValueList.TYPE_DECIMAL:
                    bd = (BigDecimal) valueObject;
                    valueString = bd.toString();
                    break;
                case SystemValueList.TYPE_STRING:
                    valueString = (String) valueObject;
            	    break;
            }
            valueText_.setText(valueString);
            VUtilities.constrain(new JLabel( valueInformation_ + ": "), 
                                    jPanel1, 
                                    gridBa2, 
                                    0, 
                                    int3, 
                                    1, 
                                    1);
                                    
            VUtilities.constrain(((Component)valueText_), 
                                    jPanel1, 
                                    gridBa2, 
                                    1, 
                                    int3++, 
                                    1, 
                                    1);
            int3++;
            int length1 = systemValue_.getDescription().length();
            if(length1 < 29 )
            {
                StringBuffer sb = new StringBuffer(systemValue_.getDescription());
                for(int i=0;i <29- length1 ;i++)
                    sb.append(" ");
                VUtilities.constrain(valueDescription_+": ",
                                         sb.toString(), 
                                         jPanel1, 
                                         gridBa2, 
                                         int3++);
            }
            else
            {
                VUtilities.constrain(valueDescription_+": ",
                                        systemValue_.getDescription(), 
                                        jPanel1, 
                                        gridBa2, 
                                        int3++);
            }
            return ((Component)jPanel1);
        }

        /** 
    	 * Returns the copyright.
        **/
    	private static String getCopyright()
        {
    	    return Copyright_v.copyright;
        }
        
        /** The key is pressed.
         * @param keyEvent  The key event.
         **/    
        public void keyPressed(KeyEvent keyEvent)
        {
        }
    
        /** The key is released.
         * @param keyEvent  The key event.
         **/    
        public void keyReleased(KeyEvent keyEvent)
        {
        }

        /** The key is typed.
         * @param keyEvent  The key event.
         **/    
        public void keyTyped(KeyEvent keyEvent)
        {
          applyButton_.setEnabled(true);
        }   
    }


