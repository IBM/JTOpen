///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VSystemValueDetailsPropertiesPane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import java.lang.String;
import java.lang.Integer;
import java.lang.Boolean;


import com.ibm.as400.access.SystemValue;
import com.ibm.as400.access.SystemValueList;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JList;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.awt.event.KeyListener;
import java.awt.event.ItemListener;

/**
 * The VSystemValueDetailsPropertiesPane class defines the  property panel
 * of a system value on an AS/400 for use in various models 
 * and panes in this package.
 * 
 * <p>Most errors are reported as ErrorEvents rather than
 * throwing exceptions.  Users should listen for ErrorEvents
 * in order to diagnose and recover from error conditions.
 * 
 * <p>VSystemValueDetailsPropertiesPane objects generate the following events:
 * <ul>
 *     <li>ErrorEvent
 *     <li>PropertyChangeEvent
 *     <li>VObjectEvent
 *     <li>WorkingEvent
 * </ul>
**/
class  VSystemValueDetailsPropertiesPane
    implements VPropertiesPane
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Private data.
    private VSystemValue object_;
    private SystemValue systemValue_;


    // Event support.
    private ChangeEventSupport changeEventSupport_;
    private ErrorEventSupport errorEventSupport_;
    private VObjectEventSupport objectEventSupport_;
    private WorkingEventSupport workingEventSupport_;

    // MRI
    final private static String valueDescription_ = (String)ResourceLoader.getText("COLUMN_DESCRIPTION") + ": ";
    final private static String valueInformation_ = (String)ResourceLoader.getText("COLUMN_VALUE") + ": ";
    final private static String generalTabText_ = (String)ResourceLoader.getText("TAB_GENERAL");

    private static DateFormat dateFormat_ = DateFormat.getDateInstance();
    private static DateFormat timeFormat_ = DateFormat.getTimeInstance();

    static
    {
      dateFormat_.setTimeZone (TimeZone.getDefault ());
      timeFormat_.setTimeZone (TimeZone.getDefault ());
    }
        


    /**
     * Constructs a VSystemValueDetailsPropertiesPane object. This creates a 
     * property panel.
     * @param   systemValue         The VSystemValue object.
     **/
    public VSystemValueDetailsPropertiesPane(VSystemValue vSystemValue,
                                             SystemValue systemValue)
    {
        if( systemValue != null)
        {
            systemValue_ = systemValue;
        }
        changeEventSupport_= new ChangeEventSupport(this);
        errorEventSupport_= new ErrorEventSupport(this);
        objectEventSupport_= new VObjectEventSupport(this);
        workingEventSupport_= new WorkingEventSupport(this);
        object_ = vSystemValue;
    }

    /**
     * Adds the specified change listener
     * to receive change event from this
     * component.
     * @param listener      The property change 
     *                      listener.
    **/
    public void addChangeListener(
        ChangeListener listener)
    {
        changeEventSupport_.addChangeListener(listener);
    }

    /**
     * Adds the specified error listener
     * to receive error event from this
     * component.
     * @param listener      The error listener.
    **/    
    public void addErrorListener(
        ErrorListener listener)
    {
        errorEventSupport_.addErrorListener(listener);
    }

    /**
     * Adds the specified VObject listener
     * to receive VObject event from this
     * component.
     * @param listener      The VObject listener.
   **/
    public void addVObjectListener(
        VObjectListener listener)
    {
        objectEventSupport_.addVObjectListener(listener);
    }

    /**
     * Adds the specified working listener
     * to receive working event from this
     * component.
     * @param listener      The working listener.
   **/
    public void addWorkingListener(
        WorkingListener listener)
    {
        workingEventSupport_.addWorkingListener(listener);
    }

    /**
     * Processes the change event.
     **/
    public void applyChanges() throws Exception
    {
    }

    /**
     * Returns the display tabbed panel for array values.
     * @return The display tabbed panel for array values.
     **/
    private Component getArrayTab()
    {
        // Initialize the general tab.
        JPanel generalTab = new JPanel ();
        GridBagLayout layout = new GridBagLayout ();
        GridBagConstraints constraints;
        generalTab.setLayout (layout);
        generalTab.setBorder (new EmptyBorder (10, 10, 10, 10));

        // Icon and name.
        int row = 0;
        VUtilities.constrain (new JLabel (object_.getText (), object_.getIcon (32, false),  SwingConstants.LEFT),
            generalTab, layout, 0, row++, 2, 1);

        // Description.
        VUtilities.constrain (valueDescription_,
            object_.getDescription (), generalTab, layout, row++);

        // Array of values.
        try
        {
          Object[] objs = (Object[])systemValue_.getValue();
          int count = objs.length;
          String[] values = new String[count];
          for (int i=0;i<count;i++)
          {
            values[i] = (String)objs[i];
          }

          VUtilities.constrain (new JLabel(valueInformation_),
              new JList(values), generalTab, layout, row++);
        }
        catch(Exception e)
        {
          errorEventSupport_.fireError(e);
        }
        return generalTab;
    }

    /**
     * Returns the display panel.
     * @return The display panel.
     **/
    public Component getComponent()
    {
        JTabbedPane pane = new JTabbedPane();
        switch(systemValue_.getType())
        {
            case SystemValueList.TYPE_INTEGER:
            case SystemValueList.TYPE_STRING:
            case SystemValueList.TYPE_DECIMAL:
                pane.addTab(generalTabText_, getDisplayTab());
                break;
            case SystemValueList.TYPE_ARRAY:
                pane.addTab(generalTabText_, getArrayTab());
                break;
            case SystemValueList.TYPE_DATE:
                pane.addTab(generalTabText_, getDateTab());
                break;
        }
        pane.setSelectedIndex(0);
        return pane;
    }

    /** 
     * Returns the copyright.
    **/
    private static String getCopyright()
    {
        return Copyright_v.copyright;
    }

    /**
     * Returns the display tabbed panel for Date values.
     * @return The display tabbed panel for Date values.
     *
     **/
    private Component getDateTab()
    {
        // Initialize the general tab.
        JPanel generalTab = new JPanel ();
        GridBagLayout layout = new GridBagLayout ();
        GridBagConstraints constraints;
        generalTab.setLayout (layout);
        generalTab.setBorder (new EmptyBorder (10, 10, 10, 10));

        DateFormat formatter = null;
        if (systemValue_.getName().equals("QDATE"))
            formatter = dateFormat_;
        else if (systemValue_.getName().equals("QTIME"))
            formatter = timeFormat_;

        // Icon and name.
        int row = 0;
        VUtilities.constrain (new JLabel (object_.getText (), object_.getIcon (32, false),  SwingConstants.LEFT),
            generalTab, layout, 0, row++, 2, 1);

        // Description.
        VUtilities.constrain (valueDescription_,
            object_.getDescription (), generalTab, layout, row++);

        // Value.
        try
        {
          VUtilities.constrain (valueInformation_,
              formatter.format(systemValue_.getValue()), generalTab, layout, row++);
        }
        catch(Exception e)
        {
          errorEventSupport_.fireError(e);
        }

        return generalTab;
    }

    /**
     * Returns the display tabbed panel for char values.
     * @return The display tabbed panel for char values.
     *
     **/
    private Component getDisplayTab()
    {
        // Initialize the general tab.
        JPanel generalTab = new JPanel ();
        GridBagLayout layout = new GridBagLayout ();
        GridBagConstraints constraints;
        generalTab.setLayout (layout);
        generalTab.setBorder (new EmptyBorder (10, 10, 10, 10));

        // Icon and name.
        int row = 0;
        VUtilities.constrain (new JLabel (object_.getText (), object_.getIcon (32, false),  SwingConstants.LEFT),
            generalTab, layout, 0, row++, 2, 1);

        // Description.
        VUtilities.constrain (valueDescription_,
            object_.getDescription (), generalTab, layout, row++);

        // Value.
        VUtilities.constrain (valueInformation_,
            object_.getValue().toString(), generalTab, layout, row++);

        return generalTab;
    }



    /**
     * Removes a change listener.
     * 
     * @param  listener    The listener.
    **/
    public void removeChangeListener(
        ChangeListener listener)
    {
       changeEventSupport_.removeChangeListener(listener);
    }

    /**
     * Removes an error listener.
     * 
     * @param  listener    The listener.
    **/
    public void removeErrorListener(
        ErrorListener listener)
    {
        errorEventSupport_.removeErrorListener(listener);
    }

    /**
     * Removes a VObjectListener.
     * 
     * @param  listener    The listener.
    **/
    public void removeVObjectListener(
        VObjectListener listener)
    {
        objectEventSupport_.removeVObjectListener(listener);
    }

    /**
     * Removes a working listener.
     * 
     * @param  listener    The listener.
    **/
    public void removeWorkingListener(
        WorkingListener listener)
    {
        workingEventSupport_.removeWorkingListener(listener);
    }


}

