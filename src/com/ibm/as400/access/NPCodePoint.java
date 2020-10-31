///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: NPCodePoint.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

class NPCodePoint extends Object implements Cloneable,
                                            java.io.Serializable 
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



    // Code Points defined in the Network Print data stream.
    static final int SPOOLED_FILE_ID = 0x0001;
    static final int WRITER_JOB_ID = 0x0002;
    static final int OUTPUT_QUEUE_ID = 0x0003;
    static final int PRINTER_FILE_ID = 0x0004;
    static final int PRINTER_DEVICE_ID = 0x0005;
    static final int SELECTION = 0x0006;
    static final int ATTRIBUTE_LIST = 0x0007;
    static final int ATTRIBUTE_VALUE = 0x0008;
    static final int DATA = 0x0009;
    static final int EXIT_PROGRAM_DATA = 0x000A;
    static final int TARGET_SPOOLED_FILE_ID = 0x000B;
    static final int SPOOLED_FILE_HANDLE = 0x000C;
    static final int MESSAGE_HANDLE = 0x000D;
    static final int LIBRARY_ID = 0x000E;
    static final int RESOURCE_ID = 0x000F;
    static final int RESOURCE_HANDLE = 0x0010;

    static final int MAX_CODEPOINT_ID = 0x0010;    // keep this up to date!

    protected final static int LEN_HEADER = 6;  // size of code point header struct (len + id)


    private byte[] data_;                  // code point data buffer.
    private int ID_;                       // code point ID.
    private int length_;                   // length of code point data
    private int offset_ = 0;               // offset into data buffer where we can start
                                          // putting our data at
    // @B1D protected int  hostCCSID_;            // target ccsid
    protected transient ConverterImpl converter_;                                            // @B1A

    protected Object clone()
    {
       NPCodePoint cp = new NPCodePoint(this);
       return cp;
    }

    NPCodePoint(NPCodePoint cp)
    {

       ID_       = cp.ID_;
       length_   = cp.length_;
       offset_   = cp.offset_;
       // @B1C hostCCSID_ = cp.hostCCSID_;
       converter_ = cp.converter_;                                             // @B1A
       if (cp.data_ != null)
       {
           data_ = new byte[cp.data_.length];
           System.arraycopy(cp.data_, offset_,
                            data_, offset_,
                            data_.length - offset_);
       }
    }

    NPCodePoint()
    {

    }

    NPCodePoint( int ID )
    {
        setID( ID );
    }

    NPCodePoint( int ID, byte[] data )
    {
        setID( ID );
        this.data_ = data;
        length_ = this.data_.length;
    }

/*
  don't use this - instead create the codepoint or find it in the list of
  prestored codepoints and then ask the codepoint for a databuffer of the
  desired length or bigger.
    NPCodePoint( int ID, int length, InputStream in )
        throws IOException
    {
        xlateObj = defaultXlateObj;
        setID( ID );
        if( (data == null) || (data.length < length) ){
            data = new byte[length];
        }
        in.read( data, 0, length );
        this.length = length;
    }
*/

    

    byte[] getDataBuffer()
    {
        return data_;
    }

    /**
      * getDataBuffer(int dataLength)
      * Get access to current data buffer and make sure it is big enough
      * to handle dataLength bytes.  It is expected that the caller will set the
      * code point data.
      * The length parameter should be the length of the code point data
      * (doesn't include the code point header).
      * Caller must also use the getOffset() method to find out how far into the
      * the databuffer they can begin writing the data to.
     **/
    byte[] getDataBuffer(int dataLength)
    {
        if( (data_ == null) || ((data_.length + offset_) < dataLength) )
        {
            data_ = new byte[dataLength];
            offset_ = 0;
        }
        length_ = dataLength;
        return data_;
    }

    /**
      *  returns the length of the code point data (does not include the
      *   code point header of a four byte length and 2 byte id).
      **/
    int getDataLength()
    {
        // total length of code point, length field, id field, and data.
        // we do it this way so that subclasses that have overridden getLength()
        // will work without overridding getDataLength()
        return( getLength() - LEN_HEADER );       
    }

    int getID()
    {
        return( ID_ );
    }

    /**
      * get offset to use in databuffer returned on getDataBuffer()
      *
      **/
    int getOffset()
    {
        return offset_;
    }


    /**
      *  method getLenth() returns the entire length of the code point including
      *  the header.  If you just want the length of the data in the codepoint you
      *  can subtract off the LEN_HEADER value or use getDataLength()
      **/
    int getLength()  
    {
        // total length of code point, length field, id field, and data.
        return( length_+LEN_HEADER );
    }


    // set the data buffer this code point should use.
    // current codepoint data is lost.
    // databuffer should be big enough to hold any expected codepoint data in the
    // future or it won't be used.
    // datalen parameter gives the number of bytes of codepoint data currently
    // in the databuffer.  It should be between 0 and the dataBuffer.length.
    // If the dataBuffer is empty, set this to 0;
    void setDataBuffer( byte[] dataBuffer, int datalen, int offset)
    {
        data_ = dataBuffer;
        length_ = datalen;
        offset_ = offset;
    }

    // @B1D // set the host ccsid for this codepoint for any text it has
    // @B1D void setHostCCSID(int ccsid)
    // @B1D {
    // @B1D     this.hostCCSID_ = ccsid;
    // @B1D }


    void setConverter(ConverterImpl converter)                      // @B1A
    {                                                               // @B1A
        this.converter_ = converter;                                // @B1A
    }                                                               // @B1A


    void setID( int ID )
    {
        this.ID_ = ID;
    }


    /**
      * reset - resets the code point to be empty
     **/
    void reset()
    {
        length_ = 0;
    }


}
