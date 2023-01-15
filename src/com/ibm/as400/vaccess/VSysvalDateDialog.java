///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VSysvalDateDialog.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import javax.swing.JDialog;
import java.text.SimpleDateFormat;
import javax.swing.JPanel;

import com.ibm.as400.access.Trace;
import com.ibm.as400.access.SystemValue;
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

import java.util.Date;
import java.awt.Frame;
import java.awt.Component;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;


import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;


    /**
     * The VSysvalDateDialog is used for modifying system date and system time in 
     * System Value visual support.
     *
    **/
    class VSysvalDateDialog extends JDialog implements ActionListener, KeyListener
    {
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

        /**
         * Apply button.
         *
        **/
        private JButton applyButton_;

        /**
         * Cancel button.
         *
        **/
        private JButton cancelButton_;

    
        /**
         * Panel for modifying system date and system time.
         *
        **/
        private VSysvalDatePane datePane_;

        
        /**
         * OK button.
         *
        **/
        private JButton okButton_;


        /**
         * Holder of SystemValue instance.
         *
        **/    
        private VSystemValue systemValue_;

        /**
         * Holder of the parent Frame object.
         *
        **/
        private Frame frame_;


        // MRI
        final private static String okButtonText_;
        final private static String cancelButtonText_;
        final private static String applyButtonText_;
        final private static String modifyDialogTitle_;


        static
        {
			okButtonText_ = (String)ResourceLoader.getText("DLG_OK");
			cancelButtonText_ = (String)ResourceLoader.getText("DLG_CANCEL");
			applyButtonText_ = (String)ResourceLoader.getText("DLG_APPLY");
                        modifyDialogTitle_ = (String)ResourceLoader.getText("DLG_MODIFY_0"); // @C1C
        }
        


        /**
         * Constructs a VSysvalDateDialog object.
         * @param frame The parent Frame object.
         * @param systemValue The VSystemValue object.
         *
        **/
        VSysvalDateDialog(VSystemValue systemValue, Frame frame)
        {
            super(frame, ResourceLoader.substitute(modifyDialogTitle_, systemValue.getName()),true); // @C1C
            systemValue_ = systemValue;
            frame_ = frame;
            
            setSize(280,180);
            datePane_ = new VSysvalDatePane(systemValue_);

            JPanel buttonsPane = getButtonsPane();
            Container c = getContentPane();

            c.add("North",datePane_.getComponent());

            c.add("South",buttonsPane);

            datePane_.addKeyPressedNotification(this);

            pack();
        }

        /**
         * Fires changes that have been made in the modifying panel.
         *
        **/
        private void fireChange()
        {
            applyButton_.setEnabled(true);
        }

        /**
         * Returns the button panel.
         * @return The button panel.
         *
        **/
        private JPanel getButtonsPane()
        {
            JPanel pane=new JPanel();
            okButton_=new JButton(okButtonText_);
            cancelButton_=new JButton(cancelButtonText_);
            applyButton_=new JButton(applyButtonText_);
            applyButton_.setEnabled(false);
            pane.add(okButton_);
            pane.add(cancelButton_);
            pane.add(applyButton_);


            okButton_.addActionListener(this);
            applyButton_.addActionListener(this);
            cancelButton_.addActionListener(this);

    
            return pane;
        }
	    
         /**
          * Processes the action event.
          * @param e The action event.
          **/
            public void actionPerformed(ActionEvent e)
            {
                Object source=e.getSource();
                java.util.Date valueDate = null;

                if(source.equals(cancelButton_))
                {
                    this.dispose();
                }

                if(source.equals(okButton_))
                {
                    valueDate = datePane_.getDate();

                    if (valueDate == null)
                    {
                      this.dispose();
                      return;
                    }

                    systemValue_.setValue(valueDate);
                    applyButton_.setEnabled(false);

                    this.dispose();
                }

                if(source.equals(applyButton_))
                {
                    valueDate = datePane_.getDate();

                    if (valueDate == null)
                    {
                      return;
                    }

                    systemValue_.setValue(valueDate);
                    applyButton_.setEnabled(false);
                }
            }
    
        
            /**
             * The key is pressed.
             * @param e  The key event.
             *
            **/
            public void keyPressed(KeyEvent e)
            {
              if(e.getKeyCode()==127)
              {
                fireChange();
              }
            }

            /**
             * The key is released.
             * @param e  The key event.
             *
            **/

            public void keyReleased(KeyEvent e)
            {
            }

            /**
             * The key is Typed.
             * @param e  The key event.
             *
            **/

            public void keyTyped(KeyEvent e)
            {
            	fireChange();
            }
    }


