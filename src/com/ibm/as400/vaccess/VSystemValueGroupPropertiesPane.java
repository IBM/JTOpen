///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VSystemValueGroupPropertiesPane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.SystemValue;
import com.ibm.as400.access.SystemValueList;

import javax.swing.ComboBoxEditor;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.event.ItemListener;
import java.awt.event.KeyListener;

/**
 * The VSystemValueGroupPropertiesPane class defines the property panel
 * of a system value group on an AS/400 for use in various models 
 * and panes in this package.
 * 
 * <p>Most errors are reported as ErrorEvents rather than
 * throwing exceptions.  Users should listen for ErrorEvents
 * in order to diagnose and recover from error conditions.
 * 
 * <p>VSystemValueGroup objects generate the following events:
 * <ul>
 *     <li>ErrorEvent
 *     <li>PropertyChangeEvent
 *     <li>VObjectEvent
 *     <li>WorkingEvent
 * </ul>
**/

class VSystemValueGroupPropertiesPane implements VPropertiesPane
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Private data.
    private VSystemValueGroup object_;

    // MRI
    private static final String generalTabText_       = ResourceLoader.getText ("TAB_GENERAL");
    private static final String descriptionText_      = ResourceLoader.getText ("COLUMN_DESCRIPTION") + ": ";

    // Event support.
    ChangeEventSupport changeEventSupport_;
    ErrorEventSupport errorEventSupport_;
    VObjectEventSupport objectEventSupport_;
    WorkingEventSupport workingEventSupport_;
    
    /**
     * Constructs a VSystemValueGroupPropertiesPane object. It creates a 
     * properties panel for VSystemValueGroup.
     * @param   vSystemValueGroup       The specified system values group.
     * @param   systemValueList         The list of the system values.
     **/
    public VSystemValueGroupPropertiesPane(VSystemValueGroup vSystemValueGroup)
    {
        changeEventSupport_= new ChangeEventSupport(this);
        errorEventSupport_= new ErrorEventSupport(this);
        objectEventSupport_= new VObjectEventSupport(this);
        workingEventSupport_= new WorkingEventSupport(this);
        object_= vSystemValueGroup;     
    }

    /**
     * Adds the specified change listener
     * to receive change event from this
     * component.
     * @param listener      The property change 
     *                      listener.
    **/
    public void addChangeListener(ChangeListener listener)
    {
         changeEventSupport_.addChangeListener(listener);
    }

    /**
     * Adds the specified error listener
     * to receive error event from this
     * component.
     * @param listener      The error listener.
    **/    
    public void addErrorListener(ErrorListener listener)
    {
         errorEventSupport_.addErrorListener(listener);
    }

    /**
     * Adds the specified VObject listener
     * to receive VObject event from this
     * component.
     * @param listener      The VObject listener.
   **/
    public void addVObjectListener(VObjectListener listener)
    {
         objectEventSupport_.addVObjectListener(listener);
    }

    /**
     * Adds the specified working listener
     * to receive working event from this
     * component.
     * @param listener      The working listener.
   **/
    public void addWorkingListener(WorkingListener listener)
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
     * Returns the component.
     * @return The component to display the properties.
     **/
    public Component getComponent()
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
        VUtilities.constrain (descriptionText_,
            object_.getDescription (), generalTab, layout, row++);

        // Build the pane.
        JTabbedPane pane = new JTabbedPane ();
        pane.addTab (generalTabText_, null, generalTab);
        pane.setSelectedIndex (0);
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
     * Removes a change listener.
     * 
     * @param  listener    The listener.
    **/
    public void removeChangeListener(ChangeListener listener)
    {
        changeEventSupport_.removeChangeListener(listener);
    }

    /**
     * Removes an error listener.
     * 
     * @param  listener    The listener.
    **/
    public void removeErrorListener(ErrorListener listener)
    {
        errorEventSupport_.removeErrorListener(listener);
    }

    /**
     * Removes a VObjectListener.
     * 
     * @param  listener    The listener.
    **/
    public void removeVObjectListener(VObjectListener listener)
    {
        objectEventSupport_.removeVObjectListener(listener);
    }

    /**
     * Removes a working listener.
     * 
     * @param  listener    The listener.
    **/
    public void removeWorkingListener(WorkingListener listener)
    {
        workingEventSupport_.removeWorkingListener(listener);
    }
}

