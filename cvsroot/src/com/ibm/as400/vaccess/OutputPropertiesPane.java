///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: OutputPropertiesPane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.SpooledFile;
import com.ibm.as400.access.PrintObject;
import com.ibm.as400.access.PrintParameterList;
import com.ibm.as400.access.QSYSObjectPathName;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;
import javax.swing.border.EmptyBorder;
import java.text.DateFormat;
import java.util.Date;
import java.util.Calendar;


/**
The OutputPropertiesPane class represents the property pane
for the VOutput object.
**/
class OutputPropertiesPane
implements VPropertiesPane, ItemListener, ActionListener
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Private data.
    private VOutput object_;                    // our visual output object
    private SpooledFile splF_;                  // the spooled file object
    private boolean functionSupported_;         // @A2A

    private JTextField userData_;               // user specified data field
    private JComboBox saveBox_;                 // save after printing field
    private JTextField printer_;                // printer field
    private JTextField outQ_;                   // output queue name field
    private JTextField outQLib_;                // output queue library field
    private JComboBox outQPtyBox_;              // output queue priority field
    private JTextField totalCopies_;            // total copies field
    private JTextField copiesLeft_;             // copies left field
    private JTextField formType_;               // form type field

    // Event support.
    private ChangeEventSupport  changeEventSupport_     = new ChangeEventSupport (this);
    private ErrorEventSupport   errorEventSupport_      = new ErrorEventSupport (this);
    private VObjectEventSupport objectEventSupport_     = new VObjectEventSupport (this);
    private WorkingEventSupport workingEventSupport_    = new WorkingEventSupport (this);

    // Static data.
    private static  DateFormat  dateFormat_     = DateFormat.getDateTimeInstance ();
    private static  Calendar    calendar_       = Calendar.getInstance ();
    private static final int    supportedVRM_   = 0X00040100; // @A2A

    // MRI
    private static final String noText_             = ResourceLoader.getPrintText("NO");
    private static final String yesText_            = ResourceLoader.getPrintText("YES");
    private static final String jobValueText_       = ResourceLoader.getPrintText("JOB_VALUE");
    private static final String standardText_       = ResourceLoader.getPrintText("STANDARD");
    private static final String outputNameText_     = ResourceLoader.getPrintText("OUTPUT_NAME") + ":";
    private static final String numberText_         = ResourceLoader.getPrintText("NUMBER") + ":";
    private static final String statusText_         = ResourceLoader.getPrintText("STATUS") + ":";
    private static final String statusClosedText_   = ResourceLoader.getPrintText("CLOSED");
    private static final String statusHeldText_     = ResourceLoader.getPrintText("HELD");
    private static final String statusMsgWaitingText_= ResourceLoader.getPrintText("MESSAGE_WAITING");
    private static final String statusOpenText_     = ResourceLoader.getPrintText("OPEN");
    private static final String statusPendingText_  = ResourceLoader.getPrintText("PENDING");
    private static final String statusPrinterText_  = ResourceLoader.getPrintText("PRINTER");
    private static final String statusReadyText_    = ResourceLoader.getPrintText("READY");
    private static final String statusSavedText_    = ResourceLoader.getPrintText("SAVED");
    private static final String statusWritingText_  = ResourceLoader.getPrintText("WRITING");
    private static final String userSpecDataText_   = ResourceLoader.getPrintText("USER_SPEC_DATA") + ":";
    private static final String saveText_           = ResourceLoader.getPrintText("SAVE_AFTER_PRINTING") + ":";
    private static final String printerText_        = ResourceLoader.getPrintText("PRINTER") + ":";
    private static final String outQText_           = ResourceLoader.getPrintText("OUTPUT_QUEUE") + ":";
    private static final String outQLibText_        = ResourceLoader.getPrintText("OUTPUT_QUEUE_LIB") + ":";
    private static final String outQPtyText_        = ResourceLoader.getPrintText("OUTQ_PRIORITY_1_9") + ":";
    private static final String totalCopiesText_    = ResourceLoader.getPrintText("TOTAL_COPIES_1_255") + ":";
    private static final String copiesLeftText_     = ResourceLoader.getPrintText("COPIES_LEFT_1_255") + ":";
    private static final String pagesPerCopyText_   = ResourceLoader.getPrintText("PAGES_PER_COPY") + ":";
    private static final String currentPageText_    = ResourceLoader.getPrintText("CURRENT_PAGE") + ":";
    private static final String lastPageText_       = ResourceLoader.getPrintText("LAST_PAGE") + ":";
    private static final String formTypeText_       = ResourceLoader.getPrintText("FORM_TYPE") + ":";
    private static final String dateText_           = ResourceLoader.getPrintText("DATE_CREATED") + ":";
    private static final String jobText_            = ResourceLoader.getPrintText("JOB") + ":";
    private static final String userText_           = ResourceLoader.getPrintText("USER") + ":";
    private static final String jobNumberText_      = ResourceLoader.getPrintText("JOB_NUMBER") + ":";
    private static final String generalText_        = ResourceLoader.getPrintText("GENERAL");
    private static final String printerQueText_     = ResourceLoader.getPrintText("PRINTERQUEUE");
    private static final String copiesText_         = ResourceLoader.getPrintText("COPIES");
    private static final String pagesText_          = ResourceLoader.getPrintText("PAGES");
    private static final String formsText_          = ResourceLoader.getPrintText("FORMS");
    private static final String originText_         = ResourceLoader.getPrintText("ORIGIN");
    private static final String userCommentText_    = ResourceLoader.getPrintText("USER_COMMENT") + ":";
    private static final String notAssignedText_    = ResourceLoader.getPrintText("NOT_ASSIGNED");
    private static final String groupText_          = ResourceLoader.getPrintText("GROUP");

/**
Constructs an OutputPropertiesPane object.

@param  object The object.
@param  splF The spooled file.
**/
    public OutputPropertiesPane (VOutput object, SpooledFile splF)
    {
        object_ = object;
        splF_ = splF;

        // @A2A
        try
        {
            int systemVRM = splF_.getSystem().getVRM();
            if (systemVRM < supportedVRM_)
            {
                functionSupported_ = false;
            }
            else
            {
                functionSupported_ = true;
            }
        }
        catch (Exception e)
        {
            errorEventSupport_.fireError (e);
            functionSupported_ = false;
        }
    }

/**
catches the state change of text field
**/
    public void actionPerformed(ActionEvent e)
    {
        // notify that something has changed so the apply button is enabled
        changeEventSupport_.fireStateChanged ();
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

@throws Exception
**/
    public void applyChanges ()
      throws Exception
    {
        // create a print parameter list to hold the attributes that were updated
        PrintParameterList pList_ = new PrintParameterList();

        ////////////////
        // user data ///
        ////////////////

        // get the previous value from the spooled file
        String oldUserData = splF_.getStringAttribute(PrintObject.ATTR_USERDATA).trim();

        // get the new value from the textfield
        String newUserData = (String)userData_.getText().trim();

        // compare the values to see if a change is needed, and set as necessary
        if (!oldUserData.equals(newUserData)) pList_.setParameter(PrintObject.ATTR_USERDATA,newUserData);

        //////////////////////////
        // save after printing ///
        //////////////////////////

        // get the previous value from the spooled file
        String oldSaveData = splF_.getStringAttribute(PrintObject.ATTR_SAVE);

        // get the new value from the combo box
        String newSaveData = (String)saveBox_.getSelectedItem();

        // change new value if necessary
        if (newSaveData.equals(noText_)) newSaveData = "*NO";
        else if (newSaveData.equals(yesText_)) newSaveData = "*YES";

        // compare the values to see if a change is needed, and set as necessary
        if (!oldSaveData.equals(newSaveData)) pList_.setParameter(PrintObject.ATTR_SAVE,newSaveData);

        if (functionSupported_ == true) // @A2A
        {
            //////////////
            // printer ///
            //////////////
            // get the previous value from the spooled file
            String oldPrinter = splF_.getStringAttribute(PrintObject.ATTR_PRINTER).trim();

            // get the new value from the textfield
            String newPrinter = (String)(printer_.getText().trim());

            // compare the values to see if a change is needed, and set as necessary
            if ((!newPrinter.equals(notAssignedText_)) && (!oldPrinter.equals(newPrinter))) {
                pList_.setParameter(PrintObject.ATTR_PRINTER,newPrinter);
            }

            ///////////////////
            // output queue ///
            ///////////////////

            // extract the output queue path into an object that knows how to parse it.
            QSYSObjectPathName outQPath = new QSYSObjectPathName(splF_.getStringAttribute(PrintObject.ATTR_OUTPUT_QUEUE));

            // get the previous value from the spooled file
            String oldOutQ = outQPath.getObjectName().trim();

            // get the new value from the textfield
            String newOutQ = (String)outQ_.getText().trim();

            // flag to indicate a output queue change
            boolean fNewOutQ = false;

            // compare the values to see if a change is needed, and set as necessary
            if (!oldOutQ.equals(newOutQ))
                {
                // create a QSYSObjectPathName object to represent the new output queue with the old outq library
                QSYSObjectPathName newOutQPath = new QSYSObjectPathName(outQPath.getLibraryName(), newOutQ, "OUTQ");

                // set the IFS output queue attribute
                pList_.setParameter(PrintObject.ATTR_OUTPUT_QUEUE,newOutQPath.getPath());

                // set the flag to say that we did have an output queuue change
                fNewOutQ = true;
                }

            ///////////////////////////
            // output queue library ///
            ///////////////////////////

            // get the previous value from the spooled file
            String oldOutQLib = outQPath.getLibraryName().trim();

            // get the new value from the textfield
            String newOutQLib = (String)outQLib_.getText().trim();

            // compare the values to see if a change is needed, and set as necessary
            if (!oldOutQLib.equals(newOutQLib))
                {
                QSYSObjectPathName newOutQPath;

                // check to see if we had a output queue name change also
                if (fNewOutQ)
                    // create a QSYSObjectPathName object to represent the new output queue library and new output queue name
                    newOutQPath = new QSYSObjectPathName(newOutQLib, newOutQ, "OUTQ");
                else
                    // create a QSYSObjectPathName object to represent the new output queue library and old output queue name
                    newOutQPath = new QSYSObjectPathName(newOutQLib, oldOutQ, "OUTQ");

                // set the IFS output queue attribute
                pList_.setParameter(PrintObject.ATTR_OUTPUT_QUEUE,newOutQPath.getPath());
                }
        }

        ////////////////////////////
        // output queue priority ///
        ////////////////////////////

        // get the previous value from the spooled file
        String oldOutQPty = splF_.getStringAttribute(PrintObject.ATTR_OUTPTY);

        // get the new value from the combo box
        String newOutQPty = (String)outQPtyBox_.getSelectedItem();

        // check to see if user selected Job Value if so set priority to *JOB
        if (newOutQPty.equals(jobValueText_)) newOutQPty = "*JOB";

        // compare the values to see if a change is needed, and set as necessary
        if (!oldOutQPty.equals(newOutQPty)) pList_.setParameter(PrintObject.ATTR_OUTPTY,newOutQPty);

        ///////////////////
        // Total copies ///
        ///////////////////

        // get the previous value from the spooled file
        String oldTotalCopies = splF_.getIntegerAttribute(PrintObject.ATTR_COPIES).toString();

        // get the new value from the textfield
        String newTotalCopies = (String)totalCopies_.getText().trim();

        // compare the values to see if a change is needed, and set as necessary
        if (!oldTotalCopies.equals(newTotalCopies))
            pList_.setParameter(PrintObject.ATTR_COPIES,Integer.parseInt(newTotalCopies));

        //////////////////
        // Copies left ///
        //////////////////

        // get the previous value from the spooled file
        String oldCopiesLeft = splF_.getIntegerAttribute(PrintObject.ATTR_COPIESLEFT).toString();

        // get the new value from the textfield
        String newCopiesLeft = (String)copiesLeft_.getText().trim();

        // compare the values to see if a change is needed, and set as necessary
        if (!oldCopiesLeft.equals(newCopiesLeft))
            pList_.setParameter(PrintObject.ATTR_COPIESLEFT,Integer.parseInt(newCopiesLeft));

        ///////////////
        // Form type //
        ///////////////

        // get the previous value from the spooled file
        String oldFormType = splF_.getStringAttribute(PrintObject.ATTR_FORMTYPE).trim();

        // get the new value from the textfield
        String newFormType = (String)formType_.getText().trim();
        if (newFormType.equals(standardText_)) newFormType = "*STD";

        // compare the values to see if a change is needed, and set as necessary
        if (!oldFormType.equals(newFormType)) pList_.setParameter(PrintObject.ATTR_FORMTYPE,newFormType);


        // fire started working event
        workingEventSupport_.fireStartWorking();

        //////////////////////////////////////////////////////
        // Set the requested attributes of the spooled file //
        //////////////////////////////////////////////////////
        splF_.setAttributes(pList_);

        // fire stopped working event
        workingEventSupport_.fireStopWorking();

        // notify object that there have been changes
        objectEventSupport_.fireObjectChanged(object_);
     }


/**
Builds Copies pane for the properties pane
**/
    private JPanel buildCopiesPane()
    {
        JPanel thePane = new JPanel ();
        GridBagLayout layout = new GridBagLayout ();
        GridBagConstraints constraints;
        thePane.setLayout (layout);
        thePane.setBorder (new EmptyBorder (10, 10, 10, 10));

        try
            {
            // Total copies
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (new JLabel (totalCopiesText_), // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  0,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            totalCopies_ = new JTextField(splF_.getIntegerAttribute(PrintObject.ATTR_COPIES).toString());
            totalCopies_.addActionListener(this);
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (totalCopies_, // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  0,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);

            // Copies left
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (new JLabel (copiesLeftText_), // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  1,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            copiesLeft_ = new JTextField(splF_.getIntegerAttribute(PrintObject.ATTR_COPIESLEFT).toString());
            copiesLeft_.addActionListener(this);
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (copiesLeft_, // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  1,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            }
        catch (Exception e)
            {
          //  thePane = null;  @A4D
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

        try
            {
            // Spooled file name
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (new JLabel (outputNameText_), // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  0,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (new JLabel (splF_.getName()), // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  0,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);

            // Spooled file number
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (new JLabel (numberText_), // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  1,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (new JLabel (Integer.toString(splF_.getNumber())), // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  1,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);

            // Spooled file status
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (new JLabel (statusText_), // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  2,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            String status = splF_.getStringAttribute(PrintObject.ATTR_SPLFSTATUS);
            String stat = null;
            if (status.trim().equals("*CLOSED")) stat = statusClosedText_;
            else if (status.trim().equals("*HELD")) stat =  statusHeldText_;
            else if (status.trim().equals("*MESSAGE")) stat =  statusMsgWaitingText_;
            else if (status.trim().equals("*OPEN")) stat =  statusOpenText_;
            else if (status.trim().equals("*PENDING")) stat =  statusPendingText_;
            else if (status.trim().equals("*PRINTER")) stat =  statusPrinterText_;
            else if (status.trim().equals("*READY")) stat =  statusReadyText_;
            else if (status.trim().equals("*SAVED")) stat =  statusSavedText_;
            else if (status.trim().equals("*WRITING")) stat =  statusWritingText_;
            else stat = " ";
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (new JLabel (stat), // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  2,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);

            // User specified data
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (new JLabel (userSpecDataText_), // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  3,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            userData_ = new JTextField(splF_.getStringAttribute(PrintObject.ATTR_USERDATA));
            userData_.addActionListener(this);
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (userData_, // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  3,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);

            // User comment
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (new JLabel (userCommentText_), // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  4,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (new JLabel (splF_.getStringAttribute(PrintObject.ATTR_USERCMT).trim()), // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  4,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);

            // Save after printing
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (new JLabel (saveText_), // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  5,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            saveBox_ = new JComboBox();
            saveBox_.addItem(yesText_);
            saveBox_.addItem(noText_);

            String curSave = splF_.getStringAttribute(PrintObject.ATTR_SAVE).trim();
            if (curSave.equals("*NO")) saveBox_.setSelectedItem(noText_);
            else if (curSave.equals("*YES")) saveBox_.setSelectedItem(yesText_);
            else saveBox_.setSelectedItem(curSave);
            saveBox_.addItemListener(this);
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (saveBox_, // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  5,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            }
        catch (Exception e)
            {
           //  thePane = null;  @A4D
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
            // Form type
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (new JLabel (formTypeText_), // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  0,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            String formType = splF_.getStringAttribute(PrintObject.ATTR_FORMTYPE).trim();
            if (formType.equals("*STD")) formType_ = new JTextField(standardText_);
            else formType_ = new JTextField(formType);
            formType_.addActionListener(this);
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (formType_, // @A1C
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
          //  thePane = null;    @A4D
            errorEventSupport_.fireError (e);
            }

        return thePane;
    }


/**
Builds Origin pane for the properties pane
**/
    private JPanel buildOriginPane()
    {
        JPanel thePane = new JPanel ();
        GridBagLayout layout = new GridBagLayout ();
        GridBagConstraints constraints;
        thePane.setLayout (layout);
        thePane.setBorder (new EmptyBorder (10, 10, 10, 10));

        try
            {
            // Date created

            // get the date and time from spooled file
            String date = splF_.getStringAttribute(PrintObject.ATTR_DATE);
            String time = splF_.getStringAttribute(PrintObject.ATTR_TIME);
            // @A3A - The code below used to substring (1,3) for the year, omitting
            //        the century field, and did not add 1900 to the result (base time).
            //        The code now substrings (0,3) and adds in 1900, thus
            //        producing the correct date.
            calendar_.set((Integer.parseInt(date.substring(0,3)) + 1900),// year @A3C
                           Integer.parseInt(date.substring(3,5))-1,// month is zero based
                           Integer.parseInt(date.substring(5,7)),  // day
                           Integer.parseInt(time.substring(0,2)),  // hour
                           Integer.parseInt(time.substring(2,4)),  // minute
                           Integer.parseInt(time.substring(4,6))); // second
            Date newDate = calendar_.getTime();

            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (new JLabel (dateText_), // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  0,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (new JLabel (dateFormat_.format (newDate)), // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  0,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);

            // Job
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (new JLabel (jobText_), // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  1,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (new JLabel (splF_.getStringAttribute(PrintObject.ATTR_JOBNAME)), // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  1,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);

            // User
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (new JLabel ("  " + userText_), // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  2,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (new JLabel (splF_.getStringAttribute(PrintObject.ATTR_JOBUSER)), // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  2,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);

            // Job number
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (new JLabel ("  " + jobNumberText_), // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  3,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (new JLabel (splF_.getStringAttribute(PrintObject.ATTR_JOBNUMBER)), // @A1C
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
          //  thePane = null;  @A4D
            errorEventSupport_.fireError (e);
            }

        return thePane;
    }


/**
Builds Pages pane for the properties pane
**/
    private JPanel buildPagesPane()
    {
        JPanel thePane = new JPanel ();
        GridBagLayout layout = new GridBagLayout ();
        GridBagConstraints constraints;
        thePane.setLayout (layout);
        thePane.setBorder (new EmptyBorder (10, 10, 10, 10));

        try
            {
            // Pages per copy
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (new JLabel (pagesPerCopyText_), // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  0,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (new JLabel (splF_.getIntegerAttribute(PrintObject.ATTR_PAGES).toString()), // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  0,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);

            // Current page
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (new JLabel (currentPageText_), // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  1,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (new JLabel (splF_.getIntegerAttribute(PrintObject.ATTR_CURPAGE).toString()), // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  1,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);

            // Last page printed
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (new JLabel (lastPageText_), // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  2,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (new JLabel (splF_.getIntegerAttribute(PrintObject.ATTR_LASTPAGE).toString()), // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  2,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            }
        catch (Exception e)
            {
          //  thePane = null;  @A4D
            errorEventSupport_.fireError (e);
            }


        return thePane;
    }


/**
Builds Printer/Queue pane for the properties pane
**/
    private JPanel buildPrinterQueuePane()
    {
        JPanel thePane = new JPanel ();
        GridBagLayout layout = new GridBagLayout ();
        GridBagConstraints constraints;
        thePane.setLayout (layout);
        thePane.setBorder (new EmptyBorder (10, 10, 10, 10));
        QSYSObjectPathName outQPath = new QSYSObjectPathName();

        try
            {
            // Printer name
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (new JLabel (printerText_), // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  0,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            if (functionSupported_ == true) // @A2A
            {
                String printer = splF_.getStringAttribute(PrintObject.ATTR_PRINTER).trim();
                String prtAssigned = splF_.getStringAttribute(PrintObject.ATTR_PRTASSIGNED).trim();
                // check to see if the spooled file is assigned to a printer or group of printers or not assigned
                if (prtAssigned.equals("3")) printer_ = new JTextField(notAssignedText_); // not assigned
                else if (prtAssigned.equals("2")) printer_ = new JTextField(groupText_); // group
                else printer_ = new JTextField(printer); // a printer
                printer_.addActionListener(this);
                constraints = new GridBagConstraints();         // @A1A
                VUtilities.constrain (printer_, // @A1C
                                      thePane,
                                      layout,
                                      constraints,
                                      1,1,1,
                                      0,1,1,
                                      constraints.HORIZONTAL,
                                      constraints.WEST);
            }

            // Output Queue name
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (new JLabel (outQText_), // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  1,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            if (functionSupported_ == true) // @A2A
            {
                outQPath.setPath(splF_.getStringAttribute(PrintObject.ATTR_OUTPUT_QUEUE));
                outQ_ = new JTextField(outQPath.getObjectName());
                outQ_.addActionListener(this);
                constraints = new GridBagConstraints();         // @A1A
                VUtilities.constrain (outQ_, // @A1C
                                      thePane,
                                      layout,
                                      constraints,
                                      1,1,1,
                                      1,1,1,
                                      constraints.HORIZONTAL,
                                      constraints.WEST);
            }

            // Output Queue library name
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (new JLabel (outQLibText_), // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  2,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            if (functionSupported_ == true) // @A2A
            {
                outQLib_ = new JTextField(outQPath.getLibraryName());
                outQLib_.addActionListener(this);
                constraints = new GridBagConstraints();         // @A1A
                VUtilities.constrain (outQLib_, // @A1C
                                      thePane,
                                      layout,
                                      constraints,
                                      1,1,1,
                                      2,1,1,
                                      constraints.HORIZONTAL,
                                      constraints.WEST);
            }

            // Output Queue Priority
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (new JLabel (outQPtyText_), // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  3,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            outQPtyBox_ = new JComboBox();
            outQPtyBox_.setEditable(true);
            String outqPty = splF_.getStringAttribute(PrintObject.ATTR_OUTPTY).trim();
            outQPtyBox_.addItem(jobValueText_);
            // we need to check for *JOB special value which equals priority 0
            if (outqPty.equals("*JOB"))
                {
                outQPtyBox_.setSelectedItem(jobValueText_);
                }
            else
                {
                outQPtyBox_.addItem(outqPty);
                outQPtyBox_.setSelectedItem(outqPty);
                }
            outQPtyBox_.addItemListener(this);
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (outQPtyBox_, // @A1C
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
          //  thePane = null;   @A4D
            errorEventSupport_.fireError (e);
            }


        return thePane;
    }


/**
Returns the properties pane.

@return             The properties pane.
@throws Exception   If an error occurs.
 **/
    public Component getComponent ()
    {
        JTabbedPane tabbedPane = null;
        tabbedPane = new JTabbedPane ();
        tabbedPane.addTab (generalText_, null, buildGeneralPane());
        tabbedPane.setSelectedIndex (0);
        tabbedPane.addTab (printerQueText_, null, buildPrinterQueuePane());
        tabbedPane.addTab (copiesText_, null, buildCopiesPane());
        tabbedPane.addTab (pagesText_, null, buildPagesPane());
        tabbedPane.addTab (formsText_, null, buildFormsPane());
        tabbedPane.addTab (originText_, null, buildOriginPane());
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


} // end OutputPropertiesPane class


