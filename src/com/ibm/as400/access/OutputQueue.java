///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: OutputQueue.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.util.Vector;
import java.beans.PropertyVetoException;

/**
 * The OutputQueue class represents an AS/400 output queue.
 * An instance of this class can be used to manipulate an individual
 * AS/400 output queue (hold, release, clear, and so on).
 *
 * See <a href="OutputQueueAttrs.html">Output Queue Attributes</a> for
 * valid attributes.
 *
 **/

public class OutputQueue extends PrintObject
implements java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    
    private static final String PATH                = "path";

    transient private Vector outputQueueListeners_  = new Vector();


    // constructor used internally (not externalized since it takes
    // an ID code point
    OutputQueue(AS400 system, NPCPIDOutQ id, NPCPAttribute attrs)
    {
	    super(system, id, attrs, NPConstants.OUTPUT_QUEUE); // @B1C
    }



    /**
     * Constructs an OutputQueue object. The AS/400 system and the
     * integrated file system name of the output queue must be set
     * later. This constructor is provided for visual application
     * builders that support JavaBeans. It is not intended for use
     * by application programmers.
     *
     * @see PrintObject#setSystem
     * @see #setPath
     **/
    public OutputQueue()
    {
        super(null, null, NPConstants.OUTPUT_QUEUE); // @B1C

        // Because of this constructor we will need to check the
        // run time state of OutputQueue objects.
    }



    /**
     * Constructs an OutputQueue object. It uses the specified system and
     * output queue name that identifies it on that system.
     *
     * @param system The AS/400 on which this output queue exists.
     * @param queueName The integrated file system name of the output queue. The format of
     * the queue string must be in the format of /QSYS.LIB/libname.LIB/queuename.OUTQ.
     *
     **/
    public OutputQueue(AS400 system,
		       String queueName)
    {
        super(system, buildIDCodePoint(queueName), null, NPConstants.OUTPUT_QUEUE); // @B1C
        // base class constructor checks for null system.
        // QSYSObjectPathName() checks for a null queueName.
    }



    /**
      *Adds the specified OutputQueue listener to receive
      *OutputQueue events from this OutputQueue.
      *
      * @see #removeOutputQueueListener
      * @param listener The OutputQueue listener.
      **/
    public void addOutputQueueListener( OutputQueueListener listener )
    {
        outputQueueListeners_.addElement(listener);
    }



    private static NPCPIDOutQ buildIDCodePoint(String IFSQueueName)
    {
	    QSYSObjectPathName ifsPath = new QSYSObjectPathName(IFSQueueName, "OUTQ");

	    return new NPCPIDOutQ(ifsPath.getObjectName(), ifsPath.getLibraryName());
    }



    // Check the run time state
    void checkRunTimeState()
    {
        // check whatever the base class needs to check
        super.checkRunTimeState();

        // OutputQueue's need to additionally check the IFS pathname.
        if( getIDCodePoint() == null )
        {
            Trace.log(Trace.ERROR, "Parameter 'path' has not been set.");
            throw new ExtendedIllegalStateException(
            PATH, ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }
    }



    // A1A - Added function
    /**
     * Chooses the implementation
     **/
    void chooseImpl()
    throws IOException, AS400SecurityException                              // @B1A
    {
        // We need to get the system to connect to...
        AS400 system = getSystem();
        if (system == null) {
            Trace.log( Trace.ERROR, "Attempt to use OutputQueue before setting system." );
            throw new ExtendedIllegalStateException("system",
                                    ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        impl_ = (OutputQueueImpl) system.loadImpl2("com.ibm.as400.access.OutputQueueImplRemote",
                                                   "com.ibm.as400.access.OutputQueueImplProxy");
        super.setImpl();
    }



    /**
     * Clears the output queue on the AS/400.
     *
     * @param clearOptions A PrintParameterList object that may have any of the
     *        following attributes set:
     * <UL>
     *   <LI> <A HREF="PrintAttributes.html#HDRKEY60>
     *        ATTR_JOBUSER</A> - Clear output queue by a user id.  May be a specific userid, "*ALL" or
     *                       "*CURRENT".  "*CURRENT" is the default.
     *   <LI> <A HREF="PrintAttributes.html#HDRKEY45>
     *        ATTR_FORMTYPE</A> - Clear output queue by a form type.  May be a specific form type, "*ALL" or
     *                        "*STD".  "*ALL" is the default.
     *   <LI> <A HREF="PrintAttributes.html#HDRKEY119>
     *        ATTR_USERDATA</A> - Clear output queue by user data.  May be a specific user data or "*ALL".
     *                        "*ALL" is the default.
     * </UL>
     *  clearOptions may be null.
     *
     * @exception AS400Exception If the AS/400 system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the AS/400.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                         AS/400 system is not at the correct level.
     **/
    public void clear(PrintParameterList clearOptions)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        checkRunTimeState();

        if (impl_ == null)                              // @A1A
            chooseImpl();                               // @A1A
        ((OutputQueueImpl) impl_).clear(clearOptions);  // @A1A
        // update the attrs, since updateAttrs was      // @A1A
        // called on the remote side...                 // @A1A
        attrs = impl_.getAttrValue();                   // @A1A
                                                        
        fireOutputQueueEvent(OutputQueueEvent.CLEARED);

    } // end clear



    // The JavaBeans 1.0 Specification strongly recommends to avoid
    // using a synchronized method to fire an event. We use a
    // synchronized block to locate the target listeners and then
    // call the event listeners from unsynchronized code.
    private void fireOutputQueueEvent( int id )
    {
        // Return if no listeners are registered.
        if( outputQueueListeners_.isEmpty() )
        {
            return;
        }

        Vector l;
        OutputQueueEvent event = new OutputQueueEvent(this, id);

        synchronized(this) { l = (Vector)outputQueueListeners_.clone(); }

        for( int i=0; i < l.size(); i++ )
        {
            switch(id)
            {
                case OutputQueueEvent.CLEARED:
                    ((OutputQueueListener)l.elementAt(i)).outputQueueCleared(event);
                    break;

                case OutputQueueEvent.HELD:
                    ((OutputQueueListener)l.elementAt(i)).outputQueueHeld(event);
                    break;

                case OutputQueueEvent.RELEASED:
                    ((OutputQueueListener)l.elementAt(i)).outputQueueReleased(event);
                    break;
            }
        }
    }



    /**
     * Returns the name of the output queue.
     *
     * @return The name of the output queue.
     **/
    public String getName()
    {
        NPCPID IDCodePoint = getIDCodePoint();

        if( IDCodePoint == null ) {
            return EMPTY_STRING; // ""
        } else {
            return IDCodePoint.getStringValue(ATTR_OUTQUE);
        }
    }



    /**
     * Returns the integrated file system pathname of the output queue.
     *
     * @return The integrated file system pathname of the output queue.
     **/
    public String getPath()
    {
        NPCPID IDCodePoint = getIDCodePoint();

        if( IDCodePoint == null ) {
            return EMPTY_STRING; // ""
        } else {
            return QSYSObjectPathName.toPath(
              IDCodePoint.getStringValue(ATTR_OUTQUELIB), // library name
              IDCodePoint.getStringValue(ATTR_OUTQUE),    // queue name
              "OUTQ" );                                   // type
        }
    }



    /**
     * Holds the output queue on the AS/400.
     *
     * @exception AS400Exception If the AS/400 system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the AS/400.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                         AS/400 system is not at the correct level.
     **/
    public void hold()
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        checkRunTimeState();

        if (impl_ == null)                              // @A1A
            chooseImpl();                               // @A1A
        ((OutputQueueImpl) impl_).hold();               // @A1A
        // update the attrs, since updateAttrs was      // @A1A
        // called on the remote side...                 // @A1A
        attrs = impl_.getAttrValue();                   // @A1A
  
        fireOutputQueueEvent(OutputQueueEvent.HELD);

    } // end hold



    // We need to initialize our transient and static data when
    // the object is de-serialized. static final data is OK.
    private void readObject(java.io.ObjectInputStream in)
      throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        outputQueueListeners_ = new Vector();
    }



    /**
     * Releases a held output queue on the AS/400.
     *
     * @exception AS400Exception If the AS/400 system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the AS/400.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                         AS/400 system is not at the correct level.
     **/
    public void release()
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        checkRunTimeState();

        if (impl_ == null)                              // @A1A
            chooseImpl();                               // @A1A
        ((OutputQueueImpl) impl_).release();            // @A1A
        // update the attrs, since updateAttrs was      // @A1A
        // called on the remote side...                 // @A1A
        attrs = impl_.getAttrValue();                   // @A1A
  
        fireOutputQueueEvent(OutputQueueEvent.RELEASED);

    } // end release



    /**
      *Removes the specified OutputQueue listener
      *so that it no longer receives OutputQueue events
      *from this OutputQueue.
      *
      * @see #addOutputQueueListener
      * @param listener The OutputQueue listener.
      **/
    public void removeOutputQueueListener( OutputQueueListener listener )
    {
        outputQueueListeners_.removeElement(listener);
    }



    /**
     * Sets the integrated file system pathname of the output queue.
     *
     * @param path The integrated file system pathname of the output queue. The format of
     * the queue string must be in the format of /QSYS.LIB/libname.LIB/queuename.OUTQ.
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
            Trace.log(Trace.ERROR, "Cannot set property 'Path' after connect.");    // @A1A
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

} // end OutputQueue class
