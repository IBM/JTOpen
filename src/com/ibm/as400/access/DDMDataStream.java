///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DDMDataStream.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;

// Base class for DDM data streams.  Provides methods to access the DDM data stream header.
class DDMDataStream extends DataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    ////////////////////////////////////////////////////////////////////////////
    // DDM header length is 6 bytes:
    //        Description                   Bytes
    //   -------------------------------    ------
    //  Total length of data stream         0,1
    //   if the hi-order bit of byte 0 is on, the datastream is continued
    //  SNA GDS architecture ID, (x'D0')    2
    //  1-byte format identifier*           3
    //  Request correlation identifier      4,5
    //
    //  *Format identifier bits:
    //    Bit      Description/Values
    //    ---      -------------------------------
    //     0       Reserved bit, always 0.
    //     1       Indicates if structure is chained, 0 = No, 1 = Yes.
    //     2       Indicates is continue after error, 0 = No, 1 = Yes.
    //     3       Indicates if next DSS has same request correlator, 0 = No, 1 = Yes.
    //    4-7      4-bit value indicating type of data stream:
    //             '0001' = RQSDSS, '0010' = RPYDSS, '0011' = OBJDSS
    ////////////////////////////////////////////////////////////////////////////
    protected static final int HEADER_LENGTH = 6;

    // Maximum data stream length.
    protected static final int MAX_DATA_STREAM_LEN = 32767;

    // Continuation bit.
    protected static final int CONTINUATION_MASK = 0x80;

    // Determine/set chaining bit
    protected static final byte CHAINED_MASK = 0x40;

    // Determine/set continue on error bit
    protected static final byte CONTINUE_ON_ERROR_MASK = 0x20;

    // Indicates if type is OBJDSS
    protected static final int OBJECT_DATA_STREAM = 17;

    // Indicates if type is RPYDSS
    protected static final int REPLY_DATA_STREAM = 16;

    // Indicates if type is RQSDSS
    protected static final int REQUEST_DATA_STREAM = 1;

    // Determine/set same correlation number bit
    protected static final byte SAME_CORRELATOR_MASK = 0x10;

    // Determines type of the data stream
    protected static final byte TYPE_MASK = 0x03;

    // Constructs a model of this data stream object.  Read from the InputStream to obtain the data stream data for the model.
    // @param  is  InputStream from which to read to obtain the data stream contents.
    // @param  dataStreams  Hashtable containing DDMDataStream objects from which to obtain a model of this object.
    // @param  system  The system from which to get the CCSID for conversion.
    // @return  DDMDataStream object
    // @exception  IOException  We are unable to read from the input stream for some reason.
    static DDMDataStream construct(InputStream is, Hashtable dataStreams, AS400ImplRemote system, int connectionID) throws IOException
    {
        // Construct a DDM data stream to receive the data stream header.  By using the default constructor for DDMDataStream, we get a data stream of size HEADER_LENGTH.
	DDMDataStream baseDataStream = new DDMDataStream();

        // Receive the header.
	if (readFromStream(is, baseDataStream.data_, 0, HEADER_LENGTH, connectionID) < HEADER_LENGTH)
	{
	    Trace.log(Trace.ERROR, "Failed to read all of the DDM data stream header.");
	    throw new ConnectionDroppedException(ConnectionDroppedException.CONNECTION_DROPPED);
	}

        // Fetch a 'model' of this particular data stream.
	DDMDataStream modelDataStream = (DDMDataStream)dataStreams.get(baseDataStream);

	DDMDataStream newDataStream = null;
	if (modelDataStream == null)
	{
            // No model was found in the hash table, so we will return a generic data stream.
	    newDataStream = new DDMDataStream();
	}
	else
	{
            // Get a new instance of the data stream
	    newDataStream = (DDMDataStream) modelDataStream.getNewDataStream();
	    if (newDataStream == null)
	    {
		newDataStream = new DDMDataStream();
	    }
	}

	newDataStream.setSystem(system);

	int packetLength = baseDataStream.getLength();
	if (packetLength - HEADER_LENGTH > 0)
	{
            // There is more data to read from the input stream for this data stream
	    if (baseDataStream.isContinued())
	    {
	        // If the data stream has the continuation bit on (hi order bit of the first byte of data_), the data being sent to us on the input stream has been broken up into packets.  This is not not the same as chaining.  When a DDM data stream is continued, the next packet to be received does not have a full header.  Only two bytes of header information (the length of the packet) are sent with the data.
                // So we need to:
	        // Read the data specified for the first packet - this will be 32767 bytes.
	        // Read the first two bytes of data from the next packet into a separate byte array.
	        // These two bytes are the length of the packet.  We then read the rest of the packet into our byte array output stream.
		byte[] maxPacket = new byte[32765];  // 32K - 2 for the two bytes of length
		byte[] nextLength = new byte[2];     // Two byte array to hold size of continued packets

	        // Initialize the header section of the new data stream.
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		b.write(baseDataStream.data_, 0, HEADER_LENGTH);
	        // Read the first packet from the input stream
		byte[] packet = new byte[packetLength - HEADER_LENGTH];
		if (readFromStream(is, packet, 0, packetLength - HEADER_LENGTH, connectionID) < packetLength - HEADER_LENGTH)
		{
		    Trace.log(Trace.ERROR, "Failed to read all of the DDM data stream packet.");
		    throw new ConnectionDroppedException(ConnectionDroppedException.CONNECTION_DROPPED);
		}
		b.write(packet);

	        // Get subsequent packets
		boolean done = false;
		while (!done)
		{
                    // Get the length of the next packet
		    if (readFromStream(is, nextLength, 0, 2, connectionID) < 2)
		    {
			Trace.log(Trace.ERROR, "Failed to read all of the DDM data stream packet length.");
			throw new ConnectionDroppedException(ConnectionDroppedException.CONNECTION_DROPPED);
		    }
		    packetLength = BinaryConverter.byteArrayToUnsignedShort(nextLength, 0);
		    if (packetLength == 0xFFFF)
		    {
                        // Hi-order bit is on, indicating that there are more packets and that this packet is 32K in length
			packet = maxPacket;    // Use the maxPacket array to store the data
			packetLength = 0x7FFF; // Turn off the hi-order bit to get the true length
		    }
		    else
		    {
                        // This is the last packet; create an array of the appropriate size
			packet = new byte[packetLength - 2];
			done = true;
		    }

	            // Read the rest of the packet from the input stream
		    if (readFromStream(is, packet, 0, packetLength - 2, connectionID) < packetLength - 2)
		    {
			Trace.log(Trace.ERROR, "Failed to read all of the DDM data stream continuation packet.");
			throw new ConnectionDroppedException(ConnectionDroppedException.CONNECTION_DROPPED);
		    }
		    b.write(packet);
		}

	        // Copy the data read into the data stream object
		newDataStream.data_ = b.toByteArray();
	    }
	    else
	    {
                // Data stream is not continued (typical case)
		newDataStream.data_ = new byte[packetLength];
		System.arraycopy(baseDataStream.data_, 0, newDataStream.data_, 0, HEADER_LENGTH);

		if (newDataStream.data_.length - HEADER_LENGTH > 0)
		{
	            // Receive any remaining bytes.
		    newDataStream.readAfterHeader(is);
		}
	    }
	}

	return newDataStream;
    }

    // Constructs an empty DDMDataStream object.
    DDMDataStream()
    {
	super(HEADER_LENGTH);
    }

    // Constructs a DDMDataStream object.
    // @param  data  the data with which to initialize this data stream.
    DDMDataStream(byte [] data)
    {
	super(HEADER_LENGTH, data);
        // The total length of the data stream is the length of the byte array, data
	setLength(data_.length);
    }

    // Constructs a DDMDataStream object with the specified total length.
    // @param  totalLength  the total length that the data stream will be.
    DDMDataStream(int totalLength)
    {
	super(HEADER_LENGTH);
	data_ = new byte[totalLength];
        // The caller has supplied the total length of the data stream
	setLength(data_.length);
    }

    // Indicates whether to continue to the next chained data stream if an error occurs.
    // @return  true if we are to continue on error, false otherwise.
    boolean continueOnError()
    {
        // Use bit-wise & with the continue on error mask to get byte data that contains a 1 or 0 for the continue on error bit and 0's in all other bit positions.
	return (data_[3] & CONTINUE_ON_ERROR_MASK) != 0;
    }

    // Retrieve the code point indicating the DDM term that this data stream represents.
    // Note:  This is the DDM term at the level below RQSDSS, RPYDSS, and OBJDSS.  If this data stream has not yet had any data besides the header associated with it, 0 is returned.
    // @return  The code point indicating the DDM term that this data stream represents.
    int getCodePoint()
    {
	if (data_.length > HEADER_LENGTH)
	{
	    return get16bit(HEADER_LENGTH + 2);
	}
	return 0;
    }

    // Retrieve the request correlation for this data stream.
    // @return  The request correlation number.
    int getCorrelation()
    {
        // The request correlation number starts at byte 4
	return get16bit(4);
    }

    // Retrieve the GDS architecture id.  Currently, this should always be x'D0'.
    // @return  byte representing the GDS id.
    byte getGDSId()
    {
	return data_[2];
    }

    // Retrieve the total length of the data stream.
    // @return  The total length of this data stream
    int getLength()
    {
	int len;
        // Length is the first two bytes of data_.
	if (isContinued())
	{
	    byte[] length = new byte[2];
	    System.arraycopy(data_, 0, length, 0, 2);
            // Do a bit-wise "and" with 0x7F to turn of the hi-order bit and leave the remaining bits unchanged
	    length[0] &= 0x7F;
	    len = BinaryConverter.byteArrayToUnsignedShort(length, 0);
	}
	else
	{
	    len = get16bit(0);
	}

	return len;
    }

    // Retrieve integer value indicating the type of this data stream.
    // @return  Integer representation of the type of this data stream.  Valid values are: 1 = RQSDSS, 2 = RPYDSS or 3 = OBJDSS.
    int getType()
    {
        // Use bit-wise & with the type mask to get byte data that contains binary 0001, 0010 or 0011 indicating the type of the data stream.
	return data_[3] & TYPE_MASK;
    }

    // Retrieve the hash code for this datastream.
    // Note:  Reply data stream sub-classes should override this method to return the reply id directly instead of calling getType().
    // @returns  The hash code for this object.
    public int hashCode()
    {
        // We use the data stream type for the hash code.
	return getType();
    }

    // Indicates if the next data stream will have the same request correlation.
    // @return  true if the next data stream will have the same request correlation, false otherwise.
    boolean hasSameRequestCorrelation()
    {
       // Use bit-wise & with the same correlator mask to get byte data that contains a 1 or 0 for the same correlator bit and 0's in all other bit positions.
	return ((data_[3] & SAME_CORRELATOR_MASK) != 0);
    }

    // Indicates whether this data stream is chained.
    // @return  true if this data stream is chained, false otherwise.
    boolean isChained()
    {
       // Use bit-wise & with the chained mask to get byte data that contains a 1 or 0 for the chained bit and 0's in all other bit positions.
	return ((data_[3] & CHAINED_MASK) != 0);
    }

    // Indicates whether this data stream is continued.  If a data stream is continued, the next reply will have a truncated header that consists of two bytes indicating the length of the reply.  This differs from chaining where a complete header is sent with the object.
    boolean isContinued()
    {
	return (data_[0] & CONTINUATION_MASK) != 0;
    }

    // Set whether to continue to the next chained data stream if an error occurs.
    // @param  cont  true if we are to continue on error, false otherwise.
    void setContinueOnError(boolean cont)
    {
	if (cont)
	{
            // Do a bit-wise "or" with the continue on error mask to set the continue on error bit to 1 if it is 0 and to leave all other bits unchanged.
	    data_[3] |= CONTINUE_ON_ERROR_MASK;
	}
	else
	{
            // Do a bit-wise and with logical "not" of the continue on error mask to set the continue on error bit to 0 if it is 1 and to leave all remaining bits unchanged.
	    data_[3] &= (~CONTINUE_ON_ERROR_MASK);
	}
    }

    // Set the request/reply correlation for this data stream.
    // @param  correlation  the request correlation number.
    void setCorrelation(int correlation)
    {
	set16bit(correlation, 4);
    }

    // Set the GDS architecture id.  Currently, this should always be x'D0'.
    // @param  id  the GDS id.
    void setGDSId(byte id)
    {
	data_[2] = id;
    }

    // Set whether this data stream is chained.
    // @param  chained  true if this data stream is chained, false otherwise.
    void setIsChained(boolean chained)
    {
	if (chained)
	{
            // Do a bit-wise "or" with the chained mask to set the chained bit to 1 if it is 0 and to leave all other bits unchanged.
	    data_[3] |= CHAINED_MASK;
	}
	else
	{
            // Do a bit-wise and with logical "not" of the chained mask to set the chained bit to 0 if it is 1 and to leave all remaining bits unchanged.
	    data_[3] &= ~CHAINED_MASK;
	}
    }

    // Set the total length of the data stream.
    // @param  length  the total length of this data stream.
    void setLength(int length)
    {
        // Set the first two bytes of data_.
	set16bit(length, 0);
    }

    // Set whether the next data stream will have the same request correlation.
    // @param  same  true if the next data stream will have the same request correlation, false otherwise.
    void setHasSameRequestCorrelation(boolean same)
    {
	if (same)
	{
            // Do a bit-wise "or" with the same correlator mask to set the same correlator to 1 if it is 0 and to leave all other bits unchanged.
	    data_[3] |= SAME_CORRELATOR_MASK;
	}
	else
	{
            // Do a bit-wise and with logical "not" of the same correlator mask to set the same correlator bit to 0 if it is 1 and to leave all remaining bits unchanged.
	    data_[3] &= (~SAME_CORRELATOR_MASK);
	}
    }

    // Set the type of this data stream.
    // @param  type  the type of this data stream.  Valid values are: 1 = RQSDSS, 2 = RPYDSS or 3 = OBJDSS.
    void setType(int type)
    {
	if (type == 1)
	{
            // Do a bit-wise "or" with byte 0x01 to set the type to a request DSS and leave all other bits unchanged.
	    data_[3] |= 0x01;
	}
	else if (type == 2)
	{
            // Do a bit-wise "or" with byte 0x02 to set the type to a reply DSS and leave all other bits unchanged.
	    data_[3] |= 0x02;
	}
	else
	{
            // Do a bit-wise "or" with byte 0x03 to set the type to an object DSS and leave all other bits unchanged.
	    data_[3] |= 0x03;
	}
    }

    // Write the data in this data stream out to the specified OutputStream.
    // @param  out  OutputStream to which to write the data.
    // @exception  IOException  Unable to write to the output stream.
    void write(OutputStream out) throws IOException
    {
        // Write the data stream to the output stream.  If the data stream is longer than the maximum, set the length to the maximum and signal continuation.  The high bit of the length signals continuation (on = continued, off = not continued).
	int bytesToWrite = data_.length;
	if (bytesToWrite > MAX_DATA_STREAM_LEN)
	{
	    bytesToWrite = MAX_DATA_STREAM_LEN;
	    data_[0] = (byte)0xFF; data_[1] = (byte)0xFF;
	}

        // Write the data stream.
	synchronized(out)
	{
	    out.write(data_, 0, bytesToWrite);
	    out.flush();
	}
	if (Trace.isTraceOn()) Trace.log(Trace.DATASTREAM, "DDMDataStream.write() (connID="+connectionID_+"):", data_, 0, bytesToWrite);

        // Is there data stream remaining to be written?  If so, the rest of the data stream is written as packets.  Packets are similar to regular data streams but lack a full header.  The first two bytes of a packet indicate the length of the data in the packet (doesn't include the two byte packet length indicator).  The high bit of the length controls continuation (on = continued, off = not continued).  The rest of the packet is data.
	if (data_.length > bytesToWrite)
	{
            // Write the rest of the data stream as packets.
	    DataOutputStream dos = new DataOutputStream(out);
	    for (int i = bytesToWrite, packetLength = 0; i < data_.length; i += packetLength - 2)
	    {
	        // Calculate the length of the next packet.
		packetLength = data_.length - i + 2;

	        // Are there more bytes to write than the maximum allowed?
		if (packetLength > MAX_DATA_STREAM_LEN)
		{
	            // Set the bytes to write to the maximum allowed.
		    packetLength = MAX_DATA_STREAM_LEN;

	            // Write the two byte packet length with the high bit on.
		    synchronized(out)
		    {
			dos.writeShort((short)0xFFFF);
			dos.flush();
		    }
		    if (Trace.isTraceOn()) Trace.log(Trace.DATASTREAM, "DDMDataStream.write() continuation (connID="+connectionID_+"):", new byte[] {(byte)0xFF, (byte)0xFF});
		}
		else
		{
	            // Write the two byte packet length.
		    synchronized(out)
		    {
			dos.writeShort(packetLength);
			dos.flush();
		    }
		    if (Trace.isTraceOn()) Trace.log(Trace.DATASTREAM, "DDMDataStream.write() packetLength (connID="+connectionID_+"):", new byte[] {(byte)(packetLength >> 8), (byte)packetLength});
		}

	        // Write the data.
		synchronized(out)
		{
		    dos.write(data_, i, packetLength - 2);
		    dos.flush();
		}
		if (Trace.isTraceOn())
		{
		    Trace.log(Trace.DATASTREAM, "DDMDataStream.write() (connID="+connectionID_+"):", data_, i, packetLength - 2);
		}
	    }
	}
	synchronized(out)
	{
	    out.flush();
	}
    }
}
