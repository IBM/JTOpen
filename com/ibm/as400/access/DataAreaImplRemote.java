///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: DataAreaImplRemote.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2007 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
// @A2 - 07/27/2007 - Allow users to write data containing single quote 
//                    characters.  User needs to specify two single quote 
//                    characters to be interpretted as a single character.
//                    This would have resulted in potential length errors
//                    being reported by toolbox code.  Therefore, some toolbox
//                    length verification has been removed.  The IBM i API's
//                    will report an error if the data length is invalid.
///////////////////////////////////////////////////////////////////////////////
package com.ibm.as400.access;

import java.io.CharConversionException;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.UnknownHostException;

/**
 Provides the remote implementations for the public Data Area classes:
 <ul compact>
 <li>DataArea (abstract base class)
 <li>CharacterDataArea
 <li>DecimalDataArea
 <li>LocalDataArea
 <li>LogicalDataArea
 </ul>
 **/
class DataAreaImplRemote implements DataAreaImpl
{
    private AS400ImplRemote system_;  // The system where the data area is located.
    private String library_;  // The library that contains the data area.
    private String name_;  // The name of the data area.
    private QSYSObjectPathName ifsPathName_;  // The full path name of the data area.

    private byte[] dataAreaSystemPathName_;  // The name and library of the data area used in program call.
    private ConverterImplRemote converter_;  // The ccsid converter for this system.

    private int ccsid_;  // The ccsid for this system.
    private RemoteCommandImpl rmtCmd_;  // Impl object for remote command host server.
    private AS400Message[] messageList_;  // The message list for the command object.

    private int length_;  // The maximum number of bytes the data area can contain.

    private boolean attributesRetrieved_;  // Flag indicating if this data area object contains current information regarding its corresponding IBM i data area.
    private int dataAreaType_ = DataArea.UNINITIALIZED;  // Type of data area object.
    private static final QSYSObjectPathName PROGRAM_NAME = new QSYSObjectPathName("/QSYS.LIB/QWCRDTAA.PGM");
    private static final int RETURNED_DATA_FIXED_HEADER_LENGTH = 36; // length of fixed part of returned data from QWCRDTAA
    private static final boolean COMMAND_CALL = true;
    private static final boolean PROGRAM_CALL = false;

    // For DecimalDataArea only:
    private int decimalPositions_ = 5;  // The default number of decimal positions.

    /**
     Resets the character data area to contain default values (blank, zero, or false).
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ConnectionDroppedException  If the connection is dropped unexpectedly.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the system object does not exist.
     @exception  ServerStartupException  If the host server cannot be started.
     @exception  UnknownHostException  If the system cannot be located.
     **/
    public void clear() throws AS400SecurityException, ConnectionDroppedException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException, ServerStartupException, UnknownHostException
    {
        // Build the string for the write.
        // In the case of Character Data Area and Local Data Area,
        // if a substring starting position and length aren't specified,
        // the IBM i API assumes '*ALL' for the starting position and hence
        // performs an overwrite of the remainder of the data area using
        // all blanks.
        String clrcmd = null;
        switch (dataAreaType_)
        {
            case DataArea.CHARACTER_DATA_AREA:
                clrcmd = "QSYS/CHGDTAARA DTAARA(" + library_ + "/" + name_ + ") VALUE(' ')";
                break;
            case DataArea.DECIMAL_DATA_AREA:
                clrcmd = "QSYS/CHGDTAARA DTAARA(" + library_ + "/" + name_ + ") VALUE(0.0)";
                break;
            case DataArea.LOCAL_DATA_AREA:
                clrcmd = "QSYS/CHGDTAARA DTAARA(*LDA) VALUE(' ')";
                break;
            case DataArea.LOGICAL_DATA_AREA:
                clrcmd = "QSYS/CHGDTAARA DTAARA(" + library_ + "/" + name_ + ") VALUE('0')";
                break;
            default:
                Trace.log (Trace.ERROR, "Programming error: clear() was called as dataAreaType=" + dataAreaType_);
                throw new InternalErrorException (InternalErrorException.UNKNOWN, dataAreaType_);
        }

        // Run the command.
        if(!run(clrcmd)) // CHGDTAARA is known to be not threadsafe
        {
            // Throw AS400MessageList.
            processExceptions(getMessages());
        }
    }

    /**
     Creates a character data area with the specified attributes.
     @param  length  The maximum number of bytes in the data area.  Valid values are 1 through 2000.
     @param  initialValue  The initial value for the data area.
     @param  textDescription  The text description for the data area.  The maximum length is 50 characters.
     @param  authority  The public authority level for the data area. Valid values are *ALL, *CHANGE, *EXCLUDE, *LIBCRTAUT, *USE, or the name of an authorization list.  The maximum length is 10 characters.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ConnectionDroppedException  If the connection is dropped unexpectedly.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectAlreadyExistsException  If the system object already exists.
     @exception  ObjectDoesNotExistException  If the system object does not exist.
     @exception  ServerStartupException  If the host server cannot be started.
     @exception  UnknownHostException  If the system cannot be located.
     **/
    public void create(int length, String initialValue, String textDescription, String authority) throws AS400SecurityException, ConnectionDroppedException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectAlreadyExistsException, ObjectDoesNotExistException, ServerStartupException, UnknownHostException
    {
        // Assume the arguments have been validated by the public class.
        if (dataAreaType_ != DataArea.CHARACTER_DATA_AREA)
        {
            Trace.log (Trace.ERROR, "Programming error: create(int,String,String,String) " +
                       "was called when dataAreaType=" + dataAreaType_);
            throw new InternalErrorException (InternalErrorException.UNKNOWN, dataAreaType_);
        }

        length_ = length;

        // Build the string for the create.
        String crtcmd = "QSYS/CRTDTAARA DTAARA(" + library_ + "/" + name_ + ") TYPE(*CHAR) LEN(" + String.valueOf(length_) + " " + ") VALUE('" + initialValue + "') TEXT('" + textDescription + "')" + " AUT(" + authority + ")";

        // Run the command.
        if(!run(crtcmd)) // CRTDTAARA is known to be not threadsafe
        {
            // Throw AS400MessageList.
            processCreateExceptions(getMessages());
        }
        attributesRetrieved_ = true;
    }

    /**
     Creates a decimal data area with the specified attributes.
     @param  length  The maximum number of digits in the data area. Valid values are 1 through 24.
     @param  decimalPositions  The number of digits to the right of the decimal point.  Valid values are 0 through 9.
     @param  initialValue  The initial value for the data area.
     @param  textDescription  The text description for the data area. The maximum length is 50 characters.
     @param  authority  The public authority level for the data area. Valid values are *ALL, *CHANGE, *EXCLUDE, *LIBCRTAUT, *USE, or the name of an authorization list. The maximum length is 10 characters.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ConnectionDroppedException  If the connection is dropped unexpectedly.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectAlreadyExistsException  If the system object already exists.
     @exception  ObjectDoesNotExistException  If the system object does not exist.
     @exception  ServerStartupException  If the host server cannot be started.
     @exception  UnknownHostException  If the system cannot be located.
     **/
    public void create(int length, int decimalPositions, BigDecimal initialValue, String textDescription, String authority) throws AS400SecurityException, ConnectionDroppedException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectAlreadyExistsException, ObjectDoesNotExistException, ServerStartupException, UnknownHostException
    {
        // Assume the arguments have been validated by the public class.
        if (dataAreaType_ != DataArea.DECIMAL_DATA_AREA)
        {
            Trace.log(Trace.ERROR, "Programming error: create(int,int,BigDecimal,String,String) was called when dataAreaType=" + dataAreaType_);
            throw new InternalErrorException (InternalErrorException.UNKNOWN, dataAreaType_);
        }

        length_ = length;
        decimalPositions_ = decimalPositions;

        // Build the string for the create.
        String crtcmd = "QSYS/CRTDTAARA DTAARA(" + library_ + "/" + name_ + ") TYPE(*DEC) LEN(" + String.valueOf(length_) + " " + String.valueOf(decimalPositions_) + ") VALUE(" + initialValue.toString() + ") TEXT('" + textDescription + "')" + " AUT(" + authority + ")";

        // Run the command.
        if(!run(crtcmd)) // CRTDTAARA is known to be not threadsafe
        {
            // Throw AS400MessageList.
            processCreateExceptions(getMessages());
        }

        attributesRetrieved_ = true;
    }

    /**
     Creates a logical data area with the specified attributes.
     @param  initialValue  The initial value for the data area.
     @param  textDescription  The text description for the data area. The maximum length is 50 characters.
     @param  authority  The public authority level for the data area. Valid values are *ALL, *CHANGE, *EXCLUDE, *LIBCRTAUT, *USE, or the name of an authorization list. The maximum length is 10 characters.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ConnectionDroppedException  If the connection is dropped unexpectedly.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectAlreadyExistsException  If the system object already exists.
     @exception  ObjectDoesNotExistException  If the system object does not exist.
     @exception  ServerStartupException  If the host server cannot be started.
     @exception  UnknownHostException  If the system cannot be located.
     **/
    public void create(boolean initialValue, String textDescription, String authority) throws AS400SecurityException, ConnectionDroppedException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectAlreadyExistsException, ObjectDoesNotExistException, ServerStartupException, UnknownHostException
    {
        // Assume the arguments have been validated by the public class.
        if (dataAreaType_ != DataArea.LOGICAL_DATA_AREA)
        {
            Trace.log (Trace.ERROR, "Programming error: create(boolean,String,String) was called when dataAreaType=" + dataAreaType_);
            throw new InternalErrorException (InternalErrorException.UNKNOWN, dataAreaType_);
        }

        // Build the string for the create.
        String crtcmd = "QSYS/CRTDTAARA DTAARA(" + library_ + "/" + name_ + ") TYPE(*LGL) LEN(1) VALUE('" + (initialValue ? "1" : "0") + "') TEXT('" + textDescription + "')" + " AUT(" + authority + ")";

        // Run the command.
        if(!run(crtcmd)) // CRTDTAARA is known to be not threadsafe
        {
            // Throw AS400MessageList.
            processCreateExceptions(getMessages());
        }

        attributesRetrieved_ = true;
    }

    /**
     Removes the data area from the system.  Note this method is NOT public.  It is overridden as a public method in the subclasses that use it.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ConnectionDroppedException  If the connection is dropped unexpectedly.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist.
     @exception  ServerStartupException  If the host server cannot be started.
     @exception  UnknownHostException  If the system cannot be located.
     **/
    public void delete() throws AS400SecurityException, ConnectionDroppedException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException, ServerStartupException, UnknownHostException
    {
        if (dataAreaType_ == DataArea.LOCAL_DATA_AREA)
        {
            Trace.log(Trace.ERROR, "Programming error: delete() was called when dataAreaType=" + dataAreaType_);
            throw new InternalErrorException(InternalErrorException.UNKNOWN, dataAreaType_);
        }

        // Do the delete.
        String dltcmd = "QSYS/DLTDTAARA DTAARA(" + library_ + "/" + name_ + ")";

        // Run the command.
        if(!run(dltcmd)) // DLTDTAARA is known to be threadsafe
        {
            // Throw AS400MessageList.
            processExceptions(getMessages());
            return;
        }

        attributesRetrieved_ = false;
    }

    /**
     Returns the number of digits to the right of the decimal point in this decimal data area.
     @return  The number of decimal positions.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IllegalObjectTypeException  If the system object is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the system object does not exist.
     **/
    public int getDecimalPositions() throws AS400SecurityException, ErrorCompletingRequestException, IllegalObjectTypeException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (dataAreaType_ != DataArea.DECIMAL_DATA_AREA)
        {
            Trace.log(Trace.ERROR, "Programming error: create(int,String,String,String) was called when dataAreaType=" + dataAreaType_);
            throw new InternalErrorException (InternalErrorException.UNKNOWN, dataAreaType_);
        }

        if (!attributesRetrieved_)
            retrieveAttributes();

        return decimalPositions_;
    }

    /**
     Returns the size of the data area.
     @return  The size of the data area, in bytes.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IllegalObjectTypeException  If the system object is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the system object does not exist.
     **/
    public int getLength() throws AS400SecurityException, ErrorCompletingRequestException, IllegalObjectTypeException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!attributesRetrieved_)
            retrieveAttributes();

        return length_;
    }

    /**
     Returns the messages from the previously run command.
     @return  The list of messages.
     **/
    private AS400Message[] getMessages()
    {
        return messageList_;
    }

    /**
     Throws the appropriate data area exception for each particular AS400Message.
     @param  messageList  The array of AS400Message objects.
     **/
    void processCreateExceptions(AS400Message[] messageList) throws AS400SecurityException, ObjectAlreadyExistsException, ObjectDoesNotExistException, AS400Exception
    {
        if (messageList == null)
            return;

        for (int msg = 0; msg < messageList.length; msg++)
        {
            Trace.log(Trace.ERROR, messageList[msg].toString());
            String xid = messageList[msg].getID();
            if (xid.equals("CPF1023"))
            {
                Trace.log(Trace.ERROR, "Re-throwing as ObjectAlreadyExistsException.");
                throw new ObjectAlreadyExistsException(ObjectAlreadyExistsException.OBJECT_ALREADY_EXISTS);
            }
            AS400Message[] ml = { messageList[msg] };
            processExceptions(ml);
        }
    }

    /**
     Throws the appropriate data area exception for each particular AS400Message.
     @param  messageList  The array of AS400Message objects.
     **/
    static void processExceptions(AS400Message[] messageList) throws AS400SecurityException, ObjectDoesNotExistException, AS400Exception
    {
        if (messageList == null)
            return;

        for (int msg = 0; msg < messageList.length; ++msg)
        {
            Trace.log(Trace.ERROR, messageList[msg].toString());
            String xid = messageList[msg].getID();

            if (xid.equals("CPF1015"))
            {
                Trace.log(Trace.ERROR, "Re-throwing as ObjectDoesNotExistException.");
                throw new ObjectDoesNotExistException(ObjectDoesNotExistException.OBJECT_DOES_NOT_EXIST);
            }
            if (xid.equals("CPF1016"))
            {
                Trace.log(Trace.ERROR, "Re-throwing as AS400SecurityException.");
                throw new AS400SecurityException(AS400SecurityException.OBJECT_AUTHORITY_INSUFFICIENT);
            }
            if (xid.equals("CPF1018"))
            {
                Trace.log(Trace.ERROR, "Re-throwing as AS400SecurityException.");
                throw new AS400SecurityException(AS400SecurityException.OBJECT_AUTHORITY_INSUFFICIENT);
            }
            if (xid.equals("CPF1021"))
            {
                Trace.log(Trace.ERROR, "Re-throwing as ObjectDoesNotExistException.");
                throw new ObjectDoesNotExistException(ObjectDoesNotExistException.LIBRARY_DOES_NOT_EXIST);
            }
            if (xid.equals("CPF1022"))
            {
                Trace.log(Trace.ERROR, "Re-throwing as AS400SecurityException.");
                throw new AS400SecurityException(AS400SecurityException.LIBRARY_AUTHORITY_INSUFFICIENT);
            }
            if (xid.equals("CPF2105"))
            {
                Trace.log(Trace.ERROR, "Re-throwing as ObjectDoesNotExistException.");
                throw new ObjectDoesNotExistException(ObjectDoesNotExistException.OBJECT_DOES_NOT_EXIST);
            }
            if (xid.equals("CPF2182"))
            {
                Trace.log(Trace.ERROR, "Re-throwing as AS400SecurityException.");
                throw new AS400SecurityException(AS400SecurityException.LIBRARY_AUTHORITY_INSUFFICIENT);
            }
            if (xid.equals("CPF2189"))
            {
                Trace.log(Trace.ERROR, "Re-throwing as AS400SecurityException.");
                throw new AS400SecurityException(AS400SecurityException.OBJECT_AUTHORITY_INSUFFICIENT);
            }

            throw new AS400Exception(messageList[msg]);
        }
    }

    /**
     Returns the data read from the decimal data area.
     @return  The decimal data read from the data area.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IllegalObjectTypeException  If the system object is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the system object does not exist.
     **/
    // Note that doing a read() will also set the attributes of this
    // object to what is returned from the system, namely the length and
    // number of decimal positions.
    public BigDecimal readBigDecimal() throws AS400SecurityException, ErrorCompletingRequestException, IllegalObjectTypeException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (dataAreaType_ != DataArea.DECIMAL_DATA_AREA)
        {
            Trace.log (Trace.ERROR, "Programming error: readBigDecimal() was called when dataAreaType=" + dataAreaType_);
            throw new InternalErrorException (InternalErrorException.UNKNOWN, dataAreaType_);
        }

        // Do the read.
        BigDecimal val = (BigDecimal)retrieveAttributes();

        return val;
    }

    /**
     Returns the value in the logical data area.
     @return  The data read from the data area.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IllegalObjectTypeException  If the system object is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the system object does not exist.
     **/
    public boolean readBoolean() throws AS400SecurityException, ErrorCompletingRequestException, IllegalObjectTypeException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (dataAreaType_ != DataArea.LOGICAL_DATA_AREA)
        {
            Trace.log(Trace.ERROR, "Programming error: readBoolean() was called when dataAreaType=" + dataAreaType_);
            throw new InternalErrorException (InternalErrorException.UNKNOWN, dataAreaType_);
        }

        // Do the read

        // We expect to retrieve the fixed header, plus 1 byte.
        int lengthOfReceiverVariable = RETURNED_DATA_FIXED_HEADER_LENGTH + 1;

        // Call the "Retrieve Data Area" API.
        byte[] dataReceived = retrieveDataAreaContents(lengthOfReceiverVariable, dataAreaSystemPathName_, -1, 1);

        // Format of data returned:
        // Offset - Type - Field
        // 0 - Bin 4 - Bytes available
        // 4 - Bin 4 - Bytes returned
        // 8 - Char 10 - Type of value returned
        // 18 - Char 10 - Library name
        // 28 - Bin 4 - Length of value returned
        // 32 - Bin 4 - Number of decimal positions
        // 36 - Char * - Value

        // Check the type
        String type = converter_.byteArrayToString(dataReceived, 8, 10).toUpperCase().trim(); //@A1C
        if (!type.equals("*LGL"))
        {
            if (type.equals("*CHAR"))
            {
                Trace.log(Trace.ERROR, "Illegal data area type for logical data area object: "+type);
                throw new IllegalObjectTypeException(IllegalObjectTypeException.DATA_AREA_CHARACTER);
            }
            else if (type.equals("*DEC"))
            {
                Trace.log(Trace.ERROR, "Illegal data area type for logical data area object: "+type);
                throw new IllegalObjectTypeException(IllegalObjectTypeException.DATA_AREA_DECIMAL);
            }
            else
            {
                Trace.log(Trace.ERROR, "Unknown data area type for logical data area object: "+type);
                throw new IOException(ResourceBundleLoader.getText("EXC_OBJECT_TYPE_UNKNOWN"));
            }
        }

        // Set the attributes
        length_ = BinaryConverter.byteArrayToInt(dataReceived, 28);
        if (length_ != 1)
        {
            Trace.log(Trace.ERROR, "Logical data area length not valid: "+length_);
            throw new IOException(ResourceBundleLoader.getText("EXC_LENGTH_NOT_VALID"));
        }

        // Get the data
        String val = converter_.byteArrayToString(dataReceived, 36, 1);
        boolean data = false;
        if (val.equals("1"))
        {
            data = true;
        }
        else if (!val.equals("0"))
        {
            Trace.log(Trace.ERROR, "Data received not valid: "+val);
            throw new IOException(ResourceBundleLoader.getText("EXC_DATA_NOT_VALID"));
        }

        return data;
    }


   /**
    Reads raw bytes from the data area.
    It retrieves <i>dataLength</i> bytes beginning at
    <i>dataAreaOffset</i> in the data area. The first byte in
    the data area is at offset 0.
    @param data The data to be written.
    @param dataBufferOffset The starting offset in <tt>data</tt>.
    @param dataAreaOffset The offset in the data area at which to start reading. (0-based)
    @param dataLength The number of bytes to read. Valid values are from
    1 through (data area size - <i>dataAreaOffset</i>).
    @return The total number of bytes read into the buffer.
    @exception AS400SecurityException          If a security or authority error occurs.
    @exception ErrorCompletingRequestException If an error occurs before the request is completed.
    @exception IllegalObjectTypeException      If the system object is not the required type.
    @exception InterruptedException            If this thread is interrupted.
    @exception IOException                     If an error occurs while communicating with the system.
    @exception ObjectDoesNotExistException     If the system object does not exist.
    **/
   public int readBytes(byte[] data, int dataBufferOffset, int dataAreaOffset, int dataLength)
     throws AS400SecurityException,
   ErrorCompletingRequestException,
   IllegalObjectTypeException,
   InterruptedException,
   IOException,
   ObjectDoesNotExistException
   {
     // Do the read

     // We expect to retrieve the fixed header, plus dataLength bytes.
     int lengthOfReceiverVariable = RETURNED_DATA_FIXED_HEADER_LENGTH + dataLength;

     // Call the "Retrieve Data Area" API.
     byte[] dataReceived = retrieveDataAreaContents(lengthOfReceiverVariable, dataAreaSystemPathName_, dataAreaOffset+1, dataLength);

     // Format of data returned:
     // Offset - Type - Field
     // 0 - Bin 4 - Bytes available    The length of all data available to
     //             return. All available data is returned if enough space
     //             is provided.
     // 4 - Bin 4 - Bytes returned    The length of all data actually
     //             returned. If the data truncated because the receiver
     //             variable was not sufficiently large to hold all of the
     //             data available, this value will be less than the bytes
     //             available.
     // 8 - Char 10 - Type of value returned
     // 18 - Char 10 - Library name
     // 28 - Bin 4 - Length of value returned
     // 32 - Bin 4 - Number of decimal positions
     // 36 - Char * - Value

     int numBytesReturned = BinaryConverter.byteArrayToInt(dataReceived, 4);
     numBytesReturned = numBytesReturned - 36; // Disregard the header bytes
     if (numBytesReturned > dataLength)
     {
       Trace.log(Trace.ERROR, "Unexpected number of bytes returned: "+numBytesReturned);
       throw new InternalErrorException (InternalErrorException.UNKNOWN, numBytesReturned);
     }
     else if (numBytesReturned < dataLength)
     {
       Trace.log(Trace.WARNING, "Fewer bytes returned than requested: "+numBytesReturned);
     }

     // The rest of the receiver array is the retrieved data.
     System.arraycopy(dataReceived, 36, data, dataBufferOffset, numBytesReturned);
     return numBytesReturned;
   }


    /**
     Calls the Retrieve Data Area (QWCRDTAA) API to retrieve bytes from specified location in the data area.
     @param lengthOfReceiverVariable The length of the receiver variable (in bytes).
     @param qualifiedDataAreaName Byte array containing library and name of the data area.
     @param startingPosition The position (1-based) in the data area at which to start the retrieve.  A value of 1 specifies the first byte in the data area.  A value of -1 will return all the bytes in the data area.
     @param dataLength The number of bytes to retrieve.
     @return The retrieved bytes.
     **/
    private byte[] retrieveDataAreaContents(int lengthOfReceiverVariable, byte[] qualifiedDataAreaName, int startingPosition, int dataLength) throws AS400SecurityException, ErrorCompletingRequestException, IllegalObjectTypeException, InterruptedException, IOException, ObjectDoesNotExistException
    {
      // Do the read
      // **** Setup the parameter list ****
      // Format for Retrieve Data Area API (QWCRDTAA):
      // 1 - Receiver variable - Output - Char *
      // 2 - Length of receiver variable - Input - Bin 4
      // 3 - Qualified data area name - Input - Char 20
      // 4 - Starting position - Input - Bin 4
      // 5 - Length of data - Input - Bin 4
      // 6 - Error code - I/O - Char *

      ProgramParameter[] parmlist = new ProgramParameter[6];

      // First parameter: output, is the receiver variable
      // Format of data returned:
      // Offset - Type - Field
      // 0 - Bin 4 - Bytes available    The length of all data available to
      //             return. All available data is returned if enough space
      //             is provided.
      // 4 - Bin 4 - Bytes returned    The length of all data actually
      //             returned. If the data truncated because the receiver
      //             variable was not sufficiently large to hold all of the
      //             data available, this value will be less than the bytes
      //             available.
      // 8 - Char 10 - Type of value returned
      // 18 - Char 10 - Library name
      // 28 - Bin 4 - Length of value returned
      // 32 - Bin 4 - Number of decimal positions
      // 36 - Char * - Value
      byte[] dataReceived = new byte[lengthOfReceiverVariable];
      parmlist[0] = new ProgramParameter(dataReceived.length);

      // Second parameter: input, length of receiver variable
      byte[] receiverLength = new byte[4];
      BinaryConverter.intToByteArray(dataReceived.length, receiverLength, 0);
      parmlist[1] = new ProgramParameter(receiverLength);

      // Third parameter: input, qualified data area name
      parmlist[2] = new ProgramParameter(qualifiedDataAreaName);

      // Fourth parameter: input, starting position
      // 1 through 2000; -1 retrieves all
      byte[] startingPos = BinaryConverter.intToByteArray(startingPosition);
      parmlist[3] = new ProgramParameter(startingPos);

      // Fifth parameter: input, length of data
      byte[] lengthOfData = BinaryConverter.intToByteArray(dataLength);
      parmlist[4] = new ProgramParameter(lengthOfData);

      // Sixth parameter: input/output, error code
      // Format ERRC0100: (could also use ERRC0200 instead?)
      // Offset - Use - Type - Field
      // 0 - Input - Bin 4 - Bytes provided
      // 4 - Output - Bin 4 - Bytes available
      // 8 - Output - Char 7 - Exception ID
      // 15 - Output - Char 1 - Reserved
      // 16 - Output - Char * - Exception data
      byte[] errorCode = new byte[17];
      // A value >=8 of bytes provided means the system will return exception
      // information on the parameter instead of throwing an exception
      // to the application. We provide 0 to ensure it does indeed
      // throw us an exception.
      BinaryConverter.intToByteArray(0, errorCode, 0);
      parmlist[5] = new ProgramParameter(errorCode, 17);

      // Create the pgm call object
      if (rmtCmd_ == null) {
        setupRemoteCommand();
      }

      // Run the program.  Failure is returned as a message list.
      if(!rmtCmd_.runProgram("QSYS", "QWCRDTAA", parmlist))  // QWCRDTAA isn't threadsafe. $B1C
      {
        // Throw AS400MessageList
        processExceptions(rmtCmd_.getMessageList());
      }

      // Get the data returned from the program
      dataReceived = parmlist[0].getOutputData();

      if (Trace.isTraceOn() && Trace.isTraceDiagnosticOn())
      {
        String areaType = DataArea.dataAreaTypeToString(dataAreaType_);
        Trace.log(Trace.DIAGNOSTIC, areaType + " data area data retrieved:", dataReceived);
      }

      return dataReceived;
    }

    /**
     Refreshes the attributes of the data area.
     This method should be called if the underlying IBM i data area has changed and it is desired that this object should reflect those changes.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IllegalObjectTypeException  If the system object is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the system object does not exist.
     **/
    public void refreshAttributes() throws AS400SecurityException, ErrorCompletingRequestException, IllegalObjectTypeException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        attributesRetrieved_ = false;
        retrieveAttributes();
    }

    //$D2C
    /**
     Makes the API call to retrieve the character data area data and attributes.
     @param offset The offset in the data area at which to start retrieving. (0-based)
     @param length The number of bytes to read.
     @return The String value read from the data area as a result of retrieving the data area's attributes.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IllegalObjectTypeException  If the system object is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the system object does not exist.
     **/
    public String retrieve(int offset, int length) throws AS400SecurityException, ErrorCompletingRequestException, IllegalObjectTypeException, InterruptedException, IOException, ObjectDoesNotExistException
    {
       if(AS400BidiTransform.isBidiCcsid(ccsid_))
          return retrieve(offset, length, AS400BidiTransform.getStringType((char)ccsid_));
       else
          return retrieve(offset, length, BidiStringType.DEFAULT);
    }


    //$D2A
    /**
     Makes the API call to retrieve the character data area data and attributes.
     @param offset The offset in the data area at which to start retrieving. (0-based)
     @param length The number of bytes to read.
     @param stringType The Data Area bidi string type, as defined by the CDRA (Character
                 Data Representation Architecture). See <a href="BidiStringType.html">
                 BidiStringType</a> for more information and valid values.
     @return The String value read from the data area as a result of retrieving the data area's attributes.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IllegalObjectTypeException  If the system object is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the system object does not exist.
     **/
    public String retrieve(int offset, int length, int stringType) throws AS400SecurityException, ErrorCompletingRequestException, IllegalObjectTypeException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (dataAreaType_ != DataArea.CHARACTER_DATA_AREA && dataAreaType_ != DataArea.LOCAL_DATA_AREA)
        {
            Trace.log(Trace.ERROR, "Programming error: retrieve(int,int) was called when dataAreaType=" + dataAreaType_);
            throw new InternalErrorException (InternalErrorException.UNKNOWN, dataAreaType_);
        }

        // Do the read

        // We expect to retrieve the fixed header, plus the data bytes.
        int lengthOfReceiverVariable = RETURNED_DATA_FIXED_HEADER_LENGTH + 4001;
        // 36 fixed header bytes, plus a max of 4001 bytes from data area.
        // With a max of 2000 characters, the worst case
        // scenario is that every other character is DBCS
        // (i.e. for a mixed byte Unicode String)
        // so that there would be a SI/SO, like this:
        // SI-1-SO  2  SI-3-SO  4  SI-5-SO  etc...
        // So for 3 initial chars, you get 7 bytes;
        // for 5 initial chars, you get 11 bytes; etc.
        // This equates to:
        // max # bytes needed = (2 * initial_chars) + 1

        // Call the "Retrieve Data Area" API.
        int startingPosition = (offset == -1 ? offset : offset+1);  // -1 means "retrieve all"
        byte[] dataReceived = retrieveDataAreaContents(lengthOfReceiverVariable, dataAreaSystemPathName_, startingPosition, length);

        // Format of data returned:
        // Offset - Type - Field
        // 0 - Bin 4 - Bytes available    The length of all data available to
        //             return. All available data is returned if enough space
        //             is provided.
        // 4 - Bin 4 - Bytes returned    The length of all data actually
        //             returned. If the data truncated because the receiver
        //             variable was not sufficiently large to hold all of the
        //             data available, this value will be less than the bytes
        //             available.
        // 8 - Char 10 - Type of value returned
        // 18 - Char 10 - Library name
        // 28 - Bin 4 - Length of value returned
        // 32 - Bin 4 - Number of decimal positions
        // 36 - Char * - Value

        // Check the type
        String type = converter_.byteArrayToString(dataReceived, 8, 10).toUpperCase().trim(); //@A1C
        if (!type.equals("*CHAR"))
        {
            if (type.equals("*DEC"))
            {
                Trace.log(Trace.ERROR, "Illegal data area type for character data area object: "+type);
                throw new IllegalObjectTypeException(IllegalObjectTypeException.DATA_AREA_DECIMAL);
            }
            else if (type.equals("*LGL"))
            {
                Trace.log(Trace.ERROR, "Illegal data area type for character data area object: "+type);
                throw new IllegalObjectTypeException(IllegalObjectTypeException.DATA_AREA_LOGICAL);
            }
            else
            {
                Trace.log(Trace.ERROR, "Illegal data area type for character data area object: "+type);
                throw new IOException(ResourceBundleLoader.getText("EXC_OBJECT_TYPE_UNKNOWN"));
            }
        }

        // Set the attributes
        if (offset == -1) // (the entire data area was retrieved)
        {
            length_ = BinaryConverter.byteArrayToInt(dataReceived, 28);
        }

        // bytesReturned reflects the number of bytes of data in the data area
        int bytesReturned = BinaryConverter.byteArrayToInt(dataReceived, 4);
        bytesReturned = bytesReturned - 36; // Don't count the first 36 bytes

        // The rest of the receiver array is the character data.

        return converter_.byteArrayToString(dataReceived, 36, bytesReturned, stringType); //@A1C //$D2C
    }

    /**
     Retrieves/refreshes the data area's attributes.
     @return  The data read from the data area as a result of retrieving the data area's attributes.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IllegalObjectTypeException  If the system object is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the system object does not exist.
     **/
    private Object retrieveAttributes() throws AS400SecurityException, ErrorCompletingRequestException, IllegalObjectTypeException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        switch (dataAreaType_)
        {
            case DataArea.CHARACTER_DATA_AREA:
            case DataArea.LOCAL_DATA_AREA:
                return retrieveAttributesString();
            case DataArea.DECIMAL_DATA_AREA:
                return retrieveAttributesBigDecimal();
            case DataArea.LOGICAL_DATA_AREA:
                return retrieveAttributesBoolean();
            default:
                Trace.log (Trace.ERROR, "Programming error: retrieveAttributes() was called as dataAreaType=" + dataAreaType_);
                throw new InternalErrorException (InternalErrorException.UNKNOWN, dataAreaType_);
        }
    }

    /**
     Retrieves/refreshes the logical data area's attributes.
     @return  The BigDecimal value read from the data area as a result of retrieving the data area's attributes.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IllegalObjectTypeException  If the system object is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the system object does not exist.
     **/
    private Object retrieveAttributesBigDecimal() throws AS400SecurityException, ErrorCompletingRequestException, IllegalObjectTypeException, InterruptedException, IOException, ObjectDoesNotExistException
    {
      // For a decimal data area, need to get the length & decimal positions
      // But since this is the only API available to us, we might as well
      // do a read of the data too.

      // Do the read

      // We expect to retrieve the fixed header, plus a max of 24 digits from data area.
      int lengthOfReceiverVariable = RETURNED_DATA_FIXED_HEADER_LENGTH + 24;

      // Call the "Retrieve Data Area" API.
      byte[] dataReceived = retrieveDataAreaContents(lengthOfReceiverVariable, dataAreaSystemPathName_, -1, 24);

      // Check the type
      String type = converter_.byteArrayToString(dataReceived, 8, 10).toUpperCase().trim(); //@A1C
      if (!type.equals("*DEC"))
      {
        if (type.equals("*CHAR"))
        {
          Trace.log(Trace.ERROR, "Illegal data area type for decimal data area object: "+type);
          throw new IllegalObjectTypeException(IllegalObjectTypeException.DATA_AREA_CHARACTER);
        }
        else if (type.equals("*LGL"))
        {
          Trace.log(Trace.ERROR, "Illegal data area type for decimal data area object: "+type);
          throw new IllegalObjectTypeException(IllegalObjectTypeException.DATA_AREA_LOGICAL);
        }
        else
        {
          Trace.log(Trace.ERROR, "Illegal data area type for decimal data area object: "+type);
          throw new IOException(ResourceBundleLoader.getText("EXC_OBJECT_TYPE_UNKNOWN"));
        }
      }

      // Set the attributes
      length_ = BinaryConverter.byteArrayToInt(dataReceived, 28);
      decimalPositions_ = BinaryConverter.byteArrayToInt(dataReceived, 32);

      // The rest of the receiver array is the packed decimal data.
      // Need to convert: packed decimal bytes -> AS400PackedDecimal -> BigDecimal.
      AS400PackedDecimal packedJava = new AS400PackedDecimal(length_, decimalPositions_);
      byte[] packed400 = new byte[24];
      System.arraycopy(dataReceived, 36, packed400, 0, 24);
      BigDecimal val = (BigDecimal)packedJava.toObject(packed400);

      attributesRetrieved_ = true;

      return val;
    }

    /**
     Retrieves/refreshes the data area's attributes.
     @return The boolean[] value read from the data area as a result of retrieving the data area's attributes.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IllegalObjectTypeException  If the system object is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the system object does not exist.
     **/
    private Object retrieveAttributesBoolean() throws AS400SecurityException, ErrorCompletingRequestException, IllegalObjectTypeException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        // Read in the entire data area to get the total length
        boolean[] obj = new boolean[1];
        obj[0] = readBoolean();

        attributesRetrieved_ = true;

        return obj;
    }

    /**
     Retrieves/refreshes the character (or local) data area's attributes.
     @return The String value read from the data area as a result of retrieving the data area's attributes.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IllegalObjectTypeException  If the system object is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the system object does not exist.
     **/
    private Object retrieveAttributesString() throws AS400SecurityException, ErrorCompletingRequestException, IllegalObjectTypeException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        // For a character data area, need to get the length.
        // But since this is the only API available to us, we might as well
        // do a read of the data too.

        // Read in the entire data area to get the total length
        String obj = retrieve(-1, 1);

        attributesRetrieved_ = true;

        return obj;
    }

    /**
     Runs the specified command using CommandCall.
     @return  True if the command ran without errors; otherwise false.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ConnectionDroppedException  If the connection is dropped unexpectedly.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ServerStartupException  If the host server cannot be started.
     @exception  UnknownHostException  If the system cannot be located.
     **/
    private boolean run(String command) throws AS400SecurityException, ConnectionDroppedException, ErrorCompletingRequestException, InterruptedException, IOException, ServerStartupException, UnknownHostException
    {
        boolean result = false;
        if (rmtCmd_ == null) {
          setupRemoteCommand();
        }
        result = rmtCmd_.runCommand(command);      // @B2C
        messageList_ = rmtCmd_.getMessageList();

        return result;
    }

    /**
     Sets the system, path, and data area type.
     Note: This method should be called only once per DataAreaImplRemote object, immediately after object creation.
     **/
    public void setAttributes(AS400Impl system, QSYSObjectPathName path, int dataAreaType) throws IOException
    {
        // Assume the arguments have been validated by the public class.
        setSystem((AS400ImplRemote)system);
        setPath(path);
        setType(dataAreaType);
    }

    /**
     Sets the fully qualified data area name. Note this method is NOT public in the public class.
     It is overridden as a public method in the subclasses that use it.
     **/
    private void setPath(QSYSObjectPathName path) throws CharConversionException, UnsupportedEncodingException
    {
        // Assume the argument has been validated by the public class.

        // Set instance vars
        ifsPathName_ = path;
        library_ = ifsPathName_.getLibraryName();
        name_ = ifsPathName_.getObjectName();

        // Set up the data area system path name buffer.

        if (ccsid_ == 0)
            ccsid_ = system_.getCcsid();
        if (converter_ == null)
            converter_ = ConverterImplRemote.getConverter(ccsid_, system_);
        dataAreaSystemPathName_ = new byte[20];
        for (int i=0; i<20; i++)         // Fill the array with blanks.
            dataAreaSystemPathName_[i] = (byte)0x40;

        converter_.stringToByteArray(name_, dataAreaSystemPathName_, 0, 10);
        converter_.stringToByteArray(library_, dataAreaSystemPathName_, 10, 10);
    }

    /**
     Sets the system on which the data area exists. The system cannot be set if a connection has already been established.
     @param  system  The system on which the data area exists.
     **/
    private void setSystem(AS400ImplRemote system)
    {
        // Assume the argument has been validated by the public class.
        system_ = system;
    }

    /**
     Sets the specific type for the Data Area object.
     @param  data  The type for Data Area object.
     **/
    private void setType(int dataAreaType)
    {
        switch (dataAreaType)
        {
            case DataArea.CHARACTER_DATA_AREA:
                length_ = CharacterDataArea.DEFAULT_LENGTH;
                break;
            case DataArea.DECIMAL_DATA_AREA:
                length_ = DecimalDataArea.DEFAULT_LENGTH;
                break;
            case DataArea.LOCAL_DATA_AREA:
                length_ = LocalDataArea.DEFAULT_LENGTH;
                break;
            case DataArea.LOGICAL_DATA_AREA:
                length_ = LogicalDataArea.DEFAULT_LENGTH;
                break;
            default:
                Trace.log (Trace.ERROR, "Programming error: setType() was called with arg=" + dataAreaType);
                throw new InternalErrorException (InternalErrorException.UNKNOWN, dataAreaType);
        }
        dataAreaType_ = dataAreaType;
    }

    // Setup remote command object on first touch.  Synchronized to protect instance variables.  This method can safely be called multiple times because it checks for a previous call before changing the instance variables.
    protected synchronized void setupRemoteCommand() throws IOException
    {
      // If not already setup.
      if (rmtCmd_ == null)
      {
        if (system_.canUseNativeOptimizations())
        {
          try
          {
            rmtCmd_ = (RemoteCommandImpl)Class.forName("com.ibm.as400.access.RemoteCommandImplNative").newInstance();
            // Avoid direct reference - it can cause NoClassDefFoundError at class loading time on Sun JVM's.
          }
          catch (Throwable e) {
            // A ClassNotFoundException would be unexpected, since canUseNativeOptions() returned true.
            Trace.log(Trace.WARNING, "Unable to instantiate class RemoteCommandImplNative.", e);
          }
        }
        if (rmtCmd_ == null)
        {
          rmtCmd_ = new RemoteCommandImplRemote();
        }
        rmtCmd_.setSystem(system_);
      }
    }


    //$D2C
    /**
     Writes the data to the character (or local) data area.  It writes <i>data.length()</i> characters from <i>data</i> to the data area beginning at <i>dataAreaOffset</i>. The first character in the data area is at offset 0.
     @param  data  The data to be written.
     @param  dataAreaOffset  The offset in the data area at which to start writing.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ConnectionDroppedException  If the connection is dropped unexpectedly.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the system object does not exist.
     @exception  ServerStartupException  If the host server cannot be started.
     @exception  UnknownHostException  If the system cannot be located.
     **/
    public void write(String data, int dataAreaOffset) throws AS400SecurityException, ConnectionDroppedException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException, ServerStartupException, UnknownHostException
    {
       if(AS400BidiTransform.isBidiCcsid(ccsid_))
          write(data, dataAreaOffset, AS400BidiTransform.getStringType((char)ccsid_));
       else
          write(data, dataAreaOffset, BidiStringType.DEFAULT);
    }


    //$D2A
    /**
     Writes the data to the character (or local) data area.  It writes <i>data.length()</i> characters from <i>data</i> to the data area beginning at <i>dataAreaOffset</i>. The first character in the data area is at offset 0.
     @param  data  The data to be written.
     @param  dataAreaOffset  The offset in the data area at which to start writing.
     @param type The Data Area bidi string type, as defined by the CDRA (Character
                 Data Representation Architecture). See <a href="BidiStringType.html">
                 BidiStringType</a> for more information and valid values.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ConnectionDroppedException  If the connection is dropped unexpectedly.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the system object does not exist.
     @exception  ServerStartupException  If the host server cannot be started.
     @exception  UnknownHostException  If the system cannot be located.
     **/
    public void write(String data, int dataAreaOffset, int type) throws AS400SecurityException, ConnectionDroppedException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException, ServerStartupException, UnknownHostException
    {
        // Assume the arguments have been validated by the public class.
        if (dataAreaType_ != DataArea.CHARACTER_DATA_AREA && dataAreaType_ != DataArea.LOCAL_DATA_AREA)
        {
            Trace.log (Trace.ERROR, "Programming error: write(String,int) was called when dataAreaType=" + dataAreaType_);
            throw new InternalErrorException (InternalErrorException.UNKNOWN, dataAreaType_);
        }

        // Build the string for the write.
        //String wrtcmd = null;
        byte[] wrtcmd;

        // convert to get the actual number of bytes being written.  In mixed
        // environments a character can expand to more than one byte.  The
        // number of byte is passed on the command.

        // To allow bidi data to be written to a data area, each
        // part (the beginning of the command, the data, the end of the commmand)
        // must be converted into bytes and passed to command call.
        byte[] part1;                                                                                     //@D2A
        byte[] part2 = converter_.stringToByteArray(data, type);                                          //@D1a  //$D2C
        byte[] part3 = converter_.stringToByteArray("')");                                                        //@D2A
        int dataLength = part2.length;                                                                    //@D2C
        int countSingleQuotePairs = 0;                       //@A2A

        switch (dataAreaType_)
        {
            case DataArea.CHARACTER_DATA_AREA:
                // Start Changes ----------------------------------------- @A2A
                // Search/count the number of single-quote pairs in "data" parm.
                // Single-quote pairs are treated as a single-quote for 
                // the "Substring length" parameter of the CHGDTAARA command.
                // For example, the following is valid:
                // QSYS/CHGDTAARA DTAARA(DPRIGGE/CHAR1 (1 5)) VALUE('AB''''E') 
                // Notice that we need to specify (1 5) rather than (1 7)

                int searchIndex = 0, foundIndex;
                while(searchIndex < data.length())
                {
                  foundIndex = data.indexOf("''", searchIndex);
                  if (foundIndex != -1)
                  {
                    ++countSingleQuotePairs;
                    searchIndex = foundIndex+2; // Skip search past this double-quote pair
                  }
                  else searchIndex = data.length();
                }
                // End Changes ------------------------------------------- @A2A

                //wrtcmd = "QSYS/CHGDTAARA DTAARA(" + library_ + "/" + name_ + " (" + (dataAreaOffset+1) + " " + dataLength + ")" + ") VALUE('" + data + "')"; //@D1c
                part1 = converter_.stringToByteArray("QSYS/CHGDTAARA DTAARA(" + library_ + "/" + name_ + " (" + (dataAreaOffset+1) + " " + (dataLength-countSingleQuotePairs)+ ")" + ") VALUE('");  //@D2C //@A2A
                break;
            case DataArea.LOCAL_DATA_AREA:
                //wrtcmd = "QSYS/CHGDTAARA DTAARA(*LDA (" + (dataAreaOffset+1) + " " + dataLength + ")) VALUE('" + data + "')";                                //@D1c
                part1 = converter_.stringToByteArray("QSYS/CHGDTAARA DTAARA(*LDA (" + (dataAreaOffset+1) + " " + dataLength + ")) VALUE('");                                 //@D2C
                break;
            default:
                Trace.log(Trace.ERROR, "Programming error: write(String,int) was called as dataAreaType=" + dataAreaType_);
                throw new InternalErrorException (InternalErrorException.UNKNOWN, dataAreaType_);
        }

        // Allocate the proper byte array size for the command.
        wrtcmd = new byte[part1.length + dataLength + part3.length];                //@D2A

        // Copy each part of the chgdtaara command bytes into the end byte array.
        System.arraycopy(part1,0,wrtcmd,0,part1.length);                            //@D2A
        System.arraycopy(part2,0,wrtcmd,part1.length,dataLength);                   //@D2A
        System.arraycopy(part3,0,wrtcmd,part1.length+dataLength,part3.length);      //@D2A

        if (rmtCmd_ == null) {
          setupRemoteCommand();
        }

        if (Trace.isTraceOn())                                    //@A2A
        {                                                         //@A2A
            String wrtcmd2 = converter_.byteArrayToString(wrtcmd);        //@A2A
            Trace.log(Trace.DIAGNOSTIC, "wrtcmd2=["+wrtcmd2+"]"); //@A2A
        }                                                         //@A2A
        // Run the command as bytes
        boolean result = rmtCmd_.runCommand(wrtcmd, "QSYS/CHGDTAARA"); // not threadsafe
        messageList_ = rmtCmd_.getMessageList();                //$D2A

        if(!result)                                             //$D2C
        {
            // Throw AS400MessageList
            processExceptions(getMessages());
        }
    }

    /**
     Writes the BigDecimal <i>data</i> value to the decimal data area.
     @param  data  The decimal data to be written.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ConnectionDroppedException  If the connection is dropped unexpectedly.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the system object does not exist.
     @exception  ServerStartupException  If the host server cannot be started.
     @exception  UnknownHostException  If the system cannot be located.
     **/
    public void write(BigDecimal data) throws AS400SecurityException, ConnectionDroppedException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException, ServerStartupException, UnknownHostException
    {
        // Assume the argument has been validated by the public class.
        if (dataAreaType_ != DataArea.DECIMAL_DATA_AREA)
        {
            Trace.log (Trace.ERROR, "Programming error: write(BigDecimal) was called when dataAreaType=" + dataAreaType_);
            throw new InternalErrorException (InternalErrorException.UNKNOWN, dataAreaType_);
        }

        // Build the string for the write.
        String wrtcmd = "QSYS/CHGDTAARA DTAARA(" + library_ + "/" + name_ + ") VALUE(" + data.toString() + ")";

        // Run the command
        if(!run(wrtcmd)) // CHGDTAARA is known to be not threadsafe
        {
            // Throw AS400MessageList
            processExceptions(getMessages());
        }
    }

    /**
     Writes the boolean value in <i>data</i> to the logical data area.
     @param  data  The data to be written.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ConnectionDroppedException  If the connection is dropped unexpectedly.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the system object does not exist.
     @exception  ServerStartupException  If the host server cannot be started.
     @exception  UnknownHostException  If the system cannot be located.
     **/
    public void write(boolean data) throws AS400SecurityException, ConnectionDroppedException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException, ServerStartupException, UnknownHostException
    {
        if (dataAreaType_ != DataArea.LOGICAL_DATA_AREA)
        {
            Trace.log(Trace.ERROR, "Programming error: write(boolean) was called when dataAreaType=" + dataAreaType_);
            throw new InternalErrorException (InternalErrorException.UNKNOWN, dataAreaType_);
        }

        // Build the string for the write.
        String wrtcmd = "QSYS/CHGDTAARA DTAARA(" + library_ + "/" + name_ + ") VALUE('" + (data ? "1" : "0") + "')";

        // Run the command
        if(!run(wrtcmd)) // CHGDTAARA is known to be not threadsafe
        {
            // Throw AS400MessageList
            processExceptions(getMessages());
        }
    }


    /**
     Writes the data to the data area.
     It writes the specified bytes to the data area, at offset <i>dataAreaOffset</i>.
     The first byte in the data area is at offset 0.
     @param data The data to be written.
     @param dataBufferOffset The starting offset in <tt>data</tt>.
     @param dataAreaOffset The offset in the data area at which to start writing. (0-based)
     @param dataLength The number of bytes to write.
     @exception AS400SecurityException          If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception InterruptedException            If this thread is interrupted.
     @exception IOException                     If an error occurs while communicating with the system.
     @exception ObjectDoesNotExistException     If the system object does not exist.
     **/
    public void write(byte[] data, int dataBufferOffset, int dataAreaOffset, int dataLength)
      throws AS400SecurityException,
    ErrorCompletingRequestException,
    InterruptedException,
    IOException,
    ObjectDoesNotExistException
    {
      // Assume the arguments have been validated by the public class.

      // Build the string for the write.
      String dataAreaIdentifier = null;
      switch (dataAreaType_)
      {
        case DataArea.CHARACTER_DATA_AREA:
          dataAreaIdentifier = library_ + "/" + name_;
          break;
        case DataArea.LOCAL_DATA_AREA:
          dataAreaIdentifier = "*LDA";
          break;
        default:
          Trace.log(Trace.ERROR, "Programming error: write(byte[],int,int,int) was called when dataAreaType=" + dataAreaType_);
          throw new InternalErrorException (InternalErrorException.UNKNOWN, dataAreaType_);
      }
      String wrtcmd = "QSYS/CHGDTAARA DTAARA(" + dataAreaIdentifier +
        " (" + (dataAreaOffset+1) + " " + dataLength + "))" +
        " VALUE(X'" + BinaryConverter.bytesToString(data, dataBufferOffset, dataLength) + "')";

      if (rmtCmd_ == null) {
        setupRemoteCommand();
      }

      if (Trace.isTraceOn()) {
        Trace.log(Trace.DIAGNOSTIC, "wrtcmd=["+wrtcmd+"]");
      }
      // Run the command as bytes
      boolean result = rmtCmd_.runCommand(wrtcmd); // CHGDTAARA is not threadsafe
      messageList_ = rmtCmd_.getMessageList();

      if(!result)
      {
        // Throw AS400MessageList
        processExceptions(getMessages());
      }
    }
}
