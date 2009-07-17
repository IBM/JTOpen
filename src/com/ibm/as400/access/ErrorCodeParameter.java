///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ErrorCodeParameter.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2009-2009 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.beans.PropertyVetoException;

/**
 Represents an IBM i "error code parameter".
 <p>
 An API error code parameter is a variable-length structure that is common to most of the system APIs. The error code parameter controls how errors are returned to the application.
 The error code parameter must be initialized before the application calls the API. Depending on how the error code structure is set, this parameter either returns information associated with an error condition, or causes the Toolbox to return the errors as AS400Message objects.
 For some APIs, the error code parameter is optional. If you do not code the optional error code parameter, the API returns both diagnostic and escape messages. If you do code the optional error code parameter, the API returns only escape messages or error codes, and never returns diagnostic messages.
 @see ProgramCall
 @see ServiceProgramCall
 @see AS400Message
 **/
public class ErrorCodeParameter extends ProgramParameter implements Serializable
{
  static final long serialVersionUID = 4L;

  private static final boolean DEBUG = false;

  private static final boolean FORMAT_ERRC0100 = true;
  private static final boolean FORMAT_ERRC0200 = false;
  private final static int CCSID_EBCDIC = 37;  // the CCSID for standard EBCDIC

  private CharConverter charConverter_;
  private boolean format_;
  private boolean returnSubstitutionData_;
  private boolean usedMinimalConstructor_;


  /**
   Constructs a simple default ErrorCodeParameter object.
   The error information resulting from the API execution will be returned as AS400Message objects, which can be accessed via {@link ProgramCall#getMessageList ProgramCall.getMessageList()} or {@link ServiceProgramCall#getMessageList ServiceProgramCall.getMessageList()}.
   <br>Note: This is the constructor that is recommended for the vast majority of applications.
   @see AS400Message
   **/
  public ErrorCodeParameter()
  {
    // Set the "bytes provided" input parameter to 0, which indicates:
    // "If an error occurs, an exception is returned to the application to indicate that the requested function failed."
    super(BinaryConverter.intToByteArray(0), 8);  // minimum output length is 8

    format_ = FORMAT_ERRC0100;
    returnSubstitutionData_ = false;
    usedMinimalConstructor_ = true;
  }


  /**
   Constructs an ErrorCodeParameter object.
   The error information resulting from the API execution will be returned as output data in the error code parameter, rather than as AS400Message objects.
   To retrieve the error information, call {@link #getMessageID getMessageID()} and {@link #getSubstitutionData getSubstitutionData()}.
   <p>Usage note: This constructor creates error code parameters which cause the run() methods of ProgramCall and ServiceProgramCall to return true even if the called program failed. With this constructor, in order to detect program failure the application must examine the error information returned in the ErrorCodeParameter.
   @param returnSubstitutionData Whether the error information returned from the system is to include error message substitution data.
   The default is false; that is, error message substitution data is not returned.
   @param useCCHAR Whether convertible character (CCHAR) support is to be used.
   The default is false; that is, CCHAR support is not used.
   @see ProgramCall#run()
   @see ServiceProgramCall#run()
   **/
  public ErrorCodeParameter(boolean returnSubstitutionData, boolean useCCHAR)
  {
    returnSubstitutionData_ = returnSubstitutionData;
    usedMinimalConstructor_ = false;

    if (useCCHAR) format_ = FORMAT_ERRC0200;
    else          format_ = FORMAT_ERRC0100;

    try
    {
      int offsetOfBytesProvidedField;  // offset of the "bytes provided" field
      byte[] inputData;
      int outputDataLength;
      // Note: Even though some fields are specified as INPUT, _all_ of the fields are actually returned in the output.  Therefore we must include the sizes of the INPUT fields in the parameter's "output data length".

      if (format_ == FORMAT_ERRC0100)  // 4 bytes of input, 12(+) bytes of output.
      {
        // Error Code format ERRC0100:
        //
        // Offset    | Use     | Type     | Field
        //
        // Dec  Hex
        // 0    0    INPUT     BINARY(4)  Bytes provided
        // 4    4    OUTPUT    BINARY(4)  Bytes available
        // 8    8    OUTPUT    CHAR(7)    Exception ID
        // 15   F    OUTPUT    CHAR(1)    Reserved
        // 16  10    OUTPUT    CHAR(*)    Exception data

        outputDataLength = 16;      // total length of fixed-length fields
        offsetOfBytesProvidedField = 0;  // "bytes provided" is 1st input field
        inputData = new byte[4];
      }

      else // format_ == FORMAT_ERRC0200: 8 bytes of input, 24(+) bytes of output.
      {
        // Error Code format ERRC0200:
        //
        // Offset    | Use     | Type     | Field
        //
        // Dec  Hex
        // 0    0    INPUT     BINARY(4)  Key  ( must be -1 )
        // 4    4    INPUT     BINARY(4)  Bytes provided
        // 8    8    OUTPUT    BINARY(4)  Bytes available
        // 12   C    OUTPUT    CHAR(7)    Exception ID
        // 19  13    OUTPUT    CHAR(1)    Reserved
        // 20  14    OUTPUT    BINARY(4)  CCSID of the CCHAR data
        // 24  18    OUTPUT    BINARY(4)  Offset to the exception data
        // 28  1C    OUTPUT    BINARY(4)  Length of the exception data
        // 32  20    OUTPUT    CHAR(*)    Exception data

        outputDataLength = 32;      // total length of fixed-length fields
        offsetOfBytesProvidedField = 4;  // "bytes provided" is 2nd input field

        // Note: Setting the first input field ("key") to -1 indicates that CCHAR support is used.
        inputData = new byte[8];  // two BINARY(4) input fields
        BinaryConverter.intToByteArray(-1, inputData, 0);  // key == -1
      }

      if (returnSubstitutionData) {
        // Add 100 extra output bytes, to accommodate returned "exception data".
        outputDataLength += 100;
      }

      // Set the "bytes provided" input field, to the output data length.
      BinaryConverter.intToByteArray(outputDataLength, inputData, offsetOfBytesProvidedField);

      // Set the input data for this ProgramParameter object.
      setInputData(inputData);

      // Set the output data length for this ProgramParameter object.
      setOutputDataLength(outputDataLength);
    }
    catch (PropertyVetoException e)  // will never happen
    {
      Trace.log(Trace.ERROR, e);
      throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION, e.getMessage());
    }
  }


  /**
   Returns the CCSID of the error message substitution data.
   0 is returned if no data CCSID has been returned from the system.
   @return  The CCSID of the substitution data.
   Note that special CCSID value 65535 indicates "no character conversion is to be performed".
   @see #getSubstitutionData
   **/
  int getDataCCSID()
  {
    // Design note: Since getSubstitutionData() pre-converts the returned substitution text to Unicode, we do not surface this method, since it might confuse the user.
    if (format_ != FORMAT_ERRC0200) return 0;

    final byte[] outputData = getOutputData();
    if (outputData == null) return 0;
    final int offsetToCCSID = 20;

    if (outputData.length < (offsetToCCSID+4))
    {
      Trace.log(Trace.ERROR, "Output data buffer is too short: " + outputData.length + " bytes. Minimum size required: " + (offsetToCCSID+4));
      throw new InternalErrorException(InternalErrorException.UNKNOWN, "Output buffer too short.");
    }

    return BinaryConverter.byteArrayToInt(outputData, offsetToCCSID);
  }


  /**
   Returns the error message ID that was returned in the error code parameter.
   For example: "CPF7B03".
   Null is returned if no message ID has been returned from the system.
   @return  The message ID.
   **/
  public String getMessageID()
  {
    if (usedMinimalConstructor_) return null;
    final byte[] outputData = getOutputData();
    if (outputData == null) return null;

    final int offsetToID = (format_ == FORMAT_ERRC0100 ? 8 : 12);

    if (outputData.length < (offsetToID+7))
    {
      Trace.log(Trace.ERROR, "Output data buffer is too short: " + outputData.length + " bytes. Minimum size required: " + (offsetToID+7));
      throw new InternalErrorException(InternalErrorException.UNKNOWN, "Output buffer too short.");
    }

    String exceptionID = getCharConverter().byteArrayToString(outputData, offsetToID, 7).trim();
    if (exceptionID.length() == 0) {
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Blank message ID returned.");
      return null;
    }
    else return exceptionID;
  }


  /**
   Returns the error message substitution data that was returned in the error code parameter.
   Null is returned if no error information has been returned from the system.
   To insert the data into the message, call {@link MessageFile#getMessage(String,String) MessageFile.getMessage(String,String)}.
   @return  The substitution data.
   **/
  public String getSubstitutionData()
  {
    if (!returnSubstitutionData_) return null;

    final byte[] outputData = getOutputData();
    if (outputData == null) return null;

    final int offsetToBytesAvail = (format_ == FORMAT_ERRC0100 ?  4 :  8);
    final int offsetToData =       (format_ == FORMAT_ERRC0100 ? 16 : 32);

    if (outputData.length < (offsetToData+1))
    {
      Trace.log(Trace.ERROR, "Output data buffer is too short: " + outputData.length + " bytes. Minimum size required: " + (offsetToData+1));
      throw new InternalErrorException(InternalErrorException.UNKNOWN, "Output buffer too short.");
    }

    final int bytesAvailable = BinaryConverter.byteArrayToInt(outputData, offsetToBytesAvail);

    int lengthOfExceptionData;
    if (format_ == FORMAT_ERRC0100) {
      lengthOfExceptionData = bytesAvailable - 16;
      // Omit the length of the fixed-length fields (16 bytes).
    }
    else {
      // Get the value of the "length of exception data" field.
      lengthOfExceptionData = BinaryConverter.byteArrayToInt(outputData, 28);
    }

    String exceptionData;
    int ccsid = getDataCCSID();
    if (ccsid == 0 || ccsid == 65535) {
      exceptionData = getCharConverter().byteArrayToString(outputData, offsetToData, lengthOfExceptionData);  // use our default converter (EBCDIC)
    }
    else  // Use the CCSID returned in the field "CCSID of the CCHAR data".
    {
      try {
        CharConverter conv = new CharConverter(ccsid);
        exceptionData = conv.byteArrayToString(outputData, offsetToData, lengthOfExceptionData);
      }
      catch (java.io.UnsupportedEncodingException e) {
        Trace.log(Trace.ERROR, "Received UnsupportedEncodingException for CCSID " + ccsid, e);
        throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION, e.getMessage());
      }
    }

    return exceptionData;
  }


  private CharConverter getCharConverter()
  {
    try
    {
      if (charConverter_ == null) charConverter_ = new CharConverter(CCSID_EBCDIC);
      return charConverter_;
    }
    catch (java.io.UnsupportedEncodingException e)  // will never happen
    {
      Trace.log(Trace.ERROR, "Received UnsupportedEncodingException for CCSID " + CCSID_EBCDIC, e);
      throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION, e.getMessage());
    }
  }


  // Deserialize and initialize transient data.
  private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException
  {
    if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "De-serializing ErrorCodeParameter object.");
    in.defaultReadObject();
  }

}
