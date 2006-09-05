///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: OutputQueueList.java
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
 * The OutputQueueList class is used to build a list of system objects of type OutputQueue.
 * The list can be filtered by library and queue name.
 *
 * @see OutputQueue
 **/

public class OutputQueueList extends PrintObjectList
implements java.io.Serializable 
{
    private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";
   
    static final long serialVersionUID = 4L;

    private static final String QUEUE_FILTER = "queueFilter";    


    /**
     * Constructs an OutputQueueList object. The system must            
     * be set later. This constructor is provided for visual application
     * builders that support JavaBeans. It is not intended for use
     * by application programmers.
     *
     * @see PrintObjectList#setSystem
     **/
    public OutputQueueList()
    {
        super(NPConstants.OUTPUT_QUEUE, new NPCPSelOutQ());
        // Because of this constructor we will need to check the
        // system before trying to use it.
    }



    /**
     * Constructs an OutputQueueList object. It uses the specified system name.
     *
     * @param system The system on which the output queues exists.
     *
     **/
    public OutputQueueList(AS400 system)
    {
        super(NPConstants.OUTPUT_QUEUE, new NPCPSelOutQ(), system);
    }
  
    
    
    /**
     * Chooses the appropriate implementation.
     **/
    void chooseImpl()
    {
        AS400 system = getSystem();
        if (system == null) {
            Trace.log( Trace.ERROR, "Attempt to use OutputQueueList before setting system.");
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }  
        impl_ = (PrintObjectListImpl) system.loadImpl2("com.ibm.as400.access.OutputQueueListImplRemote",
                                                       "com.ibm.as400.access.OutputQueueListImplProxy");
        super.setImpl();                                               
    }
  
  

    /**
      * Returns the output queue list filter.
      * @return The output queue list filter.  
      **/
    public String getQueueFilter()
    {
        // The selection code point is always present, it may
        // however be empty. If empty, getQueue() will return
        // an empty string.

        NPCPSelOutQ selectionCP = (NPCPSelOutQ)getSelectionCP();
        return( selectionCP.getQueue() );
    }


    PrintObject newNPObject(NPCPID cpid, NPCPAttribute cpattr)
    {
        return new OutputQueue(system_, (NPCPIDOutQ)cpid, cpattr);
    }


    /**
     * Sets the output queue list filter.
     * @param queueFilter The library and output queues to list.
     *  The format of the queueFilter string must be in the
     *  format of /QSYS.LIB/libname.LIB/queuename.OUTQ, where
     * <br>
     *   <I>libname</I> is the library name that contains the queues to search.
     *     It can be a specific name, a generic name, or one of these special values:
     * <ul>
     * <li> %ALL%     - All libraries are searched.
     * <li> %ALLUSR%  - All user-defined libraries, plus libraries containing user data
     *                 and having names starting with the letter Q.
     * <li> %CURLIB%  - The server job's current library.
     * <li> %LIBL%    - The server job's library list.
     * <li> %USRLIBL% - The user portion of the server job's library list.
     * </ul>
     *   <I>queuename</I> is the name of the output queues to list.
     *     It can be a specific name, a generic name, or the special value %ALL%.
     *  The default for the library is %LIBL% and for the queue name is %ALL%.
     *
     * @exception PropertyVetoException If the change is vetoed.
     *
     **/
    public void setQueueFilter(String queueFilter)
      throws PropertyVetoException
    {
        if( queueFilter == null )
        {
            Trace.log( Trace.ERROR, "Parameter 'queue' is null" );
            throw new NullPointerException( QUEUE_FILTER );
        }

        String oldQueueFilter = getQueueFilter();

        // Tell any vetoers about the change. If anyone objects
        // we let the PropertyVetoException propagate back to
        // our caller.
        vetos.fireVetoableChange( QUEUE_FILTER, oldQueueFilter, queueFilter );

        // No one vetoed, make the change.
        NPCPSelOutQ selectionCP = (NPCPSelOutQ)getSelectionCP();
        selectionCP.setQueue(queueFilter);
        
        // Propagate change to ImplRemote if necessary...
        if (impl_ != null)
            impl_.setFilter("queue", queueFilter);

        // Notify any property change listeners.
        changes.firePropertyChange( QUEUE_FILTER, oldQueueFilter, queueFilter );
    }

} // OutputQueueList class

