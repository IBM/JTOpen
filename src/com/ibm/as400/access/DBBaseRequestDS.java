///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: DBBaseRequestDS.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.ByteArrayOutputStream; //@PDA for lazy close check
import java.io.CharConversionException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.sql.SQLException;



//--------------------------------------------------------------------
//
// Each request consists of 3 parts: Header, Template, and
//                                   Optional / Variable-length Data
//
//    Header:   Bytes  1 - 4:  Length of the request
//                     5 - 6:  Header id
//                     7 - 8:  Client/Server id
//                     9 - 12: Client/Server instance
//                    13 - 16: Correlation id
//                    17 - 18: Length of template
//                    19 - 20: Request id
//
//    Template: This is fixed length data that is required.
//              Each request has it's own template format and may vary
//              depending on both the request id and client/server id.
//              The operational results are stored on the i5/OS
//              system and can be received at a later time.
//                Bitmap: Used to identify the operation results to
//                        return.  Bit 1 is the left-most bit when it
//                        arrives at the i5/OS system
//                        Bit 1: 1=reply should be sent immediately to
//                                 the client application
//                               0=reply is not sent and the rest of
//                                 the bit map is ignored
//                        Bit 2: Message Id
//                        Bit 3: First Level Text
//                        Bit 4: Second Level Text
//                        Bit 5: Data Format
//                        Bit 6: Result Data
//                        Bit 7: SQLCA SQL Communications Area
//                        Bit 8: Server Attributes
//                        Bit 9: Parameter Marker format
//                        Bit 10: Translation Tables
//                        Bit 11: Data Source Information
//                        Bit 12: Package Information
//                        Bit 13: Request is RLE compressed                         @E3A
//                        Bit 14: RLE compression reply desired                     @E3A
//                        Bit 15: Extended column descriptors                       @F1C
//                        Bit 16: Varying Length Column Compression                 @K54
//                        Bit 17-32: Reserved                                       @E3C @F1C @K54
//                Reserved Area:
//                RTNORS:  Numeric value of the Operation Results Set
//                         (ORS) that contains the data to be returned
//                         to the client.
//                         It must be non-negative.
//                FILLORS: Numeric value of the ORS used to store the
//                         data for this function.
//                         It must be non-negative.
//                BONORS:  Numeric value of the "based-on" ORS.
//                         If used, it must be positive.  If it is not
//                         positive, it will be ignored.
//                RPBHNDL: Numeric value of the Request Parameter Block
//                         (RPB) to be used.
//                         This may be set to zero indicating that an RPB
//                         will not be used.
//                PM DESCHNDL:  Parameter marker descriptor handle
//                              identifier.
//                              This field is only used when the serverId
//                              is "E004"X (SQL).
//                PARMCNT: Number of parameters in the optional/variable-
//                         length data.
//
//    Optional / Variable-length Data:
//               This is optional data and/or mandatory data that
//               varies in length.
//                 o Use format LL CP data
//                     LL = length
//                     CP = code point
//                 o Character data is optionally preceede by a
//                   coded character set of the data (CCSID)
//                 o The CCSID can identify the code page and the
//                   character set of the data
//                 o All LLs include the length of the LL
//                 o The client is responsible for providing numerics
//                   in the server format so the system will not
//                   need to perform data conversions.
//
//---------------------------------------------------------------------

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

      5. Then you are ready to send the request to the system.  There are
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

//-----------------------------------------------------//
//                                                     //
//  addParameter is used to add an optional / variable-//
//  length parameter to the datastream.  After all     //
//  the parameters are added, dataStreamReady should   //
//  be called and then the data stream will be done.   //
//                                                     //
//  The addParameter method is used to handle the      //
//  following types of parameters:                     //
//    Request Function Parameters                      //
//    Attribute Function Parameters                    //
//    Descriptor Function Parameters                   //
//    Result Set Function Parameters                   //
//    RPB Function Parameters                          //
//                                                     //
//  The addParameter used will depend on the format,   //
//  not the type.                                      //
//                                                     //
//  Each addParameter will do the following:           //
//    1. Test to make sure there is room in the        //
//       data stream.                                  //
//    2. Put the length (LL) into the data stream.     //
//       Include the 4 bytes for the LL in the length. //
//    3. Put the code point (parameter id) into the    //
//       the data stream.                              //
//    4. Put the data into the data stream.            //
//    5. Add the length to the total request length.   //
//    6. Add to parameter count.                       //
//                                                     //
//-----------------------------------------------------//


abstract class DBBaseRequestDS
extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";


  // Private data.
  private int                    currentOffset_;
  private int                    lockedLength_;
  private int                    operationResultBitmap_;
  private int                    parameterCount_;
  private boolean                rleCompressed_ = false;              // @E3A
  private final DBStorage storage_ = DBDSPool.storagePool_.getUnusedStorage(); //@P0A


  // Values for operation result bitmap.
  public static final int       ORS_BITMAP_RETURN_DATA                 = 0x80000000;    // Bit 1
  public static final int       ORS_BITMAP_MESSAGE_ID                  = 0x40000000;    // Bit 2
  public static final int       ORS_BITMAP_FIRST_LEVEL_TEXT            = 0x20000000;    // Bit 3
  public static final int       ORS_BITMAP_SECOND_LEVEL_TEXT           = 0x10000000;    // Bit 4
  public static final int       ORS_BITMAP_DATA_FORMAT                 = 0x08000000;    // Bit 5
  public static final int       ORS_BITMAP_RESULT_DATA                 = 0x04000000;    // Bit 6
  public static final int       ORS_BITMAP_SQLCA                       = 0x02000000;    // Bit 7
  public static final int       ORS_BITMAP_SERVER_ATTRIBUTES           = 0x01000000;    // Bit 8
  public static final int       ORS_BITMAP_PARAMETER_MARKER_FORMAT     = 0x00800000;    // Bit 9
  // public static final int       ORS_BITMAP_TRANSLATION_TABLES          = 0x00400000;    // Bit 10
  // public static final int       ORS_BITMAP_DATA_SOURCE_INFORMATION     = 0x00200000;    // Bit 11
  public static final int       ORS_BITMAP_PACKAGE_INFO                = 0x00100000;    // Bit 12
  public static final int       ORS_BITMAP_REQUEST_RLE_COMPRESSION     = 0x00080000;    // Bit 13       @E3A
  public static final int       ORS_BITMAP_REPLY_RLE_COMPRESSION       = 0x00040000;    // Bit 14       @E3A
  public static final int       ORS_BITMAP_EXTENDED_COLUMN_DESCRIPTORS = 0x00020000;    // Bit 15       @F1A    
  public static final int       ORS_BITMAP_VARIABLE_LENGTH_FIELD_COMPRESSION = 0x00010000;  //Bit 16    @K54
  public static final int       ORS_BITMAP_CURSOR_ATTRIBUTES           = 0x00008000;   //Bit 17        @CUR



  // Server IDs.
  protected static final int    SERVER_SQL     = 0xE004;
  protected static final int    SERVER_NDB     = 0xE005;
  protected static final int    SERVER_ROI     = 0xE006;


  // This is the length that data must be to be considered for RLE compression.      @E3A
  // It is currently set to 1024 + 40 (header and template + 1K).  It must be        @E3A
  // set to at least 40.                                                             @E3A
  private static final int      RLE_THRESHOLD_            = 1064;                 // @E3A


/**
Constructor.
**/
  protected DBBaseRequestDS(int requestId,
                            int rpbId,
                            int operationResultBitmap,
                            int parameterMarkerDescriptorHandle)
  {
    super();

    initialize(requestId, rpbId, operationResultBitmap, parameterMarkerDescriptorHandle); //@P0A
  }



  //@P0A - This code used to be in the constructor.
  // Now, just call initialize() instead of constructing a new datastream.
  void initialize(int requestId,
                  int rpbId,
                  int operationResultBitmap,
                  int parameterMarkerDescriptorHandle)
  {
    // Allocate the large byte array for storage of the
    // data stream.

    data_ = storage_.data_; //@P0C

    // Initialization.
    currentOffset_          = HEADER_LENGTH + 20;
    parameterCount_         = 0;
    operationResultBitmap_  = operationResultBitmap;
    rleCompressed_ = false; //@P0A
    lockedLength_ = 0; //@P0A

    // Data stream header.
    setHeaderID(0);
    setCSInstance(0);
    setTemplateLen(20);
    setReqRepID(requestId);

    // Data stream template.
    set32bit(operationResultBitmap, 20);   // Operation result bitmap.
    set16bit(rpbId, 28);                   // Return ORS handle.
    set16bit(rpbId, 30);                   // Fill ORS handle.
    setBasedOnORSHandle(0);                // Based on ORS handle.
    set16bit(rpbId, 34);                   // RPB handle.
    setParameterMarkerDescriptorHandle(parameterMarkerDescriptorHandle);

  }


/**
Adds another operation result to the operation result bitmap.
**/
  public void addOperationResultBitmap(int value)
  {
    operationResultBitmap_ |= value;
    set32bit(operationResultBitmap_, 20);
  }



/**
Adds a 1 byte parameter.
**/
  protected void addParameter(int codePoint, byte value) throws DBDataStreamException
  {
    lock(1, codePoint);

    data_[currentOffset_] = value;

    unlock();
  }



/**
Adds a 2 byte parameter.
**/
  protected void addParameter(int codePoint, short value)
  throws DBDataStreamException
  {
    lock(2, codePoint);

    set16bit(value, currentOffset_);

    unlock();
  }



/**
Adds a 2 byte parameter with an extra 4 byte value.
**/
  protected void addParameter(int codePoint, short value, int extra)
  throws DBDataStreamException
  {
    lock(6, codePoint);

    set16bit(value, currentOffset_);
    set32bit(extra, currentOffset_ + 2);

    unlock();
  }



/**
Adds a 4 byte parameter.
**/
  protected void addParameter(int codePoint, int value)
  throws DBDataStreamException
  {
    lock(4, codePoint);

    set32bit(value, currentOffset_);

    unlock();
  }



/**
Adds a byte array parameter.
**/
  protected void addParameter(int codePoint, byte[] value)
  throws DBDataStreamException
  {
      if(value == null){            //@eWLM     Can pass a null value in
          //"Locks" the request datastream for addition of a parameter. This will determine if there is space left in the data byte array and grow it as needed.
          lock(0, codePoint);       //@eWLM      Locks the datastream and adds the codepoint to it
          unlock();                 //@eWLM     "Unlocks" the datastream
          if(Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Value is null, sending only length and codepoint.");  //@eWLM
      }                             //@eWLM
      else                          //@eWLM
          addParameter(codePoint, value, 0, value.length);
  }

  protected void addParameter(int codePoint, byte[] value, int offset, int length) throws DBDataStreamException
  {
    lock(length, codePoint);
    System.arraycopy(value, offset, data_, currentOffset_, length);
    unlock();
  }

/**
@B2A Adds a byte array parameter including CCSID and length.
**/
  protected void addParameter(int codePoint,
                              byte[] value,
                              boolean overloadThisMethod)
  throws DBDataStreamException
  {
    addParameter(codePoint, value, 0, value.length, overloadThisMethod);
  }


  protected void addParameter(int codePoint, byte[] value, int offset, int length, boolean overloadThisMethod) throws DBDataStreamException
  {
    lock(value.length + 6, codePoint);
    set16bit((short)0xFFFF, currentOffset_);
    set32bit(length, currentOffset_ + 2);
    System.arraycopy(value, offset, data_, currentOffset_ + 6, length);
    unlock();
  }

// @E4A
/**
Adds a fixed length string parameter which contains only numbers.
This assumption avoids character conversion.
**/
  protected void addParameter(int codePoint, String value)                        // @E4A
  throws DBDataStreamException                                                // @E4A
  {                                                                               // @E4A
    char[] asChars = value.toCharArray();                                       // @E4A
    lock(asChars.length + 2, codePoint);                                        // @E4A

    set16bit(37, currentOffset_);    // CCSID                                      @E4A

    int offset = currentOffset_ + 2;                                            // @E4A
    for (int i = 0; i < asChars.length; ++i, ++offset)
    {                         // @E4A
      if (asChars[i] == ' ')                                                  // @E4A
        data_[offset] = 0x40;                                               // @E4A
      else                                                                    // @E4A
        data_[offset] = (byte)(asChars[i] | 0x00F0);                        // @E4A
    }                                                                           // @E4A

    unlock();                                                                  // @E4A
  }                                                                               // @E4A



/**
Adds a fixed length string parameter.
**/
//
// This does not need to work with double byte character sets
// as far as I know.
//
  protected void addParameter(int codePoint,
                              ConvTable converter, //@P0C
                              String value,
                              int valueLength)
  throws DBDataStreamException
  {
    lock(valueLength + 2, codePoint);

    set16bit(converter.ccsid_, currentOffset_);    // CCSID @P0C

    try
    {
      converter.stringToByteArray(value.substring (0, valueLength),
                                  data_, currentOffset_ + 2);
    }
    catch (CharConversionException e)
    {
      throw new DBDataStreamException();
    }

    unlock();
  }



/**
Adds a variable length string parameter.
**/
  protected void addParameter(int codePoint,
                              ConvTable converter, //@P0C
                              String value)
  throws DBDataStreamException, SQLException                              // @E9a
  {
    // @A1C
    // Changed code to use the converter to find out the exact
    // number of bytes the string needs to occupy so that it works
    // for both single-byte and double-byte strings.
    byte[] rawBytes = converter.stringToByteArray(value);

    if (rawBytes.length > 65535)                                            // @E9a
      JDError.throwSQLException (JDError.EXC_SQL_STATEMENT_TOO_LONG);      // @E9a 

    lock(rawBytes.length + 4, codePoint);

    set16bit(converter.ccsid_, currentOffset_);        // CCSID @P0C
    set16bit(rawBytes.length, currentOffset_ + 2);         // SL

    try
    {
      System.arraycopy(rawBytes, 0, data_, currentOffset_ + 4,
                       rawBytes.length);
    }
    catch (Exception e)
    {
      throw new DBDataStreamException();
    }

    unlock();
  }

//@540
/**
Adds a variable length string parameter with a four byte length.
**/
//Note:  This method will only be called if running to a V5R4 or later system.  boolean v5r4 is just to distinguish this
// method from the method above (protected void addParameter(int codePoint,ConvTable converter, String value)).
  protected void addParameter(int codePoint, boolean v5r4,
                              ConvTable converter,
                              String value)
  throws DBDataStreamException, SQLException                              
  {
    byte[] rawBytes = converter.stringToByteArray(value);

    if (rawBytes.length > 2097152)             //CHECK TO SEE IF GREATER THAN 2MB                                
      JDError.throwSQLException (JDError.EXC_SQL_STATEMENT_TOO_LONG);      

    lock(rawBytes.length + 6, codePoint);

    set16bit(converter.ccsid_, currentOffset_);        // CCSID 
    set32bit(rawBytes.length, currentOffset_ + 2);     // SL  - Set 4-byte length

    try
    {
      System.arraycopy(rawBytes, 0, data_, currentOffset_ + 6,
                       rawBytes.length);
    }
    catch (Exception e)
    {
      throw new DBDataStreamException();
    }

    unlock();
  }

  /**
Adds a fixed length string parameter, but uses character conversion.
**/
  protected void addParameter(int codePoint,
                              ConvTable converter, //@P0C
                              String value, 
                              boolean fixed)
  throws DBDataStreamException, SQLException                              // @E9a
  {
      if(fixed){
        // Changed code to use the converter to find out the exact
        // number of bytes the string needs to occupy so that it works
        // for both single-byte and double-byte strings.
        byte[] rawBytes = converter.stringToByteArray(value);
    
        if (rawBytes.length > 65535)                                            
        JDError.throwSQLException (JDError.EXC_SQL_STATEMENT_TOO_LONG);       

        lock(rawBytes.length + 4, codePoint);

        set16bit(converter.ccsid_, currentOffset_);       

        try
        {
            System.arraycopy(rawBytes, 0, data_, currentOffset_ + 2,
                       rawBytes.length);
        }
        catch (Exception e)
        {
        throw new DBDataStreamException();
        }

        unlock();
      }
      else
          addParameter(codePoint, converter, value);
  }

/**
Adds an empty code point
**/
  protected void addParameter(int codePoint)
  throws DBDataStreamException
  {
    lock(0, codePoint);
    unlock();
  }


/**
Adds a library list parameter.
**/
  protected void addParameter(int codePoint,
                              ConvTable converter, //@P0C
                              char[] indicators,
                              String[] libraries)
  throws DBDataStreamException
  {
    int parameterLength = 4;
    for (int i = 0; i < libraries.length; ++i)
      parameterLength += 3 + libraries[i].length();

    lock(parameterLength, codePoint);

    set16bit(converter.ccsid_, currentOffset_);    // CCSID @P0C
    set16bit(libraries.length, currentOffset_ + 2);    // number of libraries

    int offset = 4;
    try
    {
      for (int i = 0; i < libraries.length; ++i)
      {
        Character ch = new Character(indicators[i]);
        converter.stringToByteArray(ch.toString(), data_, currentOffset_ + offset);
        set16bit(libraries[i].length(), currentOffset_ + offset + 1);
        converter.stringToByteArray(libraries[i], data_, currentOffset_ + offset + 3);
        offset += 3 + libraries[i].length();
      }
    }
    catch (CharConversionException e)
    {
      throw new DBDataStreamException();
    }

    unlock();
  }



/**
Adds a NLSS indicator parameter.
**/
  protected void addParameter(int codePoint,
                              ConvTable converter, //@P0C
                              int type,
                              String tableFile,
                              String tableLibrary,
                              String languageId)
  throws DBDataStreamException
  {
    int parameterLength;
    switch (type)
    {
      case 0:
      default:
        parameterLength = 2;
        break;
      case 1:
      case 2:
        parameterLength = 7;
        break;
      case 3:
        parameterLength = 8 + tableFile.length() + tableLibrary.length();
        break;
    }

    lock(parameterLength, codePoint);

    set16bit(type, currentOffset_);            // sort value

    try
    {
      switch (type)
      {
        case 0:
        default:
          break;
        case 1:
        case 2:
          set16bit(converter.ccsid_, currentOffset_ + 2);    // CCSID @P0C
          converter.stringToByteArray(languageId, data_,
                                      currentOffset_ + 4);     // sort language id
          break;
        case 3:
          set16bit(converter.ccsid_, currentOffset_ + 2);    // CCSID @P0C
          set16bit(tableFile.length(),
                   currentOffset_ + 4);            // SL
          converter.stringToByteArray(tableFile, data_,
                                      currentOffset_ + 6);  // sort table file
          set16bit(tableLibrary.length(),
                   currentOffset_ + 6 + tableFile.length()); // SL
          converter.stringToByteArray(tableLibrary,  data_,
                                      currentOffset_ + 8 + tableFile.length()); // sort table library
          break;
      }
    }
    catch (CharConversionException e)
    {
      throw new DBDataStreamException();
    }

    unlock();
  }



/**
Adds a DBOverlay parameter.
**/
  protected void addParameter(int codePoint,
                              DBOverlay value)
  throws DBDataStreamException
  {
    lock(value.getLength(), codePoint);

    value.overlay(data_, currentOffset_);

    unlock();
  }



/**
Clears an operation result from the operation result bitmap.
**/
  public void clearOperationResultBitmap(int value)                   // @E3A
  {                                                                   // @E3A
    if ((operationResultBitmap_ & value) != 0)
    {                   // @E3A
      operationResultBitmap_ ^= value;                            // @E3A
      set32bit(operationResultBitmap_, 20);                       // @E3A
    }                                                               // @E3A
  }                                                                   // @E3A



  public void compress()                                              // @E3A
  {                                                                   // @E3A
    rleCompressed_ = true;                                          // @E3A
  }                                                                   // @E3A



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
  void dump(PrintStream ps)
  {
    dump(ps, data_, currentOffset_);

    // Report whether or not the datastream was compressed.                    @E3A
    if (rleCompressed_)                                                     // @E3A
      ps.println("Request was sent RLE compressed.");                     // @E3A
  }



/**
Output the byte stream contents to the specified PrintStream.
The output format is two hex digits per byte, one space every
four bytes, and sixteen bytes per line.

@param ps the output stream
@param data the data
@param length the length
**/
  static void dump(PrintStream ps, byte[] data, int length)
  {
    synchronized(ps)
    {                                                          // @E1A

      StringBuffer hexBuffer  = new StringBuffer();
      StringBuffer charBuffer = new StringBuffer();
      int i;
      for (i = 0; i < length; i++)
      {

        // Convert the data to 2 digits of hex.
        String temp = "00" + Integer.toHexString(data[i]);
        String hex = temp.substring(temp.length() - 2);
        hexBuffer.append(hex.toUpperCase());

        // Pad hex output at every 4 bytes.
        if (i % 4 == 3)
          hexBuffer.append(" ");

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
        {                                                              // @E1C
          // If we are here, it means that the EBCDIC to ASCII            // @E1A
          // conversion resulted in an ASCII character that does          // @E1A
          // not make sense.  Lets try it as a Unicode character          // @E1A
          // straight up.                                                 // @E1A
          if (data[i] == 0x40)   // This could be either Unicode '@'      // @E1A
            ch = ' ';          // or EBCDIC ' '.  We will assume the    // @E1A
                               // latter for dumps.                     // @E1A
          else if ((data[i] >= 0x20) && (data[i] <= 0x7E))                // @E1A
            ch = (char) data[i];                                        // @E1A
          else                                                            // @E1A
            ch = '.';
        }                                                                   // @E1A

        charBuffer.append(ch);

        // Start a new line at every 16 bytes.
        if (i % 16 == 15)
        {
          ps.println(hexBuffer + "  [" + charBuffer + "]");
          hexBuffer  = new StringBuffer();
          charBuffer = new StringBuffer();
        }
      }

      // Pad out and print the last line if necessary.
      if (i % 16 != 0)
      {
        int hexBufferLength = hexBuffer.length();
        for (int j = hexBufferLength; j <= 35; ++j)
          hexBuffer.append(" ");
        ps.println(hexBuffer + "  [" + charBuffer + "]");
      }

    }                                                                           // @E1A
  }


  public int getOperationResultBitmap()                       // @E2A
  {                                                           // @E2A
    return operationResultBitmap_;                          // @E2A
  }                                                           // @E2A

  // @E7a new method.
  // Make sure the buffer is free before going away.
  protected void finalize()
  throws Throwable
  {
    //@P0D freeCommunicationsBuffer();
    if (storage_ != null) storage_.inUse_ = false; //@P0A
    data_ = null; //@P0A
    super.finalize();
  }



/**
"Locks" the request datastream for addition of a parameter.
This will determine if there is space left in the data
byte array and grow it as needed.

@param length The length to be added to the data stream,
              in bytes, not including the LL and CP.
**/
  private void lock(int length, int codePoint)
  throws DBDataStreamException
  {
    if (storage_.checkSize(currentOffset_ + length + 6))
      data_ = storage_.data_; //@P0C
    lockedLength_ = length;

    set32bit(length + 6, currentOffset_);          // LL
    set16bit(codePoint, currentOffset_ + 4);       // CP

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
  public void setBasedOnORSHandle(int value)
  {
    set16bit(value, 32);
  }



/**
"Unlocks" the request datastream after addition of a parameter.
**/
  private void unlock()
  {
    currentOffset_ += lockedLength_;
    ++parameterCount_;
  }



/**
Sets the parameter marker descriptor handle.
**/
  public void setParameterMarkerDescriptorHandle(int value)
  {
    set16bit(value, 36);
  }





/**
Overrides the superclass to write the datastream.
**/
  void write(OutputStream out)
  throws IOException
  {
    setLength(currentOffset_);
    set16bit(parameterCount_, 38);

    if (rleCompressed_)
    {                                                                   // @E3A

      // Check to see if it is worth doing compression.                                      @E3A
      if (currentOffset_ > RLE_THRESHOLD_)
      {                                              // @E3A

        // Get another piece of storage from the pool.                                     @E3A
        DBStorage secondaryStorage = DBDSPool.storagePool_.getUnusedStorage();                  // @E3A @P0C
        try //@P0A
        {
          secondaryStorage.checkSize(currentOffset_);                                     // @E3A
          byte[] compressedBytes = secondaryStorage.data_;                      // @E3A @P0C

          // Compress the bytes not including the header (20 bytes) and template             @E3A
          // (20 bytes).  If the compression was successful, send the compressed             @E3A
          // bytes.  Otherwise, send the bytes as normal.                                    @E3A
          //
          // The format is this:                                                             @E3A
          // Bytes:           Description:                                                   @E3A
          //   4              LL - Compressed length of the entire datastream.               @E3A
          //  36              The rest of the uncompressed header and template.              @E3A
          //   4              ll - Length of the compressed data + 10.                       @E5A
          //   2              CP - The compression code point.                               @E5A
          //   4              Decompressed length of the data.                               @E3A
          //  ll-10           Compressed data.                                               @E3A @E5C
          int dataLength = currentOffset_ - 40;                                           // @E3A

          int compressedSize = DataStreamCompression.compressRLE(data_, 40,               // @E3A
                                                                 dataLength, compressedBytes, 50,                                            // @E3A @E5C
                                                                 DataStreamCompression.DEFAULT_ESCAPE);                                      // @E3A
          if (compressedSize > 0)
          {                                                       // @E3A
            int compressedSizeWithHeader = compressedSize + 50;                         // @E3A @E5C
            BinaryConverter.intToByteArray(compressedSizeWithHeader, compressedBytes, 0); // @E3A
            System.arraycopy(data_, 4, compressedBytes, 4, 36);                         // @E3A
            BinaryConverter.intToByteArray(compressedSize + 10, compressedBytes, 40);   // @E5A
            BinaryConverter.shortToByteArray((short)AS400JDBCConnection.DATA_COMPRESSION_RLE_, compressedBytes, 44);       // @E5A
            BinaryConverter.intToByteArray(dataLength, compressedBytes, 46);            // @E3A @E5C

            // Synchronization is added around the socket                                  @E3A
            // write so that requests from multiple threads                                @E3A
            // that use the same socket won't be garbled.                                  @E3A
            synchronized(out)
            {                                                         // @E3A
              out.write(compressedBytes, 0, compressedSizeWithHeader);                // @E3A
              out.flush();                                                            // @W1A
            }
            if (Trace.traceOn_) Trace.log(Trace.DATASTREAM, "Data stream sent (connID="+connectionID_+") ...", compressedBytes, 0, compressedSizeWithHeader); //@E6A @P0C
          }                                                                               // @E3A
          else
          {                                                                          // @E3A
            rleCompressed_ = false;   // Compression failed.                            // @E3A
          }                                                                               // @E3A
        }
        finally //@P0A
        {
          secondaryStorage.inUse_ = false; //@P0A
        }
      }                                                                                   // @E3A
      else
      {                                                                              // @E3A
        rleCompressed_ = false;       // Compression is not worth it.                   // @E3A
      }                                                                                   // @E3A
    }                                                                                       // @E3A

    if (!rleCompressed_)
    {                                                                  // @E3A

      // The compression was not successful, send the request uncompressed                   @E3A
      // (but still ask for the reply to be compressed).                                     @E3A
      clearOperationResultBitmap(ORS_BITMAP_REQUEST_RLE_COMPRESSION);                     // @E3A

      // @A0A
      // Synchronization is added around the socket
      // write so that requests from multiple threads
      // that use the same socket won't be garbled.
      synchronized(out)
      {
        out.write(data_, 0, currentOffset_);
        out.flush();                                         //@W1a
      }
      //@PDA only trace if stream is actually being sent now. (no trace on lazy close here)
      if (Trace.traceOn_ && !(out instanceof ByteArrayOutputStream)) Trace.log(Trace.DATASTREAM, "Data stream sent (connID="+connectionID_+") ...", data_, 0, currentOffset_);  //@E6A @P0C
    }                                                                                       // @E3A
  }
}




