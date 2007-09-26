///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DDMObjectDataStream.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
//
// @A1 - 09/19/2007 - The NULL Field Byte Map array must be set based on the 
//       maximum number of fields for any format in a given file.  See further 
//       detailed explaination below for the getObjectS38BUF() method.
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.CharConversionException;
import java.io.UnsupportedEncodingException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;

/**
 *DDM object data stream.
**/
class DDMObjectDataStream extends DDMDataStream
{
  private static final String copyright = "Copyright (C) 1997-2002 International Business Machines Corporation and others.";

  /**
   *Constructs a DDMObjectDataStream object.
  **/
  DDMObjectDataStream()
  {
    super();
  }

  /**
   *Constructs a DDMObjectDataStream object.
   *@param data the data with which to populate the object.
  **/
  DDMObjectDataStream(byte[] data)
  {
    super(data);
  }

  /**
   *Constructs a DDMObjectDataStream object.
   *@param length the length of the data stream.
  **/
  DDMObjectDataStream(int length)
  {
    super(length);
    // Initialize the header:
    //  Don't continue on error, not chained, GDS id = D0, type = RQSDSS,
    //  no same request correlation.
    setContinueOnError(false);
    setIsChained(false);
    setGDSId((byte)0xD0);
    setHasSameRequestCorrelation(false);
    setType(3);
  }

  /**
   *Returns a new instance of a DDMObjectDataStream.
   *used by the DDMDataStream.construct() method.
   *@return a new instance of a DDMObjectDataStream.
  **/
  Object getNewDataStream()
  {
    return new DDMObjectDataStream();
  }

  /**
   *Returns a unique identifier for this type of object.
   *@return a unique identifier for this type of object.
  **/
  public int hashCode()
  {
    return 3;  // Object
  }

  /**
   *Returns the S38BUF object data stream.
   *@param records the data in the buffer.
   *@param recordIncrement the bytes between the start of each record.
   *@param maxNumberOfFieldsPerFormatInFile indicates the largest of the  @A1A
   * record.getNumberOfFields() values for this file.  A physical file 
   * has only one record format so maxNumberOfFieldsPerFormatInFile would
   * be the same as getNumberOfFields().  However, a multi-format logical
   * file may have multiple record formats.  maxNumberOfFieldsPerFormatInFile
   * should be set to the largest getNumberOfFields() value for the various
   * record formats in a multi-format logical file.  In some cases, 
   * getObjectS38BUF() may be called before the recordFormat's are known. In
   * that case, maxNumberOfFieldsPerFormatInFile may be set to -1 to obtain
   * equivalent behavior prior to the addition of this new parameter.
   *@return S38BUF data stream
   * Term = S38BUF
   * Size = 6 --> Header (0-5)
   *        2 --> LL S38BUF term and parm (6,7)
   *        2 --> CP S38BUF code point (8,9)
   *@exception CharConversionException If an error occurs during conversion.
   *@exception UnsupportedEncodingException If an error occurs during conversion.
   *
   * #SSPDDM1 - Changed method to accept isSSPFile parameter and skip null field
   *            map if it is set to true
  **/
  static DDMObjectDataStream[] getObjectS38BUF(Record[] records,
                                               DDMS38OpenFeedback openFeedback,
                                               boolean isSSPFile,         // #SSPDDM1
                                               int maxNumberOfFieldsPerFormatInFile) //@A1C
  throws CharConversionException,
  UnsupportedEncodingException
  {
    // Get the record format, the record increment, and the blocking
    // factor.
    RecordFormat format = records[0].getRecordFormat();
    int recordIncrement = openFeedback.getRecordIncrement();
    int blockingFactor = openFeedback.getMaxNumberOfRecordsTransferred();

    // Instantiate an array of data streams.
    DDMObjectDataStream[] dataStreams =
    new DDMObjectDataStream[records.length / blockingFactor +
                            (records.length % blockingFactor == 0 ? 0 : 1)];

    // Create a data stream every 'blockingFactor' records.
    for (int dataStreamIndex = 0, recordIndex = 0;
        dataStreamIndex < dataStreams.length; dataStreamIndex++)
    {
      // We can only copy 'blockingFactor' records per data stream.
      // Calculate the end index of the last record to be copied to the
      // current data stream.
      int endIndex = recordIndex + blockingFactor;
      if (endIndex > records.length)
      {
        endIndex = records.length;
      }

      // Compute the total data stream length, the record data offset, and
      // the S38BUF LL.  If the record data length is greater than the
      // maximum data stream length, we must account for extra bookkeeping
      // data.
      int fourByteLength = 0;
      int recDataLength =  recordIncrement * (endIndex - recordIndex);
      int recordOffset = 10;
      int dsLength = recDataLength + 10;
      int s38BUFLL = recDataLength + 4;
      if (recDataLength > MAX_DATA_STREAM_LEN - 10)
      {
        if (recDataLength > MAX_DATA_STREAM_LEN - 4)
        {
          // We need the four byte length indicator.
          fourByteLength = recDataLength;
          recDataLength -= 4;
          dsLength += 4; // 4 byte length indicator
          s38BUFLL = 0x8008;
          recordOffset = 14;
        }

        // Subtract from record data length the amount before the packets.
        recDataLength -= (MAX_DATA_STREAM_LEN - 10);
      }

      // Instantiate the data stream.
      dataStreams[dataStreamIndex] = new DDMObjectDataStream(dsLength);

      // Set the S38BUF LL, and CP.
      dataStreams[dataStreamIndex].set16bit(s38BUFLL, 6);
      dataStreams[dataStreamIndex].set16bit(DDMTerm.S38BUF, 8);

      // Set the four byte length indicator if needed.
      if (fourByteLength != 0)
      {
        dataStreams[dataStreamIndex].set32bit(fourByteLength, 10);
      }

      // For each record, write the record data and the null field byte
      // map after the record data.
      for (; recordIndex < endIndex; recordIndex++,
          recordOffset += recordIncrement)
      {
        // Copy the record data to the data stream.
        byte[] recordData = records[recordIndex].getContents();
        System.arraycopy(recordData, 0, dataStreams[dataStreamIndex].data_,
                         recordOffset, recordData.length);

        // Write the null field byte map array after the record data.  It
        // immediately preceeds the next record. 0xf1 = null, 0xf0 != null
        // There may be a gap between the end of the record data and the
        // start of the null field byte map.
        int numFields = records[recordIndex].getNumberOfFields();
        // Skip writing the null field map for SSP files					    	// #SSPDDM1
        if (!isSSPFile)                                 // #SSPDDM1
        {
          // The NULL byte field map is left justified and has as many bytes as there @A1A 
          // are fields in the file.  In the case of a multi-format logical file, some
          // formats may have more/less getNumberOfFields() than others.  In that case,
          // the NULL byte field map has entries for the format with the largest 
          // getNumberOfFields().
          // The parameter maxNumberOfFieldsPerFormatInFile is the largest of the 
          // record format's getNumberOfFields() values.
          // Therefore, if we are writing a    
          // record to a format that has 2 fields we need to set two entries into the NULL
          // byte field map.  However, since the file could be a multi-format logical file
          // there may be other recordFormats in the file that have more fields than the one
          // currently being written to.  So, for example if maxNumberOfFieldsPerFormatInFile
          // is 3 then we need to start writing the 0xf1 or 0xf0 values at an offset as if 
          // there were 3 fields, but only need to set 2 values (if there are 2 fields in 
          // this particular record's recordFormat)
          // Previously, the code was setting the 0xf0 and 0xf1 values in the NULL byte 
          // field map right-justified because we did not know/care about  
          // maxNumberOfFieldsPerFormatInFile. This would work for physical files, single 
          // format logical files, and multi-format logical files only if each of the formats 
          // in the multi-format logical file had the exact same number of fields.
          // #SSPDDM1
          // If maxNumberOfFieldsPerFormatInFile
          if (maxNumberOfFieldsPerFormatInFile == -1)                                      //@A1A
          {
            maxNumberOfFieldsPerFormatInFile = numFields;                                  //@A1A
          }
          for (int f = 0, fieldOffset = recordOffset +                                     //@A1C
               (recordIncrement - maxNumberOfFieldsPerFormatInFile); f < numFields; fieldOffset++, f++)
          {
            dataStreams[dataStreamIndex].data_[fieldOffset] =
            (records[recordIndex].isNullField(f) ? (byte) 0xf1 : (byte) 0xf0);
          }
        }                                        // #SSPDDM1
      }
    }

    return dataStreams;
  }

/* COMMENT OUT UNUSED METHOD                                                                @A1D
  // #SSPDDM1 - method changed to all overloaded method with isSSP parameter
  static DDMObjectDataStream[] getObjectS38BUF(Record[] records,
                                               DDMS38OpenFeedback openFeedback)
  throws CharConversionException,
  UnsupportedEncodingException
  {
    return getObjectS38BUF(records, openFeedback, false);  // #SSPDDM1 - Call with isSSP default to false
  }    
*/
}
