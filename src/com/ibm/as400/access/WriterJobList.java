///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: WriterJobList.java
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
 * The WriterJobList class is used to build a list of objects of type
 * WriterJob.  The list can be filtered by writer job name or output queue.
 *
 * @see WriterJob
 **/

public class WriterJobList extends PrintObjectList
implements java.io.Serializable
{
    private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    
    static final long serialVersionUID = 4L;


    private static final String QUEUE_FILTER = "queueFilter";
    private static final String WRITER_FILTER = "writerFilter";   

    /**
     * Constructs a WriterJobList object. The AS/400 system must
     * be set later. This constructor is provided for visual application
     * builders that support JavaBeans. It is not intended for use
     * by application programmers.
     *
     * @see PrintObjectList#setSystem
     **/
    public WriterJobList()
    {
        super(NPConstants.WRITER_JOB, new NPCPSelWrtJ());               // @B1C
        // Because of this constructor we will need to check the
        // system before trying to use it.
    }



    /**
     * Constructs a WriterJobList object. It uses the system name provided.
     * The default list filter will list all writer jobs on the specified system.
     *
     * @param system The AS/400 on which the writer jobs exist.
     *
     **/
    public WriterJobList(AS400 system)
    {
        super(NPConstants.WRITER_JOB, new NPCPSelWrtJ(), system);            // @B1C
    }
 
 
 
    // @A1A - Added chooseImpl() method
    /**
     * Chooses the appropriate implementation.
     **/
    void chooseImpl()
    {
        AS400 system = getSystem();
        if (system == null) {
            Trace.log( Trace.ERROR, "Attempt to use WriterJobList before setting system.");
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }  
        impl_ = (PrintObjectListImpl) system.loadImpl2("com.ibm.as400.access.WriterJobListImplRemote",
                                                       "com.ibm.as400.access.WriterJobListImplProxy");
        super.setImpl();                                               
    }
   
   

    /**
      * Returns the output queue filter.
      *
      **/
    public String getQueueFilter()
    {
        // The selection code point is always present, the Queue Filter
        // may not have been set. If empty, getQueue() returns
        // an empty string.

        NPCPSelWrtJ selectionCP = (NPCPSelWrtJ)getSelectionCP();
        return( selectionCP.getQueue() );
    }



    /**
      * Returns the writer filter.
      *
      **/
    public String getWriterFilter()
    {
        // The selection code point is always present, the writer Filter
        // may not have been set. If empty, getWriter() returns an
        // empty string.

        NPCPSelWrtJ selectionCP = (NPCPSelWrtJ)getSelectionCP();
        return( selectionCP.getWriter() );
    }



    // @A5A
    PrintObject newNPObject(NPCPID cpid, NPCPAttribute cpattr)
    {
        return new WriterJob(system_, (NPCPIDWriter)cpid, cpattr);
    }


    /**
      * Sets the output queue filter.  Only writers active for this output queue
      * will be listed.
      * @param queueFilter Specifies the library and output queue name for which the writer
      *  jobs will be listed.   The format of the queueFilter string must be in the
      *  format of /QSYS.LIB/libname.LIB/queuename.OUTQ, where
      * <br>
      *   <I>libname</I> is the library name that contains the queue for which to list writer
      *     jobs.  It must be a specific library name.
      *   <I>queuename</I> is the name of an output queue for which to list writer jobs.
      *     It must be a specific output queue name.
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
        NPCPSelWrtJ selectionCP = (NPCPSelWrtJ)getSelectionCP();
        selectionCP.setQueue(queueFilter);
        
        // Propagate change to ImplRemote if necessary...
        if (impl_ != null) // @A1A
            impl_.setFilter("writerJobQueue", queueFilter);    // @A1A

        // Notify any property change listeners.
        changes.firePropertyChange( QUEUE_FILTER, oldQueueFilter, queueFilter );
    }



    /**
     * Sets writer list filter.
     * @param writerFilter The name of the writers to list.
     *   <I>writer</I> is the name of the writers to list.
     *     It can be a specific name, a generic name, or the special value *ALL.
     *  The default for the writerFilter is *ALL.
     *
     * @exception PropertyVetoException If the change is vetoed.
     *
     **/
    public void setWriterFilter(String writerFilter)
      throws PropertyVetoException
    {
        if( writerFilter == null )
        {
            Trace.log( Trace.ERROR, "Parameter 'writerFilter' is null" );
            throw new NullPointerException( WRITER_FILTER );
        }

        // Allow a length of 0 to remove the filter from the
        // selection code point. writerFilter.length() == 0 is OK.

        if( writerFilter.length() > 10 )
        {
            Trace.log(Trace.ERROR, "Parameter 'writerFilter' is greater than 10 characters in length.");
            throw new ExtendedIllegalArgumentException(
                "writerFilter("+writerFilter+")",
                ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }

        String oldWriterFilter = getWriterFilter();

        // Tell any vetoers about the change. If anyone objects
        // we let the PropertyVetoException propagate back to
        // our caller.
        vetos.fireVetoableChange( WRITER_FILTER,
                                  oldWriterFilter, writerFilter );

        // No one vetoed, make the change.
        NPCPSelWrtJ selectionCP = (NPCPSelWrtJ)getSelectionCP();
        selectionCP.setWriter(writerFilter);
        
        // Propagate change to ImplRemote if necessary...
        if (impl_ != null) // @A1A
            impl_.setFilter("writer", writerFilter);  // @A1A

        // Notify any property change listeners.
        changes.firePropertyChange( WRITER_FILTER,
                                    oldWriterFilter, writerFilter );
    }

} // WriterJobList class

