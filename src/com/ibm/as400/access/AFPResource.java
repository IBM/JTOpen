///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: AFPResource.java
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
 * The AFPResource class represents an AS/400 AFP resource.
 * An instance of this class can be used to manipulate an individual
 * AS/400 AFP resource.
 *
 * See <a href="AFPResourceAttrs.html">AFP Resource Attributes</a> for
 * valid attributes.
 *
 **/

public class AFPResource extends PrintObject
implements java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    public static final String  STR_FNTRSC  = "FNTRSC";
    public static final String  STR_FORMDF  = "FORMDF";
    public static final String  STR_OVL     = "OVL";
    public static final String  STR_PAGDFN  = "PAGDFN";
    public static final String  STR_PAGSEG  = "PAGSEG";
    private static final String PATH        = "path";


    // constructor used internally (not externalized since it takes
    // an ID code point)
    AFPResource(AS400 system, NPCPIDAFPResource id, NPCPAttribute attrs)
    {
        super(system, id, attrs, NPConstants.RESOURCE);  // @B1C
    }


    /**
     * Constructs an AFPResource object. The AS/400 system and the
     * integrated file system name of the AFP resource must be set
     * later. This constructor is provided for visual application
     * builders that support JavaBeans. It is not intended for use
     * by application programmers.
     *
     * @see PrintObject#setSystem
     * @see #setPath
     **/
    public AFPResource()
    {
        super(null, null, NPConstants.RESOURCE); // @B1C

        // Because of this constructor we will need to check the
        // run time state of AFPResource objects.
    }



    /**
     * Constructs an AFPResource object. It uses the system and
     * resource name that identify it on that system.
     * The AS/400 referenced by <i>system</i> must be at V3R7 or later to
     * support using AFP resources.
     *
     * @param system The AS/400 on which this AFP resource exists.
     * @param resourceName The integrated file system name of the AFP resource. The format of
     * the resource string must be in the format of "/QSYS.LIB/libname.LIB/resourcename.type".
     * Valid values for <i>type</i> include FNTRSC, FORMDF, OVL, PAGSEG, and PAGDFN.
     *
     **/
    public AFPResource(AS400 system,
                       String resourceName)
    {
        super(system, buildIDCodePoint(resourceName), null, NPConstants.RESOURCE); // @B1C

        // The base class constructor checks for null system.
        // QSYSObjectPathName() checks for a null resourceName.
    }



    private static NPCPIDAFPResource buildIDCodePoint(String IFSResourceName)
    {
        QSYSObjectPathName ifsPath = new QSYSObjectPathName(IFSResourceName);

        return new NPCPIDAFPResource(ifsPath.getObjectName(),
                                     ifsPath.getLibraryName(),
                                     ifsPath.getObjectType());
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
            Trace.log( Trace.ERROR, "Attempt to use AFPResource before setting system." );
            throw new ExtendedIllegalStateException("system",
                                    ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        impl_ = (PrintObjectImpl) system.loadImpl2("com.ibm.as400.access.AFPResourceImplRemote",
                                                   "com.ibm.as400.access.AFPResourceImplProxy");
        super.setImpl();
    }



    /**
      * Returns an input stream that can be used to read the contents of the
      * AFP resource.
      *
      * @return The input stream object that can be used to read the contents
      *         of this AFP resource.
      * @exception AS400Exception If the AS/400 system returns an error message.
      * @exception AS400SecurityException If a security or authority error occurs.
      * @exception ErrorCompletingRequestException If an error occurs before the request completed.
      * @exception IOException If an error occurs while communicating with the AS/400.
      * @exception InterruptedException If this thread is interrupted.
      * @exception RequestNotSupportedException If the requested function is not supported because
      *                                         the AS/400 system is not at the correct level.
      **/
    public PrintObjectInputStream getInputStream()
        throws AS400Exception,
               AS400SecurityException,
               ErrorCompletingRequestException,
               IOException,
               InterruptedException,
               RequestNotSupportedException
    {
        PrintObjectInputStream is = new PrintObjectInputStream(this, null);
        return is;
    }



    /**
     * Returns the name of the AFP resource.
     *
     * @return The name of the AFP resource.
     **/
    public String getName()
    {
        NPCPID IDCodePoint = getIDCodePoint();

        if( IDCodePoint == null ) {
            return PrintObject.EMPTY_STRING; // ""
        } else {
            return IDCodePoint.getStringValue(ATTR_RSCNAME);
        }
    }



    /**
     * Returns the integrated file system pathname of the AFP resource.
     *
     * @return The integrated file system pathname of the AFP resource.
     **/
    public String getPath()
    {
        // the type of an AFP resource is stored as an INT so we
        // use the special getResourceType() method to get that

        NPCPID IDCodePoint = getIDCodePoint();

        if( IDCodePoint == null ) {
            return PrintObject.EMPTY_STRING; // ""
        } else {
            return QSYSObjectPathName.toPath(
              IDCodePoint.getStringValue(ATTR_RSCLIB),   // library name
              IDCodePoint.getStringValue(ATTR_RSCNAME),  // resource name
              ((NPCPIDAFPResource)(IDCodePoint)).getResourceType() ); // type as String
        }
    }



    /**
     * Sets the integrated file system pathname of the AFP resource.
     *
     * @param path The integrated file system name of the AFP resource. The format of
     * the resource string must be in the format of "/QSYS.LIB/libname.LIB/resourcename.type".
     * Valid values for <i>type</i> include FNTRSC, FORMDF, OVL, PAGSEG, and PAGDFN.
     *
     * @exception PropertyVetoException If the change is vetoed.
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
            throw new ExtendedIllegalStateException( PATH , ExtendedIllegalStateException.PROPERTY_NOT_CHANGED ); // @A1A
        }

        String oldPath = getPath();

        // Tell any vetoers about the change. If anyone objects
        // we let the PropertyVetoException propagate back to
        // our caller.
        vetos.fireVetoableChange( PATH, oldPath, path );

        // no one vetoed, make the change
        setIDCodePoint(buildIDCodePoint(path));

        // Notify any property change listeners
        changes.firePropertyChange( PATH, oldPath, path );
    }

} // end AFPResource class
