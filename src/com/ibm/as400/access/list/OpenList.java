///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  OpenList.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2005 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access.list;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Exception;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.BinaryConverter;
//import com.ibm.as400.access.CharConverter;
import com.ibm.as400.access.ErrorCodeParameter;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.ExtendedIllegalArgumentException;
import com.ibm.as400.access.ObjectDoesNotExistException;
import com.ibm.as400.access.ProgramCall;
import com.ibm.as400.access.ProgramParameter;
import com.ibm.as400.access.Trace;

/**
 Abstract base class that wraps a list of resources on the system.  OpenList classes are Java wrappers for system API's that are known as "Open List API's" or "QGY API's".
 <p>The way an open list works is that a request is initially sent to the system with selection, filter, and sort information that is to be applied to the list of resources.  The system compiles a list of resources that satisfy the requested set of selection parameters.  The OpenList class can then retrieve the list all at once, or in pieces, depending upon the memory and performance requirements of the application.
 <p>The system can be told explicitly to compile the list of resources by calling {@link #open open()}.  If {@link #open open()} is not called explicitly by the application, an implicit call to {@link #open open()} is made by any of the following methods: {@link #getLength getLength()}, {@link #getItems getItems()}, and {@link #getItems(int,int) getItems(int,int)}.
 <p>Once the list is open, the application can retrieve resources from the list using either {@link #getItems getItems()} or {@link #getItems(int,int) getItems(offset, length)}.  One returns an Enumeration, the other returns an array and allows for arbitrarily indexing into the list on the system.  The type of resource returned is determined by the type of subclass that extends OpenList.  For example, the SpooledFileOpenList class returns SpooledFileListItem objects when getItems() is called.
 <p>When an OpenList object is no longer needed by the application, {@link #close close()} should be called to free up resources on the system.  If {@link #close close()} is not explicitly called, an attempt will be made to automatically close the list when the OpenList object is {@link #finalize garbage collected}.
 **/
public abstract class OpenList implements Serializable
{
    static final long serialVersionUID = -5967313807836097042L;

    /**
     Constant that can be used for APIs that have an error code parameter.  An empty error code parameter instructs the remote command server to return error messages via the ProgramCall message list.  This allows the ProgramCall logic to handle error conditions rather than the OpenList subclass.  All the caller has to do is this:
     <pre>
         ProgramCall pc = new ProgramCall(system, "/LIBRARY.LIB/PROGRAM.PGM", parameters);
         if (!pc.run())
         {
             AS400Message[] errorMessages = pc.getMessageList();
             throw new AS400Exception(errorMessages);
         }
     </pre>
     **/
    protected static final ProgramParameter EMPTY_ERROR_CODE_PARM = new ErrorCodeParameter();

    /*
     Status indicating complete and accurate information.  All of the requested records have been returned in the receiver variable of the Open List API.
     @see  #getInformationStatus
     */
    // public static final byte COMPLETE = (byte)0xC3;

    /*
     Status indicating incomplete information.  An interruption caused the receiver variable to contain incomplete information.
     @see  #getInformationStatus
     */
    // public static final byte INCOMPLETE = (byte)0xC9;

    /*
     Status indicating partial and accurate information.  Partial information is returned when the receiver variable is full and not all of the records requested are returned.
     @see  #getInformationStatus
     */
    // public static final byte PARTIAL = (byte)0xD7;

    /*
     Status indicating that the list is not open.
     @see  #getInformationStatus
     */
    // public static final byte NONE = 0;

    /**
     The system object specified on this OpenList's constructor.
     @see  #getSystem
     **/
    protected AS400 system_;

    // Number of objects in the list on the system, updated when open() is called.
    private int length_;
    // Used by OpenListEnumeration when it calls getItems(int, int).
    private int enumerationBlockSize_ = 1000;
    // Handle that references the user space used by the open list API's.
    private byte[] handle_;
    // If the list info has changed, close the old handle before loading the new one.
    private boolean closeHandle_ = false;

    // All the enumerations created for this list.
    private Vector enumerations_;

    // Date and time created.
    // private String creationDate_;
    // Information complete indicator.
    // private byte informationStatus_;

    /**
     Called by subclasses to construct an OpenList object.
     @param  system  The system object representing the system on which the list exists.  This cannot be null.
     **/
    protected OpenList(AS400 system)
    {
        super();
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Constructing OpenList object, system: " + system);
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        system_ = system;
    }

    /**
     This method should be implemented by subclasses to call a particular QGY API and return the 80-byte list information parameter.  This method is called by open().
     @return  The output data from the list information parameter in the call to the QGY API.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    protected abstract byte[] callOpenListAPI() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException;

    /**
     Closes the list on the system.  This releases any system resources previously in use by this OpenList.
     <p>Any Enumerations created by this OpenList by calling {@link #getItems getItems} will close, so that a call to nextElement() will throw a NoSuchElementException when they reach the end of their {@link #getEnumerationBlockSize enumeration cache block}.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public synchronized void close() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Closing open list, handle: ", handle_);
        if (handle_ == null) return;

        try
        {
            // Invalidate all Enumerations this OpenList has created.
            if (enumerations_ != null)
            {
                for (int i = enumerations_.size() - 1; i >= 0; --i)
                {
                    OpenListEnumeration element = (OpenListEnumeration)enumerations_.elementAt(i);
                    element.close();
                }
                enumerations_ = null;
            }

            ProgramParameter[] parameters = new ProgramParameter[]
            {
                new ProgramParameter(handle_),
                EMPTY_ERROR_CODE_PARM
            };
            ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QGYCLST.PGM", parameters);
            if (!pc.run())
            {
                throw new AS400Exception(pc.getMessageList());
            }
        }
        finally
        {
            // creationDate_ = null;
            // informationStatus_ = 0;
            handle_ = null;
            closeHandle_ = false;
        }
    }

    /**
     Attempts to close the list on the system when this OpenList object is garbage collected.
     **/
    protected void finalize() throws Throwable
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Finalize method for open list invoked.");
        try
        {
            if (system_.isConnected(AS400.COMMAND)) close();
        }
        catch (Exception e)
        {
            if (Trace.isTraceOn())
            {
                Trace.log(Trace.DIAGNOSTIC, "Exception occurred while finalizing open list with handle:", handle_);
                Trace.log(Trace.ERROR, e);
            }
        }
        super.finalize();
    }

    /**
     Returns the actual array of Objects that getItems(int,int) returns.  Subclasses should implement this method to return instances of their own list item classes.  This method is called by getItems(int,int).
     @param  data  The output data from the receiver variable from the call to the QGYGTLE (Get List Entries) API.
     @param  recordsReturned  The number of records returned, as reported in the open list information returned on the call to QGYGTLE.
     @param  recordLength  The length of a record, in bytes, as reported in the open list information returned on the call to QGYGTLE.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    protected abstract Object[] formatOutputData(byte[] data, int recordsReturned, int recordLength) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException;

    /**
     Returns the initial size in bytes of the receiver variable for a particular implementation of an Open List API.  Subclasses should implement this method to return an appropriate value.  This method is called by getItems(int,int).
     @param  number  The number of records in the list on the system.  This is useful if the subclass needs to return a receiver size based on how many records are in the list.
     @return  The number of bytes to allocate for the receiver variable when the QGYGTLE (Get List Entries) API is called.  This number does not have to be calculated exactly, as QGYGTLE will be called repeatedly until the correct size is known.  This number is just for the initial call to QGYGTLE.  Too low of a value may result in extra API calls, too high of a value may result in wasted bytes being sent and received.
     **/
    protected abstract int getBestGuessReceiverSize(int number);

    // I don't want to expose this yet, as asynchronous list processing may be added in the future, and the creation date getter may need to throw exceptions.
    /*
     Returns the date and time the list was created on the system.
     @return  The creation date, or null if this list is not open.
     */
    // public Date getCreationDate()
    // {
    //     if (creationDate_ == null) return null;
    //     Calendar c = AS400Calendar.getGregorianInstance();
    //     c.clear();
    //     c.set(Integer.parseInt(creationDate_.substring(0,2)) + 1900,  // Year.
    //           Integer.parseInt(creationDate_.substring(2,4)) - 1,  // Month is zero based.
    //           Integer.parseInt(creationDate_.substring(4,6)),  // Day.
    //           Integer.parseInt(creationDate_.substring(6,8)),  // Hour.
    //           Integer.parseInt(creationDate_.substring(8,10)),  // Minute.
    //           Integer.parseInt(creationDate_.substring(10,12)));  // Second.
    //     return c.getTime();
    //  }

    /**
     Returns the number of items that Enumerations returned by this OpenList's {@link #getItems getItems()} method will attempt to retrieve from the system and cache.  A larger number will result in fewer calls to the system but will take more memory.
     @return  The block size.  The default is 1000 items.
     **/
    public int getEnumerationBlockSize()
    {
        return enumerationBlockSize_;
    }

    // I don't want to expose this yet, as asynchronous list processing may be added in the future, and the information complete indicator may need to throw exceptions.
    /*
     Returns the information complete indicator, which indicates whether all requested information has been supplied on the most recent call to this Open List API.  Possible values are:
     <ul>
     <li>{@link #COMPLETE COMPLETE}
     <li>{@link #INCOMPLETE INCOMPLETE}
     <li>{@link #PARTIAL PARTIAL}
     <li>{@link #NONE NONE}
     </ul>
     @return  The status of the information built by the most recent implicit or explicit call to {@link #open open()}.
     */
    // public byte getInformationStatus()
    // {
    //     return informationStatus_;
    // }

    /**
     Returns the number of items in the list the system has built.  This method implicitly calls {@link #open open()} to instruct the system to build the list if it hasn't been built already.
     @return  The number of items, or 0 if no list was retrieved.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @exception  OpenListException  If the system is unable to correctly generate the list of items.
     @see  #open
     **/
    public synchronized int getLength() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException, OpenListException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting open list length.");
        if (handle_ == null || closeHandle_) open();
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Length:", length_);
        return length_;
    }

    /**
     Returns the list of items.  The Enumeration will retrieve the items from the list built on the system in blocks for performance.  The chunk size can be adjusted by using the {@link #setEnumerationBlockSize setEnumerationBlockSize()} method.  This method implicity calls {@link #open open()} to instruct the system to build the list if it hasn't been built already.
     <p>Note that if this OpenList is closed, the Enumeration returned by this method will also be closed, such that a subsequent call to hasMoreElements() returns false and a subsequent call to nextElement() throws a NoSuchElementException.
     <p>Calling this method in a loop without either (a) closing this OpenList or (b) calling nextElement() on the Enumerations until they are at an end, will result in a memory leak.
     @return  An Enumeration of objects.  The types of objects in the Enumeration are dependent on which particular OpenList subclass is being used.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @exception  OpenListException  If the system is unable to correctly generate the list of items.
     @see  #close
     @see  #open
     **/
    public synchronized Enumeration getItems() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException, OpenListException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Retrieving open list.");
        // Need to get the length.
        if (handle_ == null || closeHandle_) open();

        // Keep track of all Enumerations we create, in case someone closes this OpenList, we can invalidate them.
        if (enumerations_ == null)
        {
            enumerations_ = new Vector();
        }
        Enumeration items = new OpenListEnumeration(this, length_);
        enumerations_.addElement(items);
        return items;
    }

    /**
     Returns an array of items, which can be a subset of the entire list built on the system.  This method allows the user to retrieve the item list from the system in pieces.  If a call to {@link #open open()} is made (either implicitly or explicitly), then the items at a given offset will change, so a subsequent call to getItems() with the same <i>listOffset</i> and <i>number</i> will most likely not return the same items as the previous call.
     @param  listOffset  The offset into the list of items.  This value must be greater than or equal to 0 and less than the list length, or specify -1 to retrieve all of the items.
     @param  number  The number of items to retrieve out of the list, starting at the specified <i>listOffset</i>.  This value must be greater than or equal to 0 and less than or equal to the list length.  If the <i>listOffset</i> is -1, this parameter is ignored.
     @return  The array of retrieved items.  The types of items in the array are dependent on which particular OpenList subclass is being used.  The length of this array may not necessarily be equal to <i>number</i>, depending upon the size of the list on the system, and the specified <i>listOffset</i>.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @exception  OpenListException  If the system is unable to correctly generate the list of items.
     @see  com.ibm.as400.access.Job
     @see  #close
     @see  #open
     **/
    public synchronized Object[] getItems(int listOffset, int number) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException, OpenListException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Retrieving open list, list offset: " + listOffset + ", number:", number);
        if (listOffset < -1)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'listOffset' is not valid:", listOffset);
            throw new ExtendedIllegalArgumentException("listOffset (" + listOffset + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }

        if (number < 0 && listOffset != -1)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'number' is not valid:", number);
            throw new ExtendedIllegalArgumentException("number (" + number + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }

        if (handle_ == null || closeHandle_) open();

        if (listOffset == -1)
        {
            number = length_;
            listOffset = 0;
        }
        else if (listOffset + number > length_)
        {
            number = length_ - listOffset;
        }

        int lengthOfReceiverVariable = getBestGuessReceiverSize(number);
        ProgramParameter[] parameters = new ProgramParameter[]
        {
            // Receiver variable, output, char(*).
            new ProgramParameter(lengthOfReceiverVariable),
            // Length of receiver variable, input, binary(4).
            new ProgramParameter(BinaryConverter.intToByteArray(lengthOfReceiverVariable)),
            // Request handle, input, char(4).
            new ProgramParameter(handle_),
            // List information, output, char(80).
            new ProgramParameter(80),
            // Number of records to return, input, binary(4).
            new ProgramParameter(BinaryConverter.intToByteArray(number)),
            // Starting record, input, binary(4).
            new ProgramParameter(BinaryConverter.intToByteArray(listOffset + 1)),
            // Error code, I/0, char(*).
            EMPTY_ERROR_CODE_PARM
        };

        ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QGYGTLE.PGM", parameters);

        byte[] listInformation = null;
        int recordsReturned = 0;
        do
        {
            if (pc.run())
            {
                listInformation = parameters[3].getOutputData();
                recordsReturned = BinaryConverter.byteArrayToInt(listInformation, 4);
            }
            else
            {
                AS400Message[] messages = pc.getMessageList();
                // GUI0002 means the receiver variable was too small.
                if (!messages[0].getID().equals("GUI0002")) throw new AS400Exception(messages);
            }

            if (recordsReturned < number)
            {
                if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Retrieved messages, records returned: " + recordsReturned + ", number:", number);
                lengthOfReceiverVariable *= 1 + number / (recordsReturned + 1);
                if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Updated length: ", lengthOfReceiverVariable);
                parameters[0] = new ProgramParameter(lengthOfReceiverVariable);
                parameters[1] = new ProgramParameter(BinaryConverter.intToByteArray(lengthOfReceiverVariable));
            }
        } while (recordsReturned < number || listInformation == null);

        //int totalRecords = BinaryConverter.byteArrayToInt(listInformation, 0);
        int recordLength = BinaryConverter.byteArrayToInt(listInformation, 12);
        // informationStatus_ = listInfo[16];
        // CharConverter conv = new CharConverter(system_.getCcsid(), system_);
        // creationDate_ = conv.byteArrayToString(listInfo, 17, 13);
        int listStatusIndicator = listInformation[30] & 0xFF;

        // '2' means the list has been completely built.
        if (listStatusIndicator != 0xF2)
        {
            if (Trace.isTraceOn()) Trace.log(Trace.ERROR, "Unable to build object list on server, list status indicator:", listStatusIndicator);
            throw new OpenListException(listStatusIndicator);
        }

        return formatOutputData(parameters[0].getOutputData(), recordsReturned, recordLength);
    }

    /**
     Returns the system object used by this OpenList.
     @return  The system.
     **/
    public AS400 getSystem()
    {
        return system_;
    }

    /**
     Returns whether or not this list is open.
     @return  true if this list has been either implicitly or explictly {@link #open opened}; false if this list has been {@link #close closed}, or was never opened in the first place, or has had its properties changed such that it no longer accurately represents the list that was built on the system.
     **/
    public boolean isOpen()
    {
        return handle_ == null || closeHandle_;
    }

    /**
     Loads the list of items on the system.  This method instructs the system to build the list of items.  This method blocks until the system returns the total number of items it has compiled.  A subsequent call to {@link #getItems getItems()} will retrieve the actual object information and attributes for each item in the list from the system.
     <p>This method updates the list length returned by {@link #getLength getLength()}.
     <p>If this list is already open, {@link #close close()} is called implicitly.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @exception  OpenListException  If the system is unable to correctly generate the list of items.
     @see  #getLength
     @see  #close
     **/
    public synchronized void open() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException, OpenListException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Opening object list.");
        // Close the previous list.
        if (closeHandle_) close();

        byte[] listInformation = callOpenListAPI();
        if (listInformation == null || listInformation.length < 30)
        {
            if (Trace.isTraceOn()) Trace.log(Trace.ERROR, "Bad list information returned:", listInformation);
            throw new OpenListException(OpenListException.LIST_INFO_NOT_VALID);
        }

        int listStatusIndicator = listInformation[30] & 0xFF;
        // '2' means the list has been completely built.
        if (listStatusIndicator != 0xF2)
        {
            if (Trace.isTraceOn()) Trace.log(Trace.ERROR, "Unable to build object list on server, list status indicator:", listStatusIndicator);
            throw new OpenListException(listStatusIndicator);
        }

        length_ = BinaryConverter.byteArrayToInt(listInformation, 0);
        handle_ = new byte[4];
        System.arraycopy(listInformation, 8, handle_, 0, 4);
        // creationDate_ = conv.byteArrayToString(listInformation, 17, 13);
        // informationStatus_ = listInformation[16];

        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Opened object list, length: " + length_ + ", record length: " + BinaryConverter.byteArrayToInt(listInformation, 12) + ", handle:", handle_);
    }

    // Used for Enumerations so they can tell us that they have reached the end.  We remove them from our list so they can get garbage collected.
    void remove(OpenListEnumeration enum1)
    {
        enumerations_.removeElement(enum1);
    }

    /**
     Resets the handle to indicate we should close the list the next time we do something, usually as a result of one of the selection criteria being changed, since that should build a new list on the system.  Subclasses should call this method when their list filtering, selection, and sort criteria are changed in order to discard the stale list data on the system and build a new list when open() is called.
     <p>It is better that a subclass not allow any of its selection criteria to be changed while the list is open, but that is not always desirable, which is why this method exists.
     **/
    protected synchronized void resetHandle()
    {
        if (handle_ != null) closeHandle_ = true;
        // creationDate_ = null;
        // informationStatus_ = 0;
    }

    /**
     Sets the number of items that Enumerations returned by this OpenList's {@link #getItems getItems()} method will attempt to retrieve from the system and cache.  A larger number will result in fewer calls to the system but will take more memory.
     @param  enumerationBlockSize  The block size.  The default is 1000 items.  If a number less than 1 is specified, the default block size of 1000 is used.
     **/
    public void setEnumerationBlockSize(int enumerationBlockSize)
    {
        enumerationBlockSize_ = enumerationBlockSize < 1 ? 1000 : enumerationBlockSize;
    }
}
