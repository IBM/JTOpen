///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: Printer.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyVetoException;
import java.io.IOException;

/**
 * The  Printer class represents a printer.
 * An instance of this class can be used to manipulate an individual
 * printer.
 *
 * See <a href="doc-files/PrinterAttrs.html">Printer Attributes</a> for
 * valid attributes.
 *
 **/

public class Printer extends PrintObject
implements java.io.Serializable
{
    static final long serialVersionUID = 4L;

    private static boolean      fAttrIDsToRtvBuilt_ = false;
    private static final String NAME = "name";


    // constructor used internally (not externalized since it takes
    // an ID code point
    Printer(AS400 system, NPCPIDPrinter id, NPCPAttribute attrs)
    {
        super(system, id, attrs, NPConstants.PRINTER_DEVICE);
    }



    /**
     * Constructs a Printer object. The system and the
     * name of the printer must be set later. This constructor
     * is provided for visual application builders that support
     * JavaBeans. It is not intended for use by application
     * programmers.
     *
     * @see PrintObject#setSystem
     * @see #setName
     **/
    public Printer()
    {
        super(null, null, NPConstants.PRINTER_DEVICE);

        // Because of this constructor we will need to check the
        // system and printer name before trying to use them.
    }



    /**
     * Constructs a Printer object. It uses the specified system name and
     * the printer name that identifies it on that system.
     *
     * @param system The system on which this printer device exists.
     * @param printerName The name of the printer.  It cannot be greater
     *                    than 10 characters or less than 1 character
     *                    in length.
     *
     **/
    public Printer(AS400 system,
                   String printerName)
    {
        super(system, new NPCPIDPrinter(printerName), null, NPConstants.PRINTER_DEVICE);

        // base class constructor checks for a null system.
        checkPrinterName(printerName);
    }



    // check the printer name to see if valid
    void checkPrinterName( String printerName )
    {
        if (printerName == null) {
            Trace.log(Trace.ERROR, "Parameter 'printerName' is null.");
            throw new NullPointerException("printerName");
        }

        if ((printerName.length() > 10) || (printerName.length() < 1)) {
            Trace.log(Trace.ERROR, "Parameter 'printerName' is greater than 10 or less than 1 characters in length " + printerName);
            throw new ExtendedIllegalArgumentException(
              "printerName("+printerName+")",
              ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
    }


    // Check the run time state
    void checkRunTimeState()
    {
        // check whatever the base class needs to check
        super.checkRunTimeState();

        // Printers need to additionally check the printer name.
        // In this context, getIDCodePoint() returns the printer name. 
        if( getIDCodePoint() == null )
        {
            Trace.log(Trace.ERROR, "Printer name has not been set.");
            throw new ExtendedIllegalStateException(
              "name", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }
    }



    // A4A - Added chooseImpl() method
    /**
     * Chooses the implementation
     **/
    void chooseImpl()
    throws IOException, AS400SecurityException
    {
        AS400 system = getSystem();
        if (system == null) {
            Trace.log( Trace.ERROR, "Attempt to use Printer before setting system." );
            throw new ExtendedIllegalStateException("system",
                                    ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        impl_ = (PrinterImpl) system.loadImpl2("com.ibm.as400.access.PrinterImplRemote",
                                               "com.ibm.as400.access.PrinterImplProxy");
        super.setImpl();
    }



    /**
     * Returns the name of the printer.
     *
     * @return The name of the printer.  If name is not set, "" is returned.
     **/
    public String getName()
    {
        NPCPID IDCodePoint = getIDCodePoint();

        if( IDCodePoint == null ) {
            return EMPTY_STRING; // ""
        } else {
            return IDCodePoint.getStringValue(ATTR_PRINTER);
        }
    }


    /**
     * Sets one or more attributes of the object.  See
     * <a href="doc-files/PrinterAttrs.html">Printer Attributes</a> for
     * a list of valid attributes that can be changed.
     * <br>Note that only the following attributes can be changed:
     * <ul>
     * <li>{@link PrintObject#ATTR_CHANGES ATTR_CHANGES}
     * <li>{@link PrintObject#ATTR_DRWRSEP ATTR_DRWRSEP}
     * <li>{@link PrintObject#ATTR_FILESEP ATTR_FILESEP}
     * <li>{@link PrintObject#ATTR_FORMTYPE ATTR_FORMTYPE}
     * <li>{@link PrintObject#ATTR_OUTPUT_QUEUE ATTR_OUTPUT_QUEUE}
     * <li>{@link PrintObject#ATTR_DESCRIPTION ATTR_DESCRIPTION}
     * </ul>
     * Any other attributes will be ignored by this method.
     *
     * @param attributes A print parameter list that contains the
     *  attributes to be changed.
     *
     * @exception AS400Exception If the system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the system.
     * @exception InterruptedException If this thread is interrupted.
     **/
    public void setAttributes(PrintParameterList attributes)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException
    {
        if (attributes == null) {
	        Trace.log(Trace.ERROR, "Parameter 'attributes' is null.");
	        throw new NullPointerException("attributes");
	    }

        checkRunTimeState();

        if (impl_ == null) chooseImpl();

        ((PrinterImpl) impl_).setAttributes(attributes);
   
        // propagate any changes to attrs
        attrs = impl_.getAttrValue();
    }



    /**
     * Sets the name of the printer.
     *
     * @param name The name of the printer. It cannot be greater
     * than 10 characters.
     *
     * @exception PropertyVetoException If the change is vetoed.
     *
     **/
    public void setName(String name)
      throws PropertyVetoException
    {
        checkPrinterName(name);

        String oldName = getName();

        // Tell any vetoers about the change. If anyone objects
        // we let the PropertyVetoException propagate back to
        // our caller.
        vetos.fireVetoableChange(NAME, oldName, name );

        // No one vetoed, make the change.
        setIDCodePoint(new NPCPIDPrinter(name));

        // Notify any property change listeners.
        changes.firePropertyChange( NAME, oldName, name );
    }

} // end Printer class
