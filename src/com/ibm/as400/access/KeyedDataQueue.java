//////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  KeyedDataQueue.java
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
 The KeyedDataQueue class represents an iSeries server keyed data queue object.
 **/
public class KeyedDataQueue extends BaseDataQueue
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    static final long serialVersionUID = 4L;

    //The length of the key for each entry in the queue.
    int keyLength_ = 0;

    /**
     Constructs a KeyedDataQueue object.  The system and path properties must be set before using any method requiring a connection to the server.
     **/
    public KeyedDataQueue()
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing KeyedDataQueue object.");
    }

    /**
     Constructs a KeyedDataQueue object.
     @param  system  The system object representing the server on which the data queue exists.
     @param  path  The fully qualified integrated file system path name of the data queue.  The library and queue name must each be 10 characters or less.
     **/
    public KeyedDataQueue(AS400 system, String path)
    {
        super(system, path);
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing KeyedDataQueue object.");
    }

    // Convert a key to EBCDIC and pad to the correct length.
    private byte[] convertKey(String key) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        int keyLength = getKeyLength();  // Potential connection to data queue server.
        byte[] keyBytes = stringToByteArray(key);
        if (keyBytes.length == keyLength)
        {
            return keyBytes;
        }
        else if (keyBytes.length < keyLength)
        {
            byte[] copy = new byte[keyLength];
            System.arraycopy(keyBytes, 0, copy, 0, keyBytes.length);
            return copy;
        }
        else
        {
            Trace.log(Trace.ERROR, "Length of parameter 'key' is not valid: " + key);
            throw new ExtendedIllegalArgumentException("key (" + key + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
    }

    /**
     Removes all entries that match the key from the data queue.
     @param  key  The key with which data queue entries will be compared.  All entries whose key is equal to this parameter will be removed from the queue.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  IllegalObjectTypeException  If the object on the server is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public void clear(byte[] key) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Clearing keyed data queue.");

        // Check parameters.
        if (key == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'key' is null.");
            throw new NullPointerException("key");
        }
        if (key.length > 256)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'key' is not valid:", key.length);
            throw new ExtendedIllegalArgumentException("key.length (" + key.length + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }

        open();
        // Send clear request.
        impl_.clear(key);
        if (dataQueueListeners_ != null) fireDataQueueEvent(DataQueueEvent.DQ_CLEARED);
    }

    /**
     Removes all entries that match the key from the data queue.
     @param  key  The key with which data queue entries will be compared.  All entries whose key is equal to this parameter will be removed from the queue.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  IllegalObjectTypeException  If the object on the server is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public void clear(String key) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        // Check parameters.
        if (key == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'key' is null.");
            throw new NullPointerException("key");
        }
        clear(convertKey(key));
    }

    /**
     Creates a keyed data queue on the server.  The queue will be created with the following attributes: authority = *LIBCRTAUT, saveSenderInformation = false, forceToAuxiliaryStorage = false, description = (50 blanks).
     @param  keyLength  The number of bytes per data queue key. Valid values are 1-256.
     @param  maxEntryLength  The maximum number of bytes per data queue entry.  Valid values are 1-64512.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectAlreadyExistsException  If the object already exists on the server.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public void create(int keyLength, int maxEntryLength) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectAlreadyExistsException, ObjectDoesNotExistException
    {
        create(keyLength, maxEntryLength, "*LIBCRTAUT", false, false, "");
    }

    /**
     Creates a keyed data queue on the server.
     @param  keyLength  The number of bytes per data queue key. Valid values are 1-256.
     @param  maxEntryLength  The maximum number of bytes per data queue entry.  Valid values are 1-64512.
     @param  authority  The public authority for the data queue.  Valid values are *ALL, *CHANGE, *EXCLUDE, *USE, *LIBCRTAUT.
     @param  saveSenderInformation  Determines if entry origin information will be saved.
     @param  forceToAuxiliaryStorage  true if writes are forced to storage before return; false otherwise.
     @param  description  The text description.  This string must be 50 characters or less.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectAlreadyExistsException  If the object already exists on the server.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public void create(int keyLength, int maxEntryLength, String authority, boolean saveSenderInformation, boolean forceToAuxiliaryStorage, String description) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectAlreadyExistsException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Creating keyed data queue.");

        // Check parameters.
        if (keyLength < 1 || keyLength > 256)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'keyLength' is not valid:", keyLength);
            throw new ExtendedIllegalArgumentException("keyLength (" + keyLength + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }
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

        chooseImpl();

        // Don't commit to the change in attributes till the create completes.
        attributesRetrieved_ = false;

        // Save attribute values.
        maxEntryLength_ = maxEntryLength;
        saveSenderInformation_ = saveSenderInformation;
        FIFO_ = true;   // Keyed queues always FIFO.
        forceToAuxiliaryStorage_ = forceToAuxiliaryStorage;
        description_ = description;

        keyLength_ = keyLength;

        // Send create request.
        impl_.create(maxEntryLength, authority, saveSenderInformation, true, keyLength, forceToAuxiliaryStorage, description);

        if (objectListeners_ != null) fireObjectEvent(ObjectEvent.OBJECT_CREATED);
        // Attributes are complete and official.
        attributesRetrieved_ = true;
    }

    /**
     Returns the length of the keys (in bytes) on this queue.
     @return  The length of the keys.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  IllegalObjectTypeException  If the object on the server is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public int getKeyLength() throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting key length.");
        open();
        if (!attributesRetrieved_)
        {
            retrieveAttributes();
        }
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Key length:", keyLength_);
        return keyLength_;
    }

    /**
     Reads an entry from the data queue without removing it from the queue.  This method will not wait for entries if there are none on the queue.
     @param  key  The array that contains the key used to search for an entry.  An entry must have a key equal to this value to be read.
     @return  The entry read from the queue.  If no entries were available, null is returned.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  IllegalObjectTypeException  If the object on the server is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public KeyedDataQueueEntry peek(byte[] key) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        return peek(key, 0, "EQ");
    }

    /**
     Reads an entry from the data queue without removing it from the queue.
     @param  key  The array that contains the key used to search for an entry.
     @param  wait  The number of seconds to wait if the queue contains no entries.  Negative one (-1) indicates to wait until an entry is available.
     @param  searchType  The type of comparison to use to determine if a key is a match.  Valid values are EQ (equal), NE (not equal), LT (less than), LE (less than or equal), GT (greater than), and GE (greater than or equal).
     @return  The entry read from the queue.  If no entries were available, null is returned.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  IllegalObjectTypeException  If the object on the server is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public KeyedDataQueueEntry peek(byte[] key, int wait, String searchType) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Peeking keyed data queue.");

        // Check parameters.
        if (key == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'key' is null.");
            throw new NullPointerException("key");
        }
        if (key.length > 256)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'key' is not valid:", key.length);
            throw new ExtendedIllegalArgumentException("key.length (" + key.length + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
        if (wait < -1)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'wait' is not valid:", wait);
            throw new ExtendedIllegalArgumentException("wait (" + wait + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        if (searchType == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'searchType' is null.");
            throw new NullPointerException("searchType");
        }
        String search = searchType.toUpperCase();
        if (!search.equals("EQ") && !search.equals("NE") && !search.equals("LT") && !search.equals("LE") && !search.equals("GT") && !search.equals("GE"))
        {
            Trace.log(Trace.ERROR, "Value of parameter 'searchType' is not valid: " + searchType);
            throw new ExtendedIllegalArgumentException("searchType (" + searchType + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        open();

        // Send request.
        DQReceiveRecord record = impl_.read(search, wait, true, key);
        if (record == null) return null;

        KeyedDataQueueEntry entry = new KeyedDataQueueEntry(this, record.key_, record.data_, record.senderInformation_);
        if (dataQueueListeners_ != null) fireDataQueueEvent(DataQueueEvent.DQ_PEEKED);
        return entry;
    }

    /**
     Reads an entry from the data queue without removing it from the queue.  This method will not wait for entries if there are none on the queue.
     @param  key  The string that contains the key used to search for an entry.  An entry must have a key equal to this value to be read.
     @return  The entry read from the queue.  If no entries were available, null is returned.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  IllegalObjectTypeException  If the object on the server is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public KeyedDataQueueEntry peek(String key) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        return peek(key, 0, "EQ");
    }

    /**
     Reads an entry from the data queue without removing it from the queue.
     @param  key  The string that contains the key used to search for an entry.
     @param  wait  The number of seconds to wait if the queue contains no entries.  Negative one (-1) means to wait until an entry is available.
     @param  searchType  The type of comparison to use to determine if a key is a match.  Valid values are EQ (equal), NE (not equal), LT (less than), LE (less than or equal), GT (greater than), and GE (greater than or equal).
     @return  The entry read from the queue.  If no entries were available, null is returned.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  IllegalObjectTypeException  If the object on the server is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public KeyedDataQueueEntry peek(String key, int wait, String searchType) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        // Check parameters.
        if (key == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'key' is null.");
            throw new NullPointerException("key");
        }

        return peek(convertKey(key), wait, searchType);
    }

    /**
     Reads an entry from the data queue and removes it from the queue.  This method will not wait for entries if there are none on the queue.
     @param  key  The array that contains the key used to search for an entry.  An entry must have a key equal to this value to be read.
     @return  The entry read from the queue.  If no entries were available, null is returned.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  IllegalObjectTypeException  If the object on the server is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public KeyedDataQueueEntry read(byte[] key) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        return read(key, 0, "EQ");
    }

    /**
     Reads an entry from the data queue and removes it from the queue.
     @param  key  The array that contains the key used to search for an entry.
     @param  wait  The number of seconds to wait if the queue contains no entries.  Negative one (-1) indicates to wait until an entry is available.
     @param  searchType  The type of comparison to use to determine if a key is a match.  Valid values are EQ (equal), NE (not equal), LT (less than), LE (less than or equal), GT (greater than), and GE (greater than or equal).
     @return  The entry read from the queue.  If no entries were available, null is returned.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  IllegalObjectTypeException  If the object on the server is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public KeyedDataQueueEntry read(byte[] key, int wait, String searchType) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Reading data queue.");

        // Check parameters.
        if (key == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'key' is null.");
            throw new NullPointerException("key");
        }
        if (key.length > 256)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'key' is not valid:", key.length);
            throw new ExtendedIllegalArgumentException("key.length (" + key.length + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
        if (wait < -1)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'wait' is not valid:", wait);
            throw new ExtendedIllegalArgumentException("wait (" + wait + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        if (searchType == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'searchType' is null.");
            throw new NullPointerException("searchType");
        }
        String search = searchType.toUpperCase();
        if (!search.equals("EQ") && !search.equals("NE") && !search.equals("LT") && !search.equals("LE") && !search.equals("GT") && !search.equals("GE"))
        {
            Trace.log(Trace.ERROR, "Value of parameter 'searchType' is not valid: " + searchType);
            throw new ExtendedIllegalArgumentException("searchType (" + searchType + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        open();
        // Send request.
        DQReceiveRecord record =  impl_.read(search, wait, false, key);
        if (record == null) return null;

        KeyedDataQueueEntry entry = new KeyedDataQueueEntry(this, record.key_, record.data_, record.senderInformation_);
        if (dataQueueListeners_ != null) fireDataQueueEvent(DataQueueEvent.DQ_READ);
        return entry;
    }

    /**
     Reads an entry from the data queue and removes it from the queue.  This method will not wait for entries if there are none on the queue.
     @param  key  The string that contains the key used to search for an entry.  An entry must have a key equal to this value to be read.
     @return  The entry read from the queue.  If no entries were available, null is returned.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  IllegalObjectTypeException  If the object on the server is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public KeyedDataQueueEntry read(String key) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        return read(key, 0, "EQ");
    }

    /**
     Reads an entry from the data queue and removes it from the queue.
     @param  key  The string that contains the key used to search for an entry.
     @param  wait  The number of seconds to wait if the queue contains no entries.  Negative one (-1) indicates to wait until an entry is available.
     @param  searchType  The type of comparison to use to determine if a key is a match.  Valid values are EQ (equal), NE (not equal), LT (less than), LE (less than or equal), GT (greater than), and GE (greater than or equal).
     @return  The entry read from the queue.  If no entries were available, null is returned.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  IllegalObjectTypeException  If the object on the server is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public KeyedDataQueueEntry read(String key, int wait, String searchType) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        // Check parameters.
        if (key == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'key' is null.");
            throw new NullPointerException("key");
        }
        return read(convertKey(key), wait, searchType);
    }

    // Retrieves the attributes of the data queue.  This method assumes that the connection to the server has been started.  It must only be called by open().
    void retrieveAttributes() throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Retrieving keyed data queue attributes.");
        // Send attribute request.
        DQQueryRecord record = impl_.retrieveAttributes(true);

        maxEntryLength_ = record.maxEntryLength_;
        saveSenderInformation_ = record.saveSenderInformation_;
        FIFO_ = true;   // Keyed queues always FIFO.
        forceToAuxiliaryStorage_ = record.forceToAuxiliaryStorage_;
        description_ = record.description_;

        keyLength_ = record.keyLength_;

        attributesRetrieved_ = true;
    }

    /**
     Returns the String representation of this keyed data queue object.
     @return  The String representation of this keyed data queue object.
     **/
    public String toString()
    {
        return "KeyedDataQueue " + super.toString();
    }

    /**
     Writes an entry to the data queue.
     @param  key  The array that contains the key for this entry.
     @param  data  The array of bytes to write to the queue.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  IllegalObjectTypeException  If the object on the server is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public void write(byte[] key, byte[] data) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Writing keyed data queue.");

        // Check parameters.
        if (key == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'key' is null.");
            throw new NullPointerException("key");
        }
        if (key.length > 256)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'key' is not valid:", key.length);
            throw new ExtendedIllegalArgumentException("key.length (" + key.length + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
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
        impl_.write(key, data);
        if (dataQueueListeners_ != null) fireDataQueueEvent(DataQueueEvent.DQ_WRITTEN);
    }

    /**
     Writes a string entry to the data queue.
     @param  key  The string that contains the key for this entry.
     @param  data  The string to write to the queue.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  IllegalObjectTypeException  If the object on the server is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public void write(String key, String data) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        // Check parameters.
        if (key == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'key' is null.");
            throw new NullPointerException("key");
        }
        if (data == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'data' is null.");
            throw new NullPointerException("data");
        }

        write(convertKey(key), stringToByteArray(data));
    }
}
