///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PrinterFile.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.beans.PropertyVetoException;

/**
 * The PrinterFile class represents an AS/400 printer file.
 * An instance of this class can be used to manipulate an individual
 * AS/400 printer file.
 *
 * See <a href="doc-files/PrinterFileAttrs.html">Printer File Attributes</a> for
 * valid attributes.
 *
 **/

public class PrinterFile extends PrintObject
implements java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    
    static final long serialVersionUID = 4L;


    private static final String PATH = "path";

    // constructor used internally (not externalized since it takes
    // an ID code point
    PrinterFile(AS400 system, NPCPIDPrinterFile id, NPCPAttribute attrs)
    {
       super(system, id, attrs, NPConstants.PRINTER_FILE);  // @B1C
    }



    /**
     * Constructs a PrinterFile object. The AS/400 system and the
     * integrated file system name of the printer file must be set
     * later. This constructor is provided for visual application
     * builders that support JavaBeans. It is not intended for use
     * by application programmers.
     *
     * @see PrintObject#setSystem
     * @see #setPath
     **/
    public PrinterFile()
    {
        super(null, null, NPConstants.PRINTER_FILE); // @B1C

        // Because of this constructor we will need to check the
        // run time state of PrinterFile objects.
    }



    /**
     * Constructs a PrinterFile object. It uses the specified system name and
     * printer file that identify it on the system.
     *
     * @param system The AS/400 on which this printer file exists.
     * @param printerFileName The integrated file system name of the printer file. The format of
     * the printer file string must be in the format of \QSYS.LIB\libname.LIB\printerfilename.FILE.
     **/
    public PrinterFile(AS400 system,
		       String printerFileName)
    {
        super(system, buildIDCodePoint(printerFileName), null, NPConstants.PRINTER_FILE); // @B1C

        // Base class constructor checks for a null system.
        // QSYSObjectPathName() checks for a null printerFileName.
    }



    // builds the ID CodePoint
    private static NPCPIDPrinterFile buildIDCodePoint(String IFSPrinterFileName)
    {
	    QSYSObjectPathName ifsPath = new QSYSObjectPathName(IFSPrinterFileName, "FILE");

	    return new NPCPIDPrinterFile(ifsPath.getObjectName(), ifsPath.getLibraryName());
    }



    // Check the run time state
    void checkRunTimeState()
    {
        // check whatever the base class needs to check
        super.checkRunTimeState();

        // PrinterFile's need to additionally check the IFS pathname.
        if( getIDCodePoint() == null )
        {
            Trace.log(Trace.ERROR, "Parameter 'path' has not been set.");
            throw new ExtendedIllegalStateException(
              PATH, ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }
    }



    // A1A - Added chooseImpl() method
    /**
     * Chooses the appropriate implementation.
     **/
    void chooseImpl()
    throws IOException, AS400SecurityException                              // @B1A
    {
        // We need to get the system to connect to...
        AS400 system = getSystem();
        if (system == null) {
            Trace.log( Trace.ERROR, "Attempt to use PrinterFile before setting system." );
            throw new ExtendedIllegalStateException("system",
                                    ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        impl_ = (PrinterFileImpl) system.loadImpl2("com.ibm.as400.access.PrinterFileImplRemote",
                                                   "com.ibm.as400.access.PrinterFileImplProxy");
        super.setImpl();
    }



    /**
     * Returns the name of the printer file.
     *
     * @return The name of the printer file.
     **/
    public String getName()
    {
        NPCPID IDCodePoint = getIDCodePoint();

        if( IDCodePoint == null ) {
            return EMPTY_STRING; // ""
        } else {
            return IDCodePoint.getStringValue(ATTR_PRTFILE);
        }
    }



    /**
     * Returns the integrated file system pathname of the printer file.
     *
     * @return The integrated file system pathname of the printer file.
     **/
    public String getPath()
    {
        NPCPID IDCodePoint = getIDCodePoint();

        if( IDCodePoint == null ) {
            return EMPTY_STRING; // ""
        } else {
            return QSYSObjectPathName.toPath(
              IDCodePoint.getStringValue(ATTR_PRTFLIB),  // library name
              IDCodePoint.getStringValue(ATTR_PRTFILE),  // printer file name
              "FILE");                                   // type
        }
    }



    /**
     * Sets one or more attributes of the object.  See
     * <a href="doc-files/PrinterFileAttrs.html">Printer File Attributes</a> for
     * a list of valid attributes that can be changed.
     *
     * @param attributes A print parameter list that contains the
     *  attributes to be changed.
     *
     * @exception AS400Exception If the AS/400 system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the AS/400.
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

        if (impl_ == null)                                      // @A1A
            chooseImpl();                                       // @A1A
        ((PrinterFileImpl) impl_).setAttributes(attributes);    // @A1A
        // propagate any changes to attrs                       // @A1A
        attrs = impl_.getAttrValue();                           // @A1A
    }



    /**
     * Sets the integrated file system pathname of the printer file.
     *
     * @param path The integrated file system name of the printer file. The format of
     * the printer file string must be in the format of \QSYS.LIB\libname.LIB\printerfilename.FILE.
     *
     * @exception PropertyVetoException If the change is vetoed.
     *
     **/
    public void setPath(String path)
      throws PropertyVetoException
    {
        if( path == null )
        {
            Trace.log( Trace.ERROR, "Parameter 'path' is null" );
            throw new NullPointerException( PATH );
        }

        // check for connection...                                                  // @A1A
        if (impl_ != null) {                                                        // @A1A
            Trace.log(Trace.ERROR, "Cannot set property 'path' after connect.");    // @A1A
            throw new ExtendedIllegalStateException(PATH, ExtendedIllegalStateException.PROPERTY_NOT_CHANGED ); // @A1A
        }

        String oldPath = getPath();

        // Tell any vetoers about the change. If anyone objects
        // we let the PropertyVetoException propagate back to
        // our caller.
        vetos.fireVetoableChange( PATH, oldPath, path );

        // No one vetoed, make the change.
        setIDCodePoint(buildIDCodePoint(path));

        // Notify any property change listeners.
        changes.firePropertyChange( PATH, oldPath, path );
    }

} // end PrinterFile class
