///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: DataStream.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

// Base class for data streams.  Provides methods to access common data stream parts.
abstract class DataStream
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    protected static ConverterImplRemote converter_;  // Character set conversion object.
    protected int connectionID_;
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
    // @param  connectionID  The connection ID.
    // @exception  IOException  from Inputstream.read() operation.
    // @returns  Number of bytes read.
    static final int readFromStream(InputStream in, byte[] buf, int offset, int length, int connectionID) throws IOException //@P0C
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

        if (Trace.traceOn_) Trace.log(Trace.DATASTREAM, "Data stream data received (connID="+connectionID+") ...", buf, offset, bytesRead); //@P0C

        return bytesRead;
    }

    
    // readFromStreamDebug is used to debugging problems with readFromStream
    // left here so that it can be easily reused  @A7A
    static boolean traceOpened = false; 
    
    static final int readFromStreamDebug(InputStream in, byte[] buf, int offset, int length, int connectionID) throws IOException //@P0C
    {
    	long lastReadTime = System.currentTimeMillis(); 
        boolean endOfFile = false;
        int bytesRead = 0;
        while ((bytesRead < length) && !endOfFile)
        {
        	int availableCount = in.available();  
			if ( availableCount > 0) {
				int temp = in.read(buf, offset + bytesRead, length - bytesRead);
				if (temp == -1) {
					endOfFile = true;
				} else {
					bytesRead += temp;
				}
				lastReadTime = System.currentTimeMillis();
			} else {
                if (System.currentTimeMillis() > lastReadTime+120000) {
                	if (! traceOpened) { 
                		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");
                		
                		Trace.setFileName("/tmp/toolboxTrace."+sdf.format(new Date())+".txt");
                	    traceOpened=true; 
                	}
                	boolean traceTurnedOn = false; 
                	if (!Trace.traceOn_) {
                		traceTurnedOn = true;
                		Trace.setTraceAllOn(true); 
                		Trace.setTraceOn(true); 
                	}
                	Trace.log(Trace.DATASTREAM, "Waited more than 120 seconds to read "+length+" bytes.  Current buffer with header is "); 
                	Trace.log(Trace.DATASTREAM, "Data stream data received (connID="+connectionID+") ...", buf, 0, offset + bytesRead); //@P0C
                	if (traceTurnedOn) { 
                		Trace.setTraceAllOn(false); 
                		Trace.setTraceOn(false); 
                	}
                }
                try {
                	Thread.sleep(50); 
                } catch (Exception e) { 
                	
                }
            }
        }

        if (Trace.traceOn_) Trace.log(Trace.DATASTREAM, "Data stream data received (connID="+connectionID+") ...", buf, offset, bytesRead); //@P0C

        return bytesRead;
    }

    
    // Read the number of bytes asked for and loop until either that many bytes have been read or until we hit the end of file.
    // @param  in  Inputstream to read from.
    // @param  buf  Where to read the data into.
    // @param  offset  Offset within buf array to start reading into.
    // @param  length  How many bytes to read.
    // @exception  IOException  from Inputstream.read() operation.
    // @returns  Number of bytes read.
    final int readFromStream(InputStream in, byte[] buf, int offset, int length) throws IOException
    {
      return readFromStream(in, buf, offset, length, connectionID_);
    }

    final int readFromStreamDebug(InputStream in, byte[] buf, int offset, int length) throws IOException
    {
      return readFromStreamDebug(in, buf, offset, length, connectionID_);
    }

    
    protected AS400ImplRemote system_;

    protected byte[] data_;  // Contains complete data stream data.

    // Length of the header portion of the data stream.  This should be set by sub-classes of this class during construction via calls to super.
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
    protected final int get16bit(final int offset) //@P0C
    {
        //@P0D return BinaryConverter.byteArrayToUnsignedShort(data_, offset);
        return ((data_[offset] & 0xFF) << 8) + (data_[offset+1] & 0xFF); //@P0A
    }

    // Retrieve data from the data stream as an int from the specified offset.
    // @param  offset  Offset in the data stream from which to retrieve.
    protected final int get32bit(final int offset) //@P0C
    {
        //@P0D return BinaryConverter.byteArrayToInt(data_, offset);
        return ((data_[offset] & 0xFF) << 24) + ((data_[offset+1] & 0xFF) << 16) + ((data_[offset+2] & 0xFF) << 8) + (data_[offset+3] & 0xFF); //@P0A
    }

    // Retrieve data from the data stream as a long from the specified offset.
    // @param  offset  Offset in the data stream from which to retrieve.
    protected final long get64bit(final int offset) //@P0C
    {
        return BinaryConverter.byteArrayToLong(data_, offset);
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
        // int bytesRead = readFromStreamDebug(in, data_, headerLength_, data_.length - headerLength_);
    
        int bytesRead = readFromStream(in, data_, headerLength_, data_.length - headerLength_);
        if (bytesRead < data_.length - headerLength_)
        {
            if (Trace.traceOn_) Trace.log(Trace.ERROR, "Failed to read all of the data stream."); //@P0C
            throw new ConnectionDroppedException(ConnectionDroppedException.CONNECTION_DROPPED);
        }

        return bytesRead;
    }

    // Set the connection ID associated with this data stream.
    // @param  connectionID  the connection ID.
    void setConnectionID(int connectionID)
    {
        connectionID_ = connectionID;
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
    protected final void set16bit(int i, int offset) //@P0C
    {
        //@P0D BinaryConverter.unsignedShortToByteArray(i, data_, offset);
      data_[offset]   = (byte)(i >>> 8); //@P0A
      data_[offset+1] = (byte) i;        //@P0A
    }

    // Set the 4 bytes at the specified offset to the specified value.
    // @param  i  Value to be set.
    // @param  offset  Offset in the data stream at which to place the value.
    protected final void set32bit(int i, int offset) //@P0C
    {
        //@P0D BinaryConverter.intToByteArray(i, data_, offset);
      data_[offset]   = (byte)(i >>> 24); //@P0A
      data_[offset+1] = (byte)(i >>> 16); //@P0A
      data_[offset+2] = (byte)(i >>>  8); //@P0A
      data_[offset+3] = (byte) i;         //@P0A
    }

    // Set the 8 bytes at the specified offset to the specified value.
    // @param  longVal  Value to be set.
    // @param  offset  Offset in the data stream at which to place the value.
    protected final void set64bit(long longVal, int offset) //@LFS
    {
      BinaryConverter.longToByteArray(longVal, data_, offset);
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

        if (Trace.traceOn_) Trace.log(Trace.DATASTREAM, "Data stream sent (connID="+connectionID_+") ...", data_); //@P0C
    }
}
