///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: DBBaseRequestDS.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.CharConversionException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Enumeration; // @B1A
import java.util.Hashtable; // @B0A


 /**
   This is the base class for all database server request data
   streams.  Every concrete database server request data stream
   should inherit from this class.

   Here are the steps needed to send a request data stream to the
   server:

      1. For all such requests, construct the data stream object,
         passing the required parameters.

      2. The header and template portions of the data stream will
          be created by the constructor.

      3. Use the addParameter function to add any additional
          parameters required by the specific function.     These
           parameters will be part of the Optional / Variable-length
           section of the header.

       4. After setting all the parameters, but before sending the
          request, invoke the dataStreamReady function.
           This function will:
             - Clean up the data stream
             - Set values that can not be calculated until all
               all parameters are set
                 - Request length
                  - parameter count

       5. Then you are ready to send the request to the server.  There are
          two choices:
           - If no reply is needed use:
               try {
               server.send (request);
             }
             catch {
               // Handle the exception.
             }

           - If a reply is needed use:
               DataStream reply = null;
             try {
               reply = server.sendAndReceive (request);
             }
             catch (Exception e) {
               // Handle the exception.
             }


   @See com.ibm.as400.access.DBReturnRequestedDataDS
   @See com.ibm.as400.access.AS400Server
**/
abstract class DBBaseRequestDS
extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private int                    currentOffset_;
    private int                    lockedLength_;
    private int                    operationResultBitmap_;
    private int                    parameterCount_;
    private DBStorage              storage_;
    private static DBStoragePool   storagePool_ = null;                 // @B0C @B1C @B3C



    // Values for operation result bitmap.
    public static final int       ORS_BITMAP_DATA_FORMAT               = 0x08000000;
    public static final int       ORS_BITMAP_FIRST_LEVEL_TEXT          = 0x20000000;
    public static final int       ORS_BITMAP_MESSAGE_ID                = 0x40000000;
    public static final int       ORS_BITMAP_RESULT_DATA               = 0x04000000;
    public static final int       ORS_BITMAP_RETURN_DATA               = 0x80000000;
    public static final int       ORS_BITMAP_PACKAGE_INFO              = 0x00100000;
    public static final int       ORS_BITMAP_PARAMETER_MARKER_FORMAT   = 0x00800000;
    public static final int       ORS_BITMAP_SECOND_LEVEL_TEXT         = 0x10000000;
    public static final int       ORS_BITMAP_SERVER_ATTRIBUTES         = 0x01000000;
    public static final int       ORS_BITMAP_SQLCA                     = 0x02000000;



    // Server IDs.
    protected static final int    SERVER_SQL     = 0xE004;
    protected static final int    SERVER_NDB     = 0xE005;
    protected static final int    SERVER_ROI     = 0xE006;



/**
Static initializer.
**/
    static
    {
        storagePool_ = new DBStoragePool ();                    // @B0D @B3C
    }



/**
Constructor.
**/
    protected DBBaseRequestDS (int requestId,
                               int rpbId,
                               int operationResultBitmap,
                               int parameterMarkerDescriptorHandle)
    {
        super ();

        // Allocate the large byte array for storage of the
        // data stream.
        storage_ = storagePool_.getUnusedStorage ();
        data_ = storage_.getReference ();

        // Data stream header.
        setHeaderID (0);
        setCSInstance(0);
        setTemplateLen (20);
        setReqRepID (requestId);

        // Data stream template.
        set32bit (operationResultBitmap, 20);   // Operation result bitmap.
        set16bit (rpbId, 28);                   // Return ORS handle.
        set16bit (rpbId, 30);                   // Fill ORS handle.
        setBasedOnORSHandle (0);                // Based on ORS handle.
        set16bit (rpbId, 34);                   // RPB handle.
        setParameterMarkerDescriptorHandle (parameterMarkerDescriptorHandle);

        // Initialization.
        currentOffset_          = HEADER_LENGTH + 20;
        parameterCount_         = 0;
        operationResultBitmap_  = operationResultBitmap;
    }



/**
Adds another operation result to the operation result bitmap.
**/
    public void addOperationResultBitmap (int value)
    {
        operationResultBitmap_ |= value;
        set32bit (operationResultBitmap_, 20);
    }



/**
Adds a 1 byte parameter.
**/
    protected void addParameter (int codePoint, byte value)
        throws DBDataStreamException
    {
        lock (1, codePoint);

        data_[currentOffset_] = value;

        unlock ();
    }



/**
Adds a 2 byte parameter.
**/
    protected void addParameter (int codePoint, short value)
        throws DBDataStreamException
    {
        lock (2, codePoint);

        set16bit (value, currentOffset_);

        unlock ();
    }



/**
Adds a 2 byte parameter with an extra 4 byte value.
**/
    protected void addParameter (int codePoint, short value, int extra)
        throws DBDataStreamException
    {
        lock (6, codePoint);

        set16bit (value, currentOffset_);
        set32bit (extra, currentOffset_ + 2);

        unlock ();
    }



/**
Adds a 4 byte parameter.
**/
    protected void addParameter (int codePoint, int value)
        throws DBDataStreamException
    {
        lock (4, codePoint);

        set32bit (value, currentOffset_);

        unlock ();
    }



/**
Adds a byte array parameter.
**/
    protected void addParameter (int codePoint, byte[] value)
        throws DBDataStreamException
    {
        lock (value.length, codePoint);

        System.arraycopy (value, 0, data_, currentOffset_, value.length);

        unlock ();
    }



/**
@B2A Adds a byte array parameter including CCSID and length.
**/
    protected void addParameter (int codePoint,
                                 byte[] value,
                                 boolean overloadThisMethod)
        throws DBDataStreamException
    {
        lock (value.length + 6, codePoint);

        set16bit ((short) 0xFFFF, currentOffset_);              // CCSID
        set32bit (value.length, currentOffset_ + 2);            // length        
        System.arraycopy (value, 0, data_, currentOffset_ + 6, value.length);

        unlock ();
    }



/**
Adds a fixed length string parameter.
**/
//
// This does not need to work with double byte character sets
// as far as I know.
//
    protected void addParameter (int codePoint,
                                 ConverterImplRemote converter,
                                 String value,
                                 int valueLength)
        throws DBDataStreamException
    {
        lock (valueLength + 2, codePoint);

        set16bit (converter.getCcsid(), currentOffset_);    // CCSID

        try {
            converter.stringToByteArray (value.substring (0, valueLength),
                data_, currentOffset_ + 2);
        }
        catch (CharConversionException e) {
            throw new DBDataStreamException ();
        }

        unlock ();
    }



/**
Adds a variable length string parameter.
**/
    protected void addParameter (int codePoint,
                                 ConverterImplRemote converter,
                                 String value)
        throws DBDataStreamException
    {
        // @A1C
        // Changed code to use the converter to find out the exact
        // number of bytes the string needs to occupy so that it works
        // for both single-byte and double-byte strings.
        byte[] rawBytes = converter.stringToByteArray (value);
        lock (rawBytes.length + 4, codePoint);

        set16bit (converter.getCcsid(), currentOffset_);        // CCSID
        set16bit (rawBytes.length, currentOffset_ + 2);         // SL

        try {
            System.arraycopy (rawBytes, 0, data_, currentOffset_ + 4,
                rawBytes.length);
        }
        catch (Exception e) {
            throw new DBDataStreamException ();
        }

        unlock ();
    }



/**
Adds a library list parameter.
**/
    protected void addParameter (int codePoint,
                                 ConverterImplRemote converter,
                                 char[] indicators,
                                 String[] libraries)
        throws DBDataStreamException
    {
        int  parameterLength = 4;
        for (int i = 0; i < libraries.length; ++i)
            parameterLength += 3 + libraries[i].length();

        lock (parameterLength, codePoint);

        set16bit (converter.getCcsid(), currentOffset_);    // CCSID
        set16bit (libraries.length, currentOffset_ + 2);    // number of libraries

        int offset = 4;
        try {
            for (int i = 0; i < libraries.length; ++i) {
                Character ch = new Character (indicators[i]);
                converter.stringToByteArray (ch.toString(), data_, currentOffset_ + offset);
                set16bit (libraries[i].length(), currentOffset_ + offset + 1);
                converter.stringToByteArray (libraries[i], data_, currentOffset_ + offset + 3);
                offset += 3 + libraries[i].length();
            }
        }
        catch (CharConversionException e) {
            throw new DBDataStreamException ();
        }

        unlock ();
    }



/**
Adds a NLSS indicator parameter.
**/
    protected void addParameter (int codePoint,
                                 ConverterImplRemote converter,
                                 int type,
                                 String tableFile,
                                 String tableLibrary,
                                 String languageId)
        throws DBDataStreamException
    {
        int parameterLength;
        switch (type) {
           case 0:
           default:
               parameterLength = 2;
               break;
           case 1:
           case 2:
               parameterLength = 7;
               break;
           case 3:
               parameterLength = 8 + tableFile.length () + tableLibrary.length ();
               break;
        }

        lock (parameterLength, codePoint);

        set16bit (type, currentOffset_);            // sort value

        try {
            switch (type) {
                case 0:
                default:
                    break;
                case 1:
                case 2:
                    set16bit (converter.getCcsid(), currentOffset_ + 2);    // CCSID
                    converter.stringToByteArray (languageId, data_,
                        currentOffset_ + 4);     // sort language id
                    break;
                case 3:
                    set16bit (converter.getCcsid(), currentOffset_ + 2);    // CCSID
                    set16bit (tableFile.length(),
                        currentOffset_ + 4);            // SL
                    converter.stringToByteArray (tableFile, data_,
                        currentOffset_ + 6);  // sort table file
                    set16bit (tableLibrary.length(),
                        currentOffset_ + 6 + tableFile.length()); // SL
                    converter.stringToByteArray (tableLibrary,  data_,
                        currentOffset_ + 8 + tableFile.length()); // sort table library
                    break;
            }
        }
        catch (CharConversionException e) {
            throw new DBDataStreamException ();
        }

        unlock ();
    }



/**
Adds a DBOverlay parameter.
**/
    protected void addParameter (int codePoint,
                                 DBOverlay value)
        throws DBDataStreamException
    {
        lock (value.getLength (), codePoint);

        value.overlay (data_, currentOffset_);

        unlock ();
    }



/**
Output the byte stream contents to the specified PrintStream.
The output format is two hex digits per byte, one space every
four bytes, and sixteen bytes per line.

@param ps the output stream
**/
//
// We need to override the implementation from the super class, since it
// depends on the length of the byte array.  We reuse long byte arrays,
// and store the length in a separate variable.
//
// If we did not override this, the dump of a single request would show
// a bunch of extra zero bytes.
//
// In addition, we take the opportunity to also print
// limited character output.
//
    void dump (PrintStream ps)
    {
        dump (ps, data_, currentOffset_);
    }



/**
Output the byte stream contents to the specified PrintStream.
The output format is two hex digits per byte, one space every
four bytes, and sixteen bytes per line.

@param ps the output stream
@param data the data
@param length the length
**/
    static void dump (PrintStream ps, byte[] data, int length)
    {
        StringBuffer hexBuffer  = new StringBuffer ();
        StringBuffer charBuffer = new StringBuffer ();
        int i;
        for (i = 0; i < length; i++) {

            // Convert the data to 2 digits of hex.
            String temp = "00" + Integer.toHexString (data[i]);
            String hex = temp.substring (temp.length () - 2);
            hexBuffer.append (hex.toUpperCase ());

            // Pad hex output at every 4 bytes.
            if (i % 4 == 3)
                hexBuffer.append (" ");

            // Convert the data to an ASCII character.
            short ascii = (short) ((data[i] >= 0)
                ? data[i] : 256 + data[i]);
            char ch;
            if ((ascii >= 0x81) && (ascii <= 0x89))
                ch = (char) ('a' + ascii - 0x81);

            else if ((ascii >= 0x91) && (ascii <= 0x99))
                ch = (char) ('j' + ascii - 0x91);

            else if ((ascii >= 0xA2) && (ascii <= 0xA9))
                ch = (char) ('s' + ascii - 0xA2);

            else if ((ascii >= 0xC1) && (ascii <= 0xC9))
                ch = (char) ('A' + ascii - 0xC1);

            else if ((ascii >= 0xD1) && (ascii <= 0xD9))
                ch = (char) ('J' + ascii - 0xD1);

            else if ((ascii >= 0xE2) && (ascii <= 0xE9))
                ch = (char) ('S' + ascii - 0xE2);

            else if ((ascii >= 0xF0) && (ascii <= 0xF9))
                ch = (char) ('0' + ascii - 0xF0);

            else
                ch = '.';

            charBuffer.append (ch);

            // Start a new line at every 16 bytes.
            if (i % 16 == 15) {
                ps.println (hexBuffer + "  [" + charBuffer + "]");
                hexBuffer  = new StringBuffer ();
                charBuffer = new StringBuffer ();
            }
        }

        // Pad out and print the last line if necessary.
        if (i % 16 != 0) {
            int hexBufferLength = hexBuffer.length ();
            for (int j = hexBufferLength; j <= 35; ++j)
                hexBuffer.append (" ");
            ps.println (hexBuffer + "  [" + charBuffer + "]");
        }
    }


/**
Copyright.
**/
    static private String getCopyright ()
    {
        return Copyright.copyright;
    }
    


/**
"Locks" the request datastream for addition of a parameter.
This will determine if there is space left in the data
byte array and grow it as needed.

@param length The length to be added to the data stream,
              in bytes, not including the LL and CP.
**/
    private void lock (int length, int codePoint)
        throws DBDataStreamException
    {
        if (storage_.checkSize (currentOffset_ + length + 6))
            data_ = storage_.getReference ();
        lockedLength_ = length;

        set32bit (length + 6, currentOffset_);          // LL
        set16bit (codePoint, currentOffset_ + 4);       // CP

        currentOffset_ += 6;
    }



/**
Sets the numeric value of the "based-on" Operation Results
Set (ORS) into the request datastream.

@param value The numeric value of the based-on ORS.
**/
//------------------------------------------------------------
// The based-on ORS handle specifies an ORS from a previous
// operation.  It is used to be able to "chain" requests
// together without checking the results or the previous
// request.  If the previous request (whose results are stored
// in the based-on ORS) failed, then this request will not be
// executed.
//
// The based-on ORS handle should be 0 for the first request
// in a chain.
//-----------------------------------------------------------
    public void setBasedOnORSHandle (int value)
    {
        set16bit (value, 32);
    }



/**
"Unlocks" the request datastream after addition of a parameter.
**/
    private void unlock ()
    {
        currentOffset_ += lockedLength_;
        ++parameterCount_;
    }



/**
Sets the parameter marker descriptor handle.
**/
    public void setParameterMarkerDescriptorHandle (int value)
    {
        set16bit (value, 36);
    }





/**
Overrides the superclass to write the datastream.
**/
    void write (OutputStream out)
        throws IOException
    {
        setLength (currentOffset_);
        set16bit (parameterCount_, 38);

        // @A0A
        // Synchronization is added around the socket
        // write so that requests from multiple threads
        // that use the same socket won't be garbled.
        synchronized(out) {
            out.write (data_, 0, currentOffset_);
        }

        // Free the storage for others to use.  We
        // are done with it.
        storagePool_.freeStorage (storage_);
    }


}




