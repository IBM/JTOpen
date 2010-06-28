///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  BaseDataQueueImplNative.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2003 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;

// Native implementation of data queues.
class BaseDataQueueImplNative extends BaseDataQueueImplRemote
{
  private static final String CLASSNAME = "com.ibm.as400.access.BaseDataQueueImplNative";
  static
  {
    if (Trace.traceOn_) Trace.logLoadPath(CLASSNAME);
  }

    static
    {
 	   NativeMethods.loadNativeLibraryQyjspart(); 
    }

    private String name_;
    private String library_;

    // Set needed impl properties.
    public void setSystemAndPath(AS400Impl system, String path, String name, String library) throws IOException
    {
        super.setSystemAndPath(system, path, name, library);
        name_ = name;
        library_ = library;
    }

    // Native implementation of clear.
    // If key is null, do non-keyed clear.
    public void clear(byte[] key) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        // Call native method.
        byte[] swapToPH = new byte[12];
        byte[] swapFromPH = new byte[12];
        boolean didSwap = system_.swapTo(swapToPH, swapFromPH);
        try
        {
            if (key == null)
            {
                clearNative(libraryBytes_, queueNameBytes_);
            }
            else
            {
                clearKeyNative(libraryBytes_, queueNameBytes_, key);
            }
        }
        catch (NativeException e)  // Exception found by C code.
        {
            throw buildException(key != null, e);
        }
        finally
        {
            if (didSwap) system_.swapBack(swapToPH, swapFromPH);
        }
    }

    // Native implementation of create.
    // KeyLength == 0 means non-keyed queue.
    public void create(int maxEntryLength, String authority, boolean saveSenderInformation, boolean FIFO, int keyLength, boolean forceToAuxiliaryStorage, String description) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectAlreadyExistsException, ObjectDoesNotExistException
    {
        // Build command string.
        String command = "QSYS/CRTDTAQ DTAQ(" + library_ + "/" + name_ + ") TYPE(*STD) MAXLEN(" + maxEntryLength + ") FORCE(" + (forceToAuxiliaryStorage ? "*YES" : "*NO") + ") SEQ(" + (keyLength == 0 ? FIFO ? "*FIFO" : "*LIFO" : "*KEYED) KEYLEN(" + keyLength) + ") SENDERID(" + (saveSenderInformation ? "*YES" : "*NO") + ") TEXT('" + description + "') AUT(" + authority + ")";

        // Convert command to EBCDIC bytes.
        byte[] commandBytes = converter_.stringToByteArray(command);

        // Call native method.
        byte[] swapToPH = new byte[12];
        byte[] swapFromPH = new byte[12];
        boolean didSwap = system_.swapTo(swapToPH, swapFromPH);
        try
        {
            createNative(commandBytes);
        }
        catch (NativeException e)  // Exception found by C code.
        {
            String id = converter_.byteArrayToString(e.data, 12, 7);
            if (id.equals("CPF9870")) // Object already exists.
            {
                throw new ObjectAlreadyExistsException(path_, ObjectAlreadyExistsException.OBJECT_ALREADY_EXISTS);
            }
            throw buildException(e);
        }
        finally
        {
            if (didSwap) system_.swapBack(swapToPH, swapFromPH);
        }
    }

    // Native implementation of delete.
    public void delete() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
        // Build command string.
        String command = "QSYS/DLTDTAQ DTAQ("+ library_ + "/"+ name_ + ")";

        // Convert to ebcdic bytes.
        byte[] commandBytes = converter_.stringToByteArray(command);

        // Call native method.
        byte[] swapToPH = new byte[12];
        byte[] swapFromPH = new byte[12];
        boolean didSwap = system_.swapTo(swapToPH, swapFromPH);
        try
        {
            deleteNative(commandBytes);
        }
        catch (NativeException e)  // Exception found by C code.
        {
            throw buildException(e);
        }
        finally
        {
            if (didSwap) system_.swapBack(swapToPH, swapFromPH);
        }
    }

    // Native implementation of read for data queues.
    // Key == null means non-keyed queue.
    // Boolean peek determines peek or read.
    // Returns The entry read, or null if no entries on the queue.
    public DQReceiveRecord read(String search, int wait, boolean peek, byte[] key) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        // Convert parameters to EBCDIC bytes.
        byte[] searchBytes = (key == null) ? new byte[2] : converter_.stringToByteArray(search);

        // Copy to new key in case found key is different that provided key.
        byte[] newKey = null;
        if (key != null)
        {
            newKey = new byte[key.length];
            System.arraycopy(key, 0, newKey, 0, key.length);
        }

        byte[] swapToPH = new byte[12];
        byte[] swapFromPH = new byte[12];
        boolean didSwap = system_.swapTo(swapToPH, swapFromPH);
        try
        {
            DQQueryRecord qr = retrieveAttributes(key != null);
            if (qr.saveSenderInformation_)
            {
                // Call native methods with sender info.
                byte[] senderBytes = new byte[36];
                byte[] data = (key == null) ? peek ? peekSenderNative(libraryBytes_, queueNameBytes_, wait, senderBytes) : readSenderNative(libraryBytes_, queueNameBytes_, wait, senderBytes) : peek ? peekKeyedSenderNative(libraryBytes_, queueNameBytes_, wait, searchBytes, newKey, senderBytes) : readKeyedSenderNative(libraryBytes_, queueNameBytes_, wait, searchBytes, newKey, senderBytes);
                if (data.length == 0) return null;
                return new DQReceiveRecord(converter_.byteArrayToString(senderBytes), data, newKey);
            }
            // Call native methods with out sender info.
            byte[] data = (key == null) ? peek ? peekNative(libraryBytes_, queueNameBytes_, wait) : readNative(libraryBytes_, queueNameBytes_, wait) : peek ? peekKeyedNative(libraryBytes_, queueNameBytes_, wait, searchBytes, newKey) : readKeyedNative(libraryBytes_, queueNameBytes_, wait, searchBytes, newKey);
            if (data.length == 0) return null;
            return new DQReceiveRecord(null, data, newKey);
        }
        catch (NativeException e) // Exception found by C code.
        {
            throw buildException(key != null, e);
        }
        finally
        {
            if (didSwap) system_.swapBack(swapToPH, swapFromPH);
        }
    }

    // Native implementation for retrieve attributes.
    // Keyed is false for non-keyed queues.
    public DQQueryRecord retrieveAttributes(boolean keyed) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        byte[] attributes = new byte[61];

        // Call native method.
        byte[] swapToPH = new byte[12];
        byte[] swapFromPH = new byte[12];
        boolean didSwap = system_.swapTo(swapToPH, swapFromPH);
        try
        {
            retrieveAttributesNative(libraryBytes_, queueNameBytes_, attributes);
        }
        catch (NativeException e)  // Exception found in C code.
        {
            throw buildException(e);
        }
        finally
        {
            if (didSwap) system_.swapBack(swapToPH, swapFromPH);
        }

        // Verify data queue type and set attribute.
        DQQueryRecord ret = new DQQueryRecord();
        if (keyed)
        {
            if (attributes[8] != (byte)0xD2)  // Actual data queue is not a keyed data queue.
            {
                Trace.log(Trace.ERROR, "Using KeyedDataQueue for non-keyed data queue.");
                throw new IllegalObjectTypeException(IllegalObjectTypeException.DATA_QUEUE_NOT_KEYED);
            }
            ret.FIFO_ = true; // Keyed queues always FIFO.
        }
        else
        {
            if (attributes[8] == (byte)0xC6) // FIFO.
            {
                ret.FIFO_ = true;
            }
            else if (attributes[8] == (byte)0xD3) // LIFO.
            {
                ret.FIFO_ = false;
            }
            else // Queue is keyed and this is not a KeyedDataQueue object; error.
            {
                Trace.log(Trace.ERROR, "Using DataQueue for keyed data queue.");
                throw new IllegalObjectTypeException(IllegalObjectTypeException.DATA_QUEUE_KEYED);
            }
        }

        // Set attributes based on returned info.
        ret.maxEntryLength_ = BinaryConverter.byteArrayToInt(attributes, 0);
        ret.saveSenderInformation_ = attributes[9] == (byte)0xD5 ? false : true;
        ret.forceToAuxiliaryStorage_ = attributes[10] == (byte)0xD5 ? false : true;
        ret.description_ =  converter_.byteArrayToString(attributes, 11, 50);
        ret.keyLength_ = BinaryConverter.byteArrayToInt(attributes, 4);
        return ret;
    }

    // Native implementation for write.
    // Key is null for non-keyed queues.
    public void write(byte[] key, byte[] data) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        byte[] swapToPH = new byte[12];
        byte[] swapFromPH = new byte[12];
        boolean didSwap = system_.swapTo(swapToPH, swapFromPH);
        try
        {
            // Call native method.
            if (key == null)
            {
                writeNative(libraryBytes_, queueNameBytes_, data);
            }
            else
            {
                writeKeyNative(libraryBytes_, queueNameBytes_, key, data);
            }
        }
        catch (NativeException e)  // Exception detected in C code.
        {
            throw buildException(key != null, e);
        }
        finally
        {
            if (didSwap) system_.swapBack(swapToPH, swapFromPH);
        }
    }

    // Return or throw an exception based on the data received from the native method.
    // This function returns an AS400Exception and throw all others.
    private AS400Exception buildException(NativeException e) throws AS400SecurityException, ObjectDoesNotExistException
    {
        // Parse information from byte array.
       String id = converter_.byteArrayToString(e.data, 12, 7);

       int substitutionDataLength = BinaryConverter.byteArrayToInt(e.data, 80);
       int textLength = BinaryConverter.byteArrayToInt(e.data, 88);

       String text = converter_.byteArrayToString(e.data, 112 + substitutionDataLength, textLength);

        if (id.equals("CPF9801") || // Object &2 in library &3 not found.
            id.equals("CPF2105") || // Object &1 in &2 type *&3 not found.
            id.equals("CPF9805"))   // Object &2 in library &3 destroyed.
        {
            throw new ObjectDoesNotExistException(path_, ObjectDoesNotExistException.OBJECT_DOES_NOT_EXIST);
        }
        if (id.equals("CPF9810")) // Library &1 not found.
        {
            throw new ObjectDoesNotExistException(path_, ObjectDoesNotExistException.LIBRARY_DOES_NOT_EXIST);
        }
        if (id.equals("CPF9802") || // Not authorized to object &2 in &3.
            id.equals("CPF2189"))   // Not authorized to object &1 in &2 type *&3.
        {
            throw new AS400SecurityException(path_, AS400SecurityException.OBJECT_AUTHORITY_INSUFFICIENT);
        }
        if (id.equals("CPF9820") || // Not authorized to use library &1.
            id.equals("CPF2182"))   // Not authorized to library &1.
        {
            throw new AS400SecurityException(path_, AS400SecurityException.LIBRARY_AUTHORITY_INSUFFICIENT);
        }
        AS400Message msg = new AS400Message(id, text);
        msg.setType((e.data[19] & 0x0F) * 10 + (e.data[20] & 0x0F));
        msg.setSeverity(BinaryConverter.byteArrayToInt(e.data, 8));
        return new AS400Exception(msg);
    }

    // Build Exception as above, plus detect object type mismatch.
    private AS400Exception buildException(boolean expectKeyed, NativeException e) throws AS400SecurityException, IllegalObjectTypeException, ObjectDoesNotExistException
    {
        // Parse information from byte array.
        String id = converter_.byteArrayToString(e.data, 12, 7);

        if (expectKeyed && id.equals("CPF9502"))
        {
            Trace.log(Trace.ERROR, "Using KeyedDataQueue for non-keyed data queue: " + path_);
            throw new IllegalObjectTypeException(path_, IllegalObjectTypeException.DATA_QUEUE_NOT_KEYED);
        }
        if (!expectKeyed && id.equals("CPF9506"))
        {
            Trace.log(Trace.ERROR, "Using DataQueue for keyed data queue: " + path_);
            throw new IllegalObjectTypeException(path_, IllegalObjectTypeException.DATA_QUEUE_KEYED);
        }
        return buildException(e);
    }

    private native void clearNative(byte[] library, byte[] name) throws NativeException;
    private native void clearKeyNative(byte[] library, byte[] name, byte[] key) throws NativeException;
    private native void createNative(byte[] command) throws NativeException;
    private native void deleteNative(byte[] command) throws NativeException;
    private native byte[] peekKeyedSenderNative(byte[] library, byte[] name, int wait, byte[] search, byte[] key, byte[] sender) throws NativeException;
    private native byte[] readKeyedSenderNative(byte[] library, byte[] name, int wait, byte[] search, byte[] key, byte[] sender) throws NativeException;
    private native byte[] peekKeyedNative(byte[] library, byte[] name, int wait, byte[] search, byte[] key) throws NativeException;
    private native byte[] readKeyedNative(byte[] library, byte[] name, int wait, byte[] search, byte[] key) throws NativeException;
    private native byte[] peekSenderNative(byte[] library, byte[] name, int wait, byte[] sender) throws NativeException;
    private native byte[] readSenderNative(byte[] library, byte[] name, int wait, byte[] sender) throws NativeException;
    private native byte[] peekNative(byte[] library, byte[] name, int wait) throws NativeException;
    private native byte[] readNative(byte[] library, byte[] name, int wait) throws NativeException;
    private native void retrieveAttributesNative(byte[] library, byte[] name, byte[] attributes) throws NativeException;
    private native void writeNative(byte[] library, byte[] name, byte[] data) throws NativeException;
    private native void writeKeyNative(byte[] library, byte[] name, byte[] key, byte[] data) throws NativeException;
}
