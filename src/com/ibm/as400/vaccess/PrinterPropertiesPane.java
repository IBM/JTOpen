///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PrinterPropertiesPane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.Printer;
import com.ibm.as400.access.PrintObject;
import com.ibm.as400.access.PrinterList;
import com.ibm.as400.access.QSYSObjectPathName;
import com.ibm.as400.access.Trace;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import javax.swing.border.EmptyBorder;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

import java.util.Vector;


/**
The PrinterPropertiesPane class represents the property pane
for the PrinterOutput object.
**/
class PrinterPropertiesPane
implements VPropertiesPane, ItemListener
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Private data.
    private VPrinter            printer_        = null;
    private VPrinterOutput      printerOutput_  = null;
    private static final String indentString_   = "   ";
    private int overallStatusInt_;
    private int deviceStatusInt_;

    // Event support.
    private ChangeEventSupport changeEventSupport_ = new ChangeEventSupport (this);
    private ErrorEventSupport   errorEventSupport_      = new ErrorEventSupport (this);
    private VObjectEventSupport objectEventSupport_     = new VObjectEventSupport (this);
    private WorkingEventSupport workingEventSupport_    = new WorkingEventSupport (this);

    // MRI
    private static String advFuncPrintingText_ = null;
    private static String allowDirectPrintingText_;
    private static String betweenFilesText_;
    private static String betweenCopiesText_;
    private static String changesTakeEffectText_;
    private static String curFormTypeText_;
    private static String curFormTypeNtfctnText_;
    private static String curNumSepPagesText_;
    private static String curSepDrawerText_;
    private static String curValuesText_;
    private static String descriptionText_;
    private static String deviceText_;
    private static String deviceStatusText_;
    private static String endAutomaticallyText_;
    private static String endedText_;
    private static String endPendingText_;
    private static String formsAlignmentText_;
    private static String formsText_;
    private static String generalText_;
    private static String heldText_;
    private static String holdPendingText_;
    private static String includeText_;
    private static String libraryText_;
    private static String messageQueueText_;
    private static String messageWaitingText_;
    private static String nextFormTypeText_;
    private static String nextFormTypeNtfctnText_;
    private static String nextNumSepPagesText_;
    private static String nextOutputQueueText_;
    private static String nextSepDrawerText_;
    private static String numberText_;
    private static String OKText_;
    private static String outputQueueText_;
    private static String outputQueueLibText_;
    private static String outputQueueStatusText_;
    private static String printerText_;
    private static String printingText_;
    private static String separatorsText_;
    private static String startedByText_;
    private static String statusText_;
    private static String typeText_;
    private static String userText_;
    private static String waitingForDataText_;
    private static String waitingForPrinterText_;
    private static String waitingOnJobQueueQSPLText_;
    private static String writerText_;
    private static String writerStatusText_;

    // General pane data
    private JLabel      printerName_            = null;
    private JLabel      type_                   = null;
    private JTextField  descTextField_          = null;
    private JLabel      descLabel_              = null;
    private JLabel      status_                 = null;
    private JLabel      startedBy_              = null;
    private JLabel      messageQueue_           = null;
    private JLabel      library_                = null;

    // Form pane data
    private JLabel      curFormType_            = null;
//  private JComboBox   nextFormType_           = null; Not supported yet
    private JLabel      formsAlignment_         = null;
//  private JLabel      curFormTypeNtfctn_      = null; Not supported yet
//  private JComboBox   nextFormTypeNtfctn_     = null; Not supported yet

    // Separators pane data
    private JLabel      curNumSepPages_         = null;
//  private JComboBox   nextNumSepPages_        = null; Not supported yet
    private JLabel      curSepDrawer_           = null;
//  private JComboBox   nextSepDrawer_          = null; Not supported yet

    // Output Queue pane data
    private JLabel      outputQueue_            = null;
    private JLabel      outputQueueLibrary_     = null;
    private JLabel      outputQueueStatus_      = null;
//  private JComboBox   nextOutputQueue_        = null; Not supported yet
//  private JLabel      nextOutputQueueLib_     = null; Not supported yet

    // Writer pane data
    private JLabel      writer_                 = null;
    private JLabel      user_                   = null;
    private JLabel      number_                 = null;
    private JList       writerStatusList_       = null;
    private JButton     writerStatusButton_     = null;
    private JLabel      allowDirectPrinting_    = null;
    private JLabel      endAutomatically_       = null;

    // Device pane data
    private JLabel      devPrinter_             = null;
    private JLabel      devStatus_              = null;
    private JLabel      devType_                = null;
    private JLabel      advFuncPrinting_        = null;

/**
Constructs an PrinterPropertiesPane object.

@param  resource The printer output resource.
@param  list spooled file list
**/
    public PrinterPropertiesPane (VPrinter printer, VPrinterOutput printerOutput)
    {
        printer_ = printer;
        printerOutput_ = printerOutput;
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
        printerOutput_.propertiesPane_.applyChanges();
        if (printerOutput_.propertiesPane_.fChanges_ == true) // @A2A
        {
            objectEventSupport_.fireObjectChanged(printer_);
        }
    }

/**
Builds Device pane for the properties pane
**/
    private JPanel buildDevicePane()
    {
        JPanel thePane = new JPanel ();
        GridBagLayout layout = new GridBagLayout ();
        GridBagConstraints constraints;
        thePane.setLayout (layout);
        thePane.setBorder (new EmptyBorder (10, 10, 10, 10));

        try
        {
            // Printer
            constraints = new GridBagConstraints();
            VUtilities.constrain (new JLabel (printerText_),
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  0,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            devPrinter_ = new JLabel(printer_.getPrinterAttribute(PrintObject.ATTR_PRINTER));
            constraints = new GridBagConstraints();
            VUtilities.constrain (devPrinter_,
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  0,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);

            // Status
            constraints = new GridBagConstraints();
            VUtilities.constrain (new JLabel (deviceStatusText_),
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  1,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            devStatus_ = new JLabel(printer_.getPrinterAttribute(PrintObject.ATTR_DEVSTATUS));
            constraints = new GridBagConstraints();
            VUtilities.constrain (devStatus_,
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  1,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);

            // Type
            constraints = new GridBagConstraints();
            VUtilities.constrain (new JLabel (typeText_),
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  2,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            devType_ = new JLabel(printer_.getPrinterAttribute(PrintObject.ATTR_DEVTYPE));
            constraints = new GridBagConstraints();
            VUtilities.constrain (devType_,
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  2,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);

            // Advanced Function Printing
            constraints = new GridBagConstraints();
            VUtilities.constrain (new JLabel (advFuncPrintingText_),
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  3,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            advFuncPrinting_ = new JLabel(printer_.getPrinterAttribute(PrintObject.ATTR_AFP));
            constraints = new GridBagConstraints();
            VUtilities.constrain (advFuncPrinting_,
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  3,1,1,
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
Builds Forms pane for the properties pane
**/
    private JPanel buildFormsPane()
    {
        JPanel thePane = new JPanel ();
        GridBagLayout layout = new GridBagLayout ();
        GridBagConstraints constraints;
        thePane.setLayout (layout);
        thePane.setBorder (new EmptyBorder (10, 10, 10, 10));

        try
        {
            // Current form type
            constraints = new GridBagConstraints();
            VUtilities.constrain (new JLabel (curFormTypeText_),
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  0,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            curFormType_ = new JLabel(printer_.getPrinterAttribute(PrintObject.ATTR_FORMTYPE));
            constraints = new GridBagConstraints();
            VUtilities.constrain (curFormType_,
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  0,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);

/* Not supported yet
            // Next form type
            VUtilities.constrain (new JLabel (nextFormTypeText_), thePane, layout, 0, 1, 1, 1);
            if (overallStatusInt_ != printer_.OVERALLSTATUS_MESSAGEWAITING)  // ???
            {
                nextFormType_ = new JComboBox();
//                nextFormType_.addItem(printer_.getPrinterAttribute(PrintObject.ATTR_));
                nextFormType_.addItem("Unsupported");
                nextFormType_.setSelectedIndex(0);
                nextFormType_ = new JLabel(printer_.getPrinterAttribute(PrintObject.ATTR_NEXTFORMTYPE));
                VUtilities.constrain (nextFormType_, thePane, layout, 1,1,1,1);
            }
*/

            // Forms alignment
            constraints = new GridBagConstraints();
            VUtilities.constrain (new JLabel (formsAlignmentText_),
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  1,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            formsAlignment_ = new JLabel(printer_.getPrinterAttribute(PrintObject.ATTR_ALIGNFORMS));
            constraints = new GridBagConstraints();
            VUtilities.constrain (formsAlignment_,
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  1,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
/*
            // Current form type notification
            VUtilities.constrain (new JLabel (curFormTypeNtfctnText_), thePane, layout, 0, 3, 1, 1);
//            curFormTypeNtfctn_ = new JLabel(printer_.getPrinterAttribute(PrintObject.ATTR_));
            curFormTypeNtfctn_ = new JLabel("Unsupported");
            VUtilities.constrain (curFormTypeNtfctn_, thePane, layout, 1,3,1,1);
*/
/* Not supported yet
            // Next form type notification
            VUtilities.constrain (new JLabel (nextFormTypeNtfctnText_), thePane, layout, 0, 4, 1, 1);
            if (overallStatusInt_ != printer_.OVERALLSTATUS_MESSAGEWAITING)  // ???
            {
                nextFormTypeNtfctn_ = new JComboBox();
//                nextFormTypeNtfctn_.addItem(printer_.getPrinterAttribute(PrintObject.ATTR_));
                nextFormTypeNtfctn_.addItem("Unsupported");
                nextFormTypeNtfctn_.setSelectedIndex(0);
                VUtilities.constrain (nextFormTypeNtfctn_, thePane, layout, 1,4,1,1);
            }
*/
        }
        catch (Exception e)
        {
            thePane = null;
            errorEventSupport_.fireError (e);
        }

        return thePane;
    }

/**
Builds General pane for the properties pane
**/
    private JPanel buildGeneralPane()
    {
        JPanel thePane = new JPanel ();
        GridBagLayout layout = new GridBagLayout ();
        GridBagConstraints constraints;
        thePane.setLayout (layout);
        thePane.setBorder (new EmptyBorder (10, 10, 10, 10));
        String  attrString;
        Color   backgroundColor;

        try
        {
            // Printer
            constraints = new GridBagConstraints();
            VUtilities.constrain (new JLabel (printerText_),
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  0,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            printerName_ = new JLabel(printer_.getPrinterAttribute(PrintObject.ATTR_PRINTER));
            constraints = new GridBagConstraints();
            VUtilities.constrain (printerName_,
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  0,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);

            // Type
            constraints = new GridBagConstraints();
            VUtilities.constrain (new JLabel (typeText_),
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  1,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            if (deviceStatusInt_ == printer_.DEVICESTATUS_ACTIVE)
            {
                attrString = printer_.getPrinterAttribute(PrintObject.ATTR_PRTDEVTYPE);
            }
            else
            {
                attrString = printer_.getPrinterAttribute(PrintObject.ATTR_DEVTYPE);
            }
            type_ = new JLabel(attrString);
            constraints = new GridBagConstraints();
            VUtilities.constrain (type_,
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  1,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);

            // Description
            constraints = new GridBagConstraints();
            VUtilities.constrain (new JLabel (descriptionText_),
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  2,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            descTextField_ = new JTextField(printer_.getPrinterAttribute(PrintObject.ATTR_DESCRIPTION));
            descTextField_.setColumns(20);
            descTextField_.setEditable(false);
            backgroundColor = thePane.getBackground();
            descTextField_.setBackground(backgroundColor);
            constraints = new GridBagConstraints();
            VUtilities.constrain (descTextField_,
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  2,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);

            // Status
            constraints = new GridBagConstraints();
            VUtilities.constrain (new JLabel (statusText_),
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  3,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            status_ = new JLabel(printer_.getPrinterAttribute(PrintObject.ATTR_OVERALLSTS));
            constraints = new GridBagConstraints();
            VUtilities.constrain (status_,
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  3,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);

            if (deviceStatusInt_ == printer_.DEVICESTATUS_ACTIVE)
            {
                // Started by
                constraints = new GridBagConstraints();
                VUtilities.constrain (new JLabel (startedByText_),
                                      thePane,
                                      layout,
                                      constraints,
                                      0,1,1,
                                      4,1,1,
                                      constraints.HORIZONTAL,
                                      constraints.WEST);
                startedBy_ = new JLabel(printer_.getPrinterAttribute(PrintObject.ATTR_STARTEDBY));
                constraints = new GridBagConstraints();
                VUtilities.constrain (startedBy_,
                                      thePane,
                                      layout,
                                      constraints,
                                      1,1,1,
                                      4,1,1,
                                      constraints.HORIZONTAL,
                                      constraints.WEST);

/* Not supported yet
                // Changes take effect
                VUtilities.constrain (new JLabel (changesTakeEffectText_), thePane, layout, 0, 5, 1, 1);
                changesTakeEffect_ = new JLabel(printer_.getPrinterAttribute(PrintObject.ATTR_CHANGES));
                VUtilities.constrain (changesTakeEffect_, thePane, layout, 1,5,1,1);
*/
                // Message queue
                QSYSObjectPathName messageQPath = new QSYSObjectPathName(printer_.getPrinterAttribute(PrintObject.ATTR_MESSAGE_QUEUE));
                constraints = new GridBagConstraints();
                VUtilities.constrain (new JLabel (messageQueueText_),
                                      thePane,
                                      layout,
                                      constraints,
                                      0,1,1,
                                      6,1,1,
                                      constraints.HORIZONTAL,
                                      constraints.WEST);
                messageQueue_ = new JLabel(messageQPath.getObjectName().trim());
                constraints = new GridBagConstraints();
                VUtilities.constrain (messageQueue_,
                                      thePane,
                                      layout,
                                      constraints,
                                      1,1,1,
                                      6,1,1,
                                      constraints.HORIZONTAL,
                                      constraints.WEST);

                // Library
                constraints = new GridBagConstraints();
                VUtilities.constrain (new JLabel (indentString_ + libraryText_),
                                      thePane,
                                      layout,
                                      constraints,
                                      0,1,1,
                                      7,1,1,
                                      constraints.HORIZONTAL,
                                      constraints.WEST);
                library_ = new JLabel(messageQPath.getLibraryName().trim());
                constraints = new GridBagConstraints();
                VUtilities.constrain (library_,
                                      thePane,
                                      layout,
                                      constraints,
                                      1,1,1,
                                      7,1,1,
                                      constraints.HORIZONTAL,
                                      constraints.WEST);
            }
        }
        catch (Exception e)
        {
            thePane = null;
            errorEventSupport_.fireError (e);
        }

        return thePane;
    }

/**
Builds Output Queue pane for the properties pane
**/
    private JPanel buildOutputQueuePane()
    {
        JPanel thePane = new JPanel ();
        GridBagLayout layout = new GridBagLayout ();
        GridBagConstraints constraints;
        thePane.setLayout (layout);
        thePane.setBorder (new EmptyBorder (10, 10, 10, 10));

        try
        {
            // Output queue
            constraints = new GridBagConstraints();
            VUtilities.constrain (new JLabel (outputQueueText_ + ":"),
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  0,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            outputQueue_ = new JLabel(printer_.getPrinterOutputQueue());
            constraints = new GridBagConstraints();
            VUtilities.constrain (outputQueue_,
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  0,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);

            // Output queue library
            constraints = new GridBagConstraints();
            VUtilities.constrain (new JLabel (indentString_ + outputQueueLibText_),
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  1,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            outputQueueLibrary_ = new JLabel(printer_.getPrinterOutputQueueLib());
            constraints = new GridBagConstraints();
            VUtilities.constrain (outputQueueLibrary_,
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  1,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);

            if (deviceStatusInt_ == printer_.DEVICESTATUS_ACTIVE)
            {
                // Output queue status
                constraints = new GridBagConstraints();
                VUtilities.constrain (new JLabel (outputQueueStatusText_),
                                      thePane,
                                      layout,
                                      constraints,
                                      0,1,1,
                                      2,1,1,
                                      constraints.HORIZONTAL,
                                      constraints.WEST);
                outputQueueStatus_ = new JLabel(printer_.getPrinterAttribute(PrintObject.ATTR_OUTQSTS));
                constraints = new GridBagConstraints();
                VUtilities.constrain (outputQueueStatus_,
                                      thePane,
                                      layout,
                                      constraints,
                                      1,1,1,
                                      2,1,1,
                                      constraints.HORIZONTAL,
                                      constraints.WEST);

/* Not supported yet
                // Next output queue
                VUtilities.constrain (new JLabel (nextOutputQueueText_), thePane, layout, 0, 3, 1, 1);
                nextOutputQueue_ = new JComboBox();
//                nextOutputQueue_.addItem(printer_.getPrinterAttribute(PrintObject.ATTR_));
                nextOutputQueue_.addItem("Unsupported");
                nextOutputQueue_.setSelectedIndex(0);
                VUtilities.constrain (nextOutputQueue_, thePane, layout, 1,3,1,1);

                // Library
                VUtilities.constrain (new JLabel (indentString_ + libraryText_), thePane, layout, 0, 4, 1, 1);
//                nextOutputQueueLib_ = new JLabel(printer_.getPrinterAttribute(PrintObject.ATTR_));
                nextOutputQueueLib_ = new JLabel("Unsupported");
                VUtilities.constrain (nextOutputQueueLib_, thePane, layout, 1,4,1,1);
*/
            }
        }
        catch (Exception e)
        {
            thePane = null;
            errorEventSupport_.fireError (e);
        }

        return thePane;
    }

/**
Builds Separators pane for the properties pane
**/
    private JPanel buildSeparatorsPane()
    {
        JPanel thePane = new JPanel ();
        GridBagLayout layout = new GridBagLayout ();
        GridBagConstraints constraints;
        thePane.setLayout (layout);
        thePane.setBorder (new EmptyBorder (10, 10, 10, 10));

        try
        {
            // Current number of separator pages
            constraints = new GridBagConstraints();
            VUtilities.constrain (new JLabel (curNumSepPagesText_),
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  0,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            curNumSepPages_ = new JLabel(printer_.getPrinterAttribute(PrintObject.ATTR_FILESEP));
            constraints = new GridBagConstraints();
            VUtilities.constrain (curNumSepPages_,
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  0,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);

/* Not supported yet
            // Next number of separator pages
            VUtilities.constrain (new JLabel (nextNumSepPagesText_), thePane, layout, 0, 1, 1, 1);
            if (overallStatusInt_ != printer_.OVERALLSTATUS_MESSAGEWAITING)  // ???
            {
                nextNumSepPages_ = new JComboBox();
//                nextNumSepPages_.addItem(printer_.getPrinterAttribute(PrintObject.ATTR_));
                nextNumSepPages_.addItem("Unsupported");
                nextNumSepPages_.setSelectedIndex(0);
                VUtilities.constrain (nextNumSepPages_, thePane, layout, 1,1,1,1);
            }
*/
            // Current separator drawer
            constraints = new GridBagConstraints();
            VUtilities.constrain (new JLabel (curSepDrawerText_),
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  1,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            curSepDrawer_ = new JLabel(printer_.getPrinterAttribute(PrintObject.ATTR_DRWRSEP));
            constraints = new GridBagConstraints();
            VUtilities.constrain (curSepDrawer_,
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  1,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);

/* Not supported yet
            // Next separator drawer
            VUtilities.constrain (new JLabel (nextSepDrawerText_), thePane, layout, 0, 3, 1, 1);
            if (overallStatusInt_ != printer_.OVERALLSTATUS_MESSAGEWAITING)  // ???
            {
                nextSepDrawer_ = new JComboBox();
//                nextSepDrawer_.addItem(printer_.getPrinterAttribute(PrintObject.ATTR_));
                nextSepDrawer_.addItem("Unsupported");
                nextSepDrawer_.setSelectedIndex(0);
                VUtilities.constrain (nextSepDrawer_, thePane, layout, 1,3,1,1);
            }
*/
        }
        catch (Exception e)
        {
            thePane = null;
            errorEventSupport_.fireError (e);
        }

        return thePane;
    }

/**
Builds Writer pane for the properties pane
**/
    private JPanel buildWriterPane()
    {
        JPanel thePane = new JPanel ();
        thePane = new JPanel ();
        GridBagLayout layout = new GridBagLayout ();
        GridBagConstraints constraints;
        thePane.setLayout (layout);
        thePane.setBorder (new EmptyBorder (10, 10, 10, 10));
        String  attrString,
                wtrStatusString;
        int     index;
        Vector  writerStatusVector;
        Color   backgroundColor,
                foregroundColor;

        try
        {
            // Writer
            constraints = new GridBagConstraints();
            VUtilities.constrain (new JLabel (writerText_ + ":"),
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  0,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.NORTHWEST);
            writer_ = new JLabel(printer_.getPrinterAttribute(PrintObject.ATTR_WTRJOBNAME));
            constraints = new GridBagConstraints();
            VUtilities.constrain (writer_,
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  0,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.NORTHWEST);

            // User
            constraints = new GridBagConstraints();
            VUtilities.constrain (new JLabel (indentString_ + userText_),
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  1,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.NORTHWEST);
            user_ = new JLabel(printer_.getPrinterAttribute(PrintObject.ATTR_WTRJOBUSER));
            constraints = new GridBagConstraints();
            VUtilities.constrain (user_,
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  1,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.NORTHWEST);

            // Number
            constraints = new GridBagConstraints();
            VUtilities.constrain (new JLabel (indentString_ + numberText_),
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  2,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.NORTHWEST);
            number_ = new JLabel(printer_.getPrinterAttribute(PrintObject.ATTR_WTRJOBNUM));
            constraints = new GridBagConstraints();
            VUtilities.constrain (number_,
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  2,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.NORTHWEST);

            // Writer status
            constraints = new GridBagConstraints();
            VUtilities.constrain (new JLabel (writerStatusText_ + ":"),
                                  thePane,
                                  layout,
                                  constraints,
                                  0, 1, 1,
                                  3, 1, 1,
                                  constraints.HORIZONTAL,
                                  constraints.NORTHWEST);

            writerStatusVector = getWriterStatusVector();
            writerStatusList_ = new JList(writerStatusVector);
            writerStatusList_.setEnabled(false);
            backgroundColor = thePane.getBackground();
            writerStatusList_.setBackground(backgroundColor);
            writerStatusList_.setSelectionBackground(backgroundColor);
            foregroundColor = thePane.getForeground();
            writerStatusList_.setSelectionForeground(foregroundColor);
            constraints = new GridBagConstraints();
            VUtilities.constrain (writerStatusList_,
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  3,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.NORTHWEST);

            // Allow direct printing
            constraints = new GridBagConstraints();
            VUtilities.constrain (new JLabel (allowDirectPrintingText_),
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  4,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.NORTHWEST);
            allowDirectPrinting_ = new JLabel(printer_.getPrinterAttribute(PrintObject.ATTR_ALWDRTPRT));
            constraints = new GridBagConstraints();
            VUtilities.constrain (allowDirectPrinting_,
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  4,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.NORTHWEST);

            // End automatically
            constraints = new GridBagConstraints();
            VUtilities.constrain (new JLabel (endAutomaticallyText_),
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  5,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.NORTHWEST);
            endAutomatically_ = new JLabel(printer_.getPrinterAttribute(PrintObject.ATTR_WTRAUTOEND));
            constraints = new GridBagConstraints();
            VUtilities.constrain (endAutomatically_,
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  5,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.NORTHWEST);
        }
        catch (Exception e)
        {
            thePane = null;
            errorEventSupport_.fireError (e);
        }

        return thePane;
    }

/**
Returns the editor pane.

@return             The properties pane.
@throws Exception   If an error occurs.
**/
    public Component getComponent ()
    {
        JTabbedPane tabbedPane = null;
        loadStrings();
        deviceStatusInt_ = printer_.getPrinterDeviceStatus();
        overallStatusInt_ = printer_.getPrinterOverallStatus();

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab (generalText_, null, buildGeneralPane());
        if (deviceStatusInt_ == printer_.DEVICESTATUS_ACTIVE)
        {
            tabbedPane.addTab (formsText_, null, buildFormsPane());
            tabbedPane.addTab (separatorsText_, null, buildSeparatorsPane());
            tabbedPane.addTab (writerText_, null, buildWriterPane());
        }
        tabbedPane.addTab (outputQueueText_, null, buildOutputQueuePane());
        tabbedPane.addTab (deviceText_, null, buildDevicePane());
        VPropertiesPane includePane = printerOutput_.getPropertiesPane(); // must do
        includePane.addChangeListener(changeEventSupport_);
        includePane.addErrorListener(errorEventSupport_);
        includePane.addVObjectListener(objectEventSupport_);
        includePane.addWorkingListener(workingEventSupport_);
        tabbedPane.addTab (includeText_, null, printerOutput_.propertiesPane_.buildIncludePane());
        tabbedPane.setSelectedIndex (0);
        return tabbedPane;
    }


/**
Copyright.
**/
    private static String getCopyright ()
    {
        return Copyright_v.copyright;
    }

    private Vector getWriterStatusVector()
    {
        String attrString;
        Vector  writerStatusVector = new Vector();

          attrString = printer_.getPrinterAttribute(PrintObject.ATTR_WTRSTRTD);
          if (attrString != null)
          {
              attrString.trim();
              if (attrString.equals("0"))
              {
                  writerStatusVector.addElement(endedText_);
              }
              else
              {
                  attrString = printer_.getPrinterAttribute(PrintObject.ATTR_WRTNGSTS);
                  if (attrString != null)
                  {
                      attrString.trim();
                      if (!attrString.equals(printer_.splatNOString_))
                      {
                          writerStatusVector.addElement(printingText_);
                      }
                  }
                  attrString = printer_.getPrinterAttribute(PrintObject.ATTR_WTNGMSGSTS);
                  if (attrString != null)
                  {
                      attrString.trim();
                      if (!attrString.equals(printer_.splatNOString_))
                      {
                          writerStatusVector.addElement(messageWaitingText_);
                      }
                  }
                  attrString = printer_.getPrinterAttribute(PrintObject.ATTR_HELDSTS);
                  if (attrString != null)
                  {
                      attrString.trim();
                      if (!attrString.equals(printer_.splatNOString_))
                      {
                          writerStatusVector.addElement(heldText_);
                      }
                  }
                  attrString = printer_.getPrinterAttribute(PrintObject.ATTR_ENDPNDSTS);
                  if (attrString != null)
                  {
                      attrString.trim();
                      if (!attrString.equals(printer_.splatNOString_))
                      {
                          writerStatusVector.addElement(endPendingText_);
                      }
                  }
                  attrString = printer_.getPrinterAttribute(PrintObject.ATTR_HOLDPNDSTS);
                  if (attrString != null)
                  {
                      attrString.trim();
                      if (!attrString.equals(printer_.splatNOString_))
                      {
                          writerStatusVector.addElement(holdPendingText_);
                      }
                  }
                  attrString = printer_.getPrinterAttribute(PrintObject.ATTR_BTWNFILESTS);
                  if (attrString != null)
                  {
                      attrString.trim();
                      if (!attrString.equals(printer_.splatNOString_))
                      {
                          writerStatusVector.addElement(betweenFilesText_);
                      }
                  }
                  attrString = printer_.getPrinterAttribute(PrintObject.ATTR_BTWNCPYSTS);
                  if (attrString != null)
                  {
                      attrString.trim();
                      if (!attrString.equals(printer_.splatNOString_))
                      {
                          writerStatusVector.addElement(betweenCopiesText_);
                      }
                  }
                  attrString = printer_.getPrinterAttribute(PrintObject.ATTR_WTNGDATASTS);
                  if (attrString != null)
                  {
                      attrString.trim();
                      if (!attrString.equals(printer_.splatNOString_))
                      {
                          writerStatusVector.addElement(waitingForDataText_);
                      }
                  }
                  attrString = printer_.getPrinterAttribute(PrintObject.ATTR_WTNGDEVSTS);
                  if (attrString != null)
                  {
                      attrString.trim();
                      if (!attrString.equals(printer_.splatNOString_))
                      {
                          writerStatusVector.addElement(waitingForPrinterText_);
                      }
                  }
                  attrString = printer_.getPrinterAttribute(PrintObject.ATTR_ONJOBQSTS);
                  if (attrString != null)
                  {
                      attrString.trim();
                      if (!attrString.equals(printer_.splatNOString_))
                      {
                          writerStatusVector.addElement(waitingOnJobQueueQSPLText_);
                      }
                  }
              }
          }

          return writerStatusVector;
    }

/**
catches the state change of combo boxes
**/
    public void itemStateChanged(ItemEvent e)
    {
        // notify that something has changed so the apply button is enabled
        changeEventSupport_.fireStateChanged ();
    }

    private synchronized void loadStrings()
    {
        if (advFuncPrintingText_ == null)
        {
            try {
                advFuncPrintingText_        = ResourceLoader.getPrintText("ADV_FUNC_PRINTING") + ":";
                allowDirectPrintingText_    = ResourceLoader.getPrintText("ALLOW_DIRECT_PRINTING") + ":";
                betweenCopiesText_          = ResourceLoader.getPrintText("BETWEEN_COPIES");
                betweenFilesText_           = ResourceLoader.getPrintText("BETWEEN_FILES");
                changesTakeEffectText_      = ResourceLoader.getPrintText("CHANGES_TAKE_EFFECT") + ":";
                curFormTypeText_            = ResourceLoader.getPrintText("CURRENT_FORM_TYPE") + ":";
                curFormTypeNtfctnText_      = ResourceLoader.getPrintText("CURRENT_FORM_TYPE_NOTIFICATION") + ":";
                curNumSepPagesText_         = ResourceLoader.getPrintText("CURRENT_NUM_SEP_PAGES") + ":";
                curSepDrawerText_           = ResourceLoader.getPrintText("CURRENT_SEPARATOR_DRAWER") + ":";
                curValuesText_              = ResourceLoader.getPrintText("CURRENT_VALUES") + ":";
                descriptionText_            = ResourceLoader.getPrintText("DESCRIPTION") + ":";
                deviceText_                 = ResourceLoader.getPrintText("DEVICE");
                deviceStatusText_           = ResourceLoader.getPrintText("DEVICE_STATUS") + ":";
                endAutomaticallyText_       = ResourceLoader.getPrintText("END_AUTOMATICALLY") + ":";
                endPendingText_             = ResourceLoader.getPrintText("END_PENDING");
                endedText_                  = ResourceLoader.getPrintText("ENDED");
                formsAlignmentText_         = ResourceLoader.getPrintText("FORMS_ALIGNMENT") + ":";
                formsText_                  = ResourceLoader.getPrintText("FORMS");
                generalText_                = ResourceLoader.getPrintText("GENERAL");
                heldText_                   = ResourceLoader.getPrintText("HELD");
                holdPendingText_            = ResourceLoader.getPrintText("HOLD_PENDING");
                includeText_                = ResourceLoader.getPrintText("INCLUDE");
                libraryText_                = ResourceLoader.getPrintText("LIBRARY") + ":";
                messageQueueText_           = ResourceLoader.getPrintText("MESSAGE_QUEUE") + ":";
                messageWaitingText_         = ResourceLoader.getPrintText("MESSAGE_WAITING");
                nextFormTypeText_           = ResourceLoader.getPrintText("NEXT_FORM_TYPE") + ":";
                nextFormTypeNtfctnText_     = ResourceLoader.getPrintText("NEXT_FORM_TYPE_NOTIFICATION") + ":";
                nextNumSepPagesText_        = ResourceLoader.getPrintText("NEXT_NUM_SEP_PAGES") + ":";
                nextOutputQueueText_        = ResourceLoader.getPrintText("NEXT_OUTPUT_QUEUE") + ":";
                nextSepDrawerText_          = ResourceLoader.getPrintText("NEXT_SEPARATOR_DRAWER") + ":";
                numberText_                 = ResourceLoader.getPrintText("NUMBER") + ":";
                OKText_                     = ResourceLoader.getText("DLG_OK");
                outputQueueText_            = ResourceLoader.getPrintText("OUTPUT_QUEUE");
                outputQueueLibText_         = ResourceLoader.getPrintText("OUTPUT_QUEUE_LIB") + ":";
                outputQueueStatusText_      = ResourceLoader.getPrintText("OUTPUT_QUEUE_STATUS") + ":";
                printerText_                = ResourceLoader.getPrintText("PRINTER") + ":";
                printingText_               = ResourceLoader.getPrintText("PRINTING");
                separatorsText_             = ResourceLoader.getPrintText("SEPARATORS");
                startedByText_              = ResourceLoader.getPrintText("STARTED_BY") + ":";
                statusText_                 = ResourceLoader.getPrintText("STATUS") + ":";
                typeText_                   = ResourceLoader.getPrintText("TYPE") + ":";
                userText_                   = ResourceLoader.getPrintText("USER") + ":";
                waitingForDataText_         = ResourceLoader.getPrintText("WAITING_FOR_DATA");
                waitingForPrinterText_      = ResourceLoader.getPrintText("WAITING_FOR_PRINTER");
                waitingOnJobQueueQSPLText_  = ResourceLoader.getPrintText("WAITING_ON_JOB_QUEUE_QSPL");
                writerText_                 = ResourceLoader.getPrintText("WRITER");
                writerStatusText_           = ResourceLoader.getPrintText("WRITER_STATUS");
            }
            catch (Exception e) {
                errorEventSupport_.fireError (e);
            }
        }
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
}


