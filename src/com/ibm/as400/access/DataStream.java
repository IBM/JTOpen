///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: DataStream.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

// Base class for data streams.  Provides methods to access common data stream parts.
abstract class DataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    protected static ConverterImplRemote converter_;  // Character set conversion object.
    static ConverterImplRemote getDefaultConverter()
    {
        return converter_;
    }
    static void setDefaultConverter(ConverterImplRemote converter)
    {
        converter_ = converter;
    }

    // Read the number of bytes asked for and loop until either that many bytes have been read or until we hit the end of file.
    // @param  in  Inputstream to read from.
    // @param  buf  Where to read the data into.
    // @param  offset  Offset within buf array to start reading into.
    // @param  length  How many bytes to read.
    // @exception  IOException  from Inputstream.read() operation.
    // @returns  Number of bytes read.
    static int  readFromStream(InputStream in, byte[] buf, int offset, int length) throws IOException
    {
        boolean endOfFile = false;
        int bytesRead = 0;
        while ((bytesRead < length) && !endOfFile)
        {
            int temp = in.read(buf, offset + bytesRead, length - bytesRead);
            if (temp == -1)
            {
                endOfFile = true;
            }
            else
            {
                bytesRead += temp;
            }
        }

        if (Trace.isTraceOn()) Trace.log(Trace.DATASTREAM, "Data stream data received...", buf, offset, bytesRead);

        return bytesRead;
    }

    protected AS400ImplRemote system_;

    protected byte[] data_;  // Contains complete data stream data.

    // Length of the header portion of the data stream.  This should be set by sub-classes of this class during construction via calls to super..
    protected int headerLength_;


    // Constructs a data stream object with data of length headerLength initialized to hex 0's.
    // @param  headerLength  Length of the header for the data stream.
    DataStream(int headerLength)
    {
        // Initialize instance data.
        headerLength_ = headerLength;
        data_ = new byte[headerLength_];

        // Set the total length field of the data stream.
        setLength(headerLength_);
    }

    // Constructs a data stream object with data.  It is expected that data is properly formatted for the data stream (header information and remaining data stream information).
    // @param  headerLength  Length of the header for the data stream.
    // @param  data  A byte array to be used for this data stream.  The size of the byte array is the number of bytes to be used for this data stream.
    DataStream(int headerLength, byte[] data)
    {
        headerLength_ = headerLength;
        data_ = data;
    }

    // Override Object.equals so that this object is "equal" to another if they are of the same base type and their hash codes are the same.
    // @param  obj  The object with which to compare this object.
    // @return  true is the objects are equal, false otherwise.
    public boolean equals(Object obj)
    {
        if (obj instanceof DataStream)
        {
            return (hashCode() == ((DataStream)obj).hashCode());
        }
        return false;
    }

    // Retrieve the request/reply correlation for this data stream.
    // @return  The request correlation number.
    abstract int getCorrelation();

    // Retrieve the length of the data stream.
    // @return  The length of the data stream.
    abstract int getLength();

    // Get a new instance of this class.  The code for this method should be provided by the subclasses.  In the event that a sub-class does not override this method, null will be returned.
    // @return  Object representing a clone of this object.
    Object getNewDataStream()
    {
        return null;
    }

    // Retrieve the system associated with this object.
    // @return  Object representing the system.
    AS400ImplRemote getSystem()
    {
        return system_;
    }

    // Retrieve data from the data stream as a 16-bit number from the specified offset.
    // @param  offset  Offset in the data stream from which to retrieve.
    protected int get16bit(int offset)
    {
        return BinaryConverter.byteArrayToUnsignedShort(data_, offset);
    }

    // Retrieve data from the data stream as an int from the specified offset.
    // @param  offset  Offset in the data stream from which to retrieve.
    protected int get32bit(int offset)
    {
        return BinaryConverter.byteArrayToInt(data_, offset);
    }

    // Retrieve the hash code for this data stream.
    // @return  Hash code for this data stream.
    public int hashCode()
    {
        // This method should be overriden in the sub-class.
        return super.hashCode();
    }

    // Read from input stream into this data stream placing the data read into this data stream after the header portion of the data stream.
    // @param  in  InputStream from which to read the data.
    // @exception  IOException  Unable to read from the input stream.
    int readAfterHeader(InputStream in) throws IOException
    {
        int bytesRead = readFromStream(in, data_, headerLength_, data_.length - headerLength_);
        if (bytesRead < data_.length - headerLength_)
        {
            Trace.log(Trace.ERROR, "Failed to read all of the data stream.");
            throw new ConnectionDroppedException(ConnectionDroppedException.CONNECTION_DROPPED);
        }

        return bytesRead;
    }

    // Set the request correlation for this data stream.
    // @param  correlation  The request correlation number.
    abstract void setCorrelation(int correlation);

    // Set the length of the data stream.  This is the total length of the data stream.
    // @param  len  The length of the data stream.
    abstract void setLength(int len);

    // Set the system to be associated with this data stream.
    // @param  system  the object representing the system.
    void setSystem(AS400ImplRemote system)
    {
        system_ = system;
    }

    // Set the 2 bytes at the specified offset to the specified value.
    // @param  i  Value to be set.
    // @param  offset  Offset in the data stream at which to place the value.
    protected void set16bit(int i, int offset)
    {
        BinaryConverter.unsignedShortToByteArray(i, data_, offset);
    }

    // Set the 4 bytes at the specified offset to the specified value.
    // @param  i  Value to be set.
    // @param  offset  Offset in the data stream at which to place the value.
    protected void set32bit(int i, int offset)
    {
        BinaryConverter.intToByteArray(i, data_, offset);
    }

    // Write the data in this data stream out to the specified OutputStream.
    // @param  out  OutputStream to which to write the data.
    // @exception  IOException  Unable to write to the output stream.
    void write(OutputStream out) throws IOException
    {
        // Synchronization is added around the socket write so that requests from multiple threads that use the same socket won't be garbled.
        synchronized(out)
        {
            out.write(data_);
            out.flush();
        }

        if (Trace.isTraceOn()) Trace.log(Trace.DATASTREAM, "Data stream sent...", data_);
    }
}
