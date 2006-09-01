///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AFPResourceList.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.Vector;
import java.beans.PropertyVetoException;

/**
  * The AFPResourceList class is used to build a list of i5/OS AFP resource objects of type AFPResource.
  * The list can be filtered by library and resource name,
  * by resource type and by spooled file (list only resources
  * used by a particular spooled file).  In addition,
  * font resources may be filtered by pel density.
  *
  * To list and use AFP resources, your system operating system must
  * be at V3R7 or later.
  *
  *@see AFPResource
  **/


public class AFPResourceList extends PrintObjectList
implements java.io.Serializable
{
    private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    static final long serialVersionUID = 4L;

    private SpooledFile spooledFileFilter_;
    private static final String RESOURCE_FILTER = "resourceFilter";
    private static final String SPOOLED_FILE_FILTER = "spooledFileFilter";

    /**
     * Constructs an AFPResourceList. The system must be set
     * later. This constructor is provide for visual application builders
     * that support JavaBeans. It is not intended for use by
     * application programmers.
     *
     * @see PrintObjectList#setSystem
     **/
    public AFPResourceList()
    {
        super(NPConstants.RESOURCE, new NPCPSelRes());
        // Because of this constructor we will need to check the
        // the run time state of AFPResourceList objects.
    }



    /**
     * Constructs an AFPResourceList.  The default filtering
     * criteria will list all resources in the system library list.
     * Use the various setXxxxFilter methods to override the defaults.
     *
     * @param system The system on which the AFP resources exist.
     **/
    public AFPResourceList(AS400 system)
    {
        super( NPConstants.RESOURCE, new NPCPSelRes(), system );
    }


    /**
     * Chooses the appropriate implementation.
     **/
    void chooseImpl()
    {
        AS400 system = getSystem();
        if (system == null) {
            Trace.log( Trace.ERROR, "Attempt to use AFPResourceList before setting system.");
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }  
        impl_ = (PrintObjectListImpl) system.loadImpl2("com.ibm.as400.access.AFPResourceListImplRemote",
                                                       "com.ibm.as400.access.AFPResourceListImplProxy");
        super.setImpl();                                               
    }
     
    

    /**
      * Returns the library, name, and resource type being used as a
      * resource list filter.
      * @return The library, name, and resource type being used as a
      * resource list filter.
      **/
    public String getResourceFilter()
    {
        // The selection code point is always present, it may
        // however be empty. If empty, getResource will return
        // an empty string.

        NPCPSelRes selectionCP = (NPCPSelRes)getSelectionCP();
        return( selectionCP.getResource() );
    }
 
    
    
    /**
      * Returns the spooled file object being used as a
      * resource list filter.
      *@return The spooled file object being used as a
      * resource list filter.
      **/
    public SpooledFile getSpooledFileFilter()
    {
        return( spooledFileFilter_ );
    }



    PrintObject newNPObject(NPCPID cpid, NPCPAttribute cpattr)
    {
        return new AFPResource(system_, (NPCPIDAFPResource)cpid, cpattr);
    }


    /**
      * Sets resource list filter by library, name, and resource type.
      *
      * @param resourceFilter The resources to list.
      *  The format of the resourceFilter string must be in the
      *  format of "/QSYS.LIB/libname.LIB/resource.type", where
      * <br>
      *   <I>libname</I> is the library name that contains the resources to search.
      *     Resources listed will be restricted to those found in these libraries.  
      *
      *     The library name can be a specific name or one of these special values:
      * <ul>
      * <li> %ALL%     - All libraries are searched.
      * <li> %ALLUSR%  - All user-defined libraries, plus libraries containing user data
      *                 and having names starting with the letter Q.
      * <li> %CURLIB%  - The server job's current library.
      * <li> %LIBL%    - The server job's library list.
      * <li> %USRLIBL% - The user portion of the server job's library list.
      * </ul>
      * <p> 
      *   <I>resource</I> is the name of the resource(s) to list.
      *     It can be a specific name, a generic name, or the special value %ALL%.
      * <br>
      *   <I>type</I> is the type of resource to list.  It can be any of these
      *  special values:
      *  <ul>
      *  <li> %ALL%     - All resources are listed.
      *  <li> FNTRSC    - Only font resources are listed.
      *  <li> FORMDF    - Only form definitions are listed.
      *  <li> OVL       - Only overlays are listed.
      *  <li> PAGEDFN   - Only page definitions are listed.
      *  <li> PAGESEG   - Only page segments are listed.
      *  </ul>
      *  The default for the library is %LIBL%; for resource, it is %ALL%; and for
      *  type, it is %ALL%.
      *
      * @exception PropertyVetoException If the change is vetoed.
      *
      **/
    public void setResourceFilter(String resourceFilter)
      throws PropertyVetoException
    {
        if( resourceFilter == null )
        {
            Trace.log( Trace.ERROR, "Parameter 'resourceFilter' is null" );
            throw new NullPointerException( RESOURCE_FILTER );
        }

        String oldResourceFilter = getResourceFilter();

        // Tell any vetoers about the change. If anyone objects
        // we let the PropertyVetoException propagate back to
        // our caller.
        vetos.fireVetoableChange( RESOURCE_FILTER,
                                  oldResourceFilter, resourceFilter );

        // No one vetoed, make the change.
        NPCPSelRes selectionCP = (NPCPSelRes)getSelectionCP();
        selectionCP.setResource( resourceFilter );

        // Propagate change to ImplRemote if necessary...
        if (impl_ != null)
           impl_.setFilter("resource", resourceFilter);

        // Notify any property change listeners
        changes.firePropertyChange( RESOURCE_FILTER,
                                    oldResourceFilter, resourceFilter );
    }



    /**
      * Sets resource list filter by spooled file.
      * Only resources used by the spooled file are listed.
      * @param spooledFileFilter The spooled file for which
      * the resources will be listed.
      *
      * @exception PropertyVetoException If the change is vetoed.
      *
      **/
    public void setSpooledFileFilter(SpooledFile spooledFileFilter)
      throws PropertyVetoException
    {
        // Allow spooledFile to be null to remove the filter from the
        // selection code point.

        SpooledFile oldSpooledFileFilter = getSpooledFileFilter();

        // Tell any vetoers about the change. If anyone objects
        // we let the PropertyVetoException propagate back to
        // our caller.
        vetos.fireVetoableChange( SPOOLED_FILE_FILTER,
                                  oldSpooledFileFilter, spooledFileFilter );

        // No one vetoed, make the change.
        spooledFileFilter_ = spooledFileFilter;
        if( spooledFileFilter_ == null )
        {
            // A null value will remove the filter.
            setIDCodePointFilter(null);
        } else {
            setIDCodePointFilter(spooledFileFilter_.getIDCodePoint());
        }


        // Notify any property change listeners
        changes.firePropertyChange( SPOOLED_FILE_FILTER,
                                    oldSpooledFileFilter, spooledFileFilter );
    }

} // APFResourceList class

