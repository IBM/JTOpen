///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PrintObjectTransformedInputStream.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.IOException;


// NOTE: This class is derived directly from PrintObjectPageInputStream.

/**
The PrintObjectTransformedInputStream class is used to read transformed data
from a server spooled file.  The type of transform to be performed on the data
is dependent on the
<a href="PrintParameterList.html">PrintParameterList</a>
used to create an instance of the class.
<p>
An instance of this class is created using
the getTransformedInputStream method
from the class <a href="SpooledFile.html">SpooledFile</a>.
<p>
NOTE: This class is supported on OS/400 V4R4 or later.
Not all spooled file formats are supported for transform.
**/

public class PrintObjectTransformedInputStream extends InputStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Private data

    transient private AS400 system_;
    transient private PrintObjectTransformedInputStreamImpl impl_;

/**
Constructs a PrintObjectTransformedInputStream object. The PrintParameterList attribute
ATTR_MFGTYPE must be specified to indicate the type of data transform.

@param  spooledFile The SpooledFile.
@param  transformOptions The PrintParameterList options to be used when opening the SpooledFile.

@exception AS400Exception If the server returns an error message.
@exception AS400SecurityException If a security or authority error occurs.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception IOException If an error occurs while communicating with the server.
@exception InterruptedException If this thread is interrupted.
@exception RequestNotSupportedException If the requested function is not supported because the server
           operating system is not at the correct level.
**/
    PrintObjectTransformedInputStream(SpooledFile spooledFile,
                                      PrintParameterList transformOptions)
        throws AS400Exception,
               AS400SecurityException,
               ErrorCompletingRequestException,
               IOException,
               InterruptedException,
               RequestNotSupportedException
    {
        system_ = spooledFile.getSystem();
        if (impl_ == null)
            chooseImpl();
        // do connect here because it could throw Exceptions  
        system_.connectService(AS400.PRINT);
        if (spooledFile.getImpl() == null) {
            spooledFile.chooseImpl();
        }
        impl_.createPrintObjectTransformedInputStream((SpooledFileImpl) spooledFile.getImpl(), transformOptions);  // @A3A
    }



/**
Returns the number of bytes available (with blocking).

@return  The number of available bytes (with blocking).
**/
    public int available() throws IOException
    {
        return impl_.available();
    }



   // A2A - Added method
    private void chooseImpl()
    {
        if (system_ == null) {
            Trace.log( Trace.ERROR, "Attempt to use PrintObjectTransformedInputStream before setting system.");
            throw new ExtendedIllegalStateException("system",
                                    ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        impl_ = (PrintObjectTransformedInputStreamImpl) system_.loadImpl2("com.ibm.as400.access.PrintObjectTransformedInputStreamImplRemote",
                                                                          "com.ibm.as400.access.PrintObjectTransformedInputStreamImplProxy");
    }



/**
Closes the input stream and releases any resources associated with it.

@exception IOException If an error occurs while communicating with the server.
**/
    public void close() throws IOException
    {
        impl_.close();
    }



/**
Returns a boolean indicating whether this stream type supports mark and reset.

@return Always false.  Objects of this class will not support the mark and reset methods.
**/
    public boolean markSupported()
    {
        return false;
    }



/**
Reads the next byte of data from this input stream.

@return The byte read, or -1 if the end of the stream is reached.

@exception  IOException If an error occurs while communicating with the server.
**/
    public int read() throws IOException
    {
        int readchar = -1;

        byte oneByte[] = new byte[1];
        int rc = read(oneByte, 0, 1);
        if (rc == 1) {
            readchar = (int)oneByte[0];
        }
        return readchar;
    }



/**
Reads up to <i>data.length</i> bytes of data from the input
stream into <i>data</i>.

@param data The buffer into which the data is read.

@return The total number of bytes read into the buffer or -1 if there is no more
data because the end of file has been reached.

@exception IOException If an error occurs while communicating with the server.
**/
    public int read(byte[] data) throws IOException
    {
        return read(data, 0, data.length);
    }



/**
Reads up to <i>length</i> bytes of data from this input stream into <i>data</i>,
starting at the array offset <i>dataOffset</i>.

@param data The buffer into which the data is read.
@param dataOffset The start offset of the data.
@param length The maximum number of bytes to read.

@return The total number of bytes read into the buffer, or -1 if there is
no more data because the end of file has been reached.

@exception IOException If an error occurs while communicating with the server.
**/
    public int read(byte data[], int dataOffset, int length) throws IOException
    {
        return impl_.read(data, dataOffset, length);
    }



/**
Skips over the next <i>bytesToSkip</i> bytes in the stream.
This method may skip less bytes than specified if the end of
the data block is reached. The actual number of bytes skipped is returned.
No action is taken if the number of bytes to skip is not positive.

@param bytesToSkip The number of bytes to be skipped.

@return The actual number of bytes skipped.

@exception IOException If an error occurs while communicating with the server.
**/
    public long skip(long bytesToSkip) throws IOException
    {
        if (bytesToSkip <= 0) {
            return 0;
        }
        return impl_.skip(bytesToSkip);
    }

}
