///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PrintObjectListImplRemote.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.Vector;
import java.io.IOException;


/**
 * The PrintObjectList class is an abstract base class for the various types of network print object lists.
**/
abstract class PrintObjectListImplRemote implements PrintObjectListImpl, Runnable
{
  private static final String copyright = "Copyright (C) 1997-2002 International Business Machines Corporation and others.";


  // These instance variable are persistent.
  private NPCPAttributeIDList attrsToRetrieve_;
  private NPCPID idFilter_;          // for certain lists an idcodepoint may be used to filter
  private NPCPSelection selection_;  // selection filter
  private AS400ImplRemote system_;   // @A4 - changed from AS400 to AS400ImplRemote
  private int typeOfObject_ = 0;     // indicates object type

  // These instance variables are not persistent.
  private transient boolean completed_ = false;
  private transient boolean closed_ = false;
  private final Object closeLock_ = new Object();
  private transient boolean waiting_ = false;
  private final Object waitLock_ = new Object();
  private transient int waitingForItem_ = -1;
  private transient Vector cpidList_ = null;                 // @A5A
  private transient Vector cpattrList_ = null;               // @A5A
  private transient int numItems_ = 0;

  private transient Exception anyException_ = null;
  private transient Vector printObjectListListeners_ = null;

  private boolean useCache_ = true;

  /**
    *Adds the specified PrintObjectList listener to receive
    *PrintObjectList events from this print object list.
    *
    * @see #removePrintObjectListListener
    * @param listener The PrintObjectList listener.
    **/
  public void addPrintObjectListListener(PrintObjectListListener listener)
  {
    if (printObjectListListeners_ == null) printObjectListListeners_ = new Vector();
    synchronized(printObjectListListeners_)
    {
      printObjectListListeners_.addElement(listener);
    }
  }


  /**
   * This method does all of the datastream work and object creations.
  **/
  private void buildList()
  throws AS400Exception,
  AS400SecurityException,
  ErrorCompletingRequestException,
  InterruptedException, 
  IOException,
  RequestNotSupportedException
  {

    NPSystem npSystem = NPSystem.getSystem(getSystem());
    if (npSystem != null)
    {
      NPConversation conversation = npSystem.getConversation();
      try
      {
        if (conversation != null)
        {
          boolean     fTossData = false;
          boolean     fMoreData = true;
          int         count     = 0;
          // @B1D int         ccsid = conversation.getHostCCSID();
          ConverterImpl converter = conversation.getConverter();        // @B1A
          AS400Server server = conversation.getServer();
          int correlation = server.newCorrelationId();
          NPDataStream request = new NPDataStream(typeOfObject_);
          NPDataStream reply;
          request.setCorrelation(correlation);
          request.setAction(NPDataStream.LIST);
          // @B1D request.setHostCCSID(ccsid);
          request.setConverter(converter);                              // @B1A
          if (selection_ != null)
          {
            request.addCodePoint(selection_);
          }
          if (idFilter_ != null)
          {
            request.addCodePoint(idFilter_);
          }
          if (attrsToRetrieve_ != null)
          {
            request.addCodePoint(attrsToRetrieve_);
          }
          else
          {
            request.addCodePoint(getDefaultAttrsToList());
          }
          server.send(request, correlation);
          do
          {
            reply = (NPDataStream)server.receive(correlation);
            if (reply == null)
            {
              // throw execption - internal error.
              fMoreData = false;   
              Trace.log(Trace.ERROR, "buildList: Null reply from AS400Server.receive()!");
              throw new InternalErrorException(InternalErrorException.PROTOCOL_ERROR);
            }
            else
            {
              count++;
              fMoreData = !(reply.isLastReply());
              if (!fTossData)
              {
                // we are going to keep this reply so set the
                // ccsid so it can tranlsate its text correctly
                // @B1D reply.setHostCCSID(ccsid);
                reply.setConverter(converter);                            // @B1A
                int rc = reply.getReturnCode();
                if (rc == NPDataStream.RET_OK)
                {
                  // @A5D PrintObject npObject = newNPObject(getSystem(), reply);
                  NPCPID cpid = newNPCPID(reply);                        // @A5A
                  NPCPAttribute cpattr = newNPCPAttribute(reply);        // @A5A
                  if (cpid != null)                                      // @A5C
                  {
                    synchronized(closeLock_) // In case someone tries to close us before we're finished.
                    {
                      if (closed_ || completed_) // Don't add the item if we've been closed already.
                      {
                        // the list has been closed before
                        // it completed, start tossing any
                        // incoming data...
                        fTossData = true;
                      }
                      else
                      {
                        if (useCache_) cpidList_.addElement(cpid);
                        if (useCache_) cpattrList_.addElement(cpattr);
                        ++numItems_;
                        // tell any listeners an object was added to the list.                  // @A2A
                        firePrintObjectList(PrintObjectListEvent.OBJECT_ADDED, cpid, cpattr, null); // @A5A

                        if (waiting_ && waitingForItem_ <= numItems_ && waitingForItem_ > -1)
                        {
                          // Only notify if someone is waiting to be notified.
                          synchronized(waitLock_)
                          {
                            waitLock_.notifyAll();
                          }
                        }
                      }
                    }
                  }
                }
                else
                {
                  if (rc == NPDataStream.RET_CPF_MESSAGE)
                  {
                    NPCPAttribute cpCPFMessage = (NPCPAttribute)reply.getCodePoint(NPCodePoint.ATTRIBUTE_VALUE);
                    if (cpCPFMessage != null)
                    {
                      String strCPFMessageID = cpCPFMessage.getStringValue(PrintObject.ATTR_MSGID);
                      String strCPFMessageText = cpCPFMessage.getStringValue(PrintObject.ATTR_MSGTEXT);
                      String strCPFMessageHelp = cpCPFMessage.getStringValue(PrintObject.ATTR_MSGHELP);
                      if (Trace.traceOn_) Trace.log(Trace.ERROR, "buildList: CPF Message("+strCPFMessageID+") = " + strCPFMessageText + ", HelpText= " +strCPFMessageHelp); //@CRS
                      // Create an AS400Message object
                      AS400Message msg = new AS400Message(strCPFMessageID, strCPFMessageText);
                      msg.setHelp(strCPFMessageHelp);
                      AS400Exception e = new AS400Exception(msg);
                      // throw an exception containing our CPF message.
                      // our catcher will actually fire the error event.
                      throw e;
                    }
                  }
                  else
                  {
                    if (rc == NPDataStream.RET_EMPTY_LIST)
                    {
                      // the list is empty, that isn't an error.
                    }
                    else
                    {
                      // look at RC and throw appropriate exception
                      Trace.log(Trace.ERROR, "buildList: Host Return Code" + rc);
                      // we get back a 4 (INV_REQ_ACT) if we try to list
                      // AFP Resources on pre V3R7 systems..
                      if (rc == NPDataStream.RET_INV_REQ_ACT)
                      {
                        throw new RequestNotSupportedException(conversation.getAttribute(PrintObject.ATTR_NPSLEVEL),
                                                               RequestNotSupportedException.SYSTEM_LEVEL_NOT_CORRECT);
                      }
                      else
                      {
                        throw new ErrorCompletingRequestException(ErrorCompletingRequestException.AS400_ERROR,
                                                                  "QNPSERVS RC = " + rc);
                      }
                    }
                  }
                }
              }
            }
            // every 15 datastreams that we get we'll call to the
            // garbage collector to keep the VM from running out of
            // of memory
//@C0 - This is a serious performance bottleneck and should no longer be
//      necessary under today's "modern" JVMs.
//@C0D                      if ((count % 15) == 0)
//@C0D                      {
//@C0D                         System.gc();
//@C0D                      }
          } while (fMoreData);
        }
      }
      finally
      {
        npSystem.returnConversation(conversation);
      }
    }
  }



  // The sub classes have default constructors implemented
  // for JavaBean support in visual builders. We need to
  // check the run time state of the object. The sub classes
  // may add additional checks by having their own
  // checkRunTimeState(), but will call super.checkRunTimeState()
  // to get this check.
  void checkRunTimeState()
  {
    if (getSystem() == null)
    {
      Trace.log(Trace.ERROR, "checkRunTimeState: Parameter 'system' has not been set.");
      throw new ExtendedIllegalStateException(
                                             "system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }
  }



  /**
   * Closes the list so that objects in the list can be garbage collected.
  **/
  public void close()
  {
    synchronized(closeLock_)
    {
      closed_ = true;
      cpidList_ = null;               // @A5A
      cpattrList_ = null;             // @A5A
      numItems_ = 0; //@CRS
      anyException_ = null;
    } 

    // tell any listeners the list was closed.
    firePrintObjectList(PrintObjectListEvent.CLOSED, null, null, null); // @A5C

    // At this point, all other threads that are using us should not be
    // firing events.
  }



  // The JavaBeans 1.0 Specification strongly recommends to avoid
  // using a synchronized method to fire an event. We use a
  // synchronized block to locate the target listeners and then
  // call the event listeners from unsynchronized code.
  private void firePrintObjectList(int id,
                                   NPCPID cpid,                       // @A5A
                                   NPCPAttribute cpattr,              // @A5A
                                   Exception exception )
  {
    // Return if no listeners are registered.
    if (printObjectListListeners_ == null) //@CRS
    {
      return;
    }

    PrintObjectListEvent event;

    // Now that we know we have listeners, we construct
    // the event object. We could have passed an event 
    // oject to firePrintObjectList() but that would be
    // extra overhead if there were no listeners.
    if (exception !=null)
    {
      event = new PrintObjectListEvent(this, exception);
    }
    else if (cpid != null)                                      // @A5D
    {
      event = new PrintObjectListEvent(this, cpid, cpattr);    // @A5D
    }
    else
    {
      event = new PrintObjectListEvent(this, id);
    }

    synchronized(printObjectListListeners_) // We don't want listeners being added/removed while we're firing an event.
    {
      int size = printObjectListListeners_.size();

      for (int i=0; i < size; i++)
      {
        switch (id)
        {
          // OBJECT_ADDED is the most frequent case.
          case PrintObjectListEvent.OBJECT_ADDED:
            ((PrintObjectListListener)printObjectListListeners_.elementAt(i)).listObjectAdded(event);
            break;

          case PrintObjectListEvent.CLOSED:
            ((PrintObjectListListener)printObjectListListeners_.elementAt(i)).listClosed(event);
            break;

          case PrintObjectListEvent.COMPLETED:
            ((PrintObjectListListener)printObjectListListeners_.elementAt(i)).listCompleted(event);
            break;

          case PrintObjectListEvent.ERROR_OCCURRED:
            ((PrintObjectListListener)printObjectListListeners_.elementAt(i)).listErrorOccurred(event);
            break;

          case PrintObjectListEvent.OPENED:
            ((PrintObjectListListener)printObjectListListeners_.elementAt(i)).listOpened(event);
            break;
        }
      }
    }
  }



  /**
    * Non-externalized abstract method that the child-classes implement to
    * set the default attributes to retrieve on the list
    **/
  abstract NPCPAttributeIDList getDefaultAttrsToList();


  // @A5A
  public synchronized NPCPID getNPCPID(int index)
  {
    return(NPCPID)cpidList_.elementAt(index);
  }

  // @A5A
  public synchronized NPCPAttribute getNPCPAttribute(int index)
  {
    return(NPCPAttribute)cpattrList_.elementAt(index);
  }


  // This method is called by subclassed ImplRemotes
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
  final public AS400ImplRemote getSystem()  // @A4C Changed from AS400
  {
    return system_;
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
    if (anyException_ != null)
    {
      rethrowException();
    }

    return completed_;
  }



  /**
   * Non-externalized abstract method that child-classes implement to
   * create the correct type of object from the reply datastream
   **/
  abstract NPCPID newNPCPID(NPDataStream reply);                      // @A5A



  // @A5A
  NPCPAttribute newNPCPAttribute(NPDataStream reply)
  {
    return(NPCPAttribute)reply.getCodePoint(NPCodePoint.ATTRIBUTE_VALUE);   // may return null
  }


  /**
   * Builds the list asynchronously.  This method starts a thread 
   * to build the list and then returns. The caller may register 
   * listeners to obtain status about the list, or call isCompleted(), 
   * waitForItem(), or waitForListToComplete().
   **/
  public void openAsynchronously()
  {
    checkRunTimeState();

    // Start thread to build the list. We are runnable, and
    // so our run() method just calls openSynchronously().
    Thread t = new Thread(this);
    t.start();
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
    // check the run time state of the object. To support JavaBeans
    // the sub-classes provide null constructors.
    checkRunTimeState();

    AS400ImplRemote theSystem = getSystem(); // @A4C - changed to AS400ImplRemote   // @A1A
    if (theSystem != null)
    {                                           // @A1A
      NPSystem npSystem = NPSystem.getSystem(theSystem);              // @A1A 
      if (npSystem != null)                                           // @A1A
      {
        // @A1A
        NPConversation conversation = npSystem.getConversation();    // @A1A
        npSystem.returnConversation(conversation);                   // @A1A
      }                                                               // @A1A
    }                                                                  // @A1A

    cpidList_ = new Vector();                                       // @A5A
    cpattrList_ = new Vector();                                     // @A5A
    numItems_ = 0;
    completed_ = false;                                             // @A2A
    anyException_ = null;                                           // @A2A

    // tell any listeners the list was opened.                         // @A2A
    firePrintObjectList(PrintObjectListEvent.OPENED, null, null, null); // @A2A @A5C

    try                                                                // @A2A
    {
      buildList();
    }                                                                  // @A2A
    catch (Exception e)                                               // @A2A
    {
      anyException_ = e;                                           // @A2A

      // tell any listeners an error occurred.                        // @A2A
      firePrintObjectList(PrintObjectListEvent.ERROR_OCCURRED, null, null, e); // @A2A @A5C
    }                                                                  // @A2A
    finally                                                            // @A2A
    {
      // tell any listeners the list completed.                       // @A2A
      firePrintObjectList(PrintObjectListEvent.COMPLETED, null, null, null);  // @A2A @A5C
      synchronized(waitLock_) // Notify anyone who is waiting for an item or the list to complete.
      {
        completed_ = true;
        waitLock_.notifyAll();
      }
    }                                                                  // @A2A

    // we caught the exception above so that we could notify any       // @A2A
    // listeners. Now, rethrow the exception for applications          // @A2A
    // that may not be using listeners.                                // @A2A
    if (anyException_ != null)                                        // @A2A
    {
      // @A2A
      rethrowException();                                             // @A2A
    }                                                                  // @A2A
  }    



  /**
    *Removes the specified PrintObjectList listener
    *so that it no longer receives PrintObjectList events
    *from this print object list.
    *
    * @see #addPrintObjectListListener
    * @param listener The PrintObjectList listener.
    **/
  public void removePrintObjectListListener( PrintObjectListListener listener )
  {
    if (printObjectListListeners_ != null)
    {
      synchronized(printObjectListListeners_)
      {
        printObjectListListeners_.removeElement(listener);
      }
    }
  }



  /**
   * Resets the list of object attributes to retrieve.
   **/
  public void resetAttributesToRetrieve()
  {
    attrsToRetrieve_ = null;
  }



  /**
   * Resets the list filter back to default values.
   **/
  public void resetFilter()
  {
    selection_.reset();
    idFilter_ = null;      // effectively resets the id Codepoint filter;
  }



  // Rethrow any exception.
  private void rethrowException()
  throws  AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  RequestNotSupportedException
  {
    if (anyException_ instanceof AS400Exception)
      throw (AS400Exception)anyException_;
    if (anyException_ instanceof AS400SecurityException)
      throw (AS400SecurityException)anyException_;
    if (anyException_ instanceof ConnectionDroppedException)
      throw (ConnectionDroppedException)anyException_;
    if (anyException_ instanceof ErrorCompletingRequestException)
      throw (ErrorCompletingRequestException)anyException_;
    if (anyException_ instanceof InterruptedException)
      throw (InterruptedException)anyException_;
    if (anyException_ instanceof IOException)
      throw (IOException)anyException_;
    if (anyException_ instanceof RequestNotSupportedException)
      throw (RequestNotSupportedException)anyException_;

    // runtime exceptions

    if (anyException_ instanceof ExtendedIllegalStateException)
      throw (ExtendedIllegalStateException)anyException_;
    if (anyException_ instanceof NullPointerException)
      throw (NullPointerException)anyException_;

    // If we get here we are getting an exception we overlooked,
    // trace it for debugging.

    Trace.log(Trace.ERROR, "rethrowException: Exception was not rethrown.");
  }



  /**
   * This is called automatically when Thread.start() is called by openAsynchronously().
   **/
  public void run()
  {
    try
    {
      openSynchronously();
    }
    catch (Exception e)
    {
      anyException_ = e;
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
  public void setAttributesToRetrieve(int[] attributes)
  {
    // check params
    if (attributes == null)
    {
      Trace.log(Trace.ERROR, "setAttributesToRetrieve: Parameter 'attributes' is null.");
      throw new NullPointerException("attributes");
    }

    if (attrsToRetrieve_ != null)
    {
      attrsToRetrieve_.reset();
    }
    else
    {
      attrsToRetrieve_ = new NPCPAttributeIDList();
    }   

    for (int i = 0; i<attributes.length; i++)
    {
      attrsToRetrieve_.addAttrID(attributes[i]);
    }
  }



  public void setCache(boolean f)
  {
    useCache_ = f;
  }


  public void setIDCodePointFilter(NPCPID cpID)
  {
    idFilter_ = cpID;
  }



  public void setFilter(String filterType, String filter)
  {
    if (filterType.equals("resource"))
    {
      NPCPSelRes selectionCP = (NPCPSelRes)getSelectionCP();
      selectionCP.setResource(filter);
    }
    if (filterType.equals("queue"))
    {
      NPCPSelOutQ selectionCP = (NPCPSelOutQ)getSelectionCP();
      selectionCP.setQueue(filter);
    }
    else if (filterType.equals("printer"))
    {
      NPCPSelPrtD selectionCP = (NPCPSelPrtD)getSelectionCP();
      selectionCP.setPrinter(filter);
    }
    else if (filterType.equals("printerFile"))
    {
      NPCPSelPrtF selectionCP = (NPCPSelPrtF)getSelectionCP();
      selectionCP.setPrinterFile(filter);
    }
    else if (filterType.equals("formType"))
    {
      NPCPSelSplF selectionCP = (NPCPSelSplF)getSelectionCP();
      selectionCP.setFormType(filter);
    }
    else if (filterType.equals("spooledFileQueue"))
    {
      NPCPSelSplF selectionCP = (NPCPSelSplF)getSelectionCP();
      selectionCP.setQueue(filter);
    }
    else if (filterType.equals("user"))
    {
      NPCPSelSplF selectionCP = (NPCPSelSplF)getSelectionCP();
      selectionCP.setUser(filter);
    }
    else if (filterType.equals("userData"))
    {
      NPCPSelSplF selectionCP = (NPCPSelSplF)getSelectionCP();
      selectionCP.setUserData(filter);
    }
    else if (filterType.equals("writerJobQueue"))
    {
      NPCPSelWrtJ selectionCP = (NPCPSelWrtJ)getSelectionCP();
      selectionCP.setQueue(filter);
    }
    else if (filterType.equals("writer"))
    {
      NPCPSelWrtJ selectionCP = (NPCPSelWrtJ)getSelectionCP();
      selectionCP.setWriter(filter);
    }
  }


  public void setPrintObjectListAttrs(NPCPAttributeIDList attrsToRetrieve,
                                      NPCPID idFilter,
                                      NPCPSelection selection,
                                      int typeOfObject)
  {
    attrsToRetrieve_ = attrsToRetrieve;
    idFilter_ = idFilter;
    selection_ = selection;
    typeOfObject_ = typeOfObject;      
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
  public void setSystem(AS400Impl system)  // @A4C
  {
    system_ = (AS400ImplRemote) system;  // @A4C-cast to ImplRemote
  }



  /**
   * Returns the current size of the list.
   **/
  public int size()
  {
    return numItems_;
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
  public void waitForItem(int itemNumber)
  throws  AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  RequestNotSupportedException
  {
    // while the size of the list is less than the item number requested,
    // -AND- the list is not done, -OR- we are firing events, we wait.
    // @B2A - Added (cpidList_ != null)
    while ((numItems_ < itemNumber && !completed_ && !closed_))
    {
      waitingForItem_ = itemNumber;
      waiting_ = true;
      try
      {
        synchronized(waitLock_)
        {
          if (!closed_ && !completed_) waitLock_.wait();
        }
      }
      catch (InterruptedException e)
      {
        anyException_ = e;
      }

      if (anyException_ != null)
      {
        // rethrow any exception the background thread caught.
        rethrowException();
      }
    }
    waiting_ = false;
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
  public void waitForListToComplete()
  throws  AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  RequestNotSupportedException
  {
    // while the list is not done, -OR- we are firing events, we wait.
    while (!completed_ && !closed_)
    {
      waitingForItem_ = -1;
      waiting_ = true;
      try
      {
        synchronized(waitLock_)
        {
          if (!completed_ && !closed_) waitLock_.wait();
        }
      }
      catch (InterruptedException e)
      {
        anyException_ = e;
      }

      if (anyException_ != null)
      {
        // rethrow any exception the background thread caught.
        rethrowException();
      }
    }
    waiting_ = false;
  }

}  

