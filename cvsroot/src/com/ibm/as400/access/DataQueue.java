///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  DataQueue.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2003 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;

/**
 The DataQueue class represents an IBM i sequential data queue object.
 **/
public class DataQueue extends BaseDataQueue
{
    static final long serialVersionUID = 4L;

    /**
     Constructs a DataQueue object.  The system and path properties must be set before using any method requiring a connection to the system.
     **/
    public DataQueue()
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing DataQueue object.");
    }

    /**
     Constructs a DataQueue object.
     @param  system  The system object representing the system on which the data queue exists.
     @param  path  The fully qualified integrated file system path name of the data queue.  The library and queue name must each be 10 characters or less.
     **/
    public DataQueue(AS400 system, String path)
    {
        super(system, path);
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing DataQueue object.");
    }

    /**
     Creates a sequential data queue on the system.  The queue will be created with the attributes provided.
     @param  attributes  The attributes of the data queue to be created.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  IllegalObjectTypeException  If the object on the system is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectAlreadyExistsException  If the object already exists on the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void create(DataQueueAttributes attributes) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectAlreadyExistsException, ObjectDoesNotExistException
    {
        if (attributes.getKeyLength() > 0)
        {
            Trace.log(Trace.ERROR, "Using DataQueue for keyed data queue.");
            throw new IllegalObjectTypeException(IllegalObjectTypeException.DATA_QUEUE_KEYED);
        }
        create(attributes.getEntryLength(), attributes.getAuthority(), attributes.isSaveSenderInfo(), attributes.isFIFO(), attributes.isForceToAuxiliaryStorage(), attributes.getDescription());
    }

    /**
     Creates a sequential data queue on the system.  The queue will be created with the following attributes: authority = *LIBCRTAUT, saveSenderInformation = false, FIFO = true, forceToAuxiliaryStorage = false, description = (50 blanks).
     @param  maxEntryLength  The maximum number of bytes per data queue entry.  Valid values are 1-64512.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectAlreadyExistsException  If the object already exists on the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void create(int maxEntryLength) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectAlreadyExistsException, ObjectDoesNotExistException
    {
        create(maxEntryLength, "*LIBCRTAUT", false, true, false, "");
    }

    /**
     Creates a sequential data queue on the system.
     @param  maxEntryLength  The maximum number of bytes per data queue entry.  Valid values are 1-64512.
     @param  authority  The public authority for the data queue. Valid values are *ALL, *CHANGE, *EXCLUDE, *USE, *LIBCRTAUT.
     @param  saveSenderInformation  true if entry origin information will be saved; false otherwise.
     @param  FIFO  true if queue entries are processed in FIFO order, false if queue entries are processed in LIFO order.
     @param  forceToAuxiliaryStorage  true if writes are forced to storage before return; false otherwise.
     @param  description  The text description.  This string must be 50 characters or less.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectAlreadyExistsException  If the object already exists on the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void create(int maxEntryLength, String authority, boolean saveSenderInformation, boolean FIFO, boolean forceToAuxiliaryStorage, String description) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectAlreadyExistsException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Creating data queue.");

        // Check parmameters.
        if (maxEntryLength < 1 || maxEntryLength > 64512)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'maxEntryLength' is not valid:", maxEntryLength);
            throw new ExtendedIllegalArgumentException("maxEntryLength (" + maxEntryLength + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }

        if (authority == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'authority' is null.");
            throw new NullPointerException("authority");
        }
        authority = authority.toUpperCase().trim();
        if (!authority.equals("*LIBCRTAUT") && !authority.equals("*ALL") && !authority.equals("*CHANGE") && !authority.equals("*EXCLUDE") && !authority.equals("*USE"))
        {
            Trace.log(Trace.ERROR, "Value of parameter 'authority' is not valid: " + authority);
            throw new ExtendedIllegalArgumentException("authority (" + authority + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        if (description == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'description' is null.");
            throw new NullPointerException("description");
        }
        if (description.length() > 50)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'description' is not valid: " + description);
            throw new ExtendedIllegalArgumentException("description (" + description + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }

        // Start a socket connection to the system.
        chooseImpl();

        // Don't commit to the change in attributes until the create completes.
        attributesRetrieved_ = false;

        // Save attribute values.
        maxEntryLength_ = maxEntryLength;
        saveSenderInformation_ = saveSenderInformation;
        FIFO_ = FIFO;
        forceToAuxiliaryStorage_ = forceToAuxiliaryStorage;
        description_ = description;

        // Send create request.
        impl_.create(maxEntryLength, authority, saveSenderInformation, FIFO, 0, forceToAuxiliaryStorage, description);

        if (objectListeners_ != null) fireObjectEvent(ObjectEvent.OBJECT_CREATED);
        // Attributes are complete and official.
        attributesRetrieved_ = true;
    }

    /**
     Reads an entry from the data queue without removing it from the queue.  This method will not wait for entries if none are on the queue.
     @return  The entry read from the queue.  If no entries were available, null is returned.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  IllegalObjectTypeException  If the object on the system is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public DataQueueEntry peek() throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        return peek(0);
    }

    /**
     Reads an entry from the data queue without removing it from the queue.
     @param  wait  The number of seconds to wait if the queue contains no entries.  Negative one (-1) means to wait until an entry is available.
     @return  The entry read from the queue.  If no entries were available, null is returned.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  IllegalObjectTypeException  If the object on the system is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public DataQueueEntry peek(int wait) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Peeking data queue.");

        // Check parmameters.
        if (wait < -1)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'wait' is not valid:", wait);
            throw new ExtendedIllegalArgumentException("wait (" + wait + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        open();
        // Send request.
        DQReceiveRecord record = impl_.read(null, wait, true, null);
        if (record == null) return null;

        DataQueueEntry entry = new DataQueueEntry(this, record.data_, record.senderInformation_);
        if (dataQueueListeners_ != null) fireDataQueueEvent(DataQueueEvent.DQ_PEEKED);
        return entry;
    }

    /**
     Reads an entry from the data queue and removes it from the queue.  This method will not wait for entries if none are on the queue.
     @return  The entry read from the queue.  If no entries were available, null is returned.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  IllegalObjectTypeException  If the object on the system is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public DataQueueEntry read() throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        return read(0);
    }

    /**
     Reads an entry from the data queue and removes it from the queue.
     @param  wait  The number of seconds to wait if the queue contains no entries.  Negative one (-1) means to wait until an entry is available.
     @return  The entry read from the queue.  If no entries were available, null is returned.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  IllegalObjectTypeException  If the object on the system is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public DataQueueEntry read(int wait) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Reading data queue.");

        // Check parmameters.
        if (wait < -1)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'wait' is not valid:", wait);
            throw new ExtendedIllegalArgumentException("wait (" + wait + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        open();
        // Send request.
        DQReceiveRecord record = impl_.read(null, wait, false, null);
        if (record == null) return null;

        DataQueueEntry entry = new DataQueueEntry(this, record.data_, record.senderInformation_);
        if (dataQueueListeners_ != null) fireDataQueueEvent(DataQueueEvent.DQ_READ);
        return entry;
    }

    // Retrieves the attributes of the data queue.  This method assumes that the connection to the system has been started.  It must only be called by open().
    void retrieveAttributes() throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Retrieving data queue attributes.");
        // Send retrieve attribute request.
        DQQueryRecord record = impl_.retrieveAttributes(false);

        maxEntryLength_ = record.maxEntryLength_;
        saveSenderInformation_ = record.saveSenderInformation_;
        FIFO_ = record.FIFO_;
        forceToAuxiliaryStorage_ = record.forceToAuxiliaryStorage_;
        description_ = record.description_;

        attributesRetrieved_ = true;
    }

    /**
     Returns the String representation of this data queue object.
     @return  The String representation of this data queue object.
     **/
    public String toString()
    {
        return "DataQueue " + super.toString();
    }

    /**
     Writes an entry to the data queue.
     @param  data  The array of bytes to write to the queue.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  IllegalObjectTypeException  If the object on the system is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void write(byte[] data) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Writing data queue.");

        // Check parmameters.
        if (data == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'data' is null.");
            throw new NullPointerException("data");
        }
        if (data.length > 64512)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'data' is not valid:", data.length);
            throw new ExtendedIllegalArgumentException("data.length (" + data.length + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }

        open();
        // Send write request.
        impl_.write(null, data);
        if (dataQueueListeners_ != null) fireDataQueueEvent(DataQueueEvent.DQ_WRITTEN);
    }

    /**
     Writes a string entry to the data queue.
     @param  data  The string to write to the queue.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  IllegalObjectTypeException  If the object on the system is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void write(String data) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        // Check parmameters.
        if (data == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'data' is null.");
            throw new NullPointerException("data");
        }

        write(stringToByteArray(data));
    }
}
