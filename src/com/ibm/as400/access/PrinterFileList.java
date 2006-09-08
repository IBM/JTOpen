///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PrinterFileList.java
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
 * The  PrinterFileList class is used to build a list of objects of type PrinterFile.
 * The list can be filtered by library and printer file name.
 *
 *@see PrinterFile
 **/

public class PrinterFileList extends PrintObjectList
implements java.io.Serializable
{
    private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    static final long serialVersionUID = 4L;


    private static final String PRINTER_FILE_FILTER = "printerFileFilter";

    /**
     * Constructs a PrinterFileList object. The system must
     * be set later. This constructor is provided for visual application
     * builders that support JavaBeans. It is not intended for use
     * by application programmers.
     *
     * @see PrintObjectList#setSystem
     **/
    public PrinterFileList()
    {
        super(NPConstants.PRINTER_FILE, new NPCPSelPrtF());
        // Because of this constructor we will need to check the
        // system before trying to use it.
    }



    /**
     * Constructs a PrinterFileList object. It uses the system name specified.
     *
     * @param system The system on which the printer files exists.
     *
     **/
    public PrinterFileList(AS400 system)
    {
	    super(NPConstants.PRINTER_FILE, new NPCPSelPrtF(), system);
    }



    /**
     * Chooses the appropriate implementation.
     **/
    void chooseImpl()
    {
        AS400 system = getSystem();
        if (system == null) {
            Trace.log( Trace.ERROR, "Attempt to use PrinterFileList before setting system.");
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }  
        impl_ = (PrintObjectListImpl) system.loadImpl2("com.ibm.as400.access.PrinterFileListImplRemote",          
                                                       "com.ibm.as400.access.PrinterFileListImplProxy");
        super.setImpl();                                               
    }
  
  

    /**
      * Returns the printer file list filter.
      * @return The printer file list filter.
      *
      **/
    public String getPrinterFileFilter()
    {
        // The selection code point is always present, it may
        // however be empty. If empty, getPrinterFile() will
        // return an empty string.

        NPCPSelPrtF selectionCP = (NPCPSelPrtF)getSelectionCP();
        return( selectionCP.getPrinterFile() );
    }

  

    PrintObject newNPObject(NPCPID cpid, NPCPAttribute cpattr)
    {
        return new PrinterFile(system_, (NPCPIDPrinterFile)cpid, cpattr);
    }


    /**
      * Sets the printer file list filter.
      * @param printerFileFilter The library and printer files to list.
      *  The format of the printerFileFilter string must be in the
      *  format of /QSYS.LIB/libname.LIB/printerfilename.FILE, where
      * <br>
      *   <I>libname</I> is the library name that contains the printer files to search.
      *     It can be a specific name or one of these special values:
      * <ul>
      * <li> %ALL%     - All libraries are searched.
      * <li> %ALLUSR%  - All user-defined libraries, plus libraries containing user data
      *                 and having names starting with the letter Q.
      * <li> %CURLIB%  - The server job's current library.
      * <li> %LIBL%    - The server job's library list.
      * <li> %USRLIBL% - The user portion of the server job's library list.
      * </ul>
      *   <I>printerfilename</I> is the name of the printer files to list.
      *     It can be a specific name, a generic name, or the special value %ALL%.
      *  The default for the library is %LIBL% and for the printer file name is %ALL%.
      *
      * @exception PropertyVetoException If the change is vetoed.
      *
      **/
    public void setPrinterFileFilter(String printerFileFilter)
      throws PropertyVetoException
    {
        if( printerFileFilter == null )
        {
            Trace.log( Trace.ERROR, "Parameter 'printerFileFilter' is null" );
            throw new NullPointerException( PRINTER_FILE_FILTER );
        }

        String oldPrinterFileFilter = getPrinterFileFilter();

        // Tell any vetoers about the change. If anyone objects
        // we let the PropertyVetoException propagate back to
        // our caller.
        vetos.fireVetoableChange( PRINTER_FILE_FILTER,
                                  oldPrinterFileFilter, printerFileFilter ); 

        // No one vetoed, make the change.
        NPCPSelPrtF selectionCP = (NPCPSelPrtF)getSelectionCP();
        selectionCP.setPrinterFile(printerFileFilter);
        
        // Propagate change to ImplRemote if necessary... 
        if (impl_ != null)
            impl_.setFilter("printerFile", printerFileFilter);
          
        // Notify any property change listeners.
        changes.firePropertyChange( PRINTER_FILE_FILTER,
                                    oldPrinterFileFilter, printerFileFilter );
    }

} // PrinterFileList class

