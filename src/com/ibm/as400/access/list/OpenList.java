///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: OpenList.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2004 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access.list;

import com.ibm.as400.access.*;
import java.io.*;
import java.util.*;
import java.beans.PropertyVetoException;

/**
 * Abstract base class that wraps a list of resources on the iSeries server.
 * OpenList classes are Java wrappers for system APIs that are known as "Open List APIs" or "QGY APIs".
 * <P>
 * The way an open list works is that a request is initially sent to the server with selection, filter,
 * and sort information that is to be applied to the list of resources. The server compiles a list of resources that satisfy
 * the requested set of selection parameters. The OpenList class can then retrieve the list all at once, or in pieces, depending
 * upon the memory and performance requirements of the application.
 * <P>
 * The server can be told explicitly to compile the list of resources by calling {@link #open open()}. If {@link #open open()}
 * is not called explicitly by the application, an implicit call to {@link #open open()} is made by any of the
 * following methods: {@link #getLength getLength()}, {@link #getItems getItems()}, and {@link #getItems(int,int) getItems(int,int)}.
 * <P>
 * Once the list is open, the application can retrieve resources from the list using either
 * {@link #getItems getItems()} or {@link #getItems(int,int) getItems(offset, length)}. One returns an
 * Enumeration, the other returns an array and allows for arbitrarily indexing into the list on the server.
 * The type of resource returned is determined by the type of subclass that extends OpenList. For
 * example, the SpooledFileOpenList class returns SpooledFileListItem objects when getItems() is called.
 * <P>
 * When an OpenList object is no longer needed by the application, {@link #close close()} should be called to free up resources
 * on the server. If {@link #close close()} is not explicitly called, an attempt will be made to automatically close
 * the list when the OpenList object is {@link #finalize garbage collected}.
**/
public abstract class OpenList implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";
  static final long serialVersionUID = -5967313807836097042L;

  /**
   * Constant that can be used for APIs that have an error code parameter. An empty error code parameter
   * instructs the remote command server to return error messages via the ProgramCall message list.
   * This allows the ProgramCall logic to handle error conditions rather than the OpenList subclass.
   * All the caller has to do is this:
   * <PRE>
   * ProgramCall pc = new ProgramCall(system, "/LIBRARY.LIB/PROGRAM.PGM", parameters);
   * if (!pc.run())
   * {
   *   AS400Message[] errorMessages = pc.getMessageList();
   *   throw new AS400Exception(errorMessages);
   * }
   * </PRE>
  **/
  protected static final ProgramParameter EMPTY_ERROR_CODE_PARM = new ProgramParameter(new byte[4]);  

  /**
   * The system object specified on this OpenList's constructor.
   * @see #getSystem
  **/
  protected AS400 system_;

  /*
   * Status indicating complete and accurate information. All of the requested records
   * have been returned in the receiver variable of the Open List API.
   * @see #getInformationStatus
  */
//  public static final byte COMPLETE = (byte)0xC3;

  /*
   * Status indicating incomplete information. An interruption caused the receiver
   * variable to contain incomplete information.
   * @see #getInformationStatus
  */
//  public static final byte INCOMPLETE = (byte)0xC9;

  /*
   * Status indicating partial and accurate information. Partial information is returned
   * when the receiver variable is full and not all of the records requested are returned.
   * @see #getInformationStatus
  */
//  public static final byte PARTIAL = (byte)0xD7;

  /*
   * Status indicating that the list is not open.
   * @see #getInformationStatus
  */
//  public static final byte NONE = 0;


  private int length_; // number of objects in the list on the server, updated when open() is called.
  private boolean connected_; // are we loaded or closed?
  private byte[] handle_; // handle that references the user space used by the open list APIs
  private byte[] handleToClose_; // used to close a previously opened list
  private int blockSize_ = 1000; // Used by OpenListEnumeration when it calls getItems(int,int).

  private Vector enumerations_;

//  private String dateAndTimeCreated_;
//  private byte informationCompleteIndicator_;

  /**
   * Called by subclasses to construct an OpenList object.
   * @param system The system. This cannot be null.
  **/
  protected OpenList(AS400 system)
  {
    if (system == null) throw new NullPointerException("system");
    system_ = system;
  }

  /**
   * This method should be implemented by subclasses to call a particular QGY API and return
   * the 80-byte list information parameter. This method is called by open().
   * @return The output data from the list information parameter in the call to the QGY API.
  **/
  protected abstract byte[] callOpenListAPI() throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException;


  /**
   * Closes the list on the server. This releases any system resources previously in use by this OpenList.
   * <P>
   * Any Enumerations created by this OpenList by calling {@link #getItems getItems} will close, so
   * that a call to nextElement() will throw a NoSuchElementException when they reach the end of their
   * {@link #getEnumerationBlockSize enumeration cache block}.
   * @exception AS400Exception If there is a problem closing the list when the QGYCLST API is called.
  **/
  public synchronized void close() throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (!connected_)
    {
      return;
    }

    try
    {

      // Invalidate all Enumerations this OpenList has created.
      if (enumerations_ != null)
      {
        for (int i=enumerations_.size()-1; i>=0; --i)
        {
          OpenListEnumeration enum = (OpenListEnumeration)enumerations_.elementAt(i);
          enum.close();
        }
        enumerations_ = null;
      }

      if (handleToClose_ != null && (handle_ == null || handle_ == handleToClose_))
      {
        handle_ = handleToClose_;
        handleToClose_ = null;
      }
      if (Trace.isTraceOn())
      {
        Trace.log(Trace.DIAGNOSTIC, "Closing open list with handle: ", handle_);
      }
      ProgramParameter[] parms = new ProgramParameter[]
      {
        new ProgramParameter(handle_),
        EMPTY_ERROR_CODE_PARM
      };
      ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QGYCLST.PGM", parms);
      if (!pc.run())
      {
        throw new AS400Exception(pc.getMessageList());
      }
      if (handleToClose_ != null) // Just in case.
      {
        handle_ = handleToClose_;
        handleToClose_ = null;
        close();
      }
    }
    finally
    {
//      dateAndTimeCreated_ = null;
//      informationCompleteIndicator_ = 0;
      handle_ = null;
      connected_ = false;
    }
  }


  /**
   * Attempts to close the list on the system when this OpenList object is garbage collected.
  **/
  protected void finalize() throws Throwable
  {
    try
    {
      close();
    }
    catch (Exception e)
    {
      if (Trace.isTraceOn())
      {
        Trace.log(Trace.ERROR, "Exception occurred while finalizing open list with handle: ", handle_);
        Trace.log(Trace.ERROR, e);
      }
    }
    super.finalize();
  }


  /**
   * Returns the actual array of Objects that getItems(int,int) returns. Subclasses should
   * implement this method to return instances of their own list item classes. This method is called
   * by getItems(int,int).
   * @param data The output data from the receiver variable from the call to the QGYGTLE (Get List Entries) API.
   * @param recordsReturned The number of records returned, as reported in the open list information returned on the call to QGYGTLE.
   * @param recordLength The length of a record, in bytes, as reported in the open list information returned on the call to QGYGTLE.
  **/
  protected abstract Object[] formatOutputData(byte[] data, int recordsReturned, int recordLength) throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException;


  /**
   * Returns the initial size in bytes of the receiver variable for a particular implementation
   * of an Open List API. Subclasses should implement this method to return an appropriate value.
   * This method is called by getItems(int,int).
   * @param number The number of records in the list on the server. This is useful if the subclass
   * needs to return a receiver size based on how many records are in the list.
   * @return The number of bytes to allocate for the receiver variable when the QGYGTLE (Get List Entries)
   * API is called.  This number does not have to be calculated exactly, as QGYGTLE will be called repeatedly
   * until the correct size is known. This number is just for the initial call to QGYGTLE. Too low
   * of a value may result in extra API calls, too high of a value may result in wasted bytes being
   * sent and received.
  **/
  protected abstract int getBestGuessReceiverSize(int number);


  // I don't want to expose this yet, as asynchronous list processing may be 
  // added in the future, and the creation date getter may need to throw exceptions.
  /**
   * Returns the date and time the list was created on the server.
   * @return The creation date, or null if this list is not open.
  **/
/*  public Date getCreationDate()
  {
    if (dateAndTimeCreated_ == null) return null;
    Calendar c = Calendar.getInstance();
    c.clear();
    c.set(Integer.parseInt(dateAndTimeCreated_.substring(0,2)) + 1900,// year
          Integer.parseInt(dateAndTimeCreated_.substring(2,4))-1,     // month is zero based
          Integer.parseInt(dateAndTimeCreated_.substring(4,6)),       // day
          Integer.parseInt(dateAndTimeCreated_.substring(6,8)),       // hour
          Integer.parseInt(dateAndTimeCreated_.substring(8,10)),      // minute
          Integer.parseInt(dateAndTimeCreated_.substring(10,12)));    // second
    return c.getTime();
  }
*/

  /**
   * Returns the number of items that Enumerations returned by this OpenList's {@link #getItems getItems()}
   * method will attempt to retrieve from the server and cache. A larger number will result in fewer
   * calls to the server but will take more memory.
   * @return The block size. The default is 1000 items.
  **/
  public int getEnumerationBlockSize()
  {
    return blockSize_;
  }


  // I don't want to expose this yet, as asynchronous list processing may be 
  // added in the future, and the information complete indicator may need to throw exceptions.
  /**
   * Returns the information complete indicator, which indicates whether all requested
   * information has been supplied on the most recent call
   * to this Open List API. Possible values are:
   * <UL>
   * <LI>{@link #COMPLETE COMPLETE}
   * <LI>{@link #INCOMPLETE INCOMPLETE}
   * <LI>{@link #PARTIAL PARTIAL}
   * <LI>{@link #NONE NONE}
   * </UL>
   * @return The status of the information built by the most recent implicit or
   * explicit call to {@link #open open()}.
  **/
//  public byte getInformationStatus()
//  {
//    return informationCompleteIndicator_;
//  }


  /**
   * Returns the number of items in the list the server has built.
   * This method implicitly calls {@link #open open()} to instruct the server to build the list
   * if it hasn't been built already.
   * @return The number of items, or 0 if no list was retrieved.
   * @see #open
  **/
  public synchronized int getLength() throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException, OpenListException
  {
    if (handle_ == null)
    {
      open();
    }
    return length_;
  }


  /**
   * Returns the list of items. The Enumeration will retrieve the items from the list built
   * on the server in blocks for performance. The chunk size can be adjusted by using the
   * {@link #setEnumerationBlockSize setEnumerationBlockSize()} method. This method implicity calls {@link #open open()} to instruct
   * the server to build the list if it hasn't been built already.
   * <P>
   * Note that if this OpenList is closed, the Enumeration returned by this method will also be closed,
   * such that a subsequent call to hasMoreElements() returns false and a subsequent call to nextElement()
   * throws a NoSuchElementException.
   * <P>
   * Calling this method in a loop without either (a) closing this OpenList or (b) calling nextElement() on the
   * Enumerations until they are at an end, will result in a memory leak.
   * @return An Enumeration of objects. The types of objects in the Enumeration are dependent on which
   * particular OpenList subclass is being used.
   * @exception AS400Exception                  If the server returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the server.
   * @exception ObjectDoesNotExistException     If the object does not exist on the server.
   * @exception OpenListException               If the system is unable to correctly generate the list of items.
   * @see #close
   * @see #open
  **/
  public synchronized Enumeration getItems() throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException, OpenListException
  {
    if (handle_ == null)
    {
      open(); // Need to get the length_
    }

    // Keep track of all Enumerations we create, in case someone closes this OpenList,
    // we can invalidate them.
    if (enumerations_ == null)
    {
      enumerations_ = new Vector();
    }
    Enumeration enum = new OpenListEnumeration(this, length_);
    enumerations_.addElement(enum);
    return enum;
  }


  /**
   * Returns an array of items, which can be a subset of the entire list built on the server.
   * This method allows the user to retrieve the item list from the server
   * in pieces. If a call to {@link #open open()} is made (either implicitly or explicitly),
   * then the items at a given offset will change, so a subsequent call to
   * getItems() with the same <i>listOffset</i> and <i>number</i>
   * will most likely not return the same items as the previous call.
   * @param listOffset The offset into the list of items. This value must be greater than or equal to 0 and
   * less than the list length, or specify -1 to retrieve all of the items.
   * @param number The number of items to retrieve out of the list, starting at the specified
   * <i>listOffset</i>. This value must be greater than or equal to 0 and less than or equal
   * to the list length. If the <i>listOffset</i> is -1, this parameter is ignored.
   * @return The array of retrieved items. The types of items in the Enumeration are dependent on which
   * particular OpenList subclass is being used. The length of this array may not necessarily be equal to
   * <i>number</i>, depending upon the size of the list on the server, and the specified <i>listOffset</i>.
   * @exception AS400Exception                  If the system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the system.
   * @exception ObjectDoesNotExistException     If the object does not exist on the system.
   * @exception OpenListException               If the system is unable to correctly generate the list of items.
   * @see com.ibm.as400.access.Job
   * @see #close
   * @see #open
  **/
  public synchronized Object[] getItems(int listOffset, int number) throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException, OpenListException
  {
    if (listOffset < -1)
    {
      throw new ExtendedIllegalArgumentException("listOffset", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }

    if (number < 0 && listOffset != -1)
    {
      throw new ExtendedIllegalArgumentException("number", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }

    if (number == 0 && listOffset != -1)
    {
      return new Object[0];
    }

    if (handle_ == null)
    {
      open();
    }

    if (listOffset == -1) number = length_;

    int ccsid = system_.getCcsid();
    CharConverter conv = new CharConverter(ccsid);

    ProgramParameter[] parms2 = new ProgramParameter[7];
    int len = getBestGuessReceiverSize(number);
    // If the subclass sets the length wrong, that's their problem.
    parms2[0] = new ProgramParameter(len); // receiver variable
    parms2[1] = new ProgramParameter(BinaryConverter.intToByteArray(len)); // length of receiver variable
    parms2[2] = new ProgramParameter(handle_);
    parms2[3] = new ProgramParameter(80); // list information
    parms2[4] = new ProgramParameter(BinaryConverter.intToByteArray(number)); // number of records to return
    parms2[5] = new ProgramParameter(BinaryConverter.intToByteArray(listOffset == -1 ? -1 : listOffset+1)); // starting record
    parms2[6] = EMPTY_ERROR_CODE_PARM;

    ProgramCall pc2 = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QGYGTLE.PGM", parms2);
    if (!pc2.run())
    {
      throw new AS400Exception(pc2.getMessageList());
    }

    byte[] listInfo = parms2[3].getOutputData();
    if (listInfo == null || listInfo.length < 16)
    {
      if (Trace.isTraceOn()) Trace.log(Trace.ERROR, "Bad list information from QGYGTLE:", listInfo);
      throw new OpenListException(OpenListException.LIST_INFO_NOT_VALID);
    }
    int totalRecords = BinaryConverter.byteArrayToInt(listInfo, 0);
    int recordsReturned = BinaryConverter.byteArrayToInt(listInfo, 4);
    int recordLength = BinaryConverter.byteArrayToInt(listInfo, 12);
//    dateAndTimeCreated_ = conv.byteArrayToString(listInfo, 17, 13);
//    informationCompleteIndicator_ = listInfo[16];

    while (listOffset == -1 && totalRecords > recordsReturned)
    {
      len = len*(1+(totalRecords/(recordsReturned+1)));
      if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Calling ObjectList QGYGTLE again with an updated length of "+len+".");
      try
      {
        parms2[0].setOutputDataLength(len);
        parms2[1].setInputData(BinaryConverter.intToByteArray(len));
      }
      catch (PropertyVetoException pve)
      {
      }
      if (!pc2.run())
      {
        throw new AS400Exception(pc2.getMessageList());
      }
      listInfo = parms2[3].getOutputData();
      if (listInfo == null || listInfo.length < 16)
      {
        if (Trace.isTraceOn()) Trace.log(Trace.ERROR, "Bad list information from QGYGTLE:", listInfo);
        throw new OpenListException(OpenListException.LIST_INFO_NOT_VALID);
      }
      totalRecords = BinaryConverter.byteArrayToInt(listInfo, 0);
      recordsReturned = BinaryConverter.byteArrayToInt(listInfo, 4);
      recordLength = BinaryConverter.byteArrayToInt(listInfo, 12);
//      dateAndTimeCreated_ = conv.byteArrayToString(listInfo, 17, 13);
//      informationCompleteIndicator_ = listInfo[16];
    }
    
    byte listStatusIndicator = listInfo[30];
    if (listStatusIndicator != (byte)0xF2) // '2' means the list has been completely built
    {
      if (Trace.isTraceOn()) Trace.log(Trace.ERROR, "Unable to build object list on server ("+listStatusIndicator+")");
      throw new OpenListException(listStatusIndicator & 0x00FF);
    }

    byte[] data = parms2[0].getOutputData();
    return formatOutputData(data, recordsReturned, recordLength);
  }


  /**
   * Returns the system object used by this OpenList.
   * @return The system.
  **/
  public AS400 getSystem()
  {
    return system_;
  }


  /**
   * Returns whether or not this list is open.
   * @return true if this list has been either implicitly or explictly {@link #open opened};
   * false if this list has been {@link #close closed}, or was never opened in the first place, or
   * has had its properties changed such that it no longer accurately represents the list that
   * was built on the server.
  **/
  public boolean isOpen()
  {
    return connected_ && handle_ != null;
  }


  /**
   * Loads the list of items on the system. This method instructs the
   * server to build the list of items. This method blocks until the system returns
   * the total number of items it has compiled. A subsequent call to
   * {@link #getItems getItems()} will retrieve the actual object information
   * and attributes for each item in the list from the system.
   * <p>
   * This method updates the list length returned by {@link #getLength getLength()}.
   * <p>
   * If this list is already open, {@link #close close()} is called implicitly.
   *
   * @exception AS400Exception                  If the system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the system.
   * @exception ObjectDoesNotExistException     If the object does not exist on the system.
   * @exception OpenListException               If the system is unable to correctly generate the list of items.
   * @exception ServerStartupException          If the server cannot be started.
   * @exception UnknownHostException            If the system cannot be located.
   * @see #getLength
   * @see #close
  **/
  public synchronized void open() throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException, OpenListException
  {
    // Close the previous list
    if (handle_ != null || handleToClose_ != null)
    {
      close();
    }

    byte[] listInformation = callOpenListAPI();
    if (listInformation == null || listInformation.length < 16)
    {
      if (Trace.isTraceOn()) Trace.log(Trace.ERROR, "Bad list information returned:", listInformation);
      throw new OpenListException(OpenListException.LIST_INFO_NOT_VALID);
    }

    byte listStatusIndicator = listInformation[30];
    if (listStatusIndicator != (byte)0xF2) // '2' means the list has been completely built
    {
      if (Trace.isTraceOn()) Trace.log(Trace.ERROR, "Unable to build object list on server ("+listStatusIndicator+")");
      throw new OpenListException(listStatusIndicator & 0x00FF);
    }

    handle_ = new byte[4];
    System.arraycopy(listInformation, 8, handle_, 0, 4);
    connected_ = true;
    length_ = BinaryConverter.byteArrayToInt(listInformation, 0);
//    dateAndTimeCreated_ = conv.byteArrayToString(listInformation, 17, 13);
//    informationCompleteIndicator_ = listInformation[16];

    // This second program call is to retrieve the number of objects in the list.
    // It will wait until the server has fully populated the list before it
    // returns.
    ProgramParameter[] parms2 = new ProgramParameter[7];
    parms2[0] = new ProgramParameter(1); // receiver variable
    parms2[1] = new ProgramParameter(BinaryConverter.intToByteArray(1)); // length of receiver variable
    parms2[2] = new ProgramParameter(handle_); // request handle
    parms2[3] = new ProgramParameter(80); // list information
    parms2[4] = new ProgramParameter(BinaryConverter.intToByteArray(0)); // number of records to return
    parms2[5] = new ProgramParameter(BinaryConverter.intToByteArray(-1)); // starting record
    parms2[6] = EMPTY_ERROR_CODE_PARM;

    ProgramCall pc2 = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QGYGTLE.PGM", parms2);
    if (!pc2.run())
    {
      throw new AS400Exception(pc2.getMessageList());
    }
    byte[] listInfo2 = parms2[3].getOutputData();
    if (listInfo2 == null || listInfo2.length < 16)
    {
      if (Trace.isTraceOn()) Trace.log(Trace.ERROR, "Bad list information from QGYGTLE:", listInfo2);
      throw new OpenListException(OpenListException.LIST_INFO_NOT_VALID);
    }
    length_ = BinaryConverter.byteArrayToInt(listInfo2, 0);
//    dateAndTimeCreated_ = conv.byteArrayToString(listInfo2, 17, 13);
//    informationCompleteIndicator_ = listInfo2[16];
    
    listStatusIndicator = listInfo2[30];
    if (listStatusIndicator != (byte)0xF2) // '2' means the list has been completely built
    {
      if (Trace.isTraceOn()) Trace.log(Trace.ERROR, "Unable to build object list on server ("+listStatusIndicator+")");
      throw new OpenListException(listStatusIndicator & 0x00FF);
    }

    if (Trace.isTraceOn())
    {
      Trace.log(Trace.DIAGNOSTIC, "Loaded open list with length = "+length_+" and handle: ", handle_);
    }
  }


  /**
   * Used for Enumerations so they can tell us that they have reached the end.
   * We remove them from our list so they can get garbage collected.
  **/
  void remove(OpenListEnumeration enum)
  {
    enumerations_.removeElement(enum);
  }

  /**
   * Resets the handle to indicate we should close the list the next time
   * we do something, usually as a result of one of the selection criteria
   * being changed, since that should build a new list on the server.
   * Subclasses should call this method when their list filtering, selection, and sort criteria are changed
   * in order to discard the stale list data on the server and build a new list when open() is called.
   * <P>
   * It is better that a subclass not allow any of its selection criteria to be changed while
   * the list is open, but that is not always desirable, which is why this method exists.
  **/
  protected synchronized void resetHandle()
  {
    if (handleToClose_ == null) handleToClose_ = handle_; // Close the old list on the next load
    handle_ = null;    
//    dateAndTimeCreated_ = null;
//    informationCompleteIndicator_ = 0;
  }


  /**
   * Sets the number of items that Enumerations returned by this OpenList's {@link #getItems getItems()}
   * method will attempt to retrieve from the server and cache. A larger number will result in fewer
   * calls to the server but will take more memory.
   * @param blockSize The block size. The default is 1000 items. If a number less than 1 is specified, the
   * default block size of 1000 is used.
  **/
  public void setEnumerationBlockSize(int blockSize)
  {
    blockSize_ = blockSize < 1 ? 1000 : blockSize;
  }

}

