///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SpooledFileList.java
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
 * The SpooledFileList class is used to build a list of AS/400 spooled file objects of type
 * SpooledFile.  The list can be filtered by formtype, output queue, user,
 * or user data.
 *
 *@see SpooledFile
 **/

public class SpooledFileList extends PrintObjectList
implements java.io.Serializable 
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   

    static final long serialVersionUID = 4L;



    private static final String FORM_TYPE_FILTER = "formTypeFilter";
    private static final String QUEUE_FILTER = "queueFilter";
    private static final String USER_FILTER = "userFilter";
    private static final String USER_DATA_FILTER = "userDataFilter";

   
    /**
     * Constructs a SpooledFileList object. The AS/400 system
     * must be set later. This constructor is provided for visual
     * application builders that support JavaBeans. It is not
     * intended for use by application programmers.
     *
     * @see PrintObjectList#setSystem
     **/
    public SpooledFileList()
    {
        super(NPConstants.SPOOLED_FILE, new NPCPSelSplF()); // @B1C
        
        // Because of this constructor we will need to check the
        // system before trying to use it.
    }
    
    

    /**
     * Constructs a SpooledFileList object. It uses the system
     * name specified.
     * The default list filter will list all spooled files for the
     * current user on the specified system.
     *
     * @param system The AS/400 on which the spooled files exist.
     *
     **/
    public SpooledFileList(AS400 system)
    {
        super(NPConstants.SPOOLED_FILE, new NPCPSelSplF(), system); // @B1C
    }



    // @A1A - Added chooseImpl() method
    /**
     * Chooses the appropriate implementation.
     **/
    void chooseImpl()
    {
        AS400 system = getSystem();
        if (system == null) {
            Trace.log( Trace.ERROR, "Attempt to use SpooledFileList before setting system.");
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }  
        impl_ = (PrintObjectListImpl) system.loadImpl2("com.ibm.as400.access.SpooledFileListImplRemote",
                                                       "com.ibm.as400.access.SpooledFileListImplProxy");
        super.setImpl();                                               
    }



    /**
      * Returns the formtype list filter.
      *
      **/
    public String getFormTypeFilter()
    {
        // The selection code point is always present, the formType Filter     
        // may not have been set. If empty, getFormType() will
        // return an empty string.

        NPCPSelSplF selectionCP = (NPCPSelSplF)getSelectionCP();
        return( selectionCP.getFormType() );
    }



    /**
      * Returns the output queue filter.
      *
      **/
    public String getQueueFilter()
    {
        // The selection code point is always present, the Queue Filter
        // may not have been set. If empty, getQueue() will return
        // an empty string.

        NPCPSelSplF selectionCP = (NPCPSelSplF)getSelectionCP();
        return( selectionCP.getQueue() );
    }



    /**
      * Returns the user ID list filter.
      *
      **/
    public String getUserFilter()
    {
        // The selection code point is always present, the user Filter
        // may not have been set. If empty, getUser() will return
        // an empty string.

        NPCPSelSplF selectionCP = (NPCPSelSplF)getSelectionCP();
        return( selectionCP.getUser() );
    }



    /**
      * Returns the user data list filter.
      *
      **/
    public String getUserDataFilter()
    {
        // The selection code point is always present, the userData Filter
        // may not have been set. If empty, getUserData() will return
        // an empty string.

        NPCPSelSplF selectionCP = (NPCPSelSplF)getSelectionCP();
        return( selectionCP.getUserData() );
    }



    // @A5A
    PrintObject newNPObject(NPCPID cpid, NPCPAttribute cpattr)
    {
        return new SpooledFile(system_, (NPCPIDSplF)cpid, cpattr);
    }


    /**
      * Sets the formtype list filter.
      * @param formTypeFilter The form type the spooled file must to be included
      * in the list.  It cannot be greater than 10 characters.
      * The value can be any specific value or any of these special values:
      * <ul>
      *  <li> *ALL - Spooled files with any form type will be included in the list.
      *  <li> *STD - Spooled files with the form type *STD will be included in the list.
      * </ul>
      * The default is *ALL.
      *
      * @exception PropertyVetoException If the change is vetoed.
      *
      **/
    public void setFormTypeFilter(String formTypeFilter)
      throws PropertyVetoException
    {
        if( formTypeFilter == null )
        {
            Trace.log( Trace.ERROR, "Parameter 'formTypeFilter' is null" );
            throw new NullPointerException( FORM_TYPE_FILTER );
        }

        // Allow a length of 0 to remove the filter from the
        // selection code point.

        if (formTypeFilter.length() > 10)
        {
            Trace.log(Trace.ERROR, "Parameter 'formTypeFilter' is greater than 10 characters in length.");
            throw new ExtendedIllegalArgumentException(
                "formTypeFilter("+formTypeFilter+")",
                ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        String oldFormTypeFilter = getFormTypeFilter();

        // Tell any vetoers about the change. If anyone objects
        // we let the PropertyVetoException propagate back to
        // our caller.
        vetos.fireVetoableChange( FORM_TYPE_FILTER,
                                  oldFormTypeFilter, formTypeFilter );

        // No one vetoed, make the change.
        NPCPSelSplF selectionCP = (NPCPSelSplF)getSelectionCP();
        selectionCP.setFormType(formTypeFilter);
        
        // Propagate any change to ImplRemote if necessary...
        if (impl_ != null) // @A1A
            impl_.setFilter("formType", formTypeFilter); // @A1A
            
        changes.firePropertyChange( FORM_TYPE_FILTER,
                                    oldFormTypeFilter, formTypeFilter );
    }



    /**
      * Sets the output queue filter.
      * @param queueFilter The library and output queues on which to list spooled
      *  files.   The format of the queueFilter string must be in the
      *  format of /QSYS.LIB/libname.LIB/queuename.OUTQ where
      * <br>
      *   <I>libname</I> is the library name that contains the queues to search.
      *     It can be a specific name or one of these special values:
      * <ul>
      * <li> %CURLIB% - The server job's current library
      * <li> %LIBL%   - The server job's library list
      * <li> %ALL%    - All libraries are searched.  This value is only valid
      *                if the queuename is %ALL%.
      * </ul>
      *   <I>queuename</I> is the name of an output queues to search.
      *     It can be a specific name or the special value %ALL%.
      *     If it is %ALL%, then the libname must be %ALL%.
      *
      * @exception PropertyVetoException If the change is vetoed.
      *
      **/
    public void setQueueFilter(String queueFilter)
      throws PropertyVetoException
    {
        if( queueFilter == null )
        {
            Trace.log( Trace.ERROR, "Parameter 'queueFilter' is null" );
            throw new NullPointerException( QUEUE_FILTER );
        }

        String oldQueueFilter = getQueueFilter();

        // Tell any vetoers about the change. If anyone objects
        // we let the PropertyVetoException propagate back to
        // our caller.
        vetos.fireVetoableChange( QUEUE_FILTER, oldQueueFilter, queueFilter );

        // No one vetoed, make the change.
        NPCPSelSplF selectionCP = (NPCPSelSplF)getSelectionCP();
        selectionCP.setQueue(queueFilter);

        // Propagate any change to ImplRemote if necessary...
        if (impl_ != null) // @A1A
            impl_.setFilter("spooledFileQueue", queueFilter);  // @A1A

        // Notify any property change listeners.
        changes.firePropertyChange( QUEUE_FILTER, oldQueueFilter, queueFilter );
    }



    /**
     * Sets the user ID list filter.
     *
     * @param userFilter The user or users for which to list spooled files.
     * The value cannot be greater than 10 characters.
     * The value can be any specific user ID or any of these special values:
     * <UL>
     * <LI>  *ALL - Spooled files created by all users will be included in the list.
     * <LI>  *CURRENT - Spooled files created by the current user only will be in the list.
     * </UL>
     * The default is *CURRENT.
     *
     * @exception PropertyVetoException If the change is vetoed.
     *
     **/
    public void setUserFilter(String userFilter)
      throws PropertyVetoException
    {
        if( userFilter == null )
        {
            Trace.log( Trace.ERROR, "Parameter 'userFilter' is null" );
            throw new NullPointerException( USER_FILTER );
        }

        // Allow a length of 0 to remove the filter from the
        // selection code point.

        if (userFilter.length() > 10)
        {
            Trace.log(Trace.ERROR, "Parameter 'userFilter' is greater than 10 characters in length.");
            throw new ExtendedIllegalArgumentException(
                "userFilter("+userFilter+")",
                ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }

        String oldUserFilter = getUserFilter();

        // Tell any vetoers about the change. If anyone objects
        // we let the PropertyVetoException propagate back to
        // our caller.
        vetos.fireVetoableChange( USER_FILTER, oldUserFilter, userFilter );

        // No one vetoed, make the change.
        NPCPSelSplF selectionCP = (NPCPSelSplF)getSelectionCP();
        selectionCP.setUser(userFilter);
        
        // Propagate any change to ImplRemote if necessary...
        if (impl_ != null)  // @A1A
            impl_.setFilter("user", userFilter);    // @A1A
   
        // Notify any property change listeners
        changes.firePropertyChange( USER_FILTER, oldUserFilter, userFilter );
    }



    /**
     * Sets the user data list filter.
     *
     * @param userDataFilter The user data the spooled file must
     *  have for it to be included in the list.  The value can be
     *  any specific value or the special value *ALL.  The value cannot be
     *  greater than 10 characters.
     *  The default is *ALL.
     *
     * @exception PropertyVetoException If the change is vetoed.
     *
     **/
    public void setUserDataFilter(String userDataFilter)
      throws PropertyVetoException
    {
        if( userDataFilter == null )
        {
            Trace.log( Trace.ERROR, "Parameter 'userDataFilter' is null" );
            throw new NullPointerException( USER_DATA_FILTER );
        }

        // Allow a length of 0 to remove the filter from the
        // selection code point.

        if (userDataFilter.length() > 10)
        {
            Trace.log(Trace.ERROR, "Parameter 'userDataFilter' is greater than 10 characters in length.");
            throw new ExtendedIllegalArgumentException(
                "userDataFilter("+userDataFilter+")",
                ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }

        String oldUserDataFilter = getUserDataFilter();

        // Tell any vetoers about the change. If anyone objects
        // we let the PropertyVetoException propagate back to
        // our caller.
        vetos.fireVetoableChange( USER_DATA_FILTER,
                                  oldUserDataFilter, userDataFilter );

        // No one vetoed, make the change.
        NPCPSelSplF selectionCP = (NPCPSelSplF)getSelectionCP();
        selectionCP.setUserData(userDataFilter);
   
        // Propagate any change to ImplRemote if necessary...
        if (impl_ != null) // @A1A
            impl_.setFilter("userData", userDataFilter); // @A1A
   
        // Notify any property change listeners.
        changes.firePropertyChange( USER_DATA_FILTER,
                                    oldUserDataFilter, userDataFilter );
    }

} // SpooledFileList class

