///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  BaseDataQueueImplNative.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2000 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;

// Native implementation of data queues
class BaseDataQueueImplNative implements BaseDataQueueImpl
{
    private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    static
    {
        System.load("/QSYS.LIB/QYJSPART.SRVPGM");
    }

    AS400ImplRemote system_;
    String path;
    String name;
    String library;
    ConverterImplRemote conv;
    byte[] libBytes;
    byte[] qnameBytes;

    // Set needed impl properties.
    public void setSystemAndPath(AS400Impl system, String path, String name, String library) throws IOException
    {
        system_ = (AS400ImplRemote)system;
        this.path = path;
        this.name = name;
        this.library = library;

        conv = ConverterImplRemote.getConverter(system_.getCcsid(), system_);
        libBytes = conv.stringToByteArray(library);
        qnameBytes = conv.stringToByteArray(name);
    }

    // Native implementation of clear
    // if key is null, do non-keyed clear
    public void processClear(byte[] key) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
        // call native method
        byte[] swapToPH = new byte[12];
        byte[] swapFromPH = new byte[12];
        boolean didSwap = system_.swapTo(swapToPH, swapFromPH);
        try
        {
            if (key == null)
            {
                clearNative(libBytes, qnameBytes);
            }
            else
            {
                clearKeyNative(libBytes, qnameBytes, key);
            }
        }
        catch (NativeException e)  // exception found by C code
        {
            throw buildException(e);
        }
        finally
        {
            if (didSwap) system_.swapBack(swapToPH, swapFromPH);
        }
    }

    // native implementation of create
    // keyLength == 0 means non-keyed queue
    public void processCreate(int maxEntryLength, String authority, boolean saveSenderInformation, boolean FIFO, int keyLength, boolean forceToAuxiliaryStorage, String description) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectAlreadyExistsException, ObjectDoesNotExistException
    {
        // build strings for options
        String forseStr = forceToAuxiliaryStorage ? "*YES" : "*NO";
        String seqStr = keyLength == 0 ? FIFO ? "*FIFO" : "*LIFO" : "*KEYED) KEYLEN(" + keyLength;
        String senderStr = saveSenderInformation ? "*YES" : "*NO";

        // build command string
        String cmd = "QSYS/CRTDTAQ DTAQ(" + library +
          "/" + name +
          ") TYPE(*STD) MAXLEN(" + maxEntryLength +
          ") FORCE(" + forseStr +
          ") SEQ(" + seqStr +
          ") SENDERID(" + senderStr +
          ") TEXT('" + description +
          "') AUT(" + authority +
          ")";

        // convert command to ebcdic bytes
        byte[] cmdBytes = conv.stringToByteArray(cmd);
        byte[] swapToPH = new byte[12];
        byte[] swapFromPH = new byte[12];
        boolean didSwap = system_.swapTo(swapToPH, swapFromPH);
        try
        {
            // call native method
            createNative(cmdBytes);
        }
        catch (NativeException e)  // exception found by C code
        {
            String id = conv.byteArrayToString(e.data, 12, 7);
            if (id.equals("CPF9870")) // Object already exists
            {
                throw new ObjectAlreadyExistsException(path, ObjectAlreadyExistsException.OBJECT_ALREADY_EXISTS);
            }
            throw buildException(e);
        }
        finally
        {
            if (didSwap) system_.swapBack(swapToPH, swapFromPH);
        }
    }

    // native implementation of delete
    public void processDelete() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
        // build cmd string
        String cmd = "QSYS/DLTDTAQ DTAQ("+ library + "/"+ name + ")";
        // convert to ebcdic bytes
        byte[] cmdBytes = conv.stringToByteArray(cmd);
        byte[] swapToPH = new byte[12];
        byte[] swapFromPH = new byte[12];
        boolean didSwap = system_.swapTo(swapToPH, swapFromPH);
        try
        {
            // call native method
            deleteNative(cmdBytes);
        }
        catch (NativeException e)  // exception found by C code
        {
            throw buildException(e);
        }
        finally
        {
            if (didSwap) system_.swapBack(swapToPH, swapFromPH);
        }
    }

    // native implementation of read for data queues
    // key == null means non-keyed queue
    // boolean peek determines peek or read
    // returns The entry read, or null if no entries on the queue.
    public DQReceiveRecord processRead(String search, int wait, boolean peek, byte[] key, boolean saveSenderInformation) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
        // convert parameters to ebcdic bytes
        byte[] searchBytes = (key == null) ? new byte[2] : conv.stringToByteArray(search);

        // copy to new key in case found key is different that provided key
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
            if (saveSenderInformation)
            {
                // call native methods with sender info
                byte[] senderBytes = new byte[36];
                byte[] data = (key == null) ? peek ? peekSenderNative(libBytes, qnameBytes, wait, senderBytes) : readSenderNative(libBytes, qnameBytes, wait, senderBytes) : peek ? peekKeyedSenderNative(libBytes, qnameBytes, wait, searchBytes, newKey, senderBytes) : readKeyedSenderNative(libBytes, qnameBytes, wait, searchBytes, newKey, senderBytes);
                if (data.length == 0) return null;
                return new DQReceiveRecord(conv.byteArrayToString(senderBytes), data, newKey);
            }
            // call native methods with out sender info
            byte[] data = (key == null) ? peek ? peekNative(libBytes, qnameBytes, wait) : readNative(libBytes, qnameBytes, wait) : peek ? peekKeyedNative(libBytes, qnameBytes, wait, searchBytes, newKey) : readKeyedNative(libBytes, qnameBytes, wait, searchBytes, newKey);
            if (data.length == 0) return null;
            return new DQReceiveRecord(null, data, newKey);
        }
        catch (NativeException e) // exception found by c code
        {
            throw buildException(e);
        }
        finally
        {
            if (didSwap) system_.swapBack(swapToPH, swapFromPH);
        }
    }

    // native implementation for retrieve attributes
    // keyed is false for non-keyed queues
    public DQQueryRecord processRetrieveAttrs(boolean keyed) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        byte[] attributes = new byte[61];
        byte[] swapToPH = new byte[12];
        byte[] swapFromPH = new byte[12];
        boolean didSwap = system_.swapTo(swapToPH, swapFromPH);
        try
        {
            // call native method
            retrieveAttributesNative(libBytes, qnameBytes, attributes);
        }
        catch (NativeException e)  // exception found in C code
        {
            throw buildException(e);
        }
        finally
        {
            if (didSwap) system_.swapBack(swapToPH, swapFromPH);
        }

        // verify data queue type and set attribute
        DQQueryRecord ret = new DQQueryRecord();
        if (keyed)
        {
            if (attributes[8] != (byte)0xD2)  // Actual data queue is not a keyed data queue
            {
                Trace.log(Trace.ERROR, "Using KeyedDataQueue for non-keyed data queue.");
                throw new IllegalObjectTypeException(IllegalObjectTypeException.DATA_QUEUE_NOT_KEYED);
            }
            ret.FIFO_ = true; // keyed queues always FIFO
        }
        else
        {
            if (attributes[8] == (byte)0xC6) // FIFO
            {
                ret.FIFO_ = true;
            }
            else if (attributes[8] == (byte)0xD3) // LIFO
            {
                ret.FIFO_ = false;
            }
            else // queue is keyed and this is not a KeyedDataQueue object; error
            {
                Trace.log(Trace.ERROR, "Using DataQueue for keyed data queue.");
                throw new IllegalObjectTypeException(IllegalObjectTypeException.DATA_QUEUE_KEYED);
            }
        }

        // set attributes based on returned info
        ret.maxEntryLength_ = BinaryConverter.byteArrayToInt(attributes, 0);
        ret.saveSenderInformation_ = attributes[9] == (byte)0xD5 ? false : true;
        ret.forceToAuxiliaryStorage_ = attributes[10] == (byte)0xD5 ? false : true;
        ret.description_ =  conv.byteArrayToString(attributes, 11, 50);
        ret.keyLength_ = BinaryConverter.byteArrayToInt(attributes, 4);
        return ret;
    }

    // native implementation for write
    // key is null for non-keyed queues
    public void processWrite(byte[] key, byte[] data) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
        byte[] swapToPH = new byte[12];
        byte[] swapFromPH = new byte[12];
        boolean didSwap = system_.swapTo(swapToPH, swapFromPH);
        try
        {
            // call native method
            if (key == null)
            {
                writeNative(libBytes, qnameBytes, data);
            }
            else
            {
                writeKeyNative(libBytes, qnameBytes, key, data);
            }
        }
        catch (NativeException e)  // exception detected in C code
        {
            throw buildException(e);
        }
        finally
        {
            if (didSwap) system_.swapBack(swapToPH, swapFromPH);
        }
    }

    // return or throw an exception based on the data received from the native method
    // this function returns an AS400Exception and throw all others
    AS400Exception buildException(NativeException e) throws AS400SecurityException, ObjectDoesNotExistException
    {
        // parse information from byte array
       String id = conv.byteArrayToString(e.data, 12, 7);

       int substitutionDataLength = BinaryConverter.byteArrayToInt(e.data, 80);
       int textLength = BinaryConverter.byteArrayToInt(e.data, 88);

       String text = conv.byteArrayToString(e.data, 112 + substitutionDataLength, textLength);

        if (id.equals("CPF9801") || // Object &2 in library &3 not found.
            id.equals("CPF2105") || // Object &1 in &2 type *&3 not found.
            id.equals("CPF9805"))   // Object &2 in library &3 destroyed.
        {
            throw new ObjectDoesNotExistException(path, ObjectDoesNotExistException.OBJECT_DOES_NOT_EXIST);
        }
        if (id.equals("CPF9810")) // Library &1 not found.
        {
            throw new ObjectDoesNotExistException(path, ObjectDoesNotExistException.LIBRARY_DOES_NOT_EXIST);
        }
        if (id.equals("CPF9802") || // Not authorized to object &2 in &3.
            id.equals("CPF2189"))   // Not authorized to object &1 in &2 type *&3.
        {
            throw new AS400SecurityException(path, AS400SecurityException.OBJECT_AUTHORITY_INSUFFICIENT);
        }
        if (id.equals("CPF9820") || // Not authorized to use library &1.
            id.equals("CPF2182"))   // Not authorized to library &1.
        {
            throw new AS400SecurityException(path, AS400SecurityException.LIBRARY_AUTHORITY_INSUFFICIENT);
        }
        AS400Message msg = new AS400Message(id, text);
        msg.setType((e.data[19] & 0x0F) * 10 + (e.data[20] & 0x0F));
        msg.setSeverity(BinaryConverter.byteArrayToInt(e.data, 8));
        return new AS400Exception(msg);
    }

    private native void clearNative(byte[] lib, byte[] qname) throws NativeException;
    private native void clearKeyNative(byte[] lib, byte[] qname, byte[] key) throws NativeException;
    private native void createNative(byte[] cmd) throws NativeException;
    private native void deleteNative(byte[] cmd) throws NativeException;
    private native byte[] peekKeyedSenderNative(byte[] lib, byte[] qname, int wait, byte[] search, byte[] key, byte[] sender) throws NativeException;
    private native byte[] readKeyedSenderNative(byte[] lib, byte[] qname, int wait, byte[] search, byte[] key, byte[] sender) throws NativeException;
    private native byte[] peekKeyedNative(byte[] lib, byte[] qname, int wait, byte[] search, byte[] key) throws NativeException;
    private native byte[] readKeyedNative(byte[] lib, byte[] qname, int wait, byte[] search, byte[] key) throws NativeException;
    private native byte[] peekSenderNative(byte[] lib, byte[] qname, int wait, byte[] sender) throws NativeException;
    private native byte[] readSenderNative(byte[] lib, byte[] qname, int wait, byte[] sender) throws NativeException;
    private native byte[] peekNative(byte[] lib, byte[] qname, int wait) throws NativeException;
    private native byte[] readNative(byte[] lib, byte[] qname, int wait) throws NativeException;
    private native void retrieveAttributesNative(byte[] lib, byte[] qname, byte[] attributes) throws NativeException;
    private native void writeNative(byte[] lib, byte[] qname, byte[] data) throws NativeException;
    private native void writeKeyNative(byte[] lib, byte[] qname, byte[] key, byte[] data) throws NativeException;
}
