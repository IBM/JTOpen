///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: DataQueue.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;

/**
 The DataQueue class represents an AS/400 data queue object.
 **/
public class DataQueue extends BaseDataQueue
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    /**
     Constructs a DataQueue object.  The system and path properties will need to be set before using any method requiring a connection to the AS/400.
     **/
    public DataQueue()
    {
        super();
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Constructing DataQueue object.");
    }

    /**
     Constructs a DataQueue object.  Depending on how the AS400 object was constructed, the user may need to be prompted for the system name, user ID, or password when any method requiring a connection to the AS/400 is done.
     @param  system  The AS/400 system on which the data queue exists.
     @param  path  The fully qualified integrated file system path name of the data queue.  The library and queue name must each be 10 characters or less.
     **/
    public DataQueue(AS400 system, String path)
    {
        super(system, path);
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Constructing DataQueue object.");
    }

    /**
     Creates a data queue on the AS/400.  The queue will be created with the attributes provided.
     @param  attributes  The attributes of the data queue to be created.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ConnectionDroppedException  If the connection is dropped unexpectedly.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the AS/400.
     @exception  IllegalObjectTypeException  If the AS/400 object is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectAlreadyExistsException  If the AS/400 object already exists.
     @exception  ObjectDoesNotExistException  If the AS/400 object does not exist.
     @exception  ServerStartupException  If the AS/400 server cannot be started.
     @exception  UnknownHostException  If the AS/400 system cannot be located.
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
     Creates a data queue on the AS/400.  The queue will be created with the following attributes: authority = *LIBCRTAUT, saveSenderInformation = false, FIFO = true, forceToAuxiliaryStorage = false, description = (50 blanks).
     @param  maxEntryLength  The maximum number of bytes per data queue entry.  Valid values are 1-64512.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ConnectionDroppedException  If the connection is dropped unexpectedly.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the AS/400.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectAlreadyExistsException  If the AS/400 object already exists.
     @exception  ObjectDoesNotExistException  If the AS/400 object does not exist.
     @exception  ServerStartupException  If the AS/400 server cannot be started.
     @exception  UnknownHostException  If the AS/400 system cannot be located.
     **/
    public void create(int maxEntryLength) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectAlreadyExistsException, ObjectDoesNotExistException
    {
        create(maxEntryLength, "*LIBCRTAUT", false, true, false, "");
    }

    /**
     Creates a data queue on the AS/400.
     @param  maxEntryLength  The maximum number of bytes per data queue entry.  Valid values are 1-64512.
     @param  authority  The public authority for the data queue. Valid values are *ALL, *CHANGE, *EXCLUDE, *USE, *LIBCRTAUT.
     @param  saveSenderInformation  Determines if entry origin information will be saved.
     @param  FIFO  true if queue entries are processed in FIFO order, false if queue entries are processed in LIFO order.
     @param  forceToAuxiliaryStorage  true if writes are forced to storage before return.
     @param  description  The text description.  This string must be 50 characters or less.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ConnectionDroppedException  If the connection is dropped unexpectedly.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the AS/400.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectAlreadyExistsException  If the AS/400 object already exists.
     @exception  ObjectDoesNotExistException  If the AS/400 object does not exist.
     @exception  ServerStartupException  If the AS/400 server cannot be started.
     @exception  UnknownHostException  If the AS/400 system cannot be located.
     **/
    public void create(int maxEntryLength, String authority, boolean saveSenderInformation, boolean FIFO, boolean forceToAuxiliaryStorage, String description) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectAlreadyExistsException, ObjectDoesNotExistException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Creating data queue.");

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
        if (!(authority.equals("*ALL") || authority.equals("*CHANGE") || authority.equals("*EXCLUDE") || authority.equals("*USE") || authority.equals("*LIBCRTAUT")))
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

        // Start a socket connection to the server.
        connect();

        // Don't commit to the change in attributes until the create completes.
        attributesRetrieved_ = false;

        // Save attribute values.
        maxEntryLength_ = maxEntryLength;
        saveSenderInformation_ = saveSenderInformation;
        FIFO_ = FIFO;
        forceToAuxiliaryStorage_ = forceToAuxiliaryStorage;
        description_ = description;

        // Send create request.
        impl_.processCreate(maxEntryLength, authority, saveSenderInformation, FIFO, 0, forceToAuxiliaryStorage, description);

        fireCreated();
        // Attributes are complete and official.
        attributesRetrieved_ = true;
    }

    /**
     Reads an entry from the data queue without removing it from the queue.  This method will not wait for entries if none are on the queue.  An exception is thrown when running to a pre-V4R5 server if the maximum length of a message on the queue is greater than 31744 bytes.
     @return  The entry read from the queue.  If no entries were available, null is returned.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ConnectionDroppedException  If the connection is dropped unexpectedly.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the AS/400.
     @exception  IllegalObjectTypeException  If the AS/400 object is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the AS/400 object does not exist.
     **/
    public DataQueueEntry peek() throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        return peek(0);
    }

    /**
     Reads an entry from the data queue without removing it from the queue.  An exception is thrown when running to a pre-V4R5 server if the maximum length of a message on the queue is greater than 31744 bytes.
     @param  wait  The number of seconds to wait if the queue contains no entries.  Negative one (-1) means to wait until an entry is available.
     @return  The entry read from the queue.  If no entries were available, null is returned.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ConnectionDroppedException  If the connection is dropped unexpectedly.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the AS/400.
     @exception  IllegalObjectTypeException  If the AS/400 object is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the AS/400 object does not exist.
     **/
    public DataQueueEntry peek(int wait) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Peeking data queue.");

        // Check parmameters.
        if (wait < -1)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'wait' is not valid:", wait);
            throw new ExtendedIllegalArgumentException("wait (" + wait + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        open();
        // Send request.
        DQReceiveRecord record = impl_.processRead(null, wait, true, null, saveSenderInformation_);
        if (record == null) return null;

        DataQueueEntry entry = new DataQueueEntry(this, record.data_, record.senderInformation_);
        firePeek();
        return entry;
    }

    /**
     Reads an entry from the data queue and removes it from the queue.  This method will not wait for entries if none are on the queue.  An exception is thrown when running to a pre-V4R5 server if the maximum length of a message on the queue is greater than 31744 bytes.
     @return  The entry read from the queue.  If no entries were available, null is returned.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ConnectionDroppedException  If the connection is dropped unexpectedly.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the AS/400.
     @exception  IllegalObjectTypeException  If the AS/400 object is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the AS/400 object does not exist.
     **/
    public DataQueueEntry read() throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        return read(0);
    }

    /**
     Reads an entry from the data queue and removes it from the queue.  An exception is thrown when running to a pre-V4R5 server if the maximum length of a message on the queue is greater than 31744 bytes.
     @param  wait  The number of seconds to wait if the queue contains no entries.  Negative one (-1) means to wait until an entry is available.
     @return  The entry read from the queue.  If no entries were available, null is returned.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ConnectionDroppedException  If the connection is dropped unexpectedly.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the AS/400.
     @exception  IllegalObjectTypeException  If the AS/400 object is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the AS/400 object does not exist.
     **/
    public DataQueueEntry read(int wait) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Reading data queue.");

        // Check parmameters.
        if (wait < -1)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'wait' is not valid:", wait);
            throw new ExtendedIllegalArgumentException("wait (" + wait + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        open();
        // Send request.
        DQReceiveRecord record = impl_.processRead(null, wait, false, null, saveSenderInformation_);
        if (record == null) return null;

        DataQueueEntry entry = new DataQueueEntry(this, record.data_, record.senderInformation_);
        fireRead();
        return entry;
    }

    // Retrieves the attributes of the data queue.  This method assumes that the connection to the server has been started.  It must only be called by open().
    void retrieveAttributes() throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Retrieving data queue attributes.");
        // Send retrieve attribute request.
        DQQueryRecord record = impl_.processRetrieveAttrs(false);

        maxEntryLength_ = record.maxEntryLength;
        saveSenderInformation_ = record.saveSenderInformation;
        FIFO_ = record.FIFO;
        forceToAuxiliaryStorage_ = record.forceToAuxiliaryStorage;
        description_ = record.description;

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
     @exception  ConnectionDroppedException  If the connection is dropped unexpectedly.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the AS/400.
     @exception  IllegalObjectTypeException  If the AS/400 object is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the AS/400 object does not exist.
     **/
    public void write(byte[] data) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Writing data queue.");

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
        impl_.processWrite(null, data);
        fireWritten();
    }

    /**
     Writes a string entry to the data queue.
     @param  data  The string to write to the queue.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ConnectionDroppedException  If the connection is dropped unexpectedly.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the AS/400.
     @exception  IllegalObjectTypeException  If the AS/400 object is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the AS/400 object does not exist.
     @exception  UnsupportedEncodingException  If the <i>ccsid</i> is not supported.
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
