///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VPrinter.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

// Toolbox imports
import com.ibm.as400.access.AS400;  // @A15A
import com.ibm.as400.access.Printer;
import com.ibm.as400.access.PrintObject;
import com.ibm.as400.access.QSYSObjectPathName;
import com.ibm.as400.access.RequestNotSupportedException; // @A15A
import com.ibm.as400.access.Trace;

// swing imports
import javax.swing.Icon;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreeNode;

// Java awt imports
import java.awt.Component;

// Java bean imports
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.beans.PropertyVetoException;


// Java imports
import java.io.Serializable;
import java.net.URL;
import java.util.Enumeration;


/**
The VPrinter class defines the representation of a
server printer for use in various models and panes
in this package.

<p>A VPrinter object has no children.  Its details
children are the spooled files (VOutput objects)
on the printer.

<p>Most errors are reported as ErrorEvents rather than
throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>VPrinter objects generate the following events:
<ul>
    <li>ErrorEvent
    <li>PropertyChangeEvent
    <li>VObjectEvent
    <li>WorkingEvent
</ul>

@see com.ibm.as400.access.Printer
**/


public class VPrinter
implements VNode, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

/**
Property identifier for the output queue.
**/
    public static final Object  OUTPUTQUEUE_PROPERTY      = "Output queue";

/**
Property identifier for the printer name.
**/
    public static final Object  PRINTER_PROPERTY          = "Printer";

/**
Property identifier for the status.
**/
    public static final Object  STATUS_PROPERTY           = "Status";

    // Static data.
    private static String       description_ = ResourceLoader.getPrintText("AS400_PRINTER");
    private static Icon         icon16_      = ResourceLoader.getIcon ("VPrinter16.gif", description_);
    private static Icon         icon32_      = ResourceLoader.getIcon ("VPrinter32.gif", description_);
    private static final int    supportedVRM_   = 0X00040100; // @A15A
    private static boolean      requestNotSupportedFired_ = false; // @A15A

    // private data.
    private Printer                 printer_    = null;

    // @A9A
    transient private VNode                   parent_;
    transient private VPrinterOutput          printerOutput_;
    transient private VAction[]               actions_;
    transient private PrinterPropertiesPane   propertiesPane_;
    transient private boolean                 systemNotSupported_ = false; // @A15A

    // Event support.
    transient private ErrorEventSupport      errorEventSupport_;
    transient private VObjectEventSupport    objectEventSupport_;
    transient private PropertyChangeSupport  propertyChangeSupport_;
    transient private VetoableChangeSupport  vetoableChangeSupport_;
    transient private WorkingEventSupport    workingEventSupport_;
    transient private VObjectListener_       objectListener_;  //@A8A

    // Constants for Actions
    private static final int HOLD_ACTION             =  0;
    private static final int RELEASE_ACTION          =  1;
    private static final int START_ACTION            =  2;
    private static final int STOP_ACTION             =  3;
    private static final int AVAILABLE_ACTION        =  4;
    private static final int UNAVAILABLE_ACTION      =  5;
    private static final int LAST_ACTION             =  UNAVAILABLE_ACTION;

    // MRI
    private static String activeReaderText_ = null;
    private static String activeText_;
    private static String activeWriterText_;
    private static String afterAllFilesPrintText_;
    private static String afterCurFilePrintsText_;
    private static String allText_;
    private static String as36DisabledText_;
    private static String as36EnabledText_;
    private static String automaticText_;
    private static String availableText_;
    private static String availablePendingText_;
    private static String beingServicedText_;
    private static String connectPendingText_;
    private static String damagedText_;
    private static String deviceDefaultText_;
    private static String diagnosticModeText_;
    private static String endedText_;
    private static String failedReaderText_;
    private static String failedText_;
    private static String failedWriterText_;
    private static String fileDefaultText_;
    private static String formsText_;
    private static String heldText_;
    private static String holdPendingText_;
    private static String lockedText_;
    private static String messageWaitingText_;
    private static String noText_;
    private static String onJobQueueText_;
    private static String onlyFirstFileText_;
    private static String poweredOffText_;
    private static String poweredOffNotAvailText_;
    private static String printingText_;
    private static String recoveryCancelledText_;
    private static String recoveryPendingText_;
    private static String releasedText_;
    private static String signonDisplayText_;
    private static String standardText_;
    private static String startedText_;
    private static String stopPendingText_;
    private static String stoppedText_;
    private static String unavailableText_;
    private static String unavailablePendingText_;
    private static String unknownText_;
    private static String unusableText_;
    private static String variedOffText_;
    private static String variedOnText_;
    private static String varyOffPendingText_;
    private static String varyOnPendingText_;
    private static String waitingForOutQText_;
    private static String waitingForPrinterText_;
    private static String waitingOnMessageText_;
    private static String waitingOnJobQueueQSPL_;
    private static String yesText_;

    // Overall status values
    static final int OVERALLSTATUS_UNAVAILABLE = 1;
    static final int OVERALLSTATUS_POWEREDOFFNOTAVAILALBE = 2;
    static final int OVERALLSTATUS_STOPPED = 3;
    static final int OVERALLSTATUS_MESSAGEWAITING = 4;
    static final int OVERALLSTATUS_HELD = 5;
    static final int OVERALLSTATUS_STOPPENDING = 6;
    static final int OVERALLSTATUS_HOLDPENDING = 7;
    static final int OVERALLSTATUS_WAITINGFORPRINTER = 8;
    static final int OVERALLSTATUS_WAITINGTOSTART = 9;
    static final int OVERALLSTATUS_PRINTING = 10;
    static final int OVERALLSTATUS_WAITINGFOROUTQ = 11;
    static final int OVERALLSTATUS_CONNECTPENDING = 12;
    static final int OVERALLSTATUS_POWEREDOFF = 13;
    static final int OVERALLSTATUS_UNUSABLE = 14;
    static final int OVERALLSTATUS_BEINGSERVICED = 15;
    static final int OVERALLSTATUS_UNKNOWN = 999;

    // Device status values
    static final int DEVICESTATUS_VARIEDOFF = 0;
    static final int DEVICESTATUS_AS36DISABLED = 5;
    static final int DEVICESTATUS_VARYOFFPENDING = 10;
    static final int DEVICESTATUS_VARYONPENDING = 20;
    static final int DEVICESTATUS_VARIEDON = 30;
    static final int DEVICESTATUS_CONNECTPENDING = 40;
    static final int DEVICESTATUS_SIGNONDISPLAY = 50;
    static final int DEVICESTATUS_ACTIVE = 60;
    static final int DEVICESTATUS_AS36ENABLED = 62;
    static final int DEVICESTATUS_ACTIVEREADER = 63;
    static final int DEVICESTATUS_ACTIVEWRITER = 66;
    static final int DEVICESTATUS_HELD = 70;
    static final int DEVICESTATUS_POWEREDOFF = 75;
    static final int DEVICESTATUS_RECOVERYPENDING = 80;
    static final int DEVICESTATUS_RECOVERYCANCELLED = 90;
    static final int DEVICESTATUS_FAILED = 100;
    static final int DEVICESTATUS_FAILED_READER = 103;
    static final int DEVICESTATUS_FAILED_WRITER = 106;
    static final int DEVICESTATUS_DIAGNOSTICMODE = 110;
    static final int DEVICESTATUS_DAMAGED = 111;
    static final int DEVICESTATUS_LOCKED = 112;
    static final int DEVICESTATUS_UNKNOWN = 113;

    // Drawer separator values
    static final int DRAWERSEP_FILE = -1;
    static final int DRAWERSEP_DEVD = -2;

    // Server strings
    static final String ENDString_ = "END";
    static final String HELDString_ = "HELD";
    static final String HLDString_ = "HLD";
    static final String JOBQString_ = "JOBQ";
    static final String MSGWString_ = "MSGW";
    static final String RELEASEDString_ = "RELEASED";
    static final String STRString_ = "STR";
    static final String splatAFPDSString_ = "*AFPDS";
    static final String splatALLString_ = "*ALL";
    static final String splatCURRENTString_ = "*CURRENT";
    static final String splatFILEString_ = "*FILE";
    static final String splatFILEENDString_ = "*FILEEND";
    static final String splatFIRSTString_ = "*FIRST";
    static final String splatFORMSString_ = "*FORMS";
    static final String splatIPDSString_ = "*IPDS";
    static final String splatNORDYFString_ = "*NORDYF";
    static final String splatNOString_ = "*NO";
    static final String splatSCSString_ = "*SCS";
    static final String splatSTDString_ = "*STD";
    static final String splatUSERASCIIString_ = "*USERASCII";
    static final String splatWTRString_ = "*WTR";
    static final String splatYESString_ = "*YES";

    // Printer device type strings
    static final String ASCIIString_ = "ASCII";
    static final String AFPDSString_ = "AFPDS";
    static final String IPDSString_ = "IPDS";
    static final String SCSString_ = "SCS";

/**
Constructs a VPrinter object.
**/
//
// This constructor is provided for visual application
// builders that support JavaBeans. It is not
// intended for use by application programmers.
//
    public VPrinter()               //@A2C
    {
        printer_ = new Printer();       //@A10A

        // initialize transient data
        parent_ = null;
        printerOutput_ = new VPrinterOutput(true); // @A7C
        initializeTransient(); // @A9A
    }

/**
Constructs a VPrinter object.

@param printer The printer.
**/
    public VPrinter( Printer printer )               //@A2C
    {
        if (printer == null)
            throw new NullPointerException ("printer");

        printer_ = printer;

        // @A15A
        try
        {
            AS400 system = printer_.getSystem();
            int systemVRM = system.getVRM();
            if (systemVRM < supportedVRM_)
            {
                systemNotSupported_ = true;
            }
        }
        catch (Exception e) {
            errorEventSupport_.fireError (e);
        }

        // initialize transient data
        parent_ = null;
        printerOutput_ = new VPrinterOutput(printer.getSystem(),true); // @A7C        // Set the new outq name

        initializeTransient(); // @A9A
    }

/**
Constructs a VPrinter object.

@param parent The parent.
@param printer The printer.
**/
    public VPrinter( VNode parent, Printer printer )               //@A2C
    {
        if (parent == null)
            throw new NullPointerException ("parent");
        if (printer == null)
            throw new NullPointerException ("printer");

        printer_ = printer;

        // @A15A
        try
        {
            AS400 system = printer_.getSystem();
            int systemVRM = system.getVRM();
            if (systemVRM < supportedVRM_)
            {
                systemNotSupported_ = true;
            }
        }
        catch (Exception e) {
            errorEventSupport_.fireError (e);
        }

        // initialize transient data
        parent_ = parent;
        printerOutput_ = new VPrinterOutput(printer.getSystem(),true); // @A7C
        initializeTransient(); // @A9A
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
Adds a listener to be notified when the value of any
bound property changes.

@param  listener  The listener.
**/
    // Used by VPrinterOutput
    public void addPropertyChangeListener (PropertyChangeListener listener)
    {
        propertyChangeSupport_.addPropertyChangeListener (listener);
    }


/**
Adds a listener to be notified when the value of any
constrained property changes.

@param  listener  The listener.
**/
    // Used by VPrinterOutput
    public void addVetoableChangeListener (VetoableChangeListener listener)
    {
        vetoableChangeSupport_.addVetoableChangeListener (listener);
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
Adds a listener to be notified when work starts and stops
on potentially long-running operations.

@param  listener    The listener.
**/
    public void addWorkingListener (WorkingListener listener)
    {
        workingEventSupport_.addWorkingListener (listener);
    }


/**
Returns the children of the node.

@return         An empty enumeration.
**/
    public Enumeration children ()
    {
        return new Enumeration () {
            public boolean hasMoreElements() { return false;}
            public Object  nextElement()     { return null; }
        };
    }


/**
Returns the list of actions that can be performed.
<ul>
    <li>hold
    <li>release
    <li>start
    <li>stop
    <li>make available
    <li>make unavailable
</ul>

@return The actions.
**/
    public VAction[] getActions ()
    {
        reloadActions ();

        return actions_;
    }

/**
Indiciates if the node allows children.

@return  Always false.
**/
    public boolean getAllowsChildren ()
    {
        return false;
    }

/**
Returns the child node at the specified index.

@param  index   The index.

@return Always null.
**/
    public TreeNode getChildAt (int index)
    {
        return null;
    }

/**
Returns the number of children.

@return Always 0.
**/
    public int getChildCount ()
    {
        return 0;
    }

/**
Returns the default action.

@return Always null.  There is no default action.
**/
    public VAction getDefaultAction ()
    {
        return null;
    }

/**
Returns the child for the details at the specified index.
The details children are the spooled files (VOutput objects)
on the printer.

@param  index   The index.
@return         The child, or null if the index is not
                valid.
**/
    public VObject getDetailsChildAt (int index)
    {
        return printerOutput_.getDetailsChildAt(index);
    }

/**
Returns the number of children for the details.
The details children are the spooled files (VOutput objects)
on the printer.

@return  The number of children for the details.
**/
    public int getDetailsChildCount ()
    {
        return printerOutput_.getDetailsChildCount();
    }


/**
Returns the table column model to use in the details
when representing the children.  This column model
describes the details values for the children.
The details children are the spooled files (VOutput objects)
on the printer.

@return The details column model.
**/
    public TableColumnModel getDetailsColumnModel ()
    {
        return printerOutput_.getDetailsColumnModel();
    }

/**
Returns the index of the specified child for the details.
The details children are the spooled files (VOutput objects)
on the printer.

@param  child   The details child.
@return                The index, or -1 if the child is not found
                       in the details.
**/
    public int getDetailsIndex (VObject child)
    {
        return printerOutput_.getDetailsIndex(child);
    }

/**
Returns the icon.

@param  size    The icon size, either 16 or 32.  If any other
                value is given, then return the default of 16.
@param  open    This parameter has no effect.
@return         The icon.
**/
    public Icon getIcon (int size, boolean open)
    {
        if (size == 32)
            return icon32_;
        else
            return icon16_;
    }

/**
Returns the index of the specified child.

@param  child   The child.
@return         Always -1.
**/
    public int getIndex (TreeNode child)
    {
        return -1;
    }

/**
Returns the parent node.

@return The parent node, or null if there is no parent.
**/
    public TreeNode getParent ()
    {
        return parent_;
    }

/**
Returns the printer associated with this object.

@return The Printer class that is associated with this object.
**/
public Printer getPrinter()                     //@A6A
{
    return printer_;                            //@A6A
}

/**
Returns the attribute of the printer in a String.

@return The attribute of the printer in a String.
**/
String getPrinterAttribute(int attributeID)
{
    Integer attrInteger;
    String  attrString;

    loadStrings();

    if ((systemNotSupported_ == true) &&
        (requestNotSupportedFired_ == true)) // @A16A
    {
        switch (attributeID)
        {
            case PrintObject.ATTR_ALIGNFORMS:
            case PrintObject.ATTR_ALWDRTPRT:
            case PrintObject.ATTR_BTWNCPYSTS:
            case PrintObject.ATTR_BTWNFILESTS:
            case PrintObject.ATTR_CHANGES:
            case PrintObject.ATTR_DEVSTATUS:
            case PrintObject.ATTR_ENDPNDSTS:
            case PrintObject.ATTR_FILESEP:
            case PrintObject.ATTR_FORMTYPE:
            case PrintObject.ATTR_FORMTYPEMSG:
            case PrintObject.ATTR_HELDSTS:
            case PrintObject.ATTR_HOLDPNDSTS:
            case PrintObject.ATTR_JOBUSER:
            case PrintObject.ATTR_ONJOBQSTS:
            case PrintObject.ATTR_OUTQSTS:
            case PrintObject.ATTR_OVERALLSTS:
            case PrintObject.ATTR_PRTDEVTYPE:
            case PrintObject.ATTR_SPOOLFILE:
            case PrintObject.ATTR_SPLFNUM:
            case PrintObject.ATTR_STARTEDBY:
            case PrintObject.ATTR_USERDATA:
            case PrintObject.ATTR_WRTNGSTS:
            case PrintObject.ATTR_WTNGDATASTS:
            case PrintObject.ATTR_WTNGDEVSTS:
            case PrintObject.ATTR_WTNGMSGSTS:
            case PrintObject.ATTR_WTRJOBNAME:
            case PrintObject.ATTR_WTRJOBNUM:
            case PrintObject.ATTR_WTRJOBSTS:
            case PrintObject.ATTR_WTRJOBUSER:
            case PrintObject.ATTR_WTRAUTOEND:
            case PrintObject.ATTR_WTRSTRTD:
                return "";
        }
    }

    try {
        switch (attributeID)
        {
            case PrintObject.ATTR_AFP:
            case PrintObject.ATTR_ALWDRTPRT:
                attrString = printer_.getStringAttribute(attributeID); //@A4C
                if (attrString == null)
                {
                    return "";
                }
                else // @A4A
                {
                    attrString.trim();
                    if (attrString.compareTo(splatYESString_) == 0)
                    {
                        return yesText_;
                    }
                    else if (attrString.compareTo(splatNOString_) == 0)
                    {
                        return noText_;
                    }
                    else
                    {
                        return "";
                    }
                }
            case PrintObject.ATTR_ALIGNFORMS:
                attrString = printer_.getStringAttribute(attributeID); //@A4C
                if (attrString == null)
                {
                    return "";
                }
                else // @A4A
                {
                    attrString.trim();
                    if (attrString.compareTo(splatFILEString_) == 0)
                    {
                        return fileDefaultText_;
                    }
                    else if (attrString.compareTo(splatFIRSTString_) == 0)
                    {
                        return onlyFirstFileText_;
                    }
                    else if (attrString.compareTo(splatWTRString_) == 0)
                    {
                        return automaticText_;
                    }
                    else
                    {
                        return "";
                    }
                }
            case PrintObject.ATTR_CHANGES:
                attrString = printer_.getStringAttribute(attributeID); //@A4C
                if (attrString == null)
                {
                    return "";
                }
                else // @A4A
                {
                    attrString.trim();
                    if (attrString.compareTo(splatNORDYFString_) == 0)
                    {
                        return afterCurFilePrintsText_;
                    }
                    else if (attrString.compareTo(splatFILEENDString_) == 0)
                    {
                        return afterAllFilesPrintText_;
                    }
                    else
                    {
                        return "";
                    }
                }
            case PrintObject.ATTR_DEVSTATUS:
                attrInteger = printer_.getIntegerAttribute(attributeID); // @A5C
                if (attrInteger == null)  // @A5A
                {
                    return "";
                }
                else
                {
                    switch (attrInteger.intValue())
                    {
                        case DEVICESTATUS_VARIEDOFF:
                            return unavailableText_;
                        case DEVICESTATUS_AS36DISABLED: // Op Nav doesn't show
                            return as36DisabledText_;
                        case DEVICESTATUS_VARYOFFPENDING:
                            return unavailablePendingText_;
                        case DEVICESTATUS_VARYONPENDING:
                            return availablePendingText_;
                        case DEVICESTATUS_VARIEDON:
                            return availableText_;
                        case DEVICESTATUS_CONNECTPENDING:
                            return connectPendingText_;
                        case DEVICESTATUS_SIGNONDISPLAY: // Op Nav doesn't show
                            return signonDisplayText_;
                        case DEVICESTATUS_ACTIVE:
                            return activeText_;
                        case DEVICESTATUS_AS36ENABLED: // Op Nav doesn't show
                            return as36EnabledText_;
                        case DEVICESTATUS_ACTIVEREADER: // Op Nav doesn't show
                            return activeReaderText_;
                        case DEVICESTATUS_ACTIVEWRITER:
                            return activeWriterText_;
                        case DEVICESTATUS_HELD:
                            return heldText_;
                        case DEVICESTATUS_POWEREDOFF:
                            return poweredOffText_;
                        case DEVICESTATUS_RECOVERYPENDING:
                            return recoveryPendingText_;
                        case DEVICESTATUS_RECOVERYCANCELLED:
                            return recoveryCancelledText_;
                        case DEVICESTATUS_FAILED:
                            return unusableText_;
                        case DEVICESTATUS_FAILED_READER: // Op Nav doesn't show
                            return failedReaderText_;
                        case DEVICESTATUS_FAILED_WRITER:
                            return failedWriterText_;
                        case DEVICESTATUS_DIAGNOSTICMODE:
                            return beingServicedText_;
                        case DEVICESTATUS_DAMAGED:
                            return damagedText_;
                        case DEVICESTATUS_LOCKED:
                            return lockedText_;
                        case DEVICESTATUS_UNKNOWN:
                            return unknownText_;
                        default:
                            return "";
                    }
                }
            case PrintObject.ATTR_DRWRSEP:
                attrInteger = printer_.getIntegerAttribute(attributeID); // @A5C
                if (attrInteger == null) // @A5A
                {
                    return "";
                }
                else
                {
                    switch (attrInteger.intValue())
                    {
                        case DRAWERSEP_FILE:
                            return fileDefaultText_;
                        case DRAWERSEP_DEVD:
                            return deviceDefaultText_;
                        case 1:
                        case 2:
                        case 3:
                            return attrInteger.toString();
                        default:
                            return "";
                    }
                }
            case PrintObject.ATTR_FILESEP:
                attrInteger = printer_.getIntegerAttribute(attributeID); // @A5C
                if (attrInteger == null) // @A5A
                {
                    return "";
                }
                else if (attrInteger.intValue() == -1)
                {
                    return fileDefaultText_;
                }
                else
                {
                    return attrInteger.toString();
                }
            case PrintObject.ATTR_FORMTYPE:
                attrString = printer_.getStringAttribute(attributeID); //@A4C
                if (attrString == null)
                {
                    return "";
                }
                else // @A4A
                {
                    attrString.trim();
                    if (attrString.compareTo(splatALLString_) == 0)
                    {
                        return allText_;
                    }
                    else if (attrString.compareTo(splatFORMSString_) == 0)
                    {
                        return formsText_;
                    }
                    else if (attrString.compareTo(splatSTDString_) == 0)
                    {
                        return standardText_;
                    }
                    else
                    {
                        return "";
                    }
                }
            case PrintObject.ATTR_OUTQSTS:
                attrString = printer_.getStringAttribute(attributeID); //@A4C
                if (attrString == null)
                {
                    return "";
                }
                else // @A4A
                {
                    attrString.trim();
                    if (attrString.compareTo(RELEASEDString_) == 0)
                    {
                        return releasedText_;
                    }
                    else if (attrString.compareTo(HELDString_) == 0)
                    {
                        return heldText_;
                    }
                    else
                    {
                        return "";
                    }
                }
            case PrintObject.ATTR_OVERALLSTS:
                attrInteger = printer_.getIntegerAttribute(attributeID); // @A5C
                if (attrInteger == null) // @A5A
                {
                    return "";
                }
                else
                {
                    switch (attrInteger.intValue())
                    {
                        case OVERALLSTATUS_UNAVAILABLE:
                            return unavailableText_;
                        case OVERALLSTATUS_POWEREDOFFNOTAVAILALBE:
                            return poweredOffNotAvailText_;
                        case OVERALLSTATUS_STOPPED:
                            return stoppedText_;
                        case OVERALLSTATUS_MESSAGEWAITING:
                            return messageWaitingText_;
                        case OVERALLSTATUS_HELD:
                            return heldText_;
                        case OVERALLSTATUS_STOPPENDING:
                            return stopPendingText_;
                        case OVERALLSTATUS_HOLDPENDING:
                            return holdPendingText_;
                        case OVERALLSTATUS_WAITINGFORPRINTER:
                            return waitingForPrinterText_;
                        case OVERALLSTATUS_WAITINGTOSTART:
                            return waitingOnJobQueueQSPL_;
                        case OVERALLSTATUS_PRINTING:
                            return printingText_;
                        case OVERALLSTATUS_WAITINGFOROUTQ:
                            return waitingForOutQText_;
                        case OVERALLSTATUS_CONNECTPENDING:
                            return connectPendingText_;
                        case OVERALLSTATUS_POWEREDOFF:
                            return poweredOffText_;
                        case OVERALLSTATUS_UNUSABLE:
                            return unusableText_;
                        case OVERALLSTATUS_BEINGSERVICED:
                            return beingServicedText_;
                        case OVERALLSTATUS_UNKNOWN:
                            return unknownText_;
                        default:
                            return "";
                    }
                }
            case PrintObject.ATTR_DEVTYPE:
            case PrintObject.ATTR_PRTDEVTYPE:
                attrString = printer_.getStringAttribute(attributeID); //@A4C
                if (attrString == null)
                {
                    return "";
                }
                else // @A4A
                {
                    attrString.trim();
                    if (attrString.compareTo(splatAFPDSString_) == 0)
                    {
                        return AFPDSString_;
                    }
                    else if (attrString.compareTo(splatIPDSString_) == 0)
                    {
                        return IPDSString_;
                    }
                    else if (attrString.compareTo(splatSCSString_) == 0)
                    {
                        return SCSString_;
                    }
                    else if (attrString.compareTo(splatUSERASCIIString_) == 0)
                    {
                        return ASCIIString_;
                    }
                    else
                    {
                        return attrString;
                    }
                }
            case PrintObject.ATTR_WTRAUTOEND:
                attrString = printer_.getStringAttribute(attributeID); //@A4C
                if (attrString == null)
                {
                    return "";
                }
                else // @A4A
                {
                    attrString.trim();
                    if (attrString.compareTo(splatYESString_) == 0)
                    {
                        return yesText_;
                    }
                    else if (attrString.compareTo(splatNOString_) == 0)
                    {
                        return noText_;
                    }
                    else if (attrString.compareTo(splatNORDYFString_) == 0) // @A14C
                    {
                        return afterCurFilePrintsText_;  // ???
                    }
                    else if (attrString.compareTo(splatFILEENDString_) == 0)
                    {
                        return afterAllFilesPrintText_; // ???
                    }
                    else
                    {
                        return "";
                    }
                }
            case PrintObject.ATTR_WTRJOBSTS:
                attrString = printer_.getStringAttribute(attributeID); //@A4C
                if (attrString == null)
                {
                    return "";
                }
                else // @A4A
                {
                    attrString.trim();
                    if (attrString.compareTo(ENDString_) == 0)
                    {
                        return endedText_;
                    }
                    else if (attrString.compareTo(HLDString_) == 0)
                    {
                        return heldText_;
                    }
                    else if (attrString.compareTo(JOBQString_) == 0)
                    {
                        return onJobQueueText_;
                    }
                    else if (attrString.compareTo(MSGWString_) == 0)
                    {
                        return waitingOnMessageText_;
                    }
                    else if (attrString.compareTo(STRString_) == 0)
                    {
                        return startedText_;
                    }
                    else
                    {
                        return "";
                    }
                }
            default:
                // @A4A
                attrString = printer_.getStringAttribute(attributeID);
                if (attrString == null)
                {
                    return "";
                }
                else
                {
                    return attrString.trim();
                }
        }
    }
    catch (RequestNotSupportedException e) { // @A15A
        if (Trace.isTraceOn()) {
            Trace.log(Trace.ERROR,"VPrinter::getPrinterDeviceStatus failed getting attribute");
        }
        if (systemNotSupported_ == true)
        {
            if (requestNotSupportedFired_ == false)
            {
                errorEventSupport_.fireError (e);
                requestNotSupportedFired_ = true;
            }
        }
        else
        {
            errorEventSupport_.fireError (e);
        }
    }
    catch (Exception e) {
        if (Trace.isTraceOn()) {
            Trace.log(Trace.ERROR,"VPrinter::getPrinterAttribute failed getting attribute ",attributeID);
        }
        errorEventSupport_.fireError (e);
    }
    return "";
}

int getPrinterDeviceStatus()
{
    Integer attrInteger;

    if ((systemNotSupported_ == true) &&
        (requestNotSupportedFired_ == true)) // @A16A
    {
        return DEVICESTATUS_UNKNOWN;
    }

    try {
        attrInteger = printer_.getIntegerAttribute(PrintObject.ATTR_DEVSTATUS); // @A5C
        if (attrInteger != null) // @A5A
        {
            return attrInteger.intValue();
        }
    }
    catch (RequestNotSupportedException e) { // @A15A
        if (Trace.isTraceOn()) {
            Trace.log(Trace.ERROR,"VPrinter::getPrinterDeviceStatus failed getting attribute");
        }
        if (systemNotSupported_ == true)
        {
            if (requestNotSupportedFired_ == false)
            {
                errorEventSupport_.fireError (e);
                requestNotSupportedFired_ = true;
            }
        }
        else
        {
            errorEventSupport_.fireError (e);
        }
    }
    catch (Exception e) {
        errorEventSupport_.fireError (e);
    }
    return DEVICESTATUS_UNKNOWN;
}

/**
Returns the output queue of the printer in a String.

@return The output queue of the printer in a String.
**/
String getPrinterOutputQueue()
{
    String  attrString;

    if ((systemNotSupported_ == true) &&
        (requestNotSupportedFired_ == true)) // @A16A
    {
        return "";
    }

    try {
        attrString = printer_.getStringAttribute(PrintObject.ATTR_OUTPUT_QUEUE);
        if (attrString != null) // @A7A
        {
            QSYSObjectPathName outQPath = new QSYSObjectPathName(attrString);
            if (outQPath != null) // @A7A
            {
                attrString = outQPath.getObjectName(); // @A4A
                if (attrString != null) // @A7A
                {
                    return attrString.trim();
                }
            }
        }
    }
    catch (RequestNotSupportedException e) { // @A15A
        if (Trace.isTraceOn()) {
            Trace.log(Trace.ERROR,"VPrinter::getPrinterOutputQueue failed getting attribute");
        }
        if (systemNotSupported_ == true)
        {
            if (requestNotSupportedFired_ == false)
            {
                errorEventSupport_.fireError (e);
                requestNotSupportedFired_ = true;
            }
        }
        else
        {
            errorEventSupport_.fireError (e);
        }
    }
    catch (Exception e) {
        errorEventSupport_.fireError (e);
    }
    return "";
}

/**
Returns the output queue library of the printer in a String.

@return The output queue library of the printer in a String.
**/
String getPrinterOutputQueueLib()
{
    String attrString;

    if ((systemNotSupported_ == true) &&
        (requestNotSupportedFired_ == true)) // @A16A
    {
        return "";
    }

    try {
        attrString = printer_.getStringAttribute(PrintObject.ATTR_OUTPUT_QUEUE);
        if (attrString != null) // @A7A
        {
            QSYSObjectPathName outQPath = new QSYSObjectPathName(attrString);
            if (outQPath != null) // @A7A
            {
                attrString = outQPath.getLibraryName(); // @A4A
                if (attrString != null) // @A7A
                {
                    return attrString.trim();
                }
            }
        }
    }
    catch (RequestNotSupportedException e) { // @A15A
        if (Trace.isTraceOn()) {
            Trace.log(Trace.ERROR,"VPrinter::getPrinterOutputQueueLib failed getting attribute ");
        }
        if (systemNotSupported_ == true)
        {
            if (requestNotSupportedFired_ == false)
            {
                errorEventSupport_.fireError (e);
                requestNotSupportedFired_ = true;
            }
        }
        else
        {
            errorEventSupport_.fireError (e);
        }
    }
    catch (Exception e) {
        errorEventSupport_.fireError (e);
    }
    return "";
}

int getPrinterOverallStatus()
{
    Integer attrInteger;

    if ((systemNotSupported_ == true) &&
        (requestNotSupportedFired_ == true)) // @A16A
    {
        return OVERALLSTATUS_UNKNOWN;
    }

    try {
        attrInteger = printer_.getIntegerAttribute(PrintObject.ATTR_OVERALLSTS); // @A5C
        if (attrInteger != null) // @A5C
        {
            return attrInteger.intValue();
        }
    }
    catch (RequestNotSupportedException e) { // @A15A
        if (Trace.isTraceOn()) {
            Trace.log(Trace.ERROR,"VPrinter::getPrinterOverallStatus failed getting attribute ");
        }
        if (systemNotSupported_ == true)
        {
            if (requestNotSupportedFired_ == false)
            {
                errorEventSupport_.fireError (e);
                requestNotSupportedFired_ = true;
            }
        }
        else
        {
            errorEventSupport_.fireError (e);
        }
    }
    catch (Exception e) {
        errorEventSupport_.fireError (e);
    }
    return OVERALLSTATUS_UNKNOWN;
}

/**
Returns the properties pane.

@return The properties pane.
**/
    public VPropertiesPane getPropertiesPane ()
    {
        return propertiesPane_;
    }


/**
Returns a property value.

@param      propertyIdentifier  The property identifier.  The choices are
                                <ul>
                                  <li>PRINTER_PROPERTY
                                  <li>STATUS_PROPERTY
                                  <li>DESCRIPTION_PROPERTY
                                  <li>OUTPUTQUEUE_PROPERTY
                                </ul>
@return                         The property value, or null if the
                                property identifier is not recognized.
**/

    public Object getPropertyValue (Object propertyIdentifier)
    {
        try {
            if (propertyIdentifier == PRINTER_PROPERTY)
                return this;

            else if (propertyIdentifier == STATUS_PROPERTY)
            {
                return getPrinterAttribute(PrintObject.ATTR_OVERALLSTS);
            }

            else if (propertyIdentifier == DESCRIPTION_PROPERTY)
            {
                return getPrinterAttribute(PrintObject.ATTR_DESCRIPTION);
            }
            else if (propertyIdentifier == OUTPUTQUEUE_PROPERTY)
            {
                return getPrinterOutputQueue();
            }
            else
            {
                return null;                //@A10C
            }
         }
        catch (Exception e) {
            errorEventSupport_.fireError (e);
        }
        return null;                        //@A10C

    }

/**
Returns the text.  This is the name of the printer.

@return The text.
**/
    public String getText ()
    {
        return printer_.getName();
    }

/**
Indicates if the node is a leaf.

@return  Always true.
**/
    public boolean isLeaf ()
    {
        return true;
    }

/**
Indicates if the details children are sortable.

@return Always true.
**/
    public boolean isSortable ()
    {
        return true;
    }

/**
Initializes the transient data.
**/
    private void initializeTransient () // @A9A
    {
        // Initialize the event support.
        errorEventSupport_      = new ErrorEventSupport (this);
        objectEventSupport_     = new VObjectEventSupport (this);
        propertyChangeSupport_  = new PropertyChangeSupport (this);
        vetoableChangeSupport_  = new VetoableChangeSupport (this);
        workingEventSupport_    = new WorkingEventSupport (this);
        objectListener_         = new VObjectListener_();  //@A8A

        printerOutput_.addErrorListener (errorEventSupport_);              //@A6A
        printerOutput_.addVObjectListener (objectEventSupport_);           //@A6A
        printerOutput_.addWorkingListener (workingEventSupport_);          //@A6A
        printerOutput_.addPropertyChangeListener (propertyChangeSupport_); //@A6A
        printerOutput_.addVetoableChangeListener (vetoableChangeSupport_); //@A6A

        actions_                    = new VAction[LAST_ACTION + 1];
        actions_[HOLD_ACTION]       = new PrinterHoldAction (this, printer_);
        actions_[RELEASE_ACTION]    = new PrinterReleaseAction (this, printer_);
        actions_[START_ACTION]      = new PrinterStartAction (this, printer_);
        actions_[STOP_ACTION]       = new PrinterStopAction (this, printer_);
        actions_[AVAILABLE_ACTION]  = new PrinterAvailableAction (this, printer_);
        actions_[UNAVAILABLE_ACTION]= new PrinterUnavailableAction (this, printer_);

        // Listen to the actions
        for (int i = 0; i< actions_.length; ++i)
        {
            actions_[i].addErrorListener (errorEventSupport_);
            actions_[i].addVObjectListener (objectEventSupport_);
            actions_[i].addVObjectListener (objectListener_);                   //@A8A
            actions_[i].addWorkingListener (workingEventSupport_);
        } // end for

        // set printerOuput filters so that properties pane appears correct @A13A
        try
        {
            printerOutput_.setUserFilter(splatCURRENTString_);
            printerOutput_.setFormTypeFilter(splatALLString_);
            printerOutput_.setUserDataFilter(splatALLString_);

            if ((printer_ != null) &&
                (systemNotSupported_ == false)) // @A16A
            {
                String outqPath = printer_.getStringAttribute(PrintObject.ATTR_OUTPUT_QUEUE);
                if (outqPath != null)
                {
                    outqPath = outqPath.toUpperCase();
                    printerOutput_.setQueueFilter(outqPath);
                }
            }
        }
        catch (RequestNotSupportedException e) { // @A15A
            if (Trace.isTraceOn()) {
                Trace.log(Trace.ERROR,"VPrinter::getPrinterOutputQueueLib failed getting attribute ");
            }
            if (systemNotSupported_ == true)
            {
                if (requestNotSupportedFired_ == false)
                {
                    errorEventSupport_.fireError (e);
                }
            }
            else
            {
                errorEventSupport_.fireError (e);
            }
        }
        catch (Exception e)
        {
            errorEventSupport_.fireError (e);
        }

        // Initialize the properties pane.
        propertiesPane_ = new PrinterPropertiesPane(this,printerOutput_);
        propertiesPane_.addErrorListener (errorEventSupport_);              //@A3A
        propertiesPane_.addVObjectListener (objectEventSupport_);           //@A3A @A6D
        propertiesPane_.addWorkingListener (workingEventSupport_);          //@A3A @A6D

    }

/**
Loads information about the object from the server.  A printer must be
specified either on construction or from a call to setPrinter() inorder
to get information about the object from the server.
**/
    public void load ()
    {
        try
        {
            // First check to see if the queue filter has been set.  If not,
            // set it.
            if(printerOutput_.getQueueFilter().equals(""))
            {
                String outqPath = printer_.getStringAttribute(PrintObject.ATTR_OUTPUT_QUEUE);  //@A6A
                if (outqPath != null) // @A7A
                {
                    outqPath = outqPath.toUpperCase();                                             //@A6C
                    printerOutput_.setQueueFilter(outqPath);                                       //@A6C
                }
            }

            // if queue filter not set don't load printer output
            if(!(printerOutput_.getQueueFilter().equals(""))) // @A7A @A8C
            {
                // just update the printer attributes
                printer_.update();
                printerOutput_.load();
            }
        }
        catch (Exception e)
        {
            errorEventSupport_.fireError (e);
        } // end catch block
    }

/* Loads the MRI strings once for the object during the getComponent() method
*/
    private synchronized void loadStrings()
    {
        if (activeReaderText_ == null)
        {
            try {
                activeReaderText_           = ResourceLoader.getPrintText("ACTIVE_READER");
                activeText_                 = ResourceLoader.getPrintText("ACTIVE");
                activeWriterText_           = ResourceLoader.getPrintText("ACTIVE_WRITER");
                afterAllFilesPrintText_     = ResourceLoader.getPrintText("AFTER_ALL_FILES_PRINT");
                afterCurFilePrintsText_     = ResourceLoader.getPrintText("AFTER_CURRENT_FILE_PRINTS");
                allText_                    = ResourceLoader.getPrintText("ALL");
                as36DisabledText_           = ResourceLoader.getPrintText("AS36_DISABLED");
                as36EnabledText_            = ResourceLoader.getPrintText("AS36_ENABLED");
                automaticText_              = ResourceLoader.getPrintText("AUTOMATIC");
                availableText_              = ResourceLoader.getPrintText("AVAILABLE");
                availablePendingText_       = ResourceLoader.getPrintText("AVAILABLE_PENDING");
                beingServicedText_          = ResourceLoader.getPrintText("BEING_SERVICED");
                connectPendingText_         = ResourceLoader.getPrintText("CONNECT_PENDING");
                damagedText_                = ResourceLoader.getPrintText("DAMAGED");
                deviceDefaultText_          = ResourceLoader.getPrintText("DEVICE_DEFAULT");
                diagnosticModeText_         = ResourceLoader.getPrintText("DIAGNOSTIC_MODE");
                endedText_                  = ResourceLoader.getPrintText("ENDED");
                failedReaderText_           = ResourceLoader.getPrintText("FAILED_READER");
                failedText_                 = ResourceLoader.getPrintText("FAILED");
                failedWriterText_           = ResourceLoader.getPrintText("FAILED_WRITER");
                fileDefaultText_            = ResourceLoader.getPrintText("FILE_DEFAULT");
                formsText_                  = ResourceLoader.getPrintText("FORMS");
                heldText_                   = ResourceLoader.getPrintText("HELD");
                holdPendingText_            = ResourceLoader.getPrintText("HOLD_PENDING");
                lockedText_                 = ResourceLoader.getPrintText("LOCKED");
                messageWaitingText_         = ResourceLoader.getPrintText("MESSAGE_WAITING");
                noText_                     = ResourceLoader.getPrintText("NO");
                onJobQueueText_             = ResourceLoader.getPrintText("ON_JOB_QUEUE");
                onlyFirstFileText_          = ResourceLoader.getPrintText("FILE_FORM_ALIGNMENT");
                poweredOffText_             = ResourceLoader.getPrintText("POWERED_OFF");
                poweredOffNotAvailText_     = ResourceLoader.getPrintText("POWERED_OFF_NOT_AVAILABLE");
                printingText_               = ResourceLoader.getPrintText("PRINTING");
                recoveryCancelledText_      = ResourceLoader.getPrintText("RECOVERY_CANCELLED");
                recoveryPendingText_        = ResourceLoader.getPrintText("RECOVERY_PENDING");
                releasedText_               = ResourceLoader.getPrintText("RELEASED");
                signonDisplayText_          = ResourceLoader.getPrintText("SIGNON_DISPLAY");
                standardText_               = ResourceLoader.getPrintText("STANDARD");
                startedText_                = ResourceLoader.getPrintText("STARTED");
                stopPendingText_            = ResourceLoader.getPrintText("STOP_PENDING");
                stoppedText_                = ResourceLoader.getPrintText("STOPPED");
                unavailableText_            = ResourceLoader.getPrintText("UNAVAILABLE");
                unavailablePendingText_     = ResourceLoader.getPrintText("UNAVAILABLE_PENDING");
                unknownText_                = ResourceLoader.getPrintText("UNKNOWN");
                unusableText_               = ResourceLoader.getPrintText("UNUSABLE");
                variedOffText_              = ResourceLoader.getPrintText("VARIED_OFF");
                variedOnText_               = ResourceLoader.getPrintText("VARIED_ON");
                varyOffPendingText_         = ResourceLoader.getPrintText("VARY_OFF_PENDING");
                varyOnPendingText_          = ResourceLoader.getPrintText("VARY_ON_PENDING");
                waitingForOutQText_         = ResourceLoader.getPrintText("WAITING_FOR_OUTQ");
                waitingForPrinterText_      = ResourceLoader.getPrintText("WAITING_FOR_PRINTER");
                waitingOnMessageText_       = ResourceLoader.getPrintText("WAITING_ON_MESSAGE");
                waitingOnJobQueueQSPL_      = ResourceLoader.getPrintText("WAITING_ON_JOB_QUEUE_QSPL");
                yesText_                    = ResourceLoader.getPrintText("YES");
            }
            catch (Exception e) {
                errorEventSupport_.fireError (e);
            }
        }
    }


/**
Reloads the actions.
<ul>
  <li> Hold
  <li> Release
  <li> Start
  <li> Stop
  <li> Make Available
  <li> Make Unavailable
  <li> Properties - handled by parent for us
 </ul>
**/

/* RDS- These should be added sometime
    <li> Reply
    <li> Restart
*/

    private void reloadActions ()
    {
        String statusStr;
        int statusInt;

        // @A15A
        if (systemNotSupported_ == true)
        {
            // The device is not varied on so only MAKE AVAILABLE is enabled
            actions_[HOLD_ACTION].setEnabled(false);
            actions_[RELEASE_ACTION].setEnabled(false);
            actions_[START_ACTION].setEnabled(false);
            actions_[STOP_ACTION].setEnabled(false);
            actions_[AVAILABLE_ACTION].setEnabled(false);
            actions_[UNAVAILABLE_ACTION].setEnabled(false);
            return;
        }

        // Now we need to figure out which ones are active
        try
        {
            // Get device status
            statusInt = getPrinterDeviceStatus();

            if((statusInt == DEVICESTATUS_VARIEDON) ||
                (statusInt == DEVICESTATUS_ACTIVE) ||
                (statusInt == DEVICESTATUS_ACTIVEWRITER) ||
                (statusInt == DEVICESTATUS_HELD))
            {
                // The device is varied on
                actions_[AVAILABLE_ACTION].setEnabled(false);
                actions_[UNAVAILABLE_ACTION].setEnabled(true);

                // Check to see if a writer is started
                // Get the writer name.  Use this call because it's faster.
                statusStr = printer_.getStringAttribute(PrintObject.ATTR_WTRJOBSTS); // @A4C

                // Start is active only if writer does not exist
                // Stop is active if writer exists
                if(statusStr == null) // @A4C
                {
                    // The writer is not started so only START is enabled
                    actions_[HOLD_ACTION].setEnabled(false);
                    actions_[RELEASE_ACTION].setEnabled(false);
                    actions_[START_ACTION].setEnabled(true);
                    actions_[STOP_ACTION].setEnabled(false);
                }
                else
                {
                    statusStr.trim(); // @A4A
                    if (statusStr.equals("END")) // @A4A
                    {
                        // The writer is not started so only START is enabled
                        actions_[HOLD_ACTION].setEnabled(false);
                        actions_[RELEASE_ACTION].setEnabled(false);
                        actions_[START_ACTION].setEnabled(true);
                        actions_[STOP_ACTION].setEnabled(false);
                    }
                    else // @A4A
                    {
                        // There is a writer so STOP is enabled
                        actions_[START_ACTION].setEnabled(false);
                        actions_[STOP_ACTION].setEnabled(true);

                        // Is the writer held?
                        if(statusStr.equals("HLD"))
                        {
                            // The writer is held so RELEASE is enabled
                            actions_[HOLD_ACTION].setEnabled(false);
                            actions_[RELEASE_ACTION].setEnabled(true);
                        }
                        else
                        {
                            // HOLD is enabled
                            actions_[HOLD_ACTION].setEnabled(true);
                            actions_[RELEASE_ACTION].setEnabled(false);
                        }
                    }
                }
            }
            else
            {
                // The device is not varied on so only MAKE AVAILABLE is enabled
                actions_[HOLD_ACTION].setEnabled(false);
                actions_[RELEASE_ACTION].setEnabled(false);
                actions_[START_ACTION].setEnabled(false);
                actions_[STOP_ACTION].setEnabled(false);
                actions_[AVAILABLE_ACTION].setEnabled(true);
                actions_[UNAVAILABLE_ACTION].setEnabled(false);
            }

            // Reply is active only if writer message exists (ADD LATER)
            // Restart is active only if writer is currently writing
            // RDS- Add these later

        }   // end try block
        catch (Exception e)
        {
            errorEventSupport_.fireError (e);
        } // end catch block
    }

/**
Removes an error listener.

@param  listener    The listener.
**/
    public void removeErrorListener (ErrorListener listener)
    {
        errorEventSupport_.removeErrorListener (listener);
    }

/**
Removes a property change listener.

@param  listener  The listener.
**/
    // Used by VPrinterOutput
    public void removePropertyChangeListener (PropertyChangeListener listener)
    {
        propertyChangeSupport_.removePropertyChangeListener (listener);
    }


/**
Removes a vetoable change listener.

@param  listener  The listener.
**/
    // Used by VPrinterOutput
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        vetoableChangeSupport_.removeVetoableChangeListener (listener);    //@A11C
    }


/**
Removes a VObjectListener.

@param  listener    The listener.
**/
    public void removeVObjectListener (VObjectListener listener)
    {
        objectEventSupport_.removeVObjectListener (listener);
    }


/**
Removes a working listener.

@param  listener    The listener.
**/
    public void removeWorkingListener (WorkingListener listener)
    {
        workingEventSupport_.removeWorkingListener (listener);
    }

/**
Sets the printer.  A call to load() must be done after calling this
funtion inorder to update the details children.

@param   printer     The printer.

@exception PropertyVetoException If the change is vetoed.
**/
    public void setPrinter(Printer printer)
            throws PropertyVetoException
    {
        try
        {
            printer_ = printer;

            // @A15A
            AS400 system = printer_.getSystem();
            int systemVRM = system.getVRM();
            if (systemVRM < supportedVRM_)
            {
                systemNotSupported_ = true;
            }

            if(printerOutput_ == null)
                printerOutput_ = new VPrinterOutput(printer.getSystem());
            else
                printerOutput_.setSystem(printer.getSystem());

            // Set the new outq name filter
            String outqPath = printer_.getStringAttribute(PrintObject.ATTR_OUTPUT_QUEUE);  //@A12A
            if (outqPath != null)                                                          // @A12A
            {
                outqPath = outqPath.toUpperCase();                                         //@A12a
                printerOutput_.setQueueFilter(outqPath);                                   //@A12a
            }
            // no error reported for outqPath == null?
        }
        catch (RequestNotSupportedException e) { // @A16A
            if (Trace.isTraceOn()) {
                Trace.log(Trace.ERROR,"VPrinter::setPrinter failed getting attribute");
            }
            errorEventSupport_.fireError (e);
            requestNotSupportedFired_ = true;
        }
        catch (Exception e)
        {
            errorEventSupport_.fireError (e);
        } // end catch block

    }

/**
Sorts the children for the details.

@param  propertyIdentifiers The property identifiers.
@param  orders              The sort orders for each property
                            identifier, true for ascending order,
                            false for descending order.
**/
public void sortDetailsChildren (Object[] propertyIdentifiers, boolean[] orders)
{
    printerOutput_.sortDetailsChildren(propertyIdentifiers, orders);
}

/**
Returns the string representation.  This is the name of the printer.

@return The string representation.
**/
    public String toString ()
    {
        return printer_.getName();
    }

/**
Listens for events from the actions and adjusts the actions accordingly.
**/
    private class VObjectListener_                                          //@A8A
    implements VObjectListener, Serializable
    {

        public void objectChanged (VObjectEvent event)
        {
            // need to reload the actions to reflect the change
            reloadActions();
        }

        public void objectCreated (VObjectEvent event)
        {
            // Nothing here.
        }

        public void objectDeleted (VObjectEvent event)
        {
            // Nothing here.
        }
    }
}

