///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PrinterStartAction.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.Printer;
import com.ibm.as400.access.CommandCall;
import com.ibm.as400.access.Trace;
import com.ibm.as400.access.AS400Message;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

/**
The PrinterStartAction class represents the action of starting a writer
for the selected printer.
**/
class PrinterStartAction
extends DialogAction
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Private data.
    private static final String displayName_            = ResourceLoader.getText("ACTION_START");
    private static String generalTabText_; // @A2A
    private static String prtStartWtrText_;
    private static String outQText_;
    private static String outQLibText_;
    private static String msgQText_;
    private static String msgQLibText_;

    private static String libListText_;
    private static String curLibText_ ;
    private static String prtrDefText_;
    private static String userDefText_ ;
    private static String dvceDefText_;
    private static String fileDefText_ ;
    private static String inqMsgTypeText_ ;
    private static String inqInfoMsgTypeText_;
    private static String infoMsgTypeText_ ;
    private static String noneMsgTypeText_;

    private static String formTypeText_;
    private static String formTypeNotifyText_;
    private static String formTypeAllText_;
    private static String formTypeStdText_ ;
    private static String formTypeAGBTText_;

    private static String numSepPagesText_;
    private static String sepDrawerText_ ;

    // Advanced tab controls
    private static String advancedTabText_ ;
    private static String autoEndText_  ;
    private static String whenToEndText_ ;
    private static String directPrintText_;
    private static String formAlignText_ ;
    private static String firstFileText_ ;
    private static String jobNameText_ ;
    private static String jobUserText_ ;
    private static String jobNumberText_ ;
    private static String fileNumberText_ ;
    private static String startPageText_;
    private static String writerNameText_ ;

    // Advanced pane option strings
    private static String noText_ ;
    private static String yesText_ ;
    private static String onlyText_ ;
    private static String afterAllText_;
    private static String afterCurrentText_ ;
    private static String writerDefText_ ;
    private static String onlyFirstFileText_;
    private static String fileFirstAvailText_;
    private static String fileLastAvailText_ ;
    private static String defStartPageText_ ;
    private static boolean stringsLoaded_               = false; // Load MRI only once when needed
    private Integer             stringsLock_            = new Integer (0);

/* RDS- NOTE: Add "browse" buttons later */

    private Printer printer_                            = null; // the printer
    private JComboBox outqBox_                          = null; // Output Queue name
    private JComboBox outqLibBox_                       = null; // Output Queue Library name
    private JComboBox msgqBox_                          = null; // Message Queue name
    private JComboBox msgqLibBox_                       = null; // Message Queue Lib name
    private JComboBox formTypeBox_                      = null; // Form Type
    private JComboBox formNotifyBox_                    = null; // Form Type notification message
    private JComboBox numSepPageBox_                    = null; // Number of separator pages
    private JComboBox sepDrawerBox_                     = null; // Separator Drawer number

    // Advanced start options
    private JComboBox writerNameBox_                    = null; // Writer name
    private JComboBox autoEndBox_                       = null; // Automatically end?
    private JComboBox whenToEndBox_                     = null; // when to end
    private JComboBox directPrintBox_                   = null; // allow direct printing?
    private JComboBox formAlignBox_                     = null; // Form alignment
    private JComboBox firstFileBox_                     = null; // First file to print
    private JComboBox fileNumberBox_                    = null; // file Number
    private JComboBox startingPageBox_                  = null; // Starting page number
    private JTextField jobNameField_                    = null; // Name of owning job
    private JTextField jobUserField_                    = null; // user name of owning job
    private JTextField jobNumberField_                  = null; // number of owning job



/**
Constructs an PrinterStartAction object.

@param  object      The object.
@param  printer     The printer.
**/
    public PrinterStartAction (VObject object, Printer printer)
    {
        super (object);
        printer_ = printer;
    }

/* Builds the general settings pane
*/
    private JPanel buildGeneralPane()
        throws Exception
    {
        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout ();
        GridBagConstraints constraints;
        panel.setLayout (layout);
        panel.setBorder (new EmptyBorder (10, 10, 10, 10));

        try
        {
            // build components to display the start writer options
            String tempStr = new String(prtStartWtrText_);
            VUtilities.constrain( new JLabel(tempStr), panel, layout, 0,0,1,1);

            // get the printer name
            JTextField text = new JTextField(printer_.getName().trim());
            text.setEditable(false);
            VUtilities.constrain( text, panel, layout, 1,0,1,1);
            VUtilities.constrain (new JLabel (" "), panel, layout, 0, 1, 2, 1);

            // The output queue
            VUtilities.constrain (new JLabel (outQText_), panel, layout, 0, 2, 1, 1);
            outqBox_ = new JComboBox();
            outqBox_.setEditable(true);
            outqBox_.addItem(prtrDefText_);
            outqBox_.setSelectedItem(prtrDefText_);
            VUtilities.constrain( outqBox_, panel, layout, 1,2,1,1);
            VUtilities.constrain (new JLabel (outQLibText_), panel, layout, 0, 3, 1, 1);
            outqLibBox_ = new JComboBox();
            outqLibBox_.setEditable(true);
            outqLibBox_.addItem(libListText_);
            outqLibBox_.addItem(curLibText_);
            outqLibBox_.setSelectedItem(libListText_);                              //@A1A
            VUtilities.constrain( outqLibBox_, panel, layout, 1,3,1,1);
            VUtilities.constrain (new JLabel (" "), panel, layout, 0, 4, 2, 1);

            // The writer message queue
            VUtilities.constrain (new JLabel (msgQText_), panel, layout, 0, 5, 1, 1);
            msgqBox_ = new JComboBox();
            msgqBox_.setEditable(true);
            msgqBox_.addItem(prtrDefText_);
            msgqBox_.addItem(userDefText_);
            msgqBox_.setSelectedItem(prtrDefText_);
            VUtilities.constrain( msgqBox_, panel, layout, 1,5,1,1);
            VUtilities.constrain (new JLabel (msgQLibText_), panel, layout, 0, 6, 1, 1);
            msgqLibBox_ = new JComboBox();
            msgqLibBox_.setEditable(true);
            msgqLibBox_.addItem(libListText_);
            msgqLibBox_.addItem(curLibText_);
            msgqLibBox_.setSelectedItem(libListText_);                              //@A1A
            VUtilities.constrain( msgqLibBox_, panel, layout, 1,6,1,1);
            VUtilities.constrain (new JLabel (" "), panel, layout, 0, 7, 2, 1);

            // The form type boxes
            VUtilities.constrain (new JLabel (formTypeText_), panel, layout, 0, 8, 1, 1);
            formTypeBox_ = new JComboBox();
            formTypeBox_.setEditable(true);
            formTypeBox_.addItem(formTypeAllText_);
            formTypeBox_.addItem(formTypeStdText_);
            formTypeBox_.addItem(formTypeAGBTText_);
            formTypeBox_.setSelectedItem(formTypeAllText_);
            VUtilities.constrain( formTypeBox_, panel, layout, 1,8,1,1);
            VUtilities.constrain (new JLabel (formTypeNotifyText_), panel, layout, 0, 9, 1, 1);
            formNotifyBox_ = new JComboBox();
            formNotifyBox_.setEditable(false);
            formNotifyBox_.addItem(inqMsgTypeText_);
            formNotifyBox_.addItem(inqInfoMsgTypeText_);
            formNotifyBox_.addItem(infoMsgTypeText_);
            formNotifyBox_.addItem(noneMsgTypeText_);
            VUtilities.constrain( formNotifyBox_, panel, layout, 1,9,1,1);
            VUtilities.constrain (new JLabel (" "), panel, layout, 0, 10, 2, 1);

            // The separator page box
            VUtilities.constrain (new JLabel (numSepPagesText_), panel, layout, 0, 11, 1, 1);
            numSepPageBox_ = new JComboBox();
            numSepPageBox_.setEditable(false);
            numSepPageBox_.addItem(fileDefText_);

            // Add 0 thru 9
            for(int i=0; i < 10; i++) numSepPageBox_.addItem(String.valueOf(i));

            numSepPageBox_.addItem(userDefText_);
            VUtilities.constrain( numSepPageBox_, panel, layout, 1,11,1,1);

            // The drawer box
            VUtilities.constrain (new JLabel (sepDrawerText_), panel, layout, 0, 12, 1, 1);
            sepDrawerBox_ = new JComboBox();
            sepDrawerBox_.setEditable(false);
            sepDrawerBox_.addItem(dvceDefText_);

            // Add 255 more drawer choices
            for(int i=1; i < 256; i++) sepDrawerBox_.addItem(String.valueOf(i));

            VUtilities.constrain( sepDrawerBox_, panel, layout, 1,12,1,1);

            // Don't forget to add the browse buttons and advanced dialog someday
        }
        catch (Exception e)
        {
            panel = null;
            fireError (e);
        }

        return panel;
    }   // end buildGeneralPane()

/* Builds the advanced settings pane
*/
    private JPanel buildAdvancedPane()
        throws Exception
    {
        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout ();
        GridBagConstraints constraints;
        panel.setLayout (layout);
        panel.setBorder (new EmptyBorder (10, 10, 10, 10));

        try
        {
            // build components to display the start writer advanced options
            // The writer name
            VUtilities.constrain (new JLabel (writerNameText_), panel, layout, 0, 0, 1, 1);
            writerNameBox_ = new JComboBox();
            writerNameBox_.setEditable(true);
            writerNameBox_.addItem(prtrDefText_);
            writerNameBox_.setSelectedItem(prtrDefText_);
            VUtilities.constrain( writerNameBox_, panel, layout, 1,0,1,1);
            VUtilities.constrain (new JLabel (" "), panel, layout, 0, 1, 2, 1);

            // Auto end boxes
            VUtilities.constrain (new JLabel (autoEndText_), panel, layout, 0, 2, 1, 1);
            autoEndBox_ = new JComboBox();
            autoEndBox_.setEditable(false);
            autoEndBox_.addItem(noText_);
            autoEndBox_.addItem(yesText_);
            // autoEndBox_.setSelectedItem(noText_);                            //@A1D
            VUtilities.constrain( autoEndBox_, panel, layout, 1,2,1,1);
            VUtilities.constrain (new JLabel (whenToEndText_), panel, layout, 0, 3, 1, 1);
            whenToEndBox_ = new JComboBox();
            whenToEndBox_.setEditable(false);
            whenToEndBox_.addItem(afterAllText_);
            whenToEndBox_.addItem(afterCurrentText_);
            // whenToEndBox_.setSelectedItem(afterAllText_);                    //@A1D
            VUtilities.constrain( whenToEndBox_, panel, layout, 1,3,1,1);
            VUtilities.constrain (new JLabel (" "), panel, layout, 0, 4, 2, 1);

            // Allow Direct Printing
            VUtilities.constrain (new JLabel (directPrintText_), panel, layout, 0, 5, 1, 1);
            directPrintBox_ = new JComboBox();
            directPrintBox_.setEditable(false);
            directPrintBox_.addItem(noText_);
            directPrintBox_.addItem(yesText_);
            // directPrintBox_.setSelectedItem(noText_);                        //@A1D
            VUtilities.constrain( directPrintBox_, panel, layout, 1,5,1,1);
            VUtilities.constrain (new JLabel (" "), panel, layout, 0, 6, 2, 1);

            // Forms alignment
            VUtilities.constrain (new JLabel (formAlignText_), panel, layout, 0, 7, 1, 1);
            formAlignBox_ = new JComboBox();
            formAlignBox_.setEditable(false);
            formAlignBox_.addItem(fileDefText_);
            formAlignBox_.addItem(writerDefText_);
            formAlignBox_.addItem(onlyFirstFileText_);
            formAlignBox_.setSelectedItem(writerDefText_);
            VUtilities.constrain( formAlignBox_, panel, layout, 1,7,1,1);
            VUtilities.constrain (new JLabel (" "), panel, layout, 0, 8, 2, 1);

            // The first file to print controls
            // First file name
            VUtilities.constrain (new JLabel (firstFileText_), panel, layout, 0, 9, 1, 1);
            firstFileBox_ = new JComboBox();
            firstFileBox_.setEditable(true);
            firstFileBox_.addItem(fileFirstAvailText_);
            firstFileBox_.addItem(fileLastAvailText_);
            firstFileBox_.setSelectedItem(fileFirstAvailText_);
            VUtilities.constrain( firstFileBox_, panel, layout, 1,9,1,1);

            // First file job name
            VUtilities.constrain (new JLabel (jobNameText_), panel, layout, 0, 10, 1, 1);
            jobNameField_ = new JTextField(10);
            jobNameField_.setText("*");
            jobNameField_.setEditable(true);
            VUtilities.constrain( jobNameField_, panel, layout, 1,10,1,1);

            // First file job user name
            VUtilities.constrain (new JLabel (jobUserText_), panel, layout, 0, 11, 1, 1);
            jobUserField_ = new JTextField(10);
            jobUserField_.setText("");
            jobUserField_.setEditable(true);
            VUtilities.constrain( jobUserField_, panel, layout, 1,11,1,1);

            // First file job number
            VUtilities.constrain (new JLabel (jobNumberText_), panel, layout, 0, 12, 1, 1);
            jobNumberField_ = new JTextField(10);
            jobNumberField_.setText("");
            jobNumberField_.setEditable(true);
            VUtilities.constrain( jobNumberField_, panel, layout, 1,12,1,1);

            // First spooled file number
            VUtilities.constrain (new JLabel (fileNumberText_), panel, layout, 0, 13, 1, 1);
            fileNumberBox_ = new JComboBox();
            fileNumberBox_.setEditable(true);
            fileNumberBox_.addItem(onlyText_);
            fileNumberBox_.addItem(fileLastAvailText_);
            fileNumberBox_.setSelectedItem(onlyText_);
            VUtilities.constrain( fileNumberBox_, panel, layout, 1,13,1,1);
            VUtilities.constrain (new JLabel (" "), panel, layout, 0, 14, 2, 1);

            // The starting page box
            VUtilities.constrain (new JLabel (startPageText_), panel, layout, 0, 15, 1, 1);
            startingPageBox_ = new JComboBox();
            startingPageBox_.setEditable(true);
            startingPageBox_.addItem(defStartPageText_);
            startingPageBox_.setSelectedItem(defStartPageText_);
            VUtilities.constrain( startingPageBox_, panel, layout, 1,15,1,1);


            // Don't forget to add the browse buttons someday
        }
        catch (Exception e)
        {
            panel = null;
            fireError (e);
        }

        return panel;
    }   // end buildAdvancedPane()




/**
Returns the component for the dialog box.

@return The component.
**/
    public JComponent getInputComponent()
    {
        JTabbedPane tabbedPane = null;
        try
        {
            if(stringsLoaded_ == false)
                loadMRI();

            tabbedPane = new JTabbedPane();
            tabbedPane.addTab (generalTabText_, null, buildGeneralPane()); // @A2C
            tabbedPane.addTab (advancedTabText_, null, buildAdvancedPane());
            tabbedPane.setSelectedIndex (0);
            return tabbedPane;
        }
        catch (Exception e) {
            tabbedPane = null;
            fireError (e);
        }
        return tabbedPane;
    }


/**
Returns the display name for the action.

@return The display name.
**/
    public String getText ()
    {
        return displayName_;
    }

/*
Loads the MRI strings.  This is called only once per class
*/
    private void loadMRI()
    {
        synchronized (stringsLock_)
        {
            prtStartWtrText_        = ResourceLoader.getPrintText("PRINTER_TO_START")+ ":";
            generalTabText_         = ResourceLoader.getPrintText("GENERAL"); // @A2A
            outQText_               = ResourceLoader.getPrintText("OUTPUT_QUEUE") + ":";
            outQLibText_            = ResourceLoader.getPrintText("OUTPUT_QUEUE_LIB")+ ":";
            msgQText_               = ResourceLoader.getText("MESSAGE_QUEUE_DESCRIPTION")+ ":";
            msgQLibText_            = ResourceLoader.getPrintText("MESSAGE_QUEUE_LIB_DESCRIPTION") + ":";

            libListText_            = ResourceLoader.getPrintText("USE_LIBRARY_LIST");
            curLibText_             = ResourceLoader.getPrintText("USE__CURRENT_LIBRARY");
            prtrDefText_            = ResourceLoader.getPrintText("PRINTER_DEFAULT");
            userDefText_            = ResourceLoader.getPrintText("USER_DEFAULT");
            dvceDefText_            = ResourceLoader.getPrintText("DEVICE_DEFAULT");
            fileDefText_            = ResourceLoader.getPrintText("FILE_DEFAULT");
            inqMsgTypeText_         = ResourceLoader.getPrintText("MESSAGE_TYPE_INQUIRY");
            inqInfoMsgTypeText_     = ResourceLoader.getPrintText("MESSAGE_TYPE_INQ_INFO");
            infoMsgTypeText_        = ResourceLoader.getPrintText("MESSAGE_TYPE_INFO");
            noneMsgTypeText_        = ResourceLoader.getPrintText("MESSAGE_TYPE_NONE");

            formTypeText_           = ResourceLoader.getPrintText("FORM_TYPE")+ ":";
            formTypeNotifyText_     = ResourceLoader.getPrintText("FORM_TYPE_NOTIFY")+ ":";
            formTypeAllText_        = ResourceLoader.getPrintText("FORM_TYPE_ALL");
            formTypeStdText_        = ResourceLoader.getPrintText("FORM_TYPE_STANDARD");
            formTypeAGBTText_       = ResourceLoader.getPrintText("FORM_TYPE_ALL_GBT");

            numSepPagesText_        = ResourceLoader.getPrintText("NUMBER_OF_SEP_PAGES")+ ":";
            sepDrawerText_          = ResourceLoader.getPrintText("SEPARATOR_DRAWER") + ":";

            // Advanced tab controls
            advancedTabText_        = ResourceLoader.getPrintText("ADVANCED");
            autoEndText_            = ResourceLoader.getPrintText("WRITER_AUTO_END") + ":";
            whenToEndText_          = ResourceLoader.getPrintText("WRITER_WHEN_TO_END") + ":";
            directPrintText_        = ResourceLoader.getPrintText("DIRECT_PRINT") + ":";
            formAlignText_          = ResourceLoader.getPrintText("FORM_ALIGN") + ":";
            firstFileText_          = ResourceLoader.getPrintText("FIRST_FILE_NAME") + ":";
            jobNameText_            = ResourceLoader.getPrintText("FIRST_JOB_NAME") + ":";
            jobUserText_            = ResourceLoader.getPrintText("FIRST_JOB_USER") + ":";
            jobNumberText_          = ResourceLoader.getPrintText("FIRST_JOB_NUMBER") + ":";
            fileNumberText_         = ResourceLoader.getPrintText("FIRST_FILE_NUMBER") + ":";
            startPageText_          = ResourceLoader.getPrintText("FIRST_START_PAGE") + ":";
            writerNameText_         = ResourceLoader.getPrintText("WRITER_NAME") + ":";

            // Advanced pane option strings
            noText_                 = ResourceLoader.getPrintText("NO");
            yesText_                = ResourceLoader.getPrintText("YES");
            onlyText_               = ResourceLoader.getPrintText("ONLY");
            afterAllText_           = ResourceLoader.getPrintText("FILE_AFTER_ALL");
            afterCurrentText_       = ResourceLoader.getPrintText("FILE_AFTER_CURRENT");
            writerDefText_          = ResourceLoader.getPrintText("WRITER_DEFAULT");
            onlyFirstFileText_      = ResourceLoader.getPrintText("FILE_FORM_ALIGNMENT");
            fileFirstAvailText_     = ResourceLoader.getPrintText("FILE_FIRST_AVAILABLE");
            fileLastAvailText_      = ResourceLoader.getPrintText("FILE_LAST");
            defStartPageText_       = ResourceLoader.getPrintText("DEF_START_PAGE");
            stringsLoaded_ = true;
        }
    }   // endof loadMRI()

/**
Performs the action.
**/
    public void perform2 ()
    {
        try
        {
            String selectedItem = null;
            String selectedItem2 = null;

            // fire started working event
            fireStartWorking();

            // Create a string that contains the command, printer name, and all parameters
            StringBuffer cmdString_ = new StringBuffer("STRPRTWTR DEV(" + printer_.getName().trim() + ") OUTQ(");

            // Get the outq
            selectedItem = (String)outqBox_.getModel().getSelectedItem();

            if((selectedItem == null) || (selectedItem.equals(prtrDefText_)))
                cmdString_.append("*DEV) MSGQ(");
            else
            {
                // The user typed in an outq
                // Get the outq Library
                selectedItem2 = (String)outqLibBox_.getModel().getSelectedItem();

                if(selectedItem2.equals(libListText_))
                    cmdString_.append("*LIBL/");
                else if(selectedItem2.equals(curLibText_))
                    cmdString_.append("*CURLIB/");
                else if(selectedItem2.equals(""))
                    cmdString_.append("*LIBL/");
                else
                    cmdString_.append(selectedItem2 + "/");

                // Now add the outq name
                cmdString_.append(selectedItem + ") MSGQ(");
            }

            // Get the message queue
            selectedItem = (String)msgqBox_.getModel().getSelectedItem();

            if((selectedItem == null) || (selectedItem.equals(prtrDefText_)))
                cmdString_.append("*DEVD) FORMTYPE(");
            else if(selectedItem.equals(userDefText_))
                cmdString_.append("*REQUESTER) FORMTYPE(");
            else
            {
                // The user typed in an msgq
                // Get the msgq Library
                selectedItem2 = (String)msgqLibBox_.getModel().getSelectedItem();

                if(selectedItem2.equals(libListText_))
                    cmdString_.append("*LIBL/");
                else if(selectedItem2.equals(curLibText_))
                    cmdString_.append("*CURLIB/");
                else if(selectedItem2.equals(""))
                    cmdString_.append("*LIBL/");
                else
                    cmdString_.append(selectedItem2 + "/");

                // Now add the msgq name
                cmdString_.append(selectedItem + ") FORMTYPE(");
            }

            // Get the form type
            selectedItem = (String)formTypeBox_.getModel().getSelectedItem();

            if((selectedItem == null) || (selectedItem.equals(formTypeAllText_)))
                cmdString_.append("*ALL ");
            else if(selectedItem.equals(formTypeStdText_))
                cmdString_.append("*STD ");
            else if(selectedItem.equals(formTypeAGBTText_))
                cmdString_.append("*FORMS ");
            else
                cmdString_.append(selectedItem + " ");

            // Get the form message type
            selectedItem = (String)formNotifyBox_.getModel().getSelectedItem();

            if((selectedItem == null) || (selectedItem.equals(inqMsgTypeText_)))
                cmdString_.append("*INQMSG) FILESEP(");
            else if(selectedItem.equals(inqInfoMsgTypeText_))
                cmdString_.append("*MSG) FILESEP(");
            else if(selectedItem.equals(infoMsgTypeText_))
                cmdString_.append("*INFOMSG) FILESEP(");
            else
                cmdString_.append("*NOMSG) FILESEP(");

            // Get the number of separator pages
            selectedItem = (String)numSepPageBox_.getModel().getSelectedItem();

            if((selectedItem == null) || (selectedItem.equals(fileDefText_)))
                cmdString_.append("*FILE) SEPDRAWER(");
            else
                cmdString_.append(selectedItem + ") SEPDRAWER(");

            // Get the separator source drawer
            selectedItem = (String)sepDrawerBox_.getModel().getSelectedItem();

            if((selectedItem == null) || (selectedItem.equals(dvceDefText_)))
                cmdString_.append("*DEVD)");
            else if(selectedItem.equals(fileDefText_))
                cmdString_.append("*FILE)");
            else
                cmdString_.append(selectedItem + ")");

            // Get the advanced options
            // Writer name
            selectedItem = (String)writerNameBox_.getModel().getSelectedItem();

            if((selectedItem != null) && (!selectedItem.equals(prtrDefText_)))
                cmdString_.append(" WTR(" + selectedItem + ")");

            // Auto End
            selectedItem = (String)autoEndBox_.getModel().getSelectedItem();

            if((selectedItem != null) && (!selectedItem.equals(noText_)))
            {
                cmdString_.append(" AUTOEND(*YES ");
                selectedItem = (String)whenToEndBox_.getModel().getSelectedItem();

                if(selectedItem.equals(afterCurrentText_))
                    cmdString_.append("*FILEEND)");
                else
                    cmdString_.append("*NORDYF)");
            }

            // Direct Printing
            selectedItem = (String)directPrintBox_.getModel().getSelectedItem();

            if((selectedItem != null) && (selectedItem.equals(yesText_)))
                cmdString_.append(" ALWDRTPRT(*YES)");

            // Forms alignment
            selectedItem = (String)formAlignBox_.getModel().getSelectedItem();

            if((selectedItem != null) || (selectedItem.equals(fileDefText_)))
                cmdString_.append(" ALIGN(*FILE)");
            else if(!selectedItem.equals(onlyFirstFileText_))
                cmdString_.append(" ALIGN(*FIRST)");

            // First file to print boxes
            selectedItem = (String)firstFileBox_.getModel().getSelectedItem();

            if((selectedItem != null) && (!selectedItem.equals(fileFirstAvailText_)))
            {
                if(selectedItem.equals(fileLastAvailText_))
                    cmdString_.append(" FILE(*LAST)");
                else
                {
                    // The user specified a file so get all the parms
                    cmdString_.append(" FILE(" + selectedItem + ") JOB(");
                    cmdString_.append(jobNumberField_.getText() + "/");
                    cmdString_.append(jobUserField_.getText() + "/");
                    cmdString_.append(jobNameField_.getText() + ") SPLNBR(");
                    selectedItem = (String)fileNumberBox_.getModel().getSelectedItem();

                    if(selectedItem.equals(onlyText_))
                        cmdString_.append("*ONLY)");
                    else if(selectedItem.equals(fileLastAvailText_))
                        cmdString_.append("*LAST)");
                    else
                        cmdString_.append(selectedItem + ")"); // @A2C
                }
            }

            // Starting page
            selectedItem = (String)startingPageBox_.getModel().getSelectedItem();

            if((selectedItem != null) && (!selectedItem.equals(defStartPageText_)))  //@A1C
                cmdString_.append(" PAGE(" + selectedItem + ")");


            // send the command to start the writer
            CommandCall cmd = new CommandCall( printer_.getSystem());
            try
            {
                if (cmd.run(cmdString_.toString())!=true)
                {
                    // Note that there was an error
                    if (Trace.isTraceOn())
                        Trace.log (Trace.ERROR, "STRPRTWTR cmd=false for [" + printer_.getName () + "].");

                    // fire an error event
                    Exception e = new Exception(ResourceLoader.getText("EXC_AS400_ERROR"));
                    fireError(e);
                }
                else                                                    //@A1A
                {
                    // trace the send
                    if (Trace.isTraceOn())                              //@A1M
                        Trace.log (Trace.INFORMATION, "Started writer for printer[" + printer_.getName() + "].");//@A1M

                    fireObjectChanged ();                               //@A1M
                }

                // Trace the messages (returned whether or not there was an error)
                if (Trace.isTraceOn())
                {
                    AS400Message[] messagelist = cmd.getMessageList();
                    for (int i=0; i < messagelist.length; i++)
                    {
                        // show each message
                        Trace.log (Trace.INFORMATION, messagelist[i].getText());
                    }
                }
            }
            catch (Exception e)
            {
                if (Trace.isTraceOn())
                    Trace.log (Trace.ERROR, "ERROR CommandCall exception for starting [" + printer_.getName () + "].");

                fireError(e);                                           //@A1M
            }

            // fire stopped working event
            fireStopWorking();
        } // end try block
        catch (Exception e)
        {
            // trace the error
            if (Trace.isTraceOn())
                Trace.log (Trace.ERROR, "ERROR Starting writer for printer [" + printer_.getName () + "].");

            fireError (e);
        }
    }

} // end PrinterStartAction class

