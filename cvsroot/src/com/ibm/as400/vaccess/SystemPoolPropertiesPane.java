///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SystemPoolPropertiesPane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;


import com.ibm.as400.access.SystemPool;
import com.ibm.as400.access.Trace;

import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.lang.Integer;
import java.text.DateFormat;
import java.util.Date;

/**
 * The SystemPoolPropertiesPane class represents the system pool properties pane. 
**/
class SystemPoolPropertiesPane implements VPropertiesPane
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // MRI start.
    private static final String otherTabText_             = ResourceLoader.getText ("TAB_OTHER");
    private static final String generalTabText_           = ResourceLoader.getText ("TAB_GENERAL");
    
    private static final String maximumActiveThreadsText_ = ResourceLoader.getText ("SYSTEM_POOL_MAXIMUM_ACTIVE_THREADS")+":   ";
    private static final String databaseFaultsText_       = ResourceLoader.getText ("SYSTEM_POOL_DATABASE_FAULTS")+": ";
    private static final String databasePagesText_        = ResourceLoader.getText ("SYSTEM_POOL_DATABASE_PAGES")+": ";
    private static final String nondatabaseFaultsText_    = ResourceLoader.getText ("SYSTEM_POOL_NONDATABASE_FAULTS")+": ";
    private static final String nondatabasePagesText_     = ResourceLoader.getText ("SYSTEM_POOL_NONDATABASE_PAGES")+": ";
    private static final String activeToWaitText_         = ResourceLoader.getText ("SYSTEM_POOL_ACTIVE_TO_WAIT")+": ";
    private static final String waitToIneligibleText_     = ResourceLoader.getText ("SYSTEM_POOL_WAIT_TO_INELIGIBLE")+": ";
    private static final String activeToIneligibleText_   = ResourceLoader.getText ("SYSTEM_POOL_ACTIVE_TO_INELIGIBLE")+": ";
    private static final String poolNameText_             = ResourceLoader.getText ("SYSTEM_POOL_POOL_NAME")+":   ";
    private static final String subsystemNameText_        = ResourceLoader.getText ("SYSTEM_POOL_SUBSYSTEM_NAME")+":   ";
    private static final String subsystemLibraryNameText_ = ResourceLoader.getText ("SYSTEM_POOL_SUBSYSTEM__LIBRARY_NAME")+": ";
    private static final String pagingOptionText_         = ResourceLoader.getText ("SYSTEM_POOL_PAGING_OPTION")+":   ";
    // MRI end

    // Private data.
    private String[] names_;
    private Object[] values_;
    private VSystemPool object_;
    private SystemPool  systemPool_;

    // Event support.
    private ChangeEventSupport  changeEventSupport_  = new ChangeEventSupport (this);
    private ErrorEventSupport   errorEventSupport_   = new ErrorEventSupport (this);
    private VObjectEventSupport objectEventSupport_  = new VObjectEventSupport (this);
    private WorkingEventSupport workingEventSupport_ = new WorkingEventSupport (this);

    /**
     * Constructs an SystemPoolPropertiesPane object.
     *
     * @param object     The object.
     * @param SystemPool The SystemPool object.
    **/
    public SystemPoolPropertiesPane (VSystemPool object, SystemPool systemPool)
    {
        object_ = object;
        systemPool_ = systemPool;
    }

    /**
     * Adds a change listener.
     *
     * @param listener The listener.
    **/
    public void addChangeListener (ChangeListener listener)
    {
        changeEventSupport_.addChangeListener (listener);
    }

    /**
     * Adds a listener to be notified when an error occurs.
     *
     * @param listener The listener.
    **/
    public void addErrorListener (ErrorListener listener)
    {
        errorEventSupport_.addErrorListener (listener);
    }

    /**
     * Adds a listener to be notified when a VObject is changed, created,
     * or deleted.
     *
     * @param listener The listener.
    **/
    public void addVObjectListener (VObjectListener listener)
    {
        objectEventSupport_.addVObjectListener (listener);
    }

    /**
     * Adds a listener to be notified when work in a different thread
     * starts and stops.
     *
     * @param listener The listener.
    **/
    public void addWorkingListener (WorkingListener listener)
    {
        workingEventSupport_.addWorkingListener (listener);
    }

    /**
     * Applies the changes made by the SystemPool.
     *
     * @throws Exception If an error occurs.
    **/
    public void applyChanges ()
           throws Exception
    {
        // No changes are allowed.
    }

    /**
     * Returns the general tab.
     *
     * @return The general tab.
    **/
    private Component getGeneralTab()
    {
      // Initialize the general tab.
      JPanel generalTab = new JPanel ();
      GridBagLayout layout = new GridBagLayout ();
      GridBagConstraints constraints;
      generalTab.setLayout (layout);
      generalTab.setBorder (new EmptyBorder (10, 10, 10, 10));

      // Icon and name.
      int row = 0;
           
      try //@B0A
      {
        VUtilities.constrain (poolNameText_, 
                   (String)(systemPool_.getPoolName()),
                   generalTab, layout, row++);

        VUtilities.constrain (maximumActiveThreadsText_, 
                   Integer.toString(systemPool_.getMaximumActiveThreads()),
                   generalTab, layout, row++);

        VUtilities.constrain (subsystemNameText_, 
                   (String)(systemPool_.getSubsystemName()),
                   generalTab, layout, row++);

        VUtilities.constrain (pagingOptionText_, 
                   (String)(systemPool_.getPagingOption()),
                   generalTab, layout, row++);
      }
      catch(Exception e) //@B0A
      {
        if (Trace.isTraceOn() && Trace.isTraceErrorOn()) //@B0A
        {
          Trace.log(Trace.ERROR, "Unable to properly create general tab for system pool properties pane.", e); //@B0A
        }
        errorEventSupport_.fireError(e); //@B0A
      }
      return generalTab;
    }

    /**
     * Returns the other tab.
     *
     * @return The other tab.
    **/
    private Component getOtherTab()
    {
      // Initialize the other tab.
      JPanel otherTab = new JPanel ();
      GridBagLayout layout = new GridBagLayout ();
      GridBagConstraints constraints;
      otherTab.setLayout (layout);
      otherTab.setBorder (new EmptyBorder (10, 10, 10, 10));

      // Icon and name.
      int row = 0;

      try //@B0A
      {  
        VUtilities.constrain (databaseFaultsText_, 
                         Float.toString(systemPool_.getDatabaseFaults()),
                         otherTab, layout, row++);

        VUtilities.constrain (databasePagesText_, 
                         Float.toString(systemPool_.getDatabasePages()),
                         otherTab, layout, row++);

        VUtilities.constrain (nondatabaseFaultsText_, 
                         Float.toString(systemPool_.getNonDatabaseFaults()),
                         otherTab, layout, row++);

        VUtilities.constrain (nondatabasePagesText_, 
                         Float.toString(systemPool_.getNonDatabasePages()),
                         otherTab, layout, row++);       

        VUtilities.constrain (activeToWaitText_, 
                         Float.toString(systemPool_.getActiveToWait()),
                         otherTab, layout, row++);       

        VUtilities.constrain (waitToIneligibleText_, 
                         Float.toString(systemPool_.getWaitToIneligible()),
                         otherTab, layout, row++);       

        VUtilities.constrain (activeToIneligibleText_, 
                         Float.toString(systemPool_.getActiveToIneligible()),
                         otherTab, layout, row++);       
      }
      catch(Exception e) //@B0A
      {
        if (Trace.isTraceOn() && Trace.isTraceErrorOn()) //@B0A
        {
          Trace.log(Trace.ERROR, "Unable to properly create other tab for system pool properties pane.", e); //@B0A
        }
        errorEventSupport_.fireError(e); //@B0A
      }      
      return otherTab;
    }    

    /**
     * Returns the graphical component.
     *
     * @return The graphical component.
    **/
    public Component getComponent ()
    {
        JTabbedPane tabbedPane = new JTabbedPane ();
        
        tabbedPane.addTab (generalTabText_, null, getGeneralTab ());
        
        tabbedPane.addTab(otherTabText_,null,getOtherTab());
        tabbedPane.setSelectedIndex (0);

        return tabbedPane;
    }

    /**
     * Removes a change listener.
     *
     * @param listener The listener.
    **/
    public void removeChangeListener (ChangeListener listener)
    {
        changeEventSupport_.removeChangeListener (listener);
    }

    /**
     * Removes a listener to be notified when an error occurs.
     * 
     * @param listener The listener.
    **/
    public void removeErrorListener (ErrorListener listener)
    {
        errorEventSupport_.removeErrorListener (listener);
    }

    /**
     * Removes a listener to be notified when a VObject is changed,
     * created, or deleted.
     *
     * @param listener The listener.
    **/
    public void removeVObjectListener (VObjectListener listener)
    {
        objectEventSupport_.removeVObjectListener (listener);
    }

    /**
     * Removes a listener to be notified when work in a different thread
     * starts and stops.
     *
     * @param listener The listener.
    **/
    public void removeWorkingListener (WorkingListener listener)
    {
        workingEventSupport_.removeWorkingListener (listener);
    }

}
