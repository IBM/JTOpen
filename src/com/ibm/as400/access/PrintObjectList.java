///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PrintObjectList.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.Enumeration;
import java.util.Vector;
import java.io.IOException;
import java.io.UnsupportedEncodingException; // @B1A
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeSupport;
import java.beans.VetoableChangeListener;
import java.beans.PropertyVetoException;


/**
  * The  PrintObjectList class is an
  * abstract base class for the various types of network print object lists.
  *
  **/
public abstract class PrintObjectList
implements java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   
    static final long serialVersionUID = 4L;



    private static final String SYSTEM = "system";

    // These instance variable are persistent.
    private NPCPAttributeIDList attrsToRetrieve_ = null;
    private NPCPID idFilter_         = null;  // for certain lists an idcodepoint may be used to filter
    private NPCPSelection selection_ = null;  // selection filter
            AS400 system_            = null;  // The AS/400 object                  // @A5C
    private int typeOfObject_        = 0;     // The PrintObject object type
    private boolean listOutOfSync_   = false; // Is the list out of sync with the impl list? // @B2A
    private boolean objectAddedEvent_= false; // Has an object been added and event fired? // @B2A

    // These instance variables are not persistent, but private.
    private transient boolean open_;
    private transient Vector printObjectListListeners_; //@A3C - removed '= new()'
    private transient Vector theList_;          // @A5A
    
    // These instance variables are not persistent, but are package scope
    // to allow subclasses access to them... 
    transient PrintObjectListImpl   impl_;      // @A3A - the implementation to use
    transient PropertyChangeSupport changes;    // @A3C - removed '= new();'
    transient VetoableChangeSupport vetos;      // @A3C - removed '= new();'
    transient PrintObjectListListener dispatcher_; // @A5A
    
    
    
    /**
      * Constructs a  PrintObjectList object.
      * It is a non-externalized constructor for JavaBeans that allows null system.
      *
      * @param objectType One of the Network Print Server "objects" listed in
      *    the NPDataStream class such as NPDataStream.SPOOLED_FILE.
      * @param selectionCP A selection codepoint that will be used whenever the
      *    the list is built.
      **/
    PrintObjectList(int objectType,
                    NPCPSelection selectionCP)
    {
        typeOfObject_ = objectType;
        selection_ = selectionCP;
        initializeTransient();  // @A3A
    }



    /**
      * Constructs a  PrintObjectList object.
      * It is a non-externalized constructor.
      *
      * @param objectType One of the Network Print Server "objects" listed in
      *    the NPDataStream class such as NPDataStream.SPOOLED_FILE.
      * @param selectionCP A selection codepoint that will be used whenever the
      *    the list is built.
      * @param system The AS/400 on which the object(s) exists.
      **/
    PrintObjectList(int objectType,
                    NPCPSelection selectionCP,
                    AS400 system)
    {
        this(objectType, selectionCP);

        if( system == null ) {
            Trace.log(Trace.ERROR, "printObjectList: Parameter 'system' is null.");
            throw new NullPointerException(SYSTEM);
        }
        system_ = system;
    }



    /**
      *Adds the specified PrintObjectList listener to receive
      *PrintObjectList events from this print object list.
      *
      * @see #removePrintObjectListListener
      * @param listener The PrintObjectList listener.
      **/
    public /* @A5D synchronized*/ void addPrintObjectListListener( PrintObjectListListener listener )
    {        
        printObjectListListeners_.addElement( listener );
        // Add the listener to the impl_ for events fired from 
        // PrintObjectListImplRemote
        // @A5D if (impl_ != null)                              // @A3A
        // @A5D    impl_.addPrintObjectListListener(listener); // @A3A
    }



    /**
      *Adds the specified PropertyChange listener to receive
      *PropertyChange events from this print object list.
      *
      * @see #removePropertyChangeListener
      * @param listener The PropertyChange listener.
      **/
    public void addPropertyChangeListener( PropertyChangeListener listener )
    {
        changes.addPropertyChangeListener( listener );
    }



    /**
      *Adds the specified VetoableChange listener to receive
      *VetoableChange events from this print object list.
      *
      * @see #removeVetoableChangeListener
      * @param listener The VetoableChange listener.
      **/
    public void addVetoableChangeListener( VetoableChangeListener listener )
    {
        vetos.addVetoableChangeListener( listener );
    }



    /*
     * Chooses the appropriate implementation (Proxy or Remote).
     * Subclasses MUST supply the implementation to this method.
     **/
    abstract void chooseImpl();



    /**
      *Closes the list so that objects in the list can be garbage collected.
      **/
    public void close()
    {
        synchronized(this)
        {
            if (open_) {
                // ASSERT: if the list has been opened, impl_ has been assigned
                impl_.close(); // @A3A
                open_ = false;
            } else {
                // if the list was not open, return to avoid firing closed.
                return;
            }
        } 
    }
    
    

    // @A5A
    private PrintObjectListEvent createPrintObjectListEvent(int id, 
                                                            PrintObject printObject, 
                                                            Exception exception)
    {        
        PrintObjectListEvent event;

        // Now that we know we have listeners, we construct
        // the event object. We could have passed an event 
        // object to firePrintObjectList() but that would be
        // extra overhead if there were no listeners.
        if( exception !=null )
        {
            event = new PrintObjectListEvent(this, exception);
        }
        else if( printObject != null )
        {
            event = new PrintObjectListEvent(this, printObject);
        }
        else
        {
            event = new PrintObjectListEvent(this, id);
        }

        return event;
    }



    // @A5A
    private void firePrintObjectList(PrintObjectListEvent event)
    {
        event.setSource(this);

        // Return if no listeners are registered.
        if( printObjectListListeners_.isEmpty() )
        {
            return;
        }

        Vector l /* @A5D ;
        synchronized(this) { l*/ = (Vector)printObjectListListeners_.clone(); //}

        for( int i=0; i < l.size(); i++ )
        {
            switch( event.getID() )
            {
                // OBJECT_ADDED is the most frequent case.
                case PrintObjectListEvent.OBJECT_ADDED:                
                    PrintObject printObject = event.getObject();                                        // @B1A
                    theList_.addElement(printObject);                                                   // @B1A
                    objectAddedEvent_ = true;  // @B2A
                    ((PrintObjectListListener)l.elementAt(i)).listObjectAdded(event);
                    break;

                case PrintObjectListEvent.CLOSED:
                    ((PrintObjectListListener)l.elementAt(i)).listClosed(event);
                    break;

                case PrintObjectListEvent.COMPLETED:
                    ((PrintObjectListListener)l.elementAt(i)).listCompleted(event);
                    break;

                case PrintObjectListEvent.ERROR_OCCURRED:
                    ((PrintObjectListListener)l.elementAt(i)).listErrorOccurred(event);
                    break;

                case PrintObjectListEvent.OPENED:
                    ((PrintObjectListListener)l.elementAt(i)).listOpened(event);
                    break;
            }
        }
    }



    // @A5C
    // The JavaBeans 1.0 Specification strongly recommends to avoid
    // using a synchronized method to fire an event. We use a
    // synchronized block to locate the target listeners and then
    // call the event listeners from unsynchronized code.
    private void firePrintObjectList(int id,
                                     PrintObject printObject,
                                     Exception exception )
    {
        firePrintObjectList(createPrintObjectListEvent(id, printObject, exception));
    }



    /**
      * Returns one object from the list.
      *
      * @param index The index of the desired object.
      *
      * @exception ArrayIndexOutOfBoundsException If an invalid index is given.
      **/
    public /* @A5D synchronized*/ PrintObject getObject(int index)
    {
        if (!open_)
        {
            Trace.log(Trace.ERROR, "getObject: List has not been opened.");
                throw new ExtendedIllegalStateException(
                    ExtendedIllegalStateException.OBJECT_MUST_BE_OPEN);
        }
 
        // ASSERT: if the list has been opened, impl_ has been assigned
        // @A5D return impl_.getObject(index);                          // @A3A
        synchronized(theList_) {                                        // @A5A
        if ((theList_.size() > index) && ((listOutOfSync_ == false)     // @B2C
            || ((listOutOfSync_ == true) && (objectAddedEvent_ == true) // @B2A
                 && (theList_.size() == impl_.size()))))  {             // @B2A
            objectAddedEvent_ = false;                                  // @B2A     
            return (PrintObject) theList_.elementAt(index);             // @A5A
        }                                                               // @B2A
        else {                                                          // @A5A
            PrintObject npobject = null;                                // @A5A
            if (listOutOfSync_ == true) {                               // @B2A
                theList_.removeAllElements();                           // @B2A
                listOutOfSync_ = false;                                 // @B2A
            }                                                           // @B2A
            for (int i = theList_.size(); i <= index; ++i) {            // @A5A
                NPCPID cpid = impl_.getNPCPID(i);                       // @A5A
                NPCPAttribute cpattr = impl_.getNPCPAttribute(i);       // @A5A        
                try {                                                                               // @B1A
                    cpid.setConverter((new Converter(system_.getCcsid(), system_)).impl);           // @B1A
                    cpattr.setConverter((new Converter(system_.getCcsid(), system_)).impl);         // @B1A
                }                                                                                   // @B1A
                catch(UnsupportedEncodingException e) {                                             // @B1A
                    if (Trace.isTraceErrorOn())                                                     // @B1A
                        Trace.log(Trace.ERROR, "Error initializing converter for print object", e); // @B1A
                }                                                                                   // @B1A
                npobject = newNPObject(cpid, cpattr);                   // @A5A
                theList_.addElement(npobject);                          // @A5A
            }                                                           // @A5A
            return npobject;                                            // @A5A
        }                                                               // @A5A
        }                                                               // @A5A
    }



    /**
     * Returns an enumeration of the PrintObjects in the list.
     *
     **/
    public /* @A5D synchronized */ Enumeration getObjects()
    {
        if (!open_)
        {
            Trace.log(Trace.ERROR, "getObjects: List has not been opened.");
                throw new ExtendedIllegalStateException(
                    ExtendedIllegalStateException.OBJECT_MUST_BE_OPEN);
        }
        
        // ASSERT: if the list has been opened, impl_ has been assigned
        // @A5D Vector v = impl_.getObjects();          // @A3A
        // @A5D return v.elements();                    // @A3A

        // Force retrieval of all objects so far.       // @A5A
        getObject(impl_.size() - 1);                    // @A5A
        return theList_.elements();                     // @A5A
    }



    NPCPSelection getSelectionCP()
    {
        return selection_;
    }



    /**
     * Returns the AS/400 system name. This method is primarily provided for visual
     * application builders that support JavaBeans.
     *
     * @return The AS/400 system on which the objects in the list exist.
     **/
    final public AS400 getSystem()
    {
        return system_;
    }



    // @A3A - Added method
    private void initializeTransient()
    {
        impl_ = null;
        printObjectListListeners_ = new Vector();
        changes = new PropertyChangeSupport(this);
        vetos = new VetoableChangeSupport(this);
        theList_ = new Vector();                                    // @A5A
        open_ = false;

        // @A5A
        dispatcher_ = new PrintObjectListListener() {
            public void listClosed(PrintObjectListEvent event) { firePrintObjectList(event); }
            public void listCompleted(PrintObjectListEvent event) { firePrintObjectList(event); }
            public void listErrorOccurred(PrintObjectListEvent event) { firePrintObjectList(event); }
            public void listOpened(PrintObjectListEvent event) { firePrintObjectList(event); }
            public void listObjectAdded(PrintObjectListEvent event) { firePrintObjectList(event); }
        };
    }
    
    
    
    /**
     * Checks if a list that was opened asynchronously has completed.
     * If any exception occurred while the list was being retrieved, it will
     * be thrown here.
     *
     * @exception AS400Exception If the AS/400 system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ConnectionDroppedException If the connection is dropped unexpectedly.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with the AS/400.
     * @exception RequestNotSupportedException If the requested function is not supported because
     *                                         the AS/400 system is not at the correct level.
     * @return true if the list is completely built; false otherwise.
     **/
    public boolean isCompleted()
      throws  AS400Exception,
              AS400SecurityException,
              ConnectionDroppedException,
              ErrorCompletingRequestException,
              InterruptedException,
              IOException,
              RequestNotSupportedException
    {   
        if (!open_) {
            Trace.log(Trace.ERROR, "isCompleted: List has not been opened.");
                throw new ExtendedIllegalStateException(
                    ExtendedIllegalStateException.OBJECT_MUST_BE_OPEN);
        }
        // ASSERT: if the list has been opened, impl_ has been assigned
        return impl_.isCompleted();         // @A3A
    }



    // Not public!                                                              // @A5A
    abstract PrintObject newNPObject(NPCPID cpid, NPCPAttribute cpattr);        // @A5A



    /**
     * Builds the list asynchronously.  This method starts a thread 
     * to build the list and then returns. The caller may register 
     * listeners to obtain status about the list, or call isCompleted(), 
     * waitForItem(), or waitForListToComplete().
     **/
    public void openAsynchronously()
    {       
        synchronized (this) 
        {
            if (open_) {
                // list is already open.
                Trace.log(Trace.ERROR, "open: List is already opened.");
                throw new ExtendedIllegalStateException(
                    ExtendedIllegalStateException.OBJECT_CAN_NOT_BE_OPEN);
            }

            if (impl_ == null) {                                                // @A3A
                if (system_ == null) {                                          // @A3A 
                    // forewarn any listeners an error occurs.                  // @A3A
                    Exception e = new ExtendedIllegalStateException("system", 
                        ExtendedIllegalStateException.PROPERTY_NOT_SET);        // @A3A
                    firePrintObjectList(PrintObjectListEvent.ERROR_OCCURRED, null, e); // @A3A
                }                                                               // @A3A
                chooseImpl();                                                   // @A3A
            }                                                                   // @A3A

            if (!getSystem().isThreadUsed()) {                                  // @A2A
                // Our AS400 object says do not start threads.The                  @A2A
                // application should be using openSynchronously().                @A2A
                Trace.log(Trace.ERROR, "open: Threads can not be started.");    // @A2A
                throw new ExtendedIllegalStateException(                        // @A2A
                    ExtendedIllegalStateException.OBJECT_CAN_NOT_START_THREADS);// @A2A
            }                                                                   // @A2A

            open_ = true;           // @A3A
            listOutOfSync_ = true;  // @B2A
            impl_.openAsynchronously();                                         // @A3A
        }
    }
     


   /**
    * Builds the list synchronously. This method will not
    * return until the list has been built completely.
    * The caller may then call the getObjects() method
    * to get an enumeration of the list.
    *
    * @exception AS400Exception If the AS/400 system returns an error message.
    * @exception AS400SecurityException If a security or authority error occurs.
    * @exception ConnectionDroppedException If the connection is dropped unexpectedly.
    * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
    * @exception InterruptedException If this thread is interrupted.
    * @exception IOException If an error occurs while communicating with the AS/400.
    * @exception RequestNotSupportedException If the requested function is not supported because the
    *              AS/400 system is not at the correct level.
    **/
    public void openSynchronously()
     throws  AS400Exception,
             AS400SecurityException,
             ConnectionDroppedException,
             ErrorCompletingRequestException,
             InterruptedException,
             IOException,
             RequestNotSupportedException
    {
        synchronized(this)                                                  // @A2A
        {                                                                   // @A2A
            if( open_ ) {                                                   // @A2A
                // list is already open.                                    // @A2A
                Trace.log(Trace.ERROR, "open: List is already opened.");    // @A2A
                throw new ExtendedIllegalStateException(                    // @A2A
                    ExtendedIllegalStateException.OBJECT_CAN_NOT_BE_OPEN);  // @A2A
            }                                                               // @A2A 
               
            if (impl_ == null) {                                            // @A3A
               if (system_ == null) {                                       // @A3A
                    // forewarn any listeners an error occurs.              // @A3A
                    Exception e = new ExtendedIllegalStateException("system",
                        ExtendedIllegalStateException.PROPERTY_NOT_SET);    // @A3A
                    firePrintObjectList(PrintObjectListEvent.ERROR_OCCURRED, null, e); // @A3A 
                }                                                           // @A3A
                chooseImpl();                                               // @A3A
            }                                                               // @A3A
            open_ = true;           // @A3A
            listOutOfSync_ = true;  // @B2A
            impl_.openSynchronously();                                      // @A3A
        }
    }



    // We need to initialize our transient and static data when
    // the object is de-serialized. static final data is OK.
    private void readObject(java.io.ObjectInputStream in)
      throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();  
        initializeTransient();  // @A3A
    }


    
    /**
      *Removes the specified PrintObjectList listener
      *so that it no longer receives PrintObjectList events
      *from this print object list.
      *
      * @see #addPrintObjectListListener
      * @param listener The PrintObjectList listener.
      **/
    public /* @A5D synchronized */ void removePrintObjectListListener( PrintObjectListListener listener )
    {
        printObjectListListeners_.removeElement(listener);
        // Remove the listener from the impl_  - no longer monitoring for events 
        // fired from PrintObjectListImplRemote
        // @A5D if (impl_ != null)                                      // @A3A
        // @A5D     impl_.removePrintObjectListListener(listener);      // @A3A
    }



    /**
      *Removes the specified PropertyChange listener
      *so that it no longer receives PropertyChange events
      *from this print object list.
      *
      * @see #addPropertyChangeListener
      * @param listener The PropertyChange listener.
      **/
    public void removePropertyChangeListener( PropertyChangeListener listener )
    {
        changes.removePropertyChangeListener(listener);
    }



    /**
      *Removes the specified VetoableChange listener
      *so that it no longer receives VetoableChange events
      *from this print object list.
      *
      * @see #addVetoableChangeListener
      * @param listener The VetoableChange listener.
      **/
    public void removeVetoableChangeListener( VetoableChangeListener listener )
    {
        vetos.removeVetoableChangeListener(listener);
    }



    /**
     * Resets the list of object attributes to retrieve.
     **/
    public void resetAttributesToRetrieve()
    { 
        if (open_)
        {
                Trace.log(Trace.ERROR, "resetAttributesToRetrieve: List is already opened.");
                throw new ExtendedIllegalStateException(
                    ExtendedIllegalStateException.OBJECT_CAN_NOT_BE_OPEN);
        } 
        else {
            attrsToRetrieve_ = null;
            if (impl_ != null)                      // @A3A          
                impl_.resetAttributesToRetrieve();  // @A3A   
        }
    }



    /**
     * Resets the list filter back to default values.
     **/
    public void resetFilter()
    {
        if (open_)
        {
            Trace.log(Trace.ERROR, "resetFilter: List is already opened.");
            throw new ExtendedIllegalStateException(
                ExtendedIllegalStateException.OBJECT_CAN_NOT_BE_OPEN);
        }  
        else {
            selection_.reset();
            idFilter_ = null;      // effectively resets the id Codepoint filter;
            if (impl_ != null)                      // @A3A
                impl_.resetFilter();                // @A3A
        }
    }

 

    /**
     * Sets the attributes of the object that should be returned in the list.
     * This method can be used to speed up the listing if
     * only interested in a few attributes for each item in the list.
     *
     * @param attributes An array of attribute IDs that define which
     *   object attributes will be retrieved for each item in the list
     *   when the list is opened.
     *
     * @see PrintObject
     **/
    public synchronized void setAttributesToRetrieve(int[] attributes)
    {   
        // check params
	    if (attributes == null)
	    {
            Trace.log(Trace.ERROR, "setAttributesToRetrieve: Parameter 'attributes' is null.");
	        throw new NullPointerException("attributes");
	    }

	    // check state of list
	    if (open_) 
	    {
            Trace.log(Trace.ERROR, "setAttributesToRetrieve: List is already opened.");
            throw new ExtendedIllegalStateException(
                ExtendedIllegalStateException.OBJECT_CAN_NOT_BE_OPEN);
	    }

	    if (attrsToRetrieve_ != null)
	    {
	        attrsToRetrieve_.reset();
	    }
	    else {
	        attrsToRetrieve_ = new NPCPAttributeIDList();
	    }

	    for (int i = 0; i<attributes.length; i++)
	    {
	        attrsToRetrieve_.addAttrID(attributes[i]);
	    }
	    
	    if (impl_ != null)
	        impl_.setAttributesToRetrieve(attributes);
    }
       
      

    /**
     * Sets the filter by object ID code point.
     * Protected method that should be called by the subclasses that
     * can filter based on an object ID.
     **/
    void setIDCodePointFilter(NPCPID cpID)
    {
	    idFilter_ = cpID;
	    if (impl_ != null)
	        impl_.setIDCodePointFilter(cpID);
    }



    // @A3A - added method
    /**
     * This method is provided specifically for subclasses to invoke in the chooseImpl() method.
     * It registers the PrintObject listeners with the implementation (proxy and/or remote) and
     * sets the system, attributes to retrieve, codepoint ID filter,
     * selection codepoint, and PrintObject type (of the list) 
     **/
    void setImpl()
    {
        int count = printObjectListListeners_.size();
        // @A5D for (int i = 0; i < count; i++) {
        // @A5D     impl_.addPrintObjectListListener((PrintObjectListListener) printObjectListListeners_.elementAt(i));
        // @A5D }
        try {                                       // @A4A
            system_.connectService(AS400.PRINT);    // @A4A
            impl_.setSystem(system_.getImpl());     // @A4A
            impl_.addPrintObjectListListener(dispatcher_); // @A5A
        }                                           // @A4A
        catch (Exception e) {                       // @A4A 
            Trace.log(Trace.ERROR, "Error occurred connecting to AS/400 Print service.");  // @A4A
        }                       // @A4A
        impl_.setPrintObjectListAttrs(attrsToRetrieve_, idFilter_, 
                                      selection_, typeOfObject_);
    }
    
  
  
    /**
     * Sets the AS/400 system name. This method is primarily provided for
     * visual application builders that support JavaBeans. Application
     * programmers should specify the AS/400 system in the constructor
     * for the specific network print object list. For example,
     * SpooledFileList myList = new SpooledFileList(mySystem).
     *
     * @param system The AS/400 system name.
     *
     * @exception PropertyVetoException If the change is vetoed.
     *
     **/
    final public void setSystem(AS400 system)
      throws PropertyVetoException
    {
        if( system == null )
        {
            Trace.log(Trace.ERROR, "setSystem: Parameter 'system' is null.");
            throw new NullPointerException("system");
        }

        AS400 oldSystem = getSystem();

        // Tell any vetoers about the change. If anyone objects
        // we let the PropertyVetoException propagate back to
        // our caller.
        vetos.fireVetoableChange( SYSTEM, oldSystem, system );

        // No one vetoed, but if the list is open don't allow
        // the system to be changed.
        synchronized(this)
        {
            if( open_ )
            {
                Trace.log(Trace.ERROR, "setSystem: List is already opened.");
                throw new ExtendedIllegalStateException(
                    ExtendedIllegalStateException.OBJECT_CAN_NOT_BE_OPEN);
            }

            // Make the change.
            system_ = system;
            
            // Propagate the change to the ImplRemote if necessary...
            if (impl_!= null)                       // @A3A
                impl_.setSystem(system.getImpl());  // @A4C
        }

        // Notify any property change listeners.
        changes.firePropertyChange( SYSTEM, oldSystem, system );
    }



    /**
     * Returns the current size of the list.
     **/
    public synchronized int size()
    {
	    if (!open_) {
            Trace.log(Trace.ERROR, "size: List has not been opened.");
            throw new ExtendedIllegalStateException(
                ExtendedIllegalStateException.OBJECT_MUST_BE_OPEN);
	    }
	    
	    // ASSERT: if the list has been opened, impl_ has been assigned
	    return impl_.size();
    }



    /**
     * Blocks until the number of requested items are done being built.
     *
     * @param itemNumber The number of items to wait for before returning.
     *        Must be greater than 0;
     *
     * @exception AS400Exception If the AS/400 system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ConnectionDroppedException If the connection is dropped unexpectedly.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with the AS/400.
     * @exception RequestNotSupportedException If the requested funtion is not supported because the AS/400
     *                                         system is not at the correct level.
     **/
    public /*synchronized*/ void waitForItem(int itemNumber)
      throws  AS400Exception,
              AS400SecurityException,
              ConnectionDroppedException,
              ErrorCompletingRequestException,
              InterruptedException,
              IOException,
              RequestNotSupportedException
    {   
        if (!open_)
        {
            Trace.log(Trace.ERROR, "waitForItem: List has not been opened.");
                throw new ExtendedIllegalStateException(
                    ExtendedIllegalStateException.OBJECT_MUST_BE_OPEN);
        }
        // check to see that a valid number was passed.
        if (itemNumber < 1)
        {
            Trace.log(Trace.ERROR, "waitForItem: Parameter 'itemNumber' is less than 1.");
            throw new ExtendedIllegalArgumentException(
                    "itemNumber("+itemNumber+")",
                    ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }
        
        // ASSERT: if the list has been opened, impl_ has been assigned
        impl_.waitForItem(itemNumber);
    }



    /**
     * Blocks until the list is done being built.
     *
     * @exception AS400Exception If the AS/400 system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ConnectionDroppedException If the connection is dropped unexpectedly.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with the AS/400.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                    AS/400 system is not at the correct level.
     **/
    public /*synchronized*/ void waitForListToComplete()
      throws  AS400Exception,
              AS400SecurityException,
              ConnectionDroppedException,
              ErrorCompletingRequestException,
              InterruptedException,
              IOException,
              RequestNotSupportedException
    {   
        if (!open_) 
        {
            Trace.log(Trace.ERROR, "waitForListToComplete: List has not been opened.");
                throw new ExtendedIllegalStateException(
                    ExtendedIllegalStateException.OBJECT_MUST_BE_OPEN);
        } 
        
        // ASSERT: if the list has been opened, impl_ has been assigned
        impl_.waitForListToComplete();
    }
    
}  // PrintObjectList class

