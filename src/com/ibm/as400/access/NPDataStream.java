///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: NPDataStream.java
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

/**
  *Base class of Network Print data streams. This same class is used
  *for both request and reply data streams.
  */

class NPDataStream extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    static final int NP_SERVER_ID = 0xE003;

    // Network Print Server Template.
    static final int TEMPLATE_LENGTH = 12;
    static final int ACTION_ID_OFFSET = HEADER_LENGTH + 0;
    static final int FLAGS_OFFSET = HEADER_LENGTH + 2;
    static final int RC_OFFSET = HEADER_LENGTH + 6;
    static final int EO_OFFSET = HEADER_LENGTH + 8;
    static final int LAST_REPLY_MASK = (int)0x80000000;
    static final int CODE_POINT_OFFSET = HEADER_LENGTH + TEMPLATE_LENGTH;

// @B1D    // Network Print Server "objects".
// @B1D    static final int SPOOLED_FILE = 0x0001;
// @B1D    static final int WRITER_JOB = 0x0002;
// @B1D    static final int PRINTER_DEVICE = 0x0003;
// @B1D    static final int OUTPUT_QUEUE = 0x0004;
// @B1D    static final int PRINTER_FILE = 0x0005;
// @B1D    static final int LIBRARY = 0x0006;
// @B1D    static final int RESOURCE = 0x0007;
// @B1D    static final int NP_SERVER = 0x0008;

    // Network Print Server "actions".
    static final int CREATE = 0x0001;
    static final int OPEN = 0x0002;
    static final int READ = 0x0003;
    static final int WRITE = 0x0004;
    static final int CLOSE = 0x0005;
    static final int HOLD = 0x0006;
    static final int RELEASE = 0x0007;
    static final int START = 0x0008;
    static final int END = 0x0009;
    static final int DELETE = 0x000A;
    static final int MOVE = 0x000B;
    static final int SEND = 0x000C;
    static final int CALL_EXIT_PROGRAM = 0x000D;
    static final int CHANGE_ATTRIBUTES = 0x000E;
    static final int RETRIEVE_ATTRIBUTES = 0x000F;
    static final int RETRIEVE_ATTRIBUTE_INFO = 0x0010;
    static final int RETRIEVE_MESSAGE = 0x0011;
    static final int ANSWER_MESSAGE = 0x0012;
    static final int WATCH = 0x0013;
    static final int CHECK_AUTHORITY = 0x0014;
    static final int PURGE = 0x0015;
    static final int LIST = 0x0016;
    static final int SEEK = 0x0017;
    static final int TELL = 0x0018;
    static final int SELECT_PAGE = 0x0019;          /* @A1A */
    static final int OPEN_MODIFIED_SPLF = 0x001A;   /* @A2A */

    // Network Print Server return codes
    static final int RET_OK              = 0x0000;
    static final int RET_INV_REQ_STRUCT  = 0x0001;
    static final int RET_INV_REQ_ID      = 0x0002;
    static final int RET_INV_ACT_ID      = 0x0003;
    static final int RET_INV_REQ_ACT     = 0x0004;
    static final int RET_INV_CODEPOINT   = 0x0005;
    static final int RET_INV_ATTR        = 0x0006;
    static final int RET_INV_ATTRVALUE   = 0x0007;
    static final int RET_NOT_AUTHORIZED  = 0x0008;
    static final int RET_CPF_MESSAGE     = 0x0009;
    static final int RET_INV_SPLF_HANDLE = 0x000A;
    static final int RET_SPLF_CREATE_ERR = 0x000B;
    static final int RET_CL_ERROR        = 0x000C;
    static final int RET_SPLF_NOT_OPEN   = 0x000D;
    static final int RET_SPLF_NO_MESSAGE = 0x000E;
    static final int RET_SPLF_OPEN_FAILED= 0x000F;
    static final int RET_SEEK_OFF_BAD    = 0x0010;
    static final int RET_SEEK_FAILED     = 0x0011;
    static final int RET_READ_INCOMPLETE = 0x0012;
    static final int RET_READ_EOF        = 0x0013;
    static final int RET_EMPTY_LIST      = 0x0014;
    static final int RET_FUNCTION_NOT_SUP= 0x0015;
    static final int RET_CANNOT_ACC_PRTF = 0x0016;
    static final int RET_CANNOT_ACC_PRTF_ATTR = 0x0017;
    static final int RET_WRITER_NOT_FOUND  = 0x0018;
    static final int RET_RETURN_CP_MISSING = 0x0019;
    static final int RET_NO_EXIT_PGM     = 0x001A;
    static final int RET_EXIT_PGM_DENIED = 0x001B;
    static final int RET_NLV_NOT_AVAILABLE = 0x001C;
    static final int RET_INV_BYTE_COUNT    = 0x001D;  /* @A1A */
    static final int RET_SPLF_NOT_FOUND    = 0x001E;  /* @A1A */
    static final int RET_INV_DATA_TYPE     = 0x001F;  /* @A1A */
    static final int RET_READ_OUT_OF_RANGE = 0x0020;  /* @A1A */
    static final int RET_PAGE_OUT_OF_RANGE = 0x0021;  /* @A1A */

    private int reqRepID_;
    private int correlation_;
    private int actionID_;
    // @B1D private int hostCCSID_;
    private ConverterImpl converter_;                   // @B1C

    //
    // we could use a hashTable (dictionary) of codepoints using their ID as
    // hashing keys but that is probably excessive since there are only 16 codepoints
    // currently and all IDs are sequential starting at ID # 1, it will work to use
    // a simple array of codepoints with their IDs as the index
    //
    // private Vector codePoints = new Vector();
    private NPCodePoint[] codePoints_ = new NPCodePoint[NPCodePoint.MAX_CODEPOINT_ID+1];

    NPDataStream(NPDataStream ds)
    {
       super();
       data_ = new byte[ds.data_.length];
       System.arraycopy(ds.data_, 0,
                        data_, 0,
                        data_.length);

       int i;
       reqRepID_    = ds.reqRepID_;
       correlation_ = ds.correlation_;
       actionID_    = ds.actionID_;
       // @B1D hostCCSID_   = ds.hostCCSID_;
       converter_   = ds.converter_;                        // @B1A

       for (i = 0; i< codePoints_.length; i++)
       {
          if (ds.codePoints_[i] != null)
          {
             codePoints_[i] = (NPCodePoint)ds.codePoints_[i].clone();
          }
       }
    }

    NPDataStream()
    {
        super();
        reqRepID_    = 0;
        correlation_ = 0;
        actionID_    = 0;
        // @B1D hostCCSID_   = 0;
        converter_   = null;        // @B1A
    }

    NPDataStream( int reqRepID )
    {
        super();

        correlation_ = 0;
        actionID_    = 0;
        // @B1D hostCCSID_   = 0;
        converter_   = null;        // @B1A

        setObject( reqRepID );
    }

    // This method is called when we need to copy this data stream into
    // a new datastream.  We (NP) have this method called when we are
    // receiving a list of objects back from the server - each item in the
    // the list has a new datastream created to catch it.
    Object getNewDataStream()
    {
        return new NPDataStream(this);
    }

 
    public int hashCode()
    {
        return (reqRepID_ | 0x8000);
    }

    void setObject( int reqRepID )
    {
        this.reqRepID_ = reqRepID;
    }

    void setAction( int actionID )
    {
        this.actionID_ = actionID;
    }

    void setCorrelation( int correlation )
    {
        this.correlation_ = correlation;
    }

    // @B1D void setHostCCSID(int ccsid )
    // @B1D {
    // @B1D     hostCCSID_ = ccsid;
    // @B1D     // Look at any code points to determine how big this
    // @B1D     // data stream is. There does not have to be any code
    // @B1D     // points. The NP data stream always uses a fixed size
    // @B1D     // template, any variable data for the request/reply
    // @B1D     // is carried in code points.
    // @B1D 
    // @B1D     for( int i=0; i < codePoints_.length; i++ )
    // @B1D     {
    // @B1D         NPCodePoint cp = codePoints_[i];
    // @B1D         if (cp != null)
    // @B1D         {
    // @B1D             // Let the codepoint know the host ccsid encoding for any text data
    // @B1D              cp.setHostCCSID(hostCCSID_);
    // @B1D         }
    // @B1D     }
    // @B1D 
    // @B1D 
    // @B1D }

    void setConverter(ConverterImpl converter)                                          // @B1A
    {                                                                                   // @B1A
        converter_ = converter;                                                         // @B1A
                                                                                        // @B1A
        // Look at any code points to determine how big this                            // @B1A
        // data stream is. There does not have to be any code                           // @B1A
        // points. The NP data stream always uses a fixed size                          // @B1A
        // template, any variable data for the request/reply                            // @B1A
        // is carried in code points.                                                   // @B1A
        for( int i=0; i < codePoints_.length; i++ )                                     // @B1A
        {                                                                               // @B1A
            NPCodePoint cp = codePoints_[i];                                            // @B1A
            if (cp != null)                                                             // @B1A
            {                                                                           // @B1A
                // Let the codepoint know the host ccsid encoding for any text data     // @B1A
                cp.setConverter(converter_);                                            // @B1A
            }                                                                           // @B1A
        }                                                                               // @B1A
    }                                                                                   // @B1A

    void addCodePoint( NPCodePoint codePoint )
    {
        int ID = codePoint.getID();
        if (validCPID(ID))
        {
           codePoints_[ID] = codePoint;
        }
    }

    NPCodePoint getCodePoint(int ID)
    {
       NPCodePoint cp = null;
       if (validCPID(ID))
       {
          cp = codePoints_[ID];
       }
       return (cp);
    }

    void resetCodePoints()
    {
        for (int i = 0; i<codePoints_.length; i++)
        {
            codePoints_[i] = null;
        }
    }

    int getReturnCode()
    {
        return get16bit(RC_OFFSET);
    }

    boolean isLastReply()
    {
        return( (get32bit(FLAGS_OFFSET) & LAST_REPLY_MASK) == 0 );
    }

    // Most of the work for a request data stream happens here.

    void write( OutputStream out )
        throws IOException
    {
        // The minimum length of this data stream is the client
        // access header and the network print server template.

        int dsLength = HEADER_LENGTH + TEMPLATE_LENGTH;

        // Look at any code points to determine how big this
        // data stream is. There does not have to be any code
        // points. The NP data stream always uses a fixed size
        // template, any variable data for the request/reply
        // is carried in code points.

        for( int i=0; i < codePoints_.length; i++ ){
            NPCodePoint cp = codePoints_[i];
            if (cp != null)
            {
                // Let the codepoint know the host ccsid encoding for any text data
                // Do this before we ask for its length so that it can correctly
                // construct the raw data if it needs to.
                // @B1D cp.setHostCCSID(hostCCSID_);
                cp.setConverter(converter_);                            // @B1A

                // don't send empty code points
                if (cp.getDataLength() != 0)
                {
                    dsLength += cp.getLength();
                }
            }
        }

        // Reallocate ClientAccessDataStream()'s data buffer to the
        // size it needs to be for the header, template, and any
        // code points.

        data_ = new byte[dsLength];

        // Build the Client Access Data Stream header. What was
        // built by the super class constructor was tossed when we
        // reallocated data_.

        setLength( dsLength );
        setHeaderID( 0 );
        setServerID( NP_SERVER_ID );
        setCSInstance(0);
        super.setCorrelation( correlation_ );
        setTemplateLen( TEMPLATE_LENGTH );
        setReqRepID( reqRepID_ );

        // Build the Network Print Server Data Stream template.
        set16bit( actionID_, ACTION_ID_OFFSET );
        set32bit( 0, FLAGS_OFFSET );
        set16bit( 0, RC_OFFSET );
        set32bit( 0, EO_OFFSET );

        // If any code points were added to this data stream, write
        // them into the base classes data buffer.

        int cpOffset = CODE_POINT_OFFSET;
        int length;
        for( int i=0; i < codePoints_.length; i++ ){
            NPCodePoint cp = codePoints_[i];
            if (cp != null)
            {
                // don't send empty code points
                if (cp.getDataLength() != 0)
                {
                   length = cp.getLength();
                   set32bit( length, cpOffset );
                   set16bit( cp.getID(), cpOffset+4 );
                   cpOffset += NPCodePoint.LEN_HEADER;

                   // get pointer to codepoint data
                   byte[] cpData = cp.getDataBuffer();
                   int    dataOffset = cp.getOffset();

                   // adjust length to be of the data in the code point only
                   length -= NPCodePoint.LEN_HEADER;

                   //  may need to check if length > 0x7FFF (negative number)
                   System.arraycopy(cpData, dataOffset,                  // source
                                    data_, cpOffset,                     // dest
                                    length);

                   cpOffset += length;
                }
            }
        }

        super.write(out);

        if (Trace.isTraceOn() && Trace.isTraceDatastreamOn())
        {
            Trace.log(Trace.DATASTREAM,
                      "Data stream sent...",
                      data_);
        }

    }

    // Most of the work for a reply data stream happens here.

    int readAfterHeader( InputStream in )
        throws IOException
    {
        // The header has already been read by ClientAccessDataStream().
        int bytesAvailable = getLength() - HEADER_LENGTH;

        // Read the required template.
        int bytesRead = readFromStream(in, data_, HEADER_LENGTH, getTemplateLen() );

        //  - Check the return code
        //  - don't log an ERROR here - just info; this dumps a stack trace and
        //  some non-0 return codes are OK (empty list and some others).
        if( 0 != getReturnCode() )
        {
	       Trace.log(Trace.INFORMATION, " Netprint Datastream Return code was " + getReturnCode());
        }

        // Check for any code points in the data stream. Code points are optional.
        // If there are codepoints coming back, they should be prestored in the the
        // array of codepoints.
        byte[] llcp = new byte[NPCodePoint.LEN_HEADER];
        byte[] cpData = null;
        NPCodePoint cp = null;
        int    temp;
        int    dataOffset;

        while( bytesRead < bytesAvailable )
        {
            temp = readFromStream(in, llcp, 0, llcp.length);
            if (temp != llcp.length)
            {
		        Trace.log(Trace.ERROR, "Didn't read 6 bytes that we needed to!");
                throw new IOException(Integer.toString(llcp.length));
            }
            bytesRead += temp;
            int cpDataLength = BinaryConverter.byteArrayToInt(llcp, 0)-llcp.length; //sms
            int cpID = BinaryConverter.byteArrayToUnsignedShort(llcp,4);            //sms

            // if the codepoint ID is not valid or is not prestored
            //  create a generic codepoint
            if (!validCPID(cpID) || (codePoints_[cpID] == null))
            {
		         Trace.log(Trace.ERROR, "NPDataStream receiving orphan code point ID =" + cpID);
                 cp = new NPCodePoint(cpID);
                 if (!validCPID(cpID))
                 {
                    codePoints_[0] = cp;
                 } else {
                    codePoints_[cpID] = cp;
                 }
            } else {
                 cp = codePoints_[cpID];
            }

            // let the codepoint know the host ccsid encoding for any text data
            // @B1D cp.setHostCCSID(hostCCSID_);
            cp.setConverter(converter_);                        // @B1A

            cpData = cp.getDataBuffer(cpDataLength);  // ask codepoint for a receive buffer
            dataOffset = cp.getOffset();              // find out where to start writing

            temp  = readFromStream(in, cpData, dataOffset, cpDataLength);
            if (temp != cpDataLength)
            {
                Trace.log(Trace.ERROR, "Didn't read " + cpDataLength + " bytes that we needed!");
                throw new IOException(Integer.toString(cpDataLength));
            }
            bytesRead += temp;


        }

        return( bytesRead );
    }


   private boolean validCPID(int ID)
   {

      if ((ID > 0) && (ID < codePoints_.length))
      {
         return true;
      }
      return false;
   }

   

}  // end of NPDataStream class
