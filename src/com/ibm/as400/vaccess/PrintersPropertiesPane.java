///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PrintersPropertiesPane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.PrinterList;
import com.ibm.as400.access.Trace;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JComboBox;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import javax.swing.event.ChangeListener;
import javax.swing.border.EmptyBorder;



/**
The PrintersPropertiesPane class represents the property pane
for the PrinterOutput object.
**/
class PrintersPropertiesPane
implements VPropertiesPane, ItemListener
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Private data.
    private VPrinters           object_;
    private PrinterList         list_;

    private JComboBox           printerFilter_;
    private String              printerFilterString_;

    // Event support.
    private ChangeEventSupport  changeEventSupport_     = new ChangeEventSupport (this);
    private ErrorEventSupport   errorEventSupport_      = new ErrorEventSupport (this);
    private VObjectEventSupport objectEventSupport_     = new VObjectEventSupport (this);
    private WorkingEventSupport workingEventSupport_    = new WorkingEventSupport (this);

    // MRI
    private static final String includeText_       = ResourceLoader.getPrintText("INCLUDE");
    private static final String printerText_       = ResourceLoader.getPrintText("PRINTER") + ":";
    private static final String allText_           = ResourceLoader.getPrintText("ALL");

/**
Constructs an PrintersPropertiesPane object.

@param  resource The printer output resource.
@param  list spooled file list
**/
    public PrintersPropertiesPane (VPrinters object, PrinterList list)
    {
        object_ = object;
        list_ = list;
    }

/**
Adds a change listener.

@param  listener    The listener.
**/
    public void addChangeListener (ChangeListener listener)
    {
        changeEventSupport_.addChangeListener (listener);
    }


/**
Adds a listener to be notified when an error occurs.

@param  listener    The listener.
**/
    public void addErrorListener (ErrorListener listener)
    {
        errorEventSupport_.addErrorListener (listener);
    }


/**
Adds a listener to be notified when a VObject is changed,
created, or deleted.

@param  listener    The listener.
**/
    public void addVObjectListener (VObjectListener listener)
    {
        objectEventSupport_.addVObjectListener (listener);
    }


/**
Adds a listener to be notified when work in a different thread
starts and stops.

@param  listener    The listener.
**/
    public void addWorkingListener (WorkingListener listener)
    {
        workingEventSupport_.addWorkingListener (listener);
    }


/**
Applies the changes made by the user.
**/
    public void applyChanges ()
      throws Exception
    {
        /////////////////////
        // Printer filter ///
        /////////////////////

        // get the previous filter value from the list
        String oldPrinter = list_.getPrinterFilter().trim();

        // get the new filter value from the combo box and upper case it
        String newPrinter = (String)printerFilter_.getModel().getSelectedItem();

        // change new value if necessary
        if (newPrinter.trim().equals(allText_))
            newPrinter = "*ALL";

        // compare the filters to see if a change is needed, and set as necessary
        if (!oldPrinter.equals(newPrinter.trim()))
        {
            list_.setPrinterFilter(newPrinter);

            // notify object that there have been changes
            objectEventSupport_.fireObjectChanged((VObject)object_);
        }
    }

/**
Builds Include pane for the properties pane
**/
    private JPanel buildIncludePane()
    {
        JPanel thePane = new JPanel ();
        GridBagLayout layout = new GridBagLayout ();
        GridBagConstraints constraints;
        thePane.setLayout (layout);
        thePane.setBorder (new EmptyBorder (10, 10, 10, 10));

        try {
            // Printer filter
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (new JLabel (printerText_), // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  0,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            printerFilter_ = new JComboBox();
            printerFilter_.setEditable(true);
            printerFilter_.addItem(allText_);
            printerFilter_.addItemListener(this);

            String curPrinterFilter = list_.getPrinterFilter().trim();
            if (curPrinterFilter.equals("*ALL") ||
                curPrinterFilter.equals(""))  // added because default filter is ""
                printerFilter_.setSelectedItem(allText_);
            else
            {
                printerFilter_.addItem(curPrinterFilter);
                printerFilter_.setSelectedItem(curPrinterFilter);
            }

            printerFilter_.addItemListener(this);
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (printerFilter_, // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  0,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
        }
        catch (Exception e)
        {
            thePane = null;
            errorEventSupport_.fireError (e);
        }

        return thePane;
    }

/**
Returns the copyright.
**/
private static String getCopyright()
{
    return Copyright_v.copyright;
}

/**
Returns the editor pane.

@return             The properties pane.
@throws Exception   If an error occurs.
**/
    public Component getComponent ()
    {
        JTabbedPane tabbedPane = null;
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab (includeText_, null, buildIncludePane());
        tabbedPane.setSelectedIndex (0);
        return tabbedPane;
    }

/**
catches the state change of combo boxes
**/
    public void itemStateChanged(ItemEvent e)
    {
        // notify that something has changed so the apply button is enabled
        changeEventSupport_.fireStateChanged ();
    }


/**
Removes a change listener.

@param  listener    The listener.

**/
    public void removeChangeListener (ChangeListener listener)
    {
        changeEventSupport_.removeChangeListener (listener);
    }

/**
Removes a listener to be notified when an error occurs.

@param  listener    The listener.
**/
    public void removeErrorListener (ErrorListener listener)
    {
        errorEventSupport_.removeErrorListener (listener);
    }


/**
Removes a listener to be notified when a VObject is changed,
created, or deleted.

@param  listener    The listener.
**/
    public void removeVObjectListener (VObjectListener listener)
    {
        objectEventSupport_.removeVObjectListener (listener);
    }


/**
Removes a listener to be notified when work in a different thread
starts and stops.

@param  listener    The listener.
**/
    public void removeWorkingListener (WorkingListener listener)
    {
        workingEventSupport_.removeWorkingListener (listener);
    }



} // end PrintersPropertiesPane class


