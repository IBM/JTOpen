///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VSysvalDatePane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import javax.swing.JDialog;
import java.text.DateFormat;

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
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import javax.swing.JPanel;
import java.awt.Frame;
import java.awt.Component;
import java.util.Date;
import java.util.TimeZone;

import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

    /**
     * The VSysvalDatePane class defines a panel for modifying system date and time.
    **/
    class VSysvalDatePane
    {
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

        /**
         * Date and time formatter.
         *
        **/
        private DateFormat formatter_;

        /**
         * Textfield for editing the system date and system time.
         *
        **/
        private JTextField jTextField_;

        /**
         * Holder of SystemValue object.
         *
        **/
        private VSystemValue systemValue_;


        // MRI
        final private static String modifyActionTab_;
        final private static String valueName_;
        final private static String valueDescription_;
        final private static String valueInformation_;
        private static DateFormat dateFormat_ = DateFormat.getDateInstance();
        private static DateFormat timeFormat_ = DateFormat.getTimeInstance();

        static
        {

			modifyActionTab_ = (String)ResourceLoader.getText("ACTION_MODIFY");
			valueName_ = (String)ResourceLoader.getText("COLUMN_NAME");
			valueDescription_ = (String)ResourceLoader.getText("COLUMN_DESCRIPTION");
			valueInformation_ = (String)ResourceLoader.getText("COLUMN_VALUE");
                        dateFormat_.setTimeZone (TimeZone.getDefault ());
                        timeFormat_.setTimeZone (TimeZone.getDefault ());
        }
        

        /**
         * Constructs a VSysvalDatePane object.
         *
        **/
        VSysvalDatePane(VSystemValue systemValue)
        {
            systemValue_ = systemValue;
            if (systemValue.getName().equals("QDATE"))
              formatter_ = dateFormat_;
            else if (systemValue.getName().equals("QTIME"))
              formatter_ = timeFormat_;
            formatter_.setLenient(false);
        }


        /**
         * Listens to the key event of the textfield for content change detecting.
         *
        **/
        protected void addKeyPressedNotification(KeyListener l)
        {
            jTextField_.addKeyListener(l);
        }

        /**
         * Returns the actual component.
         *
        **/
        protected Component getComponent()
        {
            JTabbedPane jTabbe0= new JTabbedPane();
            jTabbe0.addTab( modifyActionTab_, ((Icon)null), getModifyTab());

            return ((Component)jTabbe0);
        }

        /**
         * Returns the date in the textfield.
         *
        **/
        protected java.util.Date getDate()
        {
            java.util.Date d = null;

            try
            {
                String dateString = jTextField_.getText();

                if (systemValue_.getName().equals("QDATE"))
                  d = new java.sql.Date(formatter_.parse(dateString).getTime());
                else if (systemValue_.getName().equals("QTIME"))
                  d = new java.sql.Time(formatter_.parse(dateString).getTime());
            }
            catch(Exception e)
            {
                systemValue_.errorEventSupport_.fireError(e);
            }

            return d;
        }


        /**
         * Returns the tabbed panel component.
         *
        **/
        private Component getModifyTab()
        {
            JPanel jPanel0= new JPanel();
            GridBagLayout gridBa2= new GridBagLayout();
            jPanel0.setLayout(((LayoutManager)gridBa2));
            jPanel0.setBorder(((Border)new EmptyBorder(10, 10, 10, 10)));
            int int3= 0;
            int3++;
            VUtilities.constrain(valueName_+": ",systemValue_.getName(), jPanel0, gridBa2, int3++);
            int3++;
            VUtilities.constrain(((Component)new JLabel(valueInformation_+": ")), jPanel0, gridBa2, 0, int3, 1, 1);

            String dstring = null;
            dstring = formatter_.format((java.util.Date)systemValue_.getValue());

            jTextField_ = new JTextField(dstring.toString());

            VUtilities.constrain(jTextField_, jPanel0, gridBa2, 1, int3, 1, 1);
            int3++;
            VUtilities.constrain(valueDescription_+": ",
                systemValue_.getDescription(), jPanel0, gridBa2, int3++);

            return ((Component)jPanel0);
        }
    }
