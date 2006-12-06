///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: DataAreaImplRemote.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others. All rights reserved.
//
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
  private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

    private AS400ImplRemote system_;  // The server where the data area is located.
    private String library_;  // The library that contains the data area.
    private String name_;  // The name of the data area.
    private QSYSObjectPathName ifsPathName_;  // The full path name of the data area.

    private byte[] dataAreaSystemPathName_;  // The name and library of the data area used in program call.
    private ConverterImplRemote converter_;  // The ccsid converter for this system.

    private int ccsid_;  // The ccsid for this system.
    private RemoteCommandImplRemote rmtCmd_;  // Impl object for remote command server.
    private AS400Message[] messageList_;  // The message list for the command object.

    private int length_;  // The maximum number of bytes the data area can contain.

    private boolean attributesRetrieved_;  // Flag indicating if this data area object contains current information regarding its corresponding server data area.
    private int dataAreaType_ = DataArea.UNINITIALIZED;  // Type of data area object.
    private static final QSYSObjectPathName PROGRAM_NAME = new QSYSObjectPathName("/QSYS.LIB/QWCRDTAA.PGM");

    // For DecimalDataArea only:
    private int decimalPositions_ = 5;  // The default number of decimal positions.

    /**
     Resets the character data area to contain default values (blank, zero, or false).
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ConnectionDroppedException  If the connection is dropped unexpectedly.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the server object does not exist.
     @exception  ServerStartupException  If the host server cannot be started.
     @exception  UnknownHostException  If the server cannot be located.
     **/
    public void clear() throws AS400SecurityException, ConnectionDroppedException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException, ServerStartupException, UnknownHostException
    {
        // Build the string for the write.
        // In the case of Character Data Area and Local Data Area,
        // if a substring starting position and length aren't specified,
        // the 400 assumes '*ALL' for the starting position and hence
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
                throw new InternalErrorException (InternalErrorException.UNEXPECTED_EXCEPTION);
        }

        // Run the command.
        if(!run(clrcmd, false))
        {
            // Throw AS400MessageList.
            processExceptions(getMessages());
        }
    }

    /**
     Creates a character data area with the specified attributes.
     @param  length  The maximum number of characters in the data area.  Valid values are 1 through 2000.
     @param  initialValue  The initial value for the data area.
     @param  textDescription  The text description for the data area.  The maximum length is 50 characters.
     @param  authority  The public authority level for the data area. Valid values are *ALL, *CHANGE, *EXCLUDE, *LIBCRTAUT, *USE, or the name of an authorization list.  The maximum length is 10 characters.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ConnectionDroppedException  If the connection is dropped unexpectedly.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectAlreadyExistsException  If the server object already exists.
     @exception  ObjectDoesNotExistException  If the server object does not exist.
     @exception  ServerStartupException  If the host server cannot be started.
     @exception  UnknownHostException  If the server cannot be located.
     **/
    public void create(int length, String initialValue, String textDescription, String authority) throws AS400SecurityException, ConnectionDroppedException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectAlreadyExistsException, ObjectDoesNotExistException, ServerStartupException, UnknownHostException
    {
        // Assume the arguments have been validated by the public class.
        if (dataAreaType_ != DataArea.CHARACTER_DATA_AREA)
        {
            Trace.log (Trace.ERROR, "Programming error: create(int,String,String,String) " +
                       "was called when dataAreaType=" + dataAreaType_);
            throw new InternalErrorException (InternalErrorException.UNEXPECTED_EXCEPTION);
        }

        length_ = length;

        // Build the string for the create.
        String crtcmd = "QSYS/CRTDTAARA DTAARA(" + library_ + "/" + name_ + ") TYPE(*CHAR) LEN(" + String.valueOf(length_) + " " + ") VALUE('" + initialValue + "') TEXT('" + textDescription + "')" + " AUT(" + authority + ")";

        // Run the command.
        if(!run(crtcmd, false))
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
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectAlreadyExistsException  If the server object already exists.
     @exception  ObjectDoesNotExistException  If the server object does not exist.
     @exception  ServerStartupException  If the host server cannot be started.
     @exception  UnknownHostException  If the server cannot be located.
     **/
    public void create(int length, int decimalPositions, BigDecimal initialValue, String textDescription, String authority) throws AS400SecurityException, ConnectionDroppedException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectAlreadyExistsException, ObjectDoesNotExistException, ServerStartupException, UnknownHostException
    {
        // Assume the arguments have been validated by the public class.
        if (dataAreaType_ != DataArea.DECIMAL_DATA_AREA)
        {
            Trace.log(Trace.ERROR, "Programming error: create(int,int,BigDecimal,String,String) was called when dataAreaType=" + dataAreaType_);
            throw new InternalErrorException (InternalErrorException.UNEXPECTED_EXCEPTION);
        }

        length_ = length;
        decimalPositions_ = decimalPositions;

        // Build the string for the create.
        String crtcmd = "QSYS/CRTDTAARA DTAARA(" + library_ + "/" + name_ + ") TYPE(*DEC) LEN(" + String.valueOf(length_) + " " + String.valueOf(decimalPositions_) + ") VALUE(" + initialValue.toString() + ") TEXT('" + textDescription + "')" + " AUT(" + authority + ")";

        // Run the command.
        if(!run(crtcmd, false))
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
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectAlreadyExistsException  If the server object already exists.
     @exception  ObjectDoesNotExistException  If the server object does not exist.
     @exception  ServerStartupException  If the host server cannot be started.
     @exception  UnknownHostException  If the server cannot be located.
     **/
    public void create(boolean initialValue, String textDescription, String authority) throws AS400SecurityException, ConnectionDroppedException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectAlreadyExistsException, ObjectDoesNotExistException, ServerStartupException, UnknownHostException
    {
        // Assume the arguments have been validated by the public class.
        if (dataAreaType_ != DataArea.LOGICAL_DATA_AREA)
        {
            Trace.log (Trace.ERROR, "Programming error: create(boolean,String,String) was called when dataAreaType=" + dataAreaType_);
            throw new InternalErrorException (InternalErrorException.UNEXPECTED_EXCEPTION);
        }

        // Build the string for the create.
        String crtcmd = "QSYS/CRTDTAARA DTAARA(" + library_ + "/" + name_ + ") TYPE(*LGL) LEN(1) VALUE('" + (initialValue ? "1" : "0") + "') TEXT('" + textDescription + "')" + " AUT(" + authority + ")";

        // Run the command.
        if(!run(crtcmd, false))
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
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the object does not exist.
     @exception  ServerStartupException  If the host server cannot be started.
     @exception  UnknownHostException  If the server cannot be located.
     **/
    public void delete() throws AS400SecurityException, ConnectionDroppedException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException, ServerStartupException, UnknownHostException
    {
        if (dataAreaType_ == DataArea.LOCAL_DATA_AREA)
        {
            Trace.log(Trace.ERROR, "Programming error: delete() was called when dataAreaType=" + dataAreaType_);
            throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
        }

        // Do the delete.
        String dltcmd = "QSYS/DLTDTAARA DTAARA(" + library_ + "/" + name_ + ")";

        // Run the command.
        if(!run(dltcmd, true))
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
     @exception  IllegalObjectTypeException  If the server object is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the server object does not exist.
     **/
    public int getDecimalPositions() throws AS400SecurityException, ErrorCompletingRequestException, IllegalObjectTypeException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (dataAreaType_ != DataArea.DECIMAL_DATA_AREA)
        {
            Trace.log(Trace.ERROR, "Programming error: create(int,String,String,String) was called when dataAreaType=" + dataAreaType_);
            throw new InternalErrorException (InternalErrorException.UNEXPECTED_EXCEPTION);
        }

        if (!attributesRetrieved_)
            retrieveAttributes();

        return decimalPositions_;
    }

    /**
     Returns the size of the data area.
     @return  The size of the data area.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IllegalObjectTypeException  If the server object is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the server object does not exist.
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
    void processExceptions(AS400Message[] messageList) throws AS400SecurityException, ObjectDoesNotExistException, AS400Exception
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
     @exception  IllegalObjectTypeException  If the server object is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the server object does not exist.
     **/
    // Note that doing a read() will also set the attributes of this
    // object to what is returned from the 400, namely the length and
    // number of decimal positions.
    public BigDecimal readBigDecimal() throws AS400SecurityException, ErrorCompletingRequestException, IllegalObjectTypeException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (dataAreaType_ != DataArea.DECIMAL_DATA_AREA)
        {
            Trace.log (Trace.ERROR, "Programming error: readBigDecimal() was called when dataAreaType=" + dataAreaType_);
            throw new InternalErrorException (InternalErrorException.UNEXPECTED_EXCEPTION);
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
     @exception  IllegalObjectTypeException  If the server object is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the server object does not exist.
     **/
    public boolean readBoolean() throws AS400SecurityException, ErrorCompletingRequestException, IllegalObjectTypeException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (dataAreaType_ != DataArea.LOGICAL_DATA_AREA)
        {
            Trace.log(Trace.ERROR, "Programming error: readBoolean() was called when dataAreaType=" + dataAreaType_);
            throw new InternalErrorException (InternalErrorException.UNEXPECTED_EXCEPTION);
        }

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
        // 0 - Bin 4 - Bytes available
        // 4 - Bin 4 - Bytes returned
        // 8 - Char 10 - Type of value returned
        // 18 - Char 10 - Library name
        // 28 - Bin 4 - Length of value returned
        // 32 - Bin 4 - Number of decimal positions
        // 36 - Char * - Value
        byte[] dataReceived = new byte[37];
        parmlist[0] = new ProgramParameter(dataReceived.length);

        // Second parameter: input, length of receiver variable
        byte[] receiverLength = new byte[4];
        BinaryConverter.intToByteArray(dataReceived.length, receiverLength, 0);
        parmlist[1] = new ProgramParameter(receiverLength);

        // Third parameter: input, qualified data area name
        parmlist[2] = new ProgramParameter(dataAreaSystemPathName_);

        // Fourth parameter: input, starting position
        // 1 through 2000; -1 retrieves all
        byte[] startingPosition = new byte[4];
        BinaryConverter.intToByteArray(-1, startingPosition, 0);
        parmlist[3] = new ProgramParameter(startingPosition);

        // Fifth parameter: input, length of data
        byte[] lengthOfData = new byte[4];
        BinaryConverter.intToByteArray(1, lengthOfData, 0);
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
        // A value >=8 of bytes provided means the 400 will return exception
        // information on the parameter instead of throwing an exception
        // to the application. We provide 0 to ensure it does indeed
        // throw us an exception.
        BinaryConverter.intToByteArray(0, errorCode, 0);
        parmlist[5] = new ProgramParameter(errorCode, 17);

        // Create the pgm call object
        if (rmtCmd_ == null)
        {
            rmtCmd_ = new RemoteCommandImplRemote();
            rmtCmd_.setSystem(system_);
        }

        // Run the program.  Failure is returned as a message list.
        if(!rmtCmd_.runProgram("QSYS", "QWCRDTAA", parmlist, false, AS400Message.MESSAGE_OPTION_UP_TO_10))  // QWCRDTAA isn't threadsafe. $B1C
        {
            // Throw AS400MessageList
            processExceptions(rmtCmd_.getMessageList());
        }

        // Get the data returned from the program
        dataReceived = parmlist[0].getOutputData();
        if (Trace.isTraceOn())
        {
            Trace.log(Trace.DIAGNOSTIC, "Logical data area data retrieved:", dataReceived);
        }

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
     Refreshes the attributes of the data area.
     This method should be called if the underlying server data area has changed and it is desired that this object should reflect those changes.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IllegalObjectTypeException  If the server object is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the server object does not exist.
     **/
    public void refreshAttributes() throws AS400SecurityException, ErrorCompletingRequestException, IllegalObjectTypeException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        attributesRetrieved_ = false;
        retrieveAttributes();
    }

    //$D2C
    /**
     Makes the API call to retrieve the character data area data and attributes.
     @return The String value read from the data area as a result of retrieving the data area's attributes.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IllegalObjectTypeException  If the server object is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the server object does not exist.
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
     @param stringType The Data Area bidi string type, as defined by the CDRA (Character
                 Data Representataion Architecture). See <a href="BidiStringType.html">
                 BidiStringType</a> for more information and valid values.
     @return The String value read from the data area as a result of retrieving the data area's attributes.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IllegalObjectTypeException  If the server object is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the server object does not exist.
     **/
    public String retrieve(int offset, int length, int stringType) throws AS400SecurityException, ErrorCompletingRequestException, IllegalObjectTypeException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (dataAreaType_ != DataArea.CHARACTER_DATA_AREA && dataAreaType_ != DataArea.LOCAL_DATA_AREA)
        {
            Trace.log(Trace.ERROR, "Programming error: retrieve(int,int) was called when dataAreaType=" + dataAreaType_);
            throw new InternalErrorException (InternalErrorException.UNEXPECTED_EXCEPTION);
        }

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
        // 0 - Bin 4 - Bytes available
        // 4 - Bin 4 - Bytes returned
        // 8 - Char 10 - Type of value returned
        // 18 - Char 10 - Library name
        // 28 - Bin 4 - Length of value returned
        // 32 - Bin 4 - Number of decimal positions
        // 36 - Char * - Value
        byte[] dataReceived = new byte[36+4001];
        // 36 from above and max of 4001 from data area
        // With a max of 2000 characters, the worst case
        // scenario is that every other character is DBCS
        // (i.e. for a mixed byte Unicode String)
        // so that there would be a SI/SO, like this:
        // SI-1-SO  2  SI-3-SO  4  SI-5-SO  etc...
        // So for 3 initial chars, you get 7 bytes;
        // for 5 initial chars, you get 11 bytes; etc.
        // This equates to:
        // max # bytes needed = (2 * initial_chars) + 1

        parmlist[0] = new ProgramParameter(dataReceived.length);

        // Second parameter: input, length of receiver variable
        byte[] receiverLength = new byte[4];
        BinaryConverter.intToByteArray(dataReceived.length, receiverLength, 0);
        parmlist[1] = new ProgramParameter(receiverLength);

        // Third parameter: input, qualified data area name
        parmlist[2] = new ProgramParameter(dataAreaSystemPathName_);

        // Fourth parameter: input, starting position
        // 1 through 2000; -1 retrieves all
        byte[] startingPosition = new byte[4];
        BinaryConverter.intToByteArray(offset, startingPosition, 0);
        parmlist[3] = new ProgramParameter(startingPosition);

        // Fifth parameter: input, length of data
        byte[] lengthOfData = new byte[4];
        BinaryConverter.intToByteArray(length, lengthOfData, 0);
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
        // A value >=8 of bytes provided means the 400 will return exception
        // information on the parameter instead of throwing an exception
        // to the application. We provide 0 to ensure it does indeed
        // throw us an exception.
        BinaryConverter.intToByteArray(0, errorCode, 0);
        parmlist[5] = new ProgramParameter(errorCode, 17);

        // Create the pgm call object
        if (rmtCmd_ == null)
        {
            rmtCmd_ = new RemoteCommandImplRemote();
            rmtCmd_.setSystem(system_);
        }

        // Run the program.  Failure is returned as a message list.
        if(!rmtCmd_.runProgram("QSYS", "QWCRDTAA", parmlist, false, AS400Message.MESSAGE_OPTION_UP_TO_10))  // QWCRDTAA isn't threadsafe. $B1C
        {
            // Throw AS400MessageList
            processExceptions(rmtCmd_.getMessageList());
        }

        // Get the data returned from the program
        dataReceived = parmlist[0].getOutputData();
        if (Trace.isTraceOn() && Trace.isTraceDiagnosticOn())
        {
            Trace.log(Trace.DIAGNOSTIC, "Character data area data retrieved:", dataReceived);
        }
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
     @exception  IllegalObjectTypeException  If the server object is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the server object does not exist.
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
                throw new InternalErrorException (InternalErrorException.UNEXPECTED_EXCEPTION);
        }
    }

    /**
     Retrieves/refreshes the logical data area's attributes.
     @return  The BigDecimal value read from the data area as a result of retrieving the data area's attributes.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IllegalObjectTypeException  If the server object is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the server object does not exist.
     **/
    private Object retrieveAttributesBigDecimal() throws AS400SecurityException, ErrorCompletingRequestException, IllegalObjectTypeException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        // For a decimal data area, need to get the length & decimal positions
        // But since this is the only API available to us, we might as well
        // do a read of the data too.

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
        // 0 - Bin 4 - Bytes available               0 0 0 44
        // 4 - Bin 4 - Bytes returned                0 0 0 44
        // 8 - Char 10 - Type of value returned      92 -60 -59 -61 64 64 64 64 64 64
        // 18 - Char 10 - Library name               -61 -30 -44 -55 -29 -56 64 64 64 64
        // 28 - Bin 4 - Length of value returned     0 0 0 15
        // 32 - Bin 4 - Number of decimal positions  0 0 0 5
        // 36 - Char * - Value
        byte[] dataReceived = new byte[60]; // 36 from above and max of 24 digits from data area
        parmlist[0] = new ProgramParameter(dataReceived.length);

        // Second parameter: input, length of receiver variable
        byte[] receiverLength = new byte[4];
        BinaryConverter.intToByteArray(dataReceived.length, receiverLength, 0);
        parmlist[1] = new ProgramParameter(receiverLength);

        // Third parameter: input, qualified data area name
        parmlist[2] = new ProgramParameter(dataAreaSystemPathName_);

        // Fourth parameter: input, starting position
        // 1 through 2000; -1 retrieves all
        byte[] startingPosition = new byte[4];
        BinaryConverter.intToByteArray(-1, startingPosition, 0);
        parmlist[3] = new ProgramParameter(startingPosition);

        // Fifth parameter: input, length of data
        byte[] lengthOfData = new byte[4];
        BinaryConverter.intToByteArray(24, lengthOfData, 0);
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
        // A value >=8 of bytes provided means the 400 will return exception
        // information on the parameter instead of throwing an exception
        // to the application. We provide 0 to ensure it does indeed
        // throw us an exception.
        BinaryConverter.intToByteArray(0, errorCode, 0);
        parmlist[5] = new ProgramParameter(errorCode, 17);

        // Create the pgm call object
        if (rmtCmd_ == null)
        {
            rmtCmd_ = new RemoteCommandImplRemote();
            rmtCmd_.setSystem(system_);
        }

        // Run the program.  Failure is returned as a message list.
        if(!rmtCmd_.runProgram("QSYS", "QWCRDTAA", parmlist, false, AS400Message.MESSAGE_OPTION_UP_TO_10))  // QWCRDTAA isn't threadsafe. $B1C
        {
            // Throw AS400MessageList
            processExceptions(rmtCmd_.getMessageList());
        }

        // Get the data returned from the program
        dataReceived = parmlist[0].getOutputData();
        if (Trace.isTraceOn() && Trace.isTraceDiagnosticOn())
        {
            Trace.log(Trace.DIAGNOSTIC, "Decimal data area data retrieved:", dataReceived);
        }

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
     @exception  IllegalObjectTypeException  If the server object is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the server object does not exist.
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
     @exception  IllegalObjectTypeException  If the server object is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the server object does not exist.
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
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ServerStartupException  If the host server cannot be started.
     @exception  UnknownHostException  If the server cannot be located.
     **/
    private boolean run(String command, boolean threadSafe) throws AS400SecurityException, ConnectionDroppedException, ErrorCompletingRequestException, InterruptedException, IOException, ServerStartupException, UnknownHostException
    {
        boolean result = false;
        if (rmtCmd_ == null)
        {
            rmtCmd_ = new RemoteCommandImplRemote();
            rmtCmd_.setSystem(system_);
        }
        result = rmtCmd_.runCommand(command, threadSafe, AS400Message.MESSAGE_OPTION_UP_TO_10);      // @B2C
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
     @param  system  The server on which the data area exists.
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
                throw new InternalErrorException (InternalErrorException.UNEXPECTED_EXCEPTION);
        }
        dataAreaType_ = dataAreaType;
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
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the server object does not exist.
     @exception  ServerStartupException  If the host server cannot be started.
     @exception  UnknownHostException  If the server cannot be located.
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
                 Data Representataion Architecture). See <a href="BidiStringType.html">
                 BidiStringType</a> for more information and valid values.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ConnectionDroppedException  If the connection is dropped unexpectedly.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the server object does not exist.
     @exception  ServerStartupException  If the host server cannot be started.
     @exception  UnknownHostException  If the server cannot be located.
     **/
    public void write(String data, int dataAreaOffset, int type) throws AS400SecurityException, ConnectionDroppedException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException, ServerStartupException, UnknownHostException
    {
        // Assume the arguments have been validated by the public class.
        if (dataAreaType_ != DataArea.CHARACTER_DATA_AREA && dataAreaType_ != DataArea.LOCAL_DATA_AREA)
        {
            Trace.log (Trace.ERROR, "Programming error: write(String,int) was called when dataAreaType=" + dataAreaType_);
            throw new InternalErrorException (InternalErrorException.UNEXPECTED_EXCEPTION);
        }

        // Build the string for the write.
        //String wrtcmd = null;
        byte[] wrtcmd;

        // convert to get the actual number of bytes being written.  In mixed
        // environments a character can expand to more than one byte.  The
        // number of byte is passed on the command.
        ConverterImplRemote ir = ConverterImplRemote.getConverter(system_.getCcsid(), system_); //@D1a

        // To allow bidi data to be written to a data area, each
        // part (the beginning of the command, the data, the end of the commmand)
        // must be converted into bytes and passed to command call.
        byte[] part1;                                                                                     //@D2A
        byte[] part2 = ir.stringToByteArray(data, type);                                          //@D1a  //$D2C
        byte[] part3 = ir.stringToByteArray("')");                                                        //@D2A
        int dataLength = part2.length;                                                                    //@D2C

        switch (dataAreaType_)
        {
            case DataArea.CHARACTER_DATA_AREA:
                //wrtcmd = "QSYS/CHGDTAARA DTAARA(" + library_ + "/" + name_ + " (" + (dataAreaOffset+1) + " " + dataLength + ")" + ") VALUE('" + data + "')"; //@D1c
                part1 = ir.stringToByteArray("QSYS/CHGDTAARA DTAARA(" + library_ + "/" + name_ + " (" + (dataAreaOffset+1) + " " + dataLength + ")" + ") VALUE('");  //@D2C
                break;
            case DataArea.LOCAL_DATA_AREA:
                //wrtcmd = "QSYS/CHGDTAARA DTAARA(*LDA (" + (dataAreaOffset+1) + " " + dataLength + ")) VALUE('" + data + "')";                                //@D1c
                part1 = ir.stringToByteArray("QSYS/CHGDTAARA DTAARA(*LDA (" + (dataAreaOffset+1) + " " + dataLength + ")) VALUE('");                                 //@D2C
                break;
            default:
                Trace.log(Trace.ERROR, "Programming error: write(String,int) was called as dataAreaType=" + dataAreaType_);
                throw new InternalErrorException (InternalErrorException.UNEXPECTED_EXCEPTION);
        }

        // Allocate the proper byte array size for the command.
        wrtcmd = new byte[part1.length + dataLength + part3.length];                //@D2A

        // Copy each part of the chgdtaara command bytes into the end byte array.
        System.arraycopy(part1,0,wrtcmd,0,part1.length);                            //@D2A
        System.arraycopy(part2,0,wrtcmd,part1.length,dataLength);                   //@D2A
        System.arraycopy(part3,0,wrtcmd,part1.length+dataLength,part3.length);      //@D2A

        if (rmtCmd_ == null)                                    //$D2A
        {                                                       //$D2A
            rmtCmd_ = new RemoteCommandImplRemote();            //$D2A
            rmtCmd_.setSystem(system_);                         //$D2A
        }                                                       //$D2A

        // Run the command as bytes
        boolean result = rmtCmd_.runCommand(wrtcmd, false, AS400Message.MESSAGE_OPTION_UP_TO_10);     //@D2C
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
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the server object does not exist.
     @exception  ServerStartupException  If the host server cannot be started.
     @exception  UnknownHostException  If the server cannot be located.
     **/
    public void write(BigDecimal data) throws AS400SecurityException, ConnectionDroppedException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException, ServerStartupException, UnknownHostException
    {
        // Assume the argument has been validated by the public class.
        if (dataAreaType_ != DataArea.DECIMAL_DATA_AREA)
        {
            Trace.log (Trace.ERROR, "Programming error: write(BigDecimal) was called when dataAreaType=" + dataAreaType_);
            throw new InternalErrorException (InternalErrorException.UNEXPECTED_EXCEPTION);
        }

        // Build the string for the write.
        String wrtcmd = "QSYS/CHGDTAARA DTAARA(" + library_ + "/" + name_ + ") VALUE(" + data.toString() + ")";

        // Run the command
        if(!run(wrtcmd, false))
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
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the server object does not exist.
     @exception  ServerStartupException  If the host server cannot be started.
     @exception  UnknownHostException  If the server cannot be located.
     **/
    public void write(boolean data) throws AS400SecurityException, ConnectionDroppedException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException, ServerStartupException, UnknownHostException
    {
        if (dataAreaType_ != DataArea.LOGICAL_DATA_AREA)
        {
            Trace.log(Trace.ERROR, "Programming error: write(boolean) was called when dataAreaType=" + dataAreaType_);
            throw new InternalErrorException (InternalErrorException.UNEXPECTED_EXCEPTION);
        }

        // Build the string for the write.
        String wrtcmd = "QSYS/CHGDTAARA DTAARA(" + library_ + "/" + name_ + ") VALUE('" + (data ? "1" : "0") + "')";

        // Run the command
        if(!run(wrtcmd, false))
        {
            // Throw AS400MessageList
            processExceptions(getMessages());
        }
    }
}
