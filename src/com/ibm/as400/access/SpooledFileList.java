///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
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
 * The SpooledFileList class is used to build a list of objects of type
 * SpooledFile.  The list can be filtered by formtype, output queue, user, ending date,
 * ending time, or user data.
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
    private static final String END_DATE_FILTER = "endDateFilter";
    private static final String END_TIME_FILTER = "endTimeFilter";
    private static final String START_DATE_FILTER = "startDateFilter";
    private static final String START_TIME_FILTER = "startTimeFilter";
    private static final String JOB_SYSTEM_FILTER = "jobSystemFilter";
   
    /**
     * Constructs a SpooledFileList object. The system
     * must be set later. This constructor is provided for visual
     * application builders that support JavaBeans. It is not
     * intended for use by application programmers.
     *
     * @see PrintObjectList#setSystem
     **/
    public SpooledFileList()
    {
        super(NPConstants.SPOOLED_FILE, new NPCPSelSplF());
        
        // Because of this constructor we will need to check the
        // system before trying to use it.
    }
    
    

    /**
     * Constructs a SpooledFileList object. It uses the system
     * name specified.
     * The default list filter will list all spooled files for the
     * current user on the specified system.
     *
     * @param system The system on which the spooled files exist.
     *
     **/
    public SpooledFileList(AS400 system)
    {
        super(NPConstants.SPOOLED_FILE, new NPCPSelSplF(), system);
    }



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

    /**
     * Returns the create job system filter.
     *
     **/
   public String getJobSystemFilter()
   {
    // If empty, getJobSystem will return an blank string.
    
       NPCPSelSplF selectionCP = (NPCPSelSplF)getSelectionCP();
       return( selectionCP.getJobSystem() );
   }

   /**
     * Returns the end create date filter.
     *
     **/
   public String getEndDateFilter()
   {
    // If empty, getEndDate will return an blank string.
    
       NPCPSelSplF selectionCP = (NPCPSelSplF)getSelectionCP();
       return( selectionCP.getEndDate() );
   }
   
   /**
     * Returns the end create date filter.
     *
     **/
   public String getEndTimeFilter()
   {
    // If empty, getEndTime will return an blank string.
    
       NPCPSelSplF selectionCP = (NPCPSelSplF)getSelectionCP();
       return( selectionCP.getEndTime() );
   }
   
   

    PrintObject newNPObject(NPCPID cpid, NPCPAttribute cpattr)
    {
        return new SpooledFile(system_, (NPCPIDSplF)cpid, cpattr);
    }

   /**
     * Returns the create start date filter.
     *
     **/
   public String getStartDateFilter()
   {
    // If empty, getStartDate will return blank string.
    
       NPCPSelSplF selectionCP = (NPCPSelSplF)getSelectionCP();
       return( selectionCP.getStartDate() );
   }

   /**
     * Returns the create date filter.
     *
     **/
   public String getStartTimeFilter()
   {
    // If empty, getStartTime will return an blank string.
    
       NPCPSelSplF selectionCP = (NPCPSelSplF)getSelectionCP();
       return( selectionCP.getEndTime() );
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
        if (impl_ != null)
            impl_.setFilter("formType", formTypeFilter);
            
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
        if (impl_ != null)
            impl_.setFilter("spooledFileQueue", queueFilter);

        // Notify any property change listeners.
        changes.firePropertyChange( QUEUE_FILTER, oldQueueFilter, queueFilter );
    }


    /**
      * Sets the create job system filter.
      * The name of the system where the job, specified in the qualified job name
      * parameter, was run. This parameter can be used in conjunction with the user
      * name, qualified output queue name, form type, user-specified data, auxiliary
      * storage pool, starting spooled file create date, starting spooled file create
      * time, ending spooled file create date, ending spooled file create time, or
      * qualified job name parameters to return a partial list of all the spooled 
      * files. The list of spooled files returned is sorted by status, output 
      * priority, date, and time.
      *
      * The following special values are supported for this parameter:
      *@param jobSystemFilter
      *<br>
      *  <I>*ALL</I> The returned list is not to be filtered based on job system name.
      *  <I>*CURRENT</I>
      *              Only spooled files created on the current system are to be returned.
      *  <job-system-name>
      *              Only spooled files created on the system specified are to be returned.</ul>
      * @exception PropertyVetoException If the change is vetoed.
      *
      **/
    public void setJobSystemFilter(String jobSystemFilter)
      throws PropertyVetoException
    {
        if( jobSystemFilter == null )
        {
            Trace.log( Trace.ERROR, "Parameter 'jobSystemFilter' is null" );
            throw new NullPointerException( JOB_SYSTEM_FILTER );
        }

        String oldJobSystemFilter = getJobSystemFilter();

        // Tell any vetoers about the change. If anyone objects
        // we let the PropertyVetoException propagate back to
        // our caller.
        vetos.fireVetoableChange( JOB_SYSTEM_FILTER, oldJobSystemFilter, jobSystemFilter );

        // No one vetoed, make the change.
        NPCPSelSplF selectionCP = (NPCPSelSplF)getSelectionCP();
        selectionCP.setJobSystem(jobSystemFilter);

        // Propagate any change to ImplRemote if necessary...
        if (impl_ != null) 
            impl_.setFilter("jobSystem", jobSystemFilter);  

        // Notify any property change listeners.
        changes.firePropertyChange( JOB_SYSTEM_FILTER, oldJobSystemFilter, jobSystemFilter );
    }

    /**
      * Sets the end date filter.
      * The date the spooled file was created on the system. If the Starting 
      * spooled file create date field is set to *ALL, then this field must be 
      * set to blanks. If a date has been specified for the Starting spooled 
      * file create date field, then this field must be set to a valid date. The
      * date must be in the CYYMMDD format or the following special value:
      * @param endDateFilter
      * <br>
      *   <I>date</I> All spooled files with a create date and time equal to or 
      * later than the starting spooled file create date are to be returned.
      * <ul>
      * <li> *LAST - All spooled files with a create date and time equal to or 
      * later than the starting spooled file create date are to be returned.
      * </ul>
      * @exception PropertyVetoException If the change is vetoed.
      *
      **/
    public void setEndDateFilter(String endDateFilter)
      throws PropertyVetoException
    {
        if( endDateFilter == null )
        {
            Trace.log( Trace.ERROR, "Parameter 'endDateFilter' is null" );
            throw new NullPointerException( END_DATE_FILTER );
        }
        if ((endDateFilter.length() > 7)) 
        {
            Trace.log(Trace.ERROR, "Parameter 'endDateFilter' has invalid length.");
            throw new ExtendedIllegalArgumentException(
                "endDateFilter("+endDateFilter+")",
                ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }

        String oldEndDateFilter = getEndDateFilter();

        // Tell any vetoers about the change. If anyone objects
        // we let the PropertyVetoException propagate back to
        // our caller.
        vetos.fireVetoableChange( END_DATE_FILTER, oldEndDateFilter, endDateFilter );

        // No one vetoed, make the change.
        NPCPSelSplF selectionCP = (NPCPSelSplF)getSelectionCP();
        selectionCP.setEndDate(endDateFilter);

        // Propagate any change to ImplRemote if necessary...
        if (impl_ != null) 
            impl_.setFilter("endDate", endDateFilter);  

        // Notify any property change listeners.
        changes.firePropertyChange( END_DATE_FILTER, oldEndDateFilter, endDateFilter );
    }

    /**
      * Sets the end time filter.
      * The time the spooled file was created on the system. This field must be 
      * set to blanks when special value *ALL is used for field Starting spooled 
      * file create date or when special value *LAST is used for field Ending 
      * spooled file create date. This field must have a value set if a date is
      * specified for field Ending spooled file create date. The time must be in the
      * HHMMSS format.
      * @param endTimeFilter
      * <br>
      *   <I>time</I> All spooled files with a create date and time equal to or 
      * later than the starting spooled file create date are to be returned.
      * 
      * @exception PropertyVetoException If the change is vetoed.
      *
      **/
    public void setEndTimeFilter(String endTimeFilter)
      throws PropertyVetoException
    {
        if( endTimeFilter == null )
        {
            Trace.log( Trace.ERROR, "Parameter 'endTimeFilter' is null" );
            throw new NullPointerException( END_TIME_FILTER );
        }

        String oldEndTimeFilter = getEndTimeFilter();

        // Tell any vetoers about the change. If anyone objects
        // we let the PropertyVetoException propagate back to
        // our caller.
        vetos.fireVetoableChange( END_TIME_FILTER, oldEndTimeFilter, endTimeFilter );

        // No one vetoed, make the change.
        NPCPSelSplF selectionCP = (NPCPSelSplF)getSelectionCP();
        selectionCP.setEndTime(endTimeFilter);

        // Propagate any change to ImplRemote if necessary...
        if (impl_ != null) 
            impl_.setFilter("endTime", endTimeFilter); 

        // Notify any property change listeners.
        changes.firePropertyChange( END_TIME_FILTER, oldEndTimeFilter, endTimeFilter );
    }

    /**
      * Sets the create start date filter.
      * The date the spooled file was created on the system. This parameter can be
      * used in conjunction with the user name, qualified output queue name, form 
      * type, user-specified data, auxiliary storage pool, job system name, starting 
      * spooled file create time, ending spooled file create date, ending spooled file
      * create time, or qualified job name parameters to return a partial list of all
      * the spooled files. The list of spooled files returned is sorted by status, 
      * output priority, date, and time. The date must be in the CYYMMDD format or 
      * one of the following special values:
      * @param startDateFilter
      * <br>
      *   <I>date</I> All spooled files with a create date and time equal to or 
      * later than the starting spooled file create date are to be returned.
      *   <I>*ALL</I> The returned list is not to be filtered based on spooled file 
      *               create date and spooled file create time.
      *   <I>*FIRST</I> All spooled files starting with the earliest create date and time and less than or equal to the ending
      *               spooled file create date and time are to be returned.
      * 
      * @exception PropertyVetoException If the change is vetoed.
      *
      **/
    public void setStartDateFilter(String startDateFilter)
      throws PropertyVetoException
    {
        if( startDateFilter == null )
        {
            Trace.log( Trace.ERROR, "Parameter 'startDateFilter' is null" );
            throw new NullPointerException( START_DATE_FILTER );
        }

        String oldStartDateFilter = getStartDateFilter();

        // Tell any vetoers about the change. If anyone objects
        // we let the PropertyVetoException propagate back to
        // our caller.
        vetos.fireVetoableChange( START_DATE_FILTER, oldStartDateFilter, startDateFilter );

        // No one vetoed, make the change.
        NPCPSelSplF selectionCP = (NPCPSelSplF)getSelectionCP();
        selectionCP.setStartDate(startDateFilter);

        // Propagate any change to ImplRemote if necessary...
        if (impl_ != null) 
            impl_.setFilter("startDate", startDateFilter);  

        // Notify any property change listeners.
        changes.firePropertyChange( START_DATE_FILTER, oldStartDateFilter, startDateFilter );
    }

    /**
      * Sets the create start time filter.
      * This parameter can be used in conjunction with the user name, qualified
      * output queue name, form type, user-specified data, auxiliary storage pool, 
      * job system name, starting spooled file create date, ending spooled file
      * create date, ending spooled file create time, or qualified job name 
      * parameters to return a partial list of all the spooled files. The list of
      * spooled files returned is sorted by status, output priority, date, and time.
      * This parameter must be set to blanks when special value *ALL or *FIRST is used
      * for parameter Starting spooled file create date. This parameter must have a
      * value set if a date is specified for parameter Starting spooled file create 
      * date. The time must be in the HHMMSS format. 
      *
      * The time format HHMMSS is defined as follows:
      * @param startTimeFilter
      * <br>
      *   <I>time</I> All spooled files with a create date and time equal to or 
      * @exception PropertyVetoException If the change is vetoed.
      *
      **/
    public void setStartTimeFilter(String startTimeFilter)
      throws PropertyVetoException
    {
        if( startTimeFilter == null )
        {
            Trace.log( Trace.ERROR, "Parameter 'startTimeFilter' is null" );
            throw new NullPointerException( START_TIME_FILTER );
        }

        String oldStartTimeFilter = getStartTimeFilter();

        // Tell any vetoers about the change. If anyone objects
        // we let the PropertyVetoException propagate back to
        // our caller.
        vetos.fireVetoableChange( START_TIME_FILTER, oldStartTimeFilter, startTimeFilter );

        // No one vetoed, make the change.
        NPCPSelSplF selectionCP = (NPCPSelSplF)getSelectionCP();
        selectionCP.setStartTime(startTimeFilter);

        // Propagate any change to ImplRemote if necessary...
        if (impl_ != null)
            impl_.setFilter("StartTime", startTimeFilter);  

        // Notify any property change listeners.
        changes.firePropertyChange( START_TIME_FILTER, oldStartTimeFilter, startTimeFilter );
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
        if (impl_ != null)
            impl_.setFilter("user", userFilter);
   
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
        if (impl_ != null)
            impl_.setFilter("userData", userDataFilter);
   
        // Notify any property change listeners.
        changes.firePropertyChange( USER_DATA_FILTER,
                                    oldUserDataFilter, userDataFilter );
    }

} // SpooledFileList class

