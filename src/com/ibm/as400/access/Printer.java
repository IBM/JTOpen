///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
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
 * The  Printer class represents an AS/400 printer.
 * An instance of this class can be used to manipulate an individual
 * AS/400 printer.
 *
 * See <a href="PrinterAttrs.html">Printer Attributes</a> for
 * valid attributes.
 *
 **/

public class Printer extends PrintObject
implements java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   
    private static boolean      fAttrIDsToRtvBuilt_ = false;
    private static final String NAME = "name";


    // constructor used internally (not externalized since it takes
    // an ID code point
    Printer(AS400 system, NPCPIDPrinter id, NPCPAttribute attrs)
    {
        super(system, id, attrs, NPConstants.PRINTER_DEVICE); // @B1C
    }



    /**
     * Constructs a Printer object. The AS/400 system and the
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
        super(null, null, NPConstants.PRINTER_DEVICE); // @B1C

        // Because of this constructor we will need to check the
        // system and printer name before trying to use them.
    }



    /**
     * Constructs a Printer object. It uses the specified system name and
     * the printer name that identifies it on that system.
     *
     * @param system The AS/400 on which this printer device exists.
     * @param printerName The name of the printer.  It cannot be greater
     *                    than 10 characters or less than 1 character
     *                    in length.
     *
     **/
    public Printer(AS400 system,
                   String printerName)
    {
        super(system, new NPCPIDPrinter(printerName), null, NPConstants.PRINTER_DEVICE); // @B1C

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



    // A4A - Added chooseImpl() method
    /**
     * Chooses the implementation
     **/
    void chooseImpl()
    throws IOException, AS400SecurityException                              // @B1A
    {
        AS400 system = getSystem();
        if (system == null) {
            Trace.log( Trace.ERROR, "Attempt to use Printer before setting system." );
            throw new ExtendedIllegalStateException("system",
                                    ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        impl_ = (PrintObjectImpl) system.loadImpl2("com.ibm.as400.access.PrinterImplRemote",
                                                   "com.ibm.as400.access.PrinterImplProxy");
        super.setImpl();
    }



    /**
     * Returns the name of the printer.
     *
     * @return The name of the printer.
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
