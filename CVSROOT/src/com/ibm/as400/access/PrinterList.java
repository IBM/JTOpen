///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PrinterList.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyVetoException;

/**
 * The PrinterList class is used to build a list of objects of type Printer.
 * The list can be filtered by printer name.
 *
 *@see Printer
 **/

public class PrinterList extends PrintObjectList
implements java.io.Serializable
{
    private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";
    
    static final long serialVersionUID = 4L;

    private static final String PRINTER_FILTER = "printerFilter";

    /**
     * Constructs a PrinterList object. The system must be
     * set later. This constructor is provided for visual application
     * builders that support JavaBeans. It is not intended for use
     * by application programmers.
     *
     * @see PrintObjectList#setSystem
     **/
    public PrinterList()
    {
	    super(NPConstants.PRINTER_DEVICE, new NPCPSelPrtD());

        // Because of this constructor we will need to check the
        // system before trying to use it.
    }



    /**
     * Constructs a PrinterList object. It uses the system name specified.
     *
     * @param system The system on which the printer devices exist.
     *
     **/
    public PrinterList(AS400 system)
    {
	    super(NPConstants.PRINTER_DEVICE, new NPCPSelPrtD(), system);
    }

    
    
    /**
     * Chooses the appropriate implementation.
     **/
    void chooseImpl()
    {
        AS400 system = getSystem();
        if (system == null) {
            Trace.log( Trace.ERROR, "Attempt to use PrinterList before setting system.");
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }  
        impl_ = (PrintObjectListImpl) system.loadImpl2("com.ibm.as400.access.PrinterListImplRemote",
                                                       "com.ibm.as400.access.PrinterListImplProxy");
        super.setImpl();                                               
    }

   
   
    /**
      * Returns the printer list filter.
      **/
    public String getPrinterFilter()
    {
        // The selection code point is always present, it may
        // however be empty. If empty, getPrinter() will return
        // an empty string.

        NPCPSelPrtD selectionCP = (NPCPSelPrtD)getSelectionCP();
        return( selectionCP.getPrinter() );
    }



    PrintObject newNPObject(NPCPID cpid, NPCPAttribute cpattr)
    {
        return new Printer(system_, (NPCPIDPrinter)cpid, cpattr);
    }


    /**
      * Sets printer list filter.
      * @param printerFilter The name of the printers to list.
      * It cannot be greater than 10 characters in length.
      * It can be a specific name, a generic name, or the special
      * value *ALL. The default for the printerFilter is *ALL.
      *
      * @exception PropertyVetoException If the change is vetoed.
      *
      **/
    public void setPrinterFilter(String printerFilter)
      throws PropertyVetoException
    {
        if( printerFilter == null )
        {
            Trace.log( Trace.ERROR, "Parameter 'printerFilter' is null" );
            throw new NullPointerException( PRINTER_FILTER );
        }

        // Allow a length of 0 to remove the filter from the
        // selection code point. printerFilter.length() == 0 is OK.

        if( printerFilter.length() > 10 )
        {
            Trace.log(Trace.ERROR, "Parameter 'printerFilter' is greater than 10 characters in length.");
            throw new ExtendedIllegalArgumentException(
                "printerFilter("+printerFilter+")",
                ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }

        String oldPrinterFilter = getPrinterFilter();

        // Tell any vetoers about the change. If anyone objects
        // we let the PropertyVetoException propagate back to
        // our caller.
        vetos.fireVetoableChange( PRINTER_FILTER, oldPrinterFilter, printerFilter );

        // No one vetoed, make the change.
        NPCPSelPrtD selectionCP = (NPCPSelPrtD)getSelectionCP();
        selectionCP.setPrinter(printerFilter);
        
        // Propagate change to ImplRemote if necessary...
        if (impl_ != null)
            impl_.setFilter("printer", printerFilter);

        // Notify any property change listeners.
        changes.firePropertyChange( PRINTER_FILTER, oldPrinterFilter, printerFilter );

    } // end setPrinterFilter

} // PrinterList class

